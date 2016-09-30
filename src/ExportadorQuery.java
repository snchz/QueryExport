import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class ExportadorQuery {
	private String _fileOutput, _fileError, _fileEjecutando, _fileOK, _fileRequisito,_query, _driver, _url, _user, _pass;
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
		String res="IGNORAR";
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
					int registros=ObtenerResulsetQuery();
					System.out.println("[...] Se han escrito "+registros+" registros.");
					if (registros==0){
						_ficheroSalida.borrarFichero();
						res="SIN DATOS";
					}else if (registros>0){
						_ficheroOK.crearFicheroYCerrarlo();
						res="OK";
					}
					
				}else{
					System.out.println("[...] Está en error.");
					res="IGNORAR";
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
				res="IGNORAR";
			}					
		}
		return res;
	}

	

	

	/**
	 * Lee los parámetros del fichero de configuración.
	 * Linea 1. Driver de base de datos. Por ejemplo: oracle.jdbc.driver.OracleDriver
	 * Linea 2. Url de conexion a la base de datos. Por ejemplo: jdbc:oracle:thin:@localhost:1521:mkyong
	 * Linea 3. Usuario de conexion a la base de datos.
	 * Linea 4. Password de conexion a la base de datos.
	 * Linea 5. Extension del fichero de salida de la consulta. En funcion del tipo de extension de salida hace una cosa u otra
	 *          Si es .ok genera el fichero solo el numero de registros de la consulta es mayor a 0 (devuelve datos)
	 * Linea 6. Query a ejecutar.
	 * Linea 7. Si es ZIP, el archivo destino se comprime. En otro caso se deja sin comprimir
	 * Linea 8. Fichero ok. Si este fichero no existe, no se ejecuta.
	 */
	private boolean leerParametros(){
		boolean res=false;
		try {
			if (!_fileConfig.contains(".config")){
				throw new Exception("Error extension de fichero de configuracion incorrecta " + _fileConfig);
			}
			List<String> lines = FileUtils.readLines(new File(_fileConfig));
			_driver = lines.get(0);
			_url = lines.get(1);
			_user = lines.get(2);
			_pass = lines.get(3);
			String extension = lines.get(4);
			try {
				if (extension.equals(EXTENSION.csv.toString()))
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
			_fileOutput=_fileConfig.replaceAll("."+EXTENSION.config.toString(), "."+_extensionSalida.toString());

			_fileError=_fileConfig.replaceAll("."+EXTENSION.config.toString(), "."+EXTENSION.error.toString());
			_fileEjecutando=_fileConfig.replaceAll("."+EXTENSION.config.toString(), "."+EXTENSION.ejecutando.toString());
			_fileOK=_fileConfig.replaceAll("."+EXTENSION.config.toString(), "."+EXTENSION.ok.toString());

			_query = lines.get(5);

			if (lines.get(6).trim().equals(EXTENSION.zip.toString()))
				_compresion = EXTENSION.zip;
			else
				_compresion = EXTENSION.none;
			try{
				_fileRequisito= lines.get(7);
			}catch (Exception e){
				_fileRequisito=_fileConfig;
			}
			if (_fileRequisito.trim().equals(""))
				_fileRequisito=_fileConfig;
			res=true;
		} catch (Exception e) {
			System.err.println("Error al leer el archivo de parametros " + _fileConfig + "\n\tDetalles: " + e.getMessage());
			res=false;
		}
		return res;
	}

	/**
	 * Ejecuta la query y la guarda a fichero.
	 * @return -1 si error, 0 si ok, numero mayor que 0 el numero de registros
	 */
	private int ObtenerResulsetQuery(){
		Statement stmt = null;
		Connection conn = null;
		ResultSet rs = null;
		int numeroRegistros=0;

		try {
			System.out.println("[Paso 1 de 4] Buscando driver " + _driver + "...");
			Class.forName(_driver);
			System.out.println("[...........] Driver " + _driver + " encontrado.");
			System.out.println("[Paso 2 de 4] Realizando conexion " + _url + "...");
			conn = DriverManager.getConnection(_url, _user, _pass);
			System.out.println("[...........] Conexion " + _url + " realizada.");
			stmt = conn.createStatement();
			System.out.println("[Paso 3 de 4] Lanzando query...");
			rs = stmt.executeQuery(_query);
			System.out.println("[...........] Query obtenida.");
			System.out.println("[Paso 3 de 4] Guardando a fichero...");
			
			ResultSetMetaData metaData = rs.getMetaData();
			Integer columnCount = metaData.getColumnCount();

			_ficheroSalida.crearFichero();
			// cabecera
			List<String> row = new ArrayList<String>();
			for (int i = 1; i <= columnCount; i++)
				row.add(metaData.getColumnName(i).toString());
			_ficheroSalida.escribirLinea(row.toArray(new String[row.size()]));
			//resultList.add(row.toArray(new String[row.size()]));
			
			// datos
			while (rs.next()) {
				numeroRegistros++;
				row = new ArrayList<String>();
				for (int i = 1; i <= columnCount; i++)
					row.add(rs.getObject(i).toString());
				_ficheroSalida.escribirLinea(row.toArray(new String[row.size()]));
				//resultList.add(row.toArray(new String[row.size()]));
			}
			System.out.println("[...........] Fichero generado.");
			_ficheroSalida.cerrarConexiones();
		} catch (ClassNotFoundException e) {
			numeroRegistros=-1;
			_ficheroError.crearFicheroYCerrarlo();
			System.err.println("Error Libreria JAVA para Base de datos no encontrada." + _fileOutput + "\n\tDetalles: "
					+ e.getMessage());
		} catch (SQLException e) {
			numeroRegistros=-1;
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
		return numeroRegistros;
	}

	


}
