# common은 install만 하면 된다.
# bootstrap이 빌드될 때 common의 class를 bootstrap.jar에 포함시킴.

# common lib
./makethrift.sh
mvn clean eclipse:eclipse package dependency:copy-dependencies install -Dmaven.test.skip
#cp ./target/pinpoint-commons-0.0.2.jar ../pinpoint-testbed/pinpoint-server
