#server
mvn clean eclipse:eclipse install package dependency:copy-dependencies -Dmaven.test.skip

rm -fr ../pinpoint-testbed/pinpoint-collector
mkdir -p ../pinpoint-testbed/pinpoint-collector/lib

cp ./target/pinpoint-collector-0.0.3-SNAPSHOT.jar ../pinpoint-testbed/pinpoint-collector
cp ./target/dependency/*.jar ../pinpoint-testbed/pinpoint-collector/lib
cp ./runscript/* ../pinpoint-testbed/pinpoint-collector