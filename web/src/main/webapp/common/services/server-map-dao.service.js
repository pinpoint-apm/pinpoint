(function() {
	'use strict';
	/**
	 * (en)ServerMapDaoService 
	 * @ko ServerMapDaoService
	 * @group Service
	 * @name ServerMapDaoService
	 * @class
	 */
	pinpointApp.constant('serverMapDaoServiceConfig', {
	    serverMapDataUrl: 'getServerMapData.pinpoint',
	    filteredServerMapDataUrl: 'getFilteredServerMapDataMadeOfDotGroup.pinpoint',
	    filtermapUrl: 'filtermap.pinpoint',
	    lastTransactionListUrl: 'lastTransactionList.pinpoint',
	    transactionListUrl: 'transactionList.pinpoint',
	    FILTER_DELIMETER: "^",
	    FILTER_ENTRY_DELIMETER: "|",
	    FILTER_FETCH_LIMIT: 5000,
		MAX_DISPLAY_COUNT_GROUP_LIST: 3
	});
	
	pinpointApp.service('ServerMapDaoService', [ 'serverMapDaoServiceConfig', function(cfg) {
	
	    var self = this;
	
	    this.abort = function() {
			if ( this._oAjax ) {
				this._oAjax.abort();
			}
		};

		this.getServerMapData = function (query, cb) {
	    	var data = {
	            applicationName: query.applicationName,
	            from: query.from,
	            to: query.to,
	            callerRange: query.callerRange,
	            calleeRange: query.calleeRange,
				bidirectional: query.bidirectional,
				wasOnly: query.wasOnly
	        };
	    	if ( isNaN( parseInt( query.serviceTypeName ) ) ) {
	    		data.serviceTypeName = query.serviceTypeName; 
	    	} else {
	    		data.serviceTypeCode = query.serviceTypeName;
	    	}
	        this._oAjax = jQuery.ajax({
	            type: 'GET',
	            url: cfg.serverMapDataUrl,
	            cache: false,
	            dataType: 'json',
	            data: data,
	            success: function (result) {
	                if (angular.isFunction(cb)) {
	                    cb(null, query, result);
	                }
	                self._oAjax = null;
	            },
	            error: function (xhr, status, error) {
	            	if ( status !== "abort" ) {
						if (angular.isFunction(cb)) {
							cb(error, query, {});
						}
					}
					self._oAjax = null;
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
	            from: query.from,
	            to: query.to,
	            originTo: query.originTo,
	            filter: query.filter,
	            limit: cfg.FILTER_FETCH_LIMIT,
				callerRange: query.callerRange,
				calleeRange: query.calleeRange,
				v: 3,
				xGroupUnit: 987,
				yGroupUnit: 57
	        };
	        if ( isNaN( parseInt( query.serviceTypeName ) ) ) {
	    		data.serviceTypeName = query.serviceTypeName; 
	    	} else {
	    		data.serviceTypeCode = query.serviceTypeName;
	    	}
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
	            });
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
			var key, innerKey;
	
	        thisNode.errorCount += node.errorCount;
	        thisNode.slowCount += node.slowCount;
	        thisNode.totalCount += node.totalCount;
	        if (node.hasAlert) {
	            thisNode.hasAlert = node.hasAlert;
	        }
	
	        if (angular.isDefined(node.histogram)) {
	            if (angular.isDefined(thisNode.histogram)) {
	                for ( key in node.histogram) {
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
	            for ( key in node.agentHistogram) {
	                if (angular.isDefined(thisNode.agentHistogram[key])) {
	                    for ( innerKey in node.agentHistogram[key]) {
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
	            for ( key in node.timeSeriesHistogram) {
	                if (angular.isDefined(thisNode.timeSeriesHistogram)) {
	                    var aTemp = [];
	                    outer:
	                    for ( innerKey in node.timeSeriesHistogram[key].values) {
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
	            for ( key in node.agentTimeSeriesHistogram) {
	                if (angular.isDefined(thisNode.agentTimeSeriesHistogram)) {
	                    if (angular.isDefined(thisNode.agentTimeSeriesHistogram[key])) {
	                        for ( innerKey in node.agentTimeSeriesHistogram[key]) {
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
	            for ( key in node.serverList) {
	                if (thisNode.serverList[key]) {
	                    for ( innerKey in node.serverList[key].instanceList) {
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
			var key, innerKey;
	
	        thisLink.errorCount += link.errorCount;
	        thisLink.slowCount += link.slowCount;
	        thisLink.totalCount += link.totalCount;
	        if (link.hasAlert) {
	            thisLink.hasAlert = link.hasAlert;
	        }
	
	        if (angular.isDefined(link.histogram)) {
	            for ( key in link.histogram) {
	                if (thisLink.histogram[key]) {
	                    thisLink.histogram[key] += link.histogram[key];
	                } else {
	                    thisLink.histogram[key] = link.histogram[key];
	                }
	            }
	        }
	
	        if (angular.isDefined(link.timeSeriesHistogram)) {
	            for ( key in link.timeSeriesHistogram) {
	                if (angular.isDefined(thisLink.timeSeriesHistogram)) {
	                    var aTemp = [];
	                    outer:
	                        for ( innerKey in link.timeSeriesHistogram[key].values) {
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
	            for ( key in link.sourceHistogram) {
	                if (angular.isDefined(thisLink.sourceHistogram[key])) {
	                    for ( innerKey in link.sourceHistogram[key]) {
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
	            for ( key in link.targetHistogram) {
	                if (angular.isDefined(thisLink.targetHistogram[key])) {
	                    for ( innerKey in link.targetHistogram[key]) {
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
	    this.mergeGroup = function (applicationMapData, mergeTypeList) {
	    	var self = this;
	        var nodes = applicationMapData.nodeDataArray;
	        var links = applicationMapData.linkDataArray;
	        var inboundCountMap = self._getInboundCountMap( nodes, links );
	
	        mergeTypeList.forEach( function( mergeType ) {
	        	var mergeTypeGroup = mergeType + "_GROUP";
	        	
	            var newNodeList = [];
	            var newLinkList = [];
	            var removeNodeIdSet = {};
	            var removeLinkIdSet = {};
	            
		        nodes.forEach(function ( node ) {
		            var newNode;
		            var newLink;
		            var newNodeKey = mergeTypeGroup + "_" + node.key;
		
		            var targetNodeCount = 0;
		            links.forEach(function ( link ) {
		                if (link.from == node.key && link.targetInfo.serviceType == mergeType && inboundCountMap[link.to] && inboundCountMap[link.to].toCount == 1) {
		                	targetNodeCount++;
		                }
		            });
		            if (targetNodeCount < 2) {
		                return;
		            }
	
		            links.forEach(function ( link ) {
		                if (link.targetInfo.serviceType != mergeType) {
		                    return;
		                }
		                if (inboundCountMap[link.to] && inboundCountMap[link.to].toCount > 1) {
		                    return;
		                }
		
		                if (link.from == node.key) {
		                    if (!newNode) {
		                    	newNode = self._createNewNode( newNodeKey, mergeTypeGroup );
		                    }
		                    if (!newLink) {
		                    	newLink = self._createNewLink( node.key, newNodeKey );
		                    }
		                    self._addToSubNode( newNode, self._getNodeByApplicationName(nodes, link.targetInfo.applicationName, mergeType ), function() {} );
		                    self._mergeLinkData( newLink, link );	
		                    newLink.unknownLinkGroup.push(link);

		                    removeNodeIdSet[link.to] = null;
		                    removeLinkIdSet[link.key] = null;
		                }
		            });
		
		            if (newNode) {
		                newNode.unknownNodeGroup.sort(function (e1, e2) {
		                    return e2.totalCount - e1.totalCount;
		                });
		                self._addListTopX( newNode );
		                newNodeList.push(newNode);
		            }
		
		            if (newLink) {
		                newLink.unknownLinkGroup.sort(function (e1, e2) {
		                    return e2.totalCount - e1.totalCount;
		                });
		                newLinkList.push(newLink);
		            }
		        });
		        self._addToOriginal( nodes, newNodeList );
		        self._addToOriginal( links, newLinkList );
		
		        self._removeByKey( nodes, removeNodeIdSet );
		        self._removeByKey( links, removeLinkIdSet );
	        });
	        return applicationMapData;
	    };
	    
	    this._addToOriginal = function( array, newData ) {
	    	newData.forEach(function ( d ) {
	            array.push(d);
	        });
	    };
	    /**
	     * in&out bound count map
	     * @param nodes
	     * @returns {}
	     */
	    this._getInboundCountMap = function( nodes, links ) {
	    	var inboundCountMap = {};
	    	nodes.forEach(function (node) {
	    		var countMap = {
	    			toCount : 0,
	    			fromCount : 0,
	    			totalCallCount : 0
	    		};
	    	    links.forEach(function (link) {
	                if (link.to === node.key) {
	                    countMap.toCount++;
	                    countMap.totalCallCount += link.totalCount;
	                }
	                if ( link.from === node.key ) {
	            	    countMap.fromCount++;
	                }
	            });
	    	    inboundCountMap[node.key] = countMap;
	        });
	    	return inboundCountMap;
	    };
	    this._selectMergeTarget = function( nodes, inboundCountMap ) {
	    	var targetNodeList = [];
			nodes.forEach(function (node, nodeIndex) {
				if ( inboundCountMap[node.key].fromCount > 0 ) {
					return;
				}
				if ( inboundCountMap[node.key].toCount < 2 ) {
					return;
				}
				targetNodeList.push( node );
			});
			return targetNodeList;
	    };
	    this._getFromNodes = function( links, nodeKey ) {
	    	var fromNodeArray = [];
	    	links.forEach( function( link ) {
				if ( link.to === nodeKey ) {
					fromNodeArray.push(link.from );
				}
			});
	    	return fromNodeArray;
	    };
	    
	    //this.mergeMultiLinkGroup = function( mapData, mergeTypeList ) {
	    this.mergeMultiLinkGroup = function( applicationMapData, mergeTypeList ) {
	    	var self = this,
	        	//applicationMapData = angular.copy(mapData),
	        	nodes = applicationMapData.nodeDataArray,
	        	links = applicationMapData.linkDataArray,
	        	inboundCountMap = this._getInboundCountMap( nodes, links ),
	        	targetNodeList = this._selectMergeTarget( nodes, inboundCountMap ),
	        	skipKeyMap = {},
				newNodeList = [],
	        	newLinkList = [],
	        	removeNodeIdSet = {},
	        	removeLinkIdSet = {};
	        
			targetNodeList.forEach(function(node, nodeIndex) {
				if ( skipKeyMap[node.key] === true && mergeTypeList.indexOf( node.serviceType ) == -1 ) {
					return;
				}
				skipKeyMap[node.key] = true;
				var mergeTypeGroup = node.serviceType + "_GROUP";
	            var newNode = null;
	            var newLinks = null;
	            var newNodeKey = mergeTypeGroup + "_" + node.key;
	            var fromNodeArray = self._getFromNodes( links, node.key );
				
				targetNodeList.forEach( function( innerNode, innerNodeIndex ) {
					if ( skipKeyMap[innerNode.key] === true || node.serviceType !== innerNode.serviceType || mergeTypeList.indexOf( innerNode.serviceType ) == -1 ) {
						return;
					}
					
					var fromNodeArrayOfInner = self._getFromNodes( links, innerNode.key );
					if ( fromNodeArray.length !== fromNodeArrayOfInner.length ) {
						return;
					}
					var i = 0;
					
					for( i = 0 ; i < fromNodeArray.length ; i++ ) {
						if ( fromNodeArrayOfInner.indexOf( fromNodeArray[i] ) === -1 ) {
							return;
						}
					}
					
					skipKeyMap[innerNode.key] = true;
					if (newNode === null) {
	                    newNode = self._createNewMultiGroupNode( newNodeKey, mergeTypeGroup );
	                }
	                if (newLinks === null) {
	                	newLinks = [];
	                	for( i = 0 ; i < fromNodeArrayOfInner.length ; i++ ) {
	                		newLinks.push( self._createNewLink( fromNodeArrayOfInner[i], newNodeKey ) );
	                	}
	                }
	                self._addToSubNode( newNode, innerNode, function( innerNodeKey ) {
	                	removeNodeIdSet[innerNodeKey] = null;
	                });
	                self._addToSubLink( links, newLinks, innerNode.key, function( linkKey, link ) {
	                	removeLinkIdSet[linkKey] = null;
	                	var aFrom = /(.*)\^(.*)/.exec( link.from );
	                	var aTo = /(.*)\^(.*)/.exec( link.to );

	                	if ( typeof newNode.subGroup[aTo[1]] === "undefined" ) {
							newNode.subGroup[aTo[1]] = [];
						}

						newNode.subGroup[aTo[1]].push({
							applicationName: aFrom[1],
							hasAlert: link.hasAlert,
							totalCount: link.totalCount,
							serviceType : aFrom[2],
							key : link.from
						});
	                } );
				});
				
				if ( newNode !== null ) {
					self._addToSubNode( newNode, node, function( nodeKey ) {
						removeNodeIdSet[nodeKey] = null;
					});
				}
				if ( newLinks !== null ) {
					self._addToSubLink( links, newLinks, node.key, function( linkKey, link ) {
						removeLinkIdSet[linkKey] = null;
						var aFrom = /(.*)\^(.*)/.exec( link.from );
	                	var aTo = /(.*)\^(.*)/.exec( link.to );

						if ( typeof newNode.subGroup[aTo[1]] === "undefined" ) {
							newNode.subGroup[aTo[1]] = [];
						}
						newNode.subGroup[aTo[1]].push({
							applicationName: aFrom[1],
							hasAlert: link.hasAlert,
							totalCount: link.totalCount,
							serviceType : aFrom[2],
							key : link.from
						});
					});
				}
				if ( newNode !== null && newLinks !== null ) {
					newNodeList.push( newNode );
					newNode.unknownNodeGroup.sort(function (e1, e2) {
						return e2.totalCount - e1.totalCount;
					});
					for( var p in newNode.subGroup ) {
						newNode.subGroup[p].sort(function(e1, e2) {
							return e2.totalCount - e1.totalCount;
						});
					}
					self._addListTopX( newNode );
					newNodeList.push( newNode );
					newLinks.forEach( function( nlink ) {
						nlink.unknownLinkGroup.sort(function (e1, e2) {
							return e2.totalCount - e1.totalCount;
						});
						newLinkList.push( nlink );
					});
				}
				newNode = null;
				newLinks = null;
			});

	        self._addToOriginal( nodes, newNodeList );
	        self._addToOriginal( links, newLinkList );
	
	        self._removeByKey( nodes, removeNodeIdSet );
	        self._removeByKey( links, removeLinkIdSet );
	
	        return applicationMapData;
	    };
	    this._addListTopX = function(newNode) {
			newNode.listTopX.push({
				"applicationName": "Total : " + newNode.unknownNodeGroup.length,
				"totalCount": newNode.totalCount,
				"tableHeader": true
			});
			for( var i = 0 ; i < Math.min( newNode.unknownNodeGroup.length , cfg.MAX_DISPLAY_COUNT_GROUP_LIST ) ; i++ ) {
				newNode.listTopX.push( newNode.unknownNodeGroup[i] );
			}
			if ( newNode.unknownNodeGroup.length > cfg.MAX_DISPLAY_COUNT_GROUP_LIST ) {
				newNode.listTopX.push({
					"applicationName": "...",
					"totalCount": ""
				});
			}
		};
	    this._createNewLink = function( fromKey, toKey ) {
	    	return {
	            "key": fromKey + "-" + toKey,
	            "from": fromKey,
	            "to": toKey,
	            "sourceInfo": {},
	            "targetInfo": [],
	            "totalCount": 0,
	            "errorCount": 0,
	            "slowCount": 0,
	            "hasAlert": false,
	            "unknownLinkGroup": [],
	            "histogram": {}
	        };
	    };
	    this._createNewNode = function( key, type ) {
	    	return {
	            "key": key,
				"category": type,
				"nodeCount": 0,
				"alertCount": 0,
				"totalCount": 0,
				"serviceType": type,
	            "instanceCount": 0,
				"unknownNodeGroup": [],
				"listTopX": []
	        };
	    };
	    this._createNewMultiGroupNode = function( key, type ) {
	    	return {
	            "key": key,
				"category": type,
				"subGroup": {},
				"nodeCount": 0,
				"alertCount": 0,
				"totalCount": 0,
				"serviceType": type,
				"instanceCount": 0,
				"unknownNodeGroup": [],
				"listTopX": []
	        };
	    };
	    this._removeByKey = function( nodes, removeIdSet ) {
	    	$.each(removeIdSet, function (key, val) {
	            nodes.forEach(function (node, i) {
	                if (node.key == key) {
	                    nodes.splice(i, 1);
	                }
	            });
	        });
	    };
	    this._addToSubNode = function( newNode, subNode, fnCall ) {
	        delete subNode.category;
	    	newNode.instanceCount += subNode.instanceCount;
	    	newNode.nodeCount++;
	    	newNode.alertCount += subNode.hasAlert ? 1 : 0;
	    	newNode.totalCount += subNode.totalCount;
	    	newNode.unknownNodeGroup.push(subNode);
	        fnCall( subNode.key );
	    };
	    this._addToSubLink = function( links, newLinks, nodeKey, fnCall ) {
	    	var self = this;
		    links.forEach( function( link ) {
				if ( link.to === nodeKey ) {
					newLinks.forEach(function( newLink ) {
						if ( newLink.from == link.from ) {
							//link.targetInfo['totalCount'] = link.totalCount;
							self._mergeLinkData( newLink, link );
			                newLink.unknownLinkGroup.push(link);								
						}
					});
					fnCall( link.key, link );
				}
			});
	    };
	    this._mergeLinkData = function( newLink, oldLink ) {
	    	newLink.totalCount += oldLink.totalCount;
	        newLink.errorCount += oldLink.errorCount;
	        newLink.slowCount += oldLink.slowCount;
	        newLink.sourceInfo = oldLink.sourceInfo;
	        if (oldLink.hasAlert) {
	            newLink.hasAlert = oldLink.hasAlert;
	        }
	    };
	    this._getNodeByApplicationName = function( nodes, applicationName, mergeType ) {
	        for(var k in nodes) {
	            if (applicationName === nodes[k].applicationName && mergeType === nodes[k].serviceType ) {
	                return nodes[k];
	            }
	        }
	        return false;
	    };
	
	    /**
	     * get node data by key
	     * @param applicationMapData
	     * @param key
	     * @returns {object}
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
	    this.getLinkNodeDataByNodeKey = function (applicationMapData, key, fromName) {
	        var links = applicationMapData.linkDataArray;
	
	        var foundLink = false;
	        links.forEach(function (link) {
	            if (link.to === key && link.from.indexOf( fromName ) != -1 ) {
	                foundLink = link;
	            }
	        });
	        return foundLink;
	    };
	
	    /**
	     * get link data by key
	     * @param applicationMapData
	     * @param key
	     * @returns {object}
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
	        var nodeProperty = ['applicationName', 'category', 'errorCount', 'hasAlert', 'instanceCount', 'isWas', 'isQueue', 'isAuthorized', 'key', 'slowCount', 'serviceType', 'totalCount', 'histogram'],
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
})();