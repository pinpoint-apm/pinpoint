VERSION="0.0.4-SNAPSHOT"
DEPLOY_DIR="../pinpoint-testbed/pinpoint-collector"

#profile
PROFILE=""
if [ "$1" != "" ] ; then
	$PROFILE="-P$1"
	echo "*********************"
	echo "USING PROFILE $1"
	echo "*********************"
else
	echo "*********************"
	echo "USING DEFAULT PROFILE"
	echo "*********************"
fi

#server
mvn clean eclipse:eclipse install package dependency:copy-dependencies -Dmaven.test.skip $PROFILE
rc=$?
if [[ $rc != 0 ]] ; then
        echo "BUILD FAILED $rc"
        exit $rc
fi

rm -fr $DEPLOY_DIR
mkdir -p $DEPLOY_DIR/lib

cp ./target/pinpoint-collector-$VERSION.jar $DEPLOY_DIR
rc=$?
if [[ $rc != 0 ]] ; then
        echo "BUILD FAILED $rc"
        exit $rc
fi

cp ./target/dependency/*.jar $DEPLOY_DIR/lib
rc=$?
if [[ $rc != 0 ]] ; then
        echo "BUILD FAILED $rc"
        exit $rc
fi

cp ./runscript/* $DEPLOY_DIR
rc=$?
if [[ $rc != 0 ]] ; then
        echo "BUILD FAILED $rc"
        exit $rc
fi

cp ./target/classes/applicationContext*.xml $DEPLOY_DIR
rc=$?
if [[ $rc != 0 ]] ; then
        echo "BUILD FAILED $rc"
        exit $rc
fi

cp ./target/classes/hbase.properties $DEPLOY_DIR
rc=$?
if [[ $rc != 0 ]] ; then
        echo "BUILD FAILED $rc"
        exit $rc
fi

cp ./target/classes/log4j.xml $DEPLOY_DIR
rc=$?
if [[ $rc != 0 ]] ; then
        echo "BUILD FAILED $rc"
        exit $rc
fi

cp ./target/classes/pinpoint-collector.properties $DEPLOY_DIR
rc=$?
if [[ $rc != 0 ]] ; then
        echo "BUILD FAILED $rc"
        exit $rc
fi