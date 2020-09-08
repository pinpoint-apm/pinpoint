---
title: "Pinpoint 1.8.4"
keywords: pinpoint release, 1.8.4
permalink: mainBase.html
sidebar: mydoc_sidebar
---

[Check out updates on lastest stable release](https://naver.github.io/pinpoint/1.8.4/main.html)

## What's New in 1.8.0

**If your application is based on Java 9/10, you should definitely consider updating Pinpoint-Agent to 1.8.**
We suggest you to upgrade the version if you are using any of [recent added plugins](#whats-new-in-180)
or if you'd like to start monitoring *Open File Descriptor*, *Direct/Mapped Buffer* metrics.

 - **Started to support Java 9/10 application monitoring.**
 - Started to support asynchronous communications for
    - JBoss
    - Jetty
    - Resin
    - Tomcat
    - Weblogic
    - Websphere
    
 - Installation Guide has been update. Build Requirement has been changed(Default Java to JDK 8).
 - Cleaned up plugin dependency

     
### Pinpoint Plugin

 - Started to support Kafka
    ![default](https://user-images.githubusercontent.com/10057874/44016329-9169cb32-9f0f-11e8-8764-8c5e9a1092df.png)
    ![default](https://user-images.githubusercontent.com/10057874/44016330-92e546a8-9f0f-11e8-9c6b-0ef66093f7c0.png)  
    (Thank you @lopiter  for your contribution)
 
 - Started to support akka-http
    ![akka](https://user-images.githubusercontent.com/10057874/44016233-32b7ba36-9f0f-11e8-953f-349bfe0efc09.png)
    ![akka](https://user-images.githubusercontent.com/10057874/44016234-33d6b732-9f0f-11e8-993b-b33dd174af25.png)  
    (Thank you @lopiter , @upgle  for your contribution)

 - Started to support undertow
    ![29ae9e22-9a65-11e8-9fa1-3dfe053cea8c](https://user-images.githubusercontent.com/10057874/44016390-c0e52654-9f0f-11e8-90c6-e9f2f1c6a190.png)
         
 - Started to support Spring asynchronous communication
    ![async](https://user-images.githubusercontent.com/10057874/44016449-f6e77c20-9f0f-11e8-88bb-e603b9d9c660.png)
     
 - Started to support WebLogic
 - Enhance Jetty plugin
 - Enhance JBoss plugin
 - Enhance okhttp plugin
 - Fix Dubbo plugin bugs

[Bug Details](https://github.com/naver/pinpoint/issues?q=is%3Aissue+milestone%3A1.8.0+is%3Aclosed+label%3Abug+label%3Amodule%3Aplugin)

   
### Pinpoint Agent 

 - Options for agents running in containers to group
    `-Dpinpoint.container` jvm argument added for grouping container applications with variable host names in the UI.
    
 - Resolved hostname retrieval triggering possible DNS lookups
    [#4427](https://github.com/naver/pinpoint/pull/4427)   
    (Thank you @nickycheng  for your contribution)

[Enhancement Detail](https://github.com/naver/pinpoint/issues?q=is%3Aissue+milestone%3A1.8.0+is%3Aclosed+label%3Aenhancement+label%3Amodule%3Aagent)
[Bug Detail](https://github.com/naver/pinpoint/issues?q=is%3Aissue+milestone%3A1.8.0+is%3Aclosed+label%3Abug+label%3Amodule%3Aagent)

 
### Pinpoint Collector

[Enhancement Detail](https://github.com/naver/pinpoint/issues?q=is%3Aissue+milestone%3A1.8.0+is%3Aclosed+label%3Aenhancement+label%3Amodule%3Acollector)
[Bug Detail](https://github.com/naver/pinpoint/issues?q=is%3Aissue+milestone%3A1.8.0+is%3Aclosed+label%3Abug+label%3Amodule%3Acollector)

 
### Pinpoint Web

 - Started to collect Direct/Mapped Buffer Metric
    ![buffer 2](https://user-images.githubusercontent.com/10057874/44016075-766f96c8-9f0e-11e8-9273-e95b19bc3742.PNG)  
    Direct buffer and Mapped buffer have been added to the Inspector.  
    
 - Started to collect Open File Descriptor Metric
     ![fd 1](https://user-images.githubusercontent.com/10057874/44016152-ccacd2d0-9f0e-11e8-8119-966f4c129a79.PNG)  
     Open file descriptor has been added to the Inspector. 
 
 - Fix alarm bug


[Enhancement Detail](https://github.com/naver/pinpoint/issues?q=is%3Aissue+milestone%3A1.8.0+is%3Aclosed+label%3Aenhancement+label%3Amodule%3Aweb)
[Bug Detail](https://github.com/naver/pinpoint/issues?q=is%3Aissue+milestone%3A1.8.0+is%3Aclosed+label%3Abug+label%3Amodule%3Aweb)

### Pinpoint Flink

[Enhancement Detail](https://github.com/naver/pinpoint/issues?q=is%3Aissue+milestone%3A1.8.0+is%3Aclosed+label%3Aenhancement+label%3Amodule%3Aflink)


### Upgrade consideration

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
