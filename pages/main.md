---
title: "Pinpoint 2.0.2"
keywords: pinpoint release, 2.0.2
permalink: main.html
sidebar: mydoc_sidebar
---

## What's New in 2.0.2

 Fixed minor bugs and addiotional plugins added.
 
### Pinpoint Plugin

 - Started to support Async Thread Plugin
   * Servermap
   ![Async Thread Plugin](https://user-images.githubusercontent.com/10057874/80352564-06070880-88af-11ea-81a4-22f5e6ac0f91.jpg)
   Thank you @zifeihan  for your contribution
 
 - Started to support Informix JDBC Plugin
   * Mix view
   ![Informix Plugin](https://user-images.githubusercontent.com/10057874/80352447-dfe16880-88ae-11ea-8502-7f76fdf61ca7.png)
   Thank you @guillermomolina for your contribution
    
### Bugs

 Fixed problem that does not retransmit when metadata transmission fails. [#6662](https://github.com/naver/pinpoint/issues/6662)
 Fixed problem where completed transaction is not recognized in certain cases while using Webflux. [#6465](https://github.com/naver/pinpoint/issues/6465)

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


