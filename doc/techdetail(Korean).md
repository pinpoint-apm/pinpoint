---
title: Technical Details
keywords: tech, technology
last_updated: Feb 1, 2018
sidebar: mydoc_sidebar
permalink: techdetail.html
disqus: false
---


이 기사에서는 트랜잭션 추적 및 바이트 코드 계측과 같은 Pinpoint의 기술들을 설명합니다. 그리고 바이트 코드를 수정하고 성능 데이터를 기록하는 Pinpoint Agent에 적용된 최적화 방법에 대해 설명합니다.

## Google의 Dapper를 모델로 한 분산 트랜잭션 추적

Pinpoint는 Google Dapper를 모델로 한 단일 트랜잭션에서 분산 요청을 추적합니다.

### Google Dapper에서 분산 트랜잭션 추적이 작동하는 방식

분산 추적 시스템의 목적은 메시지가 노드 1에서 노드 2로 전송 될 때 분산 시스템에서 노드 1과 노드 2 사이의 관계를 식별하는 것입니다 (그림 1).

![Figure 1. Message relationship in a distributed system](images/td_figure1.png)

Figure 1. 분산 시스템에서의 메시지 관계

문제는 메시지 간의 관계를 식별 할 수있는 방법이 없다는 것입니다. 예를 들어, 노드 1에서 보낸 N 개의 메시지와 노드 2에서받은 N '메시지 간의 관계를 인식 할 수 없습니다. 즉 Node 1에서 X번째 메시지를 보냈을 때, Node 2가 받은 N'개의 메시지 중에서 X번째 메시지를 선택할 수 없습니다. TCP 또는 운영 체제 레벨에서 메시지를 추적하려고했습니다. 그러나 각 프로토콜마다 별도로 구현해야하므로 성능이 낮 으면 구현 복잡성이 높아졌습니다. 게다가 메시지를 정확하게 추적하기가 어려웠습니다

그러나 이러한 문제를 해결하는 간단한 솔루션이 Google Dapper에서 구현되었습니다. 해결 방법은 메시지를 보낼 때 메시지간에 링크가 될 수있는 응용 프로그램 수준 태그를 추가하는 것입니다. 예를 들어, HTTP 요청시 HTTP 헤더의 메시지에 대한 태그 정보를 포함하고이 태그를 사용하여 메시지를 추적합니다.

> Google's Dapper

> Google's Dapper에 대한 자세한 내용은"[Dapper, a Large-Scale Distributed Systems Tracing Infrastructure](http://research.google.com/pubs/pub36356.html)."

Pinpoint는 Google Dapper의 추적 기술을 기반으로하지만 원격 호출에서 분산 트랜잭션을 추적하기 위해 호출 헤더에 응용 프로그램 수준의 태그 데이터를 추가하도록 수정되었습니다. 태그 데이터는 TraceId로 정의 된 키 모음으로 구성됩니다.

### Pinpoint의 자료 구조

Pinpoint의 핵심 자료구조는 Span과 Trace, TraceId로 이루어져 있다.
* Span: RPC(remote procedure call) 추적을 위한 기본 단위다. RPC가 도착했을 때 처리한 작업을 나타내며 추적에 필요한 데이터가 들어 있다. 코드 수준의 가시성 을 확보하기 위해, Span의 자식으로 SpanEvent라는 자료구조를 가지고 있다. Span은 TraceId를 가지고 있다.
Trace: Span의 집합으로, 연관된 RPC(Span)의 집합으로 구성된다. Span의 집합은 TransactionId가 같다. Trace는 SpanId와 ParentSpanId를 통해 트리 구조로 정렬된다.
TraceId: TransactionId와 SpanId, ParentId로 이루어진 키의 집합이다. TransactionId는 메시지의 아이디이며, SpanId와 ParentId는 RPC의 부모 자식 관계를 나타낸다. 
  - TransactionId(TxId): 단일 트랜잭션에서 분산 시스템간에 전송 / 수신 된 메시지의 ID입니다. 전체 서버 그룹에서 전역 적으로 고유해야합니다.
  - SpanId: RPC 메시지를 받았을 때 처리되는 작업(job)의 아이디를 정의한다. RPC가 노드에 도착했을 때 생성한다. 
  - ParentSpanId(pSpanId): RPC를 생성 한 상위 범위의 SpanId입니다. 노드가 트랜잭션의 시작점 인 경우 상위 범위가 없습니다 - 이 경우 스팬이 트랜잭션의 루트 스팬임을 나타 내기 위해 -1 값을 사용합니다.

