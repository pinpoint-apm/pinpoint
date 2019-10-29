---
title: 설치
keywords: pinpoint, pinpoint homepage, install, start, installation
last_updated: 2018년 2월 1일
sidebar: mydoc_sidebar
permalink: installation.html
disqus: false
---

당신의 Pinpoint instance를 설정하려면 [**latest release**](https://github.com/naver/pinpoint/releases/latest)에서 빌드 결과를 다운로드 할 수 있습니다. 또는 Git clone에서 수동으로 빌드하십시오
당신의 Pinpoint instance를 실행하기 위해서는 아래 구성 요소를 필요로 합니다.

* **HBase** (for storage)
* **Pinpoint Collector** (deployed on a web container)
* **Pinpoint Web** (deployed on a web container)
* **Pinpoint Agent** (attached to a java application for profiling)

간단한 quickstart project를 시작하려면 [quick-start guide](./quickstart.html)를 참조하십시오.

## 빠른 설치 개요
1. Hbase ([details])(#1-hbase))
        1. Set up HBase cluster - [Apache HBase](http://hbase.apache.org)
	      2. Create HBase Schemas - feed `/scripts/hbase-create.hbase` to hbase shell.
2. Build Pinpoint (Optional)([details](#2-building-pinpoint-optional)) - binaries를 사용할 필요가 없는 경우.([here](https://github.com/naver/pinpoint/releases)).
        1. Clone Pinpoint - `git clone $PINPOINT_GIT_REPOSITORY`
	      2. JAVA_HOME 환경 변수를 JDK 8 홈 디렉토리에 설정하십시오.
	      3. JAVA_6_HOME 환경 변수를 JDK 6 홈 디렉토리에 설정하십시오 (1.6.0_45 recommended).
	      4. JAVA_7_HOME 환경 변수를 JDK 7 홈 디렉토리에 설정하십시오 (1.7.0_80 recommended).
	      5. JAVA_8_HOME 환경 변수를 JDK 8 홈 디렉토리에 설정하십시오.
	      6. JAVA_9_HOME 환경 변수를 JDK 9 홈 디렉토리에 설정하십시오.
	      7. Run `./mvnw clean install -DskipTests=true` (or `./mvnw.cmd` for Windows) 
3. Pinpoint Collector ([details](#3-pinpoint-collector))
	      1. 웹 컨테이너에 *pinpoint-collector-$VERSION.war* 배포.
	      2. *pinpoint-collector.properties*, *hbase.properties* 구성.
	      3. 컨테이너 시작.
4. Pinpoint Web ([details](#4-pinpoint-web))
       	1. 웹 컨테이너에 *pinpoint-web- $ VERSION.war*를 ROOT 응용 프로그램으로 배포.
	      2. *pinpoint-collector.properties*, *hbase.properties* 구성.
      	3. 컨테이너 시작.
5. Pinpoint Agent ([details](#5-pinpoint-agent))
	      1. *pinpoint-agent/*를 편리한 위치로 추출/이동 (`$AGENT_PATH`).
	      2. '-javaagent : $ AGENT_PATH / pinpoint-bootstrap- $ VERSION.jar' JVM 인수를 설정하여 에이전트를 Java 애플리케이션에 연결하십시오.
	      3. '-Dpinpoint.agentId' 및'-Dpinpoint.applicationName' 명령 행 인수 설정.  
		         a) * agent id *를 동적으로 변경하여 컨테이너화 된 환경에서 에이전트를 시작하는 경우 '-Dpinpoint.container' 명령 행 인수를 추가하십시오
        4. 위의 옵션으로 Java 응용 프로그램을 시작하십시오.
  
## 1. HBase  
Pinpoint는 수집기 및 웹의 저장소 backend로 HBase를 사용합니다.

자신의 cluster를 설정하려면 [HBase website] (http://hbase.apache.org)에서 지침을 확인하십시오. HBase 호환성 표는 다음과 같습니다.

{% include_relative compatibilityHbase.md %}

HBase를 실행 한 후에는 수집기와 웹이 올바르게 구성되어 있고 HBase에 연결할 수 있는지 확인하십시오.
  
### HBase에 Schemas 생성
Pinpoint용 테이블을 생성하는 데 사용할 수 있는 2개의 스크립트가 있습니다: *hbase-create.hbase*와 *hbase-create-snappy.hbase*. 빠른 압축에는 * hbase-create-snappy.hbase* 를 사용하십시오(requires [snappy](http://google.github.io/snappy/)), 그렇지 않으면 *hbase-create.hbase* 를 대신 사용하십시오. 

이러한 스크립트를 실행하려면 아래와 같이 HBase 셸에 공급하십시오.

`$HBASE_HOME/bin/hbase shell hbase-create.hbase`

전체 스크립트 목록은 [here](https://github.com/naver/pinpoint/tree/master/hbase/scripts "Pinpoint HBase scripts")를 참조하십시오.

## 2. Building Pinpoint (Optional)

두 가지 옵션이 있습니다.

1. [**latest release**](https://github.com/naver/pinpoint/releases/latest)에서 빌드 결과를 다운로드하고 빌드 프로세스를 건너 뛰십시오. ** (권장) **

2. Git 복제본에서 수동으로 Pinpoint를 빌드하십시오.
      
	그렇게하려면 다음 **요구 사항** 이 충족되어야합니다:
	
   * JDK 6 installed ([jdk1.6.0_45](http://www.oracle.com/technetwork/java/javase/downloads/java-archive-downloads-javase6-419409.html#jdk-6u45-oth-JPR) recommended)
   * JDK 7 installed ([jdk1.7.0_80](http://www.oracle.com/technetwork/java/javase/downloads/java-archive-downloads-javase7-521261.html#jdk-7u80-oth-JPR) recommended)
   * JDK 8 installed
   * JDK 9 installed 
   	* JAVA_HOME environment variable set to JDK 8 home directory.
   	* JAVA_6_HOME environment variable set to JDK 6 home directory.
  	* JAVA_7_HOME environment variable set to JDK 7 home directory.
	* JAVA_8_HOME environment variable set to JDK 8 home directory.
	* JAVA_9_HOME environment variable set to JDK 9 home directory.
	
	또한 각 Pinpoint 구성 요소를 실행하는 데 필요한 Java 버전은 다음과 같습니다.
	
	{% include_relative compatibilityPinpoint.md %}
	
	
	위의 요구 사항이 충족되면 아래 명령을 실행하십시오(**mvnw** 에 대한 권한을 추가해야 실행할 수 있습니다.):
	
	`./mvnw install -DskipTests=true`
	
	이 방법으로 빌드 된 기본 에이전트는 기본적으로 로그 레벨을 DEBUG로 설정합니다. 릴리스 할 에이전트를 빌드 중이고 더 높은 로그 레벨이 필요한 경우 빌드 시 maven 프로파일을 *release* 로 설정할 수 있습니다.
	`./mvnw install -Prelease -DskipTests=true`
	
	maven 로컬 저장소 경로 또는 클래스 경로에 멀티 바이트 문자가 있으면 빌드가 실패 할 수 있습니다.
	
	이 가이드는 핀포인트 홈 디렉토리의 전체 경로를 `$PINPOINT_PATH` 라고 나타낸 것 입니다.
	
당신의 방법과는 상관없이, 다음 섹션에서 언급 한 파일과 디렉토리로 끝나야합니다.

## 3. Pinpoint Collector
웹 컨테이너에 배포 할 수있는 다음 ** war ** 파일이 있어야합니다.
	
*pinpoint-collector-$VERSION.war*

이 파일의 경로는 수동으로 빌드 한 경우 *$ PINPOINT_PATH / collector / target / pinpoint-collector- $ VERSION.war* 와 같아야합니다.
	
### Installation
Pinpoint Collector는 배포 가능한 war 파일로 패키지 되어있으므로 다른 웹 응용 프로그램과 마찬가지로 웹 컨테이너에 배포 할 수 있습니다.	

### Configuration
Pinpoint Collector에 사용 가능한 구성 파일은 두 가지가 있습니다: *pinpoint-collector.properties*, and *hbase.properties*.

* pinpoint-collector.properties-콜렉터 구성이 포함되어 있습니다. 에이전트의 구성 옵션으로 다음 값을 확인하십시오.
	* `collector.tcpListenPort` (agent's *profiler.collector.tcp.port* - default: 9994)
	* `collector.udpStatListenPort` (agent's *profiler.collector.stat.port* - default: 9995)
	* `collector.udpSpanListenPort` (agent's *profiler.collector.span.port* - default: 9996)
* hbase.properties-HBase에 연결하기위한 구성이 포함되어 있습니다
	* `hbase.client.host` (default: localhost)
	* `hbase.client.port` (default: 2181)

이 파일들은 war 파일 안의 `WEB-INF / classes /`에 있습니다.

[pinpoint-web.properties]에서 기본 구성 파일을 볼 수 있습니다. (https://github.com/naver/pinpoint/blob/master/web/src/main/resources/pinpoint-web.properties), [hbase.properties](https://github.com/naver/pinpoint/blob/master/web/src/main/resources/hbase.properties)

## 4. Pinpoint Web
웹 컨테이너에 배포 할 수있는 다음 **war** 파일이 있어야합니다

*pinpoint-web-$VERSION.war*

이 파일의 경로는 수동으로 빌드 한 경우 *$ PINPOINT_PATH / web / target / pinpoint-web- $ VERSION.war*와 같아야합니다.

Pinpoint Web Supported Browsers:

* Chrome

### Installation
Pinpoint Web은 배포 가능한 war 파일로 패키지되어 있으므로 다른 웹 응용 프로그램과 마찬가지로 웹 컨테이너에 배포 할 수 있습니다. 웹 모듈도 ROOT 애플리케이션으로 배포해야 합니다.

### Configuration
Collector와 유사하게 Pinpoint Web에는 설치와 관련된 구성 파일이 있습니다: *pinpoint-web.properties*, and *hbase.properties*. 

다음 구성 옵션을 확인하십시오:

* hbase.properties - contains configurations to connect to HBase.
	* `hbase.client.host` (default: localhost)
	* `hbase.client.port` (default: 2181)

이 파일들은 war 파일 안의`WEB-INF / classes /`에 있습니다.

[pinpoint-web.properties]에서 기본 구성 파일을 살펴볼 수 있습니다.(https://github.com/naver/pinpoint/blob/master/web/src/main/resources/pinpoint-web.properties), [hbase.properties](https://github.com/naver/pinpoint/blob/master/web/src/main/resources/hbase.properties)

## 5. Pinpoint Agent
다운로드 한 경우 Pinpoint Agent 파일의 압축을 풉니다. 아래 레이아웃이 있는 **핀포인트 에이전트** 디렉토리가 있어야 한다 :

```
pinpoint-agent
|-- boot
|   |-- pinpoint-annotations-$VERSION.jar
|   |-- pinpoint-bootstrap-core-$VERSION.jar
|   |-- pinpoint-bootstrap-java7-$VERSION.jar
|   |-- pinpoint-bootstrap-java8-$VERSION.jar
|   |-- pinpoint-bootstrap-java9-$VERSION.jar
|   |-- pinpoint-commons-$VERSION.jar
|-- lib
|   |-- log4j.xml
|   |-- pinpoint-profiler-$VERSION.jar
|   |-- pinpoint-profiler-optional-$VERSION.jar
|   |-- pinpoint-rpc-$VERSION.jar
|   |-- pinpoint-thrift-$VERSION.jar
|   |-- ...
|-- plugin
|   |-- pinpoint-activemq-client-plugin-$VERSION.jar
|   |-- pinpoint-arcus-plugin-$VERSION.jar
|   |-- ...
|-- pinpoint-bootstrap-$VERSION.jar
|-- pinpoint.config
```
이 디렉토리의 경로는 수동으로 빌드 한 경우  *$ PINPOINT_PATH / agent / target / pinpoint-agent* 와 같아야합니다.

** pinpoint-agent ** 디렉토리의 내용을 원하는 위치로 이동 / 추출 할 수 있습니다. 이 안내서는이 디렉토리의 전체 경로를`$ AGENT_PATH`라고합니다.

> 위의 *lib* 디렉토리에있는 *log4j.xml*을 수정하여 에이전트의 로그 레벨을 변경할 수 있습니다.	

Agent compatibility to Collector table:

{% include_relative compatibilityJava.md %}	

### Installation
Pinpoint Agent는 프로파일링 할 애플리케이션에 연결된 Java 에이전트로 실행됩니다. (예 : Tomcat)

에이전트에 연결하려면, 애플리케이션을 실행할 때 *$ AGENT_PATH/pinpoint-bootstrap-$VERSION.jar*를 * -javaagent JVM 인수로 전달하십시오.

* `-javaagent:$AGENT_PATH/pinpoint-bootstrap-$VERSION.jar`

또한, Pinpoint Agent는 분산 시스템에서 자신을 식별하기 위해 2 개의 명령 행 인수가 필요합니다:

* `-Dpinpoint.agentId` - uniquely identifies the application instance in which the agent is running on
* `-Dpinpoint.applicationName` - groups a number of identical application instances as a single service

애플리케이션 인스턴스를 식별하려면 *pinpoint.agentId*가 전체적으로 고유해야 한다는 점에 유의하고 동일한 *pinpoint.applicationName*을(를) 공유하는 모든 애플리케이션은 단일 서비스의 여러 인스턴스로 처리된다.

컨테이너형 환경에서 에이전트를 시작하는 경우, 컨테이너를 시작할 때마다 *Agent ID*가 자동으로 생성되도록 설정하였을 수 있습니다. 빈번한 배포 및 자동 확장으로 인해 웹 UI가 앞서 시작되어 파괴 된 모든 에이전트 목록으로 인해 혼잡 해집니다. 이러한 경우 에이전트를 시작할 때 위의 두 가지 필수 명령 인수 외에`-Dpinpoint.container`를 추가 할 수 있습니다.

**Tomcat Example**

Tomcat 시작 스크립트(*catalina.sh*)에서 *-javaagent*, *-Dpinpoint.agentId*, *-Dpinpoint.applicationName*를 *CATALINA_OPTS*에 추가하십시오.

<pre>
CATALINA_OPTS="$CATALINA_OPTS <b>-javaagent</b>:$AGENT_PATH/pinpoint-bootstrap-$VERSION.jar"
CATALINA_OPTS="$CATALINA_OPTS <b>-Dpinpoint.agentId</b>=$AGENT_ID"
CATALINA_OPTS="$CATALINA_OPTS <b>-Dpinpoint.applicationName</b>=$APPLICATION_NAME"
</pre>

웹 애플리케이션 프로파일링을 시작하려면 Tomcat을 시작하십시오.

일부 응용 프로그램 서버에는 추가 구성이 필요하거나 주의해야 할 사항이 있습니다. 자세한 내용은 아래 링크를 참조하십시오.
* [JBoss](https://github.com/naver/pinpoint/tree/master/plugins/jboss#pinpoint-jboss-plugin-configuration)
* [Jetty](https://github.com/naver/pinpoint/blob/master/plugins/jetty/README.md)
* [Resin](https://github.com/naver/pinpoint/tree/master/plugins/resin#pinpoint-resin-plugin-configuration)

### Configuration

*$ AGENT_PATH/pinpoint.config*에 Pinpoint Agent에 대한 다양한 구성 옵션이 있습니다.
	
이러한 옵션은 대부분 자체 설명이지만, 반드시 확인해야 하는 구성 옵션은 **Collector ip address**와 **TCP/UDP 포트**입니다. 이러한 값은 에이전트가 *Collector*에 대한 연결을 설정하고 올바르게 작동하기 위해 필요합니다.

다음 값을 *pinpoint.config*에서 적절하게 설정하십시오.

* `profiler.collector.ip` (default: 127.0.0.1)
* `profiler.collector.tcp.port` (collector's *collector.tcpListenPort* - default: 9994)
* `profiler.collector.stat.port` (collector's *collector.udpStatListenPort* - default: 9995)
* `profiler.collector.span.port` (collector's *collector.udpSpanListenPort* - default: 9996)

기본 * pinpoint.config * 파일을 사용 가능한 모든 구성 옵션과 함께 볼 수 있습니다. [here](https://github.com/naver/pinpoint/blob/master/agent/src/main/resources/pinpoint-real-env-lowoverhead-sample.config "pinpoint.config")

## Miscellaneous

### HBase region servers hostname resolution
collector/web은 HBase 영역 서버의 호스트 이름을 확인할 수 있어야 한다는 점에 유의하십시오. 
이는 HBase 지역 서버가 호스트 이름으로 ZooKeeper에 등록되기 때문에, 수집기/웹이 ZooKeeper에게 연결할 지역 서버 목록을 요청하면 호스트명을 받기 때문입니다.
이러한 호스트 이름이 DNS 서버에 있는지 확인하거나 이러한 항목을 collector/web 인스턴스의 *hosts* 파일에 추가하십시오.

### Routing Web requests to Agents

1.5.0부터 핀포인트는 Collector(및 그 반대)를 통해 웹에서 에이전트로 직접 요청을 보낼 수 있다.이를 가능하게하기 위해 Zookeeper를 사용하여 에이전트와 수집기간에, 그리고 collector/web 인스턴스간에 설정된 통신 채널을 조정합니다. 이 기능을 통해 실시간 통신 (활성 Thread 수 모니터링과 같은 기능)이 가능해졌습니다.

일반적으로 HBase 백엔드에서 제공하는 Zookeeper 인스턴스를 사용하므로 추가 Zookeeper 구성이 필요하지 않습니다. 관련 구성 옵션이 아래에 나와 있습니다.

* **Collector** - *pinpoint-collector.properties*
	* `cluster.enable`  
	* `cluster.zookeeper.address`
	* `cluster.zookeeper.sessiontimeout`
	* `cluster.listen.ip`
	* `cluster.listen.port`
* **Web** - *pinpoint-web.properties*
	* `cluster.enable`
	* `cluster.web.tcp.port`
	* `cluster.zookeeper.address`
	* `cluster.zookeeper.sessiontimeout`
	* `cluster.zookeeper.retry.interval`
	* `cluster.connect.address`

