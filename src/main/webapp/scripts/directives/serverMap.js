'use strict';

pinpointApp.constant('serverMapConfig', {
    serverMapDataUrl: '/getServerMapData.pinpoint',
    filteredServerMapDataUrl: '/getFilteredServerMapData.pinpoint',
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
    FILTER_FETCH_LIMIT: 3
});

pinpointApp.directive('serverMap', [ 'serverMapConfig', '$rootScope', '$window', 'Alerts', 'ProgressBar', function (cfg, $rootScope, $window, Alerts, ProgressBar) {
    return {
        restrict: 'EA',
        replace: true,
        templateUrl: 'views/serverMap.html',
        link: function postLink(scope, element, attrs) {

            // define private variables
            var serverMapCachedQuery, serverMapCachedData, bUseNodeContextMenu, bUseLinkContextMenu,
                bUseBackgroundContextMenu, oServerMap, SERVERMAP_METHOD_CACHE, oAlert, oProgressBar, htLastMapData;

            // define private variables of methods
            var showServerMap, getServerMapData, getFilteredServerMapData, reset, setNodeContextMenuPosition,
                setLinkContextMenuPosition, setBackgroundContextMenuPosition, serverMapCallback, mergeUnknown,
                setLinkOption, mergeFilteredMapData, findExistingNodeFromLastMapData, mergeNodeData,
                findExistingLinkFromLastMapData, mergeLinkData;

            // initialize
            oServerMap = null;
            SERVERMAP_METHOD_CACHE = {};
            htLastMapData = {
                applicationMapData : {
                    linkDataArray : [],
                    nodeDataArray : []
                },
                lastFetchedTimestamp : []
            };
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
                if (oServerMap) {
                    oServerMap.clear();
                }
                oProgressBar.setLoading(10);

                var query = {
                    applicationName: applicationName,
                    serviceType: serviceType,
                    from: to - period,
                    to: to,
                    period: period,
                    filter: filterText
                };

                if (filterText) {
                    getFilteredServerMapData(query, function (query, result) {
                        if (query.from === result.lastFetchedTimestamp) {
                            scope.$emit('serverMap.allFetched');
                        } else {
                            htLastMapData.lastFetchedTimestamp = result.lastFetchedTimestamp - 1;
                            scope.$emit('serverMap.fetched', htLastMapData.lastFetchedTimestamp, result.applicationMapData.nodeDataArray.length);
                        }
                        serverMapCallback(query, mergeFilteredMapData(result), mergeUnknowns, linkRouting, linkCurve);
                    });
                } else {
                    getServerMapData(query, function (query, result) {
                        serverMapCallback(query, result, mergeUnknowns, linkRouting, linkCurve);
                    });
                }
            };

            /**
             * merge filtered map data
             * @param mapData
             * @returns {{applicationMapData: {linkDataArray: Array, nodeDataArray: Array}, lastFetchedTimestamp: Array}}
             */
            mergeFilteredMapData = function (mapData) {
                if (htLastMapData.applicationMapData.linkDataArray.length === 0 && htLastMapData.applicationMapData.nodeDataArray.length === 0) {
                    htLastMapData.applicationMapData.linkDataArray = mapData.applicationMapData.linkDataArray;
                    htLastMapData.applicationMapData.nodeDataArray = mapData.applicationMapData.nodeDataArray;
                } else {
                    var newKey = {};
                    angular.forEach(mapData.applicationMapData.nodeDataArray, function (node, key) {
                        var foundNodeKeyFromLastMapData = findExistingNodeFromLastMapData(node);
                        if (foundNodeKeyFromLastMapData) {
                            mergeNodeData(foundNodeKeyFromLastMapData - 1, node);
                            newKey[node.key] = foundNodeKeyFromLastMapData;
                        } else {
                            node.key = node.id = newKey[node.key] = htLastMapData.applicationMapData.nodeDataArray.length + 1;
                            htLastMapData.applicationMapData.nodeDataArray.push(node);
                        }
                    });
                    angular.forEach(mapData.applicationMapData.linkDataArray, function (link, key) {
                        var foundLinkKeyFromLastMapData = findExistingLinkFromLastMapData(link, newKey);
                        if (foundLinkKeyFromLastMapData) {
                            mergeLinkData(foundLinkKeyFromLastMapData, link);
                        } else {
                            link.from = newKey[link.from];
                            link.to = newKey[link.to];
                            link.id = [link.from, '-', link.to].join('');
                            htLastMapData.applicationMapData.linkDataArray.push(link);
                        }
                    });
                }
                return htLastMapData;
            };

            /**
             * find existing node from last map data
             * @param node
             * @returns {*}
             */
            findExistingNodeFromLastMapData = function (node) {
                for (var key in htLastMapData.applicationMapData.nodeDataArray) {
                    if (htLastMapData.applicationMapData.nodeDataArray[key].text === node.text && htLastMapData.applicationMapData.nodeDataArray[key].serviceTypeCode === node.serviceTypeCode) {
                        return htLastMapData.applicationMapData.nodeDataArray[key].key;
                    }
                }
                return false;
            };

            /**
             * merge node data
             * @param nodeKey
             * @param node
             */
            mergeNodeData = function (nodeKey, node) {
                for (var key in node.serverList) {
//                    if (angular.isUndefined(htLastMapData.applicationMapData.nodeDataArray[nodeKey])) {
//                        htLastMapData.applicationMapData.nodeDataArray[nodeKey] = {
//                            serverList: []
//                        };
//                    }
                    if (htLastMapData.applicationMapData.nodeDataArray[nodeKey].serverList[key]) {
                        for (var innerKey in node.serverList[key].instanceList) {
                            if (htLastMapData.applicationMapData.nodeDataArray[nodeKey].serverList[key].instanceList[innerKey]) {
                                for (var insideKey in node.serverList[key].instanceList[innerKey].histogram) {
                                    if (htLastMapData.applicationMapData.nodeDataArray[nodeKey].serverList[key].instanceList[innerKey].histogram[insideKey]) {
                                        htLastMapData.applicationMapData.nodeDataArray[nodeKey].serverList[key].instanceList[innerKey].histogram[insideKey] += node.serverList[key].instanceList[innerKey].histogram[insideKey];
                                    } else {
                                        htLastMapData.applicationMapData.nodeDataArray[nodeKey].serverList[key].instanceList[innerKey].histogram[insideKey] = node.serverList[key].instanceList[innerKey].histogram[insideKey];
                                    }
                                }
                            } else {
                                htLastMapData.applicationMapData.nodeDataArray[nodeKey].serverList[key].instanceList[innerKey] = node.serverList[key].instanceList[innerKey];
                            }
                        }
                    } else {
                        htLastMapData.applicationMapData.nodeDataArray[nodeKey].serverList[key] = node.serverList[key];
                    }
                }
            };

            /**
             * find existing link from last map data
             * @param link
             * @param newKey
             * @returns {*}
             */
            findExistingLinkFromLastMapData = function (link, newKey) {
                for (var key in htLastMapData.applicationMapData.linkDataArray) {
                    console.log(htLastMapData.applicationMapData.linkDataArray[key].from , newKey[link.from] , htLastMapData.applicationMapData.linkDataArray[key].to , newKey[link.to]);
                    if (htLastMapData.applicationMapData.linkDataArray[key].from === newKey[link.from] && htLastMapData.applicationMapData.linkDataArray[key].to === newKey[link.to]) {
                        return key;
                    }
                }
                return false;
            };

            /**
             * merge link data
             * @param linkKey
             * @param link
             */
            mergeLinkData = function (linkKey, link) {
                htLastMapData.applicationMapData.linkDataArray[linkKey].text += link.text;
                htLastMapData.applicationMapData.linkDataArray[linkKey].error += link.error;
                htLastMapData.applicationMapData.linkDataArray[linkKey].slow += link.slow;
                for (var key in link.histogram) {
                    if (htLastMapData.applicationMapData.linkDataArray[linkKey].histogram[key]) {
                        htLastMapData.applicationMapData.linkDataArray[linkKey].histogram[key] += link.histogram[key];
                    } else {
                        htLastMapData.applicationMapData.linkDataArray[linkKey].histogram[key] = link.histogram[key];
                    }
                }
                for (var key in link.targetHosts) {
                    if (htLastMapData.applicationMapData.linkDataArray[linkKey].targetHosts[key]) {
                        for (var innerKey in link.targetHosts[key].histogram) {
                            if (htLastMapData.applicationMapData.linkDataArray[linkKey].targetHosts[key].histogram[innerKey]) {
                                htLastMapData.applicationMapData.linkDataArray[linkKey].targetHosts[key].histogram[innerKey] += link.targetHosts[key].histogram[innerKey];
                            } else {
                                htLastMapData.applicationMapData.linkDataArray[linkKey].targetHosts[key].histogram[innerKey] = link.targetHosts[key].histogram[innerKey];
                            }
                        }
                    } else {
                        htLastMapData.applicationMapData.linkDataArray[linkKey].targetHosts[key] = link.targetHosts[key];
                    }
                }
            };

            /**
             * get server map data 2
             * @param query
             * @param callback
             */
            getServerMapData = function (query, callback) {
                oProgressBar.setLoading(50);
                jQuery.ajax({
                    type: 'GET',
                    url: cfg.serverMapDataUrl,
                    cache: false,
                    dataType: 'json',
                    data: {
                        application: query.applicationName,
                        serviceType: query.serviceType,
                        from: query.from,
                        to: query.to
                    },
                    success: function (result) {
                        oProgressBar.setLoading(30);
                        callback(query, result);
                    },
                    error: function (xhr, status, error) {
                        console.log("ERROR", status, error);
                        oProgressBar.stopLoading();
                        oAlert.showError('There is some error.');
                    }
                });
            };

            /**
             * get filtered server map data
             * @param query
             * @param callback
             */
            getFilteredServerMapData = function (query, callback) {
                oProgressBar.setLoading(30);
                jQuery.ajax({
                    type: 'GET',
                    url: cfg.filteredServerMapDataUrl,
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
                        oProgressBar.stopLoading();
                        oAlert.showError('There is some error.');
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
             * @param data
             * @param mergeUnknowns
             * @param linkRouting
             * @param linkCurve
             */
            serverMapCallback = function (query, data, mergeUnknowns, linkRouting, linkCurve) {
                serverMapCachedQuery = angular.copy(query);
                serverMapCachedData = angular.copy(data);
                oProgressBar.setLoading(80);
                if (data.applicationMapData.nodeDataArray.length === 0) {
                    oProgressBar.stopLoading();
                    oAlert.showInfo('There is no data.');
                    return;
                }

                if (mergeUnknowns) {
                    mergeUnknown(query, data);
                }

                setLinkOption(data, linkRouting, linkCurve);
                oProgressBar.setLoading(90);

                var options = cfg.options;
                options.fOnNodeContextClicked = function (e, node) {
                    scope.$emit("serverMap.nodeContextClicked", e, query, node, data);
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
                    scope.$emit("serverMap.linkContextClicked", e, query, link, data);
                    reset();
                    scope.link = link;
                    scope.nodeCategory = link.category || '';
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
                    scope.$emit("serverMap.linkClicked", e, query, link, data);
                    reset();
                };
                options.fOnNodeClicked = function (e, node) {
                    scope.$emit("serverMap.nodeClicked", e, query, node, data);
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
                    scope.$emit("serverMap.nodeClicked", null, query, selectedNode, data);
                } catch (e) {
                    console.log(e);
                }

                oProgressBar.setLoading(100);
                if (oServerMap === null) {
                    oServerMap = new ServerMap(options);
                } else {
                    oServerMap.option(options);
                }
                oServerMap.load(data.applicationMapData);
                oProgressBar.stopLoading();
            };

            /**
             * merge unknown
             * @param query
             * @param data
             */
            mergeUnknown = function (query, data) {
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

            /**
             * set link option
             * @param data
             * @param linkRouting
             * @param linkCurve
             */
            setLinkOption = function (data, linkRouting, linkCurve) {
                var links = data.applicationMapData.linkDataArray;
                links.forEach(function (link) {
                    link.routing = linkRouting;
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
             * scope passing transaction map
             */
            scope.passingTransactionMap = function () {
                var application = scope.oNavbarDao.getApplication(),
                    period = scope.oNavbarDao.getPeriod(),
                    queryEndTime = scope.oNavbarDao.getQueryEndTime(),
                    srcServiceType = scope.srcServiceType,
                    srcApplicationName = scope.srcApplicationName,
                    destServiceType = scope.destServiceType,
                    destApplicationName = scope.destApplicationName,
                    prevFilter = scope.oNavbarDao.getFilter();

                var newFilter = ((prevFilter) ? prevFilter + cfg.FILTER_DELIMETER : "")
                    + srcServiceType + cfg.FILTER_ENTRY_DELIMETER
                    + srcApplicationName + cfg.FILTER_ENTRY_DELIMETER
                    + destServiceType + cfg.FILTER_ENTRY_DELIMETER
                    + destApplicationName;

                var url = '#/filteredMap/' + application + '/' + period + '/' + queryEndTime + '/' + newFilter;
                $window.open(url, "");
                reset();
            };

            /**
             * toggle merge unknowns
             */
            scope.toggleMergeUnknowns = function () {
                scope.mergeUnknowns = (scope.mergeUnknowns) ? false : true;
                showServerMap(scope.oNavbarDao.getApplicationName(), scope.oNavbarDao.getServiceType(), scope.oNavbarDao.getQueryEndTime(), scope.oNavbarDao.getQueryPeriod(), scope.filter, scope.mergeUnknowns, scope.linkRouting, scope.linkCurve);
                reset();
            };

            /**
             * scope toggle link lable text type
             * @param type
             */
            scope.toggleLinkLableTextType = function (type) {
                scope.totalRequestCount = (type !== 'tps') ? true : false;
                scope.tps = (type === 'tps') ? true : false;
                showServerMap(scope.oNavbarDao.getApplicationName(), scope.oNavbarDao.getServiceType(), scope.oNavbarDao.getQueryEndTime(), scope.oNavbarDao.getQueryPeriod(), scope.filter, scope.mergeUnknowns, scope.linkRouting, scope.linkCurve);
                reset();
            };

            /**
             * toggle link routing
             * @param type
             */
            scope.toggleLinkRouting = function (type) {
                scope.linkRouting = cfg.options.htLinkType.sRouting = type;
                showServerMap(scope.oNavbarDao.getApplicationName(), scope.oNavbarDao.getServiceType(), scope.oNavbarDao.getQueryEndTime(), scope.oNavbarDao.getQueryPeriod(), scope.filter, scope.mergeUnknowns, scope.linkRouting, scope.linkCurve);
                reset();
            };

            /**
             * toggle link curve
             * @param type
             */
            scope.toggleLinkCurve = function (type) {
                scope.linkCurve = cfg.options.htLinkType.sCurve = type;
                showServerMap(scope.oNavbarDao.getApplicationName(), scope.oNavbarDao.getServiceType(), scope.oNavbarDao.getQueryEndTime(), scope.oNavbarDao.getQueryPeriod(), scope.filter, scope.mergeUnknowns, scope.linkRouting, scope.linkCurve);
                reset();
            };

            /**
             * scope event on serverMap.initialize
             */
            scope.$on('serverMap.initialize', function (event, navbarDao) {
                scope.oNavbarDao = navbarDao;
                scope.bShowServerMapStatus = true;
                bUseNodeContextMenu = bUseLinkContextMenu = bUseBackgroundContextMenu = true;
                showServerMap(navbarDao.getApplicationName(), navbarDao.getServiceType(), navbarDao.getQueryEndTime(), navbarDao.getQueryPeriod(), navbarDao.getFilter(), scope.mergeUnknowns, scope.linkRouting, scope.linkCurve);
            });

            /**
             * scope event on serverMap.fetch
             */
            scope.$on('serverMap.fetch', function (event, queryPeriod, queryEndTime) {
                showServerMap(scope.oNavbarDao.getApplicationName(), scope.oNavbarDao.getServiceType(), queryEndTime, queryPeriod, scope.oNavbarDao.getFilter(), scope.mergeUnknowns, scope.linkRouting, scope.linkCurve);
            });

            /**
             * scope event on serverMap.initializeWithMapData
             */
            scope.$on('serverMap.initializeWithMapData', function (event, mapData) {
                scope.bShowServerMapStatus = false;
                bUseNodeContextMenu = bUseLinkContextMenu = bUseBackgroundContextMenu = false;
                var query = {
                    applicationName: mapData.agentId
                };
                serverMapCallback(query, mapData, false, scope.linkRouting, scope.linkCurve);
            });

        }
    };
}]);
