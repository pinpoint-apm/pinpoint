'use strict';

pinpointApp.constant('linkInfoDetailsConfig', {
    linkStatisticsUrl: '/linkStatistics.pinpoint',
    myColors: ["#2ca02c", "#3c81fa", "#f8c731", "#f69124", "#f53034"],
    maxTimeToShowLoadAsDefaultForUnknown:  60 * 60 * 12 // 12h
});

pinpointApp.directive('linkInfoDetails', [ 'linkInfoDetailsConfig', 'HelixChartVo', '$filter', 'ServerMapFilterVo',  'filteredMapUtil', 'humanReadableNumberFormatFilter', '$timeout', 'isVisible', 'ServerMapHintVo',
    function (cfg, HelixChartVo, $filter, ServerMapFilterVo, filteredMapUtil, humanReadableNumberFormatFilter, $timeout, isVisible, ServerMapHintVo) {
        return {
            restrict: 'EA',
            replace: true,
            templateUrl: 'views/linkInfoDetails.html',
            link: function postLink(scope, element, attrs) {

                // define private variables
                var htQuery, htTargetRawData, htLastLink, htUnknownResponseSummary, htUnknownLoad;

                // define private variables of methods;
                var reset, showDetailInformation, renderLoad, renderResponseSummary, parseHistogramForD3,
                    renderAllChartWhichIsVisible, hide, show;

                scope.linkSearch = '';
                /**
                 * reset
                 */
                reset = function () {
                    htQuery = false;
                    htLastLink = false;
                    htTargetRawData = false;
                    htUnknownResponseSummary = {};
                    htUnknownLoad = {};
                    scope.linkCategory = null;
                    scope.targetinfo = null;
                    scope.sourceinfo = null;
                    scope.showLinkInfoDetails = false;
                    scope.showLinkResponseSummary = false;
                    scope.ShowLinkLoad = false;
                    scope.linkSearch = '';
                    scope.linkOrderBy = 'count';
                    scope.linkOrderByNameClass = '';
                    scope.linkOrderByCountClass = 'glyphicon-sort-by-order-alt';
                    scope.linkOrderByDesc = true;
                    if (!scope.$$phase) {
                        scope.$digest();
                    }
                };

                /**
                 * show link detail information of scope
                 * @param applicationName
                 */
                scope.showLinkDetailInformation = function (applicationName) {
                    htLastLink = htTargetRawData[applicationName];
                    showDetailInformation(htLastLink);
                    scope.$emit('linkInfoDetail.showDetailInformationClicked', htQuery, htLastLink);
                };

                /**
                 * show detail information
                 * @param link
                 */
                showDetailInformation = function (link) {
                    if (link.targetRawData) {
                        htTargetRawData = link.targetRawData;
                        scope.linkCategory = 'UnknownLinkInfoBox';
                        scope.sourceInfo = link.sourceInfo;
                        scope.targetInfo = link.targetInfo;

                        scope.showLinkResponseSummaryForUnknown = (scope.oNavbarVo.getPeriod() <= cfg.maxTimeToShowLoadAsDefaultForUnknown) ? false : true;

                        renderAllChartWhichIsVisible(link);
                        $timeout(function () {
                            element.find('[data-toggle="tooltip"]').tooltip('destroy').tooltip();
                        });
                    } else {
                        scope.linkCategory = 'LinkInfoBox';
                        scope.ShowLinkLoad = true;
                        scope.showLinkResponseSummary = true;
                        renderResponseSummary('forLink', link.histogram, '100%', '150px');
                        renderLoad('forLink', link.timeSeriesHistogram, '100%', '220px');
                    }

                    scope.showLinkInfoDetails = true;
                    if (!scope.$$phase) {
                        scope.$digest();
                    }
                };

                /**
                 * render all chart which is visible
                 * @param link
                 */
                renderAllChartWhichIsVisible = function (link) {
                    $timeout(function () {
                        for (var key in link.targetInfo) {
                            var applicationName = link.targetInfo[key].applicationName,
                                className = $filter('applicationNameToClassName')(applicationName);
                            if (angular.isDefined(htUnknownResponseSummary[applicationName])) continue;
                            if (angular.isDefined(htUnknownLoad[applicationName])) continue;

                            var elQuery = '.linkInfoDetails .summaryCharts_' + className,
                                el = angular.element(elQuery);
                            var visible = isVisible(el.get(0));
                            if (!visible) continue;

                            if (scope.showLinkResponseSummaryForUnknown) {
                                htUnknownResponseSummary[applicationName] = true;
                                renderResponseSummary('forLink_' + className, link.targetRawData[applicationName].histogram, '360px', '120px');
                            } else {
                                htUnknownLoad[applicationName] = true;
                                renderLoad('forLink_' + className, link.targetRawData[applicationName].timeSeriesHistogram, '360px', '120px');
                            }
                        }
                    });
                };

                /**
                 * render response summary
                 * @param namespace
                 * @param histogram
                 * @param w
                 * @param h
                 */
                renderResponseSummary = function (namespace, histogram, w, h) {
                    scope.$broadcast('responseTimeChart.initAndRenderWithData.' + namespace, histogram, w, h, true, true);
                    scope.$on('responseTimeChart.itemClicked.' + namespace, function (event, data) {
                        var label = data.responseTime,
                            values = data.count;
                        var oServerMapFilterVo = new ServerMapFilterVo();
                        oServerMapFilterVo
                            .setMainApplication(htLastLink.filterApplicationName)
                            .setMainServiceTypeCode(htLastLink.filterApplicationServiceTypeCode)
                            .setFromApplication(htLastLink.sourceInfo.applicationName)
                            .setFromServiceType(htLastLink.sourceInfo.serviceType)
                            .setToApplication(htLastLink.targetInfo.applicationName)
                            .setToServiceType(htLastLink.targetInfo.serviceType);
                        if (htLastLink.sourceInfo.serviceType === 'USER') {
                            oServerMapFilterVo
                                .setFromApplication('USER')
                                .setFromServiceType('USER');
                        }

                        if (label.toLowerCase() === 'error') {
                            oServerMapFilterVo.setIncludeException(true);
                        } else if (label.toLowerCase() === 'slow') {
                            oServerMapFilterVo
                                .setResponseFrom(filteredMapUtil.getStartValueForFilterByLabel(label, values) * 1000)
                                .setResponseTo('max');
                        } else {
                            oServerMapFilterVo
                                .setResponseFrom(filteredMapUtil.getStartValueForFilterByLabel(label, values) * 1000)
                                .setResponseTo(parseInt(label, 10) * 1000);
                        }

                        var oServerMapHintVo = new ServerMapHintVo();
                        if (htLastLink.sourceInfo.isWas && htLastLink.targetInfo.isWas) {
                            oServerMapHintVo.setHint(htLastLink.targetInfo.applicationName, htLastLink.filterTargetRpcList)
                        }

                        scope.$emit('linkInfoDetails.ResponseSummary.barClicked', oServerMapFilterVo, oServerMapHintVo);
                    });
                };

                /**
                 * render load
                 * @param namespace
                 * @param timeSeriesHistogram
                 * @param w
                 * @param h
                 * @param useChartCursor
                 */
                renderLoad = function (namespace, timeSeriesHistogram, w, h, useChartCursor) {
                    scope.$broadcast('loadChart.initAndRenderWithData.' + namespace, timeSeriesHistogram, w, h, useChartCursor);
                };

                /**
                 * scope render link response summary
                 * @param applicationName
                 */
                scope.renderLinkResponseSummary = function (applicationName) {
                    if (angular.isUndefined(htUnknownResponseSummary[applicationName])) {
                        htUnknownResponseSummary[applicationName] = true;
                        var className = $filter('applicationNameToClassName')(applicationName);
                        renderResponseSummary('forLink_' + className, htLastLink.targetRawData[applicationName].histogram, '360px', '120px');
                    }
                };

                /**
                 * scope render link load
                 * @param applicationName
                 */
                scope.renderLinkLoad = function (applicationName) {
                    if (angular.isUndefined(htUnknownLoad[applicationName])) {
                        htUnknownLoad[applicationName] = true;
                        var className = $filter('applicationNameToClassName')(applicationName);
                        renderLoad('forLink_' + className, htLastLink.targetRawData[applicationName].timeSeriesHistogram, '360px', '120px');
                    }
                };

                /**
                 * hide
                 */
                hide = function () {
                    element.hide();
                };

                /**
                 * show
                 */
                show = function () {
                    element.show();
                };

                /**
                 * scope link order by name
                 */
                scope.linkOrderByName = function () {
                    if (scope.linkOrderBy === 'applicationName') {
                        scope.linkOrderByDesc = !scope.linkOrderByDesc;
                        if (scope.linkOrderByNameClass === 'glyphicon-sort-by-alphabet-alt') {
                            scope.linkOrderByNameClass = 'glyphicon-sort-by-alphabet';
                        } else {
                            scope.linkOrderByNameClass = 'glyphicon-sort-by-alphabet-alt';
                        }
                    } else {
                        scope.linkOrderByNameClass = 'glyphicon-sort-by-alphabet-alt';
                        scope.linkOrderByCountClass = '';
                        scope.linkOrderByDesc = true;
                        scope.linkOrderBy = 'applicationName';
                    }
                    renderAllChartWhichIsVisible(htLastLink);
                };

                /**
                 * scope link order by count
                 */
                scope.linkOrderByCount = function () {
                    if (scope.linkOrderBy === 'count') {
                        scope.linkOrderByDesc = !scope.linkOrderByDesc;
                        if (scope.linkOrderByCountClass === 'glyphicon-sort-by-order-alt') {
                            scope.linkOrderByCountClass = 'glyphicon-sort-by-order';
                        } else {
                            scope.linkOrderByCountClass = 'glyphicon-sort-by-order-alt';
                        }
                    } else {
                        scope.linkOrderByCountClass = 'glyphicon-sort-by-order-alt';
                        scope.linkOrderByNameClass = '';
                        scope.linkOrderByDesc = true;
                        scope.linkOrderBy = 'count';
                    }
                    renderAllChartWhichIsVisible(htLastLink);
                };

                /**
                 * show unknown link by
                 * @param linkSearch
                 * @param target
                 * @returns {boolean}
                 */
                scope.showUnknownLinkBy = function (linkSearch, target) {
                    if (linkSearch) {
                        if (target.applicationName.indexOf(linkSearch) > -1 ||
                            target.count.toString().indexOf(linkSearch) > -1) {
                            return true;
                        } else {
                            return false;
                        }
                    } else {
                        return true;
                    }
                };

                /**
                 * passing transaction map from link info details
                 * @param toApplicationName
                 * @param toServiceType
                 */
                scope.passingTransactionMapFromLinkInfoDetails = function (toApplicationName, toServiceType) {
                    var oServerMapFilterVo = new ServerMapFilterVo();
                    oServerMapFilterVo
                        .setMainApplication(htLastLink.filterApplicationName)
                        .setMainServiceTypeCode(htLastLink.filterApplicationServiceTypeCode)
                        .setFromApplication(htLastLink.sourceInfo.applicationName)
                        .setFromServiceType(htLastLink.sourceInfo.serviceType)
                        .setToApplication(toApplicationName)
                        .setToServiceType(toServiceType);

                    var oServerMapHintVo = new ServerMapHintVo();
                    if (htLastLink.sourceInfo.isWas && htLastLink.targetInfo.isWas) {
                        oServerMapHintVo.setHint(htLastLink.toNode.text, htLastLink.filterTargetRpcList)
                    }
                    scope.$broadcast('linkInfoDetails.openFilteredMap', oServerMapFilterVo, oServerMapHintVo);
                };

                scope.linkSearchChange = function () {
                    renderAllChartWhichIsVisible(htLastLink);
                };

                /**
                 * scope event on linkInfoDetails.hide
                 */
                scope.$on('linkInfoDetails.hide', function (event) {
                    hide();
                });

                /**
                 * scope event on linkInfoDetails.linkClicked
                 */
                scope.$on('linkInfoDetails.initialize', function (event, e, query, link) {
                    show();
                    if (angular.equals(htLastLink, link)) return;
                    reset();
                    htQuery = query;
                    htLastLink = link;
                    showDetailInformation(link);
                });

                /**
                 * scope event on linkInfoDetails.lazyRendering
                 */
                scope.$on('linkInfoDetails.lazyRendering', function (event, e) {
                    renderAllChartWhichIsVisible(htLastLink);
                });
            }
        };
    } ]);
