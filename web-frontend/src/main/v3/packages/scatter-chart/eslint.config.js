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
        ...globals.browser, // 브라우저 전역 변수
        ...globals.es2021, // ES2021 전역 변수
        ...globals.jest, // Jest 전역 변수
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
    },
  },

  // Prettier와 충돌하는 규칙 비활성화
  prettierConfig,
];
