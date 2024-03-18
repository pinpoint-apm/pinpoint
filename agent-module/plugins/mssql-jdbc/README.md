## Microsoft JDBC Driver
* Since: Pinpoint 2.0.0
* See: https://github.com/microsoft/mssql-jdbc
* Range: com.microsoft.sqlserver/mssql-jdbc [6.1, 12.1]

### Pinpoint Configuration
pinpoint.config

#### JDBC options.
~~~
profiler.jdbc.mssql=false
profiler.jdbc.mssql.setautocommit=false
profiler.jdbc.mssql.commit=false
profiler.jdbc.mssql.rollback=false
profiler.jdbc.mssql.tracesqlbindvalue=false
~~~
