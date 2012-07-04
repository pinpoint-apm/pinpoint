mvn clean package
cd ./target/classes
jar cvfM HippoAgent.jar ./src/META-INF/MANIFEST.MF ./*
cp ./HippoAgent.jar ../../
cd ../../
ls -al HippoAgent.jar


