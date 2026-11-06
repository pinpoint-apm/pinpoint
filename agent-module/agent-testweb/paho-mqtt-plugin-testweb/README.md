
## Prerequirements

- MQTT Broker is required.
  - [Mosquito](https://mosquitto.org) is available as MQTT broker.

## Install
```
$ ./mvnw -pl agent-module/agent-testweb/paho-mqtt-plugin-testweb install -Dmaven.test.skip=true
```

## Run
```
$ ./mvnw -pl agent-module/agent-testweb/paho-mqtt-plugin-testweb spring-boot:start
```
You can then access here: http://localhost:18080/

## Stop
```
$ ./mvnw -pl agent-module/agent-testweb/paho-mqtt-plugin-testweb spring-boot:stop
```
