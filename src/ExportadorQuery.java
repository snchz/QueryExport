import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.StringTokenizer;




public class ExportadorQuery {
	private String _fileOutput, _fileError, _fileEjecutando, _fileOK, _fileRequisito, _driver, _url, _user, _pass;
	private LinkedHashMap<String, String> _querys; //utilizar LinkedHashMap para a�adir las querys en FIFO y determinar el orden que queremos en las hojas
	private String _fileConfig;						//Archivo de configuracion con la ejecucion a realizar
	private Fichero _ficheroSalida,_ficheroError,_ficheroEjecutando, _ficheroOK, _ficheroRequisito;
	private boolean parametrosLeidos;
	
	
	private Fichero.Extensiones _extensionSalida, _compresion;
	
	
	/**
	 * Constructor con par�metro del archivo de configuraci�n
	 * El fichero debe tener extension .config
	 * @param configFile Fichero de Configuraci�n
	 */
	public ExportadorQuery(String configFile) {
		_fileConfig = configFile;	//Contenido parecido a querys\\archivo.config
		parametrosLeidos=leerParametros();
	}

	/**
	 * Metodo principal. Lee el archivo de configuraci�n y ejecuta la query solicitada.
	 */
	public String Ejecutar(){
		String res=null;
		System.out.println("[...] Leyendo parametros...");
		if (parametrosLeidos){
			_ficheroEjecutando=new Fichero(_fileEjecutando);
			_ficheroRequisito=new Fichero(_fileRequisito);
			_ficheroOK=new Fichero(_fileOK);
			if(!_ficheroEjecutando.existeFichero() && _ficheroRequisito.existeFichero() && !_ficheroOK.existeFichero()){
				_ficheroEjecutando.crearFicheroYCerrarlo();
				
				_ficheroError=new Fichero(_fileError);
				if (!_ficheroError.existeFichero()){
					
					System.out.println("[...] Escribiendo fichero (extension " + _extensionSalida.toString() + " )...");
					_ficheroSalida=new Fichero(_fileOutput);
					Combo combo_res=ObtenerResulsetQuery();
					int registros=combo_res.numeroRegistros;
					System.out.println("[...] Se han escrito "+registros+" registros.");
					if (registros==0){
						_ficheroSalida.borrarFichero();
						res="SIN DATOS";
					}else if (registros==1){
						_ficheroOK.crearFicheroYCerrarlo();
						res="OK "+combo_res.resultado;
					}else if (registros>1){
						_ficheroOK.crearFicheroYCerrarlo();
						res="MULTIDATOS ("+registros+" registros)";
					}
				}else{
					System.out.println("[...] Est� en error.");
					res=null;
				}
				System.out.println("[...] Compresi�n solicitada: " + _compresion.toString());
				if (_compresion == Fichero.Extensiones.zip && _ficheroSalida.existeFichero()) {
					System.out.println("[...] Comprimiendo fichero...");
					_ficheroSalida.comprimir(_compresion);
					System.out.println("[...] Borrando fichero origen...");
					_ficheroSalida.borrarFichero();
				}
				System.out.println("[...] Borrando fichero testigo Ejecutando...");
				_ficheroEjecutando.borrarFichero();
			}else{
				System.out.println("[...] Ya est� ejecutando o el fichero requisito no existe o el fichero ok ya existe.");
				res=null;
			}					
		}
		return res;
	}

