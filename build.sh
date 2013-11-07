VERSION="0.0.4"
DEPLOY_DIR="../pinpoint-testbed/pinpoint-collector/webapps"
FILENAME="pinpoint-collector-$VERSION.war"

rm $DEPLOY_DIR/$FILENAME 

#profile
PROFILE=""
if [ "$1" != "" ] ; then
	PROFILE="-P $1"
	echo "*********************"
	echo "USING PROFILE $1"
	echo "*********************"
else
	echo "*********************"
	echo "USING DEFAULT PROFILE"
	echo "*********************"
fi

rm -fr $DEPLOY_DIR/*
rc=$?
if [[ $rc != 0 ]] ; then
        echo "BUILD FAILED $rc"
        exit $rc
fi

#server
mvn clean eclipse:eclipse install package dependency:copy-dependencies -Dmaven.test.skip $PROFILE
rc=$?
if [[ $rc != 0 ]] ; then
        echo "BUILD FAILED $rc"
        exit $rc
fi

cp ./target/pinpoint-collector-$VERSION.war $DEPLOY_DIR
rc=$?
if [[ $rc != 0 ]] ; then
        echo "BUILD FAILED $rc"
        exit $rc
fi