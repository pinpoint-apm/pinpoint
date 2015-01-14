#! /bin/bash
CL_PATH=`find ../ -name '*.jar' | tr "\n" :`
echo "CLASSPATH=$CL_PATH"
java -classpath $CL_PATH com.navercorp.pinpoint.profiler.tools.NetworkAvailabilityChecker ../pinpoint.config