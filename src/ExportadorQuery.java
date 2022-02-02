
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import ficheros.Compresor;
import ficheros.Fichero;
import ficheros.FicheroCSV;
import ficheros.FicheroCompresor;
import ficheros.FicheroTexto;
import ficheros.FicheroXLSX;




public class ExportadorQuery {
	private ParametrosQuery _pq;
	Fichero _ficheroSalida;
	
	/**
	 * Constructor con parametro del archivo de configuracion
	 * El fichero debe tener extension .config
	 * @param configFile Fichero de Configuracion
	 */
	public ExportadorQuery(String configFile) {
		try {
			_pq=new ParametrosQuery(configFile);
		} catch (IOException e) {
			e.printStackTrace();
			_pq=null;
		}
	}

	private static class Combo {
		   String resultado;
		   int numeroRegistros;
	}
	

	/**
	 * Metodo principal. Lee el archivo de configuraci�n y ejecuta la query solicitada.
	 */
	public String ejecutar(){
		String res=null;
		System.out.println("[...] Leyendo parametros...");
		if (this._pq!=null){
			Fichero fEjecutando=new FicheroTexto(this._pq.getFileEjecutando(),false);
			Fichero fRequisito=new FicheroTexto(this._pq.getFileRequisito(),false);
			Fichero fOK=new FicheroTexto(this._pq.getFileOK(),false);
			Fichero fError=new FicheroTexto(this._pq.getFileError(),false);
			if(fEjecutando.existe())
				System.out.println("[...] Ya esta ejecutando.");
			else if (!fRequisito.existe())
				System.out.println("[...] El fichero requisito no existe.");
			else if (fOK.existe())
				System.out.println("[...] Ya se ha ejecutado y el fichero OK existe.");
			else if (fError.existe())
				System.out.println("[...] El fichero de error existe. Se ejecuto anteriormente con algun error.");
			else{
				fEjecutando=new FicheroTexto(this._pq.getFileEjecutando(),true);
				
				System.out.println("[...] Escribiendo fichero (extension " + this._pq.getExtensionSalida() + " )...");
				
				//Creo el fichero en funcion de su extension
				_ficheroSalida=new FicheroTexto(this._pq.getFileOutput(),false);
				if (this._pq.getExtensionSalida().equals(FicheroCSV.Extension.CSV.toString().toLowerCase()))
					_ficheroSalida=new FicheroCSV(this._pq.getFileOutput(),false);
				else if (this._pq.getExtensionSalida().equals(FicheroXLSX.Extension.XLSX.toString().toLowerCase()))
					_ficheroSalida=new FicheroXLSX(this._pq.getFileOutput(),true);
				
				
				Combo combo_res=this.obtenerResulsetQuery();
				int registros=combo_res.numeroRegistros;
				System.out.println("[...] Se han escrito "+registros+" registros.");
				if (registros==0){
					_ficheroSalida.borrar();
					res="SIN DATOS";
				}else if (registros==1){
					fOK=new FicheroTexto(this._pq.getFileOK(), true);
					res="OK "+combo_res.resultado;
				}else if (registros>1){
					fOK=new FicheroTexto(this._pq.getFileOK(), true);
					res="MULTIDATOS ("+registros+" registros)";
				}
				
				System.out.println("[...] Compresion solicitada: " + this._pq.getCompresion());
				if (this._pq.getCompresion()!=null && this._pq.getCompresion().equals(FicheroCompresor.ExtCompresion.ZIP.toString().toLowerCase()) && _ficheroSalida.existe()) {
					System.out.println("[...] Comprimiendo fichero...");
					Compresor c=new Compresor();
					c.comprimir(Compresor.ExtCompresion.ZIP,this._ficheroSalida.obtenerFileName());
					System.out.println("[...] Borrando fichero origen...");
					_ficheroSalida.borrar();
				}
				System.out.println("[...] Borrando fichero testigo Ejecutando...");
				fEjecutando.borrar();
			}					
		}
		return res;
	}

	

	/**
	 * Ejecuta la query y la guarda a fichero.
	 * @return -1 si error, 0 si ok, numero mayor que 0 el numero de registros
	 */
	private Combo obtenerResulsetQuery(){
		Statement stmt = null;
		Connection conn = null;
		ResultSet rs = null;
		Combo res=new Combo();
		res.numeroRegistros=0;
		res.resultado="";
		if (_ficheroSalida==null)
			return res;
		try {
			System.out.println("[...........] Preparando fichero " + _ficheroSalida.obtenerFileName());
			System.out.println("[Paso 1 de 5] Buscando driver " + this._pq.getDriver() + "...");
			Class.forName(this._pq.getDriver());
			System.out.println("[...........] Driver " + this._pq.getDriver() + " encontrado.");
			System.out.println("[Paso 2 de 5] Realizando conexion " + this._pq.getUrl() + "...");
			conn = DriverManager.getConnection(this._pq.getUrl(), this._pq.getUser(), this._pq.getPass());
			System.out.println("[...........] Conexion " + this._pq.getUrl() + " realizada.");
			stmt = conn.createStatement();
			
			for (String hoja:this._pq.getQuerys().keySet()){
				System.out.println("[Paso 3 de 5] Lanzando query... Hoja: "+hoja);
				rs = stmt.executeQuery(this._pq.getQuerys().get(hoja));
				System.out.println("[...........] Query obtenida.");
				System.out.println("[Paso 4 de 5] Guardando a fichero...");
				
				ResultSetMetaData metaData = rs.getMetaData();
				Integer columnCount = metaData.getColumnCount();
				
				// cabecera
				List<String> row = new ArrayList<String>();
				for (int i = 1; i <= columnCount; i++)
					row.add(metaData.getColumnName(i).toString());
				_ficheroSalida.escribirLinea(row.toArray(new String[row.size()]),new String[]{hoja});
				
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
					_ficheroSalida.escribirLinea(row.toArray(new String[row.size()]),new String[]{hoja});
					res.resultado=row.get(0);
				}
			}
			System.out.println("[...........] Fichero generado.");
			_ficheroSalida.cerrar();
		} catch (ClassNotFoundException e) {
			res.numeroRegistros=-1;
			new FicheroTexto(this._pq.getFileError(),true);
			System.err.println("Error Libreria JAVA para Base de datos no encontrada." + this._pq.getFileOutput() + "\n\tDetalles: "
					+ e.getMessage());
		} catch (SQLException e) {
			res.numeroRegistros=-1;
			new FicheroTexto(this._pq.getFileError(),true);
			System.out.println("Error al conectar. Credenciles o query incorrectas." + this._pq.getFileOutput() + "\n\tDetalles: "
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
					System.out.println("Error al cerrar conexiones." + this._pq.getFileOutput() + "\n\tDetalles: "
							+ e.getMessage());
				}
		}
		return res;
	}
}
