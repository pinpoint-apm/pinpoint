---
title: Pinpoint
keywords: pinpoint apm, apm, pinpoint, pinpoint homepage, pinpoint gitpage
tags: 
permalink: index.html
summary:
toc: false
disqus: false
layout: firstpage
---

![Build Status](https://travis-ci.org/naver/pinpoint.svg?branch=master)
![codecov](https://codecov.io/gh/naver/pinpoint/branch/master/graph/badge.svg)
 

<a class="twitter-timeline" data-lang="en" data-width="550" data-height="400" data-theme="light" data-link-color="#19CF86" href="https://twitter.com/Pinpoint_APM?ref_src=twsrc%5Etfw">Tweets by Pinpoint_APM</a> <script async src="https://platform.twitter.com/widgets.js" charset="utf-8"></script>
 
## Is your application created with PHP?

Pinpoint has started to support application written in PHP. [Check-out our php-agent repository](https://github.com/naver/pinpoint-c-agent).

## Looking for place to ask questions?

[Questions and FAQ](./faq.html) 

## Introduction  
  
**Pinpoint** is an APM (Application Performance Management) tool for large-scale distributed systems written in Java / [PHP](https://github.com/naver/pinpoint-c-agent).
Inspired by [Dapper](http://research.google.com/pubs/pub36356.html "Google Dapper"),
Pinpoint provides a solution to help analyze the overall structure of the system and how components within them are interconnected by tracing transactions across distributed applications.

You should definitely check **Pinpoint** out If you want to

* understand your [application topology](./overview.html) at a glance
* monitor your application in *Real-Time*
* gain code-level visibility to every transaction
* install APM Agents without changing a single line of code
* have minimal impact on the performance (approximately 3% increase in resource usage)

![Pinpoint](images/ss_server-map.png)

## Live Demo
 * [Demo](http://125.209.240.10:10123) - Here is a Demo, So that you can take a look at Pinpoint right away.

## Want a quick tour?
 * [Overview](./overview.html)/[History](./history.html)/[Tech Details](./techdetail.html) to get to know more about Pinpoint 
 * [Videos](./videos.html) - Checkout our videos on Pinpoint

<iframe width="560" height="315" src="https://www.youtube.com/embed/U4EwnB34Dus" frameborder="0" allow="autoplay; encrypted-media" allowfullscreen></iframe>

## Getting Started
 * [Quick-start guide(Lastest Snapshot)](./quickstart.html) for simple test run of Pinpoint
 * [Installation guide(Lastest Snapshot)](./installation.html) for further instructions.

## Google Analytics
The web module has google analytics attached that tracks the number and the order of button clicks in the server map, transaction list, and the inspector view.

This data is used to better understand how users interact with the Web UI which gives us valuable information in improving Pinpoint Web's user experience.
To disable this for any reason, set the following option to false in *pinpoint-web.properties* for your web instance.
```
config.sendUsage=false
```

## License
Pinpoint is licensed under the Apache License, Version 2.0.
See [LICENSE](https://github.com/naver/pinpoint/blob/master/LICENSE) for full license text.

```
Copyright 2019 NAVER Corp.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
