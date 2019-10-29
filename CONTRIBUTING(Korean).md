우리에게 공헌을 해주기로 선택한 당신에게 감사드립니다. 당신의 목표를 더 잘 실현하기 위해 이 페이지를 읽어주십시오.

## Issues
Pinpoint와 관련된 문제에 대해 자유롭게 토론하십시오. 버그를 알리거나 기능을 제안하는 것까지 모두 가능합니다. 새로운 논쟁을 열기 전에 고려해야 할 몇가지 사항들이 있습니다: 
* 게시하려는 내용이 이미 해결되었는지 문서 및 기존 문제를 검색하여 확인해주십시오.
* 빌드에 실패했다면, [Travis build status](https://travis-ci.org/naver/pinpoint)에서 현재 제작 상태를 확인하여 참조하십시오.
* 무언가 작동하지 않느다면, 가능한 자세히 설명하십시오. 당신이 제공하는 정보가 많을수록, 더 쉽게 문제에 해결책을 제공할 수 있습니다.
* Pinpoint와 직접적으로 관련이 없는 것이 작동하지 않는 경우(HBase와 같은)에는 고칠 수 있는지 알아보세요. 아마도 당신은 그 문제를 해결하기 위해 더 좋은 장비를 갖추고 있을 것이다. 비록 아무 효과가 없더라도, 여러분이 시도한 모든 것을 우리와 공유한다면 이 문제를 더 빨리 해결하는데 도움이 될 것이다.
* 빠른 질문의 경우나, 또는 코드 베이스와 직접 관련이 없는 문제일 경우 [Pinpoint Google Group](https://groups.google.com/forum/#!forum/pinpoint_user)에 게시하십시오.

## Pull Requests
오타나 포맷과 같은 사소한 수정은 제외하고, 모든 Pull Requests는 그들과 관련된 문제여야 합니다. 사람들이 어떤 일을 학하고 있는지 아는 것은 항상 도움이 되고, 토론하는 동안 다른(때로는 더 좋은)생각이 떠오르게 할 수도 있다.
Pull Requests를 작성하기 전에 다음 사항에 유의하십시오.
* 모든 새 Java 파일에는 라이센스 설명의 복사본이 있어야 한다. 기존 파일에서 라이센스를 복사할 수 있다.
* 당신의 코드를 테스트 했는지 철저하게 확인하십시오. 플러그인의 경우 가느하면 통합 테스트르 포함하도록 최선을 다하십시오.
* 코드를 제출하기 전에, 코드에 의해 도입된 변경 사항이 빌드 또는 테스트를 위반하지 않도록 하십시오.
* commit log를 logical chunks로 정리하여 무엇을 변경했고 변경한 이유를 우리가 쉽게 파악할 수 있도록 하십시오. (`git rebase -i` helps)
* Pull Requests를 만들기 전에 마스터 브랜치에 대해 코드를 최신 상태로 유지하십시오.
* 마스터 브랜치에 대해 Pull Requests를 작성하십시오.
* 자신의 플러그인을 만든 경우,  [plugin contribution guideline](../../wiki/Pinpoint-Plugin-Developer-Guide#iii-plugin-contribution-guideline)을 참조하십시오.

코드를 제공하기 전에 [Contributor License Agreement] (https://docs.google.com/forms/d/e/1FAIpQLSfNuUx0lkiapWF8LE0xQSVL-ZNheuy2LEIixyqCj9y5GsSzVQ/viewform?c=0&w=1)에 서명했는지 확인하십시오. 이것은 저작권이 아니다. 이것은 프로젝트의 일부로 당신의 코드를 사용하고 재분배할 수 있는 허가를 우리(Naver)에게 준다.
