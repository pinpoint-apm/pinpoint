'use strict';

describe('Service: ServerMapFilterVo', function () {

  // load the service's module
  beforeEach(module('pinpointApp'));

  // instantiate service
  var ServerMapFilterVo;
  beforeEach(inject(function (_ServerMapFilterVo_) {
    ServerMapFilterVo = _ServerMapFilterVo_;
  }));

  it('should do something', function () {
    expect(!!ServerMapFilterVo).toBe(true);
  });

});
