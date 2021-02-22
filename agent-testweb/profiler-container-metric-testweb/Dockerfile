FROM openjdk:11 
ARG JAR_FILE=target/deploy/*.jar
COPY ${JAR_FILE} container-profiler-testweb.jar
ENTRYPOINT ["java","-javaagent:/home/pinpoint/agent/target/pinpoint-agent-2.3.0-SNAPSHOT/pinpoint-bootstrap.jar","-Dpinpoint.profiler.profiles.active=local","-Dpinpoint.applicationName=ProfilerTest","-Dpinpoint.agentId=ProfilerTestAgent","-Dprofiler.transport.grpc.collector.ip=host.docker.internal","-Dserver.port=18080","-Dlogging.level.root=info","-Dspringdoc.swagger-ui.path=/","-jar","/container-profiler-testweb.jar"]