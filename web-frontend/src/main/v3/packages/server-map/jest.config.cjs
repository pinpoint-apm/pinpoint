/** @type {import('ts-jest/dist/types').InitialOptionsTsJest} */
module.exports = {
  preset: 'ts-jest',
  testEnvironment: 'jsdom',
  // Ignore compiled build output so jest does not pick up dist/test/*.js (ESM) alongside src tests.
  testPathIgnorePatterns: ['/node_modules/', '/dist/'],
  collectCoverageFrom: [
    './src/**/*.[jt]s?(x)',
    '!./src/storybook/**/*.[jt]s?(x)',
    '!**/*.stories.[jt]s?(x)',
    '!./src/index.ts',
    '!./src/index.tsx',
  ],
  moduleNameMapper: {
    '\\.(css|scss)$': '<rootDir>/src/test/mock/styleMock.ts',
  },
};
