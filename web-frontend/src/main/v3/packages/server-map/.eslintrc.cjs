module.exports = {
  parser: '@typescript-eslint/parser',
  plugins: ['@typescript-eslint'],
  extends: ['plugin:@typescript-eslint/recommended', 'plugin:prettier/recommended', 'prettier'],
  rules: {
    // Additional rules here
  },
  overrides: [
    {
      files: ['./src/**/*.ts'],
    },
  ],
};
