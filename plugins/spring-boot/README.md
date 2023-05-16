## Spring Boot
* Since: Pinpoint 1.5.0
* See: https://spring.io/projects/spring-boot
* Range: org.springframework.boot/spring-boot [1.2, 2.max]

### Pinpoint Configuration
pinpoint.config

#### Spring Beans options.
~~~
profiler.springboot.enable=true
# Classes for detecting application server type. Comma separated list of fully qualified class names. Wildcard not supported.
profiler.springboot.bootstrap.main=org.springframework.boot.loader.JarLauncher, org.springframework.boot.loader.WarLauncher, org.springframework.boot.loader.PropertiesLauncher
~~~

### Include the Java agent with a Spring Boot
In order to include javaagent in your Spring Boot application, keep in mind the following:
Since `-javaagent` is a JVM option, it must be specified before `-jar argument`.

#### Successful Case
EXECUTABLE.jar is `-jar argument`.
```
java -javaagent:${PINPOINT_AGENT_BOOTSTRAP_PATH} -Dpinpoint.agentId=AGENT -Dpinpoint.applicationName=APP -jar EXECUTABLE.jar
java -javaagent:${PINPOINT_AGENT_BOOTSTRAP_PATH} -jar -Dpinpoint.agentId=AGENT -Dpinpoint.applicationName=APP EXECUTABLE.jar
java -jar -javaagent:${PINPOINT_AGENT_BOOTSTRAP_PATH} -Dpinpoint.agentId=AGENT -Dpinpoint.applicationName=APP EXECUTABLE.jar
```

#### Failure Case
EXECUTABLE.jar is `-jar argument`.
```
java -jar EXECUTABLE.jar -javaagent:${PINPOINT_AGENT_BOOTSTRAP_PATH} -Dpinpoint.agentId=AGENT -Dpinpoint.applicationName=APP
```
