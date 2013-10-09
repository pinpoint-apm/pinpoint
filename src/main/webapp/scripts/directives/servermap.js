'use strict';

pinpointApp.constant('cfg', {
    applicationUrl: '/applications.pinpoint',
    filteredServerMapData2: '/getFilteredServerMapData2.pinpoint',
    filtermapUrl: '/filtermap.pinpoint',
    lastTransactionListUrl: '/lastTransactionList.pinpoint',
    transactionListUrl: '/transactionList.pinpoint',
    options: {
        "sContainerId": 'servermap',
        "sImageDir": '/images/icons/',
        "htIcons": {
            'APACHE': 'APACHE.png',
            'ARCUS': 'ARCUS.png',
            'CUBRID': 'CUBRID.png',
            'ETC': 'ETC.png',
            'MEMCACHED': 'MEMCACHED.png',
            'MYSQL': 'MYSQL.png',
            'QUEUE': 'QUEUE.png',
            'TOMCAT': 'TOMCAT.png',
            'UNKNOWN_CLOUD': 'UNKNOWN_CLOUD.png',
            'UNKNOWN_GROUP': 'UNKNOWN_CLOUD.png',
            'USER': 'USER.png',
            'ORACLE': 'ORACLE.png'
        },
        "htLinkType": {
            "sRouting": "AvoidsNodes", // Normal, Orthogonal, AvoidNodes
            "sCurve": "JumpGap" // Bezier, JumpOver, JumpGap
        }
    },
    FILTER_DELIMETER: "^",
    FILTER_ENTRY_DELIMETER: "|",
    FILTER_FETCH_LIMIT: 99
});

