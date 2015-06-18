describe("ApplicationNameToClassNameFilter Test", function() {
	var filterInstance;
	
	beforeEach(angular.mock.module("pinpointApp"));
	beforeEach(angular.mock.inject(function($filter) {
		filterInstance = $filter("applicationNameToClassName");
	}));
	
	it("should convert to _ from .", function() {
		expect( filterInstance("pinpoint.navercorp.com") ).toEqual("pinpoint_navercorp_com");
	});
	it("should convert to _ from ^", function() {
		expect( filterInstance("pinpoint^navercorp^com") ).toEqual("pinpoint_navercorp_com");
	});
	it("should convert to _ from :", function() {
		expect( filterInstance("pinpoint:navercorp:com") ).toEqual("pinpoint_navercorp_com");
	});
	it("should convert to _ from . ^ :", function() {
		expect( filterInstance("pinpoint.navercorp.com:8080^TOMCAT") ).toEqual("pinpoint_navercorp_com_8080_TOMCAT");
	});
});