> Google의 Dapper와 NAVER의 Pinpoint의 차이점

> Pinpoint의 "TransactionId"라는 용어는 Google Dapper의 "TraceId"와 동일한 의미를 가지며 Pinpoint의 "TraceId"는 키 모음을 나타냅니다.

### TraceId의 작동 방법
다음과 같이 4개의 노드와 3번의 RPC가 존재하는 경우를 예로 들어 TraceId가 어떻게 작동하는지 설명하겠다

![Figure 2. Example of a TraceId behavior](images/td_figure2.png)

Figure 2. Example of a TraceId behavior

그림 2에서 TransactionId(TxId)는 3개의 RPC가 한 개의 연관된 트랜잭션이라는 것을 표현한다. 하지만 TransactionId만으로는 각 RPC 간의 관계를 정렬할 수 없다. RPC 간의 정렬을 위해 SpanId와 ParentSpanId(pSpanId)가 필요하다. 노드를 Tomcat으로 비유하면, SpanId는 HTTP 요청이 Tomcat에 도착해 요청 처리를 수행하는 스레드다. ParentSpanId는 RPC 호출 시 자신을 호출한 부모 노드의 SpanId다.

Pinpoint는 TransactionId로 연관된 N개의 Span를 찾아낼 수 있고, SpanId와 ParentSpanId로 N개의 Span을 트리로 정렬할 수 있다.

SpanId와 ParentSpanId는 64비트 long형 정수다. 임의로 생성되는 값이라 충돌할 가능성이 있지만 64비트 long형 정수의 범위가 -9223372036854775808~9223372036854775807이므로 충돌할 확률은 낮다. 키가 충돌했을 때 Google Dapper와 Pinpoint는 충돌을 해결하지 않고 충돌 여부를 알려주는 방법을 사용한다.

TransactionId는 AgentId, JVM(Java virtual machine) 시작 시간, SequenceNumber로 구성된다.

* AgentId: JVM 실행 시 사용자가 임의로 정하는 아이디다. AgentId는 Pinpoint가 설치된 전체 서버군에서 중복되는 것이 없어야 한다. AgentId의 유일성을 보장하는 쉬운 방법은 호스트 이름($HOSTNAME)을 사용하는 것이다. 보통 호스트 이름은 중복되지 않기 때문이다. 서버 안에 JVM을 여러 개 기동해야 한다면 호스트 이름에 접미어(postfix)를 추가해 아이디 중복을 피할 수 있다.
* JVM 시작 시간: 0부터 시작하는 SequenceNumber의 유일성을 보장하기 위해 JVM의 시작 시간이 필요하다. 이 값은 사용자의 실수로 동일한 AgentId가 설정됐을 경우 아이디의 충돌 확률 줄이는 역할도 한다.
* SequenceNumber: Pinpoint Agent가 내부적으로 발급하는 아이디로, 0부터 순차적으로 증가하는 값이다. 개별 메시지마다 발급한다.

Google Dapper나 Twitter의 분산 트랜잭션 추적 플랫폼인 Zipkin은 TraceId(Pinpoint의 TransactionId와 같은 의미)를 임의로 발급하고, 이 키가 충돌하는 것은 자연스러운 상황으로 판단한다. 그러나 Pinpoint에서는 TransactionId의 충돌 확률을 낮추고 싶었기 때문에 위와 같이 구성했다. 데이터의 양은 적지만 충돌 확률이 높은 방식과 데이터의 양은 많지만 충돌 확률이 낮은 방식 중에서 후자를 선택한 것이다.

Pinpoint보다 더 좋은 방식도 존재할 수 있다. TransactionId 구현 후보에는 중앙 키 서버에서 키를 발급하는 방식도 있었다. 이렇게 중앙 서버에서 키를 발급하면 성능 문제와 네트워크 오류 문제가 있을 수 있어 벌크 형태로 발급받는 것까지 고려했다. 나중에 이러한 방식으로 변경할 수도 있지만 현재는 단순한 방식을 채택했다. Pinpoint는 TransactionId를 충분히 변경할 수 있는 데이터로 판단하고 있다.

## 코드 수정이 필요 없는 bytecode instrumentation

앞서 분산 트랜잭션 추적에 대해 설명했다. 이를 구현할 때는 개발자가 직접 코드를 수정하는 방법을 생각할 수 있다. 개발자가 RPC 호출 시 태그 정보를 직접 추가하도록 개발하면 된다. 하지만 분산 트랜잭션 추적이 좋은 기능이라고 해도 이를 위해 코드를 수정하는 것은 부담스러운 일이다.

