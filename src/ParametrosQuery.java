
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.StringTokenizer;

import ficheros.FicheroTexto;

public class ParametrosQuery {
	private String _fileConfig;
	private String _driver, _url, _user, _pass, _extensionSalida, _compresion;
	private String _fileOutput, _fileError, _fileEjecutando, _fileOK, _fileRequisito;
	private LinkedHashMap<String, String> _querys; //utilizar LinkedHashMap para añadir las querys en FIFO y determinar el orden que queremos en las hojas
	
	public ParametrosQuery(String configFile) throws IOException {
		_fileConfig = configFile;	//Contenido parecido a querys\\archivo.config
		Boolean res=this.leerParametros();
		if (!res){
			throw new IOException("Error al leer el fichero de configuracion "+configFile);
		}
	}
	

	
	public String getDriver() {
		return _driver;
	}



	public String getUrl() {
		return _url;
	}



	public String getUser() {
		return _user;
	}



	public String getPass() {
		return _pass;
	}



	public String getExtensionSalida() {
		return _extensionSalida;
	}



	public String getCompresion() {
		return _compresion;
	}



	public String getFileOutput() {
		return _fileOutput;
	}



	public String getFileError() {
		return _fileError;
	}



	public String getFileEjecutando() {
		return _fileEjecutando;
	}



	public String getFileOK() {
		return _fileOK;
	}



	public String getFileRequisito() {
		return _fileRequisito;
	}



	public LinkedHashMap<String, String> getQuerys() {
		return _querys;
	}



	/**
	 * Lee los par�metros del fichero de configuracion.
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
			_extensionSalida = config.obtenerValorConfiguracion(Configuracion.FORMATO_SALIDA).toLowerCase();
			
			//NOMBRE DE FICHEROS
			String extensionConfig=FicheroTexto.Extension.CONFIG.toString().toLowerCase();
			_fileOutput=_fileConfig.replaceAll("."+extensionConfig, "."+_extensionSalida);
			_fileError=_fileConfig.replaceAll("."+extensionConfig, "."+FicheroTexto.Extension.ERROR.toString().toLowerCase());
			_fileEjecutando=_fileConfig.replaceAll("."+extensionConfig, "."+FicheroTexto.Extension.EJECUTANDO.toString().toLowerCase());
			_fileOK=_fileConfig.replaceAll("."+extensionConfig, "."+FicheroTexto.Extension.OK.toString().toLowerCase());
			
			//PARAMETROS - Sustituimos los parametros de las querys
			String parametros=config.obtenerValorConfiguracion(Configuracion.PARAMETROS);
			StringTokenizer parametros_list=null;
			if (parametros!=null)
				parametros_list=new StringTokenizer(parametros, ";");
			//QUERY
			//QUERY1, QUERY2,....
			_querys=new LinkedHashMap<String, String>();
			for (int i=1;i<=Configuracion.MAX_QUERIES;i++){
				String queryNum=Configuracion.QUERY+Integer.toString(i);
				String salidaNum=Configuracion.SALIDA+Integer.toString(i);
				if (config.obtenerValorConfiguracion(queryNum)==null)
					break; //si no existe la siguiente query, salgo del bucle
				String query=config.obtenerValorConfiguracion(queryNum);
				String hoja=config.obtenerValorConfiguracion(salidaNum);
				
				//Reemplazo los parametros
				while (parametros_list.hasMoreTokens()){
					StringTokenizer par_val=new StringTokenizer(parametros_list.nextToken(), "=");
					String par=par_val.nextToken();
					String val=par_val.nextToken();
					if (query!=null)
						query=query.replaceAll(par, val);
				}
				//reseteo los parametros para la siguiente query
				parametros_list=new StringTokenizer(parametros, ";");
				
				_querys.put(hoja, query);
			}
			//COMPRESION
			_compresion=config.obtenerValorConfiguracion(Configuracion.COMPRESION);
			if (_compresion!=null)
				_compresion=_compresion.toLowerCase();
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

}
