import js from '@eslint/js';
import typescript from '@typescript-eslint/eslint-plugin';
import typescriptParser from '@typescript-eslint/parser';
import reactRefresh from 'eslint-plugin-react-refresh';
import reactHooks from 'eslint-plugin-react-hooks';
import prettier from 'eslint-plugin-prettier';
import prettierConfig from 'eslint-config-prettier';
import storybook from 'eslint-plugin-storybook';
import globals from 'globals';

export default [
  // 기본 JavaScript 권장 규칙
  js.configs.recommended,
  // 메인 설정
  {
    files: ['**/*.{js,jsx,ts,tsx}'],
    languageOptions: {
      parser: typescriptParser,
      parserOptions: {
        ecmaVersion: 'latest',
        sourceType: 'module',
        project: './tsconfig.json',
      },
      sourceType: 'module',
      globals: {
        ...globals.browser,
        ...globals.es2021,
        ...globals.node,
        ...globals.jest,
        JSX: 'readonly',
        NodeJS: 'readonly',
        React: 'readonly',
      },
    },
    plugins: {
      '@typescript-eslint': typescript,
      'react-refresh': reactRefresh,
      'react-hooks': reactHooks,
      prettier: prettier,
      storybook: storybook,
    },

    rules: {
      // TypeScript 권장 규칙
      ...typescript.configs.recommended.rules,

      // React Hooks 권장 규칙
      ...reactHooks.configs.recommended.rules,

      // Prettier 통합
      ...prettierConfig.rules,
      'prettier/prettier': 'error',

      // Storybook 권장 규칙
      ...storybook.configs.recommended.rules,

      // 커스텀 규칙 (기존과 동일)
      'react-refresh/only-export-components': ['warn', { allowConstantExport: true }],
      // API 응답 타입은 `namespace GetServerMap { ... }` 패턴을 규약으로 쓴다.
      '@typescript-eslint/no-namespace': 'off',
      'no-useless-catch': 'off',
      '@typescript-eslint/no-unused-vars': 'off',
      'no-extra-boolean-cast': 'off',
      // `interface XxxProps extends XxxFetcherProps {}` 는 컴포넌트 props 규약이라 허용하고,
      // `{}` 를 타입으로 직접 쓰는 것만 잡는다.
      '@typescript-eslint/no-empty-object-type': [
        'warn',
        { allowInterfaces: 'with-single-extends' },
      ],
    },
  },
  // 배럴 파일은 재수출만 하므로 Fast Refresh 규칙을 만족시킬 방법이 없다.
  {
    files: ['**/index.{ts,tsx}'],
    rules: {
      'react-refresh/only-export-components': 'off',
    },
  },
  // jest.mock 팩토리와 jest.isolateModules 안에서는 정적 import 를 쓸 수 없어 require 가 필수다.
  {
    files: ['**/*.test.{ts,tsx}'],
    rules: {
      '@typescript-eslint/no-require-imports': 'off',
    },
  },
];
