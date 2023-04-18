/** @type {import('ts-jest/dist/types').InitialOptionsTsJest} */
module.exports = {
  preset: 'ts-jest',
  testEnvironment: 'jsdom',
  moduleDirectories: [
    'node_modules',
  ],
  setupFilesAfterEnv: ['<rootDir>/jest.setup.js'],
};