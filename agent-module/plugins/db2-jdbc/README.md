## IBM DB2 JDBC Driver
* Since: Pinpoint 3.1.0
* See: https://www.ibm.com/products/db2
* Range: com.ibm.db2/jcc [11.1.4.4, 12.1.4.0]

### Pinpoint Configuration
pinpoint.config

#### JDBC options.
~~~
# Profile DB2.
profiler.jdbc.db2=true
# Allow profiling of setautocommit.
profiler.jdbc.db2.setautocommit=false
# Allow profiling of commit.
profiler.jdbc.db2.commit=false
# Allow profiling of rollback.
profiler.jdbc.db2.rollback=false
# Trace bindvalues for DB2 PreparedStatements (overrides profiler.jdbc.tracesqlbindvalue)
#profiler.jdbc.db2.tracesqlbindvalue=true
~~~