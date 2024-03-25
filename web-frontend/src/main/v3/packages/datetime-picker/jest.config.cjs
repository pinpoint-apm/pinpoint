module.exports = {
  preset: 'ts-jest',
  testEnvironment: 'jsdom',
  transform: {
    '^.+\\.tsx?$': 'ts-jest',
  },
  collectCoverageFrom: [
    './src/**/*.[jt]s?(x)',
    '!./src/stories/**/*.[jt]s?(x)',
    '!**/*.stories.[jt]s?(x)',
  ],
  moduleNameMapper: {
    '\\.(css|scss)$': '<rootDir>/src/test/mock/styleMock.ts',
    '^.+\\.svg$': '<rootDir>/src/test/mock/svgMock.tsx',
    '^@/components/(.*)$': '<rootDir>/src/components/$1',
    '^@/constants/(.*)$': '<rootDir>/src/constants/$1',
    '^@/utils/(.*)$': '<rootDir>/src/utils/$1',
    '^@/types/(.*)$': '<rootDir>/src/types/$1',
    '^@/assets/(.*)$': '<rootDir>/src/assets/$1',
  },
};
