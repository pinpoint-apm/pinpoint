'use strict';

describe('Service: SidebarTitleVo', function () {

  // load the service's module
  beforeEach(module('pinpointApp'));

  // instantiate service
  var SidebarTitleVo;
  beforeEach(inject(function (_SidebarTitleVo_) {
    SidebarTitleVo = _SidebarTitleVo_;
  }));

  it('should do something', function () {
    expect(!!SidebarTitleVo).toBe(true);
  });

});