pinpointApp.directive('servermap', [ 'cfg', '$rootScope', 'alerts', 'progressBar', function (cfg, $rootScope, alerts, progressBar) {
    return {
        restrict: 'EA',
        replace: true,
        templateUrl: 'views/servermap.html',
        link: function postLink(scope, element, attrs) {
            var serverMapCachedQuery, serverMapCachedData, bUseNodeContextMenu, bUseLinkContextMenu, bUseBackgroundContextMenu;
            var oServerMap = null;
            var SERVERMAP_METHOD_CACHE = {};

            alerts.setParent(element);
            progressBar.setParent(element);

            var showServerMap = function (applicationName, serviceType, to, period, filterText, mergeUnknowns, hideIndirectAccess, linkRouting, linkCurve) {
                progressBar.startLoading();
                if (oServerMap) {
                    oServerMap.clear();
                }
                progressBar.setLoading(10);

                var query = {
                    applicationName: applicationName,
                    serviceType: serviceType,
                    from: to - period,
                    to: to,
                    period: period,
                    filter: filterText,
                    hideIndirectAccess: hideIndirectAccess
                };

                if (filterText) {
                    getFilteredServerMapData(query, function (query, result) {
                        serverMapCallback(query, result, mergeUnknowns, linkRouting, linkCurve);

                        var DAY = 60 * 60 * 24 * 1000;
                        var period = query.to - query.from;
                        var fetchedPeriod = query.to - result.lastFetchedTimestamp;
                        var ratio = Math.floor(fetchedPeriod / period * 100);
                        var nextFetchFrom = result.lastFetchedTimestamp + 1;

                        // TODO 대충 24시간으로 계산하나 걸치는 경우 체크가 필요할지도. 예: 22:00 ~ 03:00
                        // TODO 그냥 날짜까지 보여줄까.
                        var dateFormat = (period > DAY) ? "yyyy/MM/dd HH:mm:ss" : "HH:mm:ss";

                        console.log("query", query);
                        console.log("result", result);

                        var strFrom = new Date(query.from).toString(dateFormat);
                        var strTo = new Date(query.to).toString(dateFormat);
                        var strOffset = new Date(result.lastFetchedTimestamp).toString(dateFormat);


                        var fetchNext = function () {
                            // TODO next 조회 로직 추가 필요.
                            // TODO 새로 조회한 것과 기존에 조회된 것 머지 기능 추가 필요.
                            console.log("fetch more " + new Date(nextFetchFrom).toString("yyyy/MM/dd HH:mm:ss") + " ~ " + new Date(query.to).toString("yyyy/MM/dd HH:mm:ss"));
                        }

                        var fetchDone = function () {
                            // TODO 조회가 완료되면 할 거 없음.
                            console.log("That's all.");
                        }

                        // lastFetchedTimestamp + 1 ~ query.to까지 계속 조회하고.
                        // 조회된 데이터가 0개이면 모두 조회되었다고 판단해도 됨.
                        // lastFetchedTimestamp가 query.to하고 같으면 더 조회하지 않아도 됨.
                        var needMoreFetch = result.applicationMapData.nodeDataArray.length > 0;
                        needMoreFetch |= result.lastFetchedTimestamp == query.to;

                        // TODO 밖으로 빼내서 핸들러 하나만 등록하도록 변경할 것.
                    });
                } else {
                    getServerMapData2(query, function (query, result) {
                        serverMapCallback(query, result, mergeUnknowns, linkRouting, linkCurve);
                    });
                }
            };

            var getServerMapData2 = function (query, callback) {
                progressBar.setLoading(50);
                jQuery.ajax({
                    type: 'GET',
                    url: '/getServerMapData2.pinpoint',
                    cache: false,
                    dataType: 'json',
                    data: {
                        application: query.applicationName,
                        serviceType: query.serviceType,
                        from: query.from,
                        to: query.to,
                        hideIndirectAccess: query.hideIndirectAccess
                    },
                    success: function (result) {
                        progressBar.setLoading(30);
                        callback(query, result);
                    },
                    error: function (xhr, status, error) {
                        console.log("ERROR", status, error);
                        progressBar.stopLoading();
                        alerts.showWarning('There is some error.');
                    }
                });
            };

            var getFilteredServerMapData = function (query, callback) {
                jQuery.ajax({
                    type: 'GET',
                    url: cfg.filteredServerMapData2,
                    cache: false,
                    dataType: 'json',
                    data: {
                        application: query.applicationName,
                        serviceType: query.serviceType,
                        from: query.from,
                        to: query.to,
                        filter: query.filter,
                        limit: cfg.FILTER_FETCH_LIMIT
                    },
                    success: function (result) {
                        callback(query, result);
                    },
                    error: function (xhr, status, error) {
                        console.log("ERROR", status, error);
                        progressBar.stopLoading();
                        alerts.showWarning('There is some error.');
                    }
                });
            };

            var reset = function () {
                scope.nodeContextMenuStyle = '';
                scope.linkContextMenuStyle = '';
                scope.backgroundContextMenuStyle = '';
                if (!scope.$$phase) {
                    scope.$digest();
                }
            };
            var setNodeContextMenuPosition = function (top, left) {
                scope.nodeContextMenuStyle = {
                    display: 'block'
                };
                var nodeContextMenu = element.find('.nodeContextMenu');
                nodeContextMenu.css({
                    'top': top,
                    'left': left
                });
                scope.$digest();
            };
            var setLinkContextMenuPosition = function (top, left) {
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
            var setBackgroundContextMenuPosition = function (top, left) {
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

            scope.passingTransactionResponseToScatterChart = function () {
                $rootScope.$broadcast('servermap.passingTransactionResponseToScatterChart', scope.node);
                reset();
            };

            scope.passingTransactionMap = function () {
                var application = scope.navbar.application,
                    period = scope.navbar.period,
                    queryEndTime = scope.navbar.queryEndTime,
                    srcServiceType = scope.srcServiceType,
                    srcApplicationName = scope.srcApplicationName,
                    destServiceType = scope.destServiceType,
                    destApplicationName = scope.destApplicationName,
                    prevFilter = scope.filter;

                var newFilter = ((prevFilter) ? prevFilter + cfg.FILTER_DELIMETER : "")
                    + srcServiceType + cfg.FILTER_ENTRY_DELIMETER
                    + srcApplicationName + cfg.FILTER_ENTRY_DELIMETER
                    + destServiceType + cfg.FILTER_ENTRY_DELIMETER
                    + destApplicationName;

                var url = '#/filteredMap/' + application + '/' + period + '/' + queryEndTime + '/' + newFilter;
                window.open(url, "");
                reset();
            };
//            scope.passingTransactionList = function () {
//                var applicationName = scope.srcApplicationName,
//                    from = scope.navbar.queryStartTime,
//                    to = scope.navbar.queryEndTime,
//                    period = scope.navbar.queryPeriod,
//                    usePeriod = scope.usePeriod,
//                    filter = scope.srcServiceType + '|' + scope.srcApplicationName + '|' + scope.destServiceType + '|' + scope.destApplicationName;
//
//                if (scope.srcServiceType === 'CLIENT') {
//                    applicationName =   scope.destServiceType;
//                    filter = scope.srcServiceType + '|' + scope.destApplicationName + '|' + scope.destServiceType + '|' + scope.destApplicationName;
//                }
//
//                if (usePeriod) {
//                    window.open(cfg.lastTransactionListUrl + "?application=" + applicationName + "&period=" + period + ( filter ? "&filter=" + filter : "") );
//                } else {
//                    window.open(cfg.transactionListUrl + "?application=" + applicationName + "&from=" + from + "&to=" + to + ( filter ? "&filter=" + filter : "") );
//                }
//                reset();
//            };

            var serverMapCallback = function (query, data, mergeUnknowns, linkRouting, linkCurve) {
                serverMapCachedQuery = angular.copy(query);
                serverMapCachedData = angular.copy(data);
                progressBar.setLoading(80);
                if (data.applicationMapData.nodeDataArray.length === 0) {
                    progressBar.stopLoading();
                    alerts.showInfo('There is no data.');
                    return;
                }

                if (mergeUnknowns) {
                    mergeUnknown(query, data);
                }

                replaceClientToUser(data);
                setLinkOption(data, linkRouting, linkCurve);
                progressBar.setLoading(90);

                var options = cfg.options;
                options.fOnNodeContextClicked = function (e, node) {
                    $rootScope.$broadcast("servermap.nodeContextClicked", e, query, node, data);
                    reset();
                    scope.node = node;
                    if (!bUseNodeContextMenu) {
                        return;
                    }
                    if (node.category !== "UNKNOWN_GROUP" && node.category !== "USER") {
                        setNodeContextMenuPosition(e.event.layerY, e.event.layerX);
                    }
                };
                options.fOnLinkContextClicked = function (e, link) {
                    $rootScope.$broadcast("servermap.linkContextClicked", e, query, link, data);
                    reset();
                    scope.link = link;
                    scope.nodeCategory = link.category || '';
                    scope.srcServiceType = link.sourceinfo.serviceType || '';
                    scope.srcApplicationName = link.sourceinfo.applicationName || '';
                    scope.destServiceType = link.targetinfo.serviceType || '';
                    scope.destApplicationName = link.targetinfo.applicationName || '';
                    if (!bUseLinkContextMenu) {
                        return;
                    }
                    setLinkContextMenuPosition(e.event.layerY, e.event.layerX);
                };
                options.fOnLinkClicked = function (e, link) {
                    $rootScope.$broadcast("servermap.linkClicked", e, query, link, data);
                    reset();
                };
                options.fOnNodeClicked = function (e, node) {
                    $rootScope.$broadcast("servermap.nodeClicked", e, query, node, data);
                    reset();
                };
                options.fOnBackgroundClicked = function (e) {
                    $rootScope.$broadcast("servermap.backgroundClicked", e, query);
                    reset();
                };
                options.fOnBackgroundContextClicked = function (e) {
                    $rootScope.$broadcast("servermap.backgroundContextClicked", e, query);
                    reset();
                    if (!bUseBackgroundContextMenu) {
                        return;
                    }
                    setBackgroundContextMenuPosition(e.diagram.lastInput.event.layerY, e.diagram.lastInput.event.layerX);
                };

                try {
                    var selectedNode = (function () {
                        for (var i = 0; i < data.applicationMapData.nodeDataArray.length; i++) {
                            var e = data.applicationMapData.nodeDataArray[i];
                            if (e.text === query.applicationName && e.serviceTypeCode === query.serviceType) {
                                return e;
                            } else if (e.text === query.applicationName && angular.isUndefined(e.serviceTypeCode)) {
                                return e;
                            }
                        }
                    })();
                    //oServerMap.highlightNodeByKey(selectedNode.key);
                    options.nBoldKey = selectedNode.key;
                    $rootScope.$broadcast("servermap.nodeClicked", null, query, selectedNode, data);
                } catch (e) {
                    console.log(e);
                }

                progressBar.setLoading(100);
                if (oServerMap === null) {
                    oServerMap = new ServerMap(options);
                } else {
                    oServerMap.option(options);
                }
                oServerMap.load(data.applicationMapData);
                progressBar.stopLoading();
            };

            var mergeUnknown = function (query, data) {
                SERVERMAP_METHOD_CACHE = {};
                var nodes = data.applicationMapData.nodeDataArray;
                var links = data.applicationMapData.linkDataArray;

                var inboundCountMap = {};
                nodes.forEach(function (node) {
                    if (!inboundCountMap[node.key]) {
                        inboundCountMap[node.key] = {
                            "sourceCount": 0,
                            "totalCallCount": 0
                        };
                    }

                    links.forEach(function (link) {
                        if (link.to === node.key) {
                            inboundCountMap[node.key].sourceCount++;
                            inboundCountMap[node.key].totalCallCount += link.text;
                        }
                    });
                });

                var newNodeList = [];
                var newLinkList = [];

                var removeNodeIdSet = {};
                var removeLinkIdSet = {};

                nodes.forEach(function (node, nodeIndex) {
                    if (node.category === "UNKNOWN_CLOUD") {
                        return;
                    }

                    var newNode;
                    var newLink;
                    var newNodeKey = "UNKNOWN_GROUP_" + node.key;

                    var unknownCount = 0;
                    links.forEach(function (link, linkIndex) {
                        if (link.from == node.key &&
                            link.targetinfo.serviceType == "UNKNOWN_CLOUD" &&
                            inboundCountMap[link.to] && inboundCountMap[link.to].sourceCount == 1) {
                            unknownCount++;
                        }
                    });
                    if (unknownCount < 2) {
                        return;
                    }

                    // for each children.
                    links.forEach(function (link, linkIndex) {
                        if (link.targetinfo.serviceType != "UNKNOWN_CLOUD") {
                            return;
                        }
                        if (inboundCountMap[link.to] && inboundCountMap[link.to].sourceCount > 1) {
                            return;
                        }

                        // branch out from current node.
                        if (link.from == node.key) {
                            if (!newNode) {
                                newNode = {
                                    "id": newNodeKey,
                                    "key": newNodeKey,
                                    "textArr": [],
                                    "text": "",
                                    "hosts": [],
                                    "category": "UNKNOWN_GROUP",
                                    "terminal": "true",
                                    "agents": [],
                                    "fig": "Rectangle"
                                };
                            }
                            if (!newLink) {
                                newLink = {
                                    "id": node.key + "-" + newNodeKey,
                                    "from": node.key,
                                    "to": newNodeKey,
                                    "sourceinfo": [],
                                    "targetinfo": [],
                                    "text": 0,
                                    "error": 0,
                                    "slow": 0,
                                    "rawdata": [],
                                    "histogram": {}
                                };
                            }

                            // fill the new node/link informations.
                            newNode.textArr.push({ 'count': link.text, 'applicationName': link.targetinfo.applicationName});

                            newLink.text += link.text;
                            newLink.error += link.error;
                            newLink.slow += link.slow;
                            newLink.sourceinfo.push(link.sourceinfo);
                            newLink.targetinfo.push(link.targetinfo);

                            var newRawData = {
                                "id": link.id,
                                "from": link.from,
                                "to": link.to,
                                "sourceinfo": link.sourceinfo,
                                "targetinfo": link.targetinfo,
                                "text": 0,
                                "count": link.text,
                                "error": link.error,
                                "slow": link.slow,
                                "histogram": link.histogram
                            };
                            newLink.rawdata[link.targetinfo.applicationName] = newRawData;

                            /*
                             * group된 노드에서 개별 노드의 정보를 조회할 때 사용됨.
                             * onclick="SERVERMAP_METHOD_CACHE['{{=
                             * value.applicationName}}']();" 으로 호출함.
                             */
                            SERVERMAP_METHOD_CACHE[link.targetinfo.applicationName] = function () {
                                linkClickHandler(null, query, newRawData);
                            }

                            $.each(link.histogram, function (key, value) {
                                if (newLink.histogram[key]) {
                                    newLink.histogram[key] += value;
                                } else {
                                    newLink.histogram[key] = value;
                                }
                            });

                            removeNodeIdSet[link.to] = null;
                            removeLinkIdSet[link.id] = null;
                        }
                    });

                    if (newNode) {
                        newNode.textArr.sort(function (e1, e2) {
                            return e2.count - e1.count;
                        });

                        var nodeCount = newNode.textArr.length - 1;
                        $.each(newNode.textArr, function (i, e) {
                            newNode.text += e.applicationName + " (" + e.count + ")" + (i < nodeCount ? "\n" : "");
                        });

//						console.log("newNode", newNode);
                        newNodeList.push(newNode);
                    }

                    if (newLink) {
                        if ((newLink.error / newLink.text * 100) > 10) {
                            newLink.category = "bad";
                        } else {
                            newLink.category = "default";
                        }

                        // targetinfo 에러를 우선으로, 요청수 내림차순 정렬.
                        newLink.targetinfo.sort(function (e1, e2) {
                            var err1 = newLink.rawdata[e1.applicationName].error;
                            var err2 = newLink.rawdata[e2.applicationName].error;

                            if (err1 + err2 > 0) {
                                return err2 - err1;
                            } else {
                                return newLink.rawdata[e2.applicationName].count - newLink.rawdata[e1.applicationName].count;
                            }
                        });

//						console.log("newLink", newLink);
                        newLinkList.push(newLink);
                    }
                });

                newNodeList.forEach(function (newNode) {
                    data.applicationMapData.nodeDataArray.push(newNode);
                });

                newLinkList.forEach(function (newLink) {
                    data.applicationMapData.linkDataArray.push(newLink);
                });

                $.each(removeNodeIdSet, function (key, val) {
                    nodes.forEach(function (node, i) {
                        if (node.id == key) {
                            nodes.splice(i, 1);
                        }
                    });
                });

                $.each(removeLinkIdSet, function (key, val) {
                    links.forEach(function (link, i) {
                        if (link.id === key) {
                            links.splice(i, 1);
                        }
                    });
                });
            };

            // TODO 임시코드로 나중에 USER와 backend를 구분할 예정.
            var replaceClientToUser = function (data) {
                var nodes = data.applicationMapData.nodeDataArray;
                nodes.forEach(function (node) {
                    if (node.category === "CLIENT") {
                        node.category = "USER";
                        node.text = "USER";
                    }
                });
            };

            var setLinkOption = function (data, linkRouting, linkCurve) {
                var links = data.applicationMapData.linkDataArray;
                links.forEach(function (link) {
                    link.routing = linkRouting;
                    link.curve = linkCurve;
                });
            };

            scope.toggleMergeUnknowns = function () {
                scope.mergeUnknowns = (scope.mergeUnknowns) ? false : true;
                showServerMap(scope.navbar.applicationName, scope.navbar.serviceType, scope.navbar.queryEndTime, scope.navbar.queryPeriod, scope.filter, scope.mergeUnknowns, scope.hideIndirectAccess, scope.linkRouting, scope.linkCurve);
                reset();
            };
            scope.toggleHideIndirectAccess = function () {
                scope.hideIndirectAccess = (scope.hideIndirectAccess) ? false : true;
                showServerMap(scope.navbar.applicationName, scope.navbar.serviceType, scope.navbar.queryEndTime, scope.navbar.queryPeriod, scope.filter, scope.mergeUnknowns, scope.hideIndirectAccess, scope.linkRouting, scope.linkCurve);
                reset();
            };
            scope.toggleLinkLableTextType = function (type) {
                scope.totalRequestCount = (type !== 'tps') ? true : false;
                scope.tps = (type === 'tps') ? true : false;
                showServerMap(scope.navbar.applicationName, scope.navbar.serviceType, scope.navbar.queryEndTime, scope.navbar.queryPeriod, scope.filter, scope.mergeUnknowns, scope.hideIndirectAccess, scope.linkRouting, scope.linkCurve);
                reset();
            };
            scope.toggleLinkRouting = function (type) {
                scope.linkRouting = cfg.options.htLinkType.sRouting = type;
                showServerMap(scope.navbar.applicationName, scope.navbar.serviceType, scope.navbar.queryEndTime, scope.navbar.queryPeriod, scope.filter, scope.mergeUnknowns, scope.hideIndirectAccess, scope.linkRouting, scope.linkCurve);
                reset();
            };
            scope.toggleLinkCurve = function (type) {
                scope.linkCurve = cfg.options.htLinkType.sCurve = type;
                showServerMap(scope.navbar.applicationName, scope.navbar.serviceType, scope.navbar.queryEndTime, scope.navbar.queryPeriod, scope.filter, scope.mergeUnknowns, scope.hideIndirectAccess, scope.linkRouting, scope.linkCurve);
                reset();
            };

            scope.$on('navbar.applicationChanged', function (event, data) {
                scope.navbar = data;
                scope.bShowServerMapStatus = true;
                bUseNodeContextMenu = bUseLinkContextMenu = bUseBackgroundContextMenu = true;
                showServerMap(data.applicationName, data.serviceType, data.queryEndTime, data.queryPeriod, scope.filter, scope.mergeUnknowns, scope.hideIndirectAccess, scope.linkRouting, scope.linkCurve);
            });
            scope.$on('servermap.initializeWithApplicationData', function (event, applicationData) {
                scope.navbar = applicationData;
                scope.bShowServerMapStatus = true;
                bUseNodeContextMenu = bUseLinkContextMenu = bUseBackgroundContextMenu = true;
                showServerMap(applicationData.applicationName, applicationData.serviceType, applicationData.queryEndTime, applicationData.queryPeriod, scope.filter, scope.mergeUnknowns, scope.hideIndirectAccess, scope.linkRouting, scope.linkCurve);
            });
            scope.$on('servermap.initializeWithMapData', function (event, mapData) {
                scope.bShowServerMapStatus = false;
                bUseNodeContextMenu = bUseLinkContextMenu = bUseBackgroundContextMenu = false;
                var query = {
                    applicationName: mapData.agentId
                };
                serverMapCallback(query, mapData, false, scope.linkRouting, scope.linkCurve);
            });

            scope.mergeUnknowns = true;
            scope.totalRequestCount = true;
            scope.bShowServerMapStatus = false;
            scope.linkRouting = cfg.options.htLinkType.sRouting;
            scope.linkCurve = cfg.options.htLinkType.sCurve;
        }
    };
}]);
