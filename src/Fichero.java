import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import com.opencsv.CSVWriter;

public class Fichero {
	
	//CSV:
	private CSVWriter _csv_wr;
	
	//XLSX:
	private SXSSFWorkbook _wb;
	private SXSSFSheet _sh;
	private FileOutputStream _out;
	private int _proximaFila;
	
	private String _fileOutput;
	private EXTENSION _extensionSalida;
	
	public enum EXTENSION {					//Tipos de extensiones admitidas
		zip, csv, xlsx, error, ejecutando, config, ok, none
	};

	/**
	 * Crear instancia de Fichero. Para crear el fichero invocar la funcion crearFichero
	 * @param fichero Nombre del fichero
	 */
	public Fichero(String fichero){
		_fileOutput=fichero;
		if (_fileOutput.substring(_fileOutput.lastIndexOf(".") + 1, _fileOutput.length()).trim().equals(EXTENSION.csv.toString())){
			_extensionSalida = EXTENSION.csv;
		}
		else if (_fileOutput.substring(_fileOutput.lastIndexOf(".") + 1, _fileOutput.length()).trim().equals(EXTENSION.ok.toString())){
			_extensionSalida = EXTENSION.ok;
		}
		else if (_fileOutput.substring(_fileOutput.lastIndexOf(".") + 1, _fileOutput.length()).trim().equals(EXTENSION.xlsx.toString())){
			_extensionSalida = EXTENSION.xlsx;
		}else{
			_extensionSalida = EXTENSION.none;
		}
	}
	
	/**
	 * Crear instancia de Fichero. Para crear el fichero poner a tre el segundo parametro
	 * @param fichero
	 * @param crear
	 */
	public Fichero(String fichero, boolean crear){
		_fileOutput=fichero;
		if (_fileOutput.substring(_fileOutput.lastIndexOf(".") + 1, _fileOutput.length()).trim().equals(EXTENSION.csv.toString())){
			_extensionSalida = EXTENSION.csv;
		}
		else if (_fileOutput.substring(_fileOutput.lastIndexOf(".") + 1, _fileOutput.length()).trim().equals(EXTENSION.ok.toString())){
			_extensionSalida = EXTENSION.ok;
		}
		else if (_fileOutput.substring(_fileOutput.lastIndexOf(".") + 1, _fileOutput.length()).trim().equals(EXTENSION.xlsx.toString())){
			_extensionSalida = EXTENSION.xlsx;
		}else{
			_extensionSalida = EXTENSION.none;
		}
		if (crear)
			this.crearFichero();		
	}
	
	/**
	 * Consultar la existencia del fichero
	 * @return Si existe retorna true, en otro caso false
	 */
	public boolean existeFichero(){
		return this.existeFichero(_fileOutput);
	}
	
	private boolean existeFichero(String fichero){
		boolean res=false;
		File f = new File(fichero);
		if(f.exists()) { //si existe devolvemos true directamente
		    res=true;
		}else{ //si no esta creado y no lo queremos crear devolvemos false y ya
			res=false;
		}
		return res;
	}
	
	/**
	 * Crear el fichero y cerrarlo. Usado sobre todo para crear ficheros vacios testigos.
	 * @return
	 */
	public boolean crearFicheroYCerrarlo() {
		return this.crearTestigo(_fileOutput);
	}
	
	private boolean crearTestigo(String fichero){
		boolean res=false;
		
		File f = new File(fichero);
		try{
			f.createNewFile();
			FileOutputStream oFile = new FileOutputStream(f, false); 
			oFile.close();
			res=true;
		}catch(Exception e){
			res=false;
			System.err.println("Error al crear el fichero "+fichero+"\n\tDetalles: "+e.getMessage());
		}
		return res;
	}
	
	/**
	 * Cre el fichero según su extension. Por ahora se aceptan extensiones csv, ok, xlsx
	 * @return
	 */
	public boolean crearFichero(){
		return this.crearFichero(_fileOutput);
	}
	
	private boolean crearFichero(String fichero){
		boolean res=true;
		_fileOutput=fichero;
		
		try{
			if (_extensionSalida == EXTENSION.csv || _extensionSalida == EXTENSION.ok){
				//inicializar variables de fichero CSV:
				_csv_wr = new CSVWriter(new FileWriter(_fileOutput), ';', CSVWriter.NO_QUOTE_CHARACTER);
			}
			else if (_extensionSalida == EXTENSION.xlsx){
				//inicializar variables de fichero xslx
				_proximaFila=0;
				_wb = new SXSSFWorkbook();
				_wb.setCompressTempFiles(true);
				_sh = (SXSSFSheet) _wb.createSheet("Hoja1");
				// keep 100 rows in memory, exceeding rows will be flushed to disk
				_sh.setRandomAccessWindowSize(100);
				_out = new FileOutputStream(_fileOutput);
			}
			else{
				res=false;
			}
		}catch(Exception e){
			res=false;
			System.err.println("Error al crear el fichero "+fichero+"\n\tDetalles: "+e.getMessage());
		}
		
		return res;
	}
	
	
	
