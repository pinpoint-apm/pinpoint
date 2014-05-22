'use strict';

pinpointApp.constant('nodeInfoDetailsConfig', {
    applicationStatisticsUrl: '/applicationStatistics.pinpoint',
    myColors: ["#2ca02c", "#3c81fa", "#f8c731", "#f69124", "#f53034"],
    maxTimeToShowLoadAsDefaultForUnknown: 60 * 60 * 12 // 12h
});

pinpointApp
    .directive('nodeInfoDetails', [ 'nodeInfoDetailsConfig', '$filter', '$timeout', 'isVisible', '$window',
        function (cfg, $filter, $timeout, isVisible, $window) {
            return {
                restrict: 'EA',
                replace: true,
                templateUrl: 'views/nodeInfoDetails.html',
                link: function postLink(scope, element) {

                    // define private variables
                    var htServermapData, htLastNode, htUnknownResponseSummary, htUnknownLoad, htQuery,
                        htAgentChartRendered, bShown;

                    // define private variables of methods
                    var reset, showDetailInformation, renderAllChartWhichIsVisible, hide, show, renderResponseSummary,
                        renderLoad;

                    // bootstrap
                    bShown = false;
                    scope.htLastUnknownNode = false;

                    angular.element($window).bind('resize',function(e) {
                        if (bShown && htLastNode.category === 'UNKNOWN_GROUP') {
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
                        if (!scope.$$phase) {
                            scope.$digest();
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

                        if (node.serviceType !== "UNKNOWN_GROUP") {
                            scope.showNodeResponseSummary = true;
                            scope.showNodeLoad = true;

                            renderResponseSummary('forNode', node.applicationName, node.histogram, '100%', '150px');
                            renderLoad('forNode', node.applicationName, node.timeSeriesHistogram, '100%', '220px', true);
                        } else if (node.serviceType === 'UNKNOWN_GROUP'){
                            scope.showNodeResponseSummaryForUnknown = (scope.oNavbarVo.getPeriod() <= cfg.maxTimeToShowLoadAsDefaultForUnknown) ? false : true;
                            renderAllChartWhichIsVisible(node);
                            scope.htLastUnknownNode = angular.copy(node);

                            $timeout(function () {
                                element.find('[data-toggle="tooltip"]').tooltip('destroy').tooltip();
                            });
                        }
                        if (!scope.$$phase) {
                            scope.$digest();
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
                                var visible = isVisible(el.get(0), 1);
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
                        scope.$broadcast('responseTimeChart.initAndRenderWithData.' + namespace, histogram, w, h, false, true);
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
                        scope.$broadcast('loadChart.initAndRenderWithData.' + namespace, timeSeriesHistogram, w, h, useChartCursor);
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
                    scope.showNodeDetailInformation = function (index) {
                        htLastNode = htLastNode.unknownNodeGroup[index];
                        showDetailInformation(htLastNode);
                        scope.$emit('nodeInfoDetail.showDetailInformationClicked', htQuery, htLastNode);
                    };

                    /**
                     * go back to unknown node
                     */
                    scope.goBackToUnknownNode = function () {
                        htLastNode = angular.copy(scope.htLastUnknownNode);
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
                    scope.renderNodeResponseSummary = function (applicationName, index) {
                        if (angular.isUndefined(htUnknownResponseSummary[applicationName])) {
                            htUnknownResponseSummary[applicationName] = true;
                            renderResponseSummary(null, applicationName, htLastNode.unknownNodeGroup[index].histogram, '360px', '180px');
                        }
                    };

                    /**
                     * scope render node load
                     * @param applicationName
                     * @param index
                     */
                    scope.renderNodeLoad = function (applicationName, index) {
                        if (angular.isUndefined(htUnknownLoad[applicationName])) {
                            htUnknownLoad[applicationName] = true;
                            renderLoad(null, applicationName, htLastNode.unknownNodeGroup[index].timeSeriesHistogram, '360px', '200px', true);
                        }
                    };

                    /**
                     * render node agent charts
                     * @param applicationName
                     */
                    scope.renderNodeAgentCharts = function (applicationName) {
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

                    /**
                     * scope event on nodeInfoDetails.initialize
                     */
                    scope.$on('nodeInfoDetails.initialize', function (event, e, query, node, mapData, navbarVo) {
                        show();
                        if (angular.equals(htLastNode, node)) {
                            if (htLastNode.category === 'UNKNOWN_GROUP') {
                                renderAllChartWhichIsVisible(htLastNode);
                            }
                            return;
                        }
                        reset();
                        htQuery = query;
                        htLastNode = node;
                        scope.htLastUnknownNode = false;
                        scope.oNavbarVo = navbarVo;
                        htServermapData = mapData;
                        showDetailInformation(node);
                    });

                    /**
                     * scope event on nodeInfoDetails.hide
                     */
                    scope.$on('nodeInfoDetails.hide', function (event) {
                        hide();
                    });

                    /**
                     * scope event on nodeInfoDetails.lazyRendering
                     */
                    scope.$on('nodeInfoDetails.lazyRendering', function (event, e) {
                        renderAllChartWhichIsVisible(htLastNode);
                    });

                    scope.$on('responseTimeChart.itemClicked.forNode', function (event, data) {
//                        console.log('on responseTimeChart.itemClicked.forNode', data);
                    });

                }
            };
    }
]);
