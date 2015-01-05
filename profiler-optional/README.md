# pinpoint-profiler-optional

**pinpoint-profiler-optional** is an optional package for pinpoint-profiler, adding features that are implemented using APIs available in JDK 7 or later.

## Requirements
In order to build pinpoint-profiler-optional, the following requirements must be met:

* JDK 7+ installed
* ```JAVA_7_HOME``` environment variable set to JDK 7 home directory

Once the ```JAVA_7_HOME``` environment is set, the *maven-compiler-plugin* invokes the JDK 7 compiler to compile the optional package:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>2.5.1</version>
    <inherited>true</inherited>
    <configuration>
        <source>1.7</source>
        <target>1.7</target>
        <debug>${compiler-debug}</debug>
        <optimize>true</optimize>
        <fork>true</fork>
        <verbose>true</verbose>
        <compilerVersion>1.7</compilerVersion>
        <executable>${JAVA_7_HOME}/bin/javac</executable>
        <encoding>UTF-8</encoding>
    </configuration>
</plugin>
```
