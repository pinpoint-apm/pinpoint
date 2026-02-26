/** @type {import('ts-jest/dist/types').InitialOptionsTsJest} */
module.exports = {
  testEnvironment: 'jsdom',
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
  transform: {
    '^.+\\.tsx?$': [
      'ts-jest',
      {
        tsconfig: {
          moduleResolution: 'node',
        },
      },
    ],
  },
};
