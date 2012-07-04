#
# local test
#

export HIPPO_AGENT_HOME="/Users/netspider/Documents/workspace_hippo/hippo-testbed/agent"

JAVA_OPTS="$JAVA_OPTS -javaagent:$HIPPO_AGENT_HOME/hippo-tomcat-profiler-0.0.1.jar -Dhippo.config=$HIPPO_AGENT_HOME/hippo.config "

CLASSPATH="$HIPPO_AGENT_HOME/lib/javassist.jar:$HIPPO_AGENT_HOME/lib/libthrift-0.8.0.wolog.jar"