mvn generate-sources -P with-thrift -Dmaven.test.skip -Dthrift.executable.path=thrift

rc=$?
if [[ $rc != 0 ]] ; then
        echo "BUILD FAILED $rc"
        exit $rc
fi