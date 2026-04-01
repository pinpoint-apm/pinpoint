---
name: add-translation
description: 영어와 한국어 로케일 파일 모두에 i18n 번역 키를 추가합니다. 사용자 노출 문자열을 추가하거나, 번역 키가 누락되었거나, 새 UI 텍스트를 국제화할 때 반드시 이 스킬을 사용하세요.
---

# 번역 키 추가

영어 텍스트 `$1`과 한국어 텍스트 `$2`를 가진 번역 키 `$0`을 추가합니다.

## 단계

1. `packages/ui/src/constants/locales/en.json` 읽기
2. `packages/ui/src/constants/locales/ko.json` 읽기
3. 키 접두사를 기반으로 올바른 섹션 결정 (COMMON, SERVER_MAP, TRANSACTION 등)
4. 올바른 섹션의 **두 파일 모두**에 키 추가, 섹션 내 알파벳 순서 유지
5. 한국어 텍스트가 제공되지 않은 경우, 나중에 쉽게 찾아 바꿀 수 있도록 `[TODO]` 접두사를 붙인 영어 텍스트를 플레이스홀더로 추가 (예: `"[TODO] English text"`)

## 키 네이밍
- 점으로 구분된 섹션을 사용하는 SCREAMING_SNAKE_CASE
- 예시: `COMMON.SAVE`, `SERVER_MAP.NODE_COUNT`, `ERROR_ANALYSIS.CHART_TITLE`

## 가이드라인
- 항상 en.json과 ko.json을 동시에 업데이트
- 한국어 번역은 직역 금지, 자연스러운 한국어로 작성
- 동적 값은 이중 중괄호로 보간: `"Hello, {{name}}!"` / `"안녕하세요, {{name}}님!"`
- 복수형에는 i18next 복수 접미사 사용: `_one`, `_other`
