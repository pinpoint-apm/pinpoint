def_path="./src/compiler/linux/"
def_version="thrift-0.10.0"

if [ -z "$1" ]; then
    tpath=${def_path}
else
    tpath=$1
fi

if [ -z "$2" ]; then
    tversion=${def_version}
else
    tversion=$2
fi

# Get user's agreement for installation
echo "INFO: Thrift will be installed. Please enter password."
sudo test 1
if [ $? -ne 0 ]; then
    echo "ERROR: Authentication failed. Exit install script."
	exit 1
fi

# Check build environment
echo "INFO: Check Installation Environment"
if [ -f "/etc/os-release" ]; then
    # Get Linux distribution name and version.
    DIST_ID=`cat /etc/os-release | grep "^ID=" | awk -F "=" '{print $2;}' | tr -d '"'`
    DIST_VERSION=`cat /etc/os-release | grep "^VERSION_ID=" | awk -F "=" '{print $2;}' | tr -d '"' | awk -F "." '{print $1;}'`
else
    echo "ERROR: This Linux destribution is not supported."
	exit 1
fi

if [[ "${DIST_ID}" = "centos" || "${DIST_ID}" = "rhel" ]]; then
    if [ "${DIST_VERSION}" -ge 7 ]; then
        echo "Linux distribution: ${DIST_ID}"
        echo "Version: ${DIST_VERSION}"
	else
        echo "ERROR: This Linux destribution version is not supported."
		exit 1
	fi
else
    echo "ERROR: This Linux destribution is not supported."
	exit 1
fi

# Install dependencies and pre-installed packages
echo "INFO: Install Dependencies and Pre-installed Packages"
if [[ "${DIST_ID}" = "centos" || "${DIST_ID}" = "rhel" ]]; then
    sudo yum groupinstall -y "Development Tools"
    sudo yum install -y ant
fi

# Get Thrift
## Suppose Pinpoint project include Thrift source code tar ball
## and the tar ball's path is $pinpoint-root/thrift/src/compiler/linux

# Install Thrift
echo "INFO: Install Thrift"
tar zxf ${tpath}${tversion}.tar.gz
cd ${tversion}
./configure
make
sudo make install
cd ..

# Copy Thrift binary to Thrift path
echo "INFO: Copy Thrift Binary to Pinpoint Thrift Path"
installed_path=`which thrift`
cp ${installed_path} ${tpath}${tversion}

# Cleanup
sudo rm -rf ${tversion}

echo "INFO: Thrift install completed!"
