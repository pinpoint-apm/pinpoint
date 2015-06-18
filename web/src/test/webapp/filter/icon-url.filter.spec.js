describe("IconUrl Filter Test", function() {
	var filterInstance;
	
	beforeEach(angular.mock.module("pinpointApp"));
	beforeEach(angular.mock.inject(function($filter) {
		filterInstance = $filter("iconUrl");
	}));
	
	it("should get general Icon URL", function() {
		expect( filterInstance("TOMCAT") ).toEqual("/images/icons/TOMCAT.png");
	});
	it("should get Unknown Icon URL", function() {
		expect( filterInstance("UNKNOWN_GROUP") ).toEqual("/images/icons/UNKNOWN.png");
	});
	it("should get empty string when not string", function() {
		expect( filterInstance(99) ).toEqual("");
	});
});