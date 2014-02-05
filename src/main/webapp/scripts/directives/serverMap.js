'use strict';

pinpointApp.constant('serverMapConfig', {
    options: {
        "sContainerId": 'servermap',
        "sImageDir": '/images/icons/',
        "htIcons": {
            'APACHE': 'APACHE.png',
            'ARCUS': 'ARCUS.png',
            'BLOC': 'BLOC.png',
            'CUBRID': 'CUBRID.png',
            'ETC': 'ETC.png',
            'MEMCACHED': 'MEMCACHED.png',
            'MYSQL': 'MYSQL.png',
            'QUEUE': 'QUEUE.png',
            'TOMCAT': 'TOMCAT.png',
            'UNKNOWN': 'UNKNOWN.png',
            'UNKNOWN_GROUP': 'UNKNOWN.png',
            'USER': 'USER.png',
            'ORACLE': 'ORACLE.png'
        },
        "htLinkType": {
            "sRouting": "Normal", // Normal, Orthogonal, AvoidNodes
            "sCurve": "JumpGap" // Bezier, JumpOver, JumpGap
        }
    }
});

pinpointApp.directive('serverMap', [ 'serverMapConfig', 'ServerMapDao', 'Alerts', 'ProgressBar', 'SidebarTitleVo', '$filter', 'ServerMapFilterVo', 'encodeURIComponentFilter',
    function (cfg, ServerMapDao, Alerts, ProgressBar, SidebarTitleVo, $filter, ServerMapFilterVo, encodeURIComponentFilter) {
        return {
            restrict: 'EA',
            replace: true,
            templateUrl: 'views/serverMap.html',
            link: function postLink(scope, element, attrs) {

                // define private variables
                var serverMapCachedQuery, serverMapCachedData, bUseNodeContextMenu, bUseLinkContextMenu, htLastQuery,
                    bUseBackgroundContextMenu, oServerMap, oAlert, oProgressBar, htLastMapData;

                // define private variables of methods
                var showServerMap, reset, setNodeContextMenuPosition,
                    setLinkContextMenuPosition, setBackgroundContextMenuPosition, serverMapCallback, setLinkOption;

                // initialize
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
                oAlert = new Alerts(element);
                oProgressBar = new ProgressBar(element);
                scope.oNavbar = null;
                scope.mergeUnknowns = true;
                scope.totalRequestCount = true;
                scope.bShowServerMapStatus = false;
                scope.linkRouting = cfg.options.htLinkType.sRouting;
                scope.linkCurve = cfg.options.htLinkType.sCurve;

                /**
                 * show server map
                 * @param applicationName
                 * @param serviceType
                 * @param to
                 * @param period
                 * @param filterText
                 * @param mergeUnknowns
                 * @param linkRouting
                 * @param linkCurve
                 */
                showServerMap = function (applicationName, serviceType, to, period, filterText, mergeUnknowns, linkRouting, linkCurve) {
                    oProgressBar.startLoading();
                    oAlert.hideError();
                    oAlert.hideWarning();
                    oAlert.hideInfo();
                    if (oServerMap) {
                        oServerMap.clear();
                    }
                    oProgressBar.setLoading(10);

                    htLastQuery = {
                        applicationName: applicationName,
                        serviceType: serviceType,
                        from: to - period,
                        to: to,
                        period: period,
                        filter: encodeURIComponentFilter(filterText)
                    };

                    if (filterText) {
                        ServerMapDao.getFilteredServerMapData(htLastQuery, function (err, query, result) {
                            if (err) {
                                oProgressBar.stopLoading();
                                oAlert.showError('There is some error.');
                            }
                            oProgressBar.setLoading(50);
                            if (query.from === result.lastFetchedTimestamp) {
                                scope.$emit('serverMap.allFetched');
                            } else {
                                htLastMapData.lastFetchedTimestamp = result.lastFetchedTimestamp - 1;
                                scope.$emit('serverMap.fetched', htLastMapData.lastFetchedTimestamp, result);
                            }
//                        ServerMapDao.mergeTimeSeriesResponses(result.timeSeriesResponses);
                            var serverMapData = ServerMapDao.addFilterProperty(filterText, ServerMapDao.mergeFilteredMapData(htLastMapData, result));
                            if (mergeUnknowns) {
                                var copiedData = angular.copy(serverMapData);
                                ServerMapDao.mergeUnknown(query, copiedData);
                                serverMapCallback(query, copiedData, linkRouting, linkCurve);
                            } else {
                                serverMapCallback(query, serverMapData, linkRouting, linkCurve);
                            }
                        });
                    } else {
                        ServerMapDao.getServerMapData(htLastQuery, function (err, query, serverMapData) {
                            if (err) {
                               oProgressBar.stopLoading();
                               oAlert.showError('There is some error.');
                            }
                            oProgressBar.setLoading(50);
                            htLastMapData = serverMapData;
                            if (mergeUnknowns) {
                                var copiedData = angular.copy(serverMapData);
                                ServerMapDao.mergeUnknown(query, copiedData);
                                serverMapCallback(query, copiedData, linkRouting, linkCurve);
                            } else {
                                serverMapCallback(query, serverMapData, linkRouting, linkCurve);
                            }

                        });
                    }
                };

                /**
                 * reset
                 */
                reset = function () {
                    scope.nodeContextMenuStyle = '';
                    scope.linkContextMenuStyle = '';
                    scope.backgroundContextMenuStyle = '';
                    scope.filterWizardStyle = '';
                    scope.responseTime = {
                        min: 0,
                        max: 30001
                    };
                    scope.includeFailed = false;
                    $('#filterWizard').modal('hide');
                    if (!scope.$$phase) {
                        scope.$digest();
                    }
                };

                /**
                 * set node context menu position
                 * @param top
                 * @param left
                 */
                setNodeContextMenuPosition = function (top, left) {
                    scope.nodeContextMenuStyle = {
                        display: 'block'
                    };
                    element.find('.nodeContextMenu').css({
                        'top': top,
                        'left': left
                    });
                    scope.$digest();
                };

                /**
                 * set link context menu position
                 * @param top
                 * @param left
                 */
                setLinkContextMenuPosition = function (top, left) {
                    scope.linkContextMenuStyle = {
                        display: 'block'
                    };
                    var linkContextMenu = element.find('.linkContextMenu');
                    linkContextMenu.css({
                        'top': top,
                        'left': left
                    });
                    scope.$digest();
                };

                /**
                 * set background context menu position
                 * @param top
                 * @param left
                 */
                setBackgroundContextMenuPosition = function (top, left) {
                    scope.backgroundContextMenuStyle = {
                        display: 'block'
                    };
                    var backgroundContextMenu = element.find('.backgroundContextMenu');
                    backgroundContextMenu.css({
                        'top': top,
                        'left': left
                    });
                    scope.$digest();
                };

                /**
                 * server map callback
                 * @param query
                 * @param copiedData
                 * @param linkRouting
                 * @param linkCurve
                 */
                serverMapCallback = function (query, copiedData, linkRouting, linkCurve) {
                    serverMapCachedQuery = angular.copy(query);
                    serverMapCachedData = angular.copy(copiedData);
                    oProgressBar.setLoading(80);
                    if (copiedData.applicationMapData.nodeDataArray.length === 0) {
                        oProgressBar.stopLoading();
                        oAlert.showInfo('There is no data.');
                        return;
                    }

                    setLinkOption(copiedData, linkRouting, linkCurve);
                    oProgressBar.setLoading(90);

                    var options = cfg.options;
                    options.fOnNodeContextClicked = function (e, node) {
                        scope.$emit("serverMap.nodeContextClicked", e, query, node, copiedData);
                        reset();
                        scope.node = node;
                        if (!bUseNodeContextMenu) {
                            return;
                        }
                        if (node.isWas === true) {
                            setNodeContextMenuPosition(e.event.layerY, e.event.layerX);
                        }
                    };
                    options.fOnLinkContextClicked = function (e, link) {
                        scope.$emit("serverMap.linkContextClicked", e, query, link, copiedData);
                        reset();
                        scope.link = link;
//                    scope.nodeCategory = link.category || '';
                        scope.srcServiceType = link.sourceinfo.serviceType || '';
                        scope.srcApplicationName = link.sourceinfo.applicationName || '';
                        scope.destServiceType = link.targetinfo.serviceType || '';
                        scope.destApplicationName = link.targetinfo.applicationName || '';

                        if (!bUseLinkContextMenu || angular.isArray(link.targetinfo)) {
                            return;
                        }
                        setLinkContextMenuPosition(e.event.layerY, e.event.layerX);
                    };
                    options.fOnLinkClicked = function (e, link) {
                        scope.$emit("serverMap.linkClicked", e, query, link, copiedData);
                        reset();
                    };
                    options.fOnNodeClicked = function (e, node) {
                        scope.$emit("serverMap.nodeClicked", e, query, node, copiedData);
                        reset();
                    };
                    options.fOnBackgroundClicked = function (e) {
                        scope.$emit("serverMap.backgroundClicked", e, query);
                        reset();
                    };
                    options.fOnBackgroundContextClicked = function (e) {
                        scope.$emit("serverMap.backgroundContextClicked", e, query);
                        reset();
                        if (!bUseBackgroundContextMenu) {
                            return;
                        }
                        setBackgroundContextMenuPosition(e.diagram.lastInput.event.layerY, e.diagram.lastInput.event.layerX);
                    };

                    try {
                        var selectedNode = _.find(copiedData.applicationMapData.nodeDataArray, function (node) {
                            if (node.text === query.applicationName && angular.isUndefined(query.serviceType)) {
                                return true;
                            } else if (node.text === query.applicationName && node.serviceTypeCode === query.serviceType) {
                                return true;
                            } else {
                                return false;
                            }
                        });
                        if (selectedNode) {
                            options.nBoldKey = selectedNode.key;
                        }
                    } catch (e) {
                        oAlert.showError('There is some error while selecting a node.');
                        console.log(e);
                    }

                    oProgressBar.setLoading(100);
                    if (oServerMap === null) {
                        oServerMap = new ServerMap(options);
                    } else {
                        oServerMap.option(options);
                    }
                    oServerMap.load(copiedData.applicationMapData);
                    oProgressBar.stopLoading();
                };

                /**
                 * set link option
                 * @param data
                 * @param linkRouting
                 * @param linkCurve
                 */
                setLinkOption = function (data, linkRouting, linkCurve) {
                    var links = data.applicationMapData.linkDataArray;
                    links.forEach(function (link) {
                        // 재귀 호출인 경우에는 avoidsnodes사용을 강제함.
                        if (link.from === link.to) {
                            link.routing = "AvoidsNodes";
                        } else {
                            link.routing = linkRouting;
                        }
                        link.curve = linkCurve;
                    });
                };

                /**
                 * scope passing transaction response to scatter chart
                 */
                scope.passingTransactionResponseToScatterChart = function () {
                    scope.$emit('serverMap.passingTransactionResponseToScatterChart', scope.node);
                    reset();
                };

                /**
                 * open filter wizard
                 */
                scope.openFilterWizard = function () {
reset();
                    var oSidebarTitleVo = new SidebarTitleVo;
                    oSidebarTitleVo
                        .setImageType(scope.srcServiceType)
                        .setTitle(scope.srcApplicationName)
                        .setImageType2(scope.destServiceType)
                        .setTitle2(scope.destApplicationName);
                    scope.$broadcast('sidebarTitle.initialize.forServerMap', oSidebarTitleVo);
                    $('#filterWizard').modal('show');
                };

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
                    var oServerMapFilterVo = new ServerMapFilterVo();
                    oServerMapFilterVo
                        .setFromApplication(scope.srcApplicationName)
                        .setFromServiceType(scope.srcServiceType)
                        .setToApplication(scope.destApplicationName)
                        .setToServiceType(scope.destServiceType)
                        .setResponseFrom(scope.responseTime.min)
                        .setResponseTo(scope.responseTime.max);
                    if (scope.includeFailed === false) {
                        oServerMapFilterVo.setIncludeException(scope.includeFailed);
                    }
                    if (scope.urlPattern) {
                        oServerMapFilterVo.setRequestUrlPattern(scope.urlPattern);
                    }
                    scope.$broadcast('serverMap.openFilteredMap', oServerMapFilterVo);
                    reset();
                };

                /**
                 * toggle merge unknowns
                 */
                scope.toggleMergeUnknowns = function () {
                    scope.mergeUnknowns = (scope.mergeUnknowns) ? false : true;
                    if (scope.mergeUnknowns) {
                        var copiedData = angular.copy(htLastMapData);
                        ServerMapDao.mergeUnknown(htLastQuery, copiedData);
                        serverMapCallback(htLastQuery, copiedData, scope.linkRouting, scope.linkCurve);
                    } else {
                        serverMapCallback(htLastQuery, htLastMapData, scope.linkRouting, scope.linkCurve);
                    }
                    reset();
                };

                /**
                 * scope toggle link lable text type
                 * @param type
                 */
                scope.toggleLinkLableTextType = function (type) {
                    scope.totalRequestCount = (type !== 'tps') ? true : false;
                    scope.tps = (type === 'tps') ? true : false;
                    serverMapCallback(htLastQuery, htLastMapData, scope.linkRouting, scope.linkCurve);
                    reset();
                };

                /**
                 * toggle link routing
                 * @param type
                 */
                scope.toggleLinkRouting = function (type) {
                    scope.linkRouting = cfg.options.htLinkType.sRouting = type;
                    serverMapCallback(htLastQuery, htLastMapData, scope.linkRouting, scope.linkCurve);
                    reset();
                };

                /**
                 * toggle link curve
                 * @param type
                 */
                scope.toggleLinkCurve = function (type) {
                    scope.linkCurve = cfg.options.htLinkType.sCurve = type;
                    serverMapCallback(htLastQuery, htLastMapData, scope.linkRouting, scope.linkCurve);
                    reset();
                };

                /**
                 * refresh
                 */
                scope.refresh = function () {
                    if (oServerMap) {
                        oServerMap.refresh();
                    }
                    reset();
                };

                /**
                 * scope event on serverMap.initialize
                 */
                scope.$on('serverMap.initialize', function (event, navbarVo) {
                    scope.oNavbarVo = navbarVo;
                    scope.bShowServerMapStatus = true;
                    bUseNodeContextMenu = bUseLinkContextMenu = bUseBackgroundContextMenu = true;
                    showServerMap(navbarVo.getApplicationName(), navbarVo.getServiceType(), navbarVo.getQueryEndTime(), navbarVo.getQueryPeriod(), navbarVo.getFilter(), scope.mergeUnknowns, scope.linkRouting, scope.linkCurve);
                });

                /**
                 * scope event on serverMap.fetch
                 */
                scope.$on('serverMap.fetch', function (event, queryPeriod, queryEndTime) {
                    showServerMap(scope.oNavbarVo.getApplicationName(), scope.oNavbarVo.getServiceType(), queryEndTime, queryPeriod, scope.oNavbarVo.getFilter(), scope.mergeUnknowns, scope.linkRouting, scope.linkCurve);
                });

                /**
                 * scope event on serverMap.initializeWithMapData
                 */
                scope.$on('serverMap.initializeWithMapData', function (event, mapData) {
                    scope.bShowServerMapStatus = false;
                    bUseBackgroundContextMenu = true;
                    bUseNodeContextMenu = bUseLinkContextMenu = false;
                    htLastQuery = {
                        applicationName: mapData.applicationId
                    };
                    htLastMapData = mapData;
                    serverMapCallback(htLastQuery, htLastMapData, scope.mergeUnknowns, scope.linkRouting, scope.linkCurve);
                });

            }
        };
    }]);
