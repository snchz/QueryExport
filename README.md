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

- Linea "DRIVER=". Driver de base de datos. Por ejemplo: oracle.jdbc.driver.OracleDriver
- Linea "CONEXION=". Url de conexion a la base de datos. Por ejemplo: __jdbc:NAME://HOST:PORT/DB__
- Linea "USUARIO=". Usuario de conexion a la base de datos.
- Linea "PASSWORD=". Password de conexion a la base de datos.
- Linea "FORMATO_SALIDA=". Extension del fichero de salida de la consulta. En funcion del tipo de extension de salida hace una cosa u otra
	- Si es .ok genera el fichero solo el numero de registros de la consulta es mayor a 0 (devuelve datos)
- Linea "QUERY=". Query a ejecutar. Debe ser en una sola linea pero admite toda la complejidad que pueda tener una query. Si se piensa usar para automatizar por ejemplo un informe basado en fecha, es recomendable preparar una query que use la hora del sistema y así evitar cambiarla constantemente.
	- Si la query está preparada para devolver un solo registro, devuelve el registro consultado para imprimirlo en el LOG. Esto es útil a la hora de hacer querys que devuelban un solo registro para indicar si la informacion cargada es correcta o no.
- Linea "MULTI_QUERY=". Si en lugar de una query, se quiere ejecutar más de una para guardarlas en una excel en distintas hojas, utilizar este parametro en lugar de QUERY. Separar las querys por ";". Es necesario informar el parametro MULTI_SALIDA para dar nombre a cada hoja.
- Linea "MULTI_SALIDA=". Separar por ";" cada una de las hojas que corresponde con cada una de las querys del parametro MULTI_QUERY.
- Linea "COMPRESION=". Si es ZIP, el archivo destino se comprime. En otro caso se deja sin comprimir
- Linea "FICHERO_CONDICION=". Fichero ok. Si este fichero no existe, no se ejecuta.
