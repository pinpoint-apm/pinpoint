(function() {
	'use strict';
	/**
	 * (en)nodeInfoDetailsDirective 
	 * @ko nodeInfoDetailsDirective
	 * @group Directive
	 * @name nodeInfoDetailsDirective
	 * @class
	 */	
	pinpointApp.constant('nodeInfoDetailsDirectiveConfig', {
	    applicationStatisticsUrl: '/applicationStatistics.pinpoint',
	    myColors: ["#2ca02c", "#3c81fa", "#f8c731", "#f69124", "#f53034"],
	    maxTimeToShowLoadAsDefaultForUnknown: 60 * 60 * 12 // 12h
	});
	
	pinpointApp.directive('nodeInfoDetailsDirective', [ 'nodeInfoDetailsDirectiveConfig', '$filter', '$timeout', 'isVisibleService', '$window', 'helpContentTemplate', 'helpContentService',
        function (cfg, $filter, $timeout, isVisibleService, $window, helpContentTemplate, helpContentService) {
            return {
                restrict: 'EA',
                replace: true,
                templateUrl: 'features/nodeInfoDetails/nodeInfoDetails.html',
                scope: {},
                link: function postLink(scope, element) {

                    // define private variables
                    var htServermapData, htLastNode, htUnknownResponseSummary, htUnknownLoad, htQuery,
                        htAgentChartRendered, bShown, sLastKey, getUnknownNode;

                    // define private variables of methods
                    var reset, showDetailInformation, renderAllChartWhichIsVisible, hide, show, renderResponseSummary,
                        renderLoad;

                    // bootstrap
                    bShown = false;
                    scope.htLastUnknownNode = false;

                    angular.element($window).bind('resize',function(e) {
                        if (bShown && /_GROUP$/.test( htLastNode.category ) ) {
                            renderAllChartWhichIsVisible(htLastNode);
                        }
                    });

                    element
                        .find('.unknown-list')
                        .bind('scroll', function (e) {
                            renderAllChartWhichIsVisible(htLastNode);
                        });

                    /**
                     * reset
                     */
                    reset = function () {
                        htUnknownResponseSummary = {};
                        htUnknownLoad = {};
                        htAgentChartRendered = {};
                        htQuery = false;
                        scope.showNodeInfoDetails = false;
                        scope.node = false;
                        scope.unknownNodeGroup = null;
                        scope.hosts = null;
                        scope.showNodeServers = false;
                        scope.agents = null;
                        scope.showAgents = false;
                        scope.showNodeResponseSummaryForUnknown = false;
                        scope.showNodeResponseSummary = false;
                        scope.showNodeLoad = false;
                        scope.agentHistogram = false;
                        scope.nodeOrderBy = 'totalCount';
                        scope.nodeOrderByNameClass = '';
                        scope.nodeOrderByCountClass = 'glyphicon-sort-by-order-alt';
                        scope.nodeOrderByDesc = true;

                        if (!(scope.$$phase == '$apply' || scope.$$phase == '$digest') ) {
                        	if (!(scope.$root.$$phase == '$apply' || scope.$root.$$phase == '$digest') ) {
                        		scope.$digest();
                        	}
                        }
                        
                    };

                    /**
                     * show detail information
                     * @param node
                     */
                    showDetailInformation = function (node) {
                        scope.showNodeInfoDetails = true;
                        scope.node = node;
                        scope.unknownNodeGroup = node.unknownNodeGroup;
                        scope.serverList = node.serverList;
                        scope.showNodeServers = _.isEmpty(scope.serverList) ? false : true;
                        scope.agentHistogram = node.agentHistogram;
                        scope.serverCount = 0;
                        scope.errorServerCount = 0;
                        for( var p in scope.serverList ) {
                        	var instanceList = scope.serverList[p].instanceList;
                        	for( var p2 in instanceList ) {
                        		scope.serverCount++;
                        		if ( scope.agentHistogram[instanceList[p2].name].Error > 0 ) {
                        			scope.errorServerCount++;
                        		}
                        	}
                        }

                        if ( /_GROUP$/.test( node.serviceType ) === false ) {
                            scope.showNodeResponseSummary = true;
                            scope.showNodeLoad = true;

                            renderResponseSummary('forNode', node.applicationName, node.histogram, '100%', '150px');
                            renderLoad('forNode', node.applicationName, node.timeSeriesHistogram, '100%', '220px', true);
                        } else if ( /_GROUP$/.test( node.serviceType ) ){
                            scope.showNodeResponseSummaryForUnknown = (scope.oNavbarVoService.getPeriod() <= cfg.maxTimeToShowLoadAsDefaultForUnknown) ? false : true;
                            renderAllChartWhichIsVisible(node);
//                            scope.htLastUnknownNode = angular.copy(node);
                            scope.htLastUnknownNode = node;

                            $timeout(function () {
                                element.find('[data-toggle="tooltip"]').tooltip('destroy').tooltip();
                            });
                        }
//                        if (!scope.$$phase) {
//                            scope.$digest();
//                        }
                        if (!(scope.$$phase == '$apply' || scope.$$phase == '$digest') ) {
                        	if (!(scope.$root.$$phase == '$apply' || scope.$root.$$phase == '$digest') ) {
                        		scope.$digest();
                        	}
                        }
                    };

                    /**
                     * render all chart which is visible
                     * @param node
                     */
                    renderAllChartWhichIsVisible = function (node) {
                        $timeout(function () {
                            angular.forEach(node.unknownNodeGroup, function (node){
                                var applicationName = node.applicationName,
                                    className = $filter('applicationNameToClassName')(applicationName);
                                if (angular.isDefined(htUnknownResponseSummary[applicationName])) return;
                                if (angular.isDefined(htUnknownLoad[applicationName])) return;

                                var elQuery = '.nodeInfoDetails .summaryCharts_' + className,
                                    el = angular.element(elQuery);
                                var visible = isVisibleService(el.get(0), 1);
                                if (!visible) return;

                                if (scope.showNodeResponseSummaryForUnknown) {
                                    htUnknownResponseSummary[applicationName] = true;
                                    renderResponseSummary(null, applicationName, node.histogram, '360px', '180px');
                                } else {
                                    htUnknownLoad[applicationName] = true;
                                    renderLoad(null, applicationName, node.timeSeriesHistogram, '360px', '200px', true);
                                }
                            });
                        });
                    };

                    /**
                     * render response summary
                     * @param namespace
                     * @param toApplicationName
                     * @param histogram
                     * @param w
                     * @param h
                     */
                    renderResponseSummary = function (namespace, toApplicationName, histogram, w, h) {
                        var className = $filter('applicationNameToClassName')(toApplicationName),
                            namespace = namespace || 'forNode_' + className;
                        scope.$broadcast('responseTimeChartDirective.initAndRenderWithData.' + namespace, histogram, w, h, false, true);
                    };

                    /**
                     * render load
                     * @param namespace
                     * @param toApplicationName
                     * @param timeSeriesHistogram
                     * @param w
                     * @param h
                     * @param useChartCursor
                     */
                    renderLoad = function (namespace, toApplicationName, timeSeriesHistogram, w, h, useChartCursor) {
                        var className = $filter('applicationNameToClassName')(toApplicationName),
                            namespace = namespace || 'forNode_' + className;
                        scope.$broadcast('loadChartDirective.initAndRenderWithData.' + namespace, timeSeriesHistogram, w, h, useChartCursor);
                    };
                    
                    getUnknownNode = function( key ) {
	                	var node = null;
	                	for( var i = 0 ; i < htLastNode.unknownNodeGroup.length ; i++ ) {
	                		if (htLastNode.unknownNodeGroup[i].key === key) {
	                			node = htLastNode.unknownNodeGroup[i];
	                			break;
	                		}
	                	}
	                	return node;
	                };

                    /**
                     * hide
                     */
                    hide = function () {
                        bShown = false;
                        element.hide();
                    };

                    /**
                     * show
                     */
                    show = function () {
                        bShown = true;
                        element.show();
                    };

                    /**
                     * show node detail information of scope
                     * @param index
                     */
                    scope.showNodeDetailInformation = function (key) {
                        htLastNode = getUnknownNode(key);//htLastNode.unknownNodeGroup[index];
                        showDetailInformation(htLastNode);
                        scope.$emit('nodeInfoDetail.showDetailInformationClicked', htQuery, htLastNode);
                    };

                    /**
                     * go back to unknown node
                     */
                    scope.goBackToUnknownNode = function () {
                        htLastNode = scope.htLastUnknownNode;
                        htUnknownResponseSummary = {};
                        htUnknownLoad = {};
                        showDetailInformation(htLastNode);
                        scope.$emit('nodeInfoDetail.showDetailInformationClicked', htQuery, htLastNode);
                    };

                    /**
                     * scope render node response summary
                     * @param applicationName
                     * @param index
                     */
                    scope.renderNodeResponseSummary = function (applicationName, key) {
                        if (angular.isUndefined(htUnknownResponseSummary[applicationName])) {
                            htUnknownResponseSummary[applicationName] = true;
                            renderResponseSummary(null, applicationName, getUnknownNode(key).histogram, '360px', '180px');
                        }
                    };

                    /**
                     * scope render node load
                     * @param applicationName
                     * @param index
                     */
                    scope.renderNodeLoad = function (applicationName, key) { 
                        if (angular.isUndefined(htUnknownLoad[applicationName])) {
                            htUnknownLoad[applicationName] = true;
                            renderLoad(null, applicationName, getUnknownNode(key).timeSeriesHistogram, '360px', '200px', true);
                        }
                    };

                    /**
                     * render node agent charts
                     * @param applicationName
                     */
                    scope.renderNodeAgentCharts = function (applicationName) {
                    	$at($at.MAIN, $at.CLK_SHOW_GRAPH);
                        if (angular.isDefined(htAgentChartRendered[applicationName])) return;
                        htAgentChartRendered[applicationName] = true;
                        renderResponseSummary(null, applicationName, htLastNode.agentHistogram[applicationName], '100%', '150px');
                        renderLoad(null, applicationName, htLastNode.agentTimeSeriesHistogram[applicationName], '100%', '200px', true);
                    };

                    /**
                     * scope node search change
                     */
                    scope.nodeSearchChange = function () {
                        renderAllChartWhichIsVisible(htLastNode);
                    };

                    /**
                     * scope node order by name
                     */
                    scope.nodeOrderByName = function () {
                        if (scope.nodeOrderBy === 'applicationName') {
                            scope.nodeOrderByDesc = !scope.nodeOrderByDesc;
                            if (scope.nodeOrderByNameClass === 'glyphicon-sort-by-alphabet-alt') {
                                scope.nodeOrderByNameClass = 'glyphicon-sort-by-alphabet';
                            } else {
                                scope.nodeOrderByNameClass = 'glyphicon-sort-by-alphabet-alt';
                            }
                        } else {
                            scope.nodeOrderByNameClass = 'glyphicon-sort-by-alphabet-alt';
                            scope.nodeOrderByCountClass = '';
                            scope.nodeOrderByDesc = true;
                            scope.nodeOrderBy = 'applicationName';
                        }
                        renderAllChartWhichIsVisible(htLastNode);
                    };

                    /**
                     * scope node order by count
                     */
                    scope.nodeOrderByCount = function () {
                        if (scope.nodeOrderBy === 'totalCount') {
                            scope.nodeOrderByDesc = !scope.nodeOrderByDesc;
                            if (scope.nodeOrderByCountClass === 'glyphicon-sort-by-order-alt') {
                                scope.nodeOrderByCountClass = 'glyphicon-sort-by-order';
                            } else {
                                scope.nodeOrderByCountClass = 'glyphicon-sort-by-order-alt';
                            }
                        } else {
                            scope.nodeOrderByCountClass = 'glyphicon-sort-by-order-alt';
                            scope.nodeOrderByNameClass = '';
                            scope.nodeOrderByDesc = true;
                            scope.nodeOrderBy = 'totalCount';
                        }
                        renderAllChartWhichIsVisible(htLastNode);
                    };

                    /**
                     * show unknown node by
                     * @param nodeSearch
                     * @param node
                     * @returns {boolean}
                     */
                    scope.showUnknownNodeBy = function (nodeSearch, node) {
                        if (nodeSearch) {
                            if (node.applicationName.indexOf(nodeSearch) > -1 ||
                                node.totalCount.toString().indexOf(nodeSearch) > -1) {
                                return true;
                            } else {
                                return false;
                            }
                        } else {
                            return true;
                        }
                    };
                    scope.showServerList = function() {
                    	scope.$emit("serverListDirective.show", true, htLastNode, scope.oNavbarVoService);
                    };

                    /**
                     * scope event on nodeInfoDetailsDirective.initialize
                     */
                    scope.$on('nodeInfoDetailsDirective.initialize', function (event, e, query, node, mapData, navbarVoService, reloadOnly, searchQuery) {
                        show();
                        // DISABLE node Cache
                        //if (angular.equals(sLastKey, node.key) && !reloadOnly) {
                        //    if (htLastNode.category === 'UNKNOWN_GROUP') {
                        //        renderAllChartWhichIsVisible(htLastNode);
                        //    }
                        //    return;
                        //}
                        reset();
                        htQuery = query;
                        sLastKey = node.key;
                        htLastNode = node;
                        scope.htLastUnknownNode = false;
                        scope.oNavbarVoService = navbarVoService;
                        scope.nodeSearch = searchQuery || "";
                        htServermapData = mapData;
                        showDetailInformation(node);
                    });

                    /**
                     * scope event on nodeInfoDetailsDirective.hide
                     */
                    scope.$on('nodeInfoDetailsDirective.hide', function (event) {
                        hide();
                    });

                    /**
                     * scope event on nodeInfoDetailsDirective.lazyRendering
                     */
                    scope.$on('nodeInfoDetailsDirective.lazyRendering', function (event, e) {
                        renderAllChartWhichIsVisible(htLastNode);
                    });

                    scope.$on('responseTimeChartDirective.itemClicked.forNode', function (event, data) {
//                        console.log('on responseTimeChartDirective.itemClicked.forNode', data);
                    });

                    jQuery('.responseSummaryChartTooltip').tooltipster({
                    	content: function() {
                    		return helpContentTemplate(helpContentService.nodeInfoDetails.responseSummary);
                    	},
                    	position: "top",
                    	trigger: "click"
                    });
                    jQuery('.loadChartTooltip').tooltipster({
                    	content: function() {
                    		return helpContentTemplate(helpContentService.nodeInfoDetails.load);
                    	},
                    	position: "top",
                    	trigger: "click"
                    });
                }
            };
	    }
	]);
})();