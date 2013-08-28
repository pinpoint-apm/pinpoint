'use strict';

pinpointApp.constant('config', {
    applicationUrl: '/applications.pinpoint',
    filtermapUrl: '/filtermap.pinpoint',
    lastTransactionListUrl: '/lastTransactionList.pinpoint',
    transactionListUrl: '/transactionList.pinpoint',
    options: {
        "sContainerId": 'servermap',
        "sImageDir": '/images/icons/',
        "sBigFont": "12pt calibri, Helvetica, Arial, sans-serif",
        "sSmallFont": "11pt calibri, Helvetica, Arial, sans-serif",
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
        "htLinkTheme": {
            "default": {
                "background": { 0: "rgb(240, 240, 240)", 0.3: "rgb(240, 240, 240)", 1: "rgba(240, 240, 240, 1)"},
                "border": "gray",
                "font": "10pt calibri, helvetica, arial, sans-serif",
                "color": "#000000", // "#919191",
                "align": "center",
                "margin": 1
            },
            "good": {
                // "background" : { 0: "rgb(240, 1, 240)", 0.3: "rgb(240, 1,
                // 240)", 1: "rgba(240, 1, 240, 1)"},
                "background": { 0: "#2CA02C"},
                "border": "green",
                "font": "10pt calibri, helvetica, arial, sans-serif",
                "color": "#919191",
                "align": "center",
                "margin": 1
            },
            "bad": {
                // "background" : { 0: "rgb(214, 27, 28)", 0.3: "rgb(214, 27,
                // 28)", 1: "rgba(214, 27, 28, 1)"},
                // "background" : { 0: "#D62728" },
                "background": { 0: "rgba(200, 27, 28, 1)" },
                "border": "#FFFFFF",
                "font": "10pt calibri, helvetica, arial, sans-serif",
                "color": "#FFFFFF",
                "align": "center",
                "margin": 1
            }
        }
    }
});