트위터의 Zipkin은 수정된 라이브러리와 자체 컨테이너(Finagle)를 사용해 분산 트랜잭션 추적 기능을 제공한다. 하지만 필요하면 코드도 수정해야 한다. Pinpoint는 코드를 수정하지 않고도 분산 트랜잭션 추적 기능을 제공하길 원했고 코드 수준의 가시성을 원했다. 이 문제를 해결하기 위해 bytecode instrumentation 기법을 도입했다. Pinpoint Agent는 RPC 호출 코드를 가로채 태그 정보를 자동으로 처리한다.

### bytecode instrumentation의 단점 극복

분산 트랜잭션 추적에는 다음과 같은 두 가지 방법이 있습니다. Bytecode 계측은 자동 방법 중 하나입니다.

* 수동 방식: Pinpoint가 API를 제공하고 개발자는 Pinpoint의 API를 사용해 중요 포인트에 데이터를 기록하는 코드를 개발한다.
* 자동 방식: Pinpoint가 라이브러리의 어떤 API를 가로챌지 결정해 코드를 개발한다. 이를 통해 개발자가 개입하지 않아도 자동으로 기능이 적용되게 한다.

두 방식의 장단점은 다음과 같다:

표 1 각 방법의 장단점


Item     |장점 |단점
---------|----------|------------
**수동 방식 ** | - 적은 개발 리소스가 필요합니다. <br>- API이 단순해져 결과적으로 버그의 수를 줄일 수 있다 . |- 개발자는 코드를 수정해야합니다..<br>- 추적 레벨이 낮아진다.
**자동 방식** |- 개발자가 코드를 수정하지 않아도 된다.<br>- 바이트코드의 정보가 많기 때문에 정밀한 데이터를 수집할 수 있다.|- Pinpoint 개발팀의 자원 소모가 크다(필자의 판단으로 수동의 10배 이상).<br>- 추적할 라이브러리 코드를 순간적으로 파악해 추적 지점을 판단할 수 있는 수준 높은 개발자가 필요하다.<br>- 난이도가 높은 개발 방법인 bytecode instrumentation을 사용하므로 버그 발생 가능성이 높다.

bytecode instrumentation은 난이도와 위험성이 높은 개발 방법이다. 하지만 개발 자원이나 난이도를 고려했을 때 bytecode instrumentation의 이득이 더 많다.

개발 자원 측면에서 bytecode instrumentation을 사용하면 개발 자원이 많이 필요한 반면 서비스 개발자의 자원은 적게 필요하다. 예를 들어, bytecode instrumentation를 사용하는 자동 방식과 라이브러리를 사용하는 수동 방식의 비용을 계산하면 다음과 같다(여기서 말하는 비용은 이해를 돕기 위해 가정한 임의의 값이다).

* 자동 방식: 총 비용 100 
  - Pinpoint 개발 비용: 100 
  - 서비스 적용 비용: 0 
* 수동 방식: 총 비용 30 
  - Pinpoint 개발 비용: 20 
  - 서비스 적용 비용: 10 

이렇게 계산하면 수동 방식이 더 나은 방법이다. 하지만 네이버의 환경은 다르다. 네이버에는 수많은 서비스가 있기 때문에 서비스 적용 비용을 수정해야 한다. 즉, 10개의 서비스에 적용한다고 계산하면 총 비용은 다음과 같다.

* Pinpoint 개발 비용 20 + 서비스 적용 비용 10 x 10 = 120

보다시피, 자동 방식이 비용적으로 더 효율적이다.

Pinpoint 팀에서 Java에 능숙하고 전문적인 많은 개발자를 보유하게되어 행운이다. 따라서 Pinpoint 개발의 기술적 어려움을 극복하는 것은 시간 문제일 뿐이다.

### bytecode instrumentation의 가치

우리가 implement bytecode instrumentation(Automatic method)을 구현하기로 선택한 이유는 이미 설명한 이유뿐만 아니라 다음 사항들 때문입니다.

#### API을 노출하지 않음

개발자가 사용할 수 있도록 API가 노출되어 있는 경우. API 제공자로서 우리는 원하는 대로 API를 수정하는 것이 제한된다. 이 제약은 API 제공자에게는 스트레스로 작용한다.

