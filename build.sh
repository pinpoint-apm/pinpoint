# profiler
pushd .
cd ../pinpoint-tomcat-profiler
mvn clean install eclipse:eclipse package dependency:copy-dependencies -Dmaven.test.skip -P withThrift -Dthrift.executable.property=/Users/netspider/DEV-TOOLS/thrift-0.9.0/bin/thrift
popd

# bootstrap
mvn clean install eclipse:eclipse package dependency:copy-dependencies -Dmaven.test.skip

rm -fr ../pinpoint-testbed/agent
mkdir -p ../pinpoint-testbed/agent/lib

cp ./target/pinpoint-profiler-bootstrap-0.0.2-jar-with-dependencies.jar ../pinpoint-testbed/agent/pinpoint-profiler-bootstrap-0.0.2.jar

cp ../pinpoint-tomcat-profiler/target/pinpoint-profiler-0.0.3-SNAPSHOT.jar ../pinpoint-testbed/agent/lib
cp ../pinpoint-tomcat-profiler/target/dependency/javassist-3.16.1-GA.jar ../pinpoint-testbed/agent/lib
cp ../pinpoint-tomcat-profiler/target/dependency/libthrift-0.9.0.jar ../pinpoint-testbed/agent/lib

cp ../pinpoint-tomcat-profiler/target/dependency/log4j-1.2.16.jar  ../pinpoint-testbed/agent/lib
cp ../pinpoint-tomcat-profiler/target/dependency/slf4j-log4j12-1.6.6.jar  ../pinpoint-testbed/agent/lib
cp ../pinpoint-tomcat-profiler/target/dependency/slf4j-api-1.6.6.jar  ../pinpoint-testbed/agent/lib

cp ../pinpoint-tomcat-profiler/runscript/help.txt ../pinpoint-testbed/agent
cp ../pinpoint-tomcat-profiler/runscript/pinpoint.config ../pinpoint-testbed/agent
cp ../pinpoint-tomcat-profiler/runscript/log4j.xml ../pinpoint-testbed/agent/lib