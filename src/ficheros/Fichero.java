package ficheros;

public interface Fichero {
	public boolean existe();
	public boolean crear();
	public boolean borrar();
	public boolean cerrar();
	public String obtenerFileName();
	/**
	 * Escribe una linea en el fichero. Previamente debe estar creado. Si no lo está, lo crea vacio.
	 * Al finalizar las escrituras, hay que cerrar el fichero con la funcion correspondiente.
	 * @param celdas Array con todas las celdas de una fila a insertar
	 * @param args Array de otros argumentos extras. Por ejemplo nombre de la hoja donde se inserta los valores en una excel
	 */
	public void escribirLinea(String[] celdas ,Object[] args);
}
