thrift_path="./src/compiler/linux/"
thrift_version="thrift-0.10.0"

rc=0
# check thrift binary
if ! [ -x "${thrift_path}${thrift_version}" ]; then
    echo -e "INFO: Thrift is not installed. Do you want to install Thrift? [y/n] "
	read resp
	if [[ "$resp" = "y" || "$resp" = "Y" ]]; then
        ./install-thrift-linux.sh ${thrift_path} ${thrift_version}
		rc=$?
	elif [[ "$resp" = "n" || "$resp" = "N" ]]; then
	    echo "ERROR: Cannot build source code without thrift. Exit this script."
        exit 1
    else
        #echo -e "Entered wrong response. Do you want to install Thrift? [y/n] "
		echo "Error: Entered wrong response."
		exit 1
    fi
fi

if [ $rc -eq 0 ]; then 
    mvn generate-sources -P with-thrift -Dmaven.test.skip -Dthrift.executable.path=${thrift_path}${thrift_version}

    rc=$?
    if [[ $rc != 0 ]] ; then
            echo "ERROR: BUILD FAILED $rc"
            exit $rc
    fi
else
    echo "ERROR: Error occured when Thrift is installed."
	exit 1
fi
