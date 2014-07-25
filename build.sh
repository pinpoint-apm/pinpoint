#! /bin/bash
# profiler-optional lib
mvn clean eclipse:eclipse package install -Dmaven.test.skip

rc=$?
if [[ $rc != 0 ]] ; then
        echo "BUILD FAILED $rc"
        exit $rc
fi