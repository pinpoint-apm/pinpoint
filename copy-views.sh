VERSION="0.0.3-SNAPSHOT"
cp ./src/main/webapp/WEB-INF/views/*.jsp ./target/pinpoint-web-$VERSION/WEB-INF/views
cp ./src/main/webapp/WEB-INF/web.xml ./target/pinpoint-web-$VERSION/WEB-INF
cp ./src/main/webapp/*.html ./target/pinpoint-web-$VERSION
cp -fr ./src/main/webapp/common ./target/pinpoint-web-$VERSION
