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
                    var htServermapData, htLastNode, htUnknownResponseSummary, htUnknownLoad, htTargetRawData, htQuery,
                        htAgentChartRendered, bShown;

                    // define private variables of methods
                    var reset, showDetailInformation, renderAllChartWhichIsVisible, hide, show, renderResponseSummary,
                        renderLoad;

                    // bootstrap
                    bShown = false;

                    angular.element($window).bind('resize',function(e) {
                        if (bShown && htLastNode.category === 'UNKNOWN_GROUP') {
                            renderAllChartWhichIsVisible(htLastNode);
                        }
                    });

                    /**
                     * reset
                     */
                    reset = function () {
                        htUnknownResponseSummary = {};
                        htUnknownLoad = {};
                        htAgentChartRendered = {};
                        htTargetRawData = false;
                        htQuery = false;
                        scope.showNodeInfoDetails = false;
                        scope.node = false;
                        scope.unknownGroup = null;
                        scope.hosts = null;
                        scope.showNodeServers = false;
                        scope.agents = null;
                        scope.showAgents = false;
                        scope.showNodeResponseSummaryForUnknown = false;
                        scope.showNodeResponseSummary = false;
                        scope.showNodeLoad = false;
                        scope.agentHistogram = false;
                        scope.nodeOrderBy = 'count';
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
                        scope.unknownGroup = node.textArr;
                        scope.serverList = node.serverList;
                        scope.showNodeServers = _.isEmpty(scope.serverList) ? false : true;
                        scope.agentHistogram = node.agentHistogram;

                        if (!node.targetRawData && node.category !== "UNKNOWN_GROUP") {
                            scope.showNodeResponseSummary = true;
                            scope.showNodeLoad = true;

                            renderResponseSummary('forNode', node.text, node.histogram, '100%', '150px');
                            renderLoad('forNode', node.text, node.timeSeriesHistogram, '100%', '220px', true);
                        } else if (node.category === 'UNKNOWN_GROUP'){
                            htTargetRawData = node.targetRawData;
                            scope.showNodeResponseSummaryForUnknown = (scope.oNavbarVo.getPeriod() <= cfg.maxTimeToShowLoadAsDefaultForUnknown) ? false : true;

                            renderAllChartWhichIsVisible(node);

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
                            for (var key in node.textArr) {
                                var applicationName = node.textArr[key].applicationName,
                                    className = $filter('applicationNameToClassName')(applicationName);
                                if (angular.isDefined(htUnknownResponseSummary[applicationName])) continue;
                                if (angular.isDefined(htUnknownLoad[applicationName])) continue;

                                var elQuery = '.nodeInfoDetails .summaryCharts_' + className,
                                    el = angular.element(elQuery);
                                var visible = isVisible(el.get(0));
                                if (!visible) continue;

                                if (scope.showNodeResponseSummaryForUnknown) {
                                    htUnknownResponseSummary[applicationName] = true;
                                    renderResponseSummary(null, applicationName, node.targetRawData[applicationName].histogram, '360px', '120px');
                                } else {
                                    htUnknownLoad[applicationName] = true;
                                    renderLoad(null, applicationName, node.targetRawData[applicationName].timeSeriesHistogram, '360px', '120px');
                                }
                            }
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
                     * @param applicationName
                     */
                    scope.showNodeDetailInformation = function (applicationName) {
                        htLastNode = htTargetRawData[applicationName];
                        showDetailInformation(htLastNode);
                        scope.$emit('nodeInfoDetail.showDetailInformationClicked', htQuery, htLastNode);
                    };

                    /**
                     * scope render node response summary
                     * @param applicationName
                     */
                    scope.renderNodeResponseSummary = function (applicationName) {
                        if (angular.isUndefined(htUnknownResponseSummary[applicationName])) {
                            htUnknownResponseSummary[applicationName] = true;
                            renderResponseSummary(null, applicationName, htLastNode.targetRawData[applicationName].histogram, '360px', '120px');
                        }
                    };

                    /**
                     * scope render node load
                     * @param applicationName
                     */
                    scope.renderNodeLoad = function (applicationName) {
                        if (angular.isUndefined(htUnknownLoad[applicationName])) {
                            htUnknownLoad[applicationName] = true;
                            renderLoad(null, applicationName, htLastNode.targetRawData[applicationName].timeSeriesHistogram, '360px', '120px');
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
                        if (scope.nodeOrderBy === 'count') {
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
                            scope.nodeOrderBy = 'count';
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
                                node.count.toString().indexOf(nodeSearch) > -1) {
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
