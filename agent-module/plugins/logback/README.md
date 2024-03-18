## Logback
* Since: Pinpoint 1.5.0
* See: https://logback.qos.ch/
* Range: ch.qos.logback/logback-core [1.0, 1.4]

### Pinpoint Configuration
pinpoint.config

#### Logging options.
~~~
profiler.logback.logging.transactioninfo=false

# replace logger pattern automatically
# %message, $msg or %m would be replace with "TxId:%X{PtxId} %msg"
# in the search config, put the longest/longer variable first.
# variables/aliases: https://github.com/qos-ch/logback/blob/master/logback-classic/src/main/java/ch/qos/logback/classic/PatternLayout.java#L54-L149
#profiler.logback.logging.pattern.replace.enable=false
#profiler.logback.logging.pattern.replace.search=%message,%msg,%m
#profiler.logback.logging.pattern.replace.with="TxId:%X{PtxId} %msg"
~~~
