#! /bin/bash

WORKING_DIR=$(dirname "$0")
cd -P "$WORKING_DIR"/.. > /dev/null

CL_PATH=$(find ./tools -name '*.jar' | tr "\n" :)
echo "CLASSPATH=$CL_PATH"

java -cp $CL_PATH com.navercorp.pinpoint.tools.NetworkAvailabilityChecker ./pinpoint-root.config
