VERSION="0.0.4-SNAPSHOT"
DEPLOY_DIR="../pinpoint-testbed/pinpoint-collector"

#profile
PROFILE=""
if [ "$1" != "" ] ; then
	$PROFILE="-P$1"
fi

#server
mvn clean eclipse:eclipse install package dependency:copy-dependencies -Dmaven.test.skip $PROFILE

rm -fr $DEPLOY_DIR
mkdir -p $DEPLOY_DIR/lib

cp ./target/pinpoint-collector-$VERSION.jar $DEPLOY_DIR
cp ./target/dependency/*.jar $DEPLOY_DIR/lib
cp ./runscript/* $DEPLOY_DIR
cp ./target/classes/applicationContext*.xml $DEPLOY_DIR
cp ./target/classes/hbase.properties $DEPLOY_DIR
cp ./target/classes/log4j.xml $DEPLOY_DIR
cp ./target/classes/pinpoint-collector.properties $DEPLOY_DIR