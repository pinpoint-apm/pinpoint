(function() {
	'use strict';
	/**
	 * (en)MainCtrl
	 * @ko MainCtrl
	 * @group Controller
	 * @name MainCtrl
	 * @class
	 */
	pinpointApp.controller( "MainCtrl", [ "filterConfig", "$scope", "$timeout", "$routeParams", "locationService", "UrlVoService", "NavbarVoService", "$window", "filteredMapUtilService", "$rootElement", "SystemConfigurationService", "AnalyticsService", "PreferenceService",
	    function (cfg, $scope, $timeout, $routeParams, locationService, UrlVoService, NavbarVoService, $window, filteredMapUtilService, $rootElement, SystemConfigService, analyticsService, preferenceService) {
			analyticsService.send(analyticsService.CONST.MAIN_PAGE);
			SystemConfigService.getConfig().then(function(config) {
				analyticsService.sendMain(analyticsService.CONST.VERSION, config["version"]);
			});

	        // define private variables
	        var oNavbarVoService, bNoData;

	        // initialize scope variables
	        $scope.hasScatter = false;
	        $window.htoScatter = {};
	        //$scope.bShowHelpIcons = false;
	        bNoData = false;
	        $scope.sidebarLoading = true;

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
				oNavbarVoService.setCalleeRange( preferenceService.getCalleeByApp($routeParams.application) );
				oNavbarVoService.setCallerRange( preferenceService.getCallerByApp($routeParams.application) );
				oNavbarVoService.setBidirectional( preferenceService.getBidirectionalByApp($routeParams.application) );
				oNavbarVoService.setWasOnly( preferenceService.getWasOnlyByApp($routeParams.application) );

				UrlVoService.initUrlVo( "main", $routeParams );

				if ( oNavbarVoService.isRealtime() ) {
					$scope.$broadcast("navbarDirective.initialize.realtime.andReload", oNavbarVoService);
				} else {
					if ( angular.isDefined($routeParams.application) && angular.isUndefined($routeParams.readablePeriod) ) {
						$scope.$broadcast("navbarDirective.initialize.andReload", oNavbarVoService);
					} else {
						$window.$routeParams = $routeParams;
						UrlVoService.autoCalculateByQueryEndDateTimeAndReadablePeriod();
						oNavbarVoService.autoCalculateByQueryEndDateTimeAndReadablePeriod();
						$scope.$broadcast("navbarDirective.initialize", oNavbarVoService);
						$scope.$broadcast("serverListDirective.initialize", oNavbarVoService );
						$scope.$broadcast("scatterDirective.initialize.forMain", oNavbarVoService);
						$scope.$broadcast("serverMapDirective.initialize", oNavbarVoService);
					}
				}
	        }, 500);
	        function getFirstPathOfLocation() {
	            return locationService.path().split("/")[1] || "main";
	        }
	        function changeLocation() {
				var url = "/" + getFirstPathOfLocation() + "/" + oNavbarVoService.getApplication() + "/";
				if ( oNavbarVoService.isRealtime() ) {
					url += oNavbarVoService.getPeriodType();

					$window.$routeParams = {
						application: oNavbarVoService.getApplication(),
						readablePeriod: oNavbarVoService.getPeriodType()
					};
				} else {
					url += oNavbarVoService.getReadablePeriod() + "/" + oNavbarVoService.getQueryEndDateTime();
				}
	            if (locationService.path() !== url ) {
	                if (locationService.path() === "/main") {
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
	        }

	        function openFilteredMapWithFilterVo(oServerMapFilterVoService, oServerMapHintVoService) {
	            var url = filteredMapUtilService.getFilteredMapUrlWithFilterVo(oNavbarVoService, oServerMapFilterVoService, oServerMapHintVoService);
	            $window.open(url, "");
	        }
	        $scope.getMainContainerClass = function () {
	        	return "";//return bNoData ? 'no-data' : '';
	        };
	        $scope.getInfoDetailsClass = function () {
	            var infoDetailsClass = [];

	            if ($scope.hasScatter) {
	                infoDetailsClass.push("has-scatter");
	            }
	            if ($scope.hasFilter) {
	                infoDetailsClass.push("has-filter");
	            }

	            return infoDetailsClass.join(" ");
	        };
	        // from server-map.directive
	        $scope.$on("serverMapDirective.hasData", function (event) {
	            bNoData = false;
	            $scope.sidebarLoading = false;
	        });
	        // from server-map.directive
	        $scope.$on("serverMapDirective.hasNoData", function (event) {
	            bNoData = true;
	            $scope.sidebarLoading = false;
	        });
	        // from navbar.directive
			// 		inspector.controller
			// 		configuration.directive
	        $scope.$on("navbarDirective.changed", function (event, navbarVo) {
	        	bNoData = false;
	            oNavbarVoService = navbarVo;
	            changeLocation(oNavbarVoService);
				// if url changed that below code is not excuted.
	            $window.htoScatter = {};
	            $scope.hasScatter = false;
	            $scope.sidebarLoading = true;

				$scope.$broadcast("realtimeChartController.close");
	            $scope.$broadcast("sidebarTitleDirective.empty.forMain");
				$scope.$broadcast("serverListDirective.initialize", oNavbarVoService );
	            $scope.$broadcast("nodeInfoDetailsDirective.hide");
				$scope.$broadcast("groupedApplicationListDirective.hide");
	            $scope.$broadcast("scatterDirective.initialize.forMain", oNavbarVoService);
	            $scope.$broadcast("serverMapDirective.initialize", oNavbarVoService);
	            $scope.$broadcast("sidebarTitleDirective.empty.forMain");
	        });
	        // from server-map.directive
	        $scope.$on("serverMapDirective.passingTransactionResponseToScatterChart", function (event, node) {
	            $scope.$broadcast("scatterDirective.initializeWithNode.forMain", node);
	        });
	        // from server-map.directive
	        $scope.$on("serverMapDirective.nodeClicked", function (event, query, node, data, searchQuery) {
				$scope.hasScatter = false;
				$scope.hasFilter = false;
				if (node["isWas"] === true && node["isAuthorized"] === true ) {
					$scope.hasScatter = true;
					$scope.$broadcast("scatterDirective.initializeWithNode.forMain", node);
				} else {
					$scope.$broadcast("scatterDirective.stopRequest.forMain");
				}
	            $scope.$broadcast("sidebarTitleDirective.initialize.forMain", node, oNavbarVoService);
				$scope.$broadcast("groupedApplicationListDirective.initialize", node, oNavbarVoService);
	            $scope.$broadcast("nodeInfoDetailsDirective.initialize", node, oNavbarVoService);
				if (oNavbarVoService && oNavbarVoService.isRealtime()) {
					$scope.$broadcast("realtimeChartController.initialize", node["isWas"], node["applicationName"], node["serviceType"], oNavbarVoService.getApplication() + "/" + oNavbarVoService.getReadablePeriod() + "/" + oNavbarVoService.getQueryEndDateTime() + "/" + oNavbarVoService.getCallerRange());
				}
	        });
	        // from server-map.directive
	        $scope.$on("serverMapDirective.linkClicked", function (event, query, link, data) {
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
	                $scope.$broadcast("filterInformationDirective.initialize.forMain", foundFilter.oServerMapFilterVoService);
	            } else {
	                $scope.hasFilter = false;
	            }
	            $scope.$broadcast("sidebarTitleDirective.initialize.forMain", link, oNavbarVoService);
				$scope.$broadcast("groupedApplicationListDirective.initialize", link, oNavbarVoService);
				$scope.$broadcast("nodeInfoDetailsDirective.initialize", link, oNavbarVoService);
	        });
	        // from server-map.directive
	        $scope.$on("serverMapDirective.openFilteredMap", function (event, oServerMapFilterVoService, oServerMapHintVoService) {
	            openFilteredMapWithFilterVo(oServerMapFilterVoService, oServerMapHintVoService);
	        });
			// from grouped-application-list.directive
	        $scope.$on("main.controller.openFilterWizard", function (event, oServerMapFilterVoService, oServerMapHintVoService) {
	            $scope.$broadcast("serverMapDirective.openFilterWizard", oServerMapFilterVoService, oServerMapHintVoService);
	        });
	        // from linkInfoDetail.directive
	        $scope.$on("linkInfoDetailsDirective.ResponseSummary.barClicked", function (event, link) {
	            openFilteredMapWithFilterVo(link);
	        });
	    }
	]);
})();