#! /bin/bash
mvn clean eclipse:eclipse package dependency:copy-dependencies install -Dmaven.test.skip

rc=$?
if [[ $rc != 0 ]] ; then
        echo "BUILD FAILED $rc"
        exit $rc
fi