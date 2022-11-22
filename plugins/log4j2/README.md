## Log4j2
* Since: Pinpoint 1.8.0
* See: https://logging.apache.org/log4j/2.x/
* Range: org.apache.logging.log4j/log4j-core [2.1, 2.19]

### Pinpoint Configuration
pinpoint.config

#### Logging options.
~~~
profiler.log4j2.logging.transactioninfo=false

# replace logger pattern automatically
# %message, $msg or %m would be replace with "TxId:%X{PtxId} %msg"
# in the search config, put the longest/longer variable first.
# variables/aliases: https://logging.apache.org/log4j/2.x/manual/layouts.html under section "Patterns"
#profiler.log4j2.logging.pattern.replace.enable=false
#profiler.log4j2.logging.pattern.replace.search=%message,%msg,%m
#profiler.log4j2.logging.pattern.replace.with="TxId:%X{PtxId} %msg"
~~~
