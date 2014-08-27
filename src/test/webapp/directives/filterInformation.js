'use strict';

describe('Directive: filterInformation', function () {

  // load the directive's module
  beforeEach(module('pinpointApp'));

  var element,
    scope;

  beforeEach(inject(function ($rootScope) {
    scope = $rootScope.$new();
  }));

  it('should make hidden element visible', inject(function ($compile) {
    element = angular.element('<filter-information></filter-information>');
    element = $compile(element)(scope);
    expect(element.text()).toBe('this is the filterInformation directive');
  }));
});
