describe("Loading Directive Test", function() {
	var newScope, $compile;
	var templateName = "sidebarLoading";
	var loadingMessage = "Loading... Wait!";
	
	beforeEach(angular.mock.module("pinpointApp"));
	beforeEach(angular.mock.inject(function(_$rootScope_, _$compile_, $templateCache) {
		newScope = _$rootScope_.$new();
		$compile = _$compile_;
		
		// original template source : int index.html <script id="sidebarLoading" type="text/ng-template">
		$templateCache.put( templateName, [
		    '<div class="cg-busy cg-busy-animation">',
		        '<div class="cg-busy cg-busy-backdrop"></div>',
		        '<div class="cg-busy-default-wrapper" style="position: absolute; top: 0px; left: 0px; right: 0px; bottom: 0px;">',
		            '<div class="cg-busy-default-sign">',
		                '<div class="cg-busy-default-spinner">',
		                    '<div class="bar1"></div>',
		                    '<div class="bar2"></div>',
		                    '<div class="bar3"></div>',
		                    '<div class="bar4"></div>',
		                    '<div class="bar5"></div>',
		                    '<div class="bar6"></div>',
		                    '<div class="bar7"></div>',
		                    '<div class="bar8"></div>',
		                    '<div class="bar9"></div>',
		                    '<div class="bar10"></div>',
		                    '<div class="bar11"></div>',
		                    '<div class="bar12"></div>',
		                '</div>',
		                '<div class="cg-busy-default-text">{{loadingMessage}}</div>',
		            '</div>',
		        '</div>',
		    '</div>'
        ].join(""));
	}));
	
	it("should bind loadingMessage", function() {
		var elem = angular.element("<div loading-directive='" + templateName + "' loading-message='" + loadingMessage + "'></div>");
		var $elem;
		newScope.$apply(function() {
			$elem = $compile(elem)(newScope);
		});
		expect( $elem.html() ).toContain(loadingMessage);
	});
});