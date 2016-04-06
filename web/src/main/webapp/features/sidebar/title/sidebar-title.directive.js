(function() {
	"use strict";
	/**
	 * (en)sidebarTitleDirective 
	 * @ko sidebarTitleDirective
	 * @group Directive
	 * @name sidebarTitleDirective
	 * @class
	 */
	pinpointApp.directive("sidebarTitleDirective", [ "$timeout", "$rootScope", "PreferenceService", "AnalyticsService",
	    function ( $timeout, $rootScope, preferenceService, analyticsService ) {
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
						scope.currentAgent = preferenceService.getAgentAllStr();
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
	
	                function initialize( oSidebarTitleVoService, node ) {
						scope.isWas = angular.isDefined( node ) ? ( angular.isDefined( node.isWas ) ? node.isWas : false ) : false;
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
						scope.currentAgent = preferenceService.getAgentAllStr();
	                    scope.stImage = false;
	                    scope.stImageShow = false;
	                    scope.stTitle = false;
	                    scope.stImage2 = false;
	                    scope.stTitle2 = false;
	                    scope.stImage2Show = false;
						scope.isWas = false;
						scope.agentList = [];
	                }
					scope.changeAgent = function() {
						analyticsService.send( analyticsService.CONST.INSPECTOR, analyticsService.CONST.CLK_CHANGE_AGENT_MAIN );
						$rootScope.$broadcast("changedCurrentAgent", scope.currentAgent );
					};
	                /**
	                 * scope on sidebarTitle.initialize.namespace
	                 */
	                scope.$on("sidebarTitleDirective.initialize." + scope.namespace, function (event, oSidebarTitleVoService, node) {
	                    initialize( oSidebarTitleVoService, node );
						initializeAgentList( node );
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