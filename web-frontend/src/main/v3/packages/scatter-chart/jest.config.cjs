/** @type {import('ts-jest/dist/types').InitialOptionsTsJest} */
module.exports = {
  preset: 'ts-jest',
  testEnvironment: 'jsdom',
  // Ignore compiled build output so jest does not pick up dist/*.js (ESM) alongside src tests.
  testPathIgnorePatterns: ['/node_modules/', '/dist/'],
  setupFiles: ['jest-canvas-mock'],
  moduleNameMapper: {
    '\\.(css|sass)$': '<rootDir>/test/mock/styleMock.ts',
  },
};
