'use strict';

describe('Directive: helixChart', function () {

  // load the directive's module
  beforeEach(module('pinpointApp'));

  var element,
    scope;

  beforeEach(inject(function ($rootScope) {
    scope = $rootScope.$new();
  }));

  it('should make hidden element visible', inject(function ($compile) {
    element = angular.element('<helix-chart></helix-chart>');
    element = $compile(element)(scope);
    expect(element.text()).toBe('this is the helixChart directive');
  }));
});
