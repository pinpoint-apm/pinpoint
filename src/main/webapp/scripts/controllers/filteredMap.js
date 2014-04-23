'use strict';

pinpointApp.controller('FilteredMapCtrl', [ 'filterConfig', '$scope', '$routeParams', '$timeout', 'TimeSliderVo', 'NavbarVo', 'encodeURIComponentFilter', '$window', 'SidebarTitleVo', 'filteredMapUtil',
    function (cfg, $scope, $routeParams, $timeout, TimeSliderVo, NavbarVo, encodeURIComponentFilter, $window, SidebarTitleVo, filteredMapUtil) {

        // define private variables
        var oNavbarVo, oTimeSliderVo;

        // define private variables of methods
        var openFilteredMapWithFilterVo;

        // initialize scope variables
        $scope.hasScatter = false;
        $window.htoScatter = {};

        /**
         * initialize
         */
        $timeout(function () {
            oNavbarVo = new NavbarVo();
            if ($routeParams.application) {
                oNavbarVo.setApplication($routeParams.application);
            }
            if ($routeParams.period) {
                oNavbarVo.setPeriod(Number($routeParams.period, 10));
            }
            if ($routeParams.queryEndTime) {
                oNavbarVo.setQueryEndTime(Number($routeParams.queryEndTime, 10));
            }
            if ($routeParams.filter) {
                oNavbarVo.setFilter($routeParams.filter);
            }
            $window.$routeParams = $routeParams;
            oNavbarVo.autoCalculateByQueryEndTimeAndPeriod();

            oTimeSliderVo = new TimeSliderVo()
                .setFrom(oNavbarVo.getQueryStartTime())
                .setTo(oNavbarVo.getQueryEndTime())
                .setInnerFrom(oNavbarVo.getQueryEndTime() - 1)
                .setInnerTo(oNavbarVo.getQueryEndTime());

            $timeout(function () {
                $scope.$broadcast('timeSlider.initialize', oTimeSliderVo);
                $scope.$broadcast('serverMap.initialize', oNavbarVo);
                $scope.$broadcast('scatter.initialize', oNavbarVo);
            });
        }, 500);

        /**
         * open filtered map with filterVo
         * @param filterDataSet
         */
        openFilteredMapWithFilterVo = function (oServerMapFilterVo) {
            var url = filteredMapUtil.getFilteredMapUrlWithFilterVo(oServerMapFilterVo, oNavbarVo);
            $window.open(url, "");
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
         * scope event on serverMap.fetched
         */
        $scope.$on('serverMap.fetched', function (event, lastFetchedTimestamp, mapData) {
            oTimeSliderVo.setInnerFrom(lastFetchedTimestamp);
            $scope.$broadcast('timeSlider.setInnerFromTo', oTimeSliderVo);

            // auto trying fetch
            if (mapData.applicationMapData.nodeDataArray.length === 0 && mapData.applicationMapData.linkDataArray.length === 0) {
                $timeout(function () {
                    $scope.$broadcast('timeSlider.moreClicked');
                }, 500);
            } else {
                $scope.$broadcast('timeSlider.enableMore');
            }
        });

        /**
         * scope event on serverMap. allFetched
         */
        $scope.$on('serverMap.allFetched', function (event) {
            oTimeSliderVo.setInnerFrom(oTimeSliderVo.getFrom());
            $scope.$broadcast('timeSlider.setInnerFromTo', oTimeSliderVo);
            $scope.$broadcast('timeSlider.changeMoreToDone');
            $scope.$broadcast('timeSlider.disableMore');
        });

        /**
         * scope event of timeSlider.moreClicked
         */
        $scope.$on('timeSlider.moreClicked', function (event) {
            var newNavbarVo = new NavbarVo();
            newNavbarVo
                .setApplication(oNavbarVo.getApplication())
                .setQueryStartTime(oNavbarVo.getQueryStartTime())
                .setQueryEndTime(oTimeSliderVo.getInnerFrom())
                .autoCalcultateByQueryStartTimeAndQueryEndTime();
            $scope.$broadcast('timeSlider.disableMore');
            $scope.$broadcast('serverMap.fetch', newNavbarVo.getQueryPeriod(), newNavbarVo.getQueryEndTime());
        });

        /**
         * scope event on serverMap.passingTransactionResponseToScatterChart
         */
        $scope.$on('serverMap.passingTransactionResponseToScatterChart', function (event, node) {
            $scope.$broadcast('scatter.initializeWithNode', node);
        });

        /**
         * scope event on serverMap.nodeClicked
         */
        $scope.$on('serverMap.nodeClicked', function (event, e, query, node, data) {
            var oSidebarTitleVo = new SidebarTitleVo;
            oSidebarTitleVo
                .setImageType(node.category)
                .setTitle(node.text);

            if (node.isWas === true) {
                $scope.hasScatter = true;
                $scope.$broadcast('scatter.initializeWithNode', node);
            } else if (node.category === 'UNKNOWN_GROUP') {
                oSidebarTitleVo
                    .setTitle('Unknown Group');
                $scope.hasScatter = false;
            } else {
                $scope.hasScatter = false;
            }
            $scope.hasFilter = false;
            $scope.$broadcast('sidebarTitle.initialize.forFilteredMap', oSidebarTitleVo);
            $scope.$broadcast('nodeInfoDetails.initialize', e, query, node, data, oNavbarVo);
            $scope.$broadcast('linkInfoDetails.reset', e, query, node, data, oNavbarVo);
        });

        /**
         * scope event on serverMap.linkClicked
         */
        $scope.$on('serverMap.linkClicked', function (event, e, query, link, data) {
            var oSidebarTitleVo = new SidebarTitleVo;
            if (link.targetRawData) {
                oSidebarTitleVo
                    .setImageType(link.sourceInfo.serviceType)
                    .setTitle('Unknown Group from ' + link.sourceInfo.applicationName);
            } else {
                oSidebarTitleVo
                    .setImageType(link.sourceInfo.serviceType)
                    .setTitle(link.sourceInfo.applicationName)
                    .setImageType2(link.targetInfo.serviceType)
                    .setTitle2(link.targetInfo.applicationName);
            }
            $scope.hasScatter = false;
            var foundFilter = filteredMapUtil.findFilterInNavbarVo(
                link.sourceInfo.applicationName,
                link.sourceInfo.serviceType,
                link.targetInfo.applicationName,
                link.targetInfo.serviceType,
                oNavbarVo
            );
            if (foundFilter) {
                $scope.hasFilter = true;
                $scope.$broadcast('filterInformation.initialize.forFilteredMap', foundFilter.oServerMapFilterVo);
            } else {
                $scope.hasFilter = false;
            }
            $scope.$broadcast('sidebarTitle.initialize.forFilteredMap', oSidebarTitleVo);
            $scope.$broadcast('nodeInfoDetails.reset');
            $scope.$broadcast('linkInfoDetails.initialize', e, query, link, data, oNavbarVo);
        });


        /**
         * scope event on serverMap.openFilteredMap
         */
        $scope.$on('serverMap.openFilteredMap', function (event, oServerMapFilterVo) {
            openFilteredMapWithFilterVo(oServerMapFilterVo);
        });

        /**
         * scope event on serverMap.openFilteredMap
         */
        $scope.$on('linkInfoDetails.openFilteredMap', function (event, oServerMapFilterVo) {
            openFilteredMapWithFilterVo(oServerMapFilterVo);
        });

        /**
         * scope event on linkInfoDetails.ResponseSummary.barClicked
         */
        $scope.$on('linkInfoDetails.ResponseSummary.barClicked', function (event, oServerMapFilterVo) {
            openFilteredMapWithFilterVo(oServerMapFilterVo);
        });

        /**
         * scope event on linkInfoDetail.showDetailInformationClicked
         */
        $scope.$on('linkInfoDetail.showDetailInformationClicked', function (event, query, link) {
            $scope.hasScatter = false;
            var oSidebarTitleVo = new SidebarTitleVo;
            oSidebarTitleVo
                .setImageType(link.sourceInfo.serviceType)
                .setTitle(link.sourceInfo.applicationName)
                .setImageType2(link.targetInfo.serviceType)
                .setTitle2(link.targetInfo.applicationName);
            $scope.$broadcast('sidebarTitle.initialize.forFilteredMap', oSidebarTitleVo);
            $scope.$broadcast('nodeInfoDetails.reset');
        });

        /**
         * scope event on nodeInfoDetail.showDetailInformationClicked
         */
        $scope.$on('nodeInfoDetail.showDetailInformationClicked', function (event, query, node) {
            $scope.hasScatter = false;
            var oSidebarTitleVo = new SidebarTitleVo;
            oSidebarTitleVo
                .setImageType(node.category)
                .setTitle(node.text);
            $scope.$broadcast('sidebarTitle.initialize.forMain', oSidebarTitleVo);
            $scope.$broadcast('linkInfoDetails.reset');
        });
    }]);
