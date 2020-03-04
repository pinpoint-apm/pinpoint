#!/usr/bin/env bash

readonly APACHE_MIRROR_URL="http://mirror.navercorp.com/apache"
readonly DEFAULT_PATH="./src/compiler/linux/"
readonly DEFAULT_BIN="thrift-0.12.0"

if [ -z "$1" ]; then
    tpath=${DEFAULT_PATH}
else
    tpath=$1
fi

if [ -z "$2" ]; then
    tbin=${DEFAULT_BIN}
else
    tbin=$2
fi

readonly tversion=`echo ${tbin} | awk -F'thrift-' '{print $2}'`

# Check if Thrift is installed in system
echo -n "Check if Thrift is installed in system... "
installed_path=`which thrift 2>&1`
if [ $? -eq 0 ]; then
    echo "yes"
    # Check Thrift version
    echo -n "Check if Thrift version is ${tversion}... "
	${installed_path} --version 2>&1 | grep ${tversion} >/dev/null
	if [ $? -eq 0 ]; then
        echo "yes"

        # Copy Thrift binary to Thrift path
        echo "INFO: Copy Thrift Binary to Pinpoint Thrift Path"
        cp ${installed_path} ${tpath}${tbin}

        if [ $? -eq 0 ]; then
            exit 0
		else
		    echo "ERROR: Thrift binary copy failed."
		    exit 1
		fi
		
	else
        echo "no"
	fi
else
    echo "no"
fi

# Check if Thrift is built but not copied to compiler path
echo -n "Check if Thrift is built but not copied to compiler path... "
if [ -x "thrift-${tversion}/compiler/cpp/thrift" ]; then
    echo "yes"

    # Copy Thrift binary to Thrift path
    echo "INFO: Copy Thrift Binary to Pinpoint Thrift Path"
    cp thrift-${tversion}/compiler/cpp/thrift ${tpath}${tbin}
    if [ $? -ne 0 ]; then
	    echo "ERROR: Thrift binary copy failed."
	    exit 1
	fi

    # cleanup
    rm -rf thrift-${tversion}
    if [ $? -ne 0 ]; then
	    echo "WARNING: Thrift build directory cleanup failed. Please remove it manually."
	fi
    exit 0
else
    echo "no"
fi

# Check build environment
echo "INFO: Check Thrift installation environment"
if [ -f "/etc/os-release" ]; then
    # Get Linux distribution name and version.
    DIST_ID=`grep "^ID=" /etc/os-release | awk -F "=" '{print $2;}' | tr -d '"'`
    DIST_VERSION=`grep "^VERSION_ID=" /etc/os-release | awk -F "=" '{print $2;}' | tr -d '"' | awk -F "." '{print $1;}'`

    echo "Linux distribution: ${DIST_ID}"
    echo "Version: ${DIST_VERSION}"
else
    echo "ERROR: This Linux distribution is not supported."
	exit 1
fi

if [[ "${DIST_ID}" = "centos" || "${DIST_ID}" = "rhel" ]]; then
    if ! [ "${DIST_VERSION}" -ge 7 ]; then
        echo "ERROR: This Linux distribution version is not supported."
		exit 1
	fi
else
    echo "ERROR: This Linux distribution is not supported."
	exit 1
fi

# Check pre-installed packages
echo "INFO: Check pre-installed packages"
pkg_count=0
if [[ "${DIST_ID}" = "centos" || "${DIST_ID}" = "rhel" ]]; then
    # Development Tools
	echo -n "Check Development Tools installed... "
    yum grouplist "Development Tools" installed 2>&1 | grep "Installed Groups:" >/dev/null
	if [ $? -eq 0 ]; then
        echo "yes"
	else
        echo "no"
		pkg_arr[${pkg_count}]="Development Tools"
		let "pkg_count += 1"
	fi

	# ant
	echo -n "Check ant installed... "
	yum list ant installed 2>&1 | grep "Installed Packages" >/dev/null
	if [ $? -eq 0 ]; then
        echo "yes"
	else
        echo "no"
		pkg_arr[${pkg_count}]="ant"
		let "pkg_count += 1"
	fi
fi

# Install dependencies and pre-installed packages if needed
if [ ${pkg_count} -ne 0 ]; then
    # If installation of pre-installed packages is required, get root privilege.
    echo "Installation of pre-installed packages is required. Please enter password to get root privilege."
    sudo test 1
    if [ $? -ne 0 ]; then
        echo "ERROR: Authentication failed. Exit install script."
	    exit 1
    fi

    # Install dependencies and pre-installed packages
    echo "INFO: Install Dependencies and Pre-installed Packages"
    if [[ "${DIST_ID}" = "centos" || "${DIST_ID}" = "rhel" ]]; then
	    for pkg in "${pkg_arr[@]}"
		do
		    echo "INFO: Install $pkg"
            if [ "$pkg" = "Development Tools" ]; then
                sudo yum groupinstall -y "Development Tools"
			elif [ "$pkg" = "ant" ]; then
                sudo yum install -y ant
			fi
		done
    fi
fi

# Get Thrift
# download thrift tarball from one of those apache mirrors to ${pinpoint-root}/thrift/src/compiler/linux
cd ${tpath} && wget -T 600 -w 1 -t 3 -c ${APACHE_MIRROR_URL}/thrift/${tversion}/thrift-${tversion}.tar.gz && cd -

# Install Thrift
echo "INFO: Install Thrift"
tar zxf ${tpath}thrift-${tversion}.tar.gz
if [ $? -ne 0 ]; then
    echo "ERROR: Thrift tarball decompression failed."
    exit 1
fi

cd thrift-${tversion}
./configure
if [ $? -ne 0 ]; then
    echo "ERROR: Thrift sourcecode configure failed."
    exit 1
fi

make
if [ $? -ne 0 ]; then
    echo "ERROR: Thrift sourcecode build failed."
    exit 1
fi
cd ..

# Copy Thrift binary to Thrift path
echo "INFO: Copy Thrift Binary to Pinpoint Thrift Path"
cp thrift-${tversion}/compiler/cpp/thrift ${tpath}${tbin}
if [ $? -ne 0 ]; then
    echo "ERROR: Thrift binary copy failed."
    exit 1
fi

# cleanup
rm -rf thrift-${tversion}
if [ $? -ne 0 ]; then
    echo "WARNING: Thrift build directory cleanup failed. Please remove it manually."
fi

echo "INFO: Thrift install completed!"
