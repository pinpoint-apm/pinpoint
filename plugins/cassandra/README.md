## Cassandra JDBC Driver
* Since: Pinpoint 1.6.0
* See: https://github.com/datastax/java-driver
* Range: com.datastax.cassandra/cassandra-driver-core [2.0, 3.11]

### Pinpoint Configuration
pinpoint.config

#### JDBC options.
~~~
# Profile CASSANDRA.
profiler.cassandra=true
# Trace bindvalues for CASSANDRA PreparedStatements (overrides profiler.jdbc.tracesqlbindvalue)
#profiler.cassandra.tracecqlbindvalue=true
~~~
