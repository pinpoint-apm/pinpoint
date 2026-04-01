# Git 워크플로우 규칙

## 브랜치

> **이것은 협상 불가입니다. 어떠한 상황에서도 위반은 허용되지 않습니다.**

- **절대로 master에 직접 커밋하지 마세요. 예외 없음 — 설정 파일이든, 작은 수정이든, 무엇이든.**
- **커밋하기 전에 항상 새 브랜치를 생성하세요.** 브랜치는 반드시 최신 `upstream/master`를 기반으로 해야 합니다.

### 커밋 전 필수 브랜치 확인 (매번)

커밋 명령 실행 전에 반드시 아래를 먼저 실행하세요:

```bash
git branch --show-current
```

출력이 `master`이면 **즉시 중단**합니다. 절대 커밋하지 말고 아래 워크플로우를 따르세요.

### 필수 브랜치 생성 워크플로우 (매번 반드시 따를 것):
```bash
git fetch upstream master
git checkout -b <branch-name> upstream/master
# ... 변경 작업, 파일 스테이징 ...
git commit -m "[#issue] Description"
git push -u origin <branch-name>
```

- 브랜치 네이밍: `fix/`, `feat/`, `refactor/` 등 접두사 뒤에 설명적인 이름 (예: `fix/9520-preserve-timestamp-during-loading`)
- 현재 master에 있다면 즉시 중단하세요. 커밋하지 마세요. 먼저 브랜치를 생성하세요.

## 커밋 전 QA 게이트 (필수)

> **커밋과 푸시 전에 반드시 `/qa-pr`을 실행하세요. 예외 없음.**

`/qa-pr` 스킬은 변경된 모든 코드에 대해 빌드, 테스트, 동작 QA를 수행합니다. QA 판정이 FAIL이면 커밋하거나 푸시하지 마세요.

`/qa-pr` 통과 후 다음 셀프 리뷰 체크를 수행하세요:
1. **셀프 리뷰**: 변경된 모든 코드를 다시 읽으세요. 로직 정확성, 디버그 코드 없음(console.log 등), 코드 스타일 준수 확인.
2. **회귀 확인**: 기존 기능이 모두 정상 작동하는지 확인. 수정된 코드의 호출자/소비자를 추적하고 호환성 확인.
3. **빌드**: `yarn build` 실행 후 통과 확인.
4. **테스트**: `yarn test` 실행 후 모든 테스트 통과 확인.

빌드나 테스트가 실패하면 커밋하지 마세요. 먼저 문제를 해결하세요.

## 커밋 메시지
- 형식: `[#issue_number] Description` (예: `[#9520] Preserve timestamp during server map loading`)
- **같은 플랫폼**: 이슈와 저장소가 같은 플랫폼에 있는 경우(예: 둘 다 github.com이거나 둘 다 oss.navercorp.com), 이슈 번호 사용: `[#issue_number]`
- **다른 플랫폼**: 이슈와 저장소가 다른 플랫폼에 있는 경우(예: 이슈는 oss.navercorp.com에, 저장소는 github.com에 있는 경우), `[#noissue]` 사용
- 메시지는 "무엇"보다 "왜"에 집중하여 간결하게 작성.

## 푸시
- **푸시 전에 항상 upstream/master를 기반으로 리베이스:**
  ```bash
  git fetch upstream master
  git rebase upstream/master
  git push -u origin <branch-name>
  ```
- 리모트 설정: `origin` = 포크 (jihea-park/pinpoint), `upstream` = pinpoint-apm/pinpoint

## Pull Request 생성
- PR은 `upstream` (pinpoint-apm/pinpoint) master 브랜치를 대상으로 합니다.
- **항상** 사용자(jihea-park)를 PR에 어사인하세요.
- **GitHub 저장소에서**, REST API로 Copilot 코드 리뷰를 요청하세요:
  ```bash
  gh api repos/{owner}/{repo}/pulls/{number}/requested_reviewers \
    --method POST -f 'reviewers[]=copilot'
  ```
  참고: `gh pr create --reviewer copilot`은 Copilot에 작동하지 않으므로 REST API를 사용하세요.
- **PR 리뷰 피드백**: 사용자가 리뷰 코멘트를 제공하면 각각을 평가하세요. 피드백이 유효하고 필요한 경우에만 코드를 변경하세요. 불필요하거나 잘못된 코멘트에는 코드를 수정하지 말고 이유를 설명하세요.
- PR 본문 형식:
  ```
  ## Summary
  - <1~3개의 불릿 포인트>

  ## Test plan
  - [ ] <체크리스트 항목>
  ```
