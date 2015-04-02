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

    var self = this;

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
     * @param lastData
     * @param newData
     * @returns {*}
     */
    this.mergeFilteredMapData = function (lastData, newData) {
        if (lastData.linkDataArray.length === 0 && lastData.nodeDataArray.length === 0) {
            lastData.linkDataArray = newData.linkDataArray;
            lastData.nodeDataArray = newData.nodeDataArray;
        } else {
            angular.forEach(newData.nodeDataArray, function (node, key) {
                var foundNodeKeyFromLastMapData = this.findExistingNodeKeyFromLastMapData(lastData, node);
                if (foundNodeKeyFromLastMapData >= 0) {
                    lastData = this.mergeNodeData(lastData, foundNodeKeyFromLastMapData, node);
                } else {
                    lastData.nodeDataArray.push(node);
                }
            }, this);

            angular.forEach(newData.linkDataArray, function (link, key) {
                var foundLinkKeyFromLastMapData = this.findExistingLinkFromLastMapData(lastData, link);
                if (foundLinkKeyFromLastMapData >= 0) {
                    lastData = this.mergeLinkData(lastData, foundLinkKeyFromLastMapData, link);
                } else {
                    lastData.linkDataArray.push(link);
                }
            }, this);
        }
        return lastData;
    };

    /**
     * find existing node from last map data
     * @param applicationMapData
     * @param node
     * @returns {*}
     */
    this.findExistingNodeKeyFromLastMapData = function (applicationMapData, node) {
        for (var key in applicationMapData.nodeDataArray) {
            if (applicationMapData.nodeDataArray[key].applicationName === node.applicationName && applicationMapData.nodeDataArray[key].serviceTypeCode === node.serviceTypeCode) {
                return key;
            }
        }
        return -1;
    };

    /**
     * find existing link from last map data
     * @param applicationMapData
     * @param link
     * @returns {*}
     */
    this.findExistingLinkFromLastMapData = function (applicationMapData, link) {
        for (var key in applicationMapData.linkDataArray) {
            if (applicationMapData.linkDataArray[key].from === link.from && applicationMapData.linkDataArray[key].to === link.to) {
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
    this.addFilterProperty = function (filters, applicationMapData) {
        var parsedFilters = this.parseFilterText(filters, applicationMapData);

        // node
        angular.forEach(applicationMapData.nodeDataArray, function (val) {
            if (angular.isDefined(_.findWhere(parsedFilters, {nodeKey: val.key}))) {
                val.isFiltered = true;
            } else {
                val.isFiltered = false;
            }
        });

        // link
        angular.forEach(applicationMapData.linkDataArray, function (val, key) {
            if (angular.isDefined(_.findWhere(parsedFilters, {fromKey: val.from, toKey: val.to}))) {
                val.isFiltered = true;
            } else {
                val.isFiltered = false;
            }
        }, this);
        return applicationMapData;
    };

    /**
     * parse filter text
     * @param filters
     * @param applicationMapData
     * @returns {Array}
     */
    this.parseFilterText = function (filters, applicationMapData) {
        var aFilter = [];

        angular.forEach(filters, function (filter) {
            aFilter.push({
                fromServiceType: filter.fst,
                fromApplication: filter.fa,
                fromKey: this.findNodeKeyByApplicationName(filter.fa, applicationMapData),
                toServiceType: filter.tst,
                toApplication: filter.ta,
                toKey: this.findNodeKeyByApplicationName(filter.ta, applicationMapData),
                nodeKey: '' // for node filtering in the future,,,,
            })
        }, this);
        return aFilter;
    };

    /**
     * find node key by text
     * @param applicationName
     * @param applicationMapData
     * @returns {*}
     */
    this.findNodeKeyByApplicationName = function (applicationName, applicationMapData) {
        //if (text === 'CLIENT') {
        //    text = 'USER';
        //}
        var result = _.findWhere(applicationMapData.nodeDataArray, {applicationName: applicationName});
        if (angular.isDefined(result)) {
            return result.key;
        } else {
            return false;
        }
    };

    /**
     * merge node data
     * @param applicationMapData
     * @param nodeKey
     * @param node
     * @returns {*}
     */
    this.mergeNodeData = function (applicationMapData, nodeKey, node) {

        if (angular.isUndefined(applicationMapData.nodeDataArray[nodeKey])) {
            applicationMapData.nodeDataArray[nodeKey] = node;
            return applicationMapData;
        }

        var thisNode = applicationMapData.nodeDataArray[nodeKey];

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
        return applicationMapData;
    };

    /**
     * merge link data
     * @param applicationMapData
     * @param linkKey
     * @param link
     * @returns {*}
     */
    this.mergeLinkData = function (applicationMapData, linkKey, link) {

        if (angular.isUndefined(applicationMapData.linkDataArray[linkKey])) {
            applicationMapData.linkDataArray[linkKey] = link;
            return  applicationMapData;
        }

        var thisLink =  applicationMapData.linkDataArray[linkKey];

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

        return applicationMapData;
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
     * @param applicationMapData
     * @returns {*}
     */
    this.mergeGroup = function (mapData, mergeTypeList) {
//        SERVERMAP_METHOD_CACHE = {};
        var applicationMapData = angular.copy(mapData);
        var nodes = applicationMapData.nodeDataArray;
        var links = applicationMapData.linkDataArray;

        var inboundCountMap = {};
        // 1. 각 노드 별로 인입되는 노드의 갯수와 request 합을 저장해 둠.
        // 		> sourceCount == 0 인 경우 root node 라고 생각 할 수 있습니다.
        // 		> sourceCount == 1 인 경우 아마도 merging을 대상이 아님.
        nodes.forEach(function (node) {
        	var sourceCount = 0;
        	var totalCallCount = 0;
        	links.forEach(function (link) {
                if (link.to === node.key) {
                    sourceCount++;
                    totalCallCount += link.totalCount;
                }
            });
        	inboundCountMap[node.key] = {
                "sourceCount": sourceCount,
                "totalCallCount": totalCallCount
            };
        });
        function getNodeByApplicationName(applicationName) {
            for(var k in nodes) {
                if (applicationName === nodes[k].applicationName) {
                    return nodes[k];
                }
            }
            return false;
        }

        mergeTypeList.forEach( function( mergeType ) {
        	var mergeTypeGroup = mergeType + "_GROUP";
        	
            var newNodeList = [];
            var newLinkList = [];
            var removeNodeIdSet = {};
            var removeLinkIdSet = {};
            
	        nodes.forEach(function (node, nodeIndex) {
	            if (node.category === mergeType) {
	                return;
	            }
	
	            var newNode;
	            var newLink;
	            var newNodeKey = mergeTypeGroup + "_" + node.key;
	
	            var unknownCount = 0;
	            // 현재 확인 중인 node가 "UNKNOWN"으로 요청을 보내는 Node인지 확인하고 그렇다면 갯수를 count 함.
	            // 또한 "UNKNOWN"의 sourceCount는 1인 것 만 대상으로 함.
	            // 		> 즉 두 개 이상의 노드로 부터 request를 받는 "UNKNOWN"은 mergeing 대상이 아님.
	            // from은 같고 to가 두 개 이상인 노드와 unknown을 골라 냄.
	            links.forEach(function (link, linkIndex) {
	                if (link.from == node.key &&
	                    link.targetInfo.serviceType == mergeType &&
	                    inboundCountMap[link.to] && inboundCountMap[link.to].sourceCount == 1) {
	                    unknownCount++;
	                }
	            });
	            // merging을 해야 하닌 2개 이하는 의미없음.
	            if (unknownCount < 2) {
	                return;
	            }
	
	            // @@여기까지 오면 현재 노드는 두 개 이상의 UNKNOWN 으로 요청을 보내는 노드가 됨.( 것두 혼자서 보냄 )  
	            // 고로 현재 노드의 to 가 되는 Unknown 노드들은 merging을 대상이 됨.
	            links.forEach(function (link, linkIndex) {
	            	// 검증
	                if (link.targetInfo.serviceType != mergeType) {
	                    return;
	                }
	                // 검증
	                if (inboundCountMap[link.to] && inboundCountMap[link.to].sourceCount > 1) {
	                    return;
	                }
	
	                // 다시 현재 노드로 부터 나가는 링크들을 골라냄.
	                if (link.from == node.key) {
	                    if (!newNode) {
	                        newNode = {
	                            "key": newNodeKey,
	                            "unknownNodeGroup": [],
	                            "serviceType": mergeTypeGroup,
	                            "category": mergeTypeGroup,
	                            "instanceCount": 0
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
	                    
	                    // 자 링크의 target이 되는 unknown 노드를 가져옴. 
	                    var thisNode = getNodeByApplicationName(link.targetInfo.applicationName);
	                    delete thisNode.category;
	                    newNode.instanceCount += thisNode.instanceCount;
	                    newNode.unknownNodeGroup.push(thisNode);
	
	                    // 각종 count 정보를 합산함.
	                    link.targetInfo['totalCount'] = link.totalCount;
	                    newLink.totalCount += link.totalCount;
	                    newLink.errorCount += link.errorCount;
	                    newLink.slowCount += link.slowCount;
	                    newLink.sourceInfo = link.sourceInfo;
	                    if (link.hasAlert) {
	                        newLink.hasAlert = link.hasAlert;
	                    }
	                    newLink.unknownLinkGroup.push(link);
	
	//                    $.each(link.histogram, function (key, value) {
	//                        if (newLink.histogram[key]) {
	//                            newLink.histogram[key] += value;
	//                        } else {
	//                            newLink.histogram[key] = value;
	//                        }
	//                    });
	
	                    // merging으로 인해 삭제할 원 노드와 원 링크의 정보를 저장해 둠.( 나중에 몰아서 삭제하도록 ).
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
	            applicationMapData.nodeDataArray.push(newNode);
	        });
	
	        newLinkList.forEach(function (newLink) {
	            applicationMapData.linkDataArray.push(newLink);
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
        });
        
        return applicationMapData;
    };

    /**
     * get node data by key
     * @param applicationMapData
     * @param key
     * @returns {boolean|hash table}
     */
    this.getNodeDataByKey = function (applicationMapData, key) {
        var nodes = applicationMapData.nodeDataArray;

        var foundNode = false;
        nodes.forEach(function (node) {
            if (node.key === key) {
                foundNode = node;
            }
        });
        return foundNode;
    };

    /**
     * get link data by key
     * @param applicationMapData
     * @param key
     * @returns {boolean|hash table}
     */
    this.getLinkDataByKey = function (applicationMapData, key) {
        var links = applicationMapData.linkDataArray;

        var foundLink = false;
        links.forEach(function (link) {
            if (link.key === key) {
                foundLink = link;
            }
        });
        return foundLink;
    };

    /**
     * get unknown node data by unknown node group
     * @param applicationMapData
     * @param unknownNodeGroup
     * @returns {*}
     */
    this.getUnknownNodeDataByUnknownNodeGroup = function (applicationMapData, unknownNodeGroup) {
        for (var k in unknownNodeGroup) {
            unknownNodeGroup[k] = self.getNodeDataByKey(applicationMapData, unknownNodeGroup[k].key);
        }

        return unknownNodeGroup;
    };

    /**
     * get unknown link data by unknown link group
     * @param applicationMapData
     * @param unknownLinkGroup
     * @returns {*}
     */
    this.getUnknownLinkDataByUnknownLinkGroup = function (applicationMapData, unknownLinkGroup) {
        for (var k in unknownLinkGroup) {
            unknownLinkGroup[k] = self.getLinkDataByKey(applicationMapData, unknownLinkGroup[k].key);
            unknownLinkGroup[k]['fromNode'] = self.getNodeDataByKey(applicationMapData, unknownLinkGroup[k].from);
            unknownLinkGroup[k]['toNode'] = self.getNodeDataByKey(applicationMapData, unknownLinkGroup[k].to);
        }

        return unknownLinkGroup;
    };

    /**
     * extract data from application map data
     * @param applicationMapData
     * @returns {*}
     */
    this.extractDataFromApplicationMapData = function (applicationMapData) {
        var nodeProperty = ['applicationName', 'category', 'errorCount', 'hasAlert', 'instanceCount', 'isWas', 'key', 'slowCount', 'serviceType', 'totalCount'],
            linkProperty = ['errorCount', 'from', 'hasAlert', 'key', 'sourceInfo', 'slowCount', 'to', 'targetInfo', 'totalCount'];
        var serverMapData = {
            nodeDataArray: [],
            linkDataArray: []
        };
        angular.forEach(applicationMapData.nodeDataArray, function (node, k) {
            serverMapData.nodeDataArray.push({});
            angular.forEach(nodeProperty, function (p) {
                serverMapData.nodeDataArray[k][p] = node[p];
            });
        });
        angular.forEach(applicationMapData.linkDataArray, function (link, k) {
            serverMapData.linkDataArray.push({});
            angular.forEach(linkProperty, function (p) {
                serverMapData.linkDataArray[k][p] = link[p];
            });
        });
        return serverMapData;
    };
}]);
