---
title: Hbase Upgrade
keywords: hbase, 업그레이드
last_updated: Mar 8, 2019
sidebar: mydoc_sidebar
permalink: hbaseupgrade.html
disqus: false
---

## 핀포인트에서 Hbase 2.x 버전을 사용하고 싶으신가요?

현재 릴리즈 버전의 기본 설정은 Hbase 1.x 버전입니다.

핀포인트 데이터베이스에서 Hbase 2.x 버전을 사용하고 싶다면,
핀포인트를 다시 컴파일해야 합니다. `hbase-shaded-client` 라이브러리의 버전이 2.x 로 변경되야 합니다.
(v2.1.1 권장, 테스트됨)

`hbase2` 프로필을 추가하면서 재컴파일을 하려면

다음 명령이나

`mvn clean install -P hbase2,release -DskipTests=true`

다음 명령 (버전 변경)

`mvn clean install -Dhbase.shaded.client.version=2.1.1 -DskipTests=true`
 
또는 pom.xml 파일에서 직접 버전을 바꾼 후 다음 명령을 사용할 수 있습니다.

```java
<!-- hbase -->
<hbase.shaded.client.version>1.2.6.1</hbase.shaded.client.version>
```
