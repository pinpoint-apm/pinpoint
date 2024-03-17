## JTDS
* Since: Pinpoint 1.5.0
* See: https://jtds.sourceforge.net/
* Range: net.sourceforge.jtds/jtds [1.2, 1.3]

### Pinpoint Configuration
pinpoint.config

#### options.
~~~
# Profile jTDS.
profiler.jdbc.jtds=true
# Allow profiling of setautocommit.
profiler.jdbc.jtds.setautocommit=false
# Allow profiling of commit.
profiler.jdbc.jtds.commit=false
# Allow profiling of rollback.
profiler.jdbc.jtds.rollback=false
# Trace bindvalues for jTDS PreparedStatements (overrides profiler.jdbc.tracesqlbindvalue)
#profiler.jdbc.jtds.tracesqlbindvalue=true
~~~
