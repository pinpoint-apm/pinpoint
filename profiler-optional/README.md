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

환경변수에 JAVA_7_HOME을 잡아주시고, maven의 settings.xml에 JAVA_7_HOME property를 아래와 같이 설정해주세요.

(m2e 플러그인을 사용한 maven 빌드를 하시려면 settings.xml을 수정하셔야 합니다.)
```xml
<settings>
    <profiles>
      <profile>
          <id>JAVA_7_HOME</id>
            <properties>
              <JAVA_7_HOME>JDK 1.7+ 패스</JAVA_7_HOME>
            </properties>
      </profile>
    </profiles>
    <activeProfiles>     
        <activeProfile>JAVA_7_HOME</activeProfile>   
    </activeProfiles>
</settings> 
```