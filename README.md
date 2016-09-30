# QueryExport
Exporting Data from a Query (any database) to a Data File (CSV, Excel...). 
Exportar los datos de una query de base de datos a un fichero. Ideal para generar reportes automatizados.

El fichero de configuración debe tener una estructura como la siguiente:

```
oracle.jdbc.driver.OracleDriver
jdbc:oracle:thin:@localhost:1521:midb
user
pass
csv
SELECT A B C FROM TABLE
zip
requisito.ok
```

- Linea 1. Nombre del driver de la base de datos (por ejemplo oracle.jdbc.driver.OracleDriver) Está preparado para añadir otros drivers a los que será necesario adicionar la libreria jdbc correspondiente.
- Linea 2. URL de conexión a la base de datos. Formato __jdbc:NAME://HOST:PORT/DB__
- Linea 3. Usuario de conexión a la base de datos.
- Linea 4. Contraseña de conexion a la base de datos.
- Linea 5. Extension del fichero a generar. El nombre será el mismo del archivo de configuracion. Por ahora solo admite extensiones .CSV y .XLSX
- Línea 6. Query a ejecutar. Debe ser en una sola linea pero admite toda la complejidad que pueda tener una query. Si se piensa usar para automatizar por ejemplo un informe basado en fecha, es recomendable preparar una query que use la hora del sistema y así evitar cambiarla constantemente.
- Línea 7. Si se quiere comprimir el fichero resultante debe aparecer el texto ZIP en esta linea. La idea es utilizarlo a la hora de generar CSV ya que la reducción de tamaño es considerable. Para archivos XLSX no tiene sentido utilizarlo ya que son ficheros autocomprimidos.
- Línea 8. Archivo .ok requisito para que se ejecute la query actual. Dejar linea vacia si no hay requisito. En caso de estar informado solo se ejecutará la query en caso de existencia del fichero.
- Línea 9 en adelante. Por ahora puede utilizarse para dejar comentarios ya que todo el resto de contenido se obvia. De cara a futuro estas lineas podrán tener utilidad para añadir alguna utilidad.
