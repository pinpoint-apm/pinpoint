# agent
mvn clean install eclipse:eclipse package dependency:copy-dependencies -Dmaven.test.skip

rm -fr ../hippo-testbed/agent
mkdir ../hippo-testbed/agent
cp ./target/hippo-tomcat-profiler-0.0.2.jar ../hippo-testbed/agent

cp ./target/dependency/javassist-3.16.1-GA.jar ../hippo-testbed/agent
cp ./target/dependency/hippo-commons-0.0.2.jar ../hippo-testbed/agent
cp ./lib/libthrift-0.8.0.wolog.jar ../hippo-testbed/agent
cp ./runscript/* ../hippo-testbed/agent
