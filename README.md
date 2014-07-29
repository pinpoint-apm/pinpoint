Pinpoint-profiler-optional
=========

아래와 같이 maven compile을 하기 위해서는 JDK 1.7+이 필요합니다.
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

환경변수에 JAVA_7_HOME을 잡아주시거나, maven의 settings.xml에 JAVA_7_HOME property를 설정해주세요.