## Pinpoint Tomcat plugin configuration

### Running embedded Tomcat or if your application shows up as *STAND_ALONE*
Pinpoint agent throws an exception if multiple class file transformers are registered for a class. Since multiple
plugins register class file transformers for `org.apache.catalina.core.StandardHostValve`, the agent will throw an
exception on start up if they all blindly add class file transformers.
To cirvumvent this issue, Tomcat plugin will only register it's class file transformers if the application is detected
or configured to be a *Tomcat* application.

As a result, if your application is not identified as a *Tomcat* application, Tomcat class file transformers will not be
registered and your application will not be traced.
When this happens, please manually set `profiler.applicationservertype=TOMCAT` in *pinpoint.config*.
 