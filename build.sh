# profiler
pushd .
cd ../hippo-tomcat-profiler
./build.sh
popd

# bootstrap
mvn clean install eclipse:eclipse package dependency:copy-dependencies -Dmaven.test.skip

rm -fr ../hippo-testbed/agent
mkdir -p ../hippo-testbed/agent/lib

cp ./target/hippo-profiler-bootstrap-0.0.2-jar-with-dependencies.jar ../hippo-testbed/agent/hippo-profiler-bootstrap-0.0.2.jar

cp ../hippo-tomcat-profiler/target/hippo-tomcat-profiler-0.0.2.jar ../hippo-testbed/agent/lib
cp ../hippo-tomcat-profiler/target/dependency/javassist-3.16.1-GA.jar ../hippo-testbed/agent/lib
cp ../hippo-tomcat-profiler/target/dependency/libthrift-0.9.0.jar ../hippo-testbed/agent/lib

cp ../hippo-tomcat-profiler/target/dependency/log4j-1.2.16.jar  ../hippo-testbed/agent/lib
cp ../hippo-tomcat-profiler/target/dependency/slf4j-log4j12-1.6.6.jar  ../hippo-testbed/agent/lib
cp ../hippo-tomcat-profiler/target/dependency/slf4j-api-1.6.6.jar  ../hippo-testbed/agent/lib

cp ../hippo-tomcat-profiler/runscript/* ../hippo-testbed/agent
