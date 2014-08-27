#! /bin/bash
VERSION="1.0.2-SNAPSHOT"
DEPLOY_DIR="../pinpoint-testbed/agent"

# profile
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

# cleanup deploy directory
rm -fr $DEPLOY_DIR

# build profiler
mvn clean install eclipse:eclipse package dependency:copy-dependencies -Dmaven.test.skip $PROFILE

rc=$?
if [[ $rc != 0 ]] ; then
        echo "BUILD FAILED $rc"
        exit $rc
fi

# copy agent directory to Testbed.
cp -r ./target/pinpoint-agent $DEPLOY_DIR
rc=$?
if [[ $rc != 0 ]] ; then
        echo "BUILD FAILED $rc"
        exit $rc
fi