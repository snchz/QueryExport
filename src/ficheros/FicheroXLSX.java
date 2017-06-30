package ficheros;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;


public class FicheroXLSX implements Fichero{
	public static enum Extension {XLSX}
	private SXSSFWorkbook _f;
	private String _filename;
	private FileOutputStream _fos;
	private int _proximaFila;
	
	public FicheroXLSX(String fichero, boolean crear) {
		_filename=fichero;
		if (crear){
			this.crear();
			this.cerrar();
		}
	}

	@Override
	public boolean existe() {
		boolean res=false;
		File f = new File(this._filename);
		if(f.exists()) { //si existe devolvemos true directamente
		    res=true;
		}else{ //si no esta creado y no lo queremos crear devolvemos false y ya
			res=false;
		}
		return res;
	}

	@Override
	public boolean crear() {
		//inicializar variables de fichero xslx
		_proximaFila=0;
		_f = new SXSSFWorkbook();
		_f.setCompressTempFiles(true);
		try {
			_fos = new FileOutputStream(this._filename);
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean borrar() {
		boolean res=false;
		try {
			File fichero = new File(this._filename);
			fichero.delete();
			res=true;
		} catch (Exception e) {
			System.err.println("Error al borrar el archivo " + this._filename + "\n\tDetalles: " + e.getMessage());
			res=false;
		}
		return res;
	}

	@Override
	public boolean cerrar() {
		if (_f!=null){
			try {
				//_f.write(_fos); TODO: Escribo o no?
				_fos.close();
				_f.close();
				this._f=null;
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	@Override
	public void escribirLinea(String[] celdas, Object[] args) {
		if (this._f==null)
			this.crear();
		int totalColumnas = celdas.length;
		String hoja=(String)args[0];
		
		SXSSFSheet _sh;
		Sheet s=this._f.getSheet(hoja);
		if (s==null){//si la hoja no existe, la creo
			_sh = (SXSSFSheet) this._f.createSheet(hoja);
			_proximaFila=0;
		}
		else
			 _sh = (SXSSFSheet) s;
			
		//reserva 100 filas en memoria, si las excede reserva otras tantas
		_sh.setRandomAccessWindowSize(100);
		
		Row row = _sh.createRow(_proximaFila);
		for (int y = 0; y < totalColumnas; y++) {
			Cell cell = row.createCell(y);
			cell.setCellValue(celdas[y].toString());
		}
		_proximaFila++;
	}

	@Override
	public String obtenerFileName() {
		return this._filename;
	}

}
