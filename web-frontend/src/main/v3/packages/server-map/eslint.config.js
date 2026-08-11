import js from '@eslint/js';
import typescript from '@typescript-eslint/eslint-plugin';
import typescriptParser from '@typescript-eslint/parser';
import prettier from 'eslint-plugin-prettier';
import prettierConfig from 'eslint-config-prettier';
import globals from 'globals';

export default [
  // 기본 JavaScript 권장 설정
  js.configs.recommended,

  // TypeScript 파일에 대한 설정
  {
    files: ['**/*.ts', '**/*.tsx'],
    languageOptions: {
      parser: typescriptParser,
      parserOptions: {
        ecmaVersion: 'latest',
        sourceType: 'module',
        project: './tsconfig.json',
      },
      globals: {
        ...globals.browser,
        ...globals.es2021,
        ...globals.jest,
      },
    },
    plugins: {
      '@typescript-eslint': typescript,
      prettier: prettier,
    },
    rules: {
      // TypeScript 권장 규칙들
      ...typescript.configs.recommended.rules,

      // Prettier 통합
      'prettier/prettier': 'error',
      '@typescript-eslint/no-unused-vars': 'warn',
      '@typescript-eslint/no-explicit-any': 'warn',
    },
  },

  // 스토리는 tsconfig 의 include 대상이 아니라 타입 정보 기반 파싱을 쓸 수 없다.
  {
    files: ['**/*.stories.tsx'],
    languageOptions: {
      parserOptions: {
        project: null,
      },
    },
  },

  // Prettier와 충돌하는 규칙 비활성화
  prettierConfig,
];
