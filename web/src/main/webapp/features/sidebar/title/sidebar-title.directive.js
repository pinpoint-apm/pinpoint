(function() {
	"use strict";
	/**
	 * (en)sidebarTitleDirective 
	 * @ko sidebarTitleDirective
	 * @group Directive
	 * @name sidebarTitleDirective
	 * @class
	 */
	pinpointApp.directive("sidebarTitleDirective", [ "$timeout", "$rootScope", "CONST_SET",
	    function ($timeout, $rootScope, CONST_SET) {
	        return {
	            restrict: "E",
	            replace: true,
	            templateUrl: "features/sidebar/title/sidebarTitle.html?v=" + G_BUILD_TIME,
	            scope: {
	                namespace: "@"
	            },
	            link: function poLink(scope, element, attrs) {
					scope.agentList = [];

	                $timeout(function () {
	                    empty();
	                });

					function initializeAgentList( node ) {
						scope.currentAgent = CONST_SET.AGENT_ALL;
						if ( typeof node === "undefined" ) {
							scope.agentList = [];
							return;
						}
						var aAgentList = [];
						if ( node.serverList ) {
							for ( var server in node.serverList ) {
								var oInstanceList = node.serverList[server].instanceList;
								for (var agentName in oInstanceList) {
									aAgentList.push(agentName);
								}
							}
						}
						scope.agentList = aAgentList;
					}
	
	                function initialize(oSidebarTitleVoService) {
	                    scope.stImage = oSidebarTitleVoService.getImage();
	                    scope.stImageShow = oSidebarTitleVoService.getImage() ? true : false;
	                    scope.stTitle = oSidebarTitleVoService.getTitle();
	                    scope.stImage2 = oSidebarTitleVoService.getImage2();
	                    scope.stImage2Show = oSidebarTitleVoService.getImage2() ? true : false;
	                    scope.stTitle2 = oSidebarTitleVoService.getTitle2();
	                    $timeout(function () {
	                        element.find('[data-toggle="tooltip"]').tooltip("destroy").tooltip();
	                    });
	                }
	
	                function empty() {
						scope.currentAgent = CONST_SET.AGENT_ALL;
	                    scope.stImage = false;
	                    scope.stImageShow = false;
	                    scope.stTitle = false;
	                    scope.stImage2 = false;
	                    scope.stTitle2 = false;
	                    scope.stImage2Show = false;
						scope.agentList = [];
	                }
					scope.changeAgent = function() {
						$rootScope.$broadcast("changedCurrentAgent", scope.currentAgent );
					};
	                /**
	                 * scope on sidebarTitle.initialize.namespace
	                 */
	                scope.$on("sidebarTitleDirective.initialize." + scope.namespace, function (event, oSidebarTitleVoService, node) {
						initializeAgentList( node );
	                    initialize(oSidebarTitleVoService);
	                });
	
	                /**
	                 * scope on sidebarTitle.empty.namespace
	                 */
	                scope.$on("sidebarTitleDirective.empty." + scope.namespace, function (event) {
	                    empty();
	                });

	            }
	        };
	    }]
	);
})();