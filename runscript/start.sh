java -Xms1g -Xmx1g -classpath `find . -name '*.jar' | tr "\n" :` com.profiler.server.Server >> server.log &
sleep 0.3
tail -f server.log