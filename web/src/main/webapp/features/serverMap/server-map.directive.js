(function() {
	"use strict";
	pinpointApp.constant("serverMapDirectiveConfig", {
	    options: {
	        "sBigFont": "11pt Lato,NanumGothic,ng,dotum,AppleGothic,sans-serif",
			"sImageDir": "images/servermap/",
	        "sSmallFont": "11pt Lato,NanumGothic,ng,dotum,AppleGothic,sans-serif",
			"htLinkType": {
				"sCurve": "JumpGap", // Bezier, JumpOver, JumpGap
	            "sRouting": "Normal" // Normal, Orthogonal, AvoidNodes
	        },
	        "htLinkTheme": {
	            "default": {
					"margin": 1,
					"fontColor": "#000",
					"fontAlign": "center",
					"fontFamily": "11pt Lato,NanumGothic,ng,dotum,AppleGothic,sans-serif",
					"borderColor": "#C5C5C5",
					"strokeWidth": 1,
					"backgroundColor": "#FFF"
	            },
	            "bad": {
					"margin": 1,
	                "fontColor": "#FF1300",
	                "fontAlign": "center",
					"fontFamily": "11pt Lato,NanumGothic,ng,dotum,AppleGothic,sans-serif",
					"borderColor": "#7D7D7D",
	                "strokeWidth": 1,
					"backgroundColor": "#FFC9C9"
	            }
	        },
			"sOverviewId": "servermapOverview",
			"sContainerId": "servermap"
		}
	});
	
	pinpointApp.directive("serverMapDirective", [ "serverMapDirectiveConfig", "$rootScope", "SystemConfigurationService", "ServerMapDaoService", "AlertsService", "ProgressBarService", "SidebarTitleVoService", "$filter", "ServerMapFilterVoService", "filteredMapUtilService", "$base64", "ServerMapHintVoService", "$timeout", "$location", "$window", "helpContentTemplate", "helpContentService", "AnalyticsService", "TooltipService",
	    function (cfg, $rootScope, SystemConfigService, ServerMapDaoService, AlertsService, ProgressBarService, SidebarTitleVoService, $filter, ServerMapFilterVoService, filteredMapUtilService, $base64, ServerMapHintVoService, $timeout, $location, $window, helpContentTemplate, helpContentService, AnalyticsService, TooltipService) {
	        return {
				replace: true,
	            restrict: "EA",
	            templateUrl: "features/serverMap/serverMap.html?v=" + G_BUILD_TIME,
	            link: function postLink( scope, element ) {
	                var htLastMergedMapData;
					var bUseLinkContextMenu = true;
					var bUseBackgroundContextMenu = true;
					var bUseNodeContextMenu = false;
	                var oAlertService = new AlertsService(element);
	                var oProgressBarService = new ProgressBarService(element);
	                var sLastSelection = false;
	                var oServerMap = null;
	                var htLastMapData = {
	                    applicationMapData: {
	                        linkDataArray: [],
	                        nodeDataArray: []
	                    },
	                    lastFetchedTimestamp: [],
	                    timeSeriesResponses: {
	                        values: {},
	                        time: []
	                    }
	                };
					var htLastQuery = {};
					var htLastLink = {};
					var htLastNode = {};
					var bIsFilterWizardLoaded = false;
					var $serverMapTime = element.find(".serverMapTime");
					var $fromAgentName = element.find(".fromAgentName");
					var $toAgentName = element.find(".toAgentName");
					var $urlPattern = element.find("#urlPattern");
					$fromAgentName.select2();
					$toAgentName.select2();

	                scope.oNavbarVoService = null;
	                scope.totalRequestCount = true;
	                scope.bShowServerMapStatus = false;
	                scope.linkRouting = cfg.options.htLinkType.sRouting;
	                scope.linkCurve = cfg.options.htLinkType.sCurve;
	                scope.searchNodeQuery = "";
	                scope.searchNodeIndex = 0;
	                scope.searchNodeList = [];
					scope.mergeTypeList = [];
					scope.mergeStatus = {};
					scope.showAntStyleHint = false;
					scope.routingType = [ "Normal", "Orthogonal", "AvoidsNodes" ];
					scope.curveType = [ "None", "JumpOver", "JumpGap", "Bezier" ];

	                function extractMergeTypeList( serverMapData ) {
	                	serverMapData.nodeDataArray.forEach( function( o ) {
	                		if ( o.isWas === false && ( angular.isUndefined( o.isQueue ) || o.isQueue === false ) && o.serviceType !== "USER" ) {
	                			if ( angular.isUndefined( scope.mergeStatus[o.serviceType] ) ) {
		                			scope.mergeTypeList.push( o.serviceType );
		                			scope.mergeStatus[o.serviceType] = true;
	                			}
	                		}
	                	});
	                }
	                function reset() {
	                    scope.nodeContextMenuStyle = '';
	                    scope.linkContextMenuStyle = '';
	                    scope.backgroundContextMenuStyle = '';
						$urlPattern.val("");
	                    scope.responseTime = {
	                        from: 0,
	                        to: 30000
	                    };
	                    scope.includeFailed = null;
	                    $("#filterWizard").modal("hide");
	
	                    if (!(scope.$$phase == "$apply" || scope.$$phase == "$digest") ) {
	                    	if (!(scope.$root.$$phase == "$apply" || scope.$root.$$phase == "$digest") ) {
	                    		scope.$digest();
	                    	}
	                    }
	                }
	                function showServerMap(applicationName, serviceTypeName, to, period, filterText, hintText, linkRouting, linkCurve) {
	                    oProgressBarService.startLoading();
	                    oAlertService.hideError();
	                    oAlertService.hideWarning();
	                    oAlertService.hideInfo();
	                    if (oServerMap) {
	                        oServerMap.clear();
	                    }
	                    oProgressBarService.setLoading(10);

	                    htLastQuery = {
	                        applicationName: applicationName,
	                        serviceTypeName: serviceTypeName,
	                        from: to - period,
	                        to: to,
	                        originTo: scope.oNavbarVoService.getQueryEndTime(),
	                        callerRange: scope.oNavbarVoService.getCallerRange(),
	                        calleeRange: scope.oNavbarVoService.getCalleeRange(),
							bidirectional: scope.oNavbarVoService.getBidirectional(),
							wasOnly: scope.oNavbarVoService.getWasOnly(),
	                        period: period,
	                        filter: $window.encodeURIComponent(filterText),
	                        hint: hintText ? $window.encodeURIComponent(hintText) : false
	                    };
	
	                    if (filterText) {
	                        ServerMapDaoService.getFilteredServerMapData(htLastQuery, function (err, query, result) {
	                            if (err) {
	                                oProgressBarService.stopLoading();
	                                oAlertService.showError('There is some error.');
	                                return false;
	                            }
	                            oProgressBarService.setLoading(50);
	                            if (query.from === result.lastFetchedTimestamp) {
	                                scope.$emit('serverMapDirective.allFetched', result);
	                            } else {
	                                htLastMapData.lastFetchedTimestamp = result.lastFetchedTimestamp - 1;
	                                scope.$emit('serverMapDirective.fetched', htLastMapData.lastFetchedTimestamp, result);
	                            }
	                            var filters = JSON.parse(filterText);
	                            htLastMapData.applicationMapData = ServerMapDaoService.mergeFilteredMapData(htLastMapData.applicationMapData, result.applicationMapData);
	                            var serverMapData = ServerMapDaoService.extractDataFromApplicationMapData(htLastMapData.applicationMapData);
	                            serverMapData = ServerMapDaoService.addFilterProperty(filters, serverMapData);
	                            extractMergeTypeList( serverMapData );
	                            if (filteredMapUtilService.doFiltersHaveUnknownNode(filters)) {
	                            	for( var key in scope.mergeStatus ) {
	                            		scope.mergeStatus[key] = false;
	                            	}
	                            }
	                            emitDataExisting(htLastMapData);
	                            serverMapCallback(query, serverMapData, linkRouting, linkCurve);
	                        });
	                    } else {
	                        ServerMapDaoService.getServerMapData(htLastQuery, function (err, query, mapData) {
	                            if (err || mapData.exception ) {
	                                oProgressBarService.stopLoading();
	                                if ( err ) {
	                                	oAlertService.showError('There is some error.');
	                                } else {
	                                	oAlertService.showError(mapData.exception);
	                                }
	                                scope.$emit('serverMapDirective.hasNoData');
	                                return false;
	                            }
	                            oProgressBarService.setLoading(50);
	                            emitDataExisting(mapData);
	                            htLastMapData = mapData;
	                            var serverMapData = ServerMapDaoService.extractDataFromApplicationMapData(mapData.applicationMapData);
	                            extractMergeTypeList( serverMapData );
	                            serverMapCallback(query, serverMapData, linkRouting, linkCurve);
	                        });
	                    }
	                }
	                function emitDataExisting(mapData) {
	                    if (mapData.applicationMapData.nodeDataArray.length === 0 ) {
	                        scope.$emit('serverMapDirective.hasNoData');
	                    } else {
	                        scope.$emit('serverMapDirective.hasData');
	                    }
	                }
	                function setNodeContextMenuPosition(top, left, applicationName, category) {
	                	scope.inspectApplicationName = applicationName;
						scope.inspectCategory = category;
	                    scope.nodeContextMenuStyle = {
	                        display: 'block',
	                        'top': top,
	                        'left': left,
	                        'z-index': 9999999 // it should be higher than 9999998, because of intro.js
	                    };
	                    scope.$digest();
	                }
	                function setLinkContextMenuPosition(top, left) {
	                    scope.linkContextMenuStyle = {
	                        display: 'block',
	                        'top': top,
	                        'left': left,
	                        'z-index': 9999999 // it should be higher than 9999998, because of intro.js
	                    };
	                    scope.$digest();
	                }
	                function setBackgroundContextMenuPosition(top, left) {
	                    scope.backgroundContextMenuStyle = {
	                        display: 'block',
	                        'top': top,
	                        'left': left,
	                        'z-index': 9999999 // it should be higher than 9999998, because of intro.js
	                    };
	                    scope.$digest();
	                }
	                function getMergeArray() {
	                	var a = [];
	                	for( var key in scope.mergeStatus ) {
	                		if ( scope.mergeStatus[key] === true ) {
	                			a.push( key );
	                		}
	                	}
	                	return a;
	                }
	                function setNoDataAlert() {
						var aFilterInfo = [];
						if ( scope.oNavbarVoService.getFilter() ) {
							var aFilter = scope.oNavbarVoService.getFilterAsJson();
							aFilterInfo.push( "<p>There is no data with the filter below.</p>" );

							angular.forEach( aFilter, function (f, idx) {
								aFilterInfo.push("<p><b>Filter");
								if (aFilter.length > 1) {
									aFilterInfo.push(" #" + ( idx + 1 ) + "");
								}
								aFilterInfo.push(" : " + f.fa + "(" + f.fst + ") ~ " + f.ta + "(" + f.tst + ")" + "</b><br>");
								aFilterInfo.push("<ul>");
								if (f.url) {
									aFilterInfo.push("<li>Url Pattern : " + $base64.decode(f.url) + "</li>");
								}
								if (f.rf && f.rt) {
									aFilterInfo.push("<li>Response Time : " + $filter("number")(f.rf) + " ms ~ " + $filter("number")(f.rt) + " ms</li>");
								}
								aFilterInfo.push("<li>Transaction Result : " + (f.ie ? "Failed Only" : "Success + Failed") + "</li>");
								aFilterInfo.push("</ul></p>");
							});
						} else {
							aFilterInfo.push( "There is no data." );
						}
						oAlertService.showInfo( aFilterInfo.join( "" ) );
					}
	                function serverMapCallback( query, applicationMapData, linkRouting, linkCurve, retry ) {
	                	var mergeArray = getMergeArray();
	                	htLastMergedMapData = ServerMapDaoService.mergeMultiLinkGroup( ServerMapDaoService.mergeGroup(applicationMapData, mergeArray), mergeArray );
	
	                    oProgressBarService.setLoading(80);
	                    if ( htLastMergedMapData.nodeDataArray.length === 0 ) {
	                        oProgressBarService.stopLoading();
	                        setNoDataAlert();
	                        return;
	                    }
	                    htLastMergedMapData.linkDataArray = setLinkOption(htLastMergedMapData.linkDataArray, linkRouting, linkCurve);
	                    oProgressBarService.setLoading(90);
	
	                    var options = cfg.options;
	                    options.fOnNodeSubGroupClicked = function(e, node, nodeKey, fromName) {
	                    	var link = ServerMapDaoService.getLinkNodeDataByNodeKey(htLastMapData.applicationMapData, nodeKey, fromName);
	                    	link.fromNode = ServerMapDaoService.getNodeDataByKey(htLastMapData.applicationMapData, link.from);
	                    	link.toNode = ServerMapDaoService.getNodeDataByKey(htLastMapData.applicationMapData, link.to);
	                    	options.fOnLinkClicked(e, link);
	                    };
	                    options.fOnNodeClicked = function (e, node, unknownKey, searchQuery) {
	                        var originalNode;
	                        if (angular.isDefined(node.unknownNodeGroup) && !unknownKey) { // 그룹 노드에서 특정 row 를 선택 한 경우의 옵션
	                            node.unknownNodeGroup = ServerMapDaoService.getUnknownNodeDataByUnknownNodeGroup(htLastMapData.applicationMapData, node.unknownNodeGroup);
	                        } else {
	                            originalNode = ServerMapDaoService.getNodeDataByKey(htLastMapData.applicationMapData, unknownKey || node.key);
	                        }
	                        if (originalNode) {
	                            node = originalNode;
	                        }
	                        sLastSelection = "node";
	                        htLastNode = node;
	                        if ( typeof retry === "undefined" ) {
								scope.$emit("serverMapDirective.nodeClicked", htLastQuery, node, htLastMergedMapData, searchQuery);
								if (scope.oNavbarVoService && scope.oNavbarVoService.isRealtime()) {
									$rootScope.$broadcast("realtimeChartController.initialize", node.isWas, node.applicationName, node.serviceType, scope.oNavbarVoService.getApplication() + "/" + scope.oNavbarVoService.getReadablePeriod() + "/" + scope.oNavbarVoService.getQueryEndDateTime() + "/" + scope.oNavbarVoService.getCallerRange());
								}
							}
	                        reset();
	                    };
	                    options.fOnNodeDoubleClicked = function(e, node ) {
							e["diagram"].centerRect( node["actualBounds"] );
							e["diagram"].scale *= 2;
	                    };
	                    options.fOnNodeContextClicked = function (e, node) {
							if ( !bUseNodeContextMenu  ) {
								return;
							}
							if ( scope.oNavbarVoService.isRealtime() ) {
								return;
							}
	                        reset();
	                        var originalNode = ServerMapDaoService.getNodeDataByKey(htLastMapData.applicationMapData, node.key);
	                        if (originalNode) {
	                            node = originalNode;
	                        }
	                        htLastNode = node;
	                        if (node.isWas === true) {
								setNodeContextMenuPosition(e.event.layerY, e.event.layerX, node.applicationName, node.category);
	                        }
	                    };
	                    options.fOnLinkClicked = function (e, link) {
							if ( scope.oNavbarVoService.isRealtime() ) {
								return;
							}
	                        var originalLink;
	                        if (angular.isDefined(link.unknownLinkGroup)) {
	                            link.unknownLinkGroup = ServerMapDaoService.getUnknownLinkDataByUnknownLinkGroup(htLastMapData.applicationMapData, link.unknownLinkGroup);
	                        } else {
	                            originalLink = ServerMapDaoService.getLinkDataByKey(htLastMapData.applicationMapData, link.key);
	                        }
	                        if (originalLink) {
	                            originalLink.fromNode = link.fromNode;
	                            originalLink.toNode = link.toNode;
	                            link = originalLink;
	                        }
	                        sLastSelection = "link";
	                        htLastLink = link;
	                        reset();
	                        scope.$emit("serverMapDirective.linkClicked", htLastQuery, link, htLastMergedMapData);
	                    };
	                    options.fOnLinkContextClicked = function (e, link) {
	                        var originalLink = ServerMapDaoService.getLinkDataByKey(htLastMapData.applicationMapData, link.key);
	                        if (originalLink) {
	                            originalLink.fromNode = link.fromNode;
	                            originalLink.toNode = link.toNode;
	                            link = originalLink;
	                        }
	                        reset();
	                        htLastLink = link;
	
	                        if (!bUseLinkContextMenu || angular.isArray(link.targetInfo)) {
	                            return;
	                        }
	
	                        setLinkContextMenuPosition(e.event.layerY, e.event.layerX);
	                        scope.$emit("serverMapDirective.linkContextClicked", e, query, link, applicationMapData);
	                    };
	                    options.fOnBackgroundClicked = function (e) {
	                        scope.$emit("serverMapDirective.backgroundClicked", e, query);
	                        reset();
	                    };
	                    options.fOnBackgroundDoubleClicked = function (e) {
	                        zoomToFit();
	                    };
	                    options.fOnBackgroundContextClicked = function (e) {
	                        scope.$emit("serverMapDirective.backgroundContextClicked", e, query);
	                        reset();
	                        if (!bUseBackgroundContextMenu) {
	                            return;
	                        }
	                        setBackgroundContextMenuPosition(e.diagram.lastInput.event.layerY, e.diagram.lastInput.event.layerX);
	                    };

						if( retry === true ) {

						} else {

						}
	                    var selectedNode;
	                    for( var i = 0 ; i < htLastMergedMapData.nodeDataArray.length ; i++ ) {
	                    	var node = htLastMergedMapData.nodeDataArray[i];
							if ( node.applicationName === query.applicationName ) {
								selectedNode = node;
								options.sBoldKey = node.key;
								break;
							}
						}

	                    oProgressBarService.setLoading(100);
	                    if (oServerMap === null) {
	                        oServerMap = new ServerMap(options, showOverview(), function( eventName ) {
								AnalyticsService.send( AnalyticsService.CONST.MAIN, AnalyticsService.CONST[eventName] );
							});
	                    } else {
	                        oServerMap.option(options);
	                    }
	                    oServerMap.load(htLastMergedMapData, scope.mergeStatus);
	                    oProgressBarService.stopLoading();

						if ( scope.oNavbarVoService && scope.oNavbarVoService.isRealtime() ) {
							sLastSelection = "node";
							htLastNode = null;
						}
	                    updateLastSelection(selectedNode);
						reloadRealtimeServerMap( query );
	                }
	                function reloadRealtimeServerMap() {

	                	var reloadRequestRepeatingTime = 5000;
	                	var reloadRequestTimeRange = 300000;
						if ( SystemConfigService.get("enableServerMapRealTime") === true && scope.oNavbarVoService.isRealtime() ) {
							$timeout(function() {
								if ( scope.oNavbarVoService.isRealtime() ) {
									htLastQuery.to = htLastQuery.to + reloadRequestRepeatingTime;
									htLastQuery.from = htLastQuery.from - reloadRequestTimeRange;
									ServerMapDaoService.getServerMapData(htLastQuery, function (err, query, mapData) {
										if ( scope.oNavbarVoService.isRealtime() ) {
											htLastMapData = mapData;
											var serverMapData = ServerMapDaoService.extractDataFromApplicationMapData(mapData.applicationMapData);
											extractMergeTypeList(serverMapData);
											serverMapCallback(query, serverMapData, scope.linkRouting, scope.linkCurve, true);
										}
									});
								}
							}, reloadRequestRepeatingTime);
						}
					}
	                function showOverview() {
	                	return /^\/main/.test( $location.path() );
					}
					function setLinkOption(linkDataArray, linkRouting, linkCurve) {
	                    var links = linkDataArray;
	                    for(var k in links) {
	                        if (links[k].from === links[k].to) {
	                            links[k].routing = scope.routingType[2];
	                        } else {
	                            links[k].routing = linkRouting;
	                        }
	                        links[k].curve = linkCurve;
	                    }
	                    return links;
	                }
	                function openFilterWizard() {
	                    reset();
	                    var oSidebarTitleVoService = new SidebarTitleVoService();

	                    // if (htLastLink.fromNode.serviceType === "USER") {
	                    //     oSidebarTitleVoService.setImageType("USER").setTitle("USER");
	                    // } else {
	                        oSidebarTitleVoService.setImageType(htLastLink.fromNode.serviceType).setTitle(htLastLink.fromNode.applicationName);
	                    // }
	                    oSidebarTitleVoService.setImageType2(htLastLink.toNode.serviceType).setTitle2(htLastLink.toNode.applicationName);

						scope.fromAgent = htLastLink.fromAgent || [];
	                    scope.toAgent = htLastLink.toAgent || [];
	                    scope.sourceInfo = htLastLink.sourceInfo;
	                    scope.targetInfo = htLastLink.targetInfo;
	                    scope.fromApplicationName = htLastLink.fromNode.applicationName;
	                    scope.toApplicationName = htLastLink.toNode.applicationName;

	                    scope.$broadcast('sidebarTitleDirective.initialize.forServerMap', oSidebarTitleVoService, htLastLink);

	                    $("#filterWizard").modal('show');
	                    if (!bIsFilterWizardLoaded) {
	                        bIsFilterWizardLoaded = true;
	                        $("#filterWizard").on("shown.bs.modal", function () {
								$fromAgentName.select2().val("").trigger("change");
								$toAgentName.select2().val("").trigger("change");
								$("slider", this).addClass("auto");
								setTimeout(function () {
									$("#filterWizard slider").removeClass("auto");
								}, 500);
								if (scope.oNavbarVoService.getFilter()) {
									var result = filteredMapUtilService.findFilterInNavbarVo(
										htLastLink.fromNode.applicationName,
										htLastLink.fromNode.serviceType,
										htLastLink.toNode.applicationName,
										htLastLink.toNode.serviceType,
										scope.oNavbarVoService);
									if (result) {
										$urlPattern.val(result.oServerMapFilterVoService.getRequestUrlPattern());
										scope.responseTime.from = result.oServerMapFilterVoService.getResponseFrom();
										var to = result.oServerMapFilterVoService.getResponseTo();
										scope.responseTime.to = to === 'max' ? 30000 : to;
										scope.includeFailed = result.oServerMapFilterVoService.getIncludeException();
										$fromAgentName.select2().val(result.oServerMapFilterVoService.getFromAgentName()).trigger("change");
										$toAgentName.select2().val(result.oServerMapFilterVoService.getToAgentName()).trigger("change");
									} else {
										scope.responseTime = {
											from: 0,
											to: 30000
										};
									}
								} else {
									scope.responseTime = {
										from: 0,
										to: 30000
									};
								}
								if (!scope.$$phase) {
									scope.$digest();
								}
							});
	                    }
	                }
					function zoomToFit() {
						if (oServerMap) {
							oServerMap.zoomToFit();
						}
					}
					function updateLastSelection(selectedNode) {
						$timeout(function () {
							if (sLastSelection === "node" && htLastNode) {
								oServerMap.highlightNodeByKey(htLastNode.key);
							} else if (sLastSelection === "link" && htLastLink) {
								oServerMap.highlightLinkByFromTo(htLastLink.from, htLastLink.to);
							} else if (selectedNode) {
								oServerMap.highlightNodeByKey(selectedNode.key);
							}
						});
					}
	                scope.passingTransactionResponseToScatterChart = function () {
	                    scope.$emit("serverMapDirective.passingTransactionResponseToScatterChart", htLastNode);
	                    reset();
	                };
	                scope.passingTransactionList = function () {
	                	AnalyticsService.send(AnalyticsService.CONST.CONTEXT, AnalyticsService.CONST.CLK_FILTER_TRANSACTION);
	                    var oServerMapFilterVoService = new ServerMapFilterVoService();
	                    oServerMapFilterVoService
	                        .setMainApplication(htLastLink.filterApplicationName)
	                        .setMainServiceTypeName(htLastLink.filterApplicationServiceTypeName)
	                        .setMainServiceTypeCode(htLastLink.filterApplicationServiceTypeCode)
	                        .setFromApplication(htLastLink.fromNode.applicationName)
	                        .setFromServiceType(htLastLink.fromNode.serviceType)
	                        .setToApplication(htLastLink.toNode.applicationName)
	                        .setToServiceType(htLastLink.toNode.serviceType);
	
	                    var oServerMapHintVoService = new ServerMapHintVoService();
	                    if (htLastLink.sourceInfo.isWas && htLastLink.targetInfo.isWas) {
	                        oServerMapHintVoService.setHint(htLastLink.toNode.applicationName, htLastLink.filterTargetRpcList);
	                    }
	                    scope.$broadcast('serverMapDirective.openFilteredMap', oServerMapFilterVoService, oServerMapHintVoService);
	                    reset();
	                };
	                scope.openFilterWizard = function () {
	                	AnalyticsService.send(AnalyticsService.CONST.CONTEXT, AnalyticsService.CONST.CLK_FILTER_TRANSACTION_WIZARD);
	                    openFilterWizard();
	                };
	                scope.responseTimeFormatting = function (value) {
	                    if ( parseInt( value ) === 30000 ) {
	                        return "30,000+ ms";
	                    } else {
	                        return $filter("number")(value) + " ms";
	                    }
	                };
	                scope.passingTransactionMap = function () {
	                    var oServerMapFilterVoService = new ServerMapFilterVoService();
	                    oServerMapFilterVoService
	                        .setMainApplication(htLastLink.filterApplicationName)
	                        .setMainServiceTypeCode(htLastLink.filterApplicationServiceTypeCode)
	                        .setMainServiceTypeName(htLastLink.filterApplicationServiceTypeName)
	                        .setFromApplication(htLastLink.fromNode.applicationName)
	                        .setFromServiceType(htLastLink.fromNode.serviceType)
	                        .setToApplication(htLastLink.toNode.applicationName)
	                        .setToServiceType(htLastLink.toNode.serviceType)
	                        .setResponseFrom(scope.responseTime.from)
	                        .setResponseTo(scope.responseTime.to)
	                        .setIncludeException(scope.includeFailed)
	                        .setRequestUrlPattern($base64.encode($urlPattern.val()));
	
	                    if (scope.fromAgentName) {
	                        oServerMapFilterVoService.setFromAgentName(scope.fromAgentName);
	                    }
	                    if (scope.toAgentName) {
	                        oServerMapFilterVoService.setToAgentName(scope.toAgentName);
	                    }
	
	                    var oServerMapHintVoService = new ServerMapHintVoService();
	                    if (htLastLink.sourceInfo.isWas && htLastLink.targetInfo.isWas) {
	                        oServerMapHintVoService.setHint(htLastLink.toNode.applicationName, htLastLink.filterTargetRpcList);
	                    }
	                    scope.$broadcast("serverMapDirective.openFilteredMap", oServerMapFilterVoService, oServerMapHintVoService);
	                    reset();
	                };
	                scope.toggleMergeGroup = function ( mergeType ) {
	                	AnalyticsService.send(AnalyticsService.CONST.CONTEXT, AnalyticsService.CONST.TG_MERGE_TYPE, mergeType);
	                	scope.mergeStatus[ mergeType ] = !scope.mergeStatus[ mergeType ];
	                    serverMapCallback(htLastQuery, ServerMapDaoService.extractDataFromApplicationMapData(htLastMapData.applicationMapData), scope.linkRouting, scope.linkCurve);
	                    reset();
	                };
	                scope.toggleLinkLabelTextType = function (type) {
	                	if ( type === "tps" ) {
	                		AnalyticsService.send(AnalyticsService.CONST.CONTEXT, AnalyticsService.CONST.TG_TPS);
	                		scope.totalRequestCount = false;
	                        scope.tps = true;
	                	} else {
	                		AnalyticsService.send(AnalyticsService.CONST.CONTEXT, AnalyticsService.CONST.TG_CALL_COUNT);
	                		scope.totalRequestCount = true;
	                        scope.tps = false;
	                	}
	                	
	                    scope.totalRequestCount = (type !== "tps") ? true : false;
	                    scope.tps = (type === "tps") ? true : false;
	                    serverMapCallback(htLastQuery, htLastMergedMapData, scope.linkRouting, scope.linkCurve);
	                    reset();
	                };
	                scope.toggleLinkRouting = function (type) {
	                	AnalyticsService.send(AnalyticsService.CONST.CONTEXT, AnalyticsService.CONST.TG_ROUTING, type);
	                    scope.linkRouting = cfg.options.htLinkType.sRouting = type;
	                    serverMapCallback(htLastQuery, htLastMergedMapData, scope.linkRouting, scope.linkCurve);
	                    reset();
	                };
	                scope.toggleLinkCurve = function (type) {
	                	AnalyticsService.send(AnalyticsService.CONST.CONTEXT, AnalyticsService.CONST.TG_CURVE, type);
	                    scope.linkCurve = cfg.options.htLinkType.sCurve = type;
	                    serverMapCallback(htLastQuery, htLastMergedMapData, scope.linkRouting, scope.linkCurve);
	                    reset();
	                };
	                scope.refresh = function () {
	                	AnalyticsService.send(AnalyticsService.CONST.CONTEXT, AnalyticsService.CONST.CLK_REFRESH);
	                    if (oServerMap) {
	                        oServerMap.refresh();
	                    }
	                    reset();
	                };
	                scope.$on("serverMapDirective.initialize", function (event, navbarVoService) {
	                    if (scope.oNavbarVoService && htLastQuery.applicationName !== navbarVoService.getApplicationName()) {
	                        sLastSelection = false;
	                    }
	                    scope.oNavbarVoService = navbarVoService;
						scope.bShowServerMapStatus = !navbarVoService.isRealtime();
	                    // if ( scope.oNavbarVoService.getQueryEndTime() === false || scope.oNavbarVoService.getQueryStartTime() === false ) {
	                    // 	scope.bShowServerMapStatus = false;
	                    // } else {
	                    // 	scope.bShowServerMapStatus = true;
	                    // }
						ServerMapDaoService.abort();
	                    showServerMap(navbarVoService.getApplicationName(), navbarVoService.getServiceTypeName(), navbarVoService.getQueryEndTime(), navbarVoService.getQueryPeriod(), navbarVoService.getFilter(), navbarVoService.getHint(), scope.linkRouting, scope.linkCurve);
	                });
	                scope.$on("serverMapDirective.fetch", function (event, queryPeriod, queryEndTime) {
	                    showServerMap(scope.oNavbarVoService.getApplicationName(), scope.oNavbarVoService.getServiceTypeName(), queryEndTime, queryPeriod, scope.oNavbarVoService.getFilter(), scope.oNavbarVoService.getHint(), scope.linkRouting, scope.linkCurve);
	                });
	                scope.$on("serverMapDirective.initializeWithMapData", function (event, bAllowNodeContextClick, mapData, navbarVoService) {
	                    reset();
	                    scope.bShowServerMapStatus = false;
	                    bUseBackgroundContextMenu = true;
						bUseNodeContextMenu = bAllowNodeContextClick;
	                    bUseLinkContextMenu = false;
	                    htLastQuery = {
	                        applicationName: mapData.applicationId
	                    };
	                    htLastMapData = mapData;
						scope.oNavbarVoService = navbarVoService;
	                    serverMapCallback(htLastQuery, ServerMapDaoService.extractDataFromApplicationMapData(htLastMapData.applicationMapData), scope.linkRouting, scope.linkCurve);
	                });
	                scope.$on("serverMapDirective.zoomToFit", function (event) {
	                    zoomToFit();
	                });
	                scope.$on("serverMapDirective.openFilterWizard", function (event, link) {
	                	if ( link ) {
							htLastLink = link;
						}
	                    openFilterWizard();
	                });
	                scope.searchNodeByEnter = function( $event ) {
	                	if ( $event.keyCode == 13 ) {
	                		scope.searchNode();
	                	}                	
	                };
	                scope.searchNodeWithCategory = function( index ) {
	                	if (oServerMap) {
	                		scope.searchNodeIndex = index;
	                        oServerMap.searchNode( scope.searchNodeList[index].applicationName, scope.searchNodeList[index].serviceType );
	                    }
	                };
	                scope.searchNode = function() {
	                	if (oServerMap && scope.searchNodeQuery !== "" ) {
	                		AnalyticsService.send(AnalyticsService.CONST.MAIN, AnalyticsService.CONST.CLK_SEARCH_NODE);
	                		scope.searchNodeIndex = 0;
	                        scope.searchNodeList = oServerMap.searchNode( scope.searchNodeQuery );
	                        jQuery(element).find(".search-result").show();//.find(".count").html("Result : " + scope.searchNodeList.length);
	                    }
	                };
	                scope.clearSearchNode = function() {
	                	AnalyticsService.send(AnalyticsService.CONST.MAIN, AnalyticsService.CONST.CLK_CLEAR_SEARCH);
	                	oServerMap.clearQuery();
	                	scope.searchNodeIndex = 0;
	                	scope.searchNodeQuery = "";
	                	scope.searchNodeList = [];
	                	jQuery(element).find(".search-result").hide();
	                };
	                scope.toggleShowAntStyleHint = function() {
	                	scope.showAntStyleHint = !scope.showAntStyleHint; 
	                };
	                scope.moveToPast = function() {
	                	scope.$emit("navbarDirective.moveToPast");
	                	$serverMapTime.effect("highlight", { color: "#FFFF00" }, 1000);
	                };
	                scope.moveToFuture = function() {
	                	scope.$emit("navbarDirective.moveToFuture");
	                	$serverMapTime.effect("highlight", { color: "#FFFF00" }, 1000);
	                };
	                scope.toggleToolbar = function() {
	                	var $toolbar = jQuery(element).find(".servermap-toolbar");
	                	var $toolbarHandleSpan = jQuery(element).find(".servermap-toolbar-handle span");
	                	
	                	if ( parseInt($toolbar.css("top")) == -1 ) {
	                		$toolbar.find(".search-result").hide();
	                		$toolbar.animate({ top: -55 }, "fast", function() {
	                			$toolbarHandleSpan.addClass("glyphicon-chevron-down").removeClass("glyphicon-chevron-up");
	                		});
	                	} else {
	                		$toolbar.animate({ top: -1 }, "fast", function() {
	                			if ( scope.searchNodeList.length > 0 ) {
	                				$toolbar.find(".search-result").show();
	                			}
	                			$toolbarHandleSpan.addClass("glyphicon-chevron-up").removeClass("glyphicon-chevron-down");
	                		});
	                	}
	                };
					TooltipService.init( "serverMap" );
	            }
	        };
	    }
	]);
})();