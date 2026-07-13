/** @type {import('ts-jest/dist/types').InitialOptionsTsJest} */
module.exports = {
  preset: 'ts-jest',
  testEnvironment: 'jsdom',
  moduleDirectories: ['node_modules'],
  // Ignore compiled build output so jest does not pick up dist/*.js (ESM) alongside src tests.
  testPathIgnorePatterns: ['/node_modules/', '/dist/'],
  collectCoverageFrom: ['./src/**/*.[jt]s?(x)'],
  moduleNameMapper: {
    '^@pinpoint-fe/ui/src/(.*)$': '<rootDir>/src/$1',
  },
  modulePathIgnorePatterns: ['.*\\.e2e\\.test\\.ts'],
  transform: {
    '^.+\\.[t|j]sx?$': 'babel-jest',
  },
};
