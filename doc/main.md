---
title: "Pinpoint 2.1.0"
keywords: pinpoint release, 2.1.0
permalink: main.html
sidebar: mydoc_sidebar
---

## What's New in 2.1.0

 Pinpoint-collector and Pinpoint-web has changed to spring boot. 
 Please check new guide for [installation](./installation.html).
 
 
 ### Improve Timeline Visibility
 
 * Async call  
    ![image](https://user-images.githubusercontent.com/3798235/84853547-60943800-b09a-11ea-9184-d737ad8f050d.png)
 * Database call  
    ![Screen Shot 2020-06-30 at 2 00 13 PM](https://user-images.githubusercontent.com/3798235/86085910-52eba300-badb-11ea-8f5c-ff420f432af1.png)
 
 ### Class Loading Metric
 
 * Inspector  
    ![클래스로딩 메트릭](https://user-images.githubusercontent.com/10057874/92567646-f2d26380-f2b8-11ea-8ecf-b1a103a74bc1.jpg)
 
 ### Record Request Header & Cookie
 
 * Configuration
      ```
     # record HTTP request headers case-sensitive
     # e.g. profiler.http.record.request.headers=X-AccessKey,X-Device-UUID
     profiler.http.record.request.headers=user-agent,accept
     
     # record HTTP request cookies(case-sensitive) in Cookie header
     # e.g. profiler.http.record.request.cookies=userid,device-id,uuid
     profiler.http.record.request.cookies=_ga
     ```
 * Distributed callstack  
     ![image](https://user-images.githubusercontent.com/7564547/86888566-57e0d000-c135-11ea-9e06-8b6a943f017d.png)

## Upgrade consideration

HBase compatibility table:

{% include_relative compatibilityHbase.md %}

Agent compatibility to Collector table:

{% include_relative compatibilityPinpoint.md %}

Additionally, the required Java version to run each Pinpoint component is given below:

{% include_relative compatibilityJava.md %}

## Supported Modules

* JDK 6+
* Supported versions of the \* indicated library may differ from the actual version.

{% include_relative modules.md %}


