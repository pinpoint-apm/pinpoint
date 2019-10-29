---
제목: 기여
핵심어: help
마지막 업데이트: 2018년 2월 1일
sidebar: mydoc_sidebar
permalink: contribution.html
disqus: false
---

귀하의 기여를 우리와 공유하도록 해주셔서 대단히 감사합니다. 이 페이지가 당신의 기여에 도움이 될 것입니다. 

첫 번째 pull_request를 하기 전에 [Contributor License Agreement](http://goo.gl/forms/A6Bp2LRoG3)에 서명했는지 확인하십시오. 이것은 저작권이 아닌 프로젝트의 일부로 당신의 코드를 사용하고 재분배할 수 있는 허가를 우리에게 주는 것이다.

## Pull request 요청하기
오타나 포맷과 같은 사소한 수정은 제외하고, 모든 Pull Requests는 그들과 관련된 문제여야 합니다. 사람들이 어떤 일을 하고 있는지 아는 것은 항상 도움이 되고, 토론하는 동안 다른(때로는 더 좋은)생각이 떠오르게 할 수도 있다.
Pull Requests를 작성하기 전에 다음 사항에 유의하십시오.
* 모든 새 Java 파일에는 라이센스 설명의 복사본이 있어야 한다. 기존 파일에서 라이센스를 복사할 수 있다.
* 당신의 코드를 테스트 했는지 철저하게 확인하십시오. 플러그인의 경우 가느하면 통합 테스트르 포함하도록 최선을 다하십시오.
* 코드를 제출하기 전에, 코드에 의해 도입된 변경 사항이 빌드 또는 테스트를 위반하지 않도록 하십시오.
* commit log를 logical chunks로 정리하여 무엇을 변경했고 변경한 이유를 우리가 쉽게 파악할 수 있도록 하십시오. (`git rebase -i` helps)
* Pull Requests를 만들기 전에 마스터 브랜치에 대해 코드를 최신 상태로 유지하십시오.
* 마스터 브랜치에 대해 Pull Requests를 작성하십시오.
* 자신의 플러그인을 만든 경우,  [plugin contribution guideline](../../wiki/Pinpoint-Plugin-Developer-Guide#iii-plugin-contribution-guideline)을 참조하십시오.

## Plugin Contribution Guideline
Plugin Contribution을 환영한다.
현재, 우리는 [Storm](https://storm.apache.org, [HBase](http://hbase.apache.org "Apache HBase"), as well as profiler support for additional languages (.NET, C++) 및  추가 언어 (.NET, C ++)에 대한 프로파일러 지원과 같은 라이브러리에 대한 추가적인 추적 지원을 원합니다.

### Technical Guide
**plug-in 개발을 위한 기술 안내서** [plugin development guide](https://naver.github.io/pinpoint/plugindevguide.html "Pinpoint Plugin Development Guide")를 들러보시오, [plugin samples] (https://github.com/naver/pinpoint-plugin-sample "Pinpoint Plugin Samples project") 프로젝트와 함께 계측 방법에 대한 아이디어를 얻습니다. 샘플은 당신이 시작하는데 도움이되는 코드를 제공해 줄 것입니다.

### Contributing Plugin
당신의 plugin에 기여하길 원한다면, 다음 요구 사항을 만족해야만 합니다.

1. 구성 키 이름은`profiler. [pluginName]`으로 시작해야합니다.
2. 하나 이상의 plugin 통합 테스트.

plugin이 완료되면 문제를 열어 다음과 같이 plugin을 제공하십시오.

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

우리 팀은 plugin을 검토할 것이고, 모든 것이 확인되면 당신의 pligin 저장소는 타사 plugin 목록 페이지에서 연결될 것이다.
plugin이 널리 사용되는 라이브러리 용이고 지속적으로 지원할 수 있다고 확신하는 경우 PR을 보내달라고 요청받을 수 있습니다. 수락하면 plugin이 Pinpoint 저장소에 병합됩니다.

모든 plugin을 소스 저장소에 통합하고 싶지만, 아직 그만큼의 소스코드를 관리할 수 있는 인력이 없다.
우리는 매우 작은 팀이고, 모든 라이브러의 전문가는 아닙니다. 지속적인 지우원에 확신이 없다면 plugin을 병합하지 않는 것이 좋습니다.

PR을 보내려면 다음과 같이  plugin을 수정해야 한다.

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
* *plugin/pom.xml*에 plugin을 하위 모듈로 추가하십시오.
* * plugins / assembly / pom.xml *에 플러그인을 종속성으로 추가하십시오.
* / agent-it / src / test 디렉토리에 플러그인 통합 테스트를 복사하십시오.
* /agent/src/main/resources/*.config 파일에 구성을 추가하십시오.
* 모든 라이센스 소스에 다음 라이센스 헤더를 삽입하십시오.
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

만약 당신이 PR에 방해받고 싶지 않다면, 당신은 우리에게 직접 PR을 하라고 말할 수도 있다.
단, Git 이력이나 Github 프로필을 통해서는 당신의 기여를 볼 수 없다는 점에 유의하십시오.








