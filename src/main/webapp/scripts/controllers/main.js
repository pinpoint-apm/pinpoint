'use strict';

pinpointApp.controller('MainCtrl', [ 'filterConfig', '$scope', '$timeout', '$routeParams', 'location', 'NavbarVo', 'encodeURIComponentFilter', '$window', 'SidebarTitleVo', 'filteredMapUtil', '$rootElement', 'helpContent',
    function (cfg, $scope, $timeout, $routeParams, location, NavbarVo, encodeURIComponentFilter, $window, SidebarTitleVo, filteredMapUtil, $rootElement, helpContent) {

        // define private variables
        var oNavbarVo, bNodeSelected, bNoData;

        // define private variables of methods
        var getFirstPathOfLocation, changeLocation, openFilteredMapWithFilterVo;

        // initialize scope variables
        $scope.hasScatter = false;
        $window.htoScatter = {};
        bNodeSelected = true;
        $scope.bShowHelpIcons = false;
        bNoData = true;
        $scope.sidebarLoading = false;

        /**
         * bootstrap
         */
        $timeout(function () {
            oNavbarVo = new NavbarVo();
            if ($routeParams.application) {
                oNavbarVo.setApplication($routeParams.application);
            }
            if ($routeParams.readablePeriod) {
                oNavbarVo.setReadablePeriod($routeParams.readablePeriod);
            }
            if ($routeParams.queryEndDateTime) {
                oNavbarVo.setQueryEndDateTime($routeParams.queryEndDateTime);
            }
            $window.$routeParams = $routeParams;
            oNavbarVo.autoCalculateByQueryEndDateTimeAndReadablePeriod();
            $scope.$broadcast('navbar.initialize', oNavbarVo);
            $scope.$broadcast('scatter.initialize', oNavbarVo);
            $scope.$broadcast('serverMap.initialize', oNavbarVo);
        }, 500);

        /**
         * get first path of location
         * @returns {*|string}
         */
        getFirstPathOfLocation = function () {
            return location.path().split('/')[1] || 'main';
        };

        /**
         * change location
         */
        changeLocation = function () {
            var url = '/' + getFirstPathOfLocation() + '/' + oNavbarVo.getApplication() + '/' + oNavbarVo.getReadablePeriod() +
                '/' + oNavbarVo.getQueryEndDateTime();
            if (location.path() !== url) {
                if (location.path() === '/main') {
                    location.path(url).replace();
                } else {
                    location.skipReload().path(url).replace();
                }
                $window.$routeParams = {
                    application: oNavbarVo.getApplication(),
                    readablePeriod: (oNavbarVo.getReadablePeriod()).toString(),
                    queryEndDateTime: (oNavbarVo.getQueryEndDateTime()).toString()
                };
                if (!$scope.$$phase) {
                    $scope.$apply();
                }
            }
        };

        /**
         * open filtered map with filter Vo
         * @param oServerMapFilterVo
         * @param oServerMapHintVo
         */
        openFilteredMapWithFilterVo = function (oServerMapFilterVo, oServerMapHintVo) {
            var url = filteredMapUtil.getFilteredMapUrlWithFilterVo(oNavbarVo, oServerMapFilterVo, oServerMapHintVo);
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
         * scope event on servermap.hasData
         */
        $scope.$on('servermap.hasData', function (event) {
            bNoData = false;
            $scope.sidebarLoading = false;
        });

        /**
         * scope event on servermap.hasNoData
         */
        $scope.$on('servermap.hasNoData', function (event) {
            bNoData = true;
            $scope.sidebarLoading = false;
        });

        /**
         * scope event on navbar.changed
         */
        $scope.$on('navbar.changed', function (event, navbarVo) {
            oNavbarVo = navbarVo;
            changeLocation(oNavbarVo);
            $window.htoScatter = {};
            $scope.hasScatter = false;
            $scope.sidebarLoading = true;
            $scope.$broadcast('sidebarTitle.empty.forMain');
            $scope.$broadcast('nodeInfoDetails.hide');
            $scope.$broadcast('linkInfoDetails.hide');
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
            bNodeSelected = true;
            var oSidebarTitleVo = new SidebarTitleVo;
            oSidebarTitleVo.setImageType(node.serviceType);

            if (node.isWas === true) {
                $scope.hasScatter = true;
                oSidebarTitleVo.setTitle(node.applicationName);
                $scope.$broadcast('scatter.initializeWithNode', node);
            } else if (node.unknownNodeGroup) {
                oSidebarTitleVo.setTitle('Unknown Group');
                $scope.hasScatter = false;
            } else {
                oSidebarTitleVo.setTitle(node.applicationName);
                $scope.hasScatter = false;
            }
            $scope.hasFilter = false;
            $scope.$broadcast('sidebarTitle.initialize.forMain', oSidebarTitleVo);
            $scope.$broadcast('nodeInfoDetails.initialize', e, query, node, data, oNavbarVo);
            $scope.$broadcast('linkInfoDetails.hide');

            $scope.refreshHelpIcons();
        });

        /**
         * scope event on serverMap.linkClicked
         */
        $scope.$on('serverMap.linkClicked', function (event, e, query, link, data) {
            bNodeSelected = false;
            var oSidebarTitleVo = new SidebarTitleVo;
            if (link.unknownLinkGroup) {
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
                $scope.$broadcast('filterInformation.initialize.forMain', foundFilter.oServerMapFilterVo);
            } else {
                $scope.hasFilter = false;
            }
            $scope.$broadcast('sidebarTitle.initialize.forMain', oSidebarTitleVo);
            $scope.$broadcast('nodeInfoDetails.hide');
            $scope.$broadcast('linkInfoDetails.initialize', e, query, link, data, oNavbarVo);

            $scope.refreshHelpIcons();
        });

        /**
         * scope event on serverMap.openFilteredMap
         */
        $scope.$on('serverMap.openFilteredMap', function (event, oServerMapFilterVo, oServerMapHintVo) {
            openFilteredMapWithFilterVo(oServerMapFilterVo, oServerMapHintVo);
        });

        /**
         * scope event on serverMap.openFilteredMap
         */
        $scope.$on('linkInfoDetails.openFilteredMap', function (event, oServerMapFilterVo, oServerMapHintVo) {
            openFilteredMapWithFilterVo(oServerMapFilterVo, oServerMapHintVo);
        });

        /**
         * scope event on linkInfoDetails.openFilterWizard
         */
        $scope.$on('linkInfoDetails.openFilterWizard', function (event, oServerMapFilterVo, oServerMapHintVo) {
            $scope.$broadcast('serverMap.openFilterWizard', oServerMapFilterVo, oServerMapHintVo);
        });

        /**
         * scope event on linkInfoDetails.ResponseSummary.barClicked
         */
        $scope.$on('linkInfoDetails.ResponseSummary.barClicked', function (event, link) {
            openFilteredMapWithFilterVo(link);
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
            $scope.$broadcast('sidebarTitle.initialize.forMain', oSidebarTitleVo);
            $scope.$broadcast('nodeInfoDetails.hide');
        });

        /**
         * scope event on nodeInfoDetail.showDetailInformationClicked
         */
        $scope.$on('nodeInfoDetail.showDetailInformationClicked', function (event, query, node) {
            $scope.hasScatter = false;
            var oSidebarTitleVo = new SidebarTitleVo;

            oSidebarTitleVo
                .setImageType(node.serviceType);
            if (node.unknownNodeGroup) {
                oSidebarTitleVo.setTitle('Unknown Group');
                $scope.hasScatter = false;
            } else {
                oSidebarTitleVo.setTitle(node.applicationName);
                $scope.hasScatter = false;
            }

            $scope.$broadcast('sidebarTitle.initialize.forMain', oSidebarTitleVo);
            $scope.$broadcast('linkInfoDetails.hide');
        });

        /**
         * help
         * @type {{steps: Array}}
         */
        $scope.IntroPlusOptions = {
            steps:[
                {
                    element: '#navbar_application',
                    intro: helpContent.navbar.applicationSelector
                },
                {
                    element: '#navbar_period',
                    intro: helpContent.navbar.periodSelector
                },
                {
                    element: '#servermap',
                    intro: helpContent.servermap.default,
                    position: 'right'
                },
                {
                    element: '#scatter',
                    intro: helpContent.scatter.default,
                    position: 'left'
                },
                {
                    element: '.nodeInfoDetails .response-summary',
                    intro: helpContent.nodeInfoDetails.responseSummary,
                    position: 'left'
                },
                {
                    element: '.nodeInfoDetails .load',
                    intro: helpContent.nodeInfoDetails.load,
                    position: 'left'
                },
                {
                    element: '.nodeInfoDetails .node-servers',
                    intro: helpContent.nodeInfoDetails.nodeServers,
                    position: 'top'
                },
                {
                    element: '.nodeInfoDetails .unknown-list',
                    intro: helpContent.nodeInfoDetails.unknownList,
                    position: 'left'
                },
                {
                    element: '.nodeInfoDetails .search-n-order',
                    intro: helpContent.nodeInfoDetails.searchAndOrder,
                    position: 'left'
                },
                {
                    element: '.linkInfoDetails .response-summary',
                    intro: helpContent.linkInfoDetails.responseSummary,
                    position: 'left'
                },
                {
                    element: '.linkInfoDetails .load',
                    intro: helpContent.linkInfoDetails.load,
                    position: 'left'
                },
                {
                    element: '.linkInfoDetails .link-servers',
                    intro: helpContent.linkInfoDetails.linkServers,
                    position: 'top'
                },
                {
                    element: '.linkInfoDetails .unknown-list',
                    intro: helpContent.linkInfoDetails.unknownList,
                    position: 'left'
                },
                {
                    element: '.linkInfoDetails .search-n-order',
                    intro: helpContent.linkInfoDetails.searchAndOrder,
                    position: 'left'
                }
            ]
        };

        /**
         * on after overlay creation
         */
        $scope.onAfterOverlayCreation = function () {
            $rootElement.find('#copyright').show();
        };

        /**
         * on before overlay removal
         */
        $scope.onBeforeOverlayRemoval = function () {
            $rootElement.find('#copyright').hide();
        };

    } ]);
