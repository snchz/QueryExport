package ficheros;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

public class Compresor {
	public static enum ExtCompresion {ZIP, ZIP7}
	
	
	public boolean comprimir(ExtCompresion ec, String fichero){
		if (ec.equals(ExtCompresion.ZIP))
			this.comprimirZip(fichero);
		return true;
	}
	
	/**
	 * Comprimir el fichero resutante.
	 * @throws Exception
	 */
	private boolean comprimirZip(String fichero){
		boolean res=false;
		try {
			//fichero de entrada
			FileInputStream in = new FileInputStream(fichero);

			//fichero de salida
			String _fileOutputZip = fichero.substring(0, fichero.lastIndexOf(".")) + ".zip";
			ZipOutputStream out = new ZipOutputStream(new FileOutputStream(_fileOutputZip));

			//nombre del fichero a comprimir
			out.putNextEntry(new ZipEntry(fichero));

			//tama�o del buffer
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
	
	//TODO adaptar a la libreria de zipeo
	@SuppressWarnings("unused")
	private boolean comprimirZip(String fichero,long tamanoMaximo){
		boolean res=false;
		try {
			String _fileOutputZip = fichero.substring(0, fichero.lastIndexOf(".")) + ".zip";
			ZipFile zipFile = new ZipFile(_fileOutputZip);
			File ficheroEntrada = new File(fichero);
			
			ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
			
            zipFile.createZipFile(ficheroEntrada, parameters, true, 10485760);
			res=true;
		} catch (ZipException e) {
			System.err.println("Error al Comprimir el archivo " + fichero + "\n\tDetalles: " + e.getMessage());
			res=false;
			e.printStackTrace();
		}
		return res;
	}

}
