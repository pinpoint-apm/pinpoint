/** @type {import('ts-jest/dist/types').InitialOptionsTsJest} */
module.exports = {
  preset: 'ts-jest',
  testEnvironment: 'jsdom',
  moduleDirectories: ['node_modules'],
  collectCoverageFrom: ['./src/**/*.[jt]s?(x)'],
  moduleNameMapper: {
    '^@pinpoint-fe/ui/(.*)$': '<rootDir>/src/$1',
  },
};
