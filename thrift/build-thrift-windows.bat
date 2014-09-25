mvn clean package dependency:copy-dependencies install -P with-thrift -Dmaven.test.skip -Dthrift.executable=./src/compiler/windows/thrift-0.9.1

rc=$?
if [[ $rc != 0 ]] ; then
        echo "BUILD FAILED $rc"
        exit $rc
fi