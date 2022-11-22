## Postgresql JDBC Driver
* Since: Pinpoint 1.6.0
* See: https://jdbc.postgresql.org/
* Range: org.postgresql/postgresql [9.2, 42.5]

### Pinpoint Configuration
pinpoint.config

#### JDBC options.
~~~
# Profile PostgreSQL.
profiler.jdbc.postgresql=true
# trace bindvalues for PreparedStatements
profiler.jdbc.postgresql.tracesqlbindvalue=true
# Allow profiling of setautocommit.
profiler.jdbc.postgresql.setautocommit=false
# Allow profiling of commit.
profiler.jdbc.postgresql.commit=false
# Allow profiling of rollback.
profiler.jdbc.postgresql.rollback=false
~~~
