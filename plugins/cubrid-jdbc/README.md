## Cubrid JDBC Driver
* Since: Pinpoint 1.6.0
* See: https://www.cubrid.com/
* Range: cubrid/cubrid-jdbc [8.2.0, 11.1.1]

### Pinpoint Configuration
pinpoint.config

#### JDBC options.
~~~
# Profile CUBRID.
profiler.jdbc.cubrid=true
# Allow profiling of setautocommit.
profiler.jdbc.cubrid.setautocommit=false
# Allow profiling of commit.
profiler.jdbc.cubrid.commit=false
# Allow profiling of rollback.
profiler.jdbc.cubrid.rollback=false
# Trace bindvalues for CUBRID PreparedStatements (overrides profiler.jdbc.tracesqlbindvalue)
#profiler.jdbc.cubrid.tracesqlbindvalue=true
~~~
