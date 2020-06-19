---
title: "Pinpoint 2.0.3"
keywords: pinpoint release, 2.0.3
permalink: main.html
sidebar: mydoc_sidebar
---

## What's New in 2.0.3


### Plugins
 * Started to support Async Thread Plugin
    * Distributed CallStack 
    ![Process](https://user-images.githubusercontent.com/10057874/84873501-4bc99b80-b0be-11ea-834e-d01928a81fda.png)

### New Features

#### Total Thread Count Inspector

 * Inspector  
    ![ThreadCount](https://user-images.githubusercontent.com/10057874/84873858-ce525b00-b0be-11ea-8199-79f8491a8233.png)


#### FilterWizard for ApplicationNode

 (Thank you @yjqg6666  for your contribution)
 * Image  
 ![필터위자드](https://user-images.githubusercontent.com/10057874/84874592-c8a94500-b0bf-11ea-96a5-ac12d6c2b48f.png)


#### Support System Properties Override in agent
 (Thank you @yjqg6666  for your contribution)
 * Description  
    with `-Dkey=value`
    ```
    -javaagent:${pinpointPath}/pinpoint-bootstrap-2.0.2.jar
    -Dpinpoint.applicationName=application
    -Dpinpoint.agentId=agent
    -Dpinpoint.sampling.rate=10
    ```

#### Automatic detection of docker environment
 (Thank you @yjqg6666 for your contribution)
 - `-Dpinpoint.container` option is no longer required

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


