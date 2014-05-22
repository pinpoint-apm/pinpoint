'use strict';

pinpointApp.constant('serverMapConfig', {
    options: {
        "sContainerId": 'servermap',
        "sBigFont": "11pt Arimo,avn85,NanumGothic,ng,dotum,AppleGothic,sans-serif",
        "sSmallFont": "11pt Arimo,avn55,NanumGothic,ng,dotum,AppleGothic,sans-serif",
        "sImageDir": '/images/servermap/',
        "htLinkType": {
            "sRouting": "Normal", // Normal, Orthogonal, AvoidNodes
            "sCurve": "JumpGap" // Bezier, JumpOver, JumpGap
        },
        "htLinkTheme": {
            "default": {
                "backgroundColor": "#ffffff",
                "borderColor": "#c5c5c5",
                "fontFamily": "11pt Arimo,avn55,NanumGothic,ng,dotum,AppleGothic,sans-serif",
                "fontColor": "#000000",
                "fontAlign": "center",
                "margin": 1
            },
            "bad": {
                "backgroundColor": "#ffc9c9",
                "borderColor": "#7d7d7d",
                "fontFamily": "11pt Arimo,avn55,NanumGothic,ng,dotum,AppleGothic,sans-serif",
                "fontColor": "#FF1300",
                "fontAlign": "center",
                "margin": 1
            }
        }
    }
});