	/**
	 * Lee los par�metros del fichero de configuraci�n.
	 * Linea "DRIVER=". Driver de base de datos. Por ejemplo: oracle.jdbc.driver.OracleDriver
	 * Linea "CONEXION=". Url de conexion a la base de datos. Por ejemplo: jdbc:oracle:thin:@localhost:1521:mkyong
	 * Linea "USUARIO=". Usuario de conexion a la base de datos.
	 * Linea "PASSWORD=". Password de conexion a la base de datos.
	 * Linea "FORMATO_SALIDA=". Extension del fichero de salida de la consulta. En funcion del tipo de extension de salida hace una cosa u otra
	 *          Si es .ok genera el fichero solo el numero de registros de la consulta es mayor a 0 (devuelve datos)
	 * Linea "QUERY=". Query a ejecutar.
	 * Linea "MULTI_QUERY=". Si en lugar de una query, se quiere ejecutar m�s de una para guardarlas en una excel en distintas hojas, utilizar
	 * 			este parametro en lugar de QUERY. Separar las querys por ";". Es necesario informar el parametro MULTI_SALIDA para dar nombre a cada hoja.
	 * Linea "MULTI_SALIDA=". Separar por ";" cada una de las hojas que corresponde con cada una de las querys del parametro MULTI_QUERY.
	 * Linea "COMPRESION=". Si es ZIP, el archivo destino se comprime. En otro caso se deja sin comprimir
	 * Linea "FICHERO_CONDICION=". Fichero ok. Si este fichero no existe, no se ejecuta.
	 */
	private boolean leerParametros(){
		boolean res=false;
		try {
			if (!_fileConfig.contains(".config")){
				throw new Exception("Error extension de fichero de configuracion incorrecta " + _fileConfig);
			}
			Configuracion config=new Configuracion(_fileConfig);
			//DRIVER
			_driver = config.obtenerValorConfiguracion(Configuracion.DRIVER);
			//CONEXION
			_url = config.obtenerValorConfiguracion(Configuracion.CONEXION);
			//USUARIO
			_user = config.obtenerValorConfiguracion(Configuracion.USUARIO);
			//PASSWORD
			_pass = config.obtenerValorConfiguracion(Configuracion.PASSWORD);
			//EXTENSION SALIDA
			String extension = config.obtenerValorConfiguracion(Configuracion.FORMATO_SALIDA);
			try {
				if (extension==null)
					_extensionSalida = Fichero.Extensiones.none;
				else if (extension.equals(Fichero.Extensiones.csv.toString()))
					_extensionSalida = Fichero.Extensiones.csv;
				else if (extension.equals(Fichero.Extensiones.ok.toString()))
					_extensionSalida = Fichero.Extensiones.ok;
				else if (extension.equals(Fichero.Extensiones.xlsx.toString()))
					_extensionSalida = Fichero.Extensiones.xlsx;
				else
					_extensionSalida = Fichero.Extensiones.none;
			} catch (Exception e) {
				_extensionSalida = Fichero.Extensiones.none;
				throw new Exception("Error en la linea de extension inv�lida: "+extension);
			}
			//NOMBRE DE FICHEROS
			_fileOutput=_fileConfig.replaceAll("."+Fichero.Extensiones.config.toString(), "."+_extensionSalida.toString());
			_fileError=_fileConfig.replaceAll("."+Fichero.Extensiones.config.toString(), "."+Fichero.Extensiones.error.toString());
			_fileEjecutando=_fileConfig.replaceAll("."+Fichero.Extensiones.config.toString(), "."+Fichero.Extensiones.ejecutando.toString());
			_fileOK=_fileConfig.replaceAll("."+Fichero.Extensiones.config.toString(), "."+Fichero.Extensiones.ok.toString());
			//QUERY
			String query = config.obtenerValorConfiguracion(Configuracion.QUERY);
			String querys = config.obtenerValorConfiguracion(Configuracion.MULTI_QUERY);
			String hojas = config.obtenerValorConfiguracion(Configuracion.MULTI_SALIDA);
			_querys=new LinkedHashMap<String, String>();
			if (query!=null){
				_querys.put("Hoja1", query);
			}else if (querys!=null){
				StringTokenizer querys_list=new StringTokenizer(querys, ";");
				StringTokenizer hojas_list=new StringTokenizer(hojas, ";");
				if (hojas_list.countTokens()!=querys_list.countTokens())
					throw new Exception("No cuadra el numero de token de las querys con el numero de tokens de las salidas. Revisa los parametros "+Configuracion.MULTI_QUERY+" y "+Configuracion.MULTI_SALIDA);
				while (hojas_list.hasMoreTokens()){
					_querys.put(hojas_list.nextToken(), querys_list.nextToken());
				}
			}else{
				throw new Exception("No hay querys para lanzar. Haz uso de los parametros "+Configuracion.QUERY+" o "+Configuracion.MULTI_QUERY);
			}
			//COMPRESION
			String compresion=config.obtenerValorConfiguracion(Configuracion.COMPRESION);
			if (compresion==null)
				_compresion = Fichero.Extensiones.none;
			else if (compresion.equals(Fichero.Extensiones.zip.toString()))
				_compresion = Fichero.Extensiones.zip;
			else
				_compresion = Fichero.Extensiones.none;
			//FICHERO CONDICION
			_fileRequisito= config.obtenerValorConfiguracion(Configuracion.FICHERO_CONDICION);
			//De no haber fichero de requisito porque no sea necesario, ponemos a si mismo
			if (_fileRequisito==null)
				_fileRequisito=_fileConfig;
			else if (_fileRequisito.trim().equals(""))
				_fileRequisito=_fileConfig;
			
			res=true;
		} catch (Exception e) {
			System.err.println("Error al leer el archivo de parametros " + _fileConfig + "\n\tDetalles: " + e.getMessage());
			e.printStackTrace();
			res=false;
		}
		return res;
	}
	
