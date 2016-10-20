## Pinpoint Jboss plugin configuration

###  Standalone mode <br/>
 Add following configuration in __standalone.conf__ :- <br/>
```bash 
JAVA_OPTS="$JAVA_OPTS -Djboss.modules.system.pkgs=com.navercorp.pinpoint.bootstrap,
com.navercorp.pinpoint.common,com.navercorp.pinpoint.exception"
JAVA_OPTS="$JAVA_OPTS -javaagent:$PINPOINT_AGENT_HOME/pinpoint-bootstrap-$PINPOINT_VERSION.jar"
JAVA_OPTS="$JAVA_OPTS -Dpinpoint.agentId=APP-AGENTID"
JAVA_OPTS="$JAVA_OPTS -Dpinpoint.applicationName=APP-STANDALONE" 
```

###  Domain mode <br/>

* Add below configuration in __domain.xml__ :- <br/>
```xml 
 <system-properties>
     ...
    <property name="jboss.modules.system.pkgs" value="com.navercorp.pinpoint.bootstrap,
com.navercorp.pinpoint.common,com.navercorp.pinpoint.exception" boot-time="true"/>
    ...
</system-properties>
```
* Add below configuration in __host.xml__ :- <br/>

```xml 
<servers>
    ...
    <server name="server-three" group="other-server-group" auto-start="true">
        <jvm name="default">
            ...
            <jvm-options>
                ...
                <option value="-javaagent:$PINPOINT_AGENT_HOME/pinpoint-bootstrap-$PINPOINT_VERSION.jar"/>
                <option value="-Dpinpoint.agentId=APP-NODE1"/>
                <option value="-Dpinpoint.applicationName=APP-DOMAIN"/>
            </jvm-options>
        </jvm>
        <socket-bindings port-offset="250"/>
    </server>
</servers> 

```

#### Set ```profiler.applicationservertype=JBOSS``` in pinpoint.config file
#### Set ```profiler.jboss.traceEjb=true``` for remote ejb based application in pinpoint.config file
#### Set ```profiler.jboss.traceEjb=false``` for non-ejb based application in pinpoint.config file
