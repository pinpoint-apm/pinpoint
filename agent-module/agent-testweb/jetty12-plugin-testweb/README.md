# pinpoint-jetty12-plugin-testweb

Jetty 12 (EE10 / EE11) test application for Pinpoint agent.

Requires Java 17.

## Install
```
$ mvn -P jdk17 install
```

## Run — EE10 (port 18180)
```
$ mvn -P jdk17 exec:java -Dexec.mainClass="com.pinpoint.test.plugin.jetty12.ee10.Jetty12EE10ServerStarterMain"
```
- http://localhost:18180/status
- http://localhost:18180/async

## Run — EE11 (port 18280)
```
$ mvn -P jdk17 exec:java -Dexec.mainClass="com.pinpoint.test.plugin.jetty12.ee11.Jetty12EE11ServerStarterMain"
```
- http://localhost:18280/status
- http://localhost:18280/async

## Stop
