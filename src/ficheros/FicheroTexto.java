package ficheros;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class FicheroTexto implements Fichero{
	public static enum Extension {TXT,OK,ERROR,EJECUTANDO,CONFIG}
	private String _filename;
	private BufferedWriter _f; 

	public FicheroTexto(String fichero, boolean crear) {
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
			_f = new BufferedWriter(new OutputStreamWriter(
			          new FileOutputStream(this._filename), "utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return false;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		}
		return false;
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
			if (this._f!=null){
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
		try {
			_f.write(Arrays.toString(celdas));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void agregarLineaAlFinal(String linea) {
		if (this._f==null)
			this.crear();
			try {
				this._f.append(linea);
			} catch (IOException e) {
				System.err.println("Error al escribir linea al final del fichero "+this._filename+"\n\t\tDetalles: "+e.getMessage());
			}
	}

	@Override
	public String obtenerFileName() {
		return this._filename;
	}

}
