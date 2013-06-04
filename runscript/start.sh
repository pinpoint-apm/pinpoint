java -Xms1g -Xmx1g -classpath `find . -name '*.jar' | tr "\n" :` Server >> collector.log &
sleep 0.3
tail -f collector.log