JMX_OPT="-Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.port=9999 -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false"
java -Xms2g -Xmx2g -classpath `find . -name '*.jar' | tr "\n" :` $JMX_OPT com.nhn.pinpoint.collector.Server >> collector.log &
sleep 0.3
tail -f collector.log