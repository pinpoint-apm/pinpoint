## MySQL JDBC Driver
* Since: Pinpoint 1.5.0
* See: https://www.mysql.com/products/connector/
* Range: mysql/mysql-connector-java [5.0, 5.max], [8.0.11, 8.0.30]
* Range: com.mysql/mysql-connector-j [8.0.31,]
* Not supported: 6.x


### Pinpoint Configuration
pinpoint.config

#### JDBC options.
~~~
# Profile MySQL.
profiler.jdbc.mysql=true
# Allow profiling of setautocommit.
profiler.jdbc.mysql.setautocommit=false
# Allow profiling of commit.
profiler.jdbc.mysql.commit=false
# Allow profiling of rollback.
profiler.jdbc.mysql.rollback=false
# Trace bindvalues for MySQL PreparedStatements (overrides profiler.jdbc.tracesqlbindvalue)
#profiler.jdbc.mysql.tracesqlbindvalue=true
~~~
