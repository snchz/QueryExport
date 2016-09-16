import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import com.opencsv.*;

public class ExportadorQuery {
	private String _fileOutput, _query, _driver, _url, _user, _pass, _CONFIG = "Configuracion.config";

	private enum EXTENSION {
		ZIP, CSV, XLSX, NONE
	};

	private EXTENSION _EXTENSION, _COMPRESION;

	/**
	 * 
	 * @param configFile Fichero de Configuración
	 */
	public ExportadorQuery(String configFile) {
		_CONFIG = configFile;
	}

	/**
	 * Metodo principal. Lee el archivo de configuración y ejecuta la query solicitada.
	 * @throws Exception
	 */
	public void Ejecutar() throws Exception {
		System.out.println("Leyendo parametros...");
		LeerParametros();
		System.out.println("Lanzando query...");
		System.out.println("Escribiendo fichero (extension " + _EXTENSION.toString() + " )...");
		ObtenerResulsetQuery();
		System.out.println("Finalizada la generacion del fichero.");

		System.out.println("Compresion " + _COMPRESION.toString());
		if (_COMPRESION == EXTENSION.ZIP) {
			System.out.println("Comprimiendo fichero...");
			Comprimir();
			System.out.println("Borrando fichero origen...");
			BorrarFicheroSinComprimir();
		}
	}

	/**
	 * Borra fichero original para dejar solo el comprimido.
	 * @throws Exception
	 */
	private void BorrarFicheroSinComprimir() throws Exception {
		try {
			File fichero = new File(_fileOutput);
			fichero.delete();
		} catch (Exception e) {
			throw new Exception("Error al borrar el archivo " + _fileOutput + "\n\tDetalles: " + e.getMessage());
		}
	}

