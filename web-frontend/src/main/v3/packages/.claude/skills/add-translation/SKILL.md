---
name: add-translation
description: Add i18n translation keys to both English and Korean locale files. Use when adding user-facing strings.
argument-hint: "[SECTION.KEY_NAME] [english-text] [korean-text]"
allowed-tools: Read, Edit
---

# Add Translation Keys

Add translation key `$0` with English text `$1` and Korean text `$2`.

## Steps

1. Read `packages/ui/src/constants/locales/en.json`
2. Read `packages/ui/src/constants/locales/ko.json`
3. Determine the correct section based on the key prefix (COMMON, SERVER_MAP, TRANSACTION, etc.)
4. Add the key to BOTH files in the correct section, maintaining alphabetical order within the section
5. If Korean text is not provided, add the English text as a placeholder prefixed with `[TODO]` (e.g., `"[TODO] English text"`) so it can be easily searched and replaced later

## Key Naming
- SCREAMING_SNAKE_CASE with dot-separated sections
- Examples: `COMMON.SAVE`, `SERVER_MAP.NODE_COUNT`, `ERROR_ANALYSIS.CHART_TITLE`

## Guidelines
- Always update BOTH en.json and ko.json simultaneously
- Korean translations should be natural Korean, not literal translation
- Use interpolation with double braces: `"Hello, {{name}}!"` / `"안녕하세요, {{name}}님!"`
- For plural forms, use i18next plural suffixes: `_one`, `_other`
