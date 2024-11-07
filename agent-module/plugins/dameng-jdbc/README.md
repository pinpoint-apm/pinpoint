## Dameng JDBC Driver
* Since: Pinpoint 3.1.0
* See: https://www.dameng.com/DM8.html
* Range:        com.dameng:DmJdbcDriver18:[8.1.1.193, 8.max)


### Pinpoint Configuration
pinpoint.config

#### JDBC options.

# Profile Dameng JDBC
profiler.jdbc.dameng=true
# Allow profiling of setautocommit.
profiler.jdbc.dameng.setautocommit=false
# Allow profiling of commit.
profiler.jdbc.dameng.commit=false
# Allow profiling of rollback.
profiler.jdbc.dameng.rollback=false
# Trace bindvalues for dameng PreparedStatements (overrides profiler.jdbc.tracesqlbindvalue)
#profiler.jdbc.dameng.tracesqlbindvalue=true

