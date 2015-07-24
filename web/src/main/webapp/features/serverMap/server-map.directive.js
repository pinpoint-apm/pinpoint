(function() {
	'use strict';
	/**
	 * (en)serverMapDirective 
	 * @ko serverMapDirective
	 * @group Directive
	 * @name serverMapDirective
	 * @class
	 */
	pinpointApp.constant('serverMapDirectiveConfig', {
	    options: {
	        "sContainerId": 'servermap',
	        "sOverviewId": 'servermapOverview',
	        "sBigFont": "11pt Lato,NanumGothic,ng,dotum,AppleGothic,sans-serif",
	        "sSmallFont": "11pt Lato,NanumGothic,ng,dotum,AppleGothic,sans-serif",
	        "sImageDir": '/images/servermap/',
	        "htLinkType": {
	            "sRouting": "Normal", // Normal, Orthogonal, AvoidNodes
	            "sCurve": "JumpGap" // Bezier, JumpOver, JumpGap
	        },
	        "htLinkTheme": {
	            "default": {
	                "backgroundColor": "#ffffff",
	                "borderColor": "#c5c5c5",
	                "fontFamily": "11pt Lato,NanumGothic,ng,dotum,AppleGothic,sans-serif",
	                "fontColor": "#000000",
	                "fontAlign": "center",
	                "margin": 1,
	                "strokeWidth": 1
	            },
	            "bad": {
	                "backgroundColor": "#ffc9c9",
	                "borderColor": "#7d7d7d",
	                "fontFamily": "11pt Lato,NanumGothic,ng,dotum,AppleGothic,sans-serif",
	                "fontColor": "#FF1300",
	                "fontAlign": "center",
	                "margin": 1,
	                "strokeWidth": 1
	            }
	        }
	    }
	});
	
	pinpointApp.directive('serverMapDirective', [ 'serverMapDirectiveConfig', 'ServerMapDaoService', 'AlertsService', 'ProgressBarService', 'SidebarTitleVoService', '$filter', 'ServerMapFilterVoService', 'filteredMapUtilService', '$base64', 'ServerMapHintVoService', '$timeout', '$location', '$window', 'helpContentTemplate', 'helpContentService',
	    function (cfg, ServerMapDaoService, AlertsService, ProgressBarService, SidebarTitleVoService, $filter, ServerMapFilterVoService, filteredMapUtilService, $base64, ServerMapHintVoService, $timeout, $location, $window, helpContentTemplate, helpContentService) {
	        return {
	            restrict: 'EA',
	            replace: true,
	            templateUrl: 'features/serverMap/serverMap.html',
	            link: function postLink(scope, element, attrs) {
	
	                // define private variables
	                var bUseNodeContextMenu, bUseLinkContextMenu, htLastQuery,
	                    bUseBackgroundContextMenu, oServerMap, oAlertService, oProgressBarService, htLastMapData, htLastLink, htLastNode,
	                    sLastSelection, $fromAgentName, $toAgentName, bIsFilterWizardLoaded, htLastMergedMapData, $serverMapTime;
	
	                // define private variables of methods
	                var showServerMap, setNodeContextMenuPosition, reset, emitDataExisting,
	                    setLinkContextMenuPosition, setBackgroundContextMenuPosition, serverMapCallback, setLinkOption,
	                    zoomToFit, updateLastSelection, openFilterWizard, searchNode, getMergeArray, extractMergeTypeList;
	
	
	                // bootstrap
	                oAlertService = new AlertsService(element);
	                oProgressBarService = new ProgressBarService(element);
	                sLastSelection = false;
	                oServerMap = null;
	                htLastMapData = {
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
	                htLastQuery = {};
	                htLastLink = {};
	                htLastNode = {};
	                scope.oNavbarVoService = null;
	                //scope.mergeUnknowns = true;
	                scope.totalRequestCount = true;
	                scope.bShowServerMapStatus = false;
	                scope.linkRouting = cfg.options.htLinkType.sRouting;
	                scope.linkCurve = cfg.options.htLinkType.sCurve;
	                scope.searchNodeQuery = "";
	                scope.searchNodeIndex = 0;
	                scope.searchNodeList = [];
	                $serverMapTime = element.find(".serverMapTime");
	                $fromAgentName = element.find('.fromAgentName');
	                $toAgentName = element.find('.toAgentName');
	                $fromAgentName.select2();
	                $toAgentName.select2();
	                bIsFilterWizardLoaded = false;
	                scope.mergeTypeList = [];
	                scope.mergeStatus = {};
	                scope.showAntStyleHint = false;
	                
	                /**
	                 * extract node serviceType info
	                 * @param serverMapData
	                 */
	                extractMergeTypeList = function( serverMapData ) {
	                	serverMapData.nodeDataArray.forEach( function( o ) {
	                		if ( o.isWas == false && o.serviceType !== "USER" ) {
	                			if ( angular.isUndefined( scope.mergeStatus[o.serviceType] ) ) {
		                			scope.mergeTypeList.push( o.serviceType );
		                			scope.mergeStatus[o.serviceType] = true;
	                			}
	                		}
	                	});
	                };
	                /**
	                 * reset
	                 */
	                reset = function () {
	                    scope.nodeContextMenuStyle = '';
	                    scope.linkContextMenuStyle = '';
	                    scope.backgroundContextMenuStyle = '';
	                    scope.urlPattern = '';
	                    scope.responseTime = {
	                        from: 0,
	                        to: 30000
	                    };
	                    scope.includeFailed = null;
	                    $('#filterWizard').modal('hide');
	
	                    if (!(scope.$$phase == '$apply' || scope.$$phase == '$digest') ) {
	                    	if (!(scope.$root.$$phase == '$apply' || scope.$root.$$phase == '$digest') ) {
	                    		scope.$digest();
	                    	}
	                    }
	                };
	
	                /**
	                 * show server map
	                 * @param applicationName
	                 * @param serviceTypeCode
	                 * @param to
	                 * @param period
	                 * @param filterText
	                 * @parma hintText
	                 * @param linkRouting
	                 * @param linkCurve
	                 */
	                showServerMap = function (applicationName, serviceTypeName, to, period, filterText, hintText, linkRouting, linkCurve) {
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
	                };
	
	                /**
	                 * emit data existing
	                 * @param mapData
	                 */
	                emitDataExisting = function (mapData) {
	                    if (mapData.applicationMapData.nodeDataArray.length === 0 || mapData.applicationMapData.linkDataArray.length === 0) {
	                        scope.$emit('serverMapDirective.hasNoData');
	                    } else {
	                        scope.$emit('serverMapDirective.hasData');
	                    }
	                };
	
	                /**
	                 * set node context menu position
	                 * @param top
	                 * @param left
	                 */
	                setNodeContextMenuPosition = function (top, left) {
	                    scope.nodeContextMenuStyle = {
	                        display: 'block',
	                        'top': top,
	                        'left': left,
	                        'z-index': 9999999 // it should be higher than 9999998, because of intro.js
	                    };
	                    scope.$digest();
	                };
	
	                /**
	                 * set link context menu position
	                 * @param top
	                 * @param left
	                 */
	                setLinkContextMenuPosition = function (top, left) {
	                    scope.linkContextMenuStyle = {
	                        display: 'block',
	                        'top': top,
	                        'left': left,
	                        'z-index': 9999999 // it should be higher than 9999998, because of intro.js
	                    };
	                    scope.$digest();
	                };
	
	                /**
	                 * set background context menu position
	                 * @param top
	                 * @param left
	                 */
	                setBackgroundContextMenuPosition = function (top, left) {
	                    scope.backgroundContextMenuStyle = {
	                        display: 'block',
	                        'top': top,
	                        'left': left,
	                        'z-index': 9999999 // it should be higher than 9999998, because of intro.js
	                    };
	                    scope.$digest();
	                };
	                getMergeArray = function() {
	                	var a = [];
	                	for( var key in scope.mergeStatus ) {
	                		if ( scope.mergeStatus[key] === true ) {
	                			a.push( key );
	                		}
	                	}
	                	return a;
	                };
	
	                /**
	                 * server map callback
	                 * @param query
	                 * @param applicationMapData
	                 * @param linkRouting
	                 * @param linkCurve
	                 */
	                serverMapCallback = function (query, applicationMapData, linkRouting, linkCurve) {
	//                	console.log( applicationMapData );
	                	var mergeArray = getMergeArray();
	                	//htLastMergedMapData
	                	htLastMergedMapData = ServerMapDaoService.mergeMultiLinkGroup( ServerMapDaoService.mergeGroup(applicationMapData, mergeArray), mergeArray );
	
	//                    console.log( htLastMergedMapData );
	//                    ServerMapDaoService.removeNoneNecessaryDataForHighPerformance(htLastMergedMapData);
	                    oProgressBarService.setLoading(80);
	                    if (htLastMergedMapData.nodeDataArray.length === 0) {
	                        oProgressBarService.stopLoading();
	                        if (scope.oNavbarVoService.getFilter()) {
	                            var aFilter = scope.oNavbarVoService.getFilterAsJson();
	                            var aFilterInfo = [];
	                            aFilterInfo.push('<p>There is no data with the filter below.</p>');
	
	                            angular.forEach(aFilter, function (f, idx) {
	                                aFilterInfo.push('<p><b>Filter');
	                                if (aFilter.length > 1) {
	                                    aFilterInfo.push(' #'+(idx+1)+'');
	                                }
	                                aFilterInfo.push(' : ' + f.fa + '(' + f.fst + ') ~ ' + f.ta + '(' + f.tst + ')' + '</b><br>');
	                                aFilterInfo.push('<ul>');
	                                if (f.url) {
	                                    aFilterInfo.push('<li>Url Pattern : ' + $base64.decode(f.url) + '</li>');
	                                }
	                                if (f.rf && f.rt) {
	                                    aFilterInfo.push('<li>Response Time : ' + $filter('number')(f.rf) + ' ms ~ ' + $filter('number')(f.rt) + ' ms</li>');
	                                }
	                                aFilterInfo.push('<li>Transaction Result : ' + (f.ie ? 'Failed Only' : 'Success + Failed') + '</li>');
	                                aFilterInfo.push('</ul></p>');
	                            });
	                            oAlertService.showInfo(aFilterInfo.join(''));
	                        } else {
	                        	oAlertService.showInfo('There is no data.');
	                        }
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
	                    }
	                    options.fOnNodeClicked = function (e, node, unknownKey, searchQuery) {
	                        var originalNode;
	                        if (angular.isDefined(node.unknownNodeGroup) && !unknownKey) {
	                            node.unknownNodeGroup = ServerMapDaoService.getUnknownNodeDataByUnknownNodeGroup(htLastMapData.applicationMapData, node.unknownNodeGroup);
	                        } else {
	                            originalNode = ServerMapDaoService.getNodeDataByKey(htLastMapData.applicationMapData, unknownKey || node.key);
	                        }
	                        if (originalNode) {
	                            node = originalNode;
	                        }
	                        sLastSelection = 'node';
	                        htLastNode = node;
	                        scope.$emit("serverMapDirective.nodeClicked", e, htLastQuery, node, htLastMergedMapData, searchQuery);
	                        reset();
	                    };
	                    options.fOnNodeDoubleClicked = function(e, node, htData ) {
	                    	e.diagram.zoomToRect( node.actualBounds, 1.2);
	                    };
	                    options.fOnNodeContextClicked = function (e, node) {
	                        reset();
	                        var originalNode = ServerMapDaoService.getNodeDataByKey(htLastMapData.applicationMapData, node.key);
	                        if (originalNode) {
	                            node = originalNode;
	                        }
	                        htLastNode = node;
//	                        if (!bUseNodeContextMenu) {
//	                            return;
//	                        }
	                        if (node.isWas === true) {
	                            setNodeContextMenuPosition(e.event.layerY, e.event.layerX);
	                        }
	                        scope.$emit("serverMapDirective.nodeContextClicked", e, query, node, applicationMapData);
	                    };
	                    options.fOnLinkClicked = function (e, link) {
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
	                        sLastSelection = 'link';
	                        htLastLink = link;
	                        reset();
	                        scope.$emit("serverMapDirective.linkClicked", e, htLastQuery, link, htLastMergedMapData);
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
	
	                    var selectedNode;
	                    try {
	                        selectedNode = _.find(htLastMergedMapData.nodeDataArray, function (node) {
	                            if (node.applicationName === query.applicationName && angular.isUndefined(query.serviceType)) {
	                                return true;
	                            } else if (node.applicationName === query.applicationName && node.serviceTypeCode === query.serviceTypeCode) {
	                                return true;
	                            } else {
	                                return false;
	                            }
	                        });
	                        if (selectedNode) {
	                            options.sBoldKey = selectedNode.key;
	                        }
	                    } catch (e) {
	                    	oAlertService.showError('There is some error while selecting a node.');
	                        console.log(e);
	                    }
	
	                    oProgressBarService.setLoading(100);
	                    if (oServerMap === null) {
	                        oServerMap = new ServerMap(options, $location);
	                    } else {
	                        oServerMap.option(options);
	                    }
	                    oServerMap.load(htLastMergedMapData);
	                    oProgressBarService.stopLoading();
	
	                    updateLastSelection(selectedNode);
	                };
	
	                /**
	                 * set link option
	                 * @param linkDataArray
	                 * @param linkRouting
	                 * @param linkCurve
	                 */
	                setLinkOption = function (linkDataArray, linkRouting, linkCurve) {
	                    var links = linkDataArray;
	                    for(var k in links) {
	                        if (links[k].from === links[k].to) {
	                            links[k].routing = "AvoidsNodes";
	                        } else {
	                            links[k].routing = linkRouting;
	                        }
	                        links[k].curve = linkCurve;
	                    }
	                    return links;
	                };
	
	                /**
	                 * open filter wizard
	                 */
	                openFilterWizard = function () {
	                    reset();
	                    var oSidebarTitleVoService = new SidebarTitleVoService;
	
	                    if (htLastLink.fromNode.serviceType === 'USER') {
	                        oSidebarTitleVoService
	                            .setImageType('USER')
	                            .setTitle('USER')
	                    } else {
	                        oSidebarTitleVoService
	                            .setImageType(htLastLink.fromNode.serviceType)
	                            .setTitle(htLastLink.fromNode.applicationName)
	                    }
	                    oSidebarTitleVoService
	                        .setImageType2(htLastLink.toNode.serviceType)
	                        .setTitle2(htLastLink.toNode.applicationName);
	
	                    scope.sourceInfo = htLastLink.sourceInfo;
	                    scope.sourceHistogram = htLastLink.sourceHistogram;
	                    scope.targetInfo = htLastLink.targetInfo;
	                    //scope.targetHistogram = htLastLink.toNode.agentHistogram;
	                    scope.targetHistogram = htLastLink.targetHistogram;
	                    scope.fromApplicationName = htLastLink.fromNode.applicationName;
	                    scope.toApplicationName = htLastLink.toNode.applicationName;
	                    $fromAgentName.select2('val', '');
	                    $toAgentName.select2('val', '');
	
	                    scope.$broadcast('sidebarTitleDirective.initialize.forServerMap', oSidebarTitleVoService);
	
	                    $('#filterWizard').modal('show');
	                    if (!bIsFilterWizardLoaded) {
	                        bIsFilterWizardLoaded = true;
	                        $('#filterWizard')
	                            .on('shown.bs.modal', function () {
	//                                scope.$broadcast('loadChartDirective.initAndSimpleRenderWithData.forFilterWizard', htLastLink.timeSeriesHistogram, '100%', '150px');
	                                $('slider', this).addClass('auto');
	                                setTimeout(function () {
	                                    $('#filterWizard slider').removeClass('auto');
	                                }, 500);
	                                if (scope.oNavbarVoService.getFilter()) {
	                                    var result = filteredMapUtilService.findFilterInNavbarVo(
	                                        htLastLink.fromNode.applicationName,
	                                        htLastLink.fromNode.serviceType,
	                                        htLastLink.toNode.applicationName,
	                                        htLastLink.toNode.serviceType,
	                                        scope.oNavbarVoService);
	                                    if (result) {
	                                        scope.urlPattern = result.oServerMapFilterVoService.getRequestUrlPattern();
	                                        scope.responseTime.from = result.oServerMapFilterVoService.getResponseFrom();
	                                        var to = result.oServerMapFilterVoService.getResponseTo();
	                                        scope.responseTime.to = to === 'max' ? 30000 : to;
	                                        scope.includeFailed = result.oServerMapFilterVoService.getIncludeException();
	                                        $fromAgentName.select2('val', result.oServerMapFilterVoService.getFromAgentName());
	                                        $toAgentName.select2('val', result.oServerMapFilterVoService.getToAgentName());
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
	                };
	
	                /**
	                 * scope passing transaction response to scatter chart
	                 */
	                scope.passingTransactionResponseToScatterChart = function () {
	                    scope.$emit('serverMapDirective.passingTransactionResponseToScatterChart', htLastNode);
	                    reset();
	                };
	
	                /**
	                 * passing transaction list
	                 */
	                scope.passingTransactionList = function () {
	                	$at($at.CONTEXT, $at.CLK_FILTER_TRANSACTION);
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
	                        oServerMapHintVoService.setHint(htLastLink.toNode.applicationName, htLastLink.filterTargetRpcList)
	                    }
	
	                    scope.$broadcast('serverMapDirective.openFilteredMap', oServerMapFilterVoService, oServerMapHintVoService);
	                    reset();
	                };
	
	                /**
	                 * open filter wizard
	                 */
	                scope.openFilterWizard = function () {
	                	$at($at.CONTEXT, $at.CLK_FILTER_TRANSACTION_WIZARD);
	                    openFilterWizard();
	                };
	
	                /**
	                 * zoom to fit
	                 */
	                zoomToFit = function () {
	                    if (oServerMap) {
	                        oServerMap.zoomToFit();
	                    }
	                };
	
	                /**
	                 * update last selection
	                 * @param selectedNode
	                 */
	                updateLastSelection = function (selectedNode) {
	                    $timeout(function () {
	                        if (sLastSelection === 'node' && htLastNode) {
	                            oServerMap.highlightNodeByKey(htLastNode.key);
	                        } else if (sLastSelection === 'link' && htLastLink) {
	                            oServerMap.highlightLinkByFromTo(htLastLink.from, htLastLink.to);
	                        } else if (selectedNode) {
	                            oServerMap.highlightNodeByKey(selectedNode.key);
	                        }
	                    });
	                };
	
	                /**
	                 * response time formatting
	                 * @param value
	                 * @returns {string}
	                 */
	                scope.responseTimeFormatting = function (value) {
	                    if (value == 30000) {
	                        return '30,000+ ms';
	                    } else {
	                        return $filter('number')(value) + ' ms';
	                    }
	                };
	
	                /**
	                 * scope passing transaction map
	                 */
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
	                        .setRequestUrlPattern($base64.encode(scope.urlPattern));
	
	                    if (scope.fromAgentName) {
	                        oServerMapFilterVoService.setFromAgentName(scope.fromAgentName);
	                    }
	                    if (scope.toAgentName) {
	                        oServerMapFilterVoService.setToAgentName(scope.toAgentName);
	                    }
	
	                    var oServerMapHintVoService = new ServerMapHintVoService();
	                    if (htLastLink.sourceInfo.isWas && htLastLink.targetInfo.isWas) {
	                        oServerMapHintVoService.setHint(htLastLink.toNode.applicationName, htLastLink.filterTargetRpcList)
	                    }
	                    scope.$broadcast('serverMapDirective.openFilteredMap', oServerMapFilterVoService, oServerMapHintVoService);
	                    reset();
	                };
	                /**
	                 * toggle merge group
	                 */
	                scope.toggleMergeGroup = function ( mergeType ) {
	                	$at($at.CONTEXT, $at.TG_MERGE_TYPE, mergeType);
	                	scope.mergeStatus[ mergeType ] = !scope.mergeStatus[ mergeType ];
	                    //scope.mergeUnknowns = (scope.mergeUnknowns) ? false : true;
	                    serverMapCallback(htLastQuery, ServerMapDaoService.extractDataFromApplicationMapData(htLastMapData.applicationMapData), scope.linkRouting, scope.linkCurve);
	                    reset();
	                };
	
	                /**
	                 * scope toggle link lable text type
	                 * @param type
	                 */
	                scope.toggleLinkLableTextType = function (type) {
	                	if ( type === "tps" ) {
	                		$at($at.CONTEXT, $at.TG_TPS);
	                		scope.totalRequestCount = false;
	                        scope.tps = true;
	                	} else {
	                		$at($at.CONTEXT, $at.TG_CALL_COUNT);
	                		scope.totalRequestCount = true;
	                        scope.tps = false;
	                	}
	                	
	                    scope.totalRequestCount = (type !== 'tps') ? true : false;
	                    scope.tps = (type === 'tps') ? true : false;
	                    serverMapCallback(htLastQuery, htLastMergedMapData, scope.linkRouting, scope.linkCurve);
	                    reset();
	                };
	
	                /**
	                 * toggle link routing
	                 * @param type
	                 */
	                scope.toggleLinkRouting = function (type) {
	                	$at($at.CONTEXT, $at.TG_ROUTING, type);
	                    scope.linkRouting = cfg.options.htLinkType.sRouting = type;
	                    serverMapCallback(htLastQuery, htLastMergedMapData, scope.linkRouting, scope.linkCurve);
	                    reset();
	                };
	
	                /**
	                 * toggle link curve
	                 * @param type
	                 */
	                scope.toggleLinkCurve = function (type) {
	                	$at($at.CONTEXT, $at.TG_CURVE, type);
	                    scope.linkCurve = cfg.options.htLinkType.sCurve = type;
	                    serverMapCallback(htLastQuery, htLastMergedMapData, scope.linkRouting, scope.linkCurve);
	                    reset();
	                };
	
	                /**
	                 * refresh
	                 */
	                scope.refresh = function () {
	                	$at($at.CONTEXT, $at.CLK_REFRESH);
	                    if (oServerMap) {
	                    	
	                        oServerMap.refresh();
	                    }
	                    reset();
	                };
	
	                /**
	                 * scope event on serverMapDirective.initialize
	                 */
	                scope.$on('serverMapDirective.initialize', function (event, navbarVoService) {
	                    if (scope.oNavbarVoService && htLastQuery.applicationName !== navbarVoService.getApplicationName()) {
	                        sLastSelection = false;
	                    }
	                    scope.oNavbarVoService = navbarVoService;
	                    if ( scope.oNavbarVoService.getQueryEndTime() === false || scope.oNavbarVoService.getQueryStartTime() === false ) {
	                    	scope.bShowServerMapStatus = false;
	                    } else {
	                    	scope.bShowServerMapStatus = true;
	                    }
	                    bUseLinkContextMenu = bUseBackgroundContextMenu = true;
	                    bUseNodeContextMenu = false;
	                    showServerMap(navbarVoService.getApplicationName(), navbarVoService.getServiceTypeName(), navbarVoService.getQueryEndTime(), navbarVoService.getQueryPeriod(), navbarVoService.getFilter(), navbarVoService.getHint(), scope.linkRouting, scope.linkCurve);
	                });
	
	                /**
	                 * scope event on serverMapDirective.fetch
	                 */
	                scope.$on('serverMapDirective.fetch', function (event, queryPeriod, queryEndTime) {
	                    showServerMap(scope.oNavbarVoService.getApplicationName(), scope.oNavbarVoService.getServiceTypeName(), queryEndTime, queryPeriod, scope.oNavbarVoService.getFilter(), scope.oNavbarVoService.getHint(), scope.linkRouting, scope.linkCurve);
	                });
	
	                /**
	                 * scope event on serverMapDirective.initializeWithMapData
	                 */
	                scope.$on('serverMapDirective.initializeWithMapData', function (event, mapData) {
	                    reset();
	                    scope.bShowServerMapStatus = false;
	                    bUseBackgroundContextMenu = true;
	                    bUseNodeContextMenu = bUseLinkContextMenu = false;
	                    htLastQuery = {
	                        applicationName: mapData.applicationId
	                    };
	                    htLastMapData = mapData;
	                    serverMapCallback(htLastQuery, ServerMapDaoService.extractDataFromApplicationMapData(htLastMapData.applicationMapData), scope.linkRouting, scope.linkCurve);
	                });
	
	                /**
	                 * scope event on serverMapDirective.zoomToFit
	                 */
	                scope.$on('serverMapDirective.zoomToFit', function (event) {
	                    zoomToFit();
	                });
	
	                scope.$on('serverMapDirective.openFilterWizard', function (event, link) {
	                    htLastLink = link;
	                    openFilterWizard();
	                });
	                
	                scope.searchNodeByEnter = function( $event ) {
	                	if ( $event.keyCode == 13 ) {
	                		scope.searchNode();
	                	}                	
	                }
	                scope.searchNodeWithCategory = function( index ) {
	                	if (oServerMap) {
	                		scope.searchNodeIndex = index;
	                        oServerMap.searchNode( scope.searchNodeList[index].applicationName, scope.searchNodeList[index].serviceType );
	                    }
	                }
	                scope.searchNode = function() {
	                	if (oServerMap && scope.searchNodeQuery !== "" ) {
	                		$at($at.MAIN, $at.CLK_SEARCH_NODE);
	                		scope.searchNodeIndex = 0;
	                        scope.searchNodeList = oServerMap.searchNode( scope.searchNodeQuery );
	                        jQuery(element).find(".search-result").show().find(".count").html("Result : " + scope.searchNodeList.length);
	                    }
	                };
	                scope.clearSearchNode = function() {
	                	$at($at.MAIN, $at.CLK_CLEAR_SEARCH);
	                	oServerMap.clearQuery();
	                	scope.searchNodeIndex = 0;
	                	scope.searchNodeQuery = "";
	                	scope.searchNodeList = [];
	                	jQuery(element).find(".search-result").hide();
	                }
	                scope.toggleShowAntStyleHint = function() {
	                	scope.showAntStyleHint = !scope.showAntStyleHint; 
	                };
	                scope.moveThePast = function() {
	                	scope.$emit("navbarDirective.moveThePast");
	                	$serverMapTime.effect("highlight", { color: "#FFFF00" }, 1000);
	                };
	                scope.moveTheFuture = function() {
	                	scope.$emit("navbarDirective.moveTheFuture");
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
	                jQuery('.serverMapTooltip').tooltipster({
                    	content: function() {
                    		return helpContentTemplate(helpContentService.servermap["default"]);
                    	},
                    	position: "bottom-right",
                    	trigger: "click"
                    });
	            }
	        };
	    }
	]);
})();