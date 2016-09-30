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
		//args=new String[1];args[0]="TestDMContable.config";
		
		if (args.length==0){
			//TODO Recorrer carpeta de CONFIGURACION
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
		
		if (!resultado.equals("IGNORAR")){
			String lineaLog=(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).format(horaInicio)+"\t"+(new SimpleDateFormat("HH:mm:ss")).format(horaFin)+"\t("+String.format("%010d", minutos)+" min)\t"+resultado+"\t"+args[0]+"\n";
			Fichero log=new Fichero("LOG.txt");
			log.escribirLineaAlFinal(lineaLog);
		}
		
	}

}