잘못된 디자인을 수정하거나 기능을 추가하기 위해 API를 변경해야 할 수도 있다. 하지만 API를 쉽게 변경할 수 없다는 제약이 있다면 API 제공자는 기능을 발전시키기 어렵다. 이 문제를 해결할 모법 답안은 'API의 확장성을 고려해 디자인한다'지만 쉬운 일이 아님은 모두가 알고 있을 것이다. API 디자인이 잘못되지 않는다는 것은 미래를 정확하게 예측할 수 있다는 것인데 그것은 거의 불가능한 일이다.

bytecode instrumentation을 사용하면 추적 API를 사용자에게 노출하지 않아도 되므로 API 의존성 문제를 겁내지 않고 디자인을 지속적으로 개선할 수 있다. 이를 반대로 생각하면, Pinpoint를 수정해 사용하려는 개발자의 입장에서는 내부 API 변경이 자주 발생할 것이라는 의미이기도 하다. 하지만 Pinpoint는 아직 기능과 성능을 보완해야 할 부분이 많아 API 변경을 제약할 생각은 없다. 현재는 API 안정성보다는 기능 발전과 디자인 개선의 우선순위를 더 높이 두고 있다

#### 손쉬운 적용과 해제

bytecode instrumentation은 라이브러리의 프로파일링 코드나 Pinpoint 자체에 문제가 생겼을 때 애플리케이션에 영향을 준다는 단점이 있다. 하지만 코드를 변경할 필요가 없으므로 Pinpoint를 쉽게 적용하고 해제할 수 있다.

JVM 구동 시 JVM 시작 스크립트에 다음과 같은 Pinpoint Agent 설정 3개를(Pinpoint Agent 구성과 관련됨) 추가하면 쉽게 Pinpoint를 적용할 수 있다.

    -javaagent:$AGENT_PATH/pinpoint-bootstrap-$VERSION.jar
    -Dpinpoint.agentId=<Agent's UniqueId>
    -Dpinpoint.applicationName=<The name indicating a same service (AgentId collection)>
    
Pinpoint 때문에 문제가 발생했다면 JVM 시작 스크립트에서 Pinpoint Agent 설정을 제거해 Pinpoint 적용을 해제하면 된다.

### How Bytecode Instrumentation Works(bytecode instrumentation의 작동 방법)

bytecode instrumentation 계측 기술은 Java bytecode instrumentation를 다루므로 개발 위험을 높이는 동시에 생산성은 감소합니다. 또한, 개발자들이 실수를 하기가 쉽다. Pinpoint는 인터셉터로 추상화해 생산성과 접근성을 높였다. . Pinpoint는 클래스 로드 시점에 애플리케이션 코드를 가로채 성능 정보와 분산 트랜잭션 추적에 필요한 코드를 주입한다. 애플리케이션 코드에 직접 추적 코드가 주입되므로 성능이 좋다.

![Figure 3. Behavior of bytecode instrumentation](images/td_figure3.png) 

Figure 3. Basic principle of bytecode instrumentation

Pinpoint는 API 인터셉트 부분과 성능 데이터 기록 부분을 분리했다. 추적 대상 메서드에 인터셉터를 주입해 앞뒤로 before() 메서드와 after() 메서드를 호출하게 하고 before() 메서드와 after() 메서드에 성능 데이터를 기록하는 부분을 구현했다. Pinpoint Agent는 bytecode instrumentation을 통해 필요한 메서드의 데이터만 기록하므로 생성되는 프로파일링 데이터의 크기가 작다.

## Pinpoint Agent의 성능 최적화

마지막으로 Pinpoint Agent의 성능을 최적화하기 위해 사용한 방법을 알아보겠다.

### 바이너리 포맷(Thrift) 사용

