# common은 install만 하면 된다.
# bootstrap이 빌드될 때 common의 class를 bootstrap.jar에 포함시킴.

# common lib
./makethrift.sh
mvn clean eclipse:eclipse package install -Dmaven.test.skip
#cp ./target/hippo-commons-0.0.2.jar ../hippo-testbed/hippo-server
