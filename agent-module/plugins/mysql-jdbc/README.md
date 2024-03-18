## MySQL JDBC Driver
* Since: Pinpoint 1.5.0
* See: https://www.mysql.com/products/connector/
* Range: mysql/mysql-connector-java [2.0, 8.0]


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
