---
name: review-code
description: Pinpoint 프로젝트 규약에 따라 코드 변경을 리뷰합니다. 커밋 전, PR 리뷰, 또는 코드 품질을 점검할 때 반드시 이 스킬을 사용하세요.
---

# 코드 리뷰

> **이 스킬은 다음 두 에이전트를 사용하여 실행합니다:**
> 1. `.claude/agents/explorer/explorer.md` — 코드베이스 탐색 및 변경 사항 파악
> 2. `.claude/agents/code-reviewer/code-reviewer.md` — 실제 코드 리뷰 수행
>
> 리뷰 시 두 에이전트의 사고방식과 체크리스트를 따르세요.

**인자 형식**: `/review-code [파일경로 또는 경로 목록]`
- `$ARGUMENTS`에 파일 경로를 전달하면 해당 파일을 리뷰합니다. 예: `/review-code packages/ui/src/hooks/api/useGetServerMap.ts`
- 경로 없이 실행하면 `git diff upstream/master --name-only`로 변경된 파일을 자동 감지합니다.

`$ARGUMENTS`의 코드를 리뷰합니다 (경로가 없으면 최근 git 변경 사항 리뷰).

## 리뷰 체크리스트

### 1. 아키텍처
- [ ] 올바른 패키지에 코드 위치 (라우팅은 apps/web, 나머지는 packages/ui)
- [ ] 페이지가 올바른 중첩 레이아웃 outlet에 배치 (InitialFetchOutlet / ConfigurationOutlet)
- [ ] API 훅이 reactQueryHelper의 공유 queryFn 사용
- [ ] 상태 관리가 올바른 계층 사용 (URL → Jotai → React Query)

### 2. 코드 스타일
- [ ] Prettier 포맷팅 (100자, 작은따옴표, 후행 쉼표, 세미콜론)
- [ ] TypeScript strict 준수 (`any` 금지, 적절한 타입 좁히기)
- [ ] 임포트 순서: external → @pinpoint-fe/* → relative
- [ ] 딥 임포트: `@pinpoint-fe/ui` 대신 `@pinpoint-fe/ui/src/constants`

### 3. 컴포넌트
- [ ] UI 프리미티브가 shadcn/ui 패턴 준수 (Radix + CVA + Tailwind)
- [ ] 도메인 컴포넌트가 UI 프리미티브를 조합하여 구성
- [ ] className 병합에 `cn()` 사용
- [ ] 인라인 스타일이나 CSS 모듈 없음

### 4. 데이터 페칭
- [ ] 엔드포인트가 EndPoints.ts에 정의됨
- [ ] 타입이 네임스페이스 패턴으로 정의됨
- [ ] `enabled` 옵션으로 조기 쿼리 실행 방지
- [ ] ProblemDetail 패턴을 통한 오류 처리

### 5. i18n
- [ ] 모든 사용자 노출 문자열에 `t()` 함수 사용
- [ ] 키가 en.json과 ko.json 모두에 추가됨
- [ ] 키 네이밍이 SECTION.KEY_NAME 규칙을 따름

### 6. 일반적인 함정
- [ ] React 훅 의존성 누락 없음
- [ ] 메모리 누수 없음 (useEffect에 클린업)
- [ ] 새 페이지에 지연 로딩 사용
- [ ] 하드코딩된 API 경로 없음 (END_POINTS 상수 사용)

결과를 심각도 순으로 정리: Critical > Warning > Suggestion
