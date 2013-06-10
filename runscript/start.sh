java -Xms1g -Xmx1g -classpath `find . -name '*.jar' | tr "\n" :` com.nhn.pinpoint.collector.Server >> collector.log &
sleep 0.3
tail -f collector.log