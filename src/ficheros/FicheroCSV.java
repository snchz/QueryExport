package ficheros;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.opencsv.CSVWriter;


public class FicheroCSV implements Fichero{
	public static enum Extension {CSV}
	private CSVWriter _f;
	private String _filename;
	
	public FicheroCSV(String fichero, boolean crear){
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
		try {
			_f = new CSVWriter(new FileWriter(this._filename), ';', CSVWriter.NO_QUOTE_CHARACTER);
		} catch (IOException e) {
			return false;
		}
		return true;
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
		try {
			if (_f!=null){
				_f.flush();
				_f.close();
				_f=null;
				return true;
			}
			return false;
		} catch (IOException e) {
			return false;
		}
	}

	@Override
	public void escribirLinea(String[] celdas, Object[] args) {
		if (this._f==null)
			this.crear();
		this._f.writeNext(celdas);
	}

	@Override
	public String obtenerFileName() {
		return this._filename;
	}
	
}
