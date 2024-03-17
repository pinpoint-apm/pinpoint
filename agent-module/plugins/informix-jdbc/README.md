## Informix JDBC Driver
* Since: Pinpoint 1.5.0
* See: https://www.ibm.com/
* Range: com.ibm.informix/jdbc [4.10, 4.50]

### Pinpoint Configuration
pinpoint.config

#### options.
~~~
# Profile INFORMIX.
profiler.jdbc.informix=true
# Allow profiling of setautocommit.
profiler.jdbc.informix.setautocommit=false
# Allow profiling of commit.
profiler.jdbc.informix.commit=false
# Allow profiling of rollback.
profiler.jdbc.informix.rollback=false
# Trace bindvalues for INFORMIX PreparedStatements (overrides profiler.jdbc.tracesqlbindvalue)
#profiler.jdbc.informix.tracesqlbindvalue=true
~~~
