(function() {
	"use strict";
	pinpointApp.filter( "startFrom", function () {
		return function( node, start ) {
			if ( !node || !node.length ) { return; }
			return node.slice( start );
		};
	});

	pinpointApp.constant("nodeInfoDetailsDirectiveConfig", {
	    maxTimeToShowLoadAsDefaultForUnknown: 60 * 60 * 12 // 12h
	});
	
	pinpointApp.directive("nodeInfoDetailsDirective", [ "nodeInfoDetailsDirectiveConfig", "$rootScope", "$filter", "$timeout", "isVisibleService", "SystemConfigurationService", "$window", "AnalyticsService", "PreferenceService", "TooltipService", "CommonAjaxService",
        function (cfg, $rootScope, $filter, $timeout, isVisibleService, SystemConfigService, $window, analyticsService, preferenceService, tooltipService, commonAjaxService ) {
            return {
                restrict: "EA",
                replace: true,
                templateUrl: "features/nodeInfoDetails/nodeInfoDetails.html?v=" + G_BUILD_TIME,
                scope: {},
                link: function postLink(scope, element) {

                    // define private variables
                    var htServermapData, htLastNode, htUnknownResponseSummary, htUnknownLoad, htQuery,
                        htAgentChartRendered, bShown, sLastKey, getUnknownNode;

                    // define private variables of methods
                    var reset, showDetailInformation, renderAllChartWhichIsVisible, hide, show,
						renderResponseSummary, updateResponseSummary, renderLoad, updateLoad,
						bRequesting = false;

                    // bootstrap
                    bShown = false;
                    scope.htLastUnknownNode = false;

                    // angular.element($window).bind("resize",function(e) {
                    //     if (bShown && /_GROUP$/.test( htLastNode.category ) ) {
                    //         renderAllChartWhichIsVisible(htLastNode);
                    //     }
                    // });
					//
                    // element
                    //     .find(".unknown-list")
                    //     .bind("scroll", function (e) {
                    //         renderAllChartWhichIsVisible(htLastNode);
                    //     });

                    /**
                     * reset
                     */
                    reset = function () {
                        htUnknownResponseSummary = {};
                        htUnknownLoad = {};
                        htAgentChartRendered = {};
                        htQuery = false;
						scope.currentAgent = preferenceService.getAgentAllStr();
                        scope.showNodeInfoDetails = false;
                        scope.node = false;
                        scope.unknownNodeGroup = null;
                        scope.hosts = null;
                        scope.showNodeServers = false;
                        scope.agents = null;
                        scope.showAgents = false;
                        scope.showNodeResponseSummaryForUnknown = false;
                        scope.showNodeResponseSummary = false;
                        scope.showNodeLoad = false;
                        scope.agentHistogram = false;
                        scope.nodeOrderBy = "totalCount";
                        scope.nodeOrderByNameClass = "";
                        scope.nodeOrderByCountClass = "glyphicon-sort-by-order-alt";
                        scope.nodeOrderByDesc = true;

                        if (!(scope.$$phase == "$apply" || scope.$$phase == "$digest") ) {
                        	if (!(scope.$root.$$phase == "$apply" || scope.$root.$$phase == "$digest") ) {
                        		scope.$digest();
                        	}
                        }
                        
                    };

                    /**
                     * show detail information
                     * @param node
                     */
					scope.pagingSize = 3;
                    showDetailInformation = function (node) {
                        scope.showNodeInfoDetails = true;
                        scope.node = node;
						scope.unknownNodeGroup = node.unknownNodeGroup;
                        scope.serverList = node.serverList;
                        scope.showNodeServers = _.isEmpty(scope.serverList) ? false : true;
                        scope.agentHistogram = node.agentHistogram;
                        if ( /_GROUP$/.test( node.serviceType ) === false ) {
                            scope.showNodeResponseSummary = true;
                            scope.showNodeLoad = true;

                            renderResponseSummary("forNode", node.applicationName, node.histogram, "100%", "150px");
                            renderLoad("forNode", node.applicationName, node.timeSeriesHistogram, "100%", "220px", true);
                        } else if ( /_GROUP$/.test( node.serviceType ) ){
                            scope.showNodeResponseSummaryForUnknown = (scope.oNavbarVoService.getPeriod() <= cfg.maxTimeToShowLoadAsDefaultForUnknown) ? false : true;
                            renderAllChartWhichIsVisible(scope.unknownNodeGroup);
                            scope.htLastUnknownNode = node;

                            $timeout(function () {
                                element.find('[data-toggle="tooltip"]').tooltip("destroy").tooltip();
                            });
                        }
                        if (!(scope.$$phase == "$apply" || scope.$$phase == "$digest") ) {
                        	if (!(scope.$root.$$phase == "$apply" || scope.$root.$$phase == "$digest") ) {
                        		scope.$digest();
                        	}
                        }
                    };

                    /**
                     * render all chart which is visible
                     * @param node
                     */
                    renderAllChartWhichIsVisible = function (nodeList, forPaging) {
                        $timeout(function () {
                            angular.forEach(nodeList, function (node){
                                var applicationName = node.applicationName,
                                    className = $filter("applicationNameToClassName")(applicationName);
								if ( forPaging !== true ) {
									if (angular.isDefined(htUnknownResponseSummary[applicationName])) return;
									if (angular.isDefined(htUnknownLoad[applicationName])) return;
								}

                                var elQuery = ".nodeInfoDetails .summaryCharts_" + className,
                                    el = angular.element(elQuery);

								if ( el.length !== 0 ) {
									var visible = isVisibleService(el.get(0), 1);
									if (!visible) return;

									if (scope.showNodeResponseSummaryForUnknown) {
										htUnknownResponseSummary[applicationName] = true;
										renderResponseSummary(null, applicationName, node.histogram, "100%", "150px");
									} else {
										htUnknownLoad[applicationName] = true;
										renderLoad(null, applicationName, node.timeSeriesHistogram, "100%", "220px", true);
									}
								}
                            });
                        });
                    };

                    /**
                     * render response summary
                     * @param namespace
                     * @param toApplicationName
                     * @param histogram
                     * @param w
                     * @param h
                     */
                    renderResponseSummary = function (namespace, toApplicationName, histogram, w, h) {
                        var className = $filter("applicationNameToClassName")(toApplicationName);
						namespace = namespace || "forNode_" + className;
                        scope.$broadcast("responseTimeChartDirective.initAndRenderWithData." + namespace, histogram, w, h, false, true);
                    };
					updateResponseSummary = function (namespace, toApplicationName, histogram, w, h) {
						var className = $filter("applicationNameToClassName")(toApplicationName);
						namespace = namespace || "forNode_" + className;
						scope.$broadcast( "responseTimeChartDirective.updateData." + namespace, histogram );
					};

                    /**
                     * render load
                     * @param namespace
                     * @param toApplicationName
                     * @param timeSeriesHistogram
                     * @param w
                     * @param h
                     * @param useChartCursor
                     */
                    renderLoad = function (namespace, toApplicationName, timeSeriesHistogram, w, h, useChartCursor) {
                        var className = $filter("applicationNameToClassName")(toApplicationName);
						namespace = namespace || "forNode_" + className;
                        scope.$broadcast("loadChartDirective.initAndRenderWithData." + namespace, timeSeriesHistogram, w, h, useChartCursor);
                    };
					updateLoad = function (namespace, toApplicationName, timeSeriesHistogram, w, h, useChartCursor) {
						var className = $filter("applicationNameToClassName")(toApplicationName);
						namespace = namespace || "forNode_" + className;
						scope.$broadcast("loadChartDirective.updateData." + namespace, timeSeriesHistogram);
					};
                    
                    getUnknownNode = function( key ) {
	                	var node = null;
	                	for( var i = 0 ; i < htLastNode.unknownNodeGroup.length ; i++ ) {
	                		if (htLastNode.unknownNodeGroup[i].key === key) {
	                			node = htLastNode.unknownNodeGroup[i];
	                			break;
	                		}
	                	}
	                	return node;
	                };

                    /**
                     * hide
                     */
                    hide = function () {
                        bShown = false;
                        element.hide();
                    };

                    /**
                     * show
                     */
                    show = function () {
                        bShown = true;
                        element.show();
                    };
					function mergeSummaryData( oData ) {
						var oSummarySum = preferenceService.getResponseTypeFormat();
						$.each( oData, function (agentName, oValue ) {
							$.each(oValue, function (innerKey, value) {
								oSummarySum[innerKey] += value;
							});
						});
						return oSummarySum;
					}
					function mergeLoadData( oData ) {
						var aLoadSum = [];
						$.each( oData, function (agentName, aData) {
							for (var i = 0; i < aData.length; i++) {
								var aSet = aData[i];
								if (aLoadSum.length < i + 1) {
									aLoadSum[i] = {
										"key": aSet.key,
										"values": []
									};
								}
								for (var j = 0; j < aSet.values.length; j++) {
									if (aLoadSum[i].values.length < j + 1) {
										aLoadSum[i].values[j] = [
											aSet.values[j][0], 0
										];
									}
									aLoadSum[i].values[j][1] += aSet.values[j][1];
								}
							}
						});
						return aLoadSum;
					}
					function calcuPagingSize( nodeList ) {
						if ( nodeList ) {
							var count = parseInt(nodeList.length / scope.pagingSize) + ( nodeList.length % scope.pagingSize === 0 ? 0 : 1 );
							scope.pagingCount = [];

							for( var i = 1 ; i <= count ; i++ ) {
								scope.pagingCount.push( i );
							}
						}
					}
					scope.isGroupNode = function() {
						if ( scope.node ) {
							return scope.node.serviceType.indexOf("_GROUP") != -1 && scope.isAuthorized;
						} else {
							return false;
						}

					};
					scope.isNotGroupNode = function() {
						if ( scope.node ) {
							return scope.node.serviceType.indexOf("_GROUP") == -1 && scope.isAuthorized;
						} else {
							return false;
						}
					};
					scope.isNotAuthorized = function() {
						return scope.isAuthorized === false;
					};
					scope.getAuthGuideUrl = function() {
						return SystemConfigService.get("securityGuideUrl");
					};

                    /**
                     * show node detail information of scope
                     * @param index
                     */
                    scope.showNodeDetailInformation = function (key) {
                        htLastNode = getUnknownNode(key);//htLastNode.unknownNodeGroup[index];
                        showDetailInformation(htLastNode);
						$rootScope.$broadcast("infoDetail.showDetailInformationClicked", htQuery, htLastNode);
                    };

                    /**
                     * go back to unknown node
                     */
                    scope.goBackToUnknownNode = function () {
                        htLastNode = scope.htLastUnknownNode;
                        htUnknownResponseSummary = {};
                        htUnknownLoad = {};
                        showDetailInformation(htLastNode, scope.currentPage );
						$rootScope.$broadcast("infoDetail.showDetailInformationClicked", htQuery, htLastNode);
                    };

                    /**
                     * scope render node response summary
                     * @param applicationName
                     * @param index
                     */
                    scope.renderNodeResponseSummary = function (applicationName, key) {
                        if (angular.isUndefined(htUnknownResponseSummary[applicationName])) {
                            htUnknownResponseSummary[applicationName] = true;
                            renderResponseSummary(null, applicationName, getUnknownNode(key).histogram, "100%", "150px");
                        }
                    };

                    /**
                     * scope render node load
                     * @param applicationName
                     * @param index
                     */
                    scope.renderNodeLoad = function (applicationName, key) { 
                        if (angular.isUndefined(htUnknownLoad[applicationName])) {
                            htUnknownLoad[applicationName] = true;
                            renderLoad(null, applicationName, getUnknownNode(key).timeSeriesHistogram, "100%", "220px", true);
                        }
                    };

                    /**
                     * render node agent charts
                     * @param applicationName
                     */
                    scope.renderNodeAgentCharts = function (applicationName) {
                    	analyticsService.send(analyticsService.CONST.MAIN, analyticsService.CONST.CLK_SHOW_GRAPH);
                        if (angular.isDefined(htAgentChartRendered[applicationName])) return;
                        htAgentChartRendered[applicationName] = true;
                        renderResponseSummary(null, applicationName, htLastNode.agentHistogram[applicationName], "100%", "150px");
                        renderLoad(null, applicationName, htLastNode.agentTimeSeriesHistogram[applicationName], "100%", "220px", true);
                    };

                    /**
                     * scope node search change
                     */
                    scope.nodeSearchChange = function () {
						scope.currentPage = 1;
						scope.unknownNodeGroup = [];
						for( var i = 0 ; i < htLastNode.unknownNodeGroup.length ; i++ ) {
							var oNode = htLastNode.unknownNodeGroup[i];
							if (scope.nodeSearch) {
								if ( oNode.applicationName.indexOf(scope.nodeSearch) > -1 || oNode.totalCount.toString().indexOf(scope.nodeSearch) > -1) {
									scope.unknownNodeGroup.push( oNode );
								}
							} else {
								scope.unknownNodeGroup.push( oNode );
							}
						}
						calcuPagingSize( scope.unknownNodeGroup );
                        renderAllChartWhichIsVisible(scope.unknownNodeGroup, true);
                    };

                    /**
                     * scope node order by name
                     */
                    scope.nodeOrderByName = function () {
                        if (scope.nodeOrderBy === "applicationName") {
                            scope.nodeOrderByDesc = !scope.nodeOrderByDesc;
                            if (scope.nodeOrderByNameClass === "glyphicon-sort-by-alphabet-alt") {
                                scope.nodeOrderByNameClass = "glyphicon-sort-by-alphabet";
                            } else {
                                scope.nodeOrderByNameClass = "glyphicon-sort-by-alphabet-alt";
                            }
                        } else {
                            scope.nodeOrderByNameClass = "glyphicon-sort-by-alphabet-alt";
                            scope.nodeOrderByCountClass = "";
                            scope.nodeOrderByDesc = true;
                            scope.nodeOrderBy = "applicationName";
                        }
                        renderAllChartWhichIsVisible(scope.unknownNodeGroup, true);
                    };

                    /**
                     * scope node order by count
                     */
                    scope.nodeOrderByCount = function () {
                        if (scope.nodeOrderBy === "totalCount") {
                            scope.nodeOrderByDesc = !scope.nodeOrderByDesc;
                            if (scope.nodeOrderByCountClass === "glyphicon-sort-by-order-alt") {
                                scope.nodeOrderByCountClass = "glyphicon-sort-by-order";
                            } else {
                                scope.nodeOrderByCountClass = "glyphicon-sort-by-order-alt";
                            }
                        } else {
                            scope.nodeOrderByCountClass = "glyphicon-sort-by-order-alt";
                            scope.nodeOrderByNameClass = "";
                            scope.nodeOrderByDesc = true;
                            scope.nodeOrderBy = "totalCount";
                        }
                        renderAllChartWhichIsVisible(scope.unknownNodeGroup, true);
                    };

                    /**
                     * show unknown node by
                     * @param nodeSearch
                     * @param node
                     * @returns {boolean}
                     */
                    scope.showUnknownNodeBy = function (nodeSearch, node) {
                        if (nodeSearch) {
                            if (node.applicationName.indexOf(nodeSearch) > -1 ||  node.totalCount.toString().indexOf(nodeSearch) > -1) {
                                return true;
                            } else {
                                return false;
                            }
                        } else {
                            return true;
                        }
                    };
                    scope.movePaging = function( nextPage ) {
						if (scope.currentPage == nextPage) return;
						scope.currentPage = nextPage;
						renderAllChartWhichIsVisible(scope.unknownNodeGroup, true);
					};
                    /**
                     * scope event on nodeInfoDetailsDirective.initialize
                     */
                    scope.$on("nodeInfoDetailsDirective.initialize", function (event, e, query, node, mapData, navbarVoService, reloadOnly, searchQuery) {
                        show();
                        reset();
                        htQuery = query;
                        sLastKey = node.key;
                        htLastNode = node;
						scope.isAuthorized = node.isAuthorized === false ? false : true;
                        scope.htLastUnknownNode = false;
                        scope.oNavbarVoService = navbarVoService;
                        scope.nodeSearch = searchQuery || "";
						scope.currentPage = 1;
                        htServermapData = mapData;

						calcuPagingSize( node.unknownNodeGroup );
						showDetailInformation(node);
                    });

                    /**
                     * scope event on nodeInfoDetailsDirective.hide
                     */
                    scope.$on("nodeInfoDetailsDirective.hide", function (event) {
                        hide();
                    });

                    /**
                     * scope event on nodeInfoDetailsDirective.lazyRendering
                     */
                    scope.$on("nodeInfoDetailsDirective.lazyRendering", function (event, e) {
                        renderAllChartWhichIsVisible(scope.unknownNodeGroup);
                    });

                    scope.$on("responseTimeChartDirective.itemClicked.forNode", function (event, data) {
//                        console.log("on responseTimeChartDirective.itemClicked.forNode", data);
                    });
					scope.$on("responseTimeChartDirective.loadRealtime", function (event, applicationName, agentName, from, to ) {
						if ( angular.isUndefined( scope.node ) || ( scope.node.applicationName !== applicationName ) ) {
							return;
						}
						if ( bRequesting === false ) {
							bRequesting = true;
							commonAjaxService.getResponseTimeHistogramData( {
								"applicationName": scope.node.applicationName,
								"serviceTypeName": scope.node.category,
								"from": from,
								"to": to
							}, function (oResult) {
								if (agentName === preferenceService.getAgentAllStr()) {
									updateResponseSummary("forNode", scope.node.applicationName, mergeSummaryData( oResult.summary ) );
									updateLoad("forNode", scope.node.applicationName, mergeLoadData( oResult.timeSeries ) );
								} else {
									updateResponseSummary("forNode", scope.node.applicationName, oResult.summary[agentName]);
									updateLoad("forNode", scope.node.applicationName, oResult.timeSeries[agentName]);
								}
								bRequesting = false;
							}, function() {
								bRequesting = false;
							});
						}
					});
					scope.$on("changedCurrentAgent.forMain", function( event, agentName ) {
						var responseSummaryData = null;
						var loadData = null;
						if ( agentName === preferenceService.getAgentAllStr() ) {
							responseSummaryData = scope.node.histogram;
							loadData = scope.node.timeSeriesHistogram;
						} else {
							responseSummaryData = scope.node.agentHistogram[agentName];
							loadData = scope.node.agentTimeSeriesHistogram[agentName];
						}
						renderResponseSummary("forNode", scope.node.applicationName, responseSummaryData, "100%", "150px");
						renderLoad("forNode", scope.node.applicationName, loadData, "100%", "220px", true);
					});

					tooltipService.init( "responseSummaryChart" );
					tooltipService.init( "loadChart" );
                }
            };
	    }
	]);
})();