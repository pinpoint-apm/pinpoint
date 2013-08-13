#profile
PROFILE=""
if [ "$1" != "" ] ; then
	$PROFILE="-P$1"
fi

# web ui
mvn clean eclipse:eclipse package war:exploded -Dmaven.test.skip $PROFILE