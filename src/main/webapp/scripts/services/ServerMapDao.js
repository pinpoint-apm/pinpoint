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
                applicationName: query.applicationName,
                serviceTypeCode: query.serviceTypeCode,
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
            applicationName: query.applicationName,
            serviceTypeCode: query.serviceTypeCode,
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
            angular.forEach(mapData.applicationMapData.nodeDataArray, function (node, key) {
                var foundNodeKeyFromLastMapData = this.findExistingNodeKeyFromLastMapData(htLastMapData, node);
                if (foundNodeKeyFromLastMapData >= 0) {
                    this.mergeNodeData(htLastMapData, foundNodeKeyFromLastMapData, node);
                } else {
                    node.key =  htLastMapData.applicationMapData.nodeDataArray.length + 1;
                    htLastMapData.applicationMapData.nodeDataArray.push(node);
                }
            }, this);

            angular.forEach(mapData.applicationMapData.linkDataArray, function (link, key) {
                var foundLinkKeyFromLastMapData = this.findExistingLinkFromLastMapData(htLastMapData, link);
                if (foundLinkKeyFromLastMapData >= 0) {
                    this.mergeLinkData(htLastMapData, foundLinkKeyFromLastMapData, link);
                } else {
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
            if (htLastMapData.applicationMapData.nodeDataArray[key].applicationName === node.applicationName && htLastMapData.applicationMapData.nodeDataArray[key].serviceTypeCode === node.serviceTypeCode) {
                return key;
            }
        }
        return -1;
    };

    /**
     * find existing link from last map data
     * @param htLastMapData
     * @param link
     * @returns {*}
     */
    this.findExistingLinkFromLastMapData = function (htLastMapData, link) {
        for (var key in htLastMapData.applicationMapData.linkDataArray) {
            if (htLastMapData.applicationMapData.linkDataArray[key].from === link.from && htLastMapData.applicationMapData.linkDataArray[key].to === link.to) {
                return key;
            }
        }
        return -1;
    };

    /**
     * add filter property
     * @param filters
     * @param mapData
     * @returns {*}
     */
    this.addFilterProperty = function (filters, mapData) {
        var parsedFilters = this.parseFilterText(filters, mapData);

        // node
        angular.forEach(mapData.applicationMapData.nodeDataArray, function (val) {
            if (angular.isDefined(_.findWhere(parsedFilters, {nodeKey: val.key}))) {
                val.isFiltered = true;
            } else {
                val.isFiltered = false;
            }
        });

        // link
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
     * @param filters
     * @param mapData
     * @returns {Array}
     */
    this.parseFilterText = function (filters, mapData) {
        var aFilter = [];

        angular.forEach(filters, function (filter) {
            aFilter.push({
                fromServiceType: filter.fst,
                fromApplication: filter.fa,
                fromKey: this.findNodeKeyByApplicationName(filter.fa, mapData),
                toServiceType: filter.tst,
                toApplication: filter.ta,
                toKey: this.findNodeKeyByApplicationName(filter.ta, mapData),
                nodeKey: '' // 추후 노드 필터시,,,,
            })
        }, this);
        return aFilter;
    };

    /**
     * find node key by text
     * @param applicationName
     * @param mapData
     * @returns {*}
     */
    this.findNodeKeyByApplicationName = function (applicationName, mapData) {
        //if (text === 'CLIENT') {
        //    text = 'USER';
        //}
        var result = _.findWhere(mapData.applicationMapData.nodeDataArray, {applicationName: applicationName});
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

        var thisNode = htLastMapData.applicationMapData.nodeDataArray[nodeKey];

        thisNode.errorCount += node.errorCount;
        thisNode.slowCount += node.slowCount;
        thisNode.totalCount += node.totalCount;
        if (node.hasAlert) {
            thisNode.hasAlert = node.hasAlert;
        }

        if (angular.isDefined(node.histogram)) {
            if (angular.isDefined(thisNode.histogram)) {
                for (var key in node.histogram) {
                    if (angular.isDefined(thisNode.histogram[key])) {
                        thisNode.histogram[key] += node.histogram[key];
                    } else {
                        thisNode.histogram[key] = node.histogram[key];
                    }
                }
            } else {
                thisNode.histogram = node.histogram;
            }
        }

        if (angular.isDefined(node.agentHistogram)) {
            for (var key in node.agentHistogram) {
                if (angular.isDefined(thisNode.agentHistogram[key])) {
                    for (var innerKey in node.agentHistogram[key]) {
                        if (angular.isDefined(thisNode.agentHistogram[key][innerKey])) {
                            thisNode.agentHistogram[key][innerKey] += node.agentHistogram[key][innerKey];
                        } else {
                            thisNode.agentHistogram[key][innerKey] = node.agentHistogram[key][innerKey];
                        }
                    }
                } else {
                    thisNode.agentHistogram[key] = node.agentHistogram[key];
                }
            }
        }

        if (angular.isDefined(node.timeSeriesHistogram)) {
            for (var key in node.timeSeriesHistogram) {
                if (angular.isDefined(thisNode.timeSeriesHistogram)) {
                    var aTemp = [];
                    outer:
                    for (var innerKey in node.timeSeriesHistogram[key].values) {
                        for (var innerInnerKey in thisNode.timeSeriesHistogram[key].values) {
                            if (thisNode.timeSeriesHistogram[key].values[innerInnerKey][0] === node.timeSeriesHistogram[key].values[innerKey][0]) {
                                thisNode.timeSeriesHistogram[key].values[innerInnerKey][1] += node.timeSeriesHistogram[key].values[innerKey][1];
                                continue outer;
                            }
                        }
                        aTemp.push(node.timeSeriesHistogram[key].values[innerKey]);
                    }
                    thisNode.timeSeriesHistogram[key].values = aTemp.concat(thisNode.timeSeriesHistogram[key].values);
                } else {
                    thisNode.timeSeriesHistogram = node.timeSeriesHistogram;
                }
            }
        }

        if (angular.isDefined(node.agentTimeSeriesHistogram)) {
            for (var key in node.agentTimeSeriesHistogram) {
                if (angular.isDefined(thisNode.agentTimeSeriesHistogram)) {
                    if (angular.isDefined(thisNode.agentTimeSeriesHistogram[key])) {
                        for (var innerKey in node.agentTimeSeriesHistogram[key]) {
                            if (angular.isDefined(thisNode.agentTimeSeriesHistogram[key][innerKey])) {
                                thisNode.agentTimeSeriesHistogram[key][innerKey].values = node.agentTimeSeriesHistogram[key][innerKey].values.concat(thisNode.agentTimeSeriesHistogram[key][innerKey].values);
                            } else {
                                thisNode.agentTimeSeriesHistogram[key][innerKey] = node.agentTimeSeriesHistogram[key][innerKey];
                            }
                        }
                    } else {
                        thisNode.agentTimeSeriesHistogram[key] = node.agentTimeSeriesHistogram[key];
                    }
                } else {
                    thisNode.agentTimeSeriesHistogram = node.agentTimeSeriesHistogram;
                }
            }
        }

        if (angular.isDefined(node.serverList)) {
            for (var key in node.serverList) {
                if (thisNode.serverList[key]) {
                    for (var innerKey in node.serverList[key].instanceList) {
                        if (thisNode.serverList[key].instanceList[innerKey]) {
                            for (var insideKey in node.serverList[key].instanceList[innerKey].histogram) {
                                if (thisNode.serverList[key].instanceList[innerKey].histogram[insideKey]) {
                                    thisNode.serverList[key].instanceList[innerKey].histogram[insideKey] += node.serverList[key].instanceList[innerKey].histogram[insideKey];
                                } else {
                                    thisNode.serverList[key].instanceList[innerKey].histogram[insideKey] = node.serverList[key].instanceList[innerKey].histogram[insideKey];
                                }
                            }
                        } else {
                            thisNode.serverList[key].instanceList[innerKey] = node.serverList[key].instanceList[innerKey];
                        }
                    }
                } else {
                    thisNode.serverList[key] = node.serverList[key];
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

        var thisLink =  htLastMapData.applicationMapData.linkDataArray[linkKey];

        thisLink.errorCount += link.errorCount;
        thisLink.slowCount += link.slowCount;
        thisLink.totalCount += link.totalCount;
        if (link.hasAlert) {
            thisLink.hasAlert = link.hasAlert;
        }

        if (angular.isDefined(link.histogram)) {
            for (var key in link.histogram) {
                if (thisLink.histogram[key]) {
                    thisLink.histogram[key] += link.histogram[key];
                } else {
                    thisLink.histogram[key] = link.histogram[key];
                }
            }
        }

        if (angular.isDefined(link.timeSeriesHistogram)) {
            for (var key in link.timeSeriesHistogram) {
                if (angular.isDefined(thisLink.timeSeriesHistogram)) {
                    var aTemp = [];
                    outer:
                        for (var innerKey in link.timeSeriesHistogram[key].values) {
                            for (var innerInnerKey in thisLink.timeSeriesHistogram[key].values) {
                                if (thisLink.timeSeriesHistogram[key].values[innerInnerKey][0] === link.timeSeriesHistogram[key].values[innerKey][0]) {
                                    thisLink.timeSeriesHistogram[key].values[innerInnerKey][1] += link.timeSeriesHistogram[key].values[innerKey][1];
                                    continue outer;
                                }
                            }
                            aTemp.push(link.timeSeriesHistogram[key].values[innerKey]);
                        }
                    thisLink.timeSeriesHistogram[key].values = aTemp.concat(thisLink.timeSeriesHistogram[key].values);
                } else {
                    thisLink.timeSeriesHistogram = link.timeSeriesHistogram;
                }
            }
        }

        if (angular.isDefined(link.sourceHistogram)) {
            for (var key in link.sourceHistogram) {
                if (angular.isDefined(thisLink.sourceHistogram[key])) {
                    for (var innerKey in link.sourceHistogram[key]) {
                        if (thisLink.sourceHistogram[key][innerKey]) {
                            thisLink.sourceHistogram[key][innerKey] += link.sourceHistogram[key][innerKey];
                        } else {
                            thisLink.sourceHistogram[key][innerKey] = link.sourceHistogram[key][innerKey];
                        }
                    }
                } else {
                    thisLink.sourceHistogram[key] = link.sourceHistogram[key];
                }

            }
        }

        if (angular.isDefined(link.targetHistogram)) {
            for (var key in link.targetHistogram) {
                if (angular.isDefined(thisLink.targetHistogram[key])) {
                    for (var innerKey in link.targetHistogram[key]) {
                        if (thisLink.targetHistogram[key][innerKey]) {
                            thisLink.targetHistogram[key][innerKey] += link.targetHistogram[key][innerKey];
                        } else {
                            thisLink.targetHistogram[key][innerKey] = link.targetHistogram[key][innerKey];
                        }
                    }
                } else {
                    thisLink.targetHistogram[key] = link.targetHistogram[key];
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
     * @param data
     * @returns {*}
     */
    this.mergeUnknown = function (data) {
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
                    inboundCountMap[node.key].totalCallCount += link.totalCount;
                }
            });
        });

        var newNodeList = [];
        var newLinkList = [];

        var removeNodeIdSet = {};
        var removeLinkIdSet = {};

        function getNodeByApplicationName(applicationName) {
            for(var k in nodes) {
                if (applicationName === nodes[k].applicationName) {
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
                            "key": newNodeKey,
                            "unknownNodeGroup": [],
                            "serviceType": "UNKNOWN_GROUP",
                            "category": "UNKNOWN_GROUP"
                        };
                    }
                    if (!newLink) {
                        newLink = {
                            "key": node.key + "-" + newNodeKey,
                            "from": node.key,
                            "to": newNodeKey,
                            "sourceInfo": {},
                            "targetInfo": [],
                            "totalCount": 0,
                            "errorCount": 0,
                            "slowCount": 0,
                            "hasAlert": false,
                            "unknownLinkGroup": [],
                            "histogram": {}
                        };
                    }

                    var thisNode = getNodeByApplicationName(link.targetInfo.applicationName);
                    delete thisNode.category;
                    newNode.unknownNodeGroup.push(thisNode);

                    link.targetInfo['totalCount'] = link.totalCount;
                    newLink.totalCount += link.totalCount;
                    newLink.errorCount += link.errorCount;
                    newLink.slowCount += link.slowCount;
                    newLink.sourceInfo = link.sourceInfo;
                    if (link.hasAlert) {
                        newLink.hasAlert = link.hasAlert;
                    }
                    newLink.unknownLinkGroup.push(link);

                    $.each(link.histogram, function (key, value) {
                        if (newLink.histogram[key]) {
                            newLink.histogram[key] += value;
                        } else {
                            newLink.histogram[key] = value;
                        }
                    });

                    removeNodeIdSet[link.to] = null;
                    removeLinkIdSet[link.key] = null;
                }
            });

            if (newNode) {
                newNode.unknownNodeGroup.sort(function (e1, e2) {
                    return e2.totalCount - e1.totalCount;
                });
                newNodeList.push(newNode);
            }

            if (newLink) {
                newLink.unknownLinkGroup.sort(function (e1, e2) {
                    return e2.totalCount - e1.totalCount;
                });
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
                if (node.key == key) {
                    nodes.splice(i, 1);
                }
            });
        });

        $.each(removeLinkIdSet, function (key, val) {
            links.forEach(function (link, i) {
                if (link.key === key) {
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
     * get node data by key
     * @param data
     * @param key
     * @returns {boolean|hash table}
     */
    this.getNodeDataByKey = function (data, key) {
        var nodes = data.applicationMapData.nodeDataArray;

        var foundNode = false;
        nodes.forEach(function (node) {
            if (node.key ===key) {
                foundNode = node;
            }
        });
        return foundNode;
    };

}]);
