## DBCP
* Since: Pinpoint 1.5.0
* See: https://commons.apache.org/proper/commons-dbcp/
* Range: commons-dbcp/commons-dbcp [1.0, 1.4]

### Pinpoint Configuration
pinpoint.config

#### HTTP Server options.
~~~
# Profile DBCP.
profiler.jdbc.dbcp=true
profiler.jdbc.dbcp.connectionclose=false
~~~
