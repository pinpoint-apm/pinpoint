## Pinpoint Resin plugin configuration

### Resin 3.x Configuration
We currently do not support Resin 3.x.

### Resin 4.x Configuration
Add the following options to the *<server>* configuration in */conf/resin.xml*:
```
<jvm-arg>-javaagent:$PINPOINT_AGENT_HOME/pinpoint-bootstrap-$PINPOINT_VERSION.jar</jvm-arg>
<jvm-arg>-Dpinpoint.agentId=$AGENT_ID</jvm-arg>
<jvm-arg>-Dpinpoint.applicationName=$APPLICATION_NAME</jvm-arg>
```

Add the following option to the *<class-loader>* configuration under *<web-app>* in */config/resin.xml*:
```
<library-loader path="$PINPOINT_AGENT_HOME/plugin"/>
```
