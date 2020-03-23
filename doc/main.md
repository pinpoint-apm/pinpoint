---
title: "Pinpoint 2.0.1"
keywords: pinpoint release, 2.0.1
permalink: main.html
sidebar: mydoc_sidebar
---

## What's New in 2.0.1

We have a new UI for Pinpoint-Web. 
A lot of users asked on the commercial issue and we are happy to 
announce that **all commercial dependencies(amchart, go.js) has been removed.**

{% include important.html content="If you plan to upgrade pinpoint-web to v2.0.0+ from version 1.8.5 or earlier 
please check the [Upgrade Guide](http://naver.github.io/pinpoint/2.0.1/main.html#notice-for-pinpoint-web-version-upgrade)" %}

Pinpoint started using GRPC as default 

### Fully changed UI

Lighter, faster, smoother
* Servermap  
![servermap](https://user-images.githubusercontent.com/20402016/76491441-04a18e00-6471-11ea-9afd-0ae9f1df367a.png)

* Distributed calltree  
![callstack](https://user-images.githubusercontent.com/10057874/68727854-ee8dd900-0608-11ea-8185-f2fc2fd101fe.jpg)

* Inspector  
![inspector](https://user-images.githubusercontent.com/10057874/68727863-f483ba00-0608-11ea-99f8-217aa6329d14.jpg)

### Support JAVA 12, 13

### Support gRpc transport 

Starting from v2.0.0, gRpc is available to transfer data between agents and collectors.
Currently, the default setting is
  * collector
    -  thrift, grpc (both active)
  * agent
    -  grpc (default)

    ![grpc](https://user-images.githubusercontent.com/10057874/69118773-edace980-0ad7-11ea-85e8-c490b86f048a.jpg)
 
### Maximum throughput limit feature

Feature to limit throughput per second for sampled transactions.
You can use these features in the following cases

* Limiting throughput per second for transactions started by this agent
* Limiting throughput per second for transactions started by an external agent

Configuration example is shown below.
~~~
# Permits per second, if throughput is 0, it is unlimited.
# "New" is a transaction that is newly traced.
profiler.sampling.new.throughput=0
# "Continue" is a transaction that is already being tracked.
profiler.sampling.continue.throughput=0
~~~
     
You can check the results on the Inspector page.
S.S.N - Skipped New, S.S.C - Skipped Continuation
![간지기능](https://user-images.githubusercontent.com/10057874/65134640-93dc6480-da3f-11e9-937f-46e88d51fc92.png)
 
### Supports externalized configuration

Pinpoint lets you externalize configuration that can change frequently

* Agent
https://github.com/naver/pinpoint/blob/master/doc/installation.md#profiles-2

* Collector 
https://github.com/naver/pinpoint/blob/master/doc/installation.md#profiles

* Web
https://github.com/naver/pinpoint/blob/master/doc/installation.md#profiles-1

* Flink
https://github.com/naver/pinpoint/blob/master/doc/application-inspector.md#3-configuration

### Notice for Pinpoint Web version upgrade

{% include callout.html content="**Important information**: 
If you upgrade pinpoint-web to v2.0.0+ from version 1.8.5 or earlier, you need to change the schema of the alarm history table.
The schema has been changed to allow multiple settings of the same alarm rule.
<br/><br/>sql statement : ALTER TABLE `alarm_history` ADD `rule_id` INT(10) NOT NULL AFTER `history_id`;" type="primary" %} 
   
### Pinpoint Plugin

 - Started to support Elasticsearch Client Plugin
   * Servermap
   ![elastic1](https://user-images.githubusercontent.com/10057874/69119552-5e550580-0ada-11ea-8d93-79b2d7aa543f.png)
   * Distributed calltree
   ![elastic2](https://user-images.githubusercontent.com/10057874/69119563-63b25000-0ada-11ea-99f5-338f20d4cf77.png)
 
 - Started to support Elasticsearch Client BBoss Plugin
   * Servermap
   ![bboss](https://user-images.githubusercontent.com/10057874/69119648-98bea280-0ada-11ea-9af9-6bc4e244d9e9.png)
   * Distributed calltree
   ![bboss2](https://user-images.githubusercontent.com/10057874/69119658-9c522980-0ada-11ea-853d-8a42de7ac95e.png)

 - Started to support Redisson Client Plugin
   * Servermap
   ![Screenshot 2019-11-18 at 19 24 41](https://user-images.githubusercontent.com/10057874/69119717-c99ed780-0ada-11ea-90f7-d1ee7546afda.jpg)
   * Distributed calltree
   ![레디슨2](https://user-images.githubusercontent.com/10057874/69119719-cc013180-0ada-11ea-8533-5703eada0640.jpg)     
 
 - Started to support Log4j2 Plugin
   * Example
       ```
       2015-04-04 14:35:20 [INFO](ContentInfoCollector:76) [txId : agent^14252^17 spanId : 1224] get content name : TECH
       2015-04-04 14:35:20 [INFO](ContentInfoCollector:123) [txId : agent^142533^18 spanId : 1231] get content name : OPINION
       2015-04-04 14:35:20 [INFO](ContentInfoCollector:12) [txId : agent^142533^19 spanId : 1246] get content name : SPORTS
       2015-04-04 14:35:20 [INFO](ContentInfoCollector:25) [txId : agent^142533^20 spanId : 1263] get content name : TECH
       2015-04-04 14:35:20 [INFO](ContentInfoCollector:56) [txId : agent^142533^21 spanId : 1265] get content name : NATIONAL
       2015-04-04 14:35:20 [INFO](ContentInfoCollector:34) [txId : agent^142533^22 spanId : 1278] get content name : OPINION
       2015-04-04 14:35:20 [INFO](ContentInfoService:55) [txId : agent^14252^18 spanId : 1231] check authorization of user
       2015-04-04 14:35:20 [INFO](ContentInfoService:14) [txId : agent^14252^17 spanId : 1224] get title of content
       2015-04-04 14:35:21 [INFO](ContentDAOImpl:14) [txId : agent^14252^17 spanId : 1224] execute query ...
       2015-04-04 14:35:21 [INFO](ContentDAOImpl:114) [txId : agent^142533^19 spanId : 1246] execute query ...    
       2015-04-04 14:35:20 [INFO](ContentInfoService:74) [txId : agent^14252^17 spanId : 1224] get top linking for content
       2015-04-04 14:35:21 [INFO](ContentDAOImpl:14) [txId : agent^142533^18 spanId : 1231] execute query ...
       2015-04-04 14:35:21 [INFO](ContentDAOImpl:114) [txId : agent^142533^21 spanId : 1265] execute query ...
       2015-04-04 14:35:22 [INFO](ContentDAOImpl:186) [txId : agent^142533^22 spanId : 1278] execute query ...
       2015-04-04 14:35:22 [ERROR](ContentDAOImpl:158) [txId : agent^142533^18 spanId : 1231]
       ```
   * Guide : [link](https://github.com/naver/pinpoint/blob/master/doc/per-request_feature_guide.md)

 - Started to support Redisson Client Plugin
   * Servermap
   ![Screenshot 2019-11-18 at 19 24 41](https://user-images.githubusercontent.com/10057874/69119717-c99ed780-0ada-11ea-90f7-d1ee7546afda.jpg)
   * Distributed calltree
   ![레디슨2](https://user-images.githubusercontent.com/10057874/69119719-cc013180-0ada-11ea-8533-5703eada0640.jpg)     

 - Started to support Spring WebFlux Plugin
   * Servermap
   ![spring-webflux-servermap-1](https://user-images.githubusercontent.com/10043788/71885301-6e55a100-317d-11ea-966e-7891232c0e46.PNG)
   * Distributed calltree
   ![spring-webflux-calltree-1](https://user-images.githubusercontent.com/10043788/71885364-95ac6e00-317d-11ea-8a15-362bdbdfe069.PNG)

 - Started to support MS Sql Plugin
   * Servermap
   ![mssql servermap](https://user-images.githubusercontent.com/10057874/73996132-20130800-499e-11ea-8ac6-2a42e33c1a45.jpg)
   * Distributed calltree
   ![mssql calltree](https://user-images.githubusercontent.com/10057874/73996140-2903d980-499e-11ea-8568-5b02401122c8.jpg)

## Special Thanks

AlphaWang, EricHetti, SRE-maker, dyyim741, gwagdalf , immusk, kwangil-ha, licoco, qq295190549, tankilo, upgle, widian, yin-bp, yjqg6666, zifeihan 

Thank you all for your wonderful contributions. Pinpoint v2.0.0 release is enriched thanks to the contributors.

If there is someone who was inadvertently excluded, please let us know.


## Upgrade consideration

HBase compatibility table:

{% include_relative compatibilityHbase.md %}

Agent compatibility to Collector table:

{% include_relative compatibilityPinpoint.md %}

Agent compatibility to Collector table:

{% include_relative compatibilityJava.md %}


## Supported Modules

* JDK 6+
* Supported versions of the \* indicated library may differ from the actual version.

{% include_relative modules.md %}


