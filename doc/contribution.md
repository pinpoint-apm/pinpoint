---
title: Contribution
keywords: help
last_updated: Feb 1, 2018
sidebar: mydoc_sidebar
permalink: contribution.html
disqus: false
---

Thank you very much for choosing to share your contribution with us. Please read this page to help yourself to the contribution.

Before making first pull-request, please make sure you've signed the [Contributor License Agreement](http://goo.gl/forms/A6Bp2LRoG3). This isn't a copyright - it gives us (Naver) permission to use and redistribute your code as part of the project.

## Making Pull Requests
Apart from trivial fixes such as typo or formatting, all pull requests should have a corresponding issue associated with them. It is always helpful to know what people are working on, and different (often better) ideas may pop up while discussing them.
Please keep these in mind before you create a pull request:
* Every new java file must have a copy of the license comment. You may copy this from an existing file.
* Make sure you've tested your code thoroughly. For plugins, please try your best to include integration tests if possible.
* Before submitting your code, make sure any changes introduced by your code does not break the build, or any tests.
* Clean up your commit log into logical chunks of work to make it easier for us to figure out what and why you've changed something. (`git rebase -i` helps)
* Please try best to keep your code updated against the master branch before creating a pull request.
* Make sure you create the pull request against our master branch.
* If you've created your own plugin, please take a look at [plugin contribution guideline](#plugin-contribution-guideline)


## Plugin Contribution Guideline
We welcome your plugin contribution.
Currently, we would love to see additional tracing support for libraries such as [Storm](https://storm.apache.org "Apache Storm"), [HBase](http://hbase.apache.org "Apache HBase"), as well as profiler support for additional languages (.NET, C++).

### Technical Guide
**For technical guides for developing plug-in,** take a look at our [plugin development guide](https://naver.github.io/pinpoint/plugindevguide.html "Pinpoint Plugin Development Guide"), along with [plugin samples](https://github.com/naver/pinpoint-plugin-sample "Pinpoint Plugin Samples project") project to get an idea of how we do instrumentation. The samples will provide you with example codes to help you get started.  

### Contributing Plugin
If you want to contribute your plugin, it has to satisfy the following requirements:

1. Configuration key names must start with `profiler.[pluginName]`.
2. At least 1 plugin integration test.

Once your plugin is complete, please open an issue to contribute the plugin as below:

```
Title: [Target Library Name] Plugin Contribution

Link: Plugin Repository URL
Target: Target Library Name
Supported Version: 
Description: Simple description about the target library and/or target library homepage URL

ServiceTypes: List of service type names and codes the plugin adds
Annotations: List of annotation key names and codes the plugin adds
Configurations: List of configuration keys and description the plugin adds.
```

Our team will review the plugin, and your plugin repository will be linked at the third-party plugin list page if everything checks out. If the plugin is for a widely used library, and if we feel confident that we can continuously provide support for it, you may be asked to send us a PR. Should you choose to accept it, your plugin will be merged to the Pinpoint repository.

As much as we'd love to merge all the plugins to the source repository, we do not have the man power to manage all of them, yet. We are a very small team, and we certainly are not experts in all of the target libraries. We feel that it would be better to not merge a plugin if we are not confident in our ability to provide continuous support for it.

To send a PR, you have to modify your plugin like this:

* Fork Pinpoint repository
* Copy your plugin under /plugins directory
* Set parent pom
```
    <parent>
        <groupId>com.navercorp.pinpoint</groupId>
        <artifactId>pinpoint-plugins</artifactId>
        <version>Current Version</version>
    </parent>
```
* Add your plugin to *plugins/pom.xml* as a sub-module.
* Add your plugin to *plugins/assembly/pom.xml* as a dependency.
* Copy your plugin integration tests under /agent-it/src/test directory.
* Add your configurations to /agent/src/main/resources/*.config files.
* Insert following license header to all java source files.
```
/*
 * Copyright 2018 Pinpoint contributors and NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
```

If you do not want to be bothered with a PR, you may choose to tell us to do it ourselves. However, please note that your contribution will not visible through git history or the Github profile.


