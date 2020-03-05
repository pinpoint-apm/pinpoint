thrift_path="./src/compiler/linux/"
thrift_bin="thrift-0.12.0"

rc=0
# check thrift binary
echo -n "Check Thrift binary... "
if ! [ -x "${thrift_path}${thrift_bin}" ]; then
    echo "no Thrift binary in path ${thrift_path}"
    ./install-thrift-linux.sh ${thrift_path} ${thrift_bin}
	rc=$?
else
    echo "ok"
fi

if [ $rc -eq 0 ]; then 
    echo "INFO: Autogenerate source code with Thrift"
    mvn generate-sources -P build-thrift -Dmaven.test.skip -Dthrift.executable.path=${thrift_path}${thrift_bin}

    rc=$?
    if [[ $rc != 0 ]] ; then
            echo "ERROR: BUILD FAILED $rc"
            exit $rc
    fi
else
    echo "ERROR: Error occured when Thrift binary is prepared."
    echo "       Please check following link, install Thrift manually, and copy thrift binary to ${thrift_path}${thrift_bin}."
    echo "       https://thrift.apache.org/docs/BuildingFromSource"
	exit 1
fi
