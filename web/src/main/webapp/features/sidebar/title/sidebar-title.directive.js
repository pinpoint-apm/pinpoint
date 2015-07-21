(function() {
	'use strict';
	/**
	 * (en)sidebarTitleDirective 
	 * @ko sidebarTitleDirective
	 * @group Directive
	 * @name sidebarTitleDirective
	 * @class
	 */
	pinpointApp.directive('sidebarTitleDirective', [ '$timeout',
	    function ($timeout) {
	        return {
	            restrict: 'E',
	            replace: true,
	            templateUrl: 'features/sidebar/title/sidebarTitle.html',
	            scope: {
	                namespace: '@'
	            },
	            link: function poLink(scope, element, attrs) {
	
	                // define private variables of methods
	                var initialize, empty;
	
	                /**
	                 * bootrap
	                 */
	                $timeout(function () {
	                    empty();
	                });
	
	                /**
	                 * initialize
	                 * @param oSidebarTitleVoService
	                 */
	                initialize = function (oSidebarTitleVoService) {
	                    scope.stImage = oSidebarTitleVoService.getImage();
	                    scope.stImageShow = oSidebarTitleVoService.getImage() ? true : false;
	                    scope.stTitle = oSidebarTitleVoService.getTitle();
	                    scope.stImage2 = oSidebarTitleVoService.getImage2();
	                    scope.stImage2Show = oSidebarTitleVoService.getImage2() ? true : false;
	                    scope.stTitle2 = oSidebarTitleVoService.getTitle2();
	                    $timeout(function () {
	                        element.find('[data-toggle="tooltip"]').tooltip('destroy').tooltip();
	                    });
	                };
	
	                /**
	                 * empty
	                 */
	                empty = function () {
	                    scope.stImage = false;
	                    scope.stImageShow = false;
	                    scope.stTitle = false;
	                    scope.stImage2 = false;
	                    scope.stTitle2 = false;
	                    scope.stImage2Show = false;
	                };
	
	                /**
	                 * scope on sidebarTitle.initialize.namespace
	                 */
	                scope.$on('sidebarTitleDirective.initialize.' + scope.namespace, function (event, oSidebarTitleVoService) {
	                    initialize(oSidebarTitleVoService);
	                });
	
	                /**
	                 * scope on sidebarTitle.empty.namespace
	                 */
	                scope.$on('sidebarTitleDirective.empty.' + scope.namespace, function (event) {
	                    empty();
	                });
	            }
	        };
	    }]
	);
})();