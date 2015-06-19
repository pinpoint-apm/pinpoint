describe("Inspector Controller Test", function() {
	
	var $rootSocpe, $controller, newScope, navbarVoService;
	
	beforeEach(angular.mock.module("pinpointApp"));
	beforeEach(angular.mock.inject(function(_$controller_, _$rootScope_, $injector) {
		$controller = _$controller_;
		$rootScope = _$rootScope_;
		newScope = _$rootScope_.$new();
		navbarVoService = $injector.get("NavbarVoService");
		navbarVoService.apply( navbarVoService, null );
		
//		spyOn($rootScope, "$broadcast").and.callThrough();
//		spyOn($rootScope, "$emit").and.callThrough();
		
	}));
	
	it("should trigger 'navbarDirective.changed' listener", function() {
		spyOn(newScope, "$on").and.callThrough();
		$controller("InspectorCtrl", {
			$scope: newScope
		});
		$rootScope.$broadcast("navbarDirective.changed", navbarVoService);
		expect(newScope.$on).toHaveBeenCalled();
	});
//	it("should trigger 'agentListDirective.agentChanged' listener", function() {
//		_$controller_("InspectorCtrl", {
//			$scope: newScope
//		});
//		$rootScope.$broadcast("agentListDirective.agentChanged", agent);
//		expect(newScope.$on).toHaveBeenCalled();
//	});	
});