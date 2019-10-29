---
title: FAQ
sidebar: mydoc_sidebar
keywords: faq, 질문, 답, 자주 묻는 질문, FAQ, 질문과 답
last_updated: 2018년 2월 1일
permalink: faq.html
toc: false
disqus: false
---

다른 질문에 대해서는, [사용자 그룹](https://groups.google.com/forum/#!forum/pinpoint_user)을 사용해주시기 바랍니다.

### 호출 스택은 어떻게 보나요?
서버 노드를 클릭하면, 오른쪽에 분산형 차트가 생깁니다. 차트는 서버를 경유한 성공/실패한 모든 요청을 보여줍니다. 관심이 있는 요청이 있다면, **분산형 차트에서 드래그**하여 선택하세요. 그러면 선택한 요청을 포함한 호출 스택을 보여줍니다.

### 에이전트의 로그 레벨은 어떻게 변경하나요?
*PINPOINT_AGENT/lib*폴더에 있는 해당 에이전트의 *log4j.xml* 파일을 수정하여 로그 레벨을 변경할 수 있습니다.

### 왜 첫번째/일부 요청만 추적되나요?
에이전트의 pinpoint.config 파일에 샘플링 속도 옵션이 있습니다 (profiler.sampling.rate).
옵션값이 N으로 설정되었다면 핀포인트 에이전트는 N 번째 트랜젝션마다 한번 샘플링합니다.
값을 1로 바꾸면 모든 트랜젝션을 추적할 수 있습니다.

### 분산형 차트의 요청 횟수가 응답 요약 차트에서의 값과 다릅니다. 왜 그런가요?
분산형 차트의 데이터는 초 단위이므로, 요청 횟수가 초마다 구별될 수 있습니다.
반면에, 서버맵, 응답 요약, 로드 차트의 데이터는 분 단위로 저장됩니다 (성능상의 이유로 콜렉터는 이를 메모리에 집계하고 매 분마다 청소합니다).
예를 들어, 만약 10:00:30 부터 10:05:30 까지 데이터를 조회한다면, 분산형 차트는 10:00:30 과 10:05:30 사이의 요청 횟수를 보여주지만, 서버맵, 응답 요약, 로드 차트는 10:00:00 과 10:05:59 사이의 요청 횟수를 보여줍니다.

### HBase에서 어플리케이션 이름과 에이전트 아이디는 어떻게 삭제하나요?
어플리케이션 이름과 에이전트 아이디는 한번 등록되면, TTL이 만료할 때 까지 HBase에 저장됩니다 (기본 1년).
하지만 더 이상 사용하지 않는다면 [관리자 API](https://github.com/naver/pinpoint/blob/master/web/src/main/java/com/navercorp/pinpoint/web/controller/AdminController.java)를 사용하면 능동적으로 삭제할 수 있습니다.
* 어플리케이션 이름 삭제 - `/admin/removeApplicationName.pinpoint?applicationName=$APPLICATION_NAME&password=$PASSWORD`
* 에이전트 아이디 삭제 - `/admin/removeAgentId.pinpoint?applicationName=$APPLICATION_NAME&agentId=$AGENT_ID&password=$PASSWORD`
password 값은 *pinpoint-web.properties*의 `admin.password` 속성에 지정한 값입니다. 값을 지정하지 않으면 password 값 없이 관리자 API를 호출할 수 있습니다.

### 어플리케이션 이름의 규칙은 무엇이 있나요?
어플리케이션 이름은 @,#,$,%,* 등의 특수문자를 지원하지 않습니다. 
어플리케이션 이름은 [a-zA-Z0-9], '.', '-', '_' 만 지원합니다.

### HBase가 너무 많은 공간을 차지합니다. 어떤 데이터부터 삭제해야 하나요?
Hbase는 확장이 용이하기 때문에 공간이 부족하면 언제든지 지역 서버를 추가할 수 있습니다. TTL 값을 낮추는 것 (특히 **AgentStatV2** 와 **TraceV2**에서) 또한 도움이 됩니다 (공간이 확보되기 전에 대규모 압축을 기다려야 할 수도 있습니다). 대규모 압축을 하는 방법에 대해서는, [여기](https://github.com/naver/pinpoint/blob/master/hbase/scripts/hbase-major-compact-htable.hbase)를 참고하시기 바랍니다.

하지만, 만약 지금 당장 공간을 확보**해야만** 한다면, **AgentStatV2** 와 **TraceV2** 테이블의 데이터가 삭제하기에 가장 안전합니다. 에이전트 통계 데이터(관찰)와 호출 스택 데이터(트랜젝션)를 잃겠지만, 다른 문제를 일으키지는 않습니다.

**MetaData** 테이블을 삭제하는 것은 호출스택에 **-METADATA-NOT-FOUND** 이 발생할 것이고 이를 "고치는" 방법은 모든 에이전트를 재시작하는 것 뿐입니다. 그러므로 이 테이블은 그대로 두는 것이 좋습니다.

### 사용자 jar 어플리케이션이 추적되지 않습니다. 도와주세요!
핀포인트 에이전트는 트랜젝션을 새로 추적하기 위해서 진입 점이 필요합니다. 보통 다양한 WAS 플러그인(Tomcat, Jetty 등)이 RPC 요청을 받으면 새 추적이 시작됩니다.
사용자 jar 어플리케이션에서는, 에이전트가 언제 어디서 시작할 지를 모르기 때문에 수동으로 설정을 해줄 필요가 있습니다.
*pinpoint.config* 파일에서 `profiler.entrypoint` 를 수정하는 것으로 설정할 수 있습니다.

### 새로운 릴리즈 버전 이후로 빌드가 실패합니다. 도와주세요!
이전 버전을 사용하고 있었다면 `mvn clean verify -DskipTests=true` 명령을 꼭 실행해주세요.

### atlassian OSGi을 사용할 때는 자바 런타임 옵션을 어떻게 설정하나요?
`-Datlassian.org.osgi.framework.bootdelegation=sun.,com.sun.,com.navercorp.*,org.apache.xerces.*`

### https://www.google-analytics.com/collect 에 요청을 보내는 이유는 뭔가요?
핀포인트 웹 모듈은 서버 맵, 트랜젝션 목록, 관찰에서의 클릭 횟수와 순서를 추적하도록 구글 애널리틱스를 사용합니다.
이 데이터는 사용자가 어떻게 웹 UI와 상호작용하는지 더 이해하기 쉽게 해주고, 핀포인트 웹의 사용자 경험을 증진하는데 유용한 정보가 됩니다. 어떤 이유로든 그만두고 싶다면, 웹 객체의 pinpoint-web.properties 에서 다음 옵션을 false 로 설정해주세요.
```
config.sendUsage=false
```

### 핀포인트에서 HBase 2.x 버전을 사용하고 싶어요.
핀포인트 데이터베이스에서 HBase 2.x 버전을 사용하려면, [Hbase 업그레이드 가이드](https://naver.github.io/pinpoint/plugindevguide.html)를 확인하세요.


### gojs를 사용하지 않으려면 무엇을 해야 하나요?
핀포인트 웹의 다음 버전에서는, visjs 와 gojs 중에서 선택할 수 있습니다.
이 옵션의 [소스코드](https://github.com/naver/pinpoint/blob/master/web/src/main/webapp/v2/src/app/app.module.ts) 와 
[가이드](https://naver.github.io/pinpoint/ui_v2.html)


