VERSION="0.0.4-SNAPSHOT"

#server
mvn clean eclipse:eclipse install package dependency:copy-dependencies -Dmaven.test.skip

rm -fr ../pinpoint-testbed/pinpoint-collector
mkdir -p ../pinpoint-testbed/pinpoint-collector/lib

cp ./target/pinpoint-collector-$VERSION.jar ../pinpoint-testbed/pinpoint-collector
cp ./target/dependency/*.jar ../pinpoint-testbed/pinpoint-collector/lib
cp ./runscript/* ../pinpoint-testbed/pinpoint-collector