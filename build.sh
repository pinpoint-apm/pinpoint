# agent
mvn clean install eclipse:eclipse package -Dmaven.test.skip

cd ./target/classes
jar cvfM HippoAgent.jar ./src/META-INF/MANIFEST.MF ./*

rm -fr ../../../hippo-testbed/agent
mkdir ../../../hippo-testbed/agent
cp ./HippoAgent.jar ../../../hippo-testbed/agent

cd ../../../hippo-tomcat-profiler
cp ./target/dependency/javassist-3.16.1-GA.jar ../hippo-testbed/agent
cp ./target/dependency/hippo-commons-0.0.1.jar ../hippo-testbed/agent
cp ./lib/libthrift-0.8.0.wolog.jar ../hippo-testbed/agent
cp ./runscript/* ../hippo-testbed/agent
