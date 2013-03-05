# common lib
./makethrift.sh
mvn clean eclipse:eclipse package install -Dmaven.test.skip
cp ./target/hippo-commons-0.0.2.jar ../hippo-testbed/hippo-server
