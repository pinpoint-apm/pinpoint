## Pinpoint Jboss plugin configuration

###  Standalone mode <br/>
 Add following configuration in __standalone.conf__ :- <br/>
```java 
JAVA_OPTS="$JAVA_OPTS -Djboss.modules.system.pkgs=org.jboss.byteman,org.jboss.logmanager,com.navercorp.pinpoint"
JAVA_OPTS="$JAVA_OPTS -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
JAVA_OPTS="$JAVA_OPTS -Xbootclasspath/p:$JBOSS_HOME/modules/system/layers/base/org/jboss/logmanager/main/jboss-logmanager-1.5.4.Final-redhat-1.jar"
JAVA_OPTS="$JAVA_OPTS -javaagent:$PINPOINT_AGENT_HOME/pinpoint-bootstrap-1.6.0-SNAPSHOT.jar"
JAVA_OPTS="$JAVA_OPTS -Dpinpoint.agentId=APP-AGENTID"
JAVA_OPTS="$JAVA_OPTS -Dpinpoint.applicationName=APP-STANDALONE" 
```

###  Domain mode <br/>

* Add below configuration in __domain.xml__ :- <br/>
```xml 
 <system-properties>
     ...
        
    <property name="jboss.modules.system.pkgs" value="com.singularity,org.jboss.logmanager,com.navercorp.pinpoint" boot-time="true"/>
    <property name="java.util.logging.manager" value="org.jboss.logmanager.LogManager"/>
    ...
    
</system-properties>

<server-group name="other-server-group" profile="full-ha">
    <jvm name="default">
        <heap size="1000m" max-size="1000m"/>
        <permgen max-size="256m"/>
        <jvm-options>
            <option value="-Xbootclasspath/p:$JBOSS_HOME/modules/system/layers/base/org/jboss/logmanager/main/jboss-logmanager-1.5.4.Final-redhat-1.jar"/>
        </jvm-options>
    </jvm>
    <socket-binding-group ref="full-ha-sockets"/>
    <deployments>
    ...
    </deployments>
</server-group>
```
* Add below configuration in __host.xml__ :- <br/>

```xml 
<servers>
    <server name="server-one" group="main-server-group" auto-start="false"/>
    <server name="server-two" group="main-server-group" auto-start="false">
        <socket-bindings port-offset="150"/>
    </server>
    <server name="server-three" group="other-server-group" auto-start="true">
        <jvm name="default">
            <heap size="1500m" max-size="1500m"/>
            <jvm-options>
                <option value="-XX:+UseParallelGC"/>
                <option value="-XX:ParallelGCThreads=1"/>
                <option value="-javaagent:$PINPOINT_AGENT_HOME/pinpoint-bootstrap-1.6.0-SNAPSHOT.jar"/>
                <option value="-Dpinpoint.agentId=APP-NODE1"/>
                <option value="-Dpinpoint.applicationName=APP-DOMAIN"/>
            </jvm-options>
        </jvm>
        <socket-bindings port-offset="250"/>
    </server>
</servers> 

```


* Set ```profiler.jboss.traceEjb=true``` for remote ejb application in pinpoint.config file
