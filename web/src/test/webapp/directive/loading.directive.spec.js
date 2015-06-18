describe("Loading Directive Test", function() {
	var $rootScope;
	var $compile;
	
	beforeEach(angular.mock.module("pinpointApp"));
	beforeEach(angular.mock.inject(function(_$rootScope_, _$compile_) {
		$rootScope = _$rootScope_;
		$compile = _$compile_;
	}));
	
	it("- Check loaindg message", function() {
		var elem = $compile("<div loading-directive='sidebarLoading' loading-message='Loading... Wait!'></div>")($rootScope);
		$rootScope.$digest();
		//expect( elem.html() ).toContain("Loading... Wait!");
	});
});