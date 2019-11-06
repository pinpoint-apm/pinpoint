Please read this if you meet this situation:
- pinpoint could collect server info (CPU, Memory) but no trace info.
- no unusual log (WARN or ERROR level). 
- your jetty use --exec parameter.

Problem:
when using jetty with parameter --exec, jetty will create a child process, if the agent parameter (-javaagent:$AGENT_PATH....) is added in start shell, the parameter is added to parent process instead of child process, so the class loaded by jetty will not be rewrited and trace data cannot be collected.

Solution:
add the agent parameter (-javaagent:$AGENT_PATH....) after --exec (in start.ini) instead of in start shell.