([Thrift](https://thrift.apache.org/)) 형식의 바이너리 포맷을 사용하면 사용 방법이나 디버깅이 어렵지만 인코딩 속도가 빠르다. 또한 생성되는 바이트 데이터의 크기가 작아 네트워크 사용량을 줄일 수 있다.

### Optimize Recorded Data for Variable-Length Encoding and Format(가변 길이 인코딩과 포맷에 최적화된 데이터 기록)

보통 long형 정수를 고정 길이로 인코딩하면 데이터의 크기가 8바이트다. 하지만 가변 길이로 인코딩(variable-length encoding)하면 숫자의 크기에 따라 1~10바이트가 된다. Pinpoint는 데이터 크기를 줄이기 위해, Thrift의 CompactProtocol을 사용해 가변 길이로 인코딩하고 인코딩 포맷에 최적화되도록 데이터를 기록한다. Pinpoint Agent는 추적된 루트(root) 메서드를 기준으로 나머지 시간을 벡터 값으로 변환해 데이터 크기를 줄인다.

> 가변 길이 인코딩

> 가변 길이 인코딩에 관한 더 자세한 내용은 "[Base 128 Varints](https://developers.google.com/protocol-buffers/docs/encoding#varints)"를 참조하십시오.

![Figure 4. Comparison between fixed-length encoding and variable-length encoding](images/td_figure4.png)

Figure 4. 고정 길이 인코딩과 가변 길이 인코딩 비교

그림 4에서 호출된 3개 메서드의 호출 시간을 알려면 6개 지점의 시간을 측정해야 한다. 이때 고정 길이 인코딩을 사용하면 48바이트(6 × 8)를 소모한다.

반면 Pinpoint Agent는 가변 길이 인코딩을 사용하고 해당 포맷에 맞게 데이터를 기록한다. 그리고 루트 메서드 시작 시간을 기준으로 삼아 다른 지점의 시간은 기준과의 차이(벡터 값)로 구한다. 벡터 값은 작은 숫자이므로 적은 수의 바이트를 사용하므로 48바이트가 아닌 13바이트를 소비했다.

메서드 수행 시간이 길어지면 가변 길이 인코딩을 사용하더라도 바이트 소모량이 증가한다. 그러나 고정 길이 인코딩보다는 소모량이 적으므로 더 효율적이다.

### 반복되는 API 정보와 SQL, 문자열을 상수 테이블로 치환

Pinpoint는 코드 수준의 정보를 추적하길 원했다. 그러나 이 기능의 문제점은 데이터의 양이 증가한다는 점이다. 정밀도가 높은 정보를 매번 서버로 보내면 데이터의 크기가 증가하므로 네트워크를 많이 사용하게 된다.

Pinpoint는 이 문제를 해결하기 위해 HBase로 구성된 원격지 서버에 상수 테이블(constant table)을 생성하는 전략을 사용한다. 메서드 A라는 정보를 Pinpoint Collector로 매번 보내면 데이터가 크므로 Pinpoint Agent는 메서드를 아이디로 치환해 HBase에 아이디와 메서드 정보를 상수 테이블로 저장하고 메서드 아이디로 추적 데이터를 생성한다. 그리고 사용자가 웹에서 추적 데이터를 조회하면 상수 테이블에서 해당 아이디의 메서드 정보를 찾아 재조합한다. SQL이나 자주 사용하는 문자열 데이터도 같은 방법으로 데이터 크기를 줄인다.

### 대량의 요청은 샘플링으로 처리

네이버가 제공하는 온라인 포털 서비스에 대한 요청량은 엄청나다. 하루 요청이 20억 건이 넘는 서비스도 있다.  이 모든 요청을 추적하는 방벙은 간단하다. 추적해야 하는 요청만큼 네트워크 인프라와 서버를 추가하고 정보를 수집할 서버도 증설하면 된다. 그러나 이 방법은 비용을 낭비하는 방법이다.

Pinpoint는 모든 요청을 추적하지 않고 샘플링한 데이터를 수집할 수 있다. 요청량이 적은 환경(개발 환경)에서는 모든 데이터를 수집하고, 요청량이 많은 환경(서비스 환경)에서는 적은 양(1~5%)의 데이터만 수집해도 전체 애플리케이션의 상태를 확인하는 데 무리가 없다. 샘플링을 통해 애플리케이션의 부하를 최소화하고 네트워크와 서버 인프라의 추가 투자 비용을 절감할 수 있다.

> Pinpoint의 샘플링 방법

> Pinpoint는 Counting 샘플러를 지원한다. 10으로 설정하면 10개의 요청 중에 1개 요청에 대해서만 데이터를 수집한다. 향후 데이터를 더 효과적으로 수집할 수 있는 샘플러를 추가할 계획이다.

### 비동기 데이터 전송으로 애플리케이션 스레드 중단 최소화

데이터 인코딩이나 원격 메시지 전송은 다른 스레드를 통해 비동기로 동작하므로 애플리케이션 스레드를 중단시키지 않는다.

#### UDP로 데이터 전송

Google Dapper와 달리 Pinpoint는 데이터를 빨리 확인하기 위해 데이터를 네트워크로 전송한다. 네트워크는 서비스와 같이 사용하는 공용 인프라로, 네트워크 폭주 시 문제가 발생할 수 있다. 이런 상황에서 Pinpoint Agent는 서비스에 네트워크 우선권을 주기 위해서 UDP 프로토콜을 사용한다.

> 참고

> 데이터 전송 API는 인터페이스로 분리되어 있어 교체할 수 있다. 로컬 파일과 같이 다른 방식의 데이터로 저장하는 구현체로 변경할 수 있다.

## 애플리케이션 적용 예

다음은 앞에서 설명한 내용을 포괄적으로 이해할 수 있도록 응용 프로그램에서 데이터를 얻는 방법의 예이다.

그림 5는  TomcatA와 TomcatB에 Pinpoint를 설치해 얻을 수 있는 내용을 나타낸다. 개별 노드의 추적 정보를 한 개의 트랜잭션으로 볼 수 있는데 이것이 분산 트랜잭션 추적 기능이다.

![Figure 5. Example 1: Pinpoint applied](images/td_figure5.png) 

Figure 5.Pinpoint 적용 예

메서드별로 Pinpoint가 하는 일은 다음과 같다.

1. 요청이 TomcatA에 도착하면 Pinpoint Agent는 TraceId를 발급한다.
  - TX_ID: TomcatA^TIME^1
  - SpanId: 10
  - ParentSpanId: -1(Root)
  
2. Spring MVC 컨트롤러에서 데이터를 기록한다.

3. HttpClient.execute() 메서드의 호출을 가로채 HttpGet에 TraceId를 설정한다.
  - 자식 TraceId 생성.
    - TX_ID: TomcatA^TIME^1 -> TomcatA^TIME^1
    - SPAN_ID: 10 -> 20
    - PARENT_SPAN_ID: -1 -> 10 (parent SpanId)
  - 자식 TraceId를 HTTP 헤더에 설정. 
    - HttpGet.setHeader(PINPOINT_TX_ID, "TomcatA^TIME^1")
    - HttpGet.setHeader(PINPOINT_SPAN_ID, "20")
    - HttpGet.setHeader(PINPOINT_PARENT_SPAN_ID, "10")

4. 태그된 요청이 TomcatB로 전송된다.
  - TomcatB는 전송된 요청에서 헤더를 확인한다. 
    - HttpServletRequest.getHeader(PINPOINT_TX_ID)
  - 헤더에서 TraceId를 인식해 자식 노드로 동작한다.  
    - TX_ID: TomcatA^TIME^1
    - SPAN_ID: 20
    - PARENT_SPAN_ID: 10

5. Spring MVC 컨트롤러에서 데이터를 기록하고 요청을 완료한다.

  ![Figure 6. Example 2: Pinpoint applied ](images/td_figure6.png)
  
6. TomcatB의 요청 처리가 끝나면 Pinpoint Agent는 추적 데이터를 Pinpoint Collector에 보내 HBase에 저장한다.

7. TomcatB의 HTTP 호출이 종료된 후 TomcatA의 요청 처리도 종료된다. Pinpoint Agent는 추적 데이터를 Pinpoint Collector로 전송해 HBase에 저장한다.

8. UI reads the trace data from HBase and creates a call stack by sorting trees.

## 결론

Pinpoint는 애플리케이션과 함께 작동하는 또 하나의 애플리케이션이다. bytecode instrumentation을 사용하기 때문에 코드 수정이 없는 것처럼 보일 뿐이다. bytecode instrumentation을 사용하는 APM은 위험도가 높다. Pinpoint에 문제가 생기면 애플리케이션도 영향을 받는다. 위험성을 없앨 수는 없지만 대신 Pinpoint는 위험성에 비교해 더 높은 가치를 주는 데 초점을 맞추고 있다. 이에 대한 득실은 사용자가 판단해야 할 문제다.

Pinpoint를 개선하기 위해 여전히 많은 양의 작업을 수행해야합니다. 불완전 함에도 불구하고 Pinpoint는 오픈 소스 프로젝트로 출시되었습니다. 우리는 귀하의 기대에 부응하기 위해 Pinpoint를 지속적으로 개발하고 개선하기 위해 노력하고 있습니다.

> 강운국 씀

> 2011 년에 저는 이와 같이 저 자신에 대해 썼습니다. 개발자로서 Microsoft 나 Oracle의 사용자처럼 사용자가 지불하고자하는 소프트웨어 프로그램을 만들고 싶습니다. Pinpoint가 오픈 소스 프로젝트로 시작되면서 내 꿈은 어느 정도 실현 된 것 같습니다. 현재로서는 Pinpoint를보다 가치 있고 사용자에게 매력적으로 만들고자합니다.




  











