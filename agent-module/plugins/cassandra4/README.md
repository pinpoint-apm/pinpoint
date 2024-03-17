## Cassandra JDBC Driver
* Since: Pinpoint 2.5.0
* See: https://github.com/datastax/java-driver
* Range: com.datastax.oss/java-driver-core [4.1.0, 4.15]

### Pinpoint Configuration
pinpoint.config

#### JDBC options.
~~~
# Share CASSANDRA settings.
profiler.cassandra=true
# SimpleStatement, PreparedStatement, BoundStatement 
profiler.cassandra.tracecqlbindvalue=true
~~~
