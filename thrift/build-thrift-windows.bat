mvn generate-sources -P build-thrift -Dmaven.test.skip -Dthrift.executable.path=./src/compiler/windows/thrift-0.12.0

rc=$?
if [[ $rc != 0 ]] ; then
        echo "BUILD FAILED $rc"
        exit $rc
fi