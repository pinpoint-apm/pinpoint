#! /bin/bash
# common은 install만 하면 된다.
# bootstrap이 빌드될 때 common의 class를 bootstrap.jar에 포함시킴.

# common lib
mvn clean eclipse:eclipse package dependency:copy-dependencies install -Dmaven.test.skip -Dthrift.executable.property=/Users/netspider/DEV-TOOLS/thrift-0.9.0/bin/thrift

rc=$?
if [[ $rc != 0 ]] ; then
        echo "BUILD FAILED $rc"
        exit $rc
fi