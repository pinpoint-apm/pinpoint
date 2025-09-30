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
      '@typescript-eslint/no-namespace': 'warn',
      'no-useless-catch': 'off',
      '@typescript-eslint/no-unused-vars': 'off',
      'no-extra-boolean-cast': 'off',
      '@typescript-eslint/no-empty-object-type': 'warn',
    },
  },
];
