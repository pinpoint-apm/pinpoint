---
title: New UI Guide
sidebar: mydoc_sidebar
tags:
keywords: UI
last_updated: Sep 1, 2018
permalink: ui_v2.html
toc: false
---

## How to test new UI in Development

Our team is redeveloping the UI with a new design using the latest Angular Framework.
If you want to experience the new UI in advance, 
please follow the instructions below.

* Update the following `RewriteForV2Filter` setting to Spring's `applicationContext-web.xml`.

```` xml
// applicationContext-web.xml
<bean id="rewriteFilter" class="com.navercorp.pinpoint.web.servlet.RewriteForV2Filter">
  <constructor-arg index="0" value="true"/>
</bean>

````

* Add `-Pv2` option when building Maven.
> mvn clean install -Pv2  
  // Please note that adding the -Pv2 option may cause longer time to build.

* URL where you can check
  * http://your.domain.name/v2

![UI Example](images/ui.png)

## 새롭게 개발 중인 UI를 테스트 할 수 있는 방법    

Pinpoint 팀은 새로운 디자인과 최신 Angular Framework 을 이용하여 UI 를 재 개발하고 있습니다.    
만약 새로운 UI를 미리 체험하고 싶다면 다음과 같은 설정이 필요합니다. 

* applicationContext-web.xml 의 `contructor-arg` 값을 `true`로 설정합니다.

```` xml
// applicationContext-web.xml
<bean id="rewriteFilter" class="com.navercorp.pinpoint.web.servlet.RewriteForV2Filter">
  <constructor-arg index="0" value="true"/>
</bean>
````
* Maven 빌드 시 `-Pv2` 옵션을 추가 합니다.  
> mvn clean install -Pv2  
  // -Pv2 옵션을 추가하면 빌드 타임이 오래 걸릴 수 있는 점을 유의해 주세요.

* 확인 할 수 있는 URL
  * http://your.domain.name/v2

![UI Example](images/ui.png)

* 개발 시 watch & build 실행 방법   
`./web/src/main/webapp/v2/` 에서 `npm install` 후  다음 명령 실행
 
```` 
> npm run build:watch
````