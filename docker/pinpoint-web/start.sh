#!/bin/bash
set -e
set -x

HBASE_HOST=${HBASE_HOST:-}
HBASE_PORT=${HBASE_PORT:-}

cp /assets/pinpoint-web.properties /usr/local/tomcat/webapps/ROOT/WEB-INF/classes/pinpoint-web.properties
cp /assets/hbase.properties /usr/local/tomcat/webapps/ROOT/WEB-INF/classes/hbase.properties

sed 's/HBASE_HOST/'"${HBASE_HOST}"'/g' -i /usr/local/tomcat/webapps/ROOT/WEB-INF/classes/hbase.properties
sed 's/HBASE_PORT/'"${HBASE_PORT}"'/g' -i /usr/local/tomcat/webapps/ROOT/WEB-INF/classes/hbase.properties

/usr/local/tomcat/bin/catalina.sh run