	private static class Combo {
		   String resultado;
		   int numeroRegistros;
	}

	/**
	 * Ejecuta la query y la guarda a fichero.
	 * @return -1 si error, 0 si ok, numero mayor que 0 el numero de registros
	 */
	private Combo ObtenerResulsetQuery(){
		Statement stmt = null;
		Connection conn = null;
		ResultSet rs = null;
		Combo res=new Combo();
		res.numeroRegistros=0;
		res.resultado="";
		try {
			System.out.println("[Paso 1 de 4] Buscando driver " + _driver + "...");
			Class.forName(_driver);
			System.out.println("[...........] Driver " + _driver + " encontrado.");
			System.out.println("[Paso 2 de 4] Realizando conexion " + _url + "...");
			conn = DriverManager.getConnection(_url, _user, _pass);
			System.out.println("[...........] Conexion " + _url + " realizada.");
			stmt = conn.createStatement();
			
			_ficheroSalida.crearFichero();
			for (String hoja:_querys.keySet()){
				System.out.println("[Paso 3 de 4] Lanzando query... Hoja: "+hoja);
				rs = stmt.executeQuery(_querys.get(hoja));
				System.out.println("[...........] Query obtenida.");
				System.out.println("[Paso 3 de 4] Guardando a fichero...");
				
				ResultSetMetaData metaData = rs.getMetaData();
				Integer columnCount = metaData.getColumnCount();
				
				// cabecera
				List<String> row = new ArrayList<String>();
				for (int i = 1; i <= columnCount; i++)
					row.add(metaData.getColumnName(i).toString());
				_ficheroSalida.escribirLinea(row.toArray(new String[row.size()]),hoja);
				
				// datos
				while (rs.next()) {
					res.numeroRegistros++;
					row = new ArrayList<String>();
					for (int i = 1; i <= columnCount; i++){
						if (rs.getObject(i)==null)//Si llega un nulo escribe la palabra (null)
							row.add("(null)");
						else
							row.add(rs.getObject(i).toString());
					}
					_ficheroSalida.escribirLinea(row.toArray(new String[row.size()]),hoja);
					res.resultado=row.get(0);
				}
			}
			System.out.println("[...........] Fichero generado.");
			_ficheroSalida.cerrarConexiones();
		} catch (ClassNotFoundException e) {
			res.numeroRegistros=-1;
			_ficheroError.crearFicheroYCerrarlo();
			System.err.println("Error Libreria JAVA para Base de datos no encontrada." + _fileOutput + "\n\tDetalles: "
					+ e.getMessage());
		} catch (SQLException e) {
			res.numeroRegistros=-1;
			_ficheroError.crearFicheroYCerrarlo();
			System.out.println("Error al conectar. Credenciles o query incorrectas." + _fileOutput + "\n\tDetalles: "
					+ e.getMessage());
		} finally {
				try {
					if (rs != null)
						rs.close();
					if (stmt != null)
						stmt.close();
					if (conn != null)
						conn.close();
				} catch (SQLException e) {
					System.out.println("Error al cerrar conexiones." + _fileOutput + "\n\tDetalles: "
							+ e.getMessage());
				}
		}
		return res;
	}
}
