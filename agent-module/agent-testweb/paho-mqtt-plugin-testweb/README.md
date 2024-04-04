
## Prerequirements

- MQTT Broker is required.
  - [Mosquito](https://mosquitto.org) is available as MQTT broker.

## Install
```
$ mvnw -P pinpoint-paho-mqtt-plugin-testweb install -Dmaven.test.skip=true
```

## Run
```
$ mvnw -P pinpoint-paho-mqtt-plugin-testweb spring-boot:start
```
You can then access here: http://localhost:18080/

## Stop
```
$ mvnw -P pinpoint-paho-mqtt-plugin-testweb spring-boot:stop
```
