# pinpoint-agent

## executable file
```
/agent-assembly/target/pinpoint-agent-assembly-$VERSION
```


## Dynamic logging options
Set the JVM option in -D format.

### Log file size
Both pinpoint.log and pinpoint_stat.log apply.

**-Dpinpoint.logging.file.size**
* 25m
* 50m
* 100m - default
* 500m

e.g. `-Dpinpoint.logging.file.size=25m`

### Log file last modified
Both pinpoint.log and pinpoint_stat.log apply.

**-Dpinpoint.logging.file.lastmodified**
* 3d
* 7d - default

e.g. `-Dpinpoint.logging.file.lastmodified=3d`

### Log file backup count
Both pinpoint.log and pinpoint_stat.log apply.

**-Dpinpoint.logging.file.rollover-strategy-max**
* 1
* 5 - default
* 10

e.g. `-Dpinpoint.logging.file.rollover-strategy-max=10`

### Log level
pinpoint.log apply

**-Dpinpoint.logging.level**
* DEBUG
* INFO - default
* WARN
* ERROR
* FATAL

 e.g. `-Dpinpoint.logging.level=DEBUG`
 
