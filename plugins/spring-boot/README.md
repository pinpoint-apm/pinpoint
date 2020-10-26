## Pinpoint Spring Boot Plugin

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
