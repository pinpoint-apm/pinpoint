## Oracle JDBC Driver
* Since: Pinpoint 1.5.0
* See: https://www.oracle.com/
* Range: com.oracle.database.jdbc/ojdbc8 [12, 27]

### Pinpoint Configuration
pinpoint.config

#### JDBC options.
~~~
# Profile Oracle DB.
profiler.jdbc.oracle=true
# Allow profiling of setautocommit.
profiler.jdbc.oracle.setautocommit=false
# Allow profiling of commit.
profiler.jdbc.oracle.commit=false
# Allow profiling of rollback.
profiler.jdbc.oracle.rollback=false
# Trace bindvalues for Oracle PreparedStatements (overrides profiler.jdbc.tracesqlbindvalue)
#profiler.jdbc.oracle.tracesqlbindvalue=true
~~~
