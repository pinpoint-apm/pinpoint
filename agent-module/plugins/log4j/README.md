## Log4j
* Since: Pinpoint 1.5.0
* See: https://logging.apache.org/log4j/2.x/
* Range: log4j/log4j [1.1, 1.2]

### Pinpoint Configuration
pinpoint.config

#### Logging options.
~~~
profiler.log4j.logging.transactioninfo=false

# replace logger pattern automatically
# %message, $msg or %m would be replace with "TxId:%X{PtxId} %msg"
# in the search config, put the longest/longer variable first.
# variables/aliases: https://logging.apache.org/log4j/1.2/apidocs/org/apache/log4j/PatternLayout.html
#profiler.log4j.logging.pattern.replace.enable=false
#profiler.log4j.logging.pattern.replace.search=%m
#profiler.log4j.logging.pattern.replace.with="TxId:%X{PtxId} %m"
~~~
