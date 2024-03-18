## MariaDB JDBC Driver
* Since: Pinpoint 1.5.0
* See: https://mariadb.com/
* Range: org.mariadb.jdbc/mariadb-java-client [1.1, 3.1]

### Pinpoint Configuration
pinpoint.config

#### JDBC options.
~~~
# Profile MariaDB
profiler.jdbc.mariadb=true
# Allow profiling of setautocommit.
profiler.jdbc.mariadb.setautocommit=false
# Allow profiling of commit.
profiler.jdbc.mariadb.commit=false
# Allow profiling of rollback.
profiler.jdbc.mariadb.rollback=false
# Trace bindvalues for MariaDB PreparedStatements (overrides profiler.jdbc.tracesqlbindvalue)
#profiler.jdbc.mariadb.tracesqlbindvalue=true
~~~
