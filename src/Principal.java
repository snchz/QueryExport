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
		
		String resultado="";
		
		if (args.length==0){
			//TODO Recorrer carpeta de CONFIGURACION o pedir introducir valores por consola
		}else{
			ExportadorQuery eq=new ExportadorQuery(args[0]);
			try {
				resultado=eq.Ejecutar();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
			
		//Log Fin
		Date horaFin=Calendar.getInstance().getTime();
		long minutos=TimeUnit.MINUTES.convert(horaFin.getTime()-horaInicio.getTime(), TimeUnit.MILLISECONDS);
		System.out.println("HORA FIN "+(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).format(horaFin));
		System.out.println("TIEMPO DE EJECUCION (minutos): "+minutos);
		//TODO:Escribor en log
		
		if (resultado != null){
			String lineaLog=(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).format(horaInicio)+"\t"+(new SimpleDateFormat("HH:mm:ss")).format(horaFin)+"\t("+String.format("%04d", minutos)+" min)\t"+String.format("%1$-50s",args[0])+"\t"+resultado+"\n";
			Fichero log=new Fichero("LOG.txt");
			log.escribirLineaAlFinal(lineaLog);
		}
		
	}

}
