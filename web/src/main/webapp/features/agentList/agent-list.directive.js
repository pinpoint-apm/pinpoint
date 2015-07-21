(function() {
	'use strict';
	/**
	 * (en)agentListDirective 
	 * @ko agentListDirective
	 * @group Directive
	 * @name agentListDirective
	 * @class
	 */	
	pinpointApp.constant('agentListConfig', {
	    agentGroupUrl: '/getAgentList.pinpoint'
	});
	
	pinpointApp.directive('agentListDirective', [ 'agentListConfig', '$rootScope', 'helpContentTemplate', 'helpContentService', function (cfg, $rootScope, helpContentTemplate, helpContentService) {
	    return {
	        restrict: 'EA',
	        replace: true,
	        templateUrl: 'features/agentList/agentList.html',
	        link: function postLink(scope, element, attrs) {
	
	            // define private variables of methods
	            var getAgentGroup, showAgentGroup, findAgentByAgentId;
	
	            /**
	             * get agent group
	             * @param query
	             * @param cb
	             */
	            getAgentGroup = function (query, cb) {
	                jQuery.ajax({
	                    type: 'GET',
	                    url: cfg.agentGroupUrl,
	                    cache: false,
	                    dataType: 'json',
	                    data: {
	                        application: query.applicationName,
	                        from: query.from,
	                        to: query.to
	                    },
	                    success: function (result) {
	                        cb(result);
	                    },
	                    error: function (xhr, status, error) {
	                        console.log("ERROR", status, error);
	                    }
	                });
	            };
	
	            /**
	             * show agent group
	             * @param applicationName
	             * @param serviceType
	             * @param from
	             * @param to
	             */
	            showAgentGroup = function (applicationName, serviceType, from, to, selectedAgentId) {
	                var query = {
	                    applicationName: applicationName,
	                    from: from,
	                    to: to
	                };
	                getAgentGroup(query, function (result) {
	                    scope.agentGroup = result;
	                    scope.select(findAgentByAgentId(selectedAgentId));
	                    scope.$digest();
	                });
	            };
	
	            findAgentByAgentId = function (agentId) {
	                for (var key in scope.agentGroup) {
	                    for (var innerKey in scope.agentGroup[key]) {
	                        if (scope.agentGroup[key][innerKey].agentId === agentId) {
	                            return scope.agentGroup[key][innerKey];
	                        }
	                    }
	                }
	                return false;
	            };
	
	            /**
	             * scope select
	             * @param agent
	             */
	            scope.select = function (agent) {
	                scope.currentAgent = agent;
	                scope.$emit('agentListDirective.agentChanged', agent);
	            };
	
	            /**
	             * scope event on agentList.initialize
	             */
	            scope.$on('agentListDirective.initialize', function (event, navbarVoService) {
	                showAgentGroup(navbarVoService.getApplicationName(), navbarVoService.getServiceTypeName(), navbarVoService.getQueryStartTime(), navbarVoService.getQueryEndTime(), navbarVoService.getAgentId());
	            });
	            jQuery('.agentListTooltip').tooltipster({
                	content: function() {
                		return helpContentTemplate(helpContentService.inspector.list);
                	},
                	position: "bottom",
                	trigger: "click"
                });
	        }
	    };
	}]);
})();