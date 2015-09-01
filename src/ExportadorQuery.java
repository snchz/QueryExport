import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
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

	public ExportadorQuery(String configFile) {
		_CONFIG = configFile;
	}

	public void Ejecutar() throws Exception {
		System.out.println("Leyendo parametros...");
		LeerParametros();
		System.out.println("Lanzando query...");
		ResultSet rs = ObtenerResulsetQuery();

		System.out.println("Extension "+_EXTENSION.toString());
		if (_EXTENSION == EXTENSION.CSV){
			System.out.println("Escribiendo fichero...");
			EscribirCSV(rs);
		}
		else if (_EXTENSION == EXTENSION.XLSX){
			System.out.println("Escribiendo fichero...");
			EscribirXLSX(rs);
		}
		System.out.println("Finalizada generacion.");
		

		System.out.println("Compresion "+_COMPRESION.toString());
		if (_COMPRESION == EXTENSION.ZIP) {
			System.out.println("Comprimiendo fichero...");
			Comprimir();
			System.out.println("Borrando fichero origen...");
			BorrarFicheroSinComprimir();
		}
	}

	private void BorrarFicheroSinComprimir() throws Exception {
		try {
			File fichero = new File(_fileOutput);
			fichero.delete();
		} catch (Exception e) {
			throw new Exception("Error al borrar el archivo " + _fileOutput + "\n\tDetalles: " + e.getMessage());
		}
	}

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
				System.out.println();
				out.write(b, 0, count);
			}
			out.close();
			in.close();
		} catch (IOException e) {
			throw new Exception("Error al Comprimir el archivo " + _fileOutput + "\n\tDetalles: " + e.getMessage());
		}
	}

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

	private void EscribirCSV(ResultSet rs) throws Exception {
		CSVWriter wr;
		try {
			wr = new CSVWriter(new FileWriter(_fileOutput), ';', CSVWriter.NO_QUOTE_CHARACTER);
			wr.writeAll(rs, true);
			wr.flush();
			wr.close();

			rs.close();
		} catch (IOException | SQLException e) {
			throw new Exception("Error al generar CSV " + _fileOutput + "\n\tDetalles: " + e.getMessage());
		}
	}

	private void EscribirXLSX(ResultSet rs) throws Exception {
		// TODO Fala ver si el close del wb afecta en algo.
		try {

			SXSSFWorkbook wb = new SXSSFWorkbook();
			wb.setCompressTempFiles(true);

			SXSSFSheet sh = (SXSSFSheet) wb.createSheet("Hoja1");
			// keep 100 rows in memory, exceeding rows will be flushed to disk
			sh.setRandomAccessWindowSize(100);
			int totalColumnas = rs.getMetaData().getColumnCount();

			// Cabecera
			Row heading = sh.createRow(1);
			ResultSetMetaData rsmd = rs.getMetaData();
			for (int x = 0; x < totalColumnas; x++) {
				Cell cell = heading.createCell(x + 1);
				cell.setCellValue(rsmd.getColumnLabel(x + 1));
			}

			// Datos
			int rowNumber = 2;

			while (rs.next()) {
				Row row = sh.createRow(rowNumber);
				for (int y = 0; y < totalColumnas; y++) {
					Cell cell = row.createCell(y + 1);
					cell.setCellValue(rs.getString(y + 1));
				}
				rowNumber++;
			}

			FileOutputStream out = new FileOutputStream(_fileOutput);
			wb.write(out);
			out.close();

			rs.close();
		} catch (SQLException e) {
			throw new Exception("Error al generar XLSX " + _fileOutput + "\n\tDetalles: " + e.getMessage());
		}
	}

	private ResultSet ObtenerResulsetQuery() throws Exception {
		Statement stmt;
		Connection conn;
		String log = "";
		try {
			log = log + "[Paso 1 de 3]Buscando driver " + _driver + "...\n";
			Class.forName(_driver);
			log = log + "Driver " + _driver + " encontrado.\n";
			log = log + "[Paso 2 de 3]Realizando conexion " + _url + "...\n";
			conn = DriverManager.getConnection(_url, _user, _pass);
			log = log + "Conexion " + _url + " realizada.\n";
			stmt = conn.createStatement();
			log = log + "[Paso 3 de 3]Lanzando query...\n";
			ResultSet rs = stmt.executeQuery(_query);
			log = log + "Query obtenida.\n";

			stmt.close();
			conn.close();
			return rs;
		} catch (SQLException | ClassNotFoundException e) {
			log = log + "ERROR!!!";
			throw new Exception("Error leer de la base de datos " + _fileOutput + "\n\tLog:\n" + log + "\n\tDetalles: "
					+ e.getMessage());
		}
	}

}
