---
name: apply-review
description: 현재 작업 중인 PR에 달린 코드 리뷰를 확인하고, 타당한 리뷰는 코드를 고쳐 반영(커밋·푸시·PR 갱신)하고, 부당한 리뷰는 이유를 설명하며 무시합니다. PR 리뷰 코멘트를 반영하거나, 리뷰 피드백에 대응하거나, "리뷰 반영", "리뷰 적용", "코드리뷰 처리"를 요청할 때 반드시 이 스킬을 사용하세요.
---

# 코드 리뷰 반영 (apply-review)

현재 작업의 PR에 달린 코드 리뷰를 가져와 **하나씩 평가**하고, 타당하면 코드에 반영하고 부당하면 무시합니다.
모든 판단은 **대화창에 설명**하고 **PR 코멘트로도 남깁니다** (적용한 것·무시한 것 모두).

## 핵심 원칙

- **모든 설명은 개발자가 아니어도 이해할 수 있게, 짧고 쉽게.** 전문 용어 대신 "무엇을 왜 바꿨는지/안 바꿨는지"를 한두 문장으로.
- **언어 규칙**: 사용자 대화창 설명은 **한국어**, PR 코멘트·커밋 메시지·PR 본문은 **영어** (`.claude/rules/git-workflow.md`).
- **리뷰를 맹목적으로 따르지 않습니다.** 각 리뷰가 정말 맞는지, 우리 코드 맥락에서 타당한지 직접 판단합니다. 틀리거나 불필요하면 정중히 무시하고 이유를 댑니다.
- **되돌리기 어려운 작업(커밋·푸시·PR 갱신) 전에는 반드시 `/qa-pr`을 통과해야 합니다.**

## 0단계: PR 컨텍스트 확정

먼저 지금 어떤 PR을 다루는지 정합니다.

```bash
git branch --show-current
gh pr view --json number,title,url
```

다음 두 경우에는 **작업을 멈추고 사용자에게 PR 링크를 요청하세요.** 추측해서 진행하지 않습니다.

- 현재 브랜치가 `master`인 경우 (master에서는 직접 작업하지 않습니다 — `.claude/rules/git-workflow.md`)
- `gh pr view`가 실패하는 등 **현재 컨텍스트로 PR을 알 수 없는 경우**

요청 예시: "현재 브랜치가 master라(또는 연결된 PR을 찾지 못해) 어떤 PR의 리뷰를 반영할지 알 수 없어요. PR 링크를 알려주세요."

링크를 받으면 거기서 `{owner}/{repo}`와 PR 번호를 파싱하고, 해당 PR 브랜치로 이동해 작업합니다:

```bash
gh pr checkout {number} --repo {owner}/{repo}
```

체크아웃 후 다시 `git branch --show-current`로 master가 아님을 확인합니다.
리모트: `origin`=내 포크(jihea-park/pinpoint), `upstream`=pinpoint-apm/pinpoint.

## 1단계: 리뷰 수집

확정된 PR의 리뷰 전부(요약 리뷰 + 인라인 코멘트, Copilot·사람 모두)를 가져옵니다.

```bash
# PR 번호와 저장소가 아직 없으면 다시 확인
gh pr view --json number,title,url

# 리뷰 요약(승인/변경요청/총평) — 리뷰어와 본문
gh api repos/{owner}/{repo}/pulls/{number}/reviews \
  --jq '.[] | {id, user: .user.login, state, body}'

# 인라인 코드 코멘트 — 파일·라인·내용·comment id (반영 대상의 핵심)
gh api repos/{owner}/{repo}/pulls/{number}/comments \
  --jq '.[] | {id, user: .user.login, path, line, body}'
```

`{owner}/{repo}`는 PR이 올라간 저장소(보통 `pinpoint-apm/pinpoint`). 위 `gh pr view`의 `url`(예: `https://github.com/pinpoint-apm/pinpoint/pull/13913`)에서 파싱해 채웁니다.
이미 답글이 달려 처리된 코멘트는 중복 대응하지 않도록 `in_reply_to_id`가 없는(최상위) 코멘트 위주로 봅니다.

## 2단계: 각 리뷰를 하나씩 평가

리뷰 코멘트마다 다음을 따집니다. **추측하지 말고 실제 코드를 열어 확인하세요.**

1. **무엇을 지적하는가** — 코멘트가 가리키는 파일·라인을 직접 읽습니다.
2. **타당한가** — 우리 코드 맥락에서 실제 문제인지, 프로젝트 규약(`.claude/rules/`)에 비춰 맞는지 판단합니다.
3. **부수효과는 없는가** — 고쳤을 때 다른 동작이 깨지지 않는지 봅니다.

판단은 두 갈래입니다:

