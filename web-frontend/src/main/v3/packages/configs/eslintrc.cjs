module.exports = {
  parser: '@typescript-eslint/parser',
  plugins: ['@typescript-eslint'],
  extends: [
    'plugin:@typescript-eslint/recommended',
    'plugin:react-hooks/recommended',
    'plugin:prettier/recommended',
    'prettier',
  ],
  rules: {
    // Additional rules here
    '@typescript-eslint/no-unused-vars': ['error', { ignoreRestSiblings: true }],
  },
};