	/**
	 * Comprimir el fichero resutante.
	 * @throws Exception
	 */
	private void Comprimir() throws Exception {
		try {
			// input file
			FileInputStream in = new FileInputStream(_fileOutput);

			// out put file
			String _fileOutputZip = _fileOutput.substring(0, _fileOutput.lastIndexOf(".")) + ".zip";
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(_fileOutputZip));

			// name the file inside the zip file
			out.putNextEntry(new ZipEntry(_fileOutput));

			// buffer size
			byte[] b = new byte[1024];
			int count;

			while ((count = in.read(b)) > 0) {
				out.write(b, 0, count);
			}
			out.close();
			in.close();
		} catch (IOException e) {
			throw new Exception("Error al Comprimir el archivo " + _fileOutput + "\n\tDetalles: " + e.getMessage());
		}
	}

	/**
	 * Lee los parámetros del fichero de configuración.
	 * Linea 1. Driver de base de datos. Por ejemplo: oracle.jdbc.driver.OracleDriver
	 * Linea 2. Url de conexion a la base de datos. Por ejemplo: jdbc:oracle:thin:@localhost:1521:mkyong
	 * Linea 3. Usuario de conexion a la base de datos.
	 * Linea 4. Password de conexion a la base de datos.
	 * Linea 5. Nombre del fichero de salida de la consulta.
	 * Linea 6. Query a ejecutar.
	 * Linea 7. Si es ZIP, el archivo destino se comprime. En otro caso se deja sin comprimir
	 * @throws Exception
	 */
	private void LeerParametros() throws Exception {
		try {
			List<String> lines = FileUtils.readLines(new File(_CONFIG));
			_driver = lines.get(0);
			_url = lines.get(1);
			_user = lines.get(2);
			_pass = lines.get(3);
			_fileOutput = lines.get(4);
			try {
				if (_fileOutput.substring(_fileOutput.lastIndexOf(".") + 1, _fileOutput.length()).toUpperCase().trim()
						.equals("CSV"))
					_EXTENSION = EXTENSION.CSV;
				else if (_fileOutput.substring(_fileOutput.lastIndexOf(".") + 1, _fileOutput.length()).toUpperCase()
						.trim().equals("XLSX"))
					_EXTENSION = EXTENSION.XLSX;
				else
					_EXTENSION = EXTENSION.NONE;
			} catch (Exception e) {
				_EXTENSION = EXTENSION.NONE;
			}

			_query = lines.get(5);

			if (lines.get(6).toUpperCase().trim().equals("ZIP"))
				_COMPRESION = EXTENSION.ZIP;
			else
				_COMPRESION = EXTENSION.NONE;

		} catch (IOException e) {
			throw new Exception(
					"Error al leer el archivo de parametros " + _CONFIG + "\n\tDetalles: " + e.getMessage());
		}
	}

	/**
	 * Ejecuta la query y la guarda a fichero.
	 * @return OK o KO
	 * @throws Exception
	 */
	private String ObtenerResulsetQuery() throws Exception {
		Statement stmt = null;
		Connection conn = null;
		ResultSet rs = null;
		String resultado="OK";

		try {
			System.out.println("[Paso 1 de 3] Buscando driver " + _driver + "...");
			Class.forName(_driver);
			System.out.println("Driver " + _driver + " encontrado.");
			System.out.println("[Paso 2 de 3] Realizando conexion " + _url + "...");
			conn = DriverManager.getConnection(_url, _user, _pass);
			System.out.println("Conexion " + _url + " realizada.");
			stmt = conn.createStatement();
			System.out.println("[Paso 3 de 3] Lanzando query...");
			rs = stmt.executeQuery(_query);
			System.out.println("Query obtenida.");
			System.out.println("Guardando a fichero...");
			
			ResultSetMetaData metaData = rs.getMetaData();
			Integer columnCount = metaData.getColumnCount();

			// cabecera
			List<String> row = new ArrayList<String>();
			for (int i = 1; i <= columnCount; i++)
				row.add(metaData.getColumnName(i).toString());
			escribirLinea(row.toArray(new String[row.size()]));
			//resultList.add(row.toArray(new String[row.size()]));
			
			// datos
			while (rs.next()) {
				row = new ArrayList<String>();
				for (int i = 1; i <= columnCount; i++)
					row.add(rs.getObject(i).toString());
				escribirLinea(row.toArray(new String[row.size()]));
				//resultList.add(row.toArray(new String[row.size()]));
			}
			cerrarConexiones();
		} catch (Exception e) {
			resultado="KO";
			throw new Exception("Error al generar informe." + _fileOutput + "\n\tDetalles: "
					+ e.getMessage());
		} finally {
			if (rs != null)
				rs.close();
			if (stmt != null)
				stmt.close();
			if (conn != null)
				conn.close();
		}
		return resultado;
	}

	/**
	 * Escribe una linea en el fichero.
	 * @param strings Array de string con los datos de las columnas de la linea
	 * @throws Exception
	 */
	private void escribirLinea(String[] strings) throws Exception {
		if (_EXTENSION == EXTENSION.CSV) {
			escribirLineaCSV(strings);
		} else if (_EXTENSION == EXTENSION.XLSX) {
			escribirLineaXLSX(strings);
		}
	}

	private SXSSFWorkbook wb;
	private SXSSFSheet sh;
	private FileOutputStream out;
	private int proximaFila;
	/**
	 * Escribe linea en Excel
	 * @param strings
	 * @throws FileNotFoundException
	 */
	private void escribirLineaXLSX(String[] strings) throws FileNotFoundException {
		if (wb==null){
			proximaFila=0;
			wb = new SXSSFWorkbook();
			wb.setCompressTempFiles(true);
			sh = (SXSSFSheet) wb.createSheet("Hoja1");
			// keep 100 rows in memory, exceeding rows will be flushed to disk
			sh.setRandomAccessWindowSize(100);
			out = new FileOutputStream(_fileOutput);
		}

		int totalColumnas = strings.length;
		
		Row row = sh.createRow(proximaFila);
		for (int y = 0; y < totalColumnas; y++) {
			Cell cell = row.createCell(y);
			cell.setCellValue(strings[y].toString());
		}
		proximaFila++;
	}

	private CSVWriter wr;
	/**
	 * Escribe linea en CSV
	 * @param strings
	 * @throws Exception
	 */
	private void escribirLineaCSV(String[] strings) throws Exception {
		try {
			if (wr==null)
				wr = new CSVWriter(new FileWriter(_fileOutput), ';', CSVWriter.NO_QUOTE_CHARACTER);
			wr.writeNext(strings);
		} catch (IOException e) {
			throw new Exception("Error al generar CSV " + _fileOutput + "\n\tDetalles: " + e.getMessage());
		}
	}
	
	/**
	 * Cierra conexiones a fichero despues de crear los distintos ficheros.
	 * @throws Exception
	 */
	private void cerrarConexiones() throws Exception{
		try {
			if (wr!=null){
				wr.flush();
				wr.close();
			}
			if (wb!=null){
				wb.write(out);
				out.close();
				wb.close();
			}
		} catch (IOException e) {
			throw new Exception("Error cerrando conexiones.\n\tDetalles: " + e.getMessage());
		}
	}

}
