(function() {
	'use strict';
	/**
	 * (en)linkInfoDetailsDirective 
	 * @ko linkInfoDetailsDirective
	 * @group Directive
	 * @name linkInfoDetailsDirective
	 * @class
	 */	
	pinpointApp.constant('linkInfoDetailsDirectiveConfig', {
	    maxTimeToShowLoadAsDefaultForUnknown:  60 * 60 * 12 // 12h
	});
	
	pinpointApp.directive('linkInfoDetailsDirective', [ 'linkInfoDetailsDirectiveConfig', "$rootScope", '$filter', 'ServerMapFilterVoService',  'filteredMapUtilService', '$timeout', 'isVisibleService', 'ServerMapHintVoService', "AnalyticsService", '$window',
	    function (cfg, $rootScope, $filter, ServerMapFilterVoService, filteredMapUtilService, $timeout, isVisibleService, ServerMapHintVoService, analyticsService, $window) {
	        return {
	            restrict: 'EA',
	            replace: true,
	            templateUrl: 'features/linkInfoDetails/linkInfoDetails.html?v=' + G_BUILD_TIME,
	            scope: true,
	            link: function postLink(scope, element, attrs) {
	
	                // define private variables
	                var htQuery, htLastLink, htUnknownResponseSummary, htUnknownLoad, bShown,
	                    htAgentChartRendered, bResponseSummaryForLinkRendered, bLoadForLinkRendered, sLastKey;
	
	                // define private variables of methods;
	                var reset, showDetailInformation, renderLoad, renderResponseSummaryWithHistogram, renderAllChartWhichIsVisible,
	                    hide, show, renderResponseSummaryWithLink, getUnknownLink;
	
	                // bootstrap
	                scope.linkSearch = '';
	                bResponseSummaryForLinkRendered = false;
	                bLoadForLinkRendered = false;
	                bShown = false;
	                scope.htLastUnknownLink = false;
	
	                // angular.element($window).bind('resize',function(e) {
	                //     if (bShown && htLastLink.targetRawData) {
	                //         renderAllChartWhichIsVisible(htLastLink);
	                //     }
	                // });
					//
	                // element
	                //     .find('.unknown-list')
	                //     .bind('scroll', function (e) {
	                //         renderAllChartWhichIsVisible(htLastLink);
	                //     });
	
	                /**
	                 * reset
	                 */
	                reset = function () {
	                    htQuery = false;
	                    htLastLink = false;
	                    htUnknownResponseSummary = {};
	                    htAgentChartRendered = {};
	                    htUnknownLoad = {};
	                    scope.linkCategory = null;
	                    scope.unknownLinkGroup = null;
	                    scope.showLinkInfoDetails = false;
	                    scope.showLinkResponseSummary = false;
	                    scope.showLinkLoad = false;
	                    scope.showLinkServers = false;
	                    scope.linkSearch = '';
	                    scope.linkOrderBy = 'totalCount';
	                    scope.linkOrderByNameClass = '';
	                    scope.linkOrderByCountClass = 'glyphicon-sort-by-order-alt';
	                    scope.linkOrderByDesc = true;
	                    scope.sourceApplicationName = '';
	                    scope.sourceHistogram = false;
	//                    scope.fromNode = false;
	                    scope.namespace = null;
	                    if (!scope.$$phase) {
	                        scope.$digest();
	                    }
	                };
	
	                /**
	                 * show detail information
	                 * @param link
	                 */
					scope.pagingSize = 3;
	                showDetailInformation = function (link) {
						scope.showLinkInfoDetails = true;
	                    scope.link = link;
	                    scope.unknownLinkGroup = link.unknownLinkGroup;

	                    if (link.unknownLinkGroup) {
	                        scope.unknownLinkGroup = link.unknownLinkGroup;
	                        scope.htLastUnknownLink = link;
	
	                        scope.showLinkResponseSummaryForUnknown = (scope.oNavbarVoService.getPeriod() <= cfg.maxTimeToShowLoadAsDefaultForUnknown) ? false : true;
	
	                        renderAllChartWhichIsVisible(scope.unknownLinkGroup);
	                        $timeout(function () {
	                            element.find('[data-toggle="tooltip"]').tooltip('destroy').tooltip();
	                        });
	                    } else {
	                        scope.showLinkResponseSummary = true;
	                        scope.showLinkLoad = true;
	                        renderResponseSummaryWithHistogram('forLink', link.targetInfo.applicationName, link.histogram, '100%', '150px');
	                        renderLoad('forLink', link.targetInfo.applicationName, link.timeSeriesHistogram, '100%', '220px', true);
	
	                        scope.showLinkServers = _.isEmpty(link.sourceHistogram) ? false : true;
	                        scope.sourceApplicationName = link.sourceInfo.applicationName;
	                        scope.sourceHistogram = link.sourceHistogram;
	                        scope.fromNode = link.fromNode;
	                    }
	
	                    if (!scope.$$phase) {
	                        scope.$digest();
	                    }
	                };
	
	                /**
	                 * render all chart which is visible
	                 * @param link
	                 */
	                renderAllChartWhichIsVisible = function (linkList, forPaging) {
	                    $timeout(function () {
	                        angular.forEach(linkList, function (link) {
	                            var applicationName = link.targetInfo.applicationName,
	                                className = $filter('applicationNameToClassName')(applicationName);
								if ( forPaging !== true ) {
									if (angular.isDefined(htUnknownResponseSummary[applicationName])) return;
									if (angular.isDefined(htUnknownLoad[applicationName])) return;
								}
	
	                            var elQuery = '.linkInfoDetails .summaryCharts_' + className,
	                                el = angular.element(elQuery);

								if ( el.length !== 0 ) {
									var visible = isVisibleService(el.get(0), 1);
									if (!visible) return;

									if (scope.showLinkResponseSummaryForUnknown) {
										htUnknownResponseSummary[applicationName] = true;
										renderResponseSummaryWithLink(null, link, '100%', '180px');
									} else {
										htUnknownLoad[applicationName] = true;
										renderLoad(null, applicationName, link.timeSeriesHistogram, '100%', '200px', true);
									}
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
	                renderResponseSummaryWithHistogram = function (namespace, toApplicationName, histogram, w, h) {
	                    var className = $filter('applicationNameToClassName')(toApplicationName);
						namespace = namespace || 'forLink_' + className;
	                    if (namespace === 'forLink' && bResponseSummaryForLinkRendered) {
	                        // scope.$broadcast('responseTimeChartDirective.updateData.' + namespace, histogram);
	                    } else {
	                        if (namespace === 'forLink') {
	                            bResponseSummaryForLinkRendered = true;
	                        }
	                        scope.$on('responseTimeChartDirective.itemClicked.' + namespace, function (event, data) {
	                            var label = data.responseTime,
	                                values = data.count;
	                            var oServerMapFilterVoService = new ServerMapFilterVoService();
	                            oServerMapFilterVoService
	                                .setMainApplication(htLastLink.filterApplicationName)
	                                .setMainServiceTypeCode(htLastLink.filterApplicationServiceTypeCode);
	
	                            if (htLastLink.sourceInfo.serviceType === 'USER') {
	                                oServerMapFilterVoService
	                                    .setFromApplication('USER')
	                                    .setFromServiceType('USER');
	                            } else {
	                                oServerMapFilterVoService
	                                    .setFromApplication(htLastLink.sourceInfo.applicationName)
	                                    .setFromServiceType(htLastLink.sourceInfo.serviceType);
	                            }
	
	                            oServerMapFilterVoService
	                                .setToApplication(htLastLink.targetInfo.applicationName)
	                                .setToServiceType(htLastLink.targetInfo.serviceType);
	
	                            if (label.toLowerCase() === 'error') {
	                                oServerMapFilterVoService.setIncludeException(true);
	                            } else if (label.toLowerCase() === 'slow') {
	                                oServerMapFilterVoService
	                                    .setResponseFrom(filteredMapUtilService.getStartValueForFilterByLabel(label, values) * 1000)
	                                    .setIncludeException(false)
	                                    .setResponseTo('max');
	                            } else {
	                                oServerMapFilterVoService
	                                    .setResponseFrom(filteredMapUtilService.getStartValueForFilterByLabel(label, values) * 1000)
	                                    .setIncludeException(false)
	                                    .setResponseTo(parseInt(label, 10) * 1000);
	                            }
	
	                            var oServerMapHintVoService = new ServerMapHintVoService();
	                            if (htLastLink.sourceInfo.isWas && htLastLink.targetInfo.isWas) {
	                                oServerMapHintVoService.setHint(htLastLink.targetInfo.applicationName, htLastLink.filterTargetRpcList);
	                            }
	                            scope.$emit('linkInfoDetailsDirective.ResponseSummary.barClicked', oServerMapFilterVoService, oServerMapHintVoService);
	                        });
	                    }
						scope.$broadcast('responseTimeChartDirective.initAndRenderWithData.' + namespace, histogram, w, h, true, true);
	                };
	
	                renderResponseSummaryWithLink = function (namespace, link, w, h) {
	                    var className = $filter('applicationNameToClassName')(link.targetInfo.applicationName);
						namespace = namespace || 'forLink_' + className;
	
	                    if (namespace === 'forLink' && bResponseSummaryForLinkRendered) {
							// scope.$broadcast('responseTimeChartDirective.initAndRenderWithData.' + namespace, link.histogram, w, h, true, true);
	                    } else {
	                        if (namespace === 'forLink') {
	                            bResponseSummaryForLinkRendered = true;
	                        }
	                    }
						scope.$broadcast('responseTimeChartDirective.initAndRenderWithData.' + namespace, link.histogram, w, h, true, true);
						scope.$on('responseTimeChartDirective.itemClicked.' + namespace, function (event, data) {
							var label = data.responseTime,
								values = data.count;
							var oServerMapFilterVoService = new ServerMapFilterVoService();
							oServerMapFilterVoService
								.setMainApplication(link.filterApplicationName)
								.setMainServiceTypeCode(link.filterApplicationServiceTypeCode);

							if (link.sourceInfo.serviceType === 'USER') {
								oServerMapFilterVoService
									.setFromApplication('USER')
									.setFromServiceType('USER');
							} else {
								oServerMapFilterVoService
									.setFromApplication(link.sourceInfo.applicationName)
									.setFromServiceType(link.sourceInfo.serviceType);
							}

							oServerMapFilterVoService
								.setToApplication(link.targetInfo.applicationName)
								.setToServiceType(link.targetInfo.serviceType);

							if (label.toLowerCase() === 'error') {
								oServerMapFilterVoService.setIncludeException(true);
							} else if (label.toLowerCase() === 'slow') {
								oServerMapFilterVoService
									.setResponseFrom(filteredMapUtilService.getStartValueForFilterByLabel(label, values) * 1000)
									.setIncludeException(false)
									.setResponseTo('max');
							} else {
								oServerMapFilterVoService
									.setResponseFrom(filteredMapUtilService.getStartValueForFilterByLabel(label, values) * 1000)
									.setIncludeException(false)
									.setResponseTo(parseInt(label, 10) * 1000);
							}

							var oServerMapHintVoService = new ServerMapHintVoService();
							if (link.sourceInfo.isWas && link.targetInfo.isWas) {
								oServerMapHintVoService.setHint(link.targetInfo.applicationName, link.filterTargetRpcList);
							}
							scope.$emit('linkInfoDetailsDirective.ResponseSummary.barClicked', oServerMapFilterVoService, oServerMapHintVoService);
						});
	                };
	
	                /**
	                 * render load
	                 * @param namespace
	                 * @param toApplicationName
	                 * @param w
	                 * @param h
	                 * @param useChartCursor
	                 */
	                renderLoad = function (namespace, toApplicationName, timeSeriesHistogram, w, h, useChartCursor) {
	                    var className = $filter('applicationNameToClassName')(toApplicationName);
						namespace = namespace || 'forLink_' + className;
	                    if (namespace === 'forLink' && bLoadForLinkRendered) {
							// scope.$broadcast('loadChartDirective.initAndRenderWithData.' + namespace, timeSeriesHistogram, w, h, useChartCursor);
	                    } else {
	                        if (namespace === 'forLink') {
	                            bLoadForLinkRendered = true;
	                        }
	                    }
						scope.$broadcast('loadChartDirective.initAndRenderWithData.' + namespace, timeSeriesHistogram, w, h, useChartCursor);
	                };
	                getUnknownLink = function( key ) {
	                	var link = null;
	                	for( var i = 0 ; i < htLastLink.unknownLinkGroup.length ; i++ ) {
	                		if (htLastLink.unknownLinkGroup[i].key === key) {
	                			link = htLastLink.unknownLinkGroup[i];
	                			break;
	                		}
	                	}
	                	return link;
	                };
					function calcuPagingSize( linkList ) {
						if ( linkList ) {
							var count = parseInt(linkList.length / scope.pagingSize) + ( linkList.length % scope.pagingSize === 0 ? 0 : 1 );
							scope.pagingCount = [];

							for( var i = 1 ; i <= count ; i++ ) {
								scope.pagingCount.push( i );
							}
						}
					}
	
	                /**
	                 * show link detail information of scope
	                 * @param index
	                 */
	                scope.showLinkDetailInformation = function (key) {
	                	htLastLink = getUnknownLink(key);
	                    showDetailInformation(htLastLink);
						$rootScope.$broadcast("infoDetail.showDetailInformationClicked", htQuery, htLastLink);
	                };
	
	                /**
	                 * go back to unknown link
	                 */
	                scope.goBackToUnknownLink = function () {
	                    htLastLink = scope.htLastUnknownLink;
	                    htUnknownResponseSummary = {};
	                    htUnknownLoad = {};
	                    showDetailInformation(htLastLink, scope.currentPage);
						$rootScope.$broadcast("infoDetail.showDetailInformationClicked", htQuery, htLastLink);
	                };
	
	                /**
	                 * scope render link response summary
	                 * @param applicationName
	                 * @param index
	                 */
	                scope.renderLinkResponseSummary = function (applicationName, key) {
	                    if (angular.isUndefined(htUnknownResponseSummary[applicationName])) {
	                        htUnknownResponseSummary[applicationName] = true;
	                        renderResponseSummaryWithLink(null, getUnknownLink(key), '100%', '180px');
	                    }
	                };
	
	                /**
	                 * scope render link load
	                 * @param applicationName
	                 * @param index
	                 */
	                scope.renderLinkLoad = function (applicationName, key) {
	                    if (angular.isUndefined(htUnknownLoad[applicationName])) {
	                        htUnknownLoad[applicationName] = true;
	                        renderLoad(null, applicationName, getUnknownLink(key).timeSeriesHistogram, '100%', '200px', true);
	                    }
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
	                 * scope link order by name
	                 */
	                scope.linkOrderByName = function () {
	                    if (scope.linkOrderBy === 'targetInfo.applicationName') {
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
	                        scope.linkOrderBy = 'targetInfo.applicationName';
	                    }
	                    renderAllChartWhichIsVisible(htLastLink, true);
	                };
	
	                /**
	                 * scope link order by count
	                 */
	                scope.linkOrderByCount = function () {
	                    if (scope.linkOrderBy === 'totalCount') {
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
	                        scope.linkOrderBy = 'totalCount';
	                    }
	                    renderAllChartWhichIsVisible(htLastLink, true);
	                };
	
	                /**
	                 * show unknown link by
	                 * @param linkSearch
	                 * @param link
	                 * @returns {boolean}
	                 */
	                scope.showUnknownLinkBy = function (linkSearch, link) {
	                    if (linkSearch) {
	                        if (link.targetInfo.applicationName.indexOf(linkSearch) > -1 || link.totalCount.toString().indexOf(linkSearch) > -1) {
	                            return true;
	                        } else {
	                            return false;
	                        }
	                    } else {
	                        return true;
	                    }
	                };

					scope.movePaging = function( nextPage ) {
						if (scope.currentPage == nextPage) return;
						scope.currentPage = nextPage;
						renderAllChartWhichIsVisible(scope.unknownLinkGroup, true);
					};
	                /**
	                 * passing transaction map from link info details
	                 * @param index
	                 */
	                scope.passingTransactionMapFromLinkInfoDetails = function (key) {
	                	var link = getUnknownLink(key);
	                    var oServerMapFilterVoService = new ServerMapFilterVoService();
	                    oServerMapFilterVoService
	                        .setMainApplication(link.filterApplicationName)
	                        .setMainServiceTypeCode(link.filterApplicationServiceTypeCode)
	                        .setFromApplication(link.sourceInfo.applicationName)
	                        .setFromServiceType(link.sourceInfo.serviceType)
	                        .setToApplication(link.targetInfo.applicationName)
	                        .setToServiceType(link.targetInfo.serviceType);
	
	                    var oServerMapHintVoService = new ServerMapHintVoService();
	                    if (link.sourceInfo.isWas && link.targetInfo.isWas) {
	                        oServerMapHintVoService.setHint(link.toNode.applicationName, link.filterTargetRpcList);
	                    }
	                    //scope.$broadcast('linkInfoDetailsDirective.openFilteredMap', oServerMapFilterVoService, oServerMapHintVoService);
	                    scope.$emit('linkInfoDetailsDirective.openFilteredMap', oServerMapFilterVoService, oServerMapHintVoService);
	                };
	
	                /**
	                 * open filter wizard
	                 * @param index
	                 */
	                scope.openFilterWizard = function (key) {
	                	var link = getUnknownLink(key);
	                    //scope.$broadcast('linkInfoDetailsDirective.openFilterWizard', link);
	                    scope.$emit('linkInfoDetailsDirective.openFilterWizard', link);
	                };
	
	                /**
	                 * render link agent charts
	                 * @param applicationName
	                 */
	                scope.renderLinkAgentCharts = function (applicationName) {
	                    if (angular.isDefined(htAgentChartRendered[applicationName])) return;
	                    htAgentChartRendered[applicationName] = true;
	                    renderResponseSummaryWithHistogram(null, applicationName, htLastLink.sourceHistogram[applicationName], '100%', '150px');
	                    renderLoad(null, applicationName, htLastLink.sourceTimeSeriesHistogram[applicationName], '100%', '200px', true);
	                };
	
	                /**
	                 * link search change
	                 */
	                scope.linkSearchChange = function () {
						scope.currentPage = 1;
						scope.unknownLinkGroup = [];
						for( var i = 0 ; i < htLastLink.unknownLinkGroup.length ; i++ ) {
							var oLink = htLastLink.unknownLinkGroup[i];
							if (scope.linkSearch) {
								if ( oLink.targetInfo.applicationName.indexOf(scope.linkSearch) > -1 || oLink.totalCount.toString().indexOf(scope.linkSearch) > -1) {
									scope.unknownLinkGroup.push( oLink );
								}
							} else {
								scope.unknownLinkGroup.push( oLink );
							}
						}
						calcuPagingSize( scope.unknownLinkGroup );
	                    renderAllChartWhichIsVisible( scope.unknownLinkGroup, true );
	                };
	
	                /**
	                 * scope event on linkInfoDetailsDirective.hide
	                 */
	                scope.$on('linkInfoDetailsDirective.hide', function (event) {
	                    hide();
	                });
	
	                /**
	                 * scope event on linkInfoDetailsDirective.linkClicked
	                 */
	                scope.$on('linkInfoDetailsDirective.initialize', function (event, e, query, link, linkData, navbarVoService, reloadOnly, searchQuery) {
	                    show();
	                    //if (angular.equals(sLastKey, link.key)) {
	                    //    //if (htLastLink.targetRawData) {
	                    //        renderAllChartWhichIsVisible(htLastLink);
	                    //    //}
	                    //    return;
	                    //}
	                    reset();
	                    htQuery = query;
	                    sLastKey = link.key;
	                    htLastLink = link;
	                    scope.htLastUnknownLink = false;
	                    scope.oNavbarVoService = navbarVoService;
	                    scope.linkSearch = searchQuery || "";
	                    scope.currentPage = 1;

	                    calcuPagingSize( link.unknownLinkGroup );
	                    showDetailInformation(link);
	                });
	
	                /**
	                 * scope event on linkInfoDetailsDirective.lazyRendering
	                 */
	                scope.$on('linkInfoDetailsDirective.lazyRendering', function (event, e) {
	                    renderAllChartWhichIsVisible(htLastLink);
	                });
	            }
	        };
	    }
	]);
})();