## Pinpoint Spring Boot plugin configuration

### If your application shows up as *STAND_ALONE*
If your application is running Tomcat on Spring Boot, and is not identified as a *SPRING_BOOT* application, Tomcat
related calls will not be traced. This usually happens if you launch through your application's main class directly
instead of Spring Boot executable jar.

When this happens, please manually set `profiler.applicationservertype=SPRING_BOOT` in *pinpoint.config*.
 