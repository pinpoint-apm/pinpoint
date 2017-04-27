(function() {
	"use strict";
	pinpointApp.constant("linkInfoDetailsDirectiveConfig", {
	    maxTimeToShowLoadAsDefaultForUnknown:  60 * 60 * 12 // 12h
	});
	
	pinpointApp.directive("linkInfoDetailsDirective", [ "linkInfoDetailsDirectiveConfig", "$rootScope", "$filter", "ServerMapFilterVoService",  "filteredMapUtilService", "$timeout", "isVisibleService", "ServerMapHintVoService",
	    function (cfg, $rootScope, $filter, ServerMapFilterVoService, filteredMapUtilService, $timeout, isVisibleService, ServerMapHintVoService) {
	        return {
				scope: true,
				replace: true,
	            restrict: "EA",
	            templateUrl: "features/linkInfoDetails/linkInfoDetails.html?v=" + G_BUILD_TIME,
	            link: function postLink( scope, element ) {
	
	                var htLastLink;
	                var bResponseSummaryForLinkRendered = false;
	                var bLoadForLinkRendered = false;

	                function reset () {
	                    htLastLink = false;
	                    scope.showLinkInfoDetails = false;
	                    scope.showLinkResponseSummary = false;
	                    scope.showLinkLoad = false;
	                    scope.sourceHistogram = false;
	                    scope.namespace = null;
	                    if (!scope.$$phase) {
	                        scope.$digest();
	                    }
	                }
	
	                function showDetailInformation (link) {
						scope.showLinkInfoDetails = true;
						scope.showLinkResponseSummary = true;
						scope.showLinkLoad = true;
						renderResponseSummaryWithHistogram("forLink", link.targetInfo.applicationName, link.histogram, "100%", "150px");
						renderLoad("forLink", link.targetInfo.applicationName, link.timeSeriesHistogram, "100%", "220px", true);

						scope.sourceHistogram = link.sourceHistogram;

	                    if (!scope.$$phase) {
							scope.$digest();
						}
	                }
	                function renderResponseSummaryWithHistogram(namespace, toApplicationName, histogram, w, h) {
	                    var className = $filter("applicationNameToClassName")(toApplicationName);
						namespace = namespace || "forLink_" + className;
	                    if (namespace === "forLink" && bResponseSummaryForLinkRendered) {
	                        // scope.$broadcast('responseTimeSummaryChartDirective.updateData.' + namespace, histogram);
	                    } else {
	                        if (namespace === "forLink") {
	                            bResponseSummaryForLinkRendered = true;
	                        }
							scope.$on("responseTimeSummaryChartDirective.itemClicked." + namespace, function (event, data) {
								var label = data.responseTime;
								var values = data.count;
								var oServerMapFilterVoService = new ServerMapFilterVoService();
								oServerMapFilterVoService
									.setMainApplication(htLastLink.filterApplicationName)
									.setMainServiceTypeCode(htLastLink.filterApplicationServiceTypeCode);

								if (htLastLink.sourceInfo.serviceType === "USER") {
									oServerMapFilterVoService
										.setFromApplication("USER")
										.setFromServiceType("USER");
								} else {
									oServerMapFilterVoService
										.setFromApplication(htLastLink.sourceInfo.applicationName)
										.setFromServiceType(htLastLink.sourceInfo.serviceType);
								}

								oServerMapFilterVoService
									.setToApplication(htLastLink.targetInfo.applicationName)
									.setToServiceType(htLastLink.targetInfo.serviceType);

								if (label.toLowerCase() === "error") {
									oServerMapFilterVoService.setIncludeException(true);
								} else if (label.toLowerCase() === "slow") {
									oServerMapFilterVoService
										.setResponseFrom(filteredMapUtilService.getStartValueForFilterByLabel(label, values) * 1000)
										.setIncludeException(false)
										.setResponseTo("max");
								} else {
									oServerMapFilterVoService
										.setResponseFrom(filteredMapUtilService.getStartValueForFilterByLabel(label, values) * 1000)
										.setIncludeException(false)
										.setResponseTo(parseInt(label, 10) * 1000);
								}

								var oServerMapHintVoService = new ServerMapHintVoService();
								if (htLastLink.sourceInfo.isWas && htLastLink.targetInfo.isWas) {
									oServerMapHintVoService.setHint(htLastLink.targetInfo.applicationName, htLastLink.filterTargetRpcList);
								}
								scope.$emit("linkInfoDetailsDirective.ResponseSummary.barClicked", oServerMapFilterVoService, oServerMapHintVoService);
							});
	                    }
						scope.$broadcast("responseTimeSummaryChartDirective.initAndRenderWithData." + namespace, histogram, w, h, true, true);
	                }
	                function renderLoad(namespace, toApplicationName, timeSeriesHistogram, w, h, useChartCursor) {
	                    var className = $filter("applicationNameToClassName")(toApplicationName);
						namespace = namespace || "forLink_" + className;
	                    if (namespace === "forLink" && bLoadForLinkRendered) {
							// scope.$broadcast('loadChartDirective.initAndRenderWithData.' + namespace, timeSeriesHistogram, w, h, useChartCursor);
	                    } else {
	                        if (namespace === "forLink") {
	                            bLoadForLinkRendered = true;
	                        }
	                    }
						scope.$broadcast("loadChartDirective.initAndRenderWithData." + namespace, timeSeriesHistogram, w, h, useChartCursor);
	                }
	                function hide() {
	                    element.hide();
	                }
	                function show() {
	                    element.show();
	                }
	                scope.$on("linkInfoDetailsDirective.hide", function () {
	                    hide();
	                });
	                scope.$on("linkInfoDetailsDirective.initialize", function (event, link, linkData, navbarVoService) {
	                	if ( link["unknownLinkGroup"] && link["unknownLinkGroup"].length > 0 ) {
	                		scope.showLinkInfoDetails = false;
						} else {
							show();
							reset();
							htLastLink = link;
							scope.oNavbarVoService = navbarVoService;
							showDetailInformation(link);
						}
	                });
	            }
	        };
	    }
	]);
})();