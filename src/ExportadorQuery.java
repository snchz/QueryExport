import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

public class ExportadorQuery {
	private String _fileOutput, _fileError, _fileEjecutando, _fileOK, _fileRequisito, _driver, _url, _user, _pass;
	private HashMap<String,String> _querys;
	private String _fileConfig;						//Archivo de configuracion con la ejecucion a realizar
	private Fichero _ficheroSalida,_ficheroError,_ficheroEjecutando, _ficheroOK, _ficheroRequisito;
	private boolean parametrosLeidos;
	
	private enum EXTENSION {					//Tipos de extensiones admitidas
		zip, csv, xlsx, error, ejecutando, config, ok, none
	};
	private EXTENSION _extensionSalida, _compresion;
	
	
	/**
	 * Constructor con parámetro del archivo de configuración
	 * El fichero debe tener extension .config
	 * @param configFile Fichero de Configuración
	 */
	public ExportadorQuery(String configFile) {
		_fileConfig = configFile;	//Contenido parecido a querys\\archivo.config
		parametrosLeidos=leerParametros();
	}

	/**
	 * Metodo principal. Lee el archivo de configuración y ejecuta la query solicitada.
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
					System.out.println("[...] Está en error.");
					res=null;
				}
				System.out.println("[...] Compresión solicitada: " + _compresion.toString());
				if (_compresion == EXTENSION.zip && _ficheroSalida.existeFichero()) {
					System.out.println("[...] Comprimiendo fichero...");
					_ficheroSalida.comprimir();
					System.out.println("[...] Borrando fichero origen...");
					_ficheroSalida.borrarFichero();
				}
				System.out.println("[...] Borrando fichero testigo Ejecutando...");
				_ficheroEjecutando.borrarFichero();
			}else{
				System.out.println("[...] Ya está ejecutando o el fichero requisito no existe o el fichero ok ya existe.");
				res=null;
			}					
		}
		return res;
	}

	/**
	 * Lee los parámetros del fichero de configuración.
	 * Linea "DRIVER=". Driver de base de datos. Por ejemplo: oracle.jdbc.driver.OracleDriver
	 * Linea "CONEXION=". Url de conexion a la base de datos. Por ejemplo: jdbc:oracle:thin:@localhost:1521:mkyong
	 * Linea "USUARIO=". Usuario de conexion a la base de datos.
	 * Linea "PASSWORD=". Password de conexion a la base de datos.
	 * Linea "FORMATO_SALIDA=". Extension del fichero de salida de la consulta. En funcion del tipo de extension de salida hace una cosa u otra
	 *          Si es .ok genera el fichero solo el numero de registros de la consulta es mayor a 0 (devuelve datos)
	 * Linea "QUERY=". Query a ejecutar.
	 * Linea "MULTI_QUERY=". Si en lugar de una query, se quiere ejecutar más de una para guardarlas en una excel en distintas hojas, utilizar
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
					_extensionSalida = EXTENSION.none;
				else if (extension.equals(EXTENSION.csv.toString()))
					_extensionSalida = EXTENSION.csv;
				else if (extension.equals(EXTENSION.ok.toString()))
					_extensionSalida = EXTENSION.ok;
				else if (extension.equals(EXTENSION.xlsx.toString()))
					_extensionSalida = EXTENSION.xlsx;
				else
					_extensionSalida = EXTENSION.none;
			} catch (Exception e) {
				_extensionSalida = EXTENSION.none;
				throw new Exception("Error en la linea de extension inválida: "+extension);
			}
			//NOMBRE DE FICHEROS
			_fileOutput=_fileConfig.replaceAll("."+EXTENSION.config.toString(), "."+_extensionSalida.toString());
			_fileError=_fileConfig.replaceAll("."+EXTENSION.config.toString(), "."+EXTENSION.error.toString());
			_fileEjecutando=_fileConfig.replaceAll("."+EXTENSION.config.toString(), "."+EXTENSION.ejecutando.toString());
			_fileOK=_fileConfig.replaceAll("."+EXTENSION.config.toString(), "."+EXTENSION.ok.toString());
			//QUERY
			String query = config.obtenerValorConfiguracion(Configuracion.QUERY);
			String querys = config.obtenerValorConfiguracion(Configuracion.MULTI_QUERY);
			String hojas = config.obtenerValorConfiguracion(Configuracion.MULTI_SALIDA);
			_querys=new HashMap<String, String>();
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
				_compresion = EXTENSION.none;
			else if (compresion.equals(EXTENSION.zip.toString()))
				_compresion = EXTENSION.zip;
			else
				_compresion = EXTENSION.none;
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
				//resultList.add(row.toArray(new String[row.size()]));
				
				// datos
				while (rs.next()) {
					res.numeroRegistros++;
					row = new ArrayList<String>();
					for (int i = 1; i <= columnCount; i++)
						row.add(rs.getObject(i).toString());
					_ficheroSalida.escribirLinea(row.toArray(new String[row.size()]),hoja);
					res.resultado=row.get(0);
					//resultList.add(row.toArray(new String[row.size()]));
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
