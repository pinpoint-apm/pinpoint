Add the agent parameter (-javaagent:$AGENT_PATH....) in start shell under the domains

For example:
1. In Windows, you should find the file: 'startWeblogic.cmd'. Then, add the agent parameter (-javaagent:$AGENT_PATH....) after 'set SAVE_JAVA_OPTIONS=%JAVA_OPTIONS%' as following:

set SAVE_JAVA_OPTIONS=%JAVA_OPTIONS%  -javaagent:$AGENT_PATH....

2. In Linux(Unix), you should find the file: 'startWeblogic.sh'. Then, add the agent parameter (-javaagent:$AGENT_PATH....) after 'SAVE_JAVA_OPTIONS="${JAVA_OPTIONS}' as following:

SAVE_JAVA_OPTIONS="${JAVA_OPTIONS} -javaagent:$AGENT_PATH...." 
