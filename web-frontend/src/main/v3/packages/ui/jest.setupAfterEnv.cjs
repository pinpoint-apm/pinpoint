// 경로에서 serviceName을 읽는 규칙은 앱의 라우트 정의(`registerAppRoutes`)에서 온다. 앱은 부트스트랩에서
// 등록하지만 테스트에는 앱이 없으므로 같은 모양의 라우트를 매 테스트 전에 넣는다.
//
// `beforeEach` 안에서 require 해야 한다. 테스트 파일의 `jest.mock`·`jest.resetModules` 뒤의 모듈
// 레지스트리에서 꺼내야, 테스트가 실제로 쓰는 것과 같은 모듈 인스턴스에 등록된다.
beforeEach(() => {
  const { registerAppRoutes } = require('./src/utils/helper/application');
  const { TEST_APP_ROUTES } = require('./src/utils/helper/__fixtures__/appRoutes');

  registerAppRoutes(TEST_APP_ROUTES);
});

// jsdom에는 scrollIntoView가 없다. 저장 실패 시 어긋난 입력으로 스크롤하는 코드가 이것을
// 부르므로, 아무것도 하지 않는 구현으로 채운다.
if (typeof Element !== 'undefined' && !Element.prototype.scrollIntoView) {
  Element.prototype.scrollIntoView = function scrollIntoView() {};
}