pinpointApp.directive('servermap', [ 'config', '$rootScope', '$templateCache', '$compile', '$timeout', function (config, $rootScope, $templateCache, $compile, $timeout) {
    return {
        restrict: 'EA',
        replace: true,
        templateUrl: 'views/servermap.html',
        link: function postLink(scope, element, attrs, ctrl) {
            var serverMapCachedQuery, serverMapCachedData;
            var oServerMap = null;
            var FILTER_DELIMETER = "^";
            var FILTER_ENTRY_DELIMETER = "|";
            var SERVERMAP_METHOD_CACHE = {};
            var myColors = ["#008000", "#4B72E3", "#A74EA7", "#BB5004", "#FF0000"];

            console.log('controller', ctrl);

            /**
             * loading
             */
            var startLoading = function () {
                setLoading(0);
                $timeout(function () {
                    $('.servermap .progress').show();
                });
            };
            var stopLoading = function () {
                $timeout(function () {
                    $('.servermap .progress').hide();
                }, 300);
            };
            var setLoading = function (p) {
                $('.servermap .progress .bar').width(p + '%');
            };

            var showWarning = function (msg) {
                $timeout(function () {
                    $('.servermap .warning').show();
                    $('.servermap .warning .msg').text(msg);
                }, 300);
            };

            var showServerMap = function (applicationName, serviceType, to, period, filterText, hideIndirectAccess) {
                startLoading();
                if (oServerMap) {
                    oServerMap.clear();
                }
                setLoading(10);

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
// getFilteredServerMapData(query, function(query, result) {
// if (cb) {
// cb(query, result);
// }
// serverMapCallback(query, result);
// });
                } else {
                    getServerMapData2(query, function (query, result) {
                        serverMapCallback(query, result);
                    });
                }
            };

            var getServerMapData2 = function (query, callback) {
                setLoading(50);
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
                        setLoading(30);
                        callback(query, result);
                    },
                    error: function (xhr, status, error) {
                        console.log("ERROR", status, error);
                        stopLoading();
                        showWarning('There is some error.');
                    }
                });
            };

//			var getFilteredServerMapData = function (query, callback) {
//				jQuery.ajax({
//					type: 'GET',
//					url: '/getFilteredServerMapData2.pinpoint',
//					cache: false,
//					dataType: 'json',
//					data: {
//						application: query.applicationName,
//						serviceType: query.serviceType,
//						from: query.from,
//						to: query.to,
//						filter: query.filter
//					},
//					success: function (result) {
//						callback(query, result);
//					},
//					error: function (xhr, status, error) {
//						console.log("ERROR", status, error);
//					}
//				});
//			};

            var reset = function () {
                scope.linkContextMenuStyle = '';
                scope.$digest();
            };
            var setLinkContextMenuPostition = function (top, left) {
                scope.linkContextMenuStyle = {
                    display: 'block'
                };
                var linkContextMenu = element.find('.linkContextMenu');
                linkContextMenu.css({
                    'top': top,
                    'left' : left
                });
                scope.$digest();
            };

            scope.passingTransactionMap = function () {
                var applicationName = scope.navbar.applicationName,
                    serviceType = scope.navbar.serviceType,
                    begin = scope.navbar.queryStartTime,
                    end = scope.navbar.queryEndTime,
                    srcServiceType = scope.srcServiceType,
                    srcApplicationName = scope.srcApplicationName,
                    destServiceType = scope.destServiceType,
                    destApplicationName = scope.destApplicationName,
                    prevFilter = scope.filter;

                if (srcServiceType === "CLIENT") {
                    applicationName = srcApplicationName = destApplicationName;
                }
//                TOMCAT|books.api.dev|ORACLE|XDEV01
//?application=books.api.dev&serviceType=1010&from=1377193800484&to=1377238200484&filter=CLIENT|books.api.dev|TOMCAT|books.api.dev
                var params = {
                    "application" : applicationName,
                    "serviceType" : serviceType,
                    "from" : begin,
                    "to" : end,
                    "filter" : ((prevFilter) ? prevFilter + FILTER_DELIMETER : "")
                        + srcServiceType + FILTER_ENTRY_DELIMETER
                        + srcApplicationName + FILTER_ENTRY_DELIMETER
                        + destServiceType + FILTER_ENTRY_DELIMETER
                        + destApplicationName
                }
                window.open(config.filtermapUrl + "?" + decodeURIComponent($.param(params)), "");
            };
            scope.passingTransactionList = function () {
                var applicationName = scope.srcApplicationName,
                    from = scope.navbar.queryStartTime,
                    to = scope.navbar.queryEndTime,
                    period = scope.navbar.queryPeriod,
                    usePeriod = scope.usePeriod,
                    filter = scope.srcServiceType + '|' + scope.srcApplicationName + '|' + scope.destServiceType + '|' + scope.destApplicationName;

                if (scope.srcServiceType === 'CLIENT') {
                    applicationName =   scope.destServiceType;
                    filter = scope.srcServiceType + '|' + scope.destApplicationName + '|' + scope.destServiceType + '|' + scope.destApplicationName;
                }

                if (usePeriod) {
                    window.open(config.lastTransactionListUrl + "?application=" + applicationName + "&period=" + period + ( filter ? "&filter=" + filter : "") );
                } else {
                    window.open(config.transactionListUrl + "?application=" + applicationName + "&from=" + from + "&to=" + to + ( filter ? "&filter=" + filter : "") );
                }
            };

            var serverMapCallback = function (query, data, ignoreCache) {
                var containerId = config.options.sContainerId;

                serverMapCachedQuery = angular.copy(query);
                serverMapCachedData = angular.copy(data);
                setLoading(80);
                if (data.applicationMapData.nodeDataArray.length === 0) {
                    stopLoading();
                    showWarning('There is no data.');
// warning("NO DATA", "");
                    return;
                } else {
// clearAllWarnings();
//					$("#" + containerId).show();
                }

// if ($('#mergeUnknown').data('selected')) {
// // if ($('#mergeUnknown').is(':checked')) {
// mergeUnknown(query, data);
// }
                mergeUnknown(query, data);
                replaceClientToUser(data);
                setLoading(90);

                var options = config.options;
                options.fOnNodeContextClick = function (e, d) {
                    $rootScope.$broadcast("servermap.nodeContextClicked", e, query, d);
                    reset();
                };
                options.fOnLinkContextClick = function (e, d) {
                    $rootScope.$broadcast("servermap.linkContextClicked", e, query, d);
                    reset();
                    setLinkContextMenuPostition(e.layerY, e.layerX);
                    scope.srcServiceType = d.sourceinfo.serviceType;
                    scope.srcApplicationName = d.sourceinfo.applicationName;
                    scope.destServiceType = d.targetinfo.serviceType;
                    scope.destApplicationName = d.targetinfo.applicationName;
                };
                options.fOnLinkClick = function (e, d) {
                    $rootScope.$broadcast("servermap.linkClicked", e, query, d);
                    reset();
                };
                options.fOnNodeClick = function (e, d) {
                    $rootScope.$broadcast("servermap.nodeClicked", e, query, d);
                    reset();
                };
                options.fOnBackgroundClick = function (e) {
                    $rootScope.$broadcast("servermap.backgroundClicked", e, query);
                    reset();
                };

                setLoading(100);
                oServerMap = new ServerMap(options);
                oServerMap.load(data.applicationMapData);
                stopLoading();

                try {
                    var selectedNode = (function () {
                        for (var i = 0; i < data.applicationMapData.nodeDataArray.length; i++) {
                            var e = data.applicationMapData.nodeDataArray[i];
                            if (e.text === query.applicationName && e.serviceTypeCode === query.serviceType) {
                                return e;
                            }
                        }
                    })();
                    oServerMap.highlightNodeByKey(selectedNode.key);
//					nodeClickHandler(null, query, selectedNode);
                    $rootScope.$broadcast("servermap.nodeClicked", null, query, selectedNode);
                } catch (e) {
                    console.log(e);
                }
            };

            // TODO 임시코드로 나중에 USER와 backend를 구분할 예정.
            var replaceClientToUser = function (data) {
                var nodes = data.applicationMapData.nodeDataArray;
                nodes.forEach(function (node) {
                    if (node.category == "CLIENT") {
                        node.category = "USER";
                        node.text = "USER";
                    }
                });
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
                                    "fig": "FramedRectangle"
                                }
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

            scope.$on('navbar.applicationChanged', function (event, data) {
                console.log('got navbar.applicationChanged from servermap : ', data);
                scope.navbar = data;
                showServerMap(data.applicationName, data.serviceType, data.queryEndTime, data.queryPeriod, '', false);
            });


        }
    }
}]);
