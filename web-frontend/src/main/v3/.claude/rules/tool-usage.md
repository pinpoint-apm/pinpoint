# 도구 사용 규칙

## GitHub/이슈 접근 우선순위

사용자가 `oss.navercorp.com` 또는 `github.com/pinpoint-apm/pinpoint`의 링크를 제공하는 경우:

1. **MCP GitHub 도구 시도 전에 항상 `gh` CLI를 먼저 사용**하세요.
2. 예시:
   ```bash
   # 이슈 보기
   gh issue view <number> --repo <owner/repo>

   # PR 보기
   gh pr view <number> --repo <owner/repo>

   # 이슈 코멘트 목록
   gh issue view <number> --repo <owner/repo> --comments
   ```
3. 이슈, PR, 기타 GitHub에서 접근 가능한 모든 리소스에 적용됩니다.
4. `gh` CLI를 사용할 수 없거나 작업에 충분하지 않은 경우에만 MCP 도구로 대체하세요.
