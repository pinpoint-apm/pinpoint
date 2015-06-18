describe("UserLocales Service Test", function() {
	var mockService;
	
	beforeEach(angular.mock.module("pinpointApp"));
	beforeEach(angular.mock.inject(function($injector) {
		mockService = $injector.get("UserLocalesService");
	}));
	
	it("should has userlocale and defaultLocale property", function() {
		expect( mockService ).toBeDefined("userLocale");
		expect( mockService ).toBeDefined("defaultLocale");
	});
	it("should not empty userlocale and defaultLocale property", function() {
		expect( mockService.userlocale ).not.toEqual("");
		expect( mockService.defaultLocale ).not.toEqual("");
	});
});