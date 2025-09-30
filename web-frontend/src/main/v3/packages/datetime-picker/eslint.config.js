import js from '@eslint/js';
import typescript from '@typescript-eslint/eslint-plugin';
import typescriptParser from '@typescript-eslint/parser';
import reactHooks from 'eslint-plugin-react-hooks';
import reactRefresh from 'eslint-plugin-react-refresh';
import storybook from 'eslint-plugin-storybook';
import prettier from 'eslint-plugin-prettier';
import prettierConfig from 'eslint-config-prettier';
import globals from 'globals';

export default [
  // 기본 JavaScript 권장 설정
  js.configs.recommended,

  // 전역 설정
  {
    languageOptions: {
      parser: typescriptParser,
      ecmaVersion: 'latest',
      sourceType: 'module',
      globals: {
        ...globals.browser,
        ...globals.es2021,
        ...globals.node,
        ...globals.jest,
      },
    },
  },

  // TypeScript 설정
  {
    files: ['**/*.ts', '**/*.tsx'],
    plugins: {
      '@typescript-eslint': typescript,
    },
    rules: {
      ...typescript.configs.recommended.rules,
      'no-undef': 'off', // TypeScript가 이미 타입 체크를 하므로 비활성화
    },
  },

  // React Hooks 설정
  {
    plugins: {
      'react-hooks': reactHooks,
    },
    rules: {
      ...reactHooks.configs.recommended.rules,
    },
  },

  // React Refresh 설정
  {
    plugins: {
      'react-refresh': reactRefresh,
    },
    rules: {
      'react-refresh/only-export-components': [
        'warn',
        {
          allowConstantExport: true,
        },
      ],
    },
  },

  // Storybook 설정
  {
    plugins: {
      storybook: storybook,
    },
    rules: {
      ...storybook.configs.recommended.rules,
    },
  },

  // Prettier 설정
  {
    plugins: {
      prettier: prettier,
    },
    rules: {
      ...prettier.configs.recommended.rules,
      ...prettierConfig.rules,
    },
  },
];
