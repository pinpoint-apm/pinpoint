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
  // color@5 부터 color 와 그 의존성(color-string 등)이 ESM 전용이라, jest 의 CJS
  // 런타임이 그대로는 읽지 못한다. color 로 시작하는 패키지만 무시 목록에서 빼고
  // ts-jest 가 CJS 로 바꿔 넘기게 한다. tsconfig 는 여기서만 덮어쓴다 — 빌드용
  // tsconfig 는 ESM 그대로 두어야 vite 번들이 영향받지 않는다.
  transform: {
    '^.+\\.[tj]sx?$': ['ts-jest', { tsconfig: { allowJs: true, module: 'commonjs' } }],
  },
  transformIgnorePatterns: ['/node_modules/(?!color)'],
};
