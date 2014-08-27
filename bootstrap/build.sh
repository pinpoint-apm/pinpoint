#! /bin/bash
VERSION="1.0.2-SNAPSHOT"

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

# build bootstrap
mvn clean install eclipse:eclipse package dependency:copy-dependencies -Dmaven.test.skip $PROFILE
rc=$?
if [[ $rc != 0 ]] ; then
        echo "BUILD FAILED $rc"
        exit $rc
fi