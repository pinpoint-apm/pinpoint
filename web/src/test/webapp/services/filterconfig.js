'use strict';

describe('Service: filterConfig', function () {

  // load the service's module
  beforeEach(module('pinpointApp'));

  // instantiate service
  var filterConfig;
  beforeEach(inject(function (_filterConfig_) {
    filterConfig = _filterConfig_;
  }));

  it('should do something', function () {
    expect(!!filterConfig).toBe(true);
  });

});
