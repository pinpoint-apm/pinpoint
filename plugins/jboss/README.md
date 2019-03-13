## Pinpoint JBoss plugin configuration

### Known Issue
There is a bug in our ASM engine in 1.6.0. In order to trace jboss in 1.6.0, **you must set `profiler.instrument.engine=JAVASSIST` in pinpoint.config**. (The issue has been fixed in 1.6.1)

**You must set jboss log manager starting from pinpoint 1.6.1+**
 - issue : #2612

###  Standalone mode <br/>
 Add following configuration in __standalone.conf__ :- <br/>
```bash 
JAVA_OPTS="$JAVA_OPTS -Djboss.modules.system.pkgs=org.jboss.byteman,org.jboss.logmanager,com.navercorp.pinpoint.bootstrap,
com.navercorp.pinpoint.common,com.navercorp.pinpoint.exception"
JAVA_OPTS="$JAVA_OPTS -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
JAVA_OPTS="$JAVA_OPTS -Xbootclasspath/p:$JBOSS_HOME/modules/system/layers/base/org/jboss/logmanager/main/jboss-logmanager-$JBOSS_LOGMANAGER_VERSION.jar"
JAVA_OPTS="$JAVA_OPTS -javaagent:$PINPOINT_AGENT_HOME/pinpoint-bootstrap-$PINPOINT_VERSION.jar"
JAVA_OPTS="$JAVA_OPTS -Dpinpoint.applicationName=APP-APPLICATION-NAME" 
JAVA_OPTS="$JAVA_OPTS -Dpinpoint.agentId=APP-AGENTID"
```

###  Domain mode <br/>

* Add below configuration in __domain.xml__ :- <br/>
```xml 
 <system-properties>
     ...
    <property name="jboss.modules.system.pkgs" value="org.jboss.logmanager,com.navercorp.pinpoint.bootstrap,
com.navercorp.pinpoint.common,com.navercorp.pinpoint.exception" boot-time="true"/>
    <property name="java.util.logging.manager" value="org.jboss.logmanager.LogManager"/>
    ...
</system-properties>
```
* Add below configuration in __host.xml__ :- <br/>

```xml 
<servers>
    ...
    <server name="server-one" group="main-server-group">
        ...
        <jvm name="default">
            ...
            <jvm-options>
                ...
                <option value="-Xbootclasspath/p:$JBOSS_HOME/modules/system/layers/base/org/jboss/logmanager/main/jboss-logmanager-$JBOSS_LOGMANAGER_VERSION.jar"/>
                <option value="-javaagent:$PINPOINT_AGENT_HOME/pinpoint-bootstrap-$PINPOINT_VERSION.jar"/>
                <option value="-Dpinpoint.applicationName=APP-APPLICATION-NAME"/>
                <option value="-Dpinpoint.agentId=APP-AGENT-1"/>
            </jvm-options>
        </jvm>
        ...
    </server>
    
    <server name="server-two" group="main-server-group" auto-start="true">
            ...
            <jvm name="default">
                ...
                <jvm-options>
                    ...
                    <option value="-Xbootclasspath/p:$JBOSS_HOME/modules/system/layers/base/org/jboss/logmanager/main/jboss-logmanager-$JBOSS_LOGMANAGER_VERSION.jar"/>
                    <option value="-javaagent:$PINPOINT_AGENT_HOME/pinpoint-bootstrap-$PINPOINT_VERSION.jar"/>
                    <option value="-Dpinpoint.applicationName=APP-APPLICATION-NAME"/>
                    <option value="-Dpinpoint.agentId=APP-AGENT-2"/>
                </jvm-options>
            </jvm>
            ...
        </server>
        
    
</servers> 

```

#### Set ```profiler.jboss.traceEjb=true``` for remote ejb based application in *pinpoint.config* file
#### Set ```profiler.jboss.traceEjb=false``` for non-ejb based application in *pinpoint.config* file

### If your application shows up as *STAND_ALONE*
Pinpoint agent throws an exception if multiple class file transformers are registered for a class. Since multiple
plugins register class file transformers for `org.apache.catalina.core.StandardHostValve`, the agent will throw an
exception on start up if they all blindly add class file transformers.
To cirvumvent this issue, JBoss plugin will only register it's class file transformers if the application is detected or
configured to be a *JBOSS* application.

As a result, if your application is not identified as a *JBOSS* application, JBoss class file transformers will not be
registered and your application will not be traced.
When this happens, please manually set `profiler.applicationservertype=JBOSS` in *pinpoint.config*.
 