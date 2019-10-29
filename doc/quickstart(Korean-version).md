---
제목: Quick Start 안내서
핵심어: start, begin, quickstart, quick
마지막 업데이트: 2018년 2월 1일
sidebar: mydoc_sidebar
permalink: quickstart.html
disqus: false
---

만약 docker를 사용할 경우, 가장 쉽게 둘러 볼 수 있는 [Take a look at pinpoint docker](./docker.html) 살펴 보십시오.

각 구성 요소에 대해 4 개의 간단한 스크립트를 실행하여 자신의 컴퓨터에서 샘플 Pinpoint 인스턴스를 실행할 수 있습니다: Collector, Web, Sample TestApp, HBase.

구성 요소가 실행되고 나면 http://localhost:28080을 방문하여 Pinpoint Web UI를 볼 수 있어야 하고, 샘플 TestApp에서 트랜잭션을 생성하려면 http://localhost:28081을 참조하십시오.

Pinpoint는 3개의 주요 요소(Collector, Web, Agent)로 구성되어있고 저장소로 HBase를 사용한다. Collector와 Web은 단순한 WAR 파일로 포장되며, Agent는 Java Agent로 응용 프로그램에 첨부될 수 있도록 패키징된다.

Pinpoint QuickStart는 Pinpoint QuickStart는 에이전트가 자체적으로 연결할 수있는 샘플 TestApp을 제공하고 [Tomcat Maven Plugin] (http://tomcat.apache.org/maven-plugin.html)을 사용하여 세 가지 구성 요소를 모두 실행합니다.

## 요구 사항
Pinpoint를 빌드하기 위해서, 다음 요구 사항을 만족해야 한다.

* JDK 6 설치 ([jdk1.6.0_45](http://www.oracle.com/technetwork/java/javase/downloads/java-archive-downloads-javase6-419409.html#jdk-6u45-oth-JPR) recommended)
* JDK 7 설치 ([jdk1.7.0_80](http://www.oracle.com/technetwork/java/javase/downloads/java-archive-downloads-javase7-521261.html#jdk-7u80-oth-JPR) recommended)
* JDK 8 설치
* JDK 9 설치
* JAVA_HOME environment variable set to JDK 8 home directory.
* JAVA_6_HOME environment variable set to JDK 6 home directory.
* JAVA_7_HOME environment variable set to JDK 7 home directory.
* JAVA_8_HOME environment variable set to JDK 8 home directory.
* JAVA_9_HOME environment variable set to JDK 9 home directory.

QuickStart는 Linux, OSX 및 Windows를 지원한다.

## 시작하기

`git clone https://github.com/naver/pinpoint.git`에서 Pinpoint를 다운받거나 [download](https://github.com/naver/pinpoint/archive/master.zip) 압축파일을 받아 압축을 해제한다.

`./mvnw install -DskipTests=true`를 실행하여 Pinpoint를 설치

### 설치 & HBase 시작

다음 스크립트는 [Apache download site](http://apache.mirror.cdnetworks.com/hbase/)에서 HBase를 독립적으로 다운로드한다.

> **Windows에서**, you'll have to download HBase manually from [Apache download site](http://apache.mirror.cdnetworks.com/hbase/)에서 HBase를 수동으로 다운로드해야 합니다.
>
> `HBase-1.0.3-bin.tar.gz`를 다운로드 후 압축을 푸십시오.
>
> 최종 HBase 디렉토리가 "quickstart\hbase\hbase"처럼 보이도록 디렉토리의 이름을 "hbase"로 변경하십시오.
>
> 또한 해당`.cmd` 파일로 아래 스크립트를 실행해야합니다.

**Download & Start** - Run `quickstart/bin/start-hbase.sh`

**Initialize Tables** - Run `quickstart/bin/init-hbase.sh`

### Start Pinpoint Daemons

**Collector** - Run `quickstart/bin/start-collector.sh`

**TestApp** - Run `quickstart/bin/start-testapp.sh`

**Web UI** - Run `quickstart/bin/start-web.s

시작 스크립트가 완료되면 Tomcat 로그의 마지막 10 줄이 콘솔에 표시됩니다.

**Collector**

![Collector quick start successful](images/ss_quickstart-collector-log.png)

**TestApp**

![TestApp quick start successful](images/ss_quickstart-testapp-log.png)

**Web UI**

![Web quick start successful](images/ss_quickstart-web-log.png)

### 상태 확인
HBase와 3 개의 데몬이 실행되면 다음 주소를 방문하여 당신의 Pinpoint 인스턴스를 테스트 할 수 있습니다.

* Web UI - http://localhost:28080
* TestApp - http://localhost:28081

TestApp UI를 사용하여 추적 데이터를 Pinpoint에 전송하고 Pinpoint Web UI를 사용하여 확인할 수 있다. TestApp은 *TESTAPP*에 따라 *test-agent*로 등록된다.

## 멈춤

**Web UI** - Run `quickstart/bin/stop-web.sh`

**TestApp** - Run `quickstart/bin/stop-testapp.sh`

**Collector** - Run `quickstart/bin/stop-collector.sh`

**HBase** - Run `quickstart/bin/stop-hbase.sh`

## 기타

Pinpoint Web은 Mysql을 사용하여 사용자, 사용자 그룹 및 경보 구성을 유지합니다.</br>
그러나 Quickstart는 메모리 사용을 줄이기 위해 MockDAO를 사용한다.</br>
따라서 Quickstart에 Mysql을 사용하려면, Pinpoint Web을 참조하시오. [applicationContext-dao-config.xml](https://github.com/naver/pinpoint/blob/master/web/src/main/resources/applicationContext-dao-config.xml), [jdbc.properties](https://github.com/naver/pinpoint/blob/master/web/src/main/resources/jdbc.properties).

또한 알림을 실행하려면 추가 논리를 구현해야 한다. 이것을 참조하십시오. [link](./alarm.html)






