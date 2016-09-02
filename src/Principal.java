import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Principal {

	/**
	 * 
	 * @param args Se le pasa el archivo de configuración por parámetro
	 */
	public static void main(String[] args) {
		//Log inicio
		Date horaInicio=Calendar.getInstance().getTime();
		System.out.println("HORA INICIO "+(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).format(horaInicio));
		
		ExportadorQuery eq=new ExportadorQuery(args[0]);
		try {
			eq.Ejecutar();
		} catch (Exception e) {
			e.printStackTrace();
		}
			
		//Log Fin
		Date horaFin=Calendar.getInstance().getTime();
		long minutos=TimeUnit.MINUTES.convert(horaFin.getTime()-horaInicio.getTime(), TimeUnit.MILLISECONDS);
		System.out.println("HORA FIN "+(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).format(horaFin));
		System.out.println("TIEMPO DE EJECUCION: "+minutos);
	}

}
