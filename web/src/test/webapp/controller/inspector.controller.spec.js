describe("Inspector Controller Test", function() {
	
	var $rootSocpe, $controller;
	
	beforeEach(angular.mock.module("pinpointApp"));
	beforeEach(angular.mock.inject(function(_$controller_, _$rootScope_) {
		$rootScope = _$rootScope_.$new();
		$controller = _$controller_("InspectorCtrl", {
			$scope: $rootScope
		});
	}));
	
	it("How to test controller", function() {
		
	})
});