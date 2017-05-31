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
					var htLastNode = null;
					var oNavbarVoService = null;
					var bIsNode = true;
					scope.agentList = [];
					scope.showServerListHtml = false;
					scope.serverCount = 0;
					scope.errorServerCount = 0;

	                $timeout(function () {
	                    empty();
	                });

					function initializeAgentList( node ) {
						scope.currentAgent = preferenceService.getAgentAllStr();
						if ( typeof node === "undefined" || node.isAuthorized === false ) {
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
	
	                function initialize( oSidebarTitleVoService, node, navbarVoService ) {
						htLastNode = node;
						oNavbarVoService = navbarVoService;
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
						checkServerData();
	                }
					function checkServerData() {
						scope.serverCount = 0;
						scope.errorServerCount = 0;
						var p, p2;
						if ( angular.isUndefined( htLastNode.isAuthorized ) || htLastNode.isAuthorized === false ) {
							scope.showServerListHtml = false;
						} else {
							if (htLastNode.sourceHistogram) {
								// link
								bIsNode = false;
								scope.showServerListHtml = ( htLastNode.sourceHistogram && _.isEmpty(htLastNode.sourceHistogram) === false ) ? true : false;

								for (p in htLastNode.sourceHistogram) {
									scope.serverCount++;
									if (htLastNode.sourceHistogram[p].Error > 0) {
										scope.errorServerCount++;
									}
								}
							} else {
								// node
								bIsNode = true;
								scope.showServerListHtml = ( htLastNode.serverList && _.isEmpty(htLastNode.serverList) === false ) ? true : false;

								for (p in htLastNode.serverList) {
									var instanceList = htLastNode.serverList[p].instanceList;
									for (p2 in instanceList) {
										scope.serverCount++;
										if (( htLastNode.agentHistogram[instanceList[p2].name] ) &&
											( htLastNode.agentHistogram[instanceList[p2].name].Error > 0 )) {
											scope.errorServerCount++;
										}
									}
								}
							}
						}
					}
	
	                function empty() {
						scope.currentAgent = preferenceService.getAgentAllStr();
						scope.showServerListHtml = false;
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
						$rootScope.$broadcast("changedCurrentAgent.forMain", scope.currentAgent );
						$rootScope.$broadcast("changedCurrentAgent.forFilteredMap", scope.currentAgent );
					};
					scope.showServerList = function() {
						analyticsService.send(analyticsService.CONST.MAIN, analyticsService.CONST.CLK_SHOW_SERVER_LIST);
						$rootScope.$broadcast("serverListDirective.show", bIsNode, htLastNode, oNavbarVoService);
					};
	                /**
	                 * scope on sidebarTitle.initialize.namespace
	                 */
	                scope.$on("sidebarTitleDirective.initialize." + scope.namespace, function (event, oSidebarTitleVoService, node, navbarVoService) {
	                    initialize( oSidebarTitleVoService, node, navbarVoService );
						initializeAgentList( node );
						$rootScope.$broadcast("serverListDirective.setData", bIsNode, htLastNode, oNavbarVoService);
	                });
	
	                /**
	                 * scope on sidebarTitle.empty.namespace
	                 */
	                scope.$on("sidebarTitleDirective.empty." + scope.namespace, function (event) {
	                    empty();
	                });
					scope.$on("infoDetail.showDetailInformationClicked", function( event, htQuery, node ) {
						htLastNode = node;
						checkServerData();
					});

	            }
	        };
	    }]
	);
})();