- **타당함 → 반영** (3단계로)
- **부당하거나 불필요함 → 무시**: 왜 적용하지 않는지 쉬운 말로 설명합니다. 예) 리뷰가 가정한 상황이 실제로는 생기지 않음 / 이미 다른 곳에서 처리됨 / 이슈 범위 밖 / 제안대로면 오히려 기존 동작이 깨짐.

> 부당해 보여도 단정하기 전에 한 번 더 코드로 확인하세요. 반대로 "리뷰어가 그렇게 말했으니까"는 적용의 이유가 될 수 없습니다.

## 3단계: 타당한 리뷰 반영 — 영향 분석 후 수정

코드를 고치기 전에 `.claude/rules/code-review-policy.md`의 회귀 방지 절차를 따릅니다:

- 고칠 파일의 **모든 호출자·소비자**를 먼저 파악합니다 (타입/훅/아톰/props/엔드포인트 변경 시 사용처 전부 Grep).
- 수정이 **이번 PR이 해결하려던 이슈와 충돌하지 않는지** 확인합니다. (PR 제목·본문·연결 이슈의 의도를 다시 읽기)
- 수정 후, 영향받은 파일을 다시 읽어 기존 시그니처·반환 타입·props·import 경로가 깨지지 않았는지 검증합니다.

여러 리뷰를 반영할 경우 관련 수정을 모아 **하나의 커밋**으로 묶습니다(성격이 전혀 다르면 분리 가능).

## 4단계: 리베이스로 최종 코드 만든 뒤 `/qa-pr` 필수 (게이트)

QA는 **커밋·푸시될 최종 코드**에 대해 해야 의미가 있습니다. 리베이스로 충돌을 해결하면 코드가 바뀌므로, **리베이스를 먼저 끝내고 그 결과에 `/qa-pr`을 실행**합니다. (QA 뒤에 리베이스를 하면 변경된 코드가 검증되지 않은 채 커밋될 수 있습니다.)

```bash
# 1) 먼저 upstream 최신화 리베이스 — 충돌이 있으면 해결까지 완료 (git-workflow.md)
git fetch upstream master
git rebase upstream/master
```

```text
# 2) 그 다음 /qa-pr 실행 — 빌드(yarn build)·테스트(yarn test)·동작 QA·회귀 점검
```

판정이 **FAIL이면 절대 커밋하지 말고** 문제를 먼저 해결합니다. (`.claude/rules/git-workflow.md`의 QA 게이트)
`/qa-pr` 통과 후 셀프 체크: 디버그 코드(`console.log` 등) 없음, 코드 스타일 준수, 기존 기능 정상.

## 5단계: 커밋 · 푸시 · PR 갱신

리베이스와 QA가 모두 끝나고 PASS일 때만 진행합니다.

```bash
git add <변경 파일>
git commit -m "[#issue_number] Description"   # 영어로. 같은 플랫폼이면 #issue_number, 다른 플랫폼이면 #noissue (git-workflow.md)
git push origin <branch-name>                 # 기존 PR 갱신이므로 -u 불필요 (이미 origin 추적 중)
```

커밋·PR 메시지는 **항상 영어**. 기존 PR이 같은 브랜치를 추적하므로 push만으로 PR이 갱신됩니다.

## 6단계: 판단을 PR 코멘트로 남기기 (적용·무시 모두)

각 리뷰 코멘트에 **답글**로 결론을 남깁니다. 답글 본문은 **영어**로, 짧고 명확하게.

```bash
# 특정 인라인 리뷰 코멘트에 답글 (본문은 영어로 — 플레이스홀더도 영어)
gh api repos/{owner}/{repo}/pulls/{number}/comments/{comment_id}/replies \
  --method POST -f body="Applied. <one-line reason in English>"
# 또는 무시한 경우
gh api repos/{owner}/{repo}/pulls/{number}/comments/{comment_id}/replies \
  --method POST -f body="Not applied. <one-line reason in English>"
```

요약 리뷰(인라인이 아닌 총평)에 대응할 때는 PR 본문 코멘트를 사용:

```bash
gh pr comment {number} --body "<summary of applied/ignored items in English>"
```

## 7단계: 대화창 보고

마지막으로 사용자에게 쉬운 말로 정리해 보여줍니다:

1. **적용한 리뷰**: 무엇을, 왜 타당해서, 어떻게 고쳤는지 (각 한두 문장)
2. **무시한 리뷰**: 무엇을, 왜 적용하지 않았는지 (각 한두 문장)
3. **QA 결과**: PASS / FAIL
4. **반영 결과**: 커밋 해시, 푸시·PR 갱신 여부, 남긴 PR 코멘트

> 적용/무시 어느 쪽이든 "왜"가 가장 중요합니다. 비개발자도 납득할 수 있게 설명하세요.
