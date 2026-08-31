// react-router v7의 CJS 엔트리는 server-runtime까지 함께 싣고, 그 모듈이 로드 시점에
// `new TextEncoder()`를 만든다. jest의 jsdom 환경에는 TextEncoder/TextDecoder 전역이
// 없어서 `jest.requireActual('react-router')`를 쓰는 스위트가 임포트에서 바로 죽는다.
// Node의 구현을 전역에 채워 넣는다.
const { TextEncoder, TextDecoder } = require('node:util');

if (typeof globalThis.TextEncoder === 'undefined') {
  globalThis.TextEncoder = TextEncoder;
}
if (typeof globalThis.TextDecoder === 'undefined') {
  globalThis.TextDecoder = TextDecoder;
}
