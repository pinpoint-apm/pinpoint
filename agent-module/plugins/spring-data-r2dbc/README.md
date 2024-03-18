## Spring Data R2DBC
* Since: Pinpoint 2.5.0
* See: https://spring.io/projects/spring-data-r2dbc
* Range: org.springframework.data/spring-data-r2dbc [1.0, 1.5]
* Range: org.mariadb/r2dbc-mariadb [1.1.2]
* Range: io.r2dbc/r2dbc-h2 [0.9.1]
* Range: org.postgresql/r2dbc-postgresql [0.9.1]
* Range: dev.miku/r2dbc-mysql [0.8.2]
* Range: com.github.jasync-sql/jasync-r2dbc-mysql [2.0.8]
* Range: com.oracle.database.r2dbc/oracle-r2dbc [1.0.0]
* Range: io.r2dbc/r2dbc-mssql [0.9.0]

### Pinpoint Configuration
pinpoint.config

#### Data options.
~~~
# Postgresql, H2, MySQL, Mariadb, Oracle, MsSQL
# Use some JDBC settings.
# For example, the settings for profiler.jdbc.xxx=true/false and profiler.jdbc.xxx.tracesqlbindvalue=true/false are also used by R2DBC.
profiler.spring.data.r2dbc.enable=true
~~~

