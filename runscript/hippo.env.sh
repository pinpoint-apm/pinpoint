#
# release
#

export HIPPO_AGENT_HOME="/home1/irteam/apps/hippo/agent"

JAVA_OPTS="$JAVA_OPTS -javaagent:$HIPPO_AGENT_HOME/HippoAgent.jar -Dhippo.config=$HIPPO_AGENT_HOME/hippo.config "

CLASSPATH="$HIPPO_AGENT_HOME/lib/javassist.jar:$HIPPO_AGENT_HOME/lib/libthrift-0.8.0.wolog.jar"