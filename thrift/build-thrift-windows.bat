mvn generate-sources -P with-thrift -Dmaven.test.skip -Dthrift.executable.path=./src/compiler/windows/thrift-0.10.0

rc=$?
if [[ $rc != 0 ]] ; then
        echo "BUILD FAILED $rc"
        exit $rc
fi