	/**
	 * Escribe una linea en el fichero. Previamente debe estar creado.
	 * @param strings Array de string con los datos de las columnas de la linea
	 */
	public void escribirLinea(String[] strings) {
		try{
			if (_extensionSalida== EXTENSION.csv || _extensionSalida== EXTENSION.ok) {
				escribirLineaCSV(strings);
			} else if (_extensionSalida == EXTENSION.xlsx) {
				escribirLineaXLSX(strings);
			} else {
				System.err.println("Tipo de extension de salida no reconocido: "+_extensionSalida+"\n\t\tTipos reconocidos: xlsx, csv, ok.");
			}
		}catch(Exception e){
			System.err.println("Error al escribir en el fichero : "+_fileOutput+"\n\t\tDetalles: "+e.getMessage());
		}
	}
	
	/**
	 * Si no exsite el fichero lo crea. Añade linea al final.
	 * @param linea
	 * @return
	 */
	public boolean escribirLineaAlFinal(String linea){
		return this.escribirLineaAlFinal(_fileOutput,linea);
	}

	
	/**
	 * Escribe una linea en el fichero.
	 * @param strings Array de string con los datos de las columnas de la linea
	 */
	private boolean escribirLineaAlFinal(String fichero, String linea) {
		boolean res=false;
		if (!this.existeFichero(fichero))
			this.crearTestigo(fichero);
		try {
			BufferedWriter salida = new BufferedWriter(new FileWriter(fichero, true));
			salida.append(linea);
			salida.close();
			res=true;
		} catch (IOException e) {
			System.err.println("Error al escribir linea al final del fichero "+fichero+"\n\t\tDetalles: "+e.getMessage());
			res=false;
		}
		return res;
	}

	
	
	/**
	 * Escribe linea en CSV
	 * @param strings
	 * @throws Exception
	 */
	private void escribirLineaCSV(String[] strings) throws Exception {
		_csv_wr.writeNext(strings);
	}
	
	/**
	 * Cierra conexiones a fichero despues de crear los distintos ficheros.
	 * @throws Exception
	 */
	public boolean cerrarConexiones(){
		boolean res=true;
		try {
			if (_csv_wr!=null){
				_csv_wr.flush();
				_csv_wr.close();
			}
			if (_wb!=null){
				_wb.write(_out);
				_out.close();
				_wb.close();
			}
		} catch (IOException e) {
			System.err.println("Error cerrando conexiones.\n\tDetalles: " + e.getMessage());
			res=false;
		}
		return res;
	}
	/**
	 * Escribe linea en Excel
	 * @param strings
	 * @throws FileNotFoundException
	 */
	private void escribirLineaXLSX(String[] strings){
		int totalColumnas = strings.length;
		
		Row row = _sh.createRow(_proximaFila);
		for (int y = 0; y < totalColumnas; y++) {
			Cell cell = row.createCell(y);
			cell.setCellValue(strings[y].toString());
		}
		_proximaFila++;
	}
	
	public boolean comprimir(){
		return this.comprimir(_fileOutput);
	}
	
	/**
	 * Comprimir el fichero resutante.
	 * @throws Exception
	 */
	private boolean comprimir(String fichero){
		boolean res=false;
		try {
			// input file
			FileInputStream in = new FileInputStream(fichero);

			// out put file
			String _fileOutputZip = fichero.substring(0, fichero.lastIndexOf(".")) + ".zip";
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(_fileOutputZip));

			// name the file inside the zip file
			out.putNextEntry(new ZipEntry(fichero));

			// buffer size
			byte[] b = new byte[1024];
			int count;

			while ((count = in.read(b)) > 0) {
				out.write(b, 0, count);
			}
			out.close();
			in.close();
			res=true;
		} catch (IOException e) {
			System.err.println("Error al Comprimir el archivo " + fichero + "\n\tDetalles: " + e.getMessage());
			res=false;
		}
		return res;
	}
	
	public boolean borrarFichero(){
		return this.borrarFichero(_fileOutput);
	}
	
	/**
	 * Borra fichero original para dejar solo el comprimido.
	 */
	public boolean borrarFichero(String file){
		boolean res=false;
		try {
			File fichero = new File(file);
			fichero.delete();
			res=true;
		} catch (Exception e) {
			System.err.println("Error al borrar el archivo " + file + "\n\tDetalles: " + e.getMessage());
			res=false;
		}
		return res;
	}
	
	public static void main(String[] args) {
		Fichero f=new Fichero("fichero.xlsx");
		f.crearFichero();
		String[] lineas= new String[2];
		lineas[0]="Hola";
		lineas[1]="Adios";
		f.escribirLinea(lineas);
		f.cerrarConexiones();
	}
	
}
