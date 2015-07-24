(function() {
	'use strict';
	/**
	 * (en)FilteredMapCtrl 
	 * @ko FilteredMapCtrl
	 * @group Controller
	 * @name FilteredMapCtrl
	 * @class
	 */
	pinpointApp.controller('FilteredMapCtrl', [ 'filterConfig', '$scope', '$routeParams', '$timeout', 'TimeSliderVoService', 'NavbarVoService', '$window', 'SidebarTitleVoService', 'filteredMapUtilService', '$rootElement',
	    function (cfg, $scope, $routeParams, $timeout, TimeSliderVoService, NavbarVoService, $window, SidebarTitleVoService, filteredMapUtilService, $rootElement) {
			$at($at.FILTEREDMAP_PAGE);
	        // define private variables
	        var oNavbarVoService, oTimeSliderVoService, bNodeSelected, bNoData, reloadOnlyForNode, reloadOnlyForLink;
	
	        // define private variables of methods
	        var openFilteredMapWithFilterVo, broadcastScatterScanResultToScatter;
	
	        // initialize scope variables
	        $scope.hasScatter = false;
	        $window.htoScatter = {};
	        bNoData = true;
	        reloadOnlyForNode = false;
	        reloadOnlyForLink = false;
	        $scope.sidebarLoading = false;
	
	        /**
	         * initialize
	         */
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
	                $scope.$broadcast('serverMapDirective.initialize', oNavbarVoService);
	                $scope.$broadcast('scatterDirective.initialize', oNavbarVoService);
	            });
	
	        }, 500);
	
	        /**
	         * open filtered map with filterVo
	         * @param filterDataSet
	         * @param filterTargetRpcList
	         */
	        openFilteredMapWithFilterVo = function (oServerMapFilterVoService, filterTargetRpcList) {
	            var url = filteredMapUtilService.getFilteredMapUrlWithFilterVo(oNavbarVoService, oServerMapFilterVoService, filterTargetRpcList);
	            $window.open(url, "");
	        };
	
	        /**
	         * broadcast scatter scan result to scatter
	         * @param applicationScatterScanResult
	         */
	        broadcastScatterScanResultToScatter = function (applicationScatterScanResult) {
	            if (angular.isDefined(applicationScatterScanResult)) {
	                angular.forEach(applicationScatterScanResult, function (val, key) {
	                    $scope.$broadcast('scatterDirective.initializeWithData', key, val);
	                });
	            }
	        };
	
	        /**
	         * get main container class
	         */
	        $scope.getMainContainerClass = function () {
	            return bNoData ? 'no-data' : '';
	        };
	
	        /**
	         * get info details class
	         * @returns {string}
	         */
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
	
	        /**
	         * scope event on servermapDirective.hasData
	         */
	        $scope.$on('serverMapDirective.hasData', function (event) {
	            bNoData = false;
	            $scope.sidebarLoading = false;
	        });
	
	        /**
	         * scope event on servermap.hasNoData
	         */
	        $scope.$on('serverMap.hasNoData', function (event) {
	            bNoData = true;
	            $scope.sidebarLoading = false;
	        });
	
	        /**
	         * scope event on serverMapDirective.fetched
	         */
	        $scope.$on('serverMapDirective.fetched', function (event, lastFetchedTimestamp, mapData) {
	            oTimeSliderVoService.setInnerFrom(lastFetchedTimestamp);
	            reloadOnlyForNode = true;
	            reloadOnlyForLink = true;
	            $scope.$broadcast('timeSliderDirective.setInnerFromTo', oTimeSliderVoService);
	            broadcastScatterScanResultToScatter(mapData.applicationScatterScanResult);
	
	            // auto trying fetch
	            if (mapData.applicationMapData.nodeDataArray.length === 0 && mapData.applicationMapData.linkDataArray.length === 0) {
	                $timeout(function () {
	                    $scope.$broadcast('timeSliderDirective.moreClicked');
	                }, 500);
	            } else {
	                $scope.$broadcast('timeSliderDirective.enableMore');
	            }
	        });
	
	        /**
	         * scope event on serverMapDirective. allFetched
	         */
	        $scope.$on('serverMapDirective.allFetched', function (event, mapData) {
	            oTimeSliderVoService.setInnerFrom(oTimeSliderVoService.getFrom());
	            reloadOnlyForNode = true;
	            reloadOnlyForLink = true;
	            $scope.$broadcast('timeSliderDirective.setInnerFromTo', oTimeSliderVoService);
	            $scope.$broadcast('timeSliderDirective.changeMoreToDone');
	            $scope.$broadcast('timeSliderDirective.disableMore');
	
	            broadcastScatterScanResultToScatter(mapData.applicationScatterScanResult);
	        });
	
	        /**
	         * scope event of timeSliderDirective.moreClicked
	         */
	        $scope.$on('timeSliderDirective.moreClicked', function (event) {
	            var newNavbarVoService = new NavbarVoService();
	            newNavbarVoService
	                .setApplication(oNavbarVoService.getApplication())
	                .setQueryStartTime(oNavbarVoService.getQueryStartTime())
	                .setQueryEndTime(oTimeSliderVoService.getInnerFrom())
	                .autoCalcultateByQueryStartTimeAndQueryEndTime();
	            $scope.sidebarLoading = true;
	            $scope.$broadcast('timeSliderDirective.disableMore');
	            $scope.$broadcast('serverMapDirective.fetch', newNavbarVoService.getQueryPeriod(), newNavbarVoService.getQueryEndTime());
	        });
	
	        /**
	         * scope event on serverMapDirective.passingTransactionResponseToScatterChart
	         */
	//        $scope.$on('serverMapDirective.passingTransactionResponseToScatterChart', function (event, node) {
	//            $scope.$broadcast('scatterDirective.initializeWithData', node);
	//        });
	
	        /**
	         * scope event on serverMapDirective.nodeClicked
	         */
	        $scope.$on('serverMapDirective.nodeClicked', function (event, e, query, node, data) {
	            bNodeSelected = true;
	            var oSidebarTitleVoService = new SidebarTitleVoService;
	            oSidebarTitleVoService.setImageType(node.serviceType);
	
	            if (node.isWas === true) {
	                $scope.hasScatter = true;
	                oSidebarTitleVoService.setTitle(node.applicationName);
	                $scope.$broadcast('scatterDirective.showByNode', node);
	            } else if (node.unknownNodeGroup) {
	            	oSidebarTitleVoService.setTitle( node.serviceType.replace( "_", " " ) );
	                $scope.hasScatter = false;
	            } else {
	                oSidebarTitleVoService.setTitle(node.applicationName);
	                $scope.hasScatter = false;
	            }
	            $scope.hasFilter = false;
	            $scope.$broadcast('sidebarTitleDirective.initialize.forFilteredMap', oSidebarTitleVoService);
	            $scope.$broadcast('nodeInfoDetailsDirective.initialize', e, query, node, data, oNavbarVoService, reloadOnlyForNode);
	            $scope.$broadcast('linkInfoDetailsDirective.hide', e, query, node, data, oNavbarVoService);
	            reloadOnlyForNode = false;
	        });
	
	        /**
	         * scope event on serverMapDirective.linkClicked
	         */
	        $scope.$on('serverMapDirective.linkClicked', function (event, e, query, link, data) {
	            bNodeSelected = false;
	            var oSidebarTitleVoService = new SidebarTitleVoService;
	            if (link.unknownLinkGroup) {
	                oSidebarTitleVoService
	                    .setImageType(link.sourceInfo.serviceType)
	                    .setTitle('Unknown Group from ' + link.sourceInfo.applicationName);
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
	            $scope.$broadcast('sidebarTitleDirective.initialize.forFilteredMap', oSidebarTitleVoService);
	            $scope.$broadcast('nodeInfoDetailsDirective.hide');
	            $scope.$broadcast('linkInfoDetailsDirective.initialize', e, query, link, data, oNavbarVoService, reloadOnlyForLink);
	            reloadOnlyForLink = false;
	        });
	
	
	        /**
	         * scope event on serverMapDirective.openFilteredMap
	         */
	        $scope.$on('serverMapDirective.openFilteredMap', function (event, oServerMapFilterVoService, filterTargetRpcList) {
	            openFilteredMapWithFilterVo(oServerMapFilterVoService, filterTargetRpcList);
	        });
	
	        /**
	         * scope event on serverMapDirective.openFilteredMap
	         */
	        $scope.$on('linkInfoDetailsDirective.openFilteredMap', function (event, oServerMapFilterVoService, filterTargetRpcList) {
	            openFilteredMapWithFilterVo(oServerMapFilterVoService, filterTargetRpcList);
	        });
	
	        /**
	         * scope event on linkInfoDetailsDirective.openFilterWizard
	         */
	        $scope.$on('linkInfoDetailsDirective.openFilterWizard', function (event, oServerMapFilterVoService, oServerMapHintVoService) {
	            $scope.$broadcast('serverMapDirective.openFilterWizard', oServerMapFilterVoService, oServerMapHintVoService);
	        });
	
	        /**
	         * scope event on linkInfoDetailsDirective.ResponseSummary.barClicked
	         */
	        $scope.$on('linkInfoDetailsDirective.ResponseSummary.barClicked', function (event, oServerMapFilterVoService, filterTargetRpcList) {
	            openFilteredMapWithFilterVo(oServerMapFilterVoService, filterTargetRpcList);
	        });
	
	        /**
	         * scope event on linkInfoDetail.showDetailInformationClicked
	         */
	        $scope.$on('linkInfoDetail.showDetailInformationClicked', function (event, query, link) {
	            $scope.hasScatter = false;
	            var oSidebarTitleVoService = new SidebarTitleVoService;
	            oSidebarTitleVoService
	                .setImageType(link.sourceInfo.serviceType)
	                .setTitle(link.sourceInfo.applicationName)
	                .setImageType2(link.targetInfo.serviceType)
	                .setTitle2(link.targetInfo.applicationName);
	            $scope.$broadcast('sidebarTitleDirective.initialize.forFilteredMap', oSidebarTitleVoService);
	            $scope.$broadcast('nodeInfoDetailsDirective.hide');
	        });
	
	        /**
	         * scope event on nodeInfoDetail.showDetailInformationClicked
	         */
	        $scope.$on('nodeInfoDetail.showDetailInformationClicked', function (event, query, node) {
	            $scope.hasScatter = false;
	            var oSidebarTitleVoService = new SidebarTitleVoService;
	            oSidebarTitleVoService
	                .setImageType(node.serviceType)
	                .setTitle(node.applicationName);
	            $scope.$broadcast('sidebarTitleDirective.initialize.forMain', oSidebarTitleVoService);
	            $scope.$broadcast('linkInfoDetailsDirective.hide');
	        });
	    }
	]);
})();