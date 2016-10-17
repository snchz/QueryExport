import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class Configuracion {
	private Properties _prop;
	private OutputStream _output;
	private InputStream _input;
	private String _fichero;
	public static String FICHERO_CONDICION="FICHERO_CONDICION", 
			USUARIO="USUARIO", CONEXION="CONEXION", PASSWORD="PASSWORD", DRIVER="DRIVER", 
			QUERY="QUERY", 
			MULTI_QUERY="MULTI_QUERY", MULTI_SALIDA="MULTI_SALIDA",
			FORMATO_SALIDA="FORMATO_SALIDA", COMPRESION="COMPRESION";
	
	public Configuracion(String fichero){
		_prop = new Properties();
		_fichero=fichero;
	}
	
	public void agregarValorConfiguracion(String nombre, String valor){
		_prop.setProperty(nombre, valor);
	}
	
	public void guardarConfiguracionAFichero(){
		try {
			_output = new FileOutputStream(_fichero);
			_prop.store(_output, null);
		} catch (IOException e) {
			System.err.println("Error al guardar configuracion\n\tDetalles: "+e.getMessage());
		}
	}
	
	public String obtenerValorConfiguracion(String nombre){
		if (_prop.isEmpty()&&_input==null){
			try {
				_input = new FileInputStream(_fichero);
				_prop.load(_input);
			} catch (FileNotFoundException e) {
				System.err.println("Error al abrir configuracion. EL archivo no se encuentra.\n\tDetalles: "+e.getMessage());
			} catch (IOException e) {
				System.err.println("Error al abrir configuracion. Archivo incorrecto.\n\tDetalles: "+e.getMessage());
			}
		}
		return _prop.getProperty(nombre);
	}
	
	public void cerrarConfiguracion(){
		try {
			if (_output!=null)
				_output.close();
			if (_input!=null)
				_input.close();
			
		} catch (IOException e) {
			System.err.println("Error al cerrrar configuracion\n\tDetalles: "+e.getMessage());
		}
	}
	
	public static void main(String[] args) {
		Configuracion c=new Configuracion("Ejemplo.config");
		/*c.agregarValorConfiguracion("DRIVER", "com.ibm.db2.jcc.DB2Driver");
		c.agregarValorConfiguracion("CONEXION", "jdbc:db2://UEGBABKS.GEOBAN.GS.CORP:60010/UEGBABKS");
		c.agregarValorConfiguracion("USUARIO", "x340382");
		c.agregarValorConfiguracion("PASSWORD", "Csacsa10");
		c.agregarValorConfiguracion("FORMATO_SALIDA", "xlsx");
		c.agregarValorConfiguracion("QUERY", "SELECT N8996_IDEMPR, N8996_IDDOCUMENT, N8996_DOCTYPE, N8996_FEALTAINC, N8996_INDREPLI,N8996_DOCSTATUS, N8996_FECALTALOC FROM TTGGSOP.DR_DISPUTA_DOCU WHERE N8996_FEALTAINC >= char(CURRENT_DATE - 1 DAY)||' 00:00:00' and N8996_FEALTAINC < char(CURRENT_DATE - 1 DAY)||' 23:59:00'");
		c.agregarValorConfiguracion("FICHERO_CONDICION", "LOG.txt");
		c.guardarConfiguracionAFichero();*/
		System.out.println(c.obtenerValorConfiguracion("CACA"));
		c.cerrarConfiguracion();
	}

}
