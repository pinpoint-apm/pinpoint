(function() {
	"use strict";
	pinpointApp.constant( "groupedApplicationListDirectiveConfig", {
	});
	pinpointApp.directive( "groupedApplicationListDirective", [ "groupedApplicationListDirectiveConfig", "$rootScope", "$window", "filteredMapUtilService", "ServerMapFilterVoService", "ServerMapHintVoService", "AnalyticsService",
		function( cfg, $rootScope, $window, filteredMapUtilService, ServerMapFilterVoService, ServerMapHintVoService, AnalyticsService ) {
			return {
				restrict: "EA",
				replace: true,
				templateUrl: "features/groupedApplicationList/groupedApplicationList.html?v=" + G_BUILD_TIME,
				link: function postLink( scope ) {
					var savedNavbarVoService;
					scope.node;
					scope.selectedNode = null;
					scope.selectedLink = null;
					scope.isGroupedNode = false;
					scope.isGroupedLink = false;
					scope.searchKeyword = {};
					scope.totalCount = 0;
					scope.groupedApplicationList = [];

					scope.isSelectedNode = function( node ) {
						if ( scope.selectedNode ) {
							return node.key === scope.selectedNode.key ? "selected" : "";
						} else {
							return "";
						}
					};
					scope.isSelectedLink = function( link ) {
						if ( scope.selectedLink ) {
							return link.key === scope.selectedLink.key ? "selected" : "";
						} else {
							return "";
						}
					};
					scope.onSelectNodeApplication = function( node ) {
						AnalyticsService.send(AnalyticsService.CONST.CLK_NODE_IN_GROUPED_VIEW);
						scope.selectedNode = node;
						$rootScope.$broadcast("nodeInfoDetailsDirective.initialize", node, savedNavbarVoService, true);
					};
					scope.onSelectLinkApplication = function( link ) {
						AnalyticsService.send(AnalyticsService.CONST.CLK_LINK_IN_GROUPED_VIEW);
						scope.selectedLink = link;
						$rootScope.$broadcast("nodeInfoDetailsDirective.initialize", {
							from: "",
							to: "",
							histogram: link.histogram,
							timeSeriesHistogram: link.timeSeriesHistogram
						}, savedNavbarVoService, true);
					};

					scope.openFilteredMapWindow = function( link ) {
						var oServerMapFilterVoService = new ServerMapFilterVoService();
						oServerMapFilterVoService
							.setMainApplication(link.filterApplicationName)
							.setMainServiceTypeName(link.filterApplicationServiceTypeName)
							.setMainServiceTypeCode(link.filterApplicationServiceTypeCode)
							.setFromApplication(link.sourceInfo.applicationName)
							.setFromServiceType(link.sourceInfo.serviceType)
							.setToApplication(link.targetInfo.applicationName)
							.setToServiceType(link.targetInfo.serviceType);

						var oServerMapHintVoService = new ServerMapHintVoService();
						if (link.sourceInfo.isWas && link.targetInfo.isWas) {
							oServerMapHintVoService.setHint(link.toNode.applicationName, link.filterTargetRpcList);
						}
						$window.open(filteredMapUtilService.getFilteredMapUrlWithFilterVo( savedNavbarVoService, oServerMapFilterVoService, oServerMapHintVoService ), "");
					};
					scope.openFilterWizard = function( link ) {
						scope.$emit("main.controller.openFilterWizard", link);
					};
					scope.$on("groupedApplicationListDirective.hide", function() {
						scope.isGroupedNode = false;
						scope.isGroupedLink = false;
					});
					scope.$on("groupedApplicationListDirective.initialize", function (event, target, navbarVoService) {
						scope.selectedNode = null;
						scope.selectedLink = null;
						scope.searchKeyword = {};
						if ( target["unknownNodeGroup"] && target["unknownNodeGroup"].length > 0 ) {
							AnalyticsService.send(AnalyticsService.CONST.CLK_SHOW_GROUPED_NODE_VIEW);
							savedNavbarVoService = navbarVoService;
							scope.node = target;
							scope.totalCount = target["totalCount"];
							scope.groupedApplicationList = target["unknownNodeGroup"];
							scope.isGroupedNode = true;
							scope.isGroupedLink = false;
						} else if ( target["unknownLinkGroup"] && target["unknownLinkGroup"].length > 0 ) {
							AnalyticsService.send(AnalyticsService.CONST.CLK_SHOW_GROUPED_LINK_VIEW);
							savedNavbarVoService = navbarVoService;
							scope.totalCount = target["totalCount"];
							scope.groupedApplicationList = target["unknownLinkGroup"];
							scope.isGroupedNode = false;
							scope.isGroupedLink = true;
						} else {
							scope.isGroupedNode = false;
							scope.isGroupedLink = false;
						}
						if (!(scope.$$phase == "$apply" || scope.$$phase == "$digest") ) {
							if (!(scope.$root.$$phase == "$apply" || scope.$root.$$phase == "$digest") ) {
								scope.$digest();
							}
						}
					});
				}
			};
		}
	]);
})();