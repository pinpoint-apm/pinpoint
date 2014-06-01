'use strict';

pinpointApp.controller('MainCtrl', [ 'filterConfig', '$scope', '$timeout', '$routeParams', 'location', 'NavbarVo', 'encodeURIComponentFilter', '$window', 'SidebarTitleVo', 'filteredMapUtil', '$rootElement',
    function (cfg, $scope, $timeout, $routeParams, location, NavbarVo, encodeURIComponentFilter, $window, SidebarTitleVo, filteredMapUtil, $rootElement) {

        // define private variables
        var oNavbarVo, bNodeSelected, bNoData;

        // define private variables of methods
        var getFirstPathOfLocation, changeLocation, openFilteredMapWithFilterVo;

        // initialize scope variables
        $scope.hasScatter = false;
        $window.htoScatter = {};
        bNodeSelected = true;
        $scope.bShowHelpIcons = false;
        bNoData = false;

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

        $scope.$on('servermap.hasData', function (event) {
            bNoData = false;
        });

        $scope.$on('servermap.hasNoData', function (event) {
            bNoData = true;
        });

        /**
         * scope event on navbar.changed
         */
        $scope.$on('navbar.changed', function (event, navbarVo) {
            oNavbarVo = navbarVo;
            changeLocation(oNavbarVo);
            $window.htoScatter = {};
            $scope.hasScatter = false;
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
         * scope event on linkInfoDetails.ResponseSummary.barClicked
         */
        $scope.$on('linkInfoDetails.ResponseSummary.barClicked', function (event, oServerMapFilterVo, oServerMapHintVo) {
            openFilteredMapWithFilterVo(oServerMapFilterVo, oServerMapHintVo);
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
                    intro: "핀포인트가 설치된 어플리케이션 목록 입니다."
                },
                {
                    element: '#navbar_period',
                    intro: "조회방법1 : 현재 시간을 기준으로 x 시간 전 조회<br>조회방법2 : 시작 ~ 끝 시간 조회"
                },
                {
                    element: '#servermap',
                    intro: "<div style='width:300px;'>분산된 서버를 도식화한 지도 입니다.<br>사용방법1 : 왼쪽 버튼으로 노드/링크를 선택한다.<br>사용방법2 : 오른쪽 버튼으로 노드를 선택한다.<br>사용방법3 : 휠을 이용하여 확대/축소 한다.<br>사용방법4 : 배경을 더블 클릭하여 가운데로 맞춘다.</div>",
                    position: 'right'
                },
                {
                    element: '#scatter',
                    intro: '마우스로 드래그 하시면 해당 영역의 트렌젝션을 상세보기 할 수 있습니다.',
                    position: 'left'
                },
                {
                    element: '.nodeInfoDetails .response-summary',
                    intro: "응답시간 요약 입니다.",
                    position: 'left'
                },
                {
                    element: '.nodeInfoDetails .load',
                    intro: '시간별 응답 속도 입니다.',
                    position: 'left'
                },
                {
                    element: '.nodeInfoDetails .node-servers',
                    intro: '물리서버와 인스터스 정보 입니다.',
                    position: 'top'
                },
                {
                    element: '.nodeInfoDetails .unknown-list',
                    intro: '차트 오른쪽 상단의 아이콘부터,<br>첫번째 : Response Summary / Load 차트 변환<br>두번째 : 해당 노드 상세보기',
                    position: 'left'
                },
                {
                    element: '.nodeInfoDetails .search-n-order',
                    intro: '서버 이름과 Count로 검색이 가능합니다.<br>Name, Count 클릭시 오름/내림차순 정렬 됩니다.',
                    position: 'left'
                },
                {
                    element: '.linkInfoDetails .response-summary',
                    intro: "응답시간 요약 입니다.<br>컬럼 클릭으로 필러링 됩니다.",
                    position: 'left'
                },
                {
                    element: '.linkInfoDetails .load',
                    intro: '시간별 응답 속도 입니다.',
                    position: 'left'
                },
                {
                    element: '.linkInfoDetails .link-servers',
                    intro: '인스터스 정보 입니다.',
                    position: 'top'
                },
                {
                    element: '.linkInfoDetails .unknown-list',
                    intro: '차트 오른쪽 상단의 아이콘부터,<br>첫번째 : Response Summary / Load 차트 변환<br>두번째 : 해당 노드 상세보기',
                    position: 'left'
                },
                {
                    element: '.linkInfoDetails .search-n-order',
                    intro: '서버 이름과 Count로 검색이 가능합니다.<br>Name, Count 클릭시 오름/내림차순 정렬 됩니다.',
                    position: 'left'
                }
            ]
        };

    } ]);
