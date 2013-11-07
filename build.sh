#profile
PROFILE=""
if [ "$1" != "" ] ; then
	PROFILE="-P $1"
	echo "*********************"
	echo "USING PROFILE $1"
	echo "*********************"
else
	echo "*********************"
	echo "USING DEFAULT PROFILE"
	echo "*********************"
fi

echo "mvn clean eclipse:eclipse package war:exploded -Dmaven.test.skip $PROFILE"

# web ui
mvn clean eclipse:eclipse package war:exploded -Dmaven.test.skip $PROFILE

rc=$?
if [[ $rc != 0 ]] ; then
        echo "BUILD FAILED $rc"
        exit $rc
fi