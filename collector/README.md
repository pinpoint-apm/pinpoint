# pinpoint-collector

## build

```
mvnw install -pl collector -am -Dmaven.test.skip=true
```
or
```
cd collector
../mvnw install -pl collector -am -Dmaven.test.skip=true -f ../pom.xml
```


## run
```
java -jar -Dpinpoint.zookeeper.address=$ZOOKEEPER_ADDRESS collector/target/deploy/pinpoint-collector-boot-$VERSION.jar
```

spring-profiles
```
java -jar -Dpinpoint.zookeeper.address=$ZOOKEEPER_ADDRESS -Dspring.profiles.active=release collector/target/deploy/pinpoint-collector-boot-$VERSION.jar
```

-----------

## Collector port
## gRPC port
| port | protocol | type  |
|------|----------|-------|
| 9991 | TCP      | agent |
| 9992 | TCP      | span  |
| 9993 | TCP      | stat  |

## Configuration for development environment
Use /config directory [External Application Properties](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config.files)
- Create property file in `/resources/config/`
  web/src/main/resources/config/MyConfig.properties

## Jitter-based Scheduling

The collector supports jitter-based scheduling for the AvgMax and Statistics link schedulers to distribute HBase load more evenly across time when multiple collectors are running.

### Why use jitter?
When multiple collector instances start at the same time, they execute flush tasks simultaneously, causing load spikes on HBase. Jitter introduces random initial delays for each DAO's flush task, spreading the load over time.

### Configuration

Enable jitter in your `pinpoint-collector.properties`:

```properties
# Enable jitter for AvgMax link scheduler
collector.map-link.avg.jitter.enabled=true
# Jitter spread percentage (0.0 to 1.0, default 0.5 = 50%)
collector.map-link.avg.jitter.spread=0.5

# Enable jitter for Statistics link scheduler
collector.map-link.stat.jitter.enabled=true
# Jitter spread percentage (0.0 to 1.0, default 0.5 = 50%)
collector.map-link.stat.jitter.spread=0.5
```

### Behavior

- **Without jitter** (default): All DAOs flush at the same fixed intervals
- **With jitter**: Each DAO execution delay is jittered symmetrically around the base interval

The jitter is applied using a spread percentage (based on HBase's JitterScheduledThreadPoolExecutor implementation):
- With spread=0.5 (50%), a 5000ms interval becomes 2500ms to 7500ms randomly
- The delay varies as: `baseInterval +/- (baseInterval * spread)`
- This provides balanced distribution, avoiding both too-early and too-late executions

This helps reduce synchronized load spikes when multiple collectors are deployed.