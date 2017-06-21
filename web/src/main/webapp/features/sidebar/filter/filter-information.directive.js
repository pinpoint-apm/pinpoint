(function() {
	'use strict';
	/**
	 * (en)filterInformationDirective 
	 * @ko filterInformationDirective
	 * @group Directive
	 * @name filterInformationDirective
	 * @class
	 */
	pinpointApp.directive('filterInformationDirective', [ '$filter', '$base64',
	    function ($filter, $base64) {
	        return {
	            restrict: 'EA',
	            replace: true,
	            templateUrl: 'features/sidebar/filter/filterInformation.html?v=' + G_BUILD_TIME,
	            scope : {
	                namespace : '@' // string value
	            },
	            link: function postLink(scope, element, attrs) {
	
	                // define private variables;
	
	                // define private variables of methods;
	                var initialize, reset;
	
	                /**
	                 * initialize
	                 * @param oServerMapFilterVoService
	                 */
	                initialize = function (oServerMapFilterVoService) {
	                    reset();
	                    if (oServerMapFilterVoService.getRequestUrlPattern()) {
	                        scope.urlPattern = $base64.decode(oServerMapFilterVoService.getRequestUrlPattern());
	                    }
	                    var ie = oServerMapFilterVoService.getIncludeException();
	                    if ( ie === null ) {
							scope.includeException = "Success + Failed";
						} else if ( ie === true ) {
							scope.includeException = "Failed Only";
						} else {
							scope.includeException = "Success Only";
						}

	                    if (angular.isNumber(oServerMapFilterVoService.getResponseFrom()) &&
							oServerMapFilterVoService.getResponseTo()) {
	                        var responseTime = [];
	                        responseTime.push($filter('number')(oServerMapFilterVoService.getResponseFrom()));
	                        responseTime.push('ms');
	                        responseTime.push('~');
	                        if (oServerMapFilterVoService.getResponseTo() === 'max') {
	                            responseTime.push('30,000+');
	                        } else {
	                            responseTime.push($filter('number')(oServerMapFilterVoService.getResponseTo()));
	                        }
	                        responseTime.push('ms');
	
	                        scope.responseTime = responseTime.join(' ');
	                    }
	
	                    var fromAgentName = oServerMapFilterVoService.getFromAgentName();
	                    var toAgentName = oServerMapFilterVoService.getToAgentName();
	                    if (fromAgentName || toAgentName) {
	                        scope.agentFilterInfo = (fromAgentName || 'all') + ' -> ' + (toAgentName || 'all');
	                    } else {
	                        scope.agentFilterInfo = false;
	                    }
	
	                };
	
	                /**
	                 * reset
	                 */
	                reset = function () {
	                    scope.urlPattern = 'none';
	                    scope.responseTime = 'none';
	                    scope.includeException = 'none';
	                };
	
	                /**
	                 * scope event on filterInformationDirective.initialize.namespace
	                 */
	                scope.$on('filterInformationDirective.initialize.' + scope.namespace, function (e, oServerMapFilterVoService) {
	                    initialize(oServerMapFilterVoService);
	                });
	            }
	        };
	    }
	]);
})();