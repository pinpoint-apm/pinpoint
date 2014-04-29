'use strict';

pinpointApp.constant('serverMapDaoConfig', {
    serverMapDataUrl: '/getServerMapData.pinpoint',
    filteredServerMapDataUrl: '/getFilteredServerMapData.pinpoint',
    filtermapUrl: '/filtermap.pinpoint',
    lastTransactionListUrl: '/lastTransactionList.pinpoint',
    transactionListUrl: '/transactionList.pinpoint',
    FILTER_DELIMETER: "^",
    FILTER_ENTRY_DELIMETER: "|",
    FILTER_FETCH_LIMIT: 5000
});

pinpointApp.service('ServerMapDao', [ 'serverMapDaoConfig', function ServerMapDao(cfg) {

    /**
     * get server map data
     * @param query
     * @param callback
     */
     this.getServerMapData = function (query, cb) {
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
                if (angular.isFunction(cb)) {
                    cb(null, query, result);
                }
            },
            error: function (xhr, status, error) {
                if (angular.isFunction(cb)) {
                    cb(error, query, {});
                }
            }
        });
    };

    /**
     * get filtered server map data
     * @param query
     * @param callback
     */
    this.getFilteredServerMapData = function (query, cb) {
        var data = {
            application: query.applicationName,
            serviceType: query.serviceType,
            from: query.from,
            to: query.to,
            originTo: query.originTo,
            filter: query.filter,
            limit: cfg.FILTER_FETCH_LIMIT
        };
        if (query.hint) {
            data.hint = query.hint;
        }
        jQuery.ajax({
            type: 'GET',
            url: cfg.filteredServerMapDataUrl,
            cache: false,
            dataType: 'json',
            data: data,
            success: function (result) {
                if (angular.isFunction(cb)) {
                    cb(null, query, result);
                }
            },
            error: function (xhr, status, error) {
                if (angular.isFunction(cb)) {
                    cb(error, query, {});
                }
            }
        });
    };

    /**
     *merge filtered map data
     * @param htLastMapData
     * @param mapData
     * @returns {*}
     */
    this.mergeFilteredMapData = function (htLastMapData, mapData) {
        if (htLastMapData.applicationMapData.linkDataArray.length === 0 && htLastMapData.applicationMapData.nodeDataArray.length === 0) {
            htLastMapData.applicationMapData.linkDataArray = mapData.applicationMapData.linkDataArray;
            htLastMapData.applicationMapData.nodeDataArray = mapData.applicationMapData.nodeDataArray;
        } else {
            var newKey = {};
            angular.forEach(mapData.applicationMapData.nodeDataArray, function (node, key) {
                var foundNodeKeyFromLastMapData = this.findExistingNodeKeyFromLastMapData(htLastMapData, node);
                if (foundNodeKeyFromLastMapData >= 0) {
                    this.mergeNodeData(htLastMapData, foundNodeKeyFromLastMapData, node);
                    newKey[node.key] = foundNodeKeyFromLastMapData;
                } else {
                    node.key = node.id = newKey[node.key] = htLastMapData.applicationMapData.nodeDataArray.length + 1;
                    htLastMapData.applicationMapData.nodeDataArray.push(node);
                }
            }, this);
            angular.forEach(mapData.applicationMapData.linkDataArray, function (link, key) {
                var foundLinkKeyFromLastMapData = this.findExistingLinkFromLastMapData(htLastMapData, link, newKey);
                if (foundLinkKeyFromLastMapData) {
                    this.mergeLinkData(htLastMapData, foundLinkKeyFromLastMapData, link);
                } else {
                    link.from = newKey[link.from];
                    link.to = newKey[link.to];
                    link.id = [link.from, '-', link.to].join('');
                    htLastMapData.applicationMapData.linkDataArray.push(link);
                }
            }, this);
        }
        return htLastMapData;
    };

    /**
     * find existing node from last map data
     * @param htLastMapData
     * @param node
     * @returns {*}
     */
    this.findExistingNodeKeyFromLastMapData = function (htLastMapData, node) {
        for (var key in htLastMapData.applicationMapData.nodeDataArray) {
            if (htLastMapData.applicationMapData.nodeDataArray[key].text === node.text && htLastMapData.applicationMapData.nodeDataArray[key].serviceTypeCode === node.serviceTypeCode) {
                return key;
            }
        }
        return false;
    };

    /**
     * find existing link from last map data
     * @param htLastMapData
     * @param link
     * @param newKey
     * @returns {*}
     */
    this.findExistingLinkFromLastMapData = function (htLastMapData, link, newKey) {
        for (var key in htLastMapData.applicationMapData.linkDataArray) {
            if (htLastMapData.applicationMapData.linkDataArray[key].from === newKey[link.from] && htLastMapData.applicationMapData.linkDataArray[key].to === newKey[link.to]) {
                return key;
            }
        }
        return false;
    };

    /**
     * add filter property
     * @param filterText
     * @param mapData
     * @returns {*}
     */
    this.addFilterProperty = function (filterText, mapData) {
        var parsedFilters = this.parseFilterText(filterText, mapData);

        angular.forEach(mapData.applicationMapData.linkDataArray, function (val, key) {
            if (angular.isDefined(_.findWhere(parsedFilters, {fromKey: val.from, toKey: val.to}))) {
                val.isFiltered = true;
            } else {
                val.isFiltered = false;
            }
        }, this);
        return mapData;
    };

    /**
     * parse filter text
     * @param filterText
     * @param mapData
     * @returns {Array}
     */
    this.parseFilterText = function (filterText, mapData) {
        var filters = JSON.parse(filterText),
            aFilter = [];

        angular.forEach(filters, function (filter) {
            aFilter.push({
                fromCategory: filter.fst,
                fromText: filter.fa,
                fromKey: this.findNodeKeyByText(filter.fa, mapData),
                toCategory: filter.tst,
                toText: filter.ta,
                toKey: this.findNodeKeyByText(filter.ta, mapData)
            })
        }, this);
        return aFilter;
    };

    /**
     * find node key by text
     * @param text
     * @param mapData
     * @returns {*}
     */
    this.findNodeKeyByText = function (text, mapData) {
        //if (text === 'CLIENT') {
        //    text = 'USER';
        //}
        var result = _.findWhere(mapData.applicationMapData.nodeDataArray, {text: text});
        if (angular.isDefined(result)) {
            return result.key;
        } else {
            return false;
        }
    };

    /**
     * merge node data
     * @param htLastMapData
     * @param nodeKey
     * @param node
     * @returns {*}
     */
    this.mergeNodeData = function (htLastMapData, nodeKey, node) {

        if (angular.isUndefined(htLastMapData.applicationMapData.nodeDataArray[nodeKey])) {
            htLastMapData.applicationMapData.nodeDataArray[nodeKey] = node;
            return htLastMapData;
        }

        if (angular.isDefined(node.histogram)) {
            for (var key in node.histogram) {
                if (angular.isDefined(htLastMapData.applicationMapData.nodeDataArray[nodeKey].histogram)) {
                    if (htLastMapData.applicationMapData.nodeDataArray[nodeKey].histogram[key]) {
                        htLastMapData.applicationMapData.nodeDataArray[nodeKey].histogram[key] += node.histogram[key];
                    } else {
                        htLastMapData.applicationMapData.nodeDataArray[nodeKey].histogram[key] = node.histogram[key];
                    }
                } else {
                    htLastMapData.applicationMapData.nodeDataArray[nodeKey].histogram = {};
                    htLastMapData.applicationMapData.nodeDataArray[nodeKey].histogram[key] = node.histogram[key];
                }
            }
        }

        if (angular.isDefined(node.agentHistogram)) {
            for (var key in node.agentHistogram) {
                if (angular.isDefined(htLastMapData.applicationMapData.nodeDataArray[nodeKey].agentHistogram[key])) {
                    for (var innerKey in node.agentHistogram[key]) {
                        if (angular.isDefined(htLastMapData.applicationMapData.nodeDataArray[nodeKey].agentHistogram[key][innerKey])) {
                            htLastMapData.applicationMapData.nodeDataArray[nodeKey].agentHistogram[key][innerKey] += node.agentHistogram[key][innerKey];
                        } else {
                            htLastMapData.applicationMapData.nodeDataArray[nodeKey].agentHistogram[key][innerKey] = node.agentHistogram[key][innerKey];
                        }
                    }
                } else {
                    htLastMapData.applicationMapData.nodeDataArray[nodeKey].agentHistogram[key] = node.agentHistogram[key];
                }
            }
        }

        if (angular.isDefined(node.timeSeriesHistogram)) {
            for (var key in node.timeSeriesHistogram) {
                if (angular.isDefined(htLastMapData.applicationMapData.nodeDataArray[nodeKey].timeSeriesHistogram)) {
                    var aTemp = [];
                    outer:
                    for (var innerKey in node.timeSeriesHistogram[key].values) {
                        for (var innerInnerKey in htLastMapData.applicationMapData.nodeDataArray[nodeKey].timeSeriesHistogram[key].values) {
                            if (htLastMapData.applicationMapData.nodeDataArray[nodeKey].timeSeriesHistogram[key].values[innerInnerKey][0] === node.timeSeriesHistogram[key].values[innerKey][0]) {
                                htLastMapData.applicationMapData.nodeDataArray[nodeKey].timeSeriesHistogram[key].values[innerInnerKey][1] += node.timeSeriesHistogram[key].values[innerKey][1];
                                continue outer;
                            }
                        }
                        aTemp.push(node.timeSeriesHistogram[key].values[innerKey]);
                    }
                    htLastMapData.applicationMapData.nodeDataArray[nodeKey].timeSeriesHistogram[key].values = aTemp.concat(htLastMapData.applicationMapData.nodeDataArray[nodeKey].timeSeriesHistogram[key].values);
                } else {
                    htLastMapData.applicationMapData.nodeDataArray[nodeKey].timeSeriesHistogram = node.timeSeriesHistogram;
                }
            }
        }

        if (angular.isDefined(node.agentTimeSeriesHistogram)) {
            for (var key in node.agentTimeSeriesHistogram) {
                if (angular.isDefined(htLastMapData.applicationMapData.nodeDataArray[nodeKey].agentTimeSeriesHistogram)) {
                    if (angular.isDefined(htLastMapData.applicationMapData.nodeDataArray[nodeKey].agentTimeSeriesHistogram[key])) {
                        for (var innerKey in node.agentTimeSeriesHistogram[key]) {
                            if (angular.isDefined(htLastMapData.applicationMapData.nodeDataArray[nodeKey].agentTimeSeriesHistogram[key][innerKey])) {
                                htLastMapData.applicationMapData.nodeDataArray[nodeKey].agentTimeSeriesHistogram[key][innerKey].values = node.agentTimeSeriesHistogram[key][innerKey].values.concat(htLastMapData.applicationMapData.nodeDataArray[nodeKey].agentTimeSeriesHistogram[key][innerKey].values);
                            } else {
                                htLastMapData.applicationMapData.nodeDataArray[nodeKey].agentTimeSeriesHistogram[key][innerKey] = node.agentTimeSeriesHistogram[key][innerKey];
                            }
                        }
                    } else {
                        htLastMapData.applicationMapData.nodeDataArray[nodeKey].agentTimeSeriesHistogram[key] = node.agentTimeSeriesHistogram[key];
                    }
                } else {
                    htLastMapData.applicationMapData.nodeDataArray[nodeKey].agentTimeSeriesHistogram = node.agentTimeSeriesHistogram;
                }
            }
        }

        if (angular.isDefined(node.serverList)) {
            for (var key in node.serverList) {
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
        }
        return htLastMapData;
    };

    /**
     * merge link data
     * @param htLastMapData
     * @param linkKey
     * @param link
     * @returns {*}
     */
    this.mergeLinkData = function (htLastMapData, linkKey, link) {

        if (angular.isUndefined(htLastMapData.applicationMapData.linkDataArray[linkKey])) {
            htLastMapData.applicationMapData.linkDataArray[linkKey] = link;
            return  htLastMapData;
        }

        htLastMapData.applicationMapData.linkDataArray[linkKey].text += link.text;
        htLastMapData.applicationMapData.linkDataArray[linkKey].error += link.error;
        htLastMapData.applicationMapData.linkDataArray[linkKey].slow += link.slow;

        if (angular.isDefined(link.histogram)) {
            for (var key in link.histogram) {
                if (htLastMapData.applicationMapData.linkDataArray[linkKey].histogram[key]) {
                    htLastMapData.applicationMapData.linkDataArray[linkKey].histogram[key] += link.histogram[key];
                } else {
                    htLastMapData.applicationMapData.linkDataArray[linkKey].histogram[key] = link.histogram[key];
                }
            }
        }

        if (angular.isDefined(link.sourceHistogram)) {
            for (var key in link.sourceHistogram) {
                for (var innerKey in link.sourceHistogram[key]) {
                    if (htLastMapData.applicationMapData.linkDataArray[linkKey].sourceHistogram[key][innerKey]) {
                        htLastMapData.applicationMapData.linkDataArray[linkKey].sourceHistogram[key][innerKey] += link.sourceHistogram[key][innerKey];
                    } else {
                        htLastMapData.applicationMapData.linkDataArray[linkKey].sourceHistogram[key][innerKey] = link.sourceHistogram[key][innerKey];
                    }
                }
            }
        }

        if (angular.isDefined(link.targetHosts)) {
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
        }
        return htLastMapData;
    };

    /**
     * merge time series responses
     * @param htLastMapData
     * @param timeSeriesResponses
     * @returns {*}
     */
    this.mergeTimeSeriesResponses = function (htLastMapData, timeSeriesResponses) {
        angular.forEach(timeSeriesResponses.values, function (val, key) {
            if (angular.isUndefined(htLastMapData.timeSeriesResponses.values[key])) {
                htLastMapData.timeSeriesResponses.values[key] = val;
            } else {
                htLastMapData.timeSeriesResponses.values[key] = _.union(val, htLastMapData.timeSeriesResponses.values[key]);
            }
        }, this);
        htLastMapData.timeSeriesResponses.time = _.union(timeSeriesResponses.time, htLastMapData.timeSeriesResponses.time);
        return htLastMapData;
    };



    /**
     * merge unknown
     * @param query
     * @param data
     * @returns {*}
     */
    this.mergeUnknown = function (query, data) {
//        SERVERMAP_METHOD_CACHE = {};
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

        function getNodeByApplicationName(applicationName) {
            for(var k in nodes) {
                if (applicationName === nodes[k].text) {
                    return nodes[k];
                }
            }
            return false;
        }

        nodes.forEach(function (node, nodeIndex) {
            if (node.category === "UNKNOWN") {
                return;
            }

            var newNode;
            var newLink;
            var newNodeKey = "UNKNOWN_GROUP_" + node.key;

            var unknownCount = 0;
            links.forEach(function (link, linkIndex) {
                if (link.from == node.key &&
                    link.targetInfo.serviceType == "UNKNOWN" &&
                    inboundCountMap[link.to] && inboundCountMap[link.to].sourceCount == 1) {
                    unknownCount++;
                }
            });
            if (unknownCount < 2) {
                return;
            }

            // for each children.
            links.forEach(function (link, linkIndex) {
                if (link.targetInfo.serviceType != "UNKNOWN") {
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
                            "targetRawData" : {},
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
                            "filterApplicationName": '',
                            "filterApplicationServiceTypeCode": '',
                            "sourceInfo": {},
                            "targetInfo": [],
                            "text": 0,
                            "error": 0,
                            "slow": 0,
                            "targetRawData": {},
                            "histogram": {}
                        };
                    }

                    // fill the new node/link informations.
                    newNode.textArr.push({ 'count': link.text, 'applicationName': link.targetInfo.applicationName});
                    newNode.targetRawData[link.targetInfo.applicationName] = getNodeByApplicationName(link.targetInfo.applicationName);

                    newLink.text += link.text;
                    newLink.error += link.error;
                    newLink.slow += link.slow;
                    newLink.filterApplicationName = link.filterApplicationName;
                    newLink.filterApplicationServiceTypeCode = link.filterApplicationServiceTypeCode;
                    newLink.sourceInfo = link.sourceInfo;
                    link.targetInfo['count'] = link.text;
                    newLink.targetInfo.push(link.targetInfo);

                    newLink.targetRawData[link.targetInfo.applicationName] = angular.copy(link);

                    /*
                     * group된 노드에서 개별 노드의 정보를 조회할 때 사용됨.
                     * onclick="SERVERMAP_METHOD_CACHE['{{=
                     * value.applicationName}}']();" 으로 호출함.
                     */
//                    SERVERMAP_METHOD_CACHE[link.targetinfo.applicationName] = function () {
//                        linkClickHandler(null, query, newtargetRawData);
//                    };

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
                newLink.targetInfo.sort(function (e1, e2) {
                    var err1 = newLink.targetRawData[e1.applicationName].error;
                    var err2 = newLink.targetRawData[e2.applicationName].error;

                    if (err1 + err2 > 0) {
                        return err2 - err1;
                    } else {
                        return newLink.targetRawData[e2.applicationName].count - newLink.targetRawData[e1.applicationName].count;
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

        return data;
    };

    /**
     * remove none necessary data for high performance
     * @param data
     */
    this.removeNoneNecessaryDataForHighPerformance = function (data) {
        var nodes = data.applicationMapData.nodeDataArray;

        nodes.forEach(function (node, i) {
            if (angular.isDefined(node.histogram)) {
                delete node.histogram;
            }
            if (angular.isDefined(node.timeSeriesHistogram)) {
                delete node.timeSeriesHistogram;
            }
            if (angular.isDefined(node.agentHistogram)) {
                delete node.agentHistogram;
            }
            if (angular.isDefined(node.serverList)) {
                delete node.serverList;
            }
        });
    };

    /**
     * get node data by id
     * @param data
     * @param id
     * @returns {boolean|hash table}
     */
    this.getNodeDataById = function (data, id) {
        var nodes = data.applicationMapData.nodeDataArray;

        var foundNode = false;
        nodes.forEach(function (node) {
            if (node.id === id) {
                foundNode = node;
            }
        });
        return foundNode;
    }
}]);
