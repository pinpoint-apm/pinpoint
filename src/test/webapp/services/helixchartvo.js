'use strict';

describe('Service: helixChartVo', function () {

  // load the service's module
  beforeEach(module('pinpointApp'));

  // instantiate service
  var helixChartVo;
  beforeEach(inject(function (_helixChartVo_) {
    helixChartVo = _helixChartVo_;
  }));

  it('should do something', function () {
    expect(!!helixChartVo).toBe(true);
  });

});
