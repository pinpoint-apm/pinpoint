(function() {
	'use strict';
	/**
	 * (en)FilteredMapCtrl 
	 * @ko FilteredMapCtrl
	 * @group Controller
	 * @name FilteredMapCtrl
	 * @class
	 */
	pinpointApp.controller('FilteredMapCtrl', [ 'filterConfig', '$scope', '$routeParams', '$timeout', 'TimeSliderVoService', 'NavbarVoService', '$window', 'SidebarTitleVoService', 'filteredMapUtilService', '$rootElement', 'AnalyticsService',
	    function (cfg, $scope, $routeParams, $timeout, TimeSliderVoService, NavbarVoService, $window, SidebarTitleVoService, filteredMapUtilService, $rootElement, analyticsService) {
			analyticsService.send(analyticsService.CONST.FILTEREDMAP_PAGE);
	        // define private variables
	        var oNavbarVoService, oTimeSliderVoService, bNodeSelected, reloadOnlyForNode, reloadOnlyForLink, bLoadingPause = false, bIngRequest = true, bDoneRequest = false;

	        // initialize scope variables
	        $scope.hasScatter = false;
	        $window.htoScatter = {};
	        reloadOnlyForNode = false;
	        reloadOnlyForLink = false;
	        $scope.sidebarLoading = false;
	
	        $timeout(function () {
	            oNavbarVoService = new NavbarVoService();
	            if ($routeParams.application) {
	                oNavbarVoService.setApplication($routeParams.application);
	            }
	            if ($routeParams.readablePeriod) {
	                oNavbarVoService.setReadablePeriod($routeParams.readablePeriod);
	            }
	            if ($routeParams.queryEndDateTime) {
	                oNavbarVoService.setQueryEndDateTime($routeParams.queryEndDateTime);
	            }
	            if ($routeParams.filter) {
	                oNavbarVoService.setFilter($routeParams.filter);
	            }
	            if ($routeParams.hint) {
	                oNavbarVoService.setHint($routeParams.hint);
	            }
	            $window.$routeParams = $routeParams;
	            oNavbarVoService.autoCalculateByQueryEndDateTimeAndReadablePeriod();
	
	            oTimeSliderVoService = new TimeSliderVoService()
	                .setFrom(oNavbarVoService.getQueryStartTime())
	                .setTo(oNavbarVoService.getQueryEndTime())
	                .setInnerFrom(oNavbarVoService.getQueryEndTime() - 1)
	                .setInnerTo(oNavbarVoService.getQueryEndTime());
	
	            $timeout(function () {
	                $scope.$broadcast('timeSliderDirective.initialize', oTimeSliderVoService);
					$scope.$broadcast('serverListDirective.initialize', oNavbarVoService );
	                $scope.$broadcast('scatterDirective.initialize.forFilteredMap', oNavbarVoService);
					$scope.$broadcast('serverMapDirective.initialize', oNavbarVoService);
	            });
	
	        }, 500);
	
	        function openFilteredMapWithFilterVo(oServerMapFilterVoService, filterTargetRpcList) {
	            var url = filteredMapUtilService.getFilteredMapUrlWithFilterVo(oNavbarVoService, oServerMapFilterVoService, filterTargetRpcList);
	            $window.open(url, "");
	        }
	
	        function broadcastScatterScanResultToScatter(applicationScatterData) {
	            if (angular.isDefined(applicationScatterData)) {
	                angular.forEach(applicationScatterData, function (val, key) {
	                	var copyVal = angular.copy(val);
						$scope.$broadcast('scatterDirective.initializeWithData.forFilteredMap', key, val);
						$scope.$broadcast('scatterDirective.initializeWithData.forServerList', key, copyVal);
	                });
	            }
	        }
	        function loadData() {
				var newNavbarVoService = new NavbarVoService();
				newNavbarVoService
					.setApplication(oNavbarVoService.getApplication())
					.setQueryStartTime(oNavbarVoService.getQueryStartTime())
					.setQueryEndTime(oTimeSliderVoService.getInnerFrom())
					.autoCalcultateByQueryStartTimeAndQueryEndTime();
				$scope.sidebarLoading = true;
				$scope.$broadcast('timeSliderDirective.disableMore');
				$scope.$broadcast('serverMapDirective.fetch', newNavbarVoService.getQueryPeriod(), newNavbarVoService.getQueryEndTime());
			}
			function checkNextLoading() {
				bIngRequest = false;
				if ( bDoneRequest === false ) {
					if (bLoadingPause === false) {
						loadData();
						bIngRequest = true;
					}
				}
			}
	
	        $scope.getMainContainerClass = function () {
				return "";
	        };
	
	        $scope.getInfoDetailsClass = function () {
	            var infoDetailsClass = [];
	
	            if ($scope.hasScatter) {
	                infoDetailsClass.push('has-scatter');
	            }
	            if ($scope.hasFilter) {
	                infoDetailsClass.push('has-filter');
	            }
	
	            return infoDetailsClass.join(' ');
	        };
	
	        $scope.$on('serverMapDirective.hasData', function () {
	            $scope.sidebarLoading = false;
	        });

			$scope.$on('serverMapDirective.hasNoData', function () {
	            $scope.sidebarLoading = false;
				checkNextLoading();
	        });
	
	        $scope.$on('serverMapDirective.fetched', function (event, lastFetchedTimestamp, mapData) {
	            oTimeSliderVoService.setInnerFrom(lastFetchedTimestamp);
	            reloadOnlyForNode = true;
	            reloadOnlyForLink = true;
	            $scope.$broadcast('timeSliderDirective.setInnerFromTo', oTimeSliderVoService);
	            broadcastScatterScanResultToScatter(mapData.applicationScatterData);
	        });
	
	        $scope.$on('serverMapDirective.allFetched', function (event, mapData) {
				bDoneRequest = true;
	            oTimeSliderVoService.setInnerFrom(oTimeSliderVoService.getFrom());
	            reloadOnlyForNode = true;
	            reloadOnlyForLink = true;
	            $scope.$broadcast('timeSliderDirective.setInnerFromTo', oTimeSliderVoService);
	            $scope.$broadcast('timeSliderDirective.changeMoreToDone');
	            $scope.$broadcast('timeSliderDirective.disableMore');

	            broadcastScatterScanResultToScatter(mapData.applicationScatterData);

	        });

			$scope.$on('timeSliderDirective.changeLoadingStatus', function (event, bPause) {
				bLoadingPause = bPause;
				if ( bLoadingPause === false && bIngRequest === false ) {
					loadData();
				}
			});
	        $scope.$on('timeSliderDirective.moreClicked', function () {
	        	loadData();
	        });

	        $scope.$on('serverMapDirective.nodeClicked', function (event, query, node, data) {
	            bNodeSelected = true;
	            var oSidebarTitleVoService = new SidebarTitleVoService();
	            oSidebarTitleVoService.setImageType(node.serviceType);

				$scope.hasScatter = false;
				if (node.unknownNodeGroup) {
					oSidebarTitleVoService.setTitle( node.serviceType );
				} else {
					if (node.isWas === true) {
						$scope.hasScatter = true;
						$scope.$broadcast('scatterDirective.showByNode.forFilteredMap', node);
					}
					oSidebarTitleVoService.setTitle(node.applicationName);
				}

	            $scope.hasFilter = false;
	            $scope.$broadcast('sidebarTitleDirective.initialize.forFilteredMap', oSidebarTitleVoService, node, oNavbarVoService);
				$scope.$broadcast('groupedApplicationListDirective.initialize', node, data, oNavbarVoService);
	            $scope.$broadcast('nodeInfoDetailsDirective.initialize', node, data, oNavbarVoService, reloadOnlyForNode);
	            $scope.$broadcast('linkInfoDetailsDirective.hide', query, node, data, oNavbarVoService);
	            reloadOnlyForNode = false;

				checkNextLoading();
	        });
	
	        $scope.$on('serverMapDirective.linkClicked', function (event, query, link, data) {
	            bNodeSelected = false;
	            var oSidebarTitleVoService = new SidebarTitleVoService();
	            if (link.unknownLinkGroup) {
	                oSidebarTitleVoService
	                    .setImageType(link.sourceInfo.serviceType)
	                    .setTitle(link.toNode.serviceType.replace("_", " ") + ' from ' + link.sourceInfo.applicationName);
	            } else {
	                oSidebarTitleVoService
	                    .setImageType(link.sourceInfo.serviceType)
	                    .setTitle(link.sourceInfo.applicationName)
	                    .setImageType2(link.targetInfo.serviceType)
	                    .setTitle2(link.targetInfo.applicationName);
	            }
	            $scope.hasScatter = false;
	            var foundFilter = filteredMapUtilService.findFilterInNavbarVo(
	                link.sourceInfo.applicationName,
	                link.sourceInfo.serviceType,
	                link.targetInfo.applicationName,
	                link.targetInfo.serviceType,
	                oNavbarVoService
	            );
	            if (foundFilter) {
	                $scope.hasFilter = true;
	                $scope.$broadcast('filterInformationDirective.initialize.forFilteredMap', foundFilter.oServerMapFilterVoService);
	            } else {
	                $scope.hasFilter = false;
	            }
	            $scope.$broadcast('sidebarTitleDirective.initialize.forFilteredMap', oSidebarTitleVoService, link);
				$scope.$broadcast('groupedApplicationListDirective.initialize', link, data, oNavbarVoService);
	            $scope.$broadcast('nodeInfoDetailsDirective.hide');
	            $scope.$broadcast('linkInfoDetailsDirective.initialize', link, data, oNavbarVoService);
	            reloadOnlyForLink = false;
	        });
	
	        $scope.$on('serverMapDirective.openFilteredMap', function (event, oServerMapFilterVoService, filterTargetRpcList) {
	            openFilteredMapWithFilterVo(oServerMapFilterVoService, filterTargetRpcList);
	        });
	
	        $scope.$on('linkInfoDetailsDirective.openFilteredMap', function (event, oServerMapFilterVoService, filterTargetRpcList) {
	            openFilteredMapWithFilterVo(oServerMapFilterVoService, filterTargetRpcList);
	        });
	
	        $scope.$on('linkInfoDetailsDirective.openFilterWizard', function (event, oServerMapFilterVoService, oServerMapHintVoService) {
	            $scope.$broadcast('serverMapDirective.openFilterWizard', oServerMapFilterVoService, oServerMapHintVoService);
	        });
	
	        $scope.$on('linkInfoDetailsDirective.ResponseSummary.barClicked', function (event, oServerMapFilterVoService, filterTargetRpcList) {
	            openFilteredMapWithFilterVo(oServerMapFilterVoService, filterTargetRpcList);
	        });
	
	        $scope.$on('linkInfoDetail.showDetailInformationClicked', function (event, query, link) {
	            $scope.hasScatter = false;
	            var oSidebarTitleVoService = new SidebarTitleVoService();
	            oSidebarTitleVoService
	                .setImageType(link.sourceInfo.serviceType)
	                .setTitle(link.sourceInfo.applicationName)
	                .setImageType2(link.targetInfo.serviceType)
	                .setTitle2(link.targetInfo.applicationName);
	            $scope.$broadcast('sidebarTitleDirective.initialize.forFilteredMap', oSidebarTitleVoService);
	            $scope.$broadcast('nodeInfoDetailsDirective.hide');
	        });
	
	        $scope.$on('nodeInfoDetail.showDetailInformationClicked', function (event, query, node) {
	            $scope.hasScatter = false;
	            var oSidebarTitleVoService = new SidebarTitleVoService();
	            oSidebarTitleVoService
	                .setImageType(node.serviceType)
	                .setTitle(node.applicationName);
	            $scope.$broadcast('sidebarTitleDirective.initialize.forMain', oSidebarTitleVoService, node);
	            $scope.$broadcast('linkInfoDetailsDirective.hide');
	        });
	    }
	]);
})();