pinpointApp.directive('serverMap', [ 'serverMapConfig', 'ServerMapDao', 'Alerts', 'ProgressBar', 'SidebarTitleVo', '$filter', 'ServerMapFilterVo', 'encodeURIComponentFilter', 'filteredMapUtil', '$base64', 'ServerMapHintVo',
    function (cfg, ServerMapDao, Alerts, ProgressBar, SidebarTitleVo, $filter, ServerMapFilterVo, encodeURIComponentFilter, filteredMapUtil, $base64, ServerMapHintVo) {
        return {
            restrict: 'EA',
            replace: true,
            templateUrl: 'views/serverMap.html',
            link: function postLink(scope, element, attrs) {

                // define private variables
                var bUseNodeContextMenu, bUseLinkContextMenu, htLastQuery,
                    bUseBackgroundContextMenu, oServerMap, oAlert, oProgressBar, htLastMapData, htLastLink, htLastNode,
                    sLastSelection;

                // define private variables of methods
                var showServerMap, setNodeContextMenuPosition, reset,
                    setLinkContextMenuPosition, setBackgroundContextMenuPosition, serverMapCallback, setLinkOption;


                // bootstrap
                oAlert = new Alerts(element);
                oProgressBar = new ProgressBar(element);
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
                scope.oNavbarVo = null;
                scope.mergeUnknowns = true;
                scope.totalRequestCount = true;
                scope.bShowServerMapStatus = false;
                scope.linkRouting = cfg.options.htLinkType.sRouting;
                scope.linkCurve = cfg.options.htLinkType.sCurve;

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
                    if (!scope.$$phase) {
                        scope.$digest();
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
                 * @param mergeUnknowns
                 * @param linkRouting
                 * @param linkCurve
                 */
                showServerMap = function (applicationName, serviceTypeCode, to, period, filterText, hintText, mergeUnknowns, linkRouting, linkCurve) {
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
                        serviceTypeCode: serviceTypeCode,
                        from: to - period,
                        to: to,
                        originTo: scope.oNavbarVo.getQueryEndTime(),
                        period: period,
                        filter: encodeURIComponentFilter(filterText),
                        hint: hintText ? encodeURIComponentFilter(hintText) : false
                    };

                    if (filterText) {
                        ServerMapDao.getFilteredServerMapData(htLastQuery, function (err, query, result) {
                            if (err) {
                                oProgressBar.stopLoading();
                                oAlert.showError('There is some error.');
                                return false;
                            }
                            oProgressBar.setLoading(50);
                            if (query.from === result.lastFetchedTimestamp) {
                                scope.$emit('serverMap.allFetched', result);
                            } else {
                                htLastMapData.lastFetchedTimestamp = result.lastFetchedTimestamp - 1;
                                scope.$emit('serverMap.fetched', htLastMapData.lastFetchedTimestamp, result);
                            }

                            var filters = JSON.parse(filterText);
                            var serverMapData = ServerMapDao.addFilterProperty(filters, ServerMapDao.mergeFilteredMapData(htLastMapData, result));
                            if (filteredMapUtil.doFiltersHaveUnknownNode(filters)) scope.mergeUnknowns = mergeUnknowns = false;
                            if (mergeUnknowns) {
                                var copiedData = angular.copy(serverMapData);
                                ServerMapDao.mergeUnknown(query, copiedData);
                                serverMapCallback(query, copiedData, mergeUnknowns, linkRouting, linkCurve);
                            } else {
                                serverMapCallback(query, serverMapData, mergeUnknowns, linkRouting, linkCurve);
                            }
                        });
                    } else {
                        ServerMapDao.getServerMapData(htLastQuery, function (err, query, serverMapData) {
                            if (err) {
                                oProgressBar.stopLoading();
                                oAlert.showError('There is some error.');
                                return false;
                            }
                            oProgressBar.setLoading(50);
                            htLastMapData = serverMapData;
                            serverMapCallback(query, serverMapData, mergeUnknowns, linkRouting, linkCurve);
                        });
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
                 * @param mergeUnknowns
                 * @param copiedData
                 * @param linkRouting
                 * @param linkCurve
                 */
                serverMapCallback = function (query, data, mergeUnknowns, linkRouting, linkCurve) {

                    var copiedData;
                    copiedData = angular.copy(data);
                    if (mergeUnknowns) {
                        ServerMapDao.mergeUnknown(htLastQuery, copiedData);
                    }

                    ServerMapDao.removeNoneNecessaryDataForHighPerformance(copiedData);
                    oProgressBar.setLoading(80);
                    if (copiedData.applicationMapData.nodeDataArray.length === 0) {
                        oProgressBar.stopLoading();
                        if (scope.oNavbarVo.getFilter()) {
                            var aFilter = scope.oNavbarVo.getFilterAsJson();
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
                            oAlert.showInfo(aFilterInfo.join(''));
                        } else {
                            oAlert.showInfo('There is no data.');
                        }
                        return;
                    }

                    setLinkOption(copiedData, linkRouting, linkCurve);
                    oProgressBar.setLoading(90);

                    var options = cfg.options;
                    options.fOnNodeClicked = function (e, node) {
                        var originalNode = ServerMapDao.getNodeDataByKey(data, node.key);
                        if (originalNode) {
                            node = originalNode;
                        }
                        sLastSelection = 'node';
                        htLastNode = node;
                        scope.$emit("serverMap.nodeClicked", e, query, node, data);
                        reset();
                    };
                    options.fOnNodeContextClicked = function (e, node) {
                        scope.$emit("serverMap.nodeContextClicked", e, query, node, data);
                        reset();
                        var originalNode = ServerMapDao.getNodeDataByKey(data, node.key);
                        if (originalNode) {
                            node = originalNode;
                        }
                        htLastNode = node;
                        if (!bUseNodeContextMenu) {
                            return;
                        }
                        if (node.isWas === true) {
                            setNodeContextMenuPosition(e.event.layerY, e.event.layerX);
                        }
                    };
                    options.fOnLinkClicked = function (e, link) {
                        scope.$emit("serverMap.linkClicked", e, query, link, data);
                        sLastSelection = 'link';
                        htLastLink = link;
                        reset();
                    };
                    options.fOnLinkContextClicked = function (e, link) {
                        scope.$emit("serverMap.linkContextClicked", e, query, link, data);
                        reset();
                        htLastLink = link;

                        if (!bUseLinkContextMenu || angular.isArray(link.targetInfo)) {
                            return;
                        }
                        setLinkContextMenuPosition(e.event.layerY, e.event.layerX);
                    };
                    options.fOnBackgroundClicked = function (e) {
                        scope.$emit("serverMap.backgroundClicked", e, query);
                        reset();
                    };
                    options.fOnBackgroundDoubleClicked = function (e) {
                        oServerMap.zoomToFit();
                    };
                    options.fOnBackgroundContextClicked = function (e) {
                        scope.$emit("serverMap.backgroundContextClicked", e, query);
                        reset();
                        if (!bUseBackgroundContextMenu) {
                            return;
                        }
                        setBackgroundContextMenuPosition(e.diagram.lastInput.event.layerY, e.diagram.lastInput.event.layerX);
                    };

                    var selectedNode;
                    try {
                        selectedNode = _.find(copiedData.applicationMapData.nodeDataArray, function (node) {
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

                    if (sLastSelection === 'node' && htLastNode) {
                        oServerMap.highlightNodeByKey(htLastNode.key);
                    } else if (sLastSelection === 'link' && htLastLink) {
                        oServerMap.highlightLinkByFromTo(htLastLink.from, htLastLink.to);
                    } else if (selectedNode) {
                        oServerMap.highlightNodeByKey(selectedNode.key);
                    }
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
                    scope.$emit('serverMap.passingTransactionResponseToScatterChart', htLastNode);
                    reset();
                };

                /**
                 * passing transaction list
                 */
                scope.passingTransactionList = function () {
                    var oServerMapFilterVo = new ServerMapFilterVo();

                    oServerMapFilterVo
                        .setMainApplication(htLastLink.filterApplicationName)
                        .setMainServiceTypeCode(htLastLink.filterApplicationServiceTypeCode)
                        .setFromApplication(htLastLink.fromNode.applicationName)
                        .setFromServiceType(htLastLink.fromNode.serviceType)
                        .setToApplication(htLastLink.toNode.applicationName)
                        .setToServiceType(htLastLink.toNode.serviceType);

                    var oServerMapHintVo = new ServerMapHintVo();
                    if (htLastLink.sourceInfo.isWas && htLastLink.targetInfo.isWas) {
                        oServerMapHintVo.setHint(htLastLink.toNode.applicationName, htLastLink.filterTargetRpcList)
                    }

                    scope.$broadcast('serverMap.openFilteredMap', oServerMapFilterVo, oServerMapHintVo);
                    reset();
                };

                /**
                 * open filter wizard
                 */
                var bIsFilterWizardLoaded = false;
                scope.openFilterWizard = function () {
                    reset();
                    var oSidebarTitleVo = new SidebarTitleVo;

                    if (htLastLink.fromNode.serviceType === 'USER') {
                        oSidebarTitleVo
                            .setImageType('USER')
                            .setTitle('USER')
                    } else {
                        oSidebarTitleVo
                            .setImageType(htLastLink.fromNode.serviceType)
                            .setTitle(htLastLink.fromNode.applicationName)
                    }
                    oSidebarTitleVo
                        .setImageType2(htLastLink.toNode.serviceType)
                        .setTitle2(htLastLink.toNode.applicationName);

                    scope.$broadcast('sidebarTitle.initialize.forServerMap', oSidebarTitleVo);

                    $('#filterWizard').modal('show');
                    if (!bIsFilterWizardLoaded) {
                        bIsFilterWizardLoaded = true;
                        $('#filterWizard')
                            .on('shown.bs.modal', function () {
                                $('slider', this).addClass('auto');
                                setTimeout(function () {
                                    $('#filterWizard slider').removeClass('auto');
                                }, 500);
                                if (scope.oNavbarVo.getFilter()) {
                                    var result = filteredMapUtil.findFilterInNavbarVo(
                                        htLastLink.fromNode.applicationName,
                                        htLastLink.fromNode.serviceType,
                                        htLastLink.toNode.applicationName,
                                        htLastLink.toNode.serviceType,
                                        scope.oNavbarVo);
                                    if (result) {
                                        scope.urlPattern = result.oServerMapFilterVo.getRequestUrlPattern();
                                        scope.responseTime.from = result.oServerMapFilterVo.getResponseFrom();
                                        var to = result.oServerMapFilterVo.getResponseTo();
                                        scope.responseTime.to = to === 'max' ? 30000 : to;
                                        scope.includeFailed = result.oServerMapFilterVo.getIncludeException();
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
                    var oServerMapFilterVo = new ServerMapFilterVo();
                    oServerMapFilterVo
                        .setMainApplication(htLastLink.filterApplicationName)
                        .setMainServiceTypeCode(htLastLink.filterApplicationServiceTypeCode)
                        .setFromApplication(htLastLink.fromNode.applicationName)
                        .setFromServiceType(htLastLink.fromNode.serviceType)
                        .setToApplication(htLastLink.toNode.applicationName)
                        .setToServiceType(htLastLink.toNode.serviceType)
                        .setResponseFrom(scope.responseTime.from)
                        .setResponseTo(scope.responseTime.to)
                        .setIncludeException(scope.includeFailed)
                        .setRequestUrlPattern($base64.encode(scope.urlPattern));

                    var oServerMapHintVo = new ServerMapHintVo();
                    if (htLastLink.sourceInfo.isWas && htLastLink.targetInfo.isWas) {
                        oServerMapHintVo.setHint(htLastLink.toNode.applicationName, htLastLink.filterTargetRpcList)
                    }
                    scope.$broadcast('serverMap.openFilteredMap', oServerMapFilterVo, oServerMapHintVo);
                    reset();
                };

                /**
                 * toggle merge unknowns
                 */
                scope.toggleMergeUnknowns = function () {
                    scope.mergeUnknowns = (scope.mergeUnknowns) ? false : true;
                    serverMapCallback(htLastQuery, htLastMapData, scope.mergeUnknowns, scope.linkRouting, scope.linkCurve);
                    reset();
                };

                /**
                 * scope toggle link lable text type
                 * @param type
                 */
                scope.toggleLinkLableTextType = function (type) {
                    scope.totalRequestCount = (type !== 'tps') ? true : false;
                    scope.tps = (type === 'tps') ? true : false;
                    serverMapCallback(htLastQuery, htLastMapData, scope.mergeUnknowns, scope.linkRouting, scope.linkCurve);
                    reset();
                };

                /**
                 * toggle link routing
                 * @param type
                 */
                scope.toggleLinkRouting = function (type) {
                    scope.linkRouting = cfg.options.htLinkType.sRouting = type;
                    serverMapCallback(htLastQuery, htLastMapData, scope.mergeUnknowns, scope.linkRouting, scope.linkCurve);
                    reset();
                };

                /**
                 * toggle link curve
                 * @param type
                 */
                scope.toggleLinkCurve = function (type) {
                    scope.linkCurve = cfg.options.htLinkType.sCurve = type;
                    serverMapCallback(htLastQuery, htLastMapData, scope.mergeUnknowns, scope.linkRouting, scope.linkCurve);
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
                    if (scope.oNavbarVo && htLastQuery.applicationName !== navbarVo.getApplicationName()) {
                        sLastSelection = false;
                    }
                    scope.oNavbarVo = navbarVo;
                    scope.bShowServerMapStatus = true;
                    bUseLinkContextMenu = bUseBackgroundContextMenu = true;
                    bUseNodeContextMenu = false;
                    showServerMap(navbarVo.getApplicationName(), navbarVo.getServiceTypeCode(), navbarVo.getQueryEndTime(), navbarVo.getQueryPeriod(), navbarVo.getFilter(), navbarVo.getHint(), scope.mergeUnknowns, scope.linkRouting, scope.linkCurve);
                });

                /**
                 * scope event on serverMap.fetch
                 */
                scope.$on('serverMap.fetch', function (event, queryPeriod, queryEndTime) {
                    showServerMap(scope.oNavbarVo.getApplicationName(), scope.oNavbarVo.getServiceTypeCode(), queryEndTime, queryPeriod, scope.oNavbarVo.getFilter(), scope.oNavbarVo.getHint(), scope.mergeUnknowns, scope.linkRouting, scope.linkCurve);
                });

                /**
                 * scope event on serverMap.initializeWithMapData
                 */
                scope.$on('serverMap.initializeWithMapData', function (event, mapData) {
                    reset();
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
