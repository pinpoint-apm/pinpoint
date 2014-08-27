#! /bin/bash
VERSION="1.0.2-SNAPSHOT"
OUT_JAR="pinpoint-profiler-optional-$VERSION.jar"
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

# cleanup optional jar in deploy directory
rm $DEPLOY_DIR/$OUT_JAR

mvn clean install eclipse:eclipse package -Dmaven.test.skip $PROFILE

rc=$?
if [[ $rc != 0 ]] ; then
        echo "BUILD FAILED $rc"
        exit $rc
fi

# copy optional jar to deploy directory
cp ./target/$OUT_JAR $DEPLOY_DIR
rc=$?
if [[ $rc != 0 ]] ; then
        echo "BUILD FAILED $rc"
        exit $rc
fi