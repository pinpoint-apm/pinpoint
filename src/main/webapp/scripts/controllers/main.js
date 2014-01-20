'use strict';

pinpointApp.controller('MainCtrl', [ 'filterConfig', '$scope', '$timeout', '$routeParams', 'location', 'NavbarVo', 'encodeURIComponentFilter', '$window', 'SidebarTitleVo', 'filteredMapUtil',
    function (cfg, $scope, $timeout, $routeParams, location, NavbarVo, encodeURIComponentFilter, $window, SidebarTitleVo, filteredMapUtil) {

        // define private variables
        var oNavbarVo;

        // define private variables of methods
        var getFirstPathOfLocation, changeLocation, openFilteredMapWithFilterDataSet;

        // initialize scope variables
        $scope.hasScatter = false;

        /**
         * bootstrap
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
            oNavbarVo.autoCalculateByQueryEndTimeAndPeriod();
            $scope.$broadcast('navbar.initialize', oNavbarVo);
            $scope.$broadcast('scatter.initialize', oNavbarVo);
            $scope.$broadcast('serverMap.initialize', oNavbarVo);
        }, 300);

        /**
         * get first path of loction
         * @returns {*|string}
         */
        getFirstPathOfLocation = function () {
            var splitedPath = location.path().split('/');
            return splitedPath[1] || 'main';
        };

        /**
         * change location
         */
        changeLocation = function () {
            var url = '/' + getFirstPathOfLocation() + '/' + oNavbarVo.getApplication() + '/' + oNavbarVo.getPeriod() + '/' + oNavbarVo.getQueryEndTime();
            if (location.path() !== url) {
                if (location.path() === '/main') {
                    location.path(url).replace();
                } else {
                    location.skipReload().path(url).replace();
                }
                if (!$scope.$$phase) {
                    $scope.$apply();
                }
            }
        };

        /**
         * open filtered map with filter data set
         * @param filterDataSet
         */
        openFilteredMapWithFilterDataSet = function (filterDataSet) {
            var newFilter = filteredMapUtil.parseFilter(filterDataSet, oNavbarVo.getApplication(), oNavbarVo.getFilter()),
                url = '#/filteredMap/' + oNavbarVo.getApplication() + '/' + oNavbarVo.getPeriod() + '/' + oNavbarVo.getQueryEndTime() + '/' + encodeURIComponentFilter(newFilter);
            $window.open(url, "");
        };

        /**
         * scope event on navbar.changed
         */
        $scope.$on('navbar.changed', function (event, navbarVo) {
            oNavbarVo = navbarVo;
            changeLocation(oNavbarVo);
            $scope.hasScatter = false;
            $scope.$broadcast('sidebarTitle.empty.forMain');
            $scope.$broadcast('nodeInfoDetails.reset');
            $scope.$broadcast('linkInfoDetails.reset');
            $scope.$broadcast('scatter.initialize', oNavbarVo);
            $scope.$broadcast('serverMap.initialize', oNavbarVo);
            $scope.$broadcast('sidebarTitle.empty.forMain');
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

            if (node.category === 'TOMCAT') {
                $scope.hasScatter = true;
                $scope.$broadcast('scatter.initializeWithNode', node);
            } else if (node.category === 'UNKNOWN_GROUP') {
                oSidebarTitleVo
                    .setTitle('Unknown Group');
                $scope.hasScatter = false;
            } else {
                $scope.hasScatter = false;
            }

            $scope.$broadcast('sidebarTitle.initialize.forMain', oSidebarTitleVo);
            $scope.$broadcast('nodeInfoDetails.initialize', e, query, node, data, oNavbarVo);
            $scope.$broadcast('linkInfoDetails.reset');
        });

        /**
         * scope event on serverMap.linkClicked
         */
        $scope.$on('serverMap.linkClicked', function (event, e, query, link, data) {
            var oSidebarTitleVo = new SidebarTitleVo;
            if (link.rawdata) {
                oSidebarTitleVo
                    .setImageType('UNKNOWN_GROUP')
                    .setTitle('Unknown Links');
            } else {
                oSidebarTitleVo
                    .setImageType(link.sourceinfo.serviceType)
                    .setTitle(link.sourceinfo.applicationName)
                    .setImageType2(link.targetinfo.serviceType)
                    .setTitle2(link.targetinfo.applicationName);
            }
            $scope.hasScatter = false;
            $scope.$broadcast('sidebarTitle.initialize.forMain', oSidebarTitleVo);
            $scope.$broadcast('nodeInfoDetails.reset');
            $scope.$broadcast('linkInfoDetails.initialize', e, query, link, data, oNavbarVo);
        });

        /**
         * scope event on serverMap.openFilteredMap
         */
        $scope.$on('serverMap.openFilteredMap', function (event, filterDataSet) {
            openFilteredMapWithFilterDataSet(filterDataSet);
        });

        /**
         * scope event on linkInfoDetails.ResponseSummary.barClicked
         */
        $scope.$on('linkInfoDetails.ResponseSummary.barClicked', function (event, filterDataSet) {
            openFilteredMapWithFilterDataSet(filterDataSet);
        });

        /**
         * scope event on linkInfoDetail.showDetailInformationClicked
         */
        $scope.$on('linkInfoDetail.showDetailInformationClicked', function (event, query, link) {
            $scope.hasScatter = false;
            var oSidebarTitleVo = new SidebarTitleVo;
            oSidebarTitleVo
                .setImageType(link.sourceinfo.serviceType)
                .setTitle(link.sourceinfo.applicationName)
                .setImageType2(link.targetinfo.serviceType)
                .setTitle2(link.targetinfo.applicationName);
            $scope.$broadcast('sidebarTitle.initialize.forMain', oSidebarTitleVo);
            $scope.$broadcast('nodeInfoDetails.reset');
            $scope.$broadcast('linkInfoDetails.initialize', null, query, link);

        });

    } ]);
