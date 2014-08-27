'use strict';

describe('Service: filteredMapUtil', function () {

  // load the service's module
  beforeEach(module('pinpointApp'));

  // instantiate service
  var filteredMapUtil;
  beforeEach(inject(function (_filteredMapUtil_) {
    filteredMapUtil = _filteredMapUtil_;
  }));

  it('should do something', function () {
    expect(!!filteredMapUtil).toBe(true);
  });

});
