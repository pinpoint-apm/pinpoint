describe("IconUrl Filter Test", function() {
	var filterInstance;
	
	beforeEach(angular.mock.module("pinpointApp"));
	beforeEach(angular.mock.inject(function($filter) {
		filterInstance = $filter("iconUrl");
	}));
	
	it("Get general Icon URL", function() {
		expect( filterInstance("TOMCAT") ).toEqual("/images/icons/TOMCAT.png");
	});
	it("Get Unknown Icon URL", function() {
		expect( filterInstance("UNKNOWN_GROUP") ).toEqual("/images/icons/UNKNOWN.png");
	});
	it("Get empty string when not string", function() {
		expect( filterInstance(99) ).toEqual("");
	});
});