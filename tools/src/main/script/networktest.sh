#! /bin/bash

WORKING_DIR=$(dirname "$0")
cd -P "$WORKING_DIR"/.. > /dev/null

CL_PATH=$(find . -name '*.jar' | tr "\n" :)
echo "CLASSPATH=$CL_PATH"
java -classpath $CL_PATH com.navercorp.pinpoint.tools.NetworkAvailabilityChecker pinpoint.config
