/** @type {import('ts-jest/dist/types').InitialOptionsTsJest} */
module.exports = {
  preset: 'ts-jest',
  testEnvironment: 'jsdom',
  setupFiles: ['<rootDir>/jest.setup.cjs'],
  moduleDirectories: ['node_modules'],
  // Ignore compiled build output so jest does not pick up dist/*.js (ESM) alongside src tests.
  testPathIgnorePatterns: ['/node_modules/', '/dist/'],
  collectCoverageFrom: ['./src/**/*.[jt]s?(x)'],
  moduleNameMapper: {
    '^@pinpoint-fe/ui/src/(.*)$': '<rootDir>/src/$1',
  },
  modulePathIgnorePatterns: ['.*\\.e2e\\.test\\.ts'],
  // react-resizable-panels 는 ESM 전용(`type: module`, exports 에 require 조건 없음)이라
  // jest 의 CJS 런타임이 그대로는 읽지 못한다. 이 패키지만 무시 목록에서 빼고 babel 이 CJS 로
  // 바꿔 넘기게 한다. node_modules 안의 파일에는 상위 babel 설정이 닿지 않으므로
  // `configFile` 로 이 패키지의 설정을 명시해야 한다(없으면 엔트리만 넘어가고 그 아래에서 막힌다).
  transformIgnorePatterns: ['/node_modules/(?!react-resizable-panels/)'],
  transform: {
    '^.+\\.[t|j]sx?$': ['babel-jest', { configFile: require.resolve('./babel.config.cjs') }],
  },
};
