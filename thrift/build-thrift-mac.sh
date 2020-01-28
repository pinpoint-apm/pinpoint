mvn generate-sources -P build-thrift -Dmaven.test.skip -Dthrift.executable.path=thrift

rc=$?
if [[ $rc != 0 ]] ; then
        echo "BUILD FAILED $rc"
        exit $rc
fi