import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FileUtils;
import com.opencsv.*;


public class Principal {
	static String _fileOutput, _query, _driver, _url, _user, _pass, _CONFIG = "Configuracion.config";

	public static void main(String[] args) {
		System.out.println("INICIO "+(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).format(Calendar.getInstance().getTime()));
		_CONFIG=args[0];
		LeerParametros();
		QueryToCSV();
		Comprimir();
		BorrarFicheroSinComprimir();
		System.out.println("FIN "+(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).format(Calendar.getInstance().getTime()));
		
	}

	private static void BorrarFicheroSinComprimir() {
		System.out.println("Borrando fichero "+_fileOutput+" ...");
		File fichero = new File(_fileOutput);
		fichero.delete();
		System.out.println("Fichero "+_fileOutput+" borrado!");
	}

	private static void Comprimir() {
		try {
			System.out.println("Comprimiendo fichero "+_fileOutput+" ...");
			// input file
			FileInputStream in = new FileInputStream(_fileOutput);
			
			// out put file
			String _fileOutputZip=_fileOutput.substring(0, _fileOutput.lastIndexOf("."))+".zip";
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

			System.out.println("Fichero comprimido en "+_fileOutputZip+"!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void LeerParametros() {
		try {
			System.out.println("Leyendo archivo de configuracion "+_CONFIG+" ...");
			List<String> lines = FileUtils.readLines(new File(_CONFIG));
			_driver = lines.get(0);
			_url = lines.get(1);
			_user = lines.get(2);
			_pass = lines.get(3);
			_fileOutput = lines.get(4);
			_query = lines.get(5);
			System.out.println("Parametros configurados!");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void QueryToCSV() {
		CSVWriter wr;
		Statement stmt;
		Connection conn;
		try {
			Class.forName(_driver);
			System.out.println("Driver " + _driver + " encontrado!");
			conn = DriverManager.getConnection(_url, _user, _pass);
			System.out.println("Conexion " + _url + " realizada!");
			stmt = conn.createStatement();
			System.out.println("Lanzando query...");
			ResultSet rs = stmt.executeQuery(_query);
			System.out.println("Query obtenida!");
			System.out.println("Escribiendo fichero...");
			wr = new CSVWriter(new FileWriter(_fileOutput), ';', CSVWriter.NO_QUOTE_CHARACTER);
			wr.writeAll(rs, true);
			System.out.println("Fichero escrito!");
			wr.flush();
			wr.close();

			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException | IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

}
