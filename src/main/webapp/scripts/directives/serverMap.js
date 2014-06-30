'use strict';

pinpointApp.constant('serverMapConfig', {
    options: {
        "sContainerId": 'servermap',
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
                "margin": 1
            },
            "bad": {
                "backgroundColor": "#ffc9c9",
                "borderColor": "#7d7d7d",
                "fontFamily": "11pt Lato,NanumGothic,ng,dotum,AppleGothic,sans-serif",
                "fontColor": "#FF1300",
                "fontAlign": "center",
                "margin": 1
            }
        }
    }
});

pinpointApp.directive('serverMap', [ 'serverMapConfig', 'ServerMapDao', 'Alerts', 'ProgressBar', 'SidebarTitleVo', '$filter', 'ServerMapFilterVo', 'encodeURIComponentFilter', 'filteredMapUtil', '$base64', 'ServerMapHintVo', '$timeout',
    function (cfg, ServerMapDao, Alerts, ProgressBar, SidebarTitleVo, $filter, ServerMapFilterVo, encodeURIComponentFilter, filteredMapUtil, $base64, ServerMapHintVo, $timeout) {
        return {
            restrict: 'EA',
            replace: true,
            templateUrl: 'views/serverMap.html',
            link: function postLink(scope, element, attrs) {

                // define private variables
                var bUseNodeContextMenu, bUseLinkContextMenu, htLastQuery,
                    bUseBackgroundContextMenu, oServerMap, oAlert, oProgressBar, htLastMapData, htLastLink, htLastNode,
                    sLastSelection, $fromAgentName, $toAgentName, bIsFilterWizardLoaded;

                // define private variables of methods
                var showServerMap, setNodeContextMenuPosition, reset, emitDataExisting,
                    setLinkContextMenuPosition, setBackgroundContextMenuPosition, serverMapCallback, setLinkOption,
                    zoomToFit, updateLastSelection, openFilterWizard;


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
                $fromAgentName = element.find('.fromAgentName');
                $toAgentName = element.find('.toAgentName');
                $fromAgentName.select2();
                $toAgentName.select2();
                bIsFilterWizardLoaded = false;

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
                            htLastMapData.applicationMapData = ServerMapDao.mergeFilteredMapData(htLastMapData.applicationMapData, result.applicationMapData);
                            var serverMapData = ServerMapDao.extractDataFromApplicationMapData(htLastMapData.applicationMapData);
                            serverMapData = ServerMapDao.addFilterProperty(filters, serverMapData);
                            if (filteredMapUtil.doFiltersHaveUnknownNode(filters)) scope.mergeUnknowns = mergeUnknowns = false;
                            emitDataExisting(htLastMapData);
                            serverMapCallback(query, serverMapData, mergeUnknowns, linkRouting, linkCurve);
                        });
                    } else {
                        ServerMapDao.getServerMapData(htLastQuery, function (err, query, mapData) {
                            if (err) {
                                oProgressBar.stopLoading();
                                oAlert.showError('There is some error.');
                                scope.$emit('servermap.hasNoData');
                                return false;
                            }
                            oProgressBar.setLoading(50);
                            emitDataExisting(mapData);
                            htLastMapData = mapData;
                            var serverMapData = ServerMapDao.extractDataFromApplicationMapData(mapData.applicationMapData);
                            serverMapCallback(query, serverMapData, mergeUnknowns, linkRouting, linkCurve);
                        });
                    }
                };

                /**
                 * emit data existing
                 * @param mapData
                 */
                emitDataExisting = function (mapData) {
                    if (mapData.applicationMapData.nodeDataArray.length === 0 || mapData.applicationMapData.linkDataArray.length === 0) {
                        scope.$emit('servermap.hasNoData');
                    } else {
                        scope.$emit('servermap.hasData');
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

                /**
                 * server map callback
                 * @param query
                 * @param applicationMapData
                 * @param mergeUnknowns
                 * @param linkRouting
                 * @param linkCurve
                 */
                serverMapCallback = function (query, applicationMapData, mergeUnknowns, linkRouting, linkCurve) {
                    var lastCopiedData = applicationMapData;
                    if (mergeUnknowns) {
                        lastCopiedData = ServerMapDao.mergeUnknown(applicationMapData);
                    }

//                    ServerMapDao.removeNoneNecessaryDataForHighPerformance(lastCopiedData);
                    oProgressBar.setLoading(80);
                    if (lastCopiedData.nodeDataArray.length === 0) {
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

                    setLinkOption(lastCopiedData, linkRouting, linkCurve);
                    oProgressBar.setLoading(90);

                    var options = cfg.options;
                    options.fOnNodeClicked = function (e, node, unknownKey) {
                        var originalNode;
                        if (angular.isDefined(node.unknownNodeGroup) && !unknownKey) {
                            node.unknownNodeGroup = ServerMapDao.getUnknownNodeDataByUnknownNodeGroup(htLastMapData.applicationMapData, node.unknownNodeGroup);
                        } else {
                            originalNode = ServerMapDao.getNodeDataByKey(htLastMapData.applicationMapData, unknownKey || node.key);
                        }
                        if (originalNode) {
                            node = originalNode;
                        }
                        sLastSelection = 'node';
                        htLastNode = node;
                        scope.$emit("serverMap.nodeClicked", e, htLastQuery, node, lastCopiedData);
                        reset();
                    };
                    options.fOnNodeContextClicked = function (e, node) {
                        reset();
                        var originalNode = ServerMapDao.getNodeDataByKey(htLastMapData.applicationMapData, node.key);
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
                        scope.$emit("serverMap.nodeContextClicked", e, query, node, applicationMapData);
                    };
                    options.fOnLinkClicked = function (e, link) {
                        var originalLink;
                        if (angular.isDefined(link.unknownLinkGroup)) {
                            link.unknownLinkGroup = ServerMapDao.getUnknownLinkDataByUnknownLinkGroup(htLastMapData.applicationMapData, link.unknownLinkGroup);
                        } else {
                            originalLink = ServerMapDao.getLinkDataByKey(htLastMapData.applicationMapData, link.key);
                        }
                        if (originalLink) {
                            originalLink.fromNode = link.fromNode;
                            originalLink.toNode = link.toNode;
                            link = originalLink;
                        }
                        sLastSelection = 'link';
                        htLastLink = link;
                        reset();
                        scope.$emit("serverMap.linkClicked", e, htLastQuery, link, lastCopiedData);
                    };
                    options.fOnLinkContextClicked = function (e, link) {
                        var originalLink = ServerMapDao.getLinkDataByKey(htLastMapData.applicationMapData, link.key);
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
                        scope.$emit("serverMap.linkContextClicked", e, query, link, applicationMapData);
                    };
                    options.fOnBackgroundClicked = function (e) {
                        scope.$emit("serverMap.backgroundClicked", e, query);
                        reset();
                    };
                    options.fOnBackgroundDoubleClicked = function (e) {
                        zoomToFit();
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
                        selectedNode = _.find(lastCopiedData.nodeDataArray, function (node) {
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
                    oServerMap.load(lastCopiedData);
                    oProgressBar.stopLoading();

                    updateLastSelection(selectedNode);
                };

                /**
                 * set link option
                 * @param data
                 * @param linkRouting
                 * @param linkCurve
                 */
                setLinkOption = function (data, linkRouting, linkCurve) {
                    var links = data.linkDataArray;
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

                openFilterWizard = function () {
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

                    scope.sourceInfo = htLastLink.sourceInfo;
                    scope.sourceHistogram = htLastLink.sourceHistogram;
                    scope.targetInfo = htLastLink.targetInfo;
                    scope.targetHistogram = htLastLink.toNode.agentHistogram;
                    scope.fromApplicationName = htLastLink.fromNode.applicationName;
                    scope.toApplicationName = htLastLink.toNode.applicationName;
                    $fromAgentName.select2('val', '');
                    $toAgentName.select2('val', '');

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
                                        $fromAgentName.select2('val', result.oServerMapFilterVo.getFromAgentName());
                                        $toAgentName.select2('val', result.oServerMapFilterVo.getToAgentName());
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
                scope.openFilterWizard = function () {
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

                    if (scope.fromAgentName) {
                        oServerMapFilterVo.setFromAgentName(scope.fromAgentName);
                    }
                    if (scope.toAgentName) {
                        oServerMapFilterVo.setToAgentName(scope.toAgentName);
                    }

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
                    serverMapCallback(htLastQuery, htLastMapData.applicationMapData, scope.mergeUnknowns, scope.linkRouting, scope.linkCurve);
                    reset();
                };

                /**
                 * scope toggle link lable text type
                 * @param type
                 */
                scope.toggleLinkLableTextType = function (type) {
                    scope.totalRequestCount = (type !== 'tps') ? true : false;
                    scope.tps = (type === 'tps') ? true : false;
                    serverMapCallback(htLastQuery, htLastMapData.applicationMapData, scope.mergeUnknowns, scope.linkRouting, scope.linkCurve);
                    reset();
                };

                /**
                 * toggle link routing
                 * @param type
                 */
                scope.toggleLinkRouting = function (type) {
                    scope.linkRouting = cfg.options.htLinkType.sRouting = type;
                    serverMapCallback(htLastQuery, htLastMapData.applicationMapData, scope.mergeUnknowns, scope.linkRouting, scope.linkCurve);
                    reset();
                };

                /**
                 * toggle link curve
                 * @param type
                 */
                scope.toggleLinkCurve = function (type) {
                    scope.linkCurve = cfg.options.htLinkType.sCurve = type;
                    serverMapCallback(htLastQuery, htLastMapData.applicationMapData, scope.mergeUnknowns, scope.linkRouting, scope.linkCurve);
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
                    serverMapCallback(htLastQuery, htLastMapData.applicationMapData, scope.mergeUnknowns, scope.linkRouting, scope.linkCurve);
                });

                /**
                 * scope event on serverMap.zoomToFit
                 */
                scope.$on('serverMap.zoomToFit', function (event) {
                    zoomToFit();
                });

                scope.$on('serverMap.openFilterWizard', function (event, link) {
                    htLastLink = link;
                    openFilterWizard();
                });
            }
        };
    }]);
