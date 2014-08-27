'use strict';

describe('Animations', function() {

  beforeEach(module('ngAnimate'));

  var scope, $animate;

  // Load ngAnimate and a mock scope
  beforeEach(inject(function($rootScope, _$animate_) {
    scope = $rootScope.$new();
    $animate = _$animate_;
  }));

  it('the animate service should be properly defined', function() {
    expect($animate).toBeDefined();
  });

});
