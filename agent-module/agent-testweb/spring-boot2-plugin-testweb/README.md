# spring-boot2-plugin-testweb

Spring Boot 2 (Java 8) testweb for agent verification on JDK 8.

## Install
```
$ mvnw -pl agent-module/agent-testweb/spring-boot2-plugin-testweb install -Dmaven.test.skip=true
```

## Run (JDK 8, cmd.exe)
```
> "%JAVA_8_HOME%\bin\java" -javaagent:<agent-dir>\pinpoint-bootstrap.jar ^
    -Dpinpoint.profiler.profiles.active=local ^
    -Dpinpoint.applicationName=PluginTest -Dpinpoint.agentId=PluginTestAgent ^
    -jar target\pinpoint-spring-boot2-plugin-testweb-<version>-exec.jar
```
You can then access here: http://localhost:18080/

## Bootstrap classloader instrumentation check
Run with:
```
-Dprofiler.include=java.util.zip.CheckedInputStream
-Dprofiler.instrument.jdk.allow.classnames=java.util.concurrent.CompletableFuture,java.lang.ProcessBuilder,java.util.function.Supplier,java.util.zip.CheckedInputStream
```
then call `http://localhost:18080/bootstrap/zip` and check the agent log for
`define class:com.navercorp.pinpoint.profiler.instrument.interceptor.InterceptorHolder$$<N> cl:null`.
