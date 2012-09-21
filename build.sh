#server
mvn clean eclipse:eclipse package dependency:copy-dependencies -Dmaven.test.skip
rm -fr ../hippo-testbed/hippo-server
mkdir -p ../hippo-testbed/hippo-server/lib
cp ./target/hippo-server-0.0.1.jar ../hippo-testbed/hippo-server
cp ./target/dependency/*.jar ../hippo-testbed/hippo-server/lib
cp ./runscript/* ../hippo-testbed/hippo-server
