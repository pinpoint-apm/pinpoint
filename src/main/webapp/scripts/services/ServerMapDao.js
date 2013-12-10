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
                var foundNodeKeyFromLastMapData = this.findExistingNodeFromLastMapData(htLastMapData, node);
                if (foundNodeKeyFromLastMapData) {
                    this.mergeNodeData(htLastMapData, foundNodeKeyFromLastMapData - 1, node);
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
    this.findExistingNodeFromLastMapData = function (htLastMapData, node) {
        for (var key in htLastMapData.applicationMapData.nodeDataArray) {
            if (htLastMapData.applicationMapData.nodeDataArray[key].text === node.text && htLastMapData.applicationMapData.nodeDataArray[key].serviceTypeCode === node.serviceTypeCode) {
                return htLastMapData.applicationMapData.nodeDataArray[key].key;
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
        var splitedFilter = filterText.split(cfg.FILTER_DELIMETER),
            aFilter = [];
        angular.forEach(splitedFilter, function (val, key) {
            var filter = val.split(cfg.FILTER_ENTRY_DELIMETER);
            aFilter.push({
                fromCategory: filter[0],
                fromText: filter[1],
                fromKey: this.findNodeKeyByText(filter[1], mapData),
                toCategory: filter[2],
                toText: filter[3],
                toKey: this.findNodeKeyByText(filter[3], mapData)
            });
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
        if (text === 'CLIENT') {
            text = 'USER';
        }
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
}]);
