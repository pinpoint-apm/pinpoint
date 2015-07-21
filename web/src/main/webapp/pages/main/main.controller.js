(function() {
	'use strict';
	/**
	 * (en)MainCtrl 
	 * @ko MainCtrl
	 * @group Controller
	 * @name MainCtrl
	 * @class
	 */
	pinpointApp.controller('MainCtrl', [ 'filterConfig', '$scope', '$timeout', '$routeParams', 'locationService', 'NavbarVoService', '$window', 'SidebarTitleVoService', 'filteredMapUtilService', '$rootElement',
	    function (cfg, $scope, $timeout, $routeParams, locationService, NavbarVoService, $window, SidebarTitleVoService, filteredMapUtilService, $rootElement) {
			$at($at.MAIN_PAGE);
	        // define private variables
	        var oNavbarVoService, bNodeSelected, bNoData;
	
	        // define private variables of methods
	        var getFirstPathOfLocation, changeLocation, openFilteredMapWithFilterVo;
	
	        // initialize scope variables
	        $scope.hasScatter = false;
	        $window.htoScatter = {};
	        bNodeSelected = true;
	        //$scope.bShowHelpIcons = false;
	        bNoData = false;
	        $scope.sidebarLoading = true;
	
	        /**
	         * bootstrap
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
	            $window.$routeParams = $routeParams;
	            oNavbarVoService.autoCalculateByQueryEndDateTimeAndReadablePeriod();
	            $scope.$broadcast('navbarDirective.initialize', oNavbarVoService);
	            $scope.$broadcast('scatterDirective.initialize', oNavbarVoService);
	            $scope.$broadcast('serverMapDirective.initialize', oNavbarVoService);
	        }, 500);
	
	        /**
	         * get first path of location
	         * @returns {*|string}
	         */
	        getFirstPathOfLocation = function () {
	            return locationService.path().split('/')[1] || 'main';
	        };
	
	        /**
	         * change location
	         */
	        changeLocation = function () {
	            var url = '/' + getFirstPathOfLocation() + '/' + oNavbarVoService.getApplication() + '/' + oNavbarVoService.getReadablePeriod() +
	                '/' + oNavbarVoService.getQueryEndDateTime();
	            if (locationService.path() !== url) {
	                if (locationService.path() === '/main') {
	                	locationService.path(url).replace();
	                } else {
	                	locationService.skipReload().path(url).replace();
	                }
	                $window.$routeParams = {
	                    application: oNavbarVoService.getApplication(),
	                    readablePeriod: (oNavbarVoService.getReadablePeriod()).toString(),
	                    queryEndDateTime: (oNavbarVoService.getQueryEndDateTime()).toString()
	                };
	                if (!$scope.$$phase) {
	                    $scope.$apply();
	                }
	            }
	        };
	
	        /**
	         * open filtered map with filter Vo
	         * @param oServerMapFilterVoService
	         * @param oServerMapHintVoService
	         */
	        openFilteredMapWithFilterVo = function (oServerMapFilterVoService, oServerMapHintVoService) {
	            var url = filteredMapUtilService.getFilteredMapUrlWithFilterVo(oNavbarVoService, oServerMapFilterVoService, oServerMapHintVoService);
	            $window.open(url, "");
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
	         * scope event on servermapDirective.hasNoData
	         */
	        $scope.$on('serverMapDirective.hasNoData', function (event) {
	            bNoData = true;
	            $scope.sidebarLoading = false;
	        });
	
	        /**
	         * scope event on navbarDirective.changed
	         */
	        $scope.$on('navbarDirective.changed', function (event, navbarVo) {
	        	bNoData = false;
	            oNavbarVoService = navbarVo;
	            changeLocation(oNavbarVoService);
	            $window.htoScatter = {};
	            $scope.hasScatter = false;
	            $scope.sidebarLoading = true;
	            $scope.$broadcast('sidebarTitleDirective.empty.forMain');
	            $scope.$broadcast('nodeInfoDetailsDirective.hide');
	            $scope.$broadcast('linkInfoDetailsDirective.hide');
	            $scope.$broadcast('scatterDirective.initialize', oNavbarVoService);
	            $scope.$broadcast('serverMapDirective.initialize', oNavbarVoService);
	            $scope.$broadcast('sidebarTitleDirective.empty.forMain');
	        });
	
	        /**
	         * scope event on serverMapDirective.passingTransactionResponseToScatterChart
	         */
	        $scope.$on('serverMapDirective.passingTransactionResponseToScatterChart', function (event, node) {
	            $scope.$broadcast('scatterDirective.initializeWithNode', node);
	        });
	
	        /**
	         * scope event on serverMapDirective.nodeClicked
	         */
	        $scope.$on('serverMapDirective.nodeClicked', function (event, e, query, node, data, searchQuery) {
	            bNodeSelected = true;
	            var oSidebarTitleVoService = new SidebarTitleVoService;
	            oSidebarTitleVoService.setImageType(node.serviceType);
	
	            if (node.isWas === true) {
	                $scope.hasScatter = true;
	                oSidebarTitleVoService.setTitle(node.applicationName);
	                $scope.$broadcast('scatterDirective.initializeWithNode', node);
	            } else if (node.unknownNodeGroup) {
	                oSidebarTitleVoService.setTitle( node.serviceType.replace( "_", " " ) );
	                $scope.hasScatter = false;
	            } else {
	                oSidebarTitleVoService.setTitle(node.applicationName);
	                $scope.hasScatter = false;
	            }
	            $scope.hasFilter = false;
	            $scope.$broadcast('sidebarTitleDirective.initialize.forMain', oSidebarTitleVoService);
	            $scope.$broadcast('nodeInfoDetailsDirective.initialize', e, query, node, data, oNavbarVoService, null, searchQuery);
	            $scope.$broadcast('linkInfoDetailsDirective.hide');
	
//	            $scope.refreshHelpIcons();
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
	                $scope.$broadcast('filterInformationDirective.initialize.forMain', foundFilter.oServerMapFilterVoService);
	            } else {
	                $scope.hasFilter = false;
	            }
	            $scope.$broadcast('sidebarTitleDirective.initialize.forMain', oSidebarTitleVoService);
	            $scope.$broadcast('nodeInfoDetailsDirective.hide');
	            $scope.$broadcast('linkInfoDetailsDirective.initialize', e, query, link, data, oNavbarVoService);
	
//	            $scope.refreshHelpIcons();
	        });
	
	        /**
	         * scope event on serverMapDirective.openFilteredMap
	         */
	        $scope.$on('serverMapDirective.openFilteredMap', function (event, oServerMapFilterVoService, oServerMapHintVoService) {
	            openFilteredMapWithFilterVo(oServerMapFilterVoService, oServerMapHintVoService);
	        });
	
	        /**
	         * scope event on serverMapDirective.openFilteredMap
	         */
	        $scope.$on('linkInfoDetailsDirective.openFilteredMap', function (event, oServerMapFilterVoService, oServerMapHintVoService) {
	            openFilteredMapWithFilterVo(oServerMapFilterVoService, oServerMapHintVoService);
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
	        $scope.$on('linkInfoDetailsDirective.ResponseSummary.barClicked', function (event, link) {
	            openFilteredMapWithFilterVo(link);
	        });
	
	        /**
	         * scope event on linkInfoDetailsDirective.showDetailInformationClicked
	         */
	        $scope.$on('linkInfoDetailsDirective.showDetailInformationClicked', function (event, query, link) {
	            $scope.hasScatter = false;
	            var oSidebarTitleVoService = new SidebarTitleVoService;
	            oSidebarTitleVoService
	                .setImageType(link.sourceInfo.serviceType)
	                .setTitle(link.sourceInfo.applicationName)
	                .setImageType2(link.targetInfo.serviceType)
	                .setTitle2(link.targetInfo.applicationName);
	            $scope.$broadcast('sidebarTitleDirective.initialize.forMain', oSidebarTitleVoService);
	            $scope.$broadcast('nodeInfoDetailsDirective.hide');
	        });
	
	        /**
	         * scope event on nodeInfoDetailDirective.showDetailInformationClicked
	         */
	        $scope.$on('nodeInfoDetailDirective.showDetailInformationClicked', function (event, query, node) {
	            $scope.hasScatter = false;
	            var oSidebarTitleVoService = new SidebarTitleVoService;
	
	            oSidebarTitleVoService
	                .setImageType(node.serviceType);
	            if (node.unknownNodeGroup) {
	                oSidebarTitleVoService.setTitle( node.serviceType.replace( "_", " " ) );
	                $scope.hasScatter = false;
	            } else {
	                oSidebarTitleVoService.setTitle(node.applicationName);
	                $scope.hasScatter = false;
	            }
	
	            $scope.$broadcast('sidebarTitleDirective.initialize.forMain', oSidebarTitleVoService);
	            $scope.$broadcast('linkInfoDetailsDirective.hide');
	        });
	
	        $scope.loadingOption = {
	        	hideTip : "init"
	        };
	        $scope.$watch( 'loadingOption.hideTip', function(newValue) {
	        	if ( newValue == "init" ) return;
	    		if ( $window.localStorage ) {
	    			var now = new Date();
	    			now.setDate(now.getDate() + 30);
	        		$window.localStorage.setItem( "__HIDE_LOADING_TIP", newValue ? now.valueOf() : "-" ); 
	        	}
	        });
	    }
	]);
})();