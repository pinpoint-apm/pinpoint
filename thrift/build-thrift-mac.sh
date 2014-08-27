mvn clean eclipse:eclipse package dependency:copy-dependencies install -P with-thrift -Dmaven.test.skip -Dthrift.executable=thrift

rc=$?
if [[ $rc != 0 ]] ; then
        echo "BUILD FAILED $rc"
        exit $rc
fi