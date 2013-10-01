'use strict';

pinpointApp.constant('navbarConfig', {
    applicationUrl: '/applications.pinpoint'
});

pinpointApp.directive('navbar', [ 'navbarConfig', '$rootScope', '$http',
    '$document', '$timeout', '$location', '$routeParams',
    function (navbarConfig, $rootScope, $http, $document, $timeout, $location, $routeParams) {
        return {
            restrict: 'EA',
            replace: true,
            templateUrl: 'views/navbar.html',
            link: function (scope, element, attrs) {

                /**
                 * initialize
                 */
                scope.applications = [
                    {
                        text: 'Loading...',
                        value: ''
                    }
                ];

                scope.application = '';
                scope.disableApplication = true;
                scope.period = '';
                scope.queryEndTime = '';
                var applicationElement = element.find('.application');
                applicationElement.width(200);
                $http.defaults.useXDomain = true;

                $timeout(function () {
                    if ($routeParams.period) {
                        scope.period = $routeParams.period;
                    }
                    if ($routeParams.agentId) {
                        scope.agentId = $routeParams.agentId;
                    }
                    if ($routeParams.filter) {
                        scope.filter = $routeParams.filter;
                    }
                });

                /**
                 * date time picker
                 */
                var elDatetimepicker = element.find('#datetimepicker');
                elDatetimepicker.datetimepicker({
                    pick12HourFormat: false,
                    pickSeconds: false
                });
                $timeout(function () {
                    if ($routeParams.queryEndTime) {
                        setDateTime($routeParams.queryEndTime);
                    } else {
                        setDateTime();
                    }
                });

                /**
                 * set DateTime
                 */
                var setDateTime = function (time) {
                    var elDatetimepicker = element.find('#datetimepicker');
                    var picker = elDatetimepicker.data('datetimepicker');
                    var date = new Date();
                    if (time) {
                        date.setTime(time);
                    }
                    picker.setDate(date);
                };

                /**
                 * now
                 */
                scope.now = function () {
                    setDateTime();
                    if (!scope.period) {
                        scope.period = 20;
                    }
                    broadcast();
                };

                var getFirstPath = function () {
                    var splitedPath = $location.path().split('/');
                    return splitedPath[1] || 'main';
                };

                /**
                 * _boardcast as applicationChanged with args
                 */
                var broadcast = scope.broadcast = function () {
                    var firstPath = getFirstPath();

                    scope.queryPeriod = getQueryPeriod();
                    scope.queryEndTime = getQueryEndTime();

                    if (!scope.application || !scope.period || !scope.queryEndTime) {
                        $location.path('/' + firstPath);
                        return;
                    }

                    var splitedApp = scope.application.split('@'),
                        data = {
                            application: scope.application,
                            applicationName: splitedApp[0],
                            serviceType: splitedApp[1],
                            period: scope.period,
                            queryPeriod: scope.queryPeriod,
                            queryStartTime: scope.queryEndTime - scope.queryPeriod,
                            queryEndTime: scope.queryEndTime
                        },
                        url = '/' + firstPath + '/' + scope.application + '/' + scope.period + '/' + getQueryEndTime();

                    if (scope.agentId) {
                        data.agentId = scope.agentId;
                        url += '/' + scope.agentId;
                    } else if (scope.filter) {
                        data.filter = scope.filter;
                        url += '/' + scope.filter;
                    }

                    if($location.path() !== url) {
                        $location.path(url);
                    }

                    $timeout(function () {
                        $rootScope.$broadcast('navbar.applicationChanged', data);
                    }, 100);

                };

                /**
                 * get query period
                 */
                var getQueryPeriod = function () {
                    return scope.period * 1000 * 60;
                };

                var getQueryEndTime = function () {
                    var date = elDatetimepicker.data('datetimepicker'),
                        time = new Date(date.getDate());
                    return time.getTime();
                };

                /**
                 * get Application List
                 */
                var getApplicationList = function () {
                    $http.get(navbarConfig.applicationUrl).success(function (data, status) {

                        if (angular.isArray(data) === false || data.length === 0) {
                            scope.applications[0].text = 'Application not found.';
                        } else {
                            parseApplicationList(data, function () {
                                scope.disableApplication = false;
                                $timeout(function () { // it should be apply after pushing data, so
                                    // it should work like nextTick
                                    applySelect2Plugnin();
                                });
                            });
                        }

                    }).error(function (data, status) {
                        scope.applications[0].text = 'Application error.';
                    });
                };

                /**
                 * parse Application List
                 */
                var parseApplicationList = function (data, cb) {
                    scope.applications = [
                        {
                            text: '',
                            value: ''
                        }
                    ];
                    angular.forEach(data, function (value, key) {
                        scope.applications.push({
                            text: value.applicationName + "@" + value.serviceType,
                            value: value.applicationName + "@" + value.code
                        });
                    });
                    if (angular.isFunction(cb))
                        cb.apply(scope);
                };


                /**
                 * apply Select2 Plugin
                 */
                var applySelect2Plugnin = function () {
                    /**
                     * formatOptionText
                     */
                    var formatOptionText = function (state) {
                        if (!state.id)
                            return state.text;
                        var chunk = state.text.split("@");
                        if (chunk.length > 1) {
                            var img = $document.get(0).createElement("img");
                            img.src = "/images/icons/" + chunk[1] + ".png";
                            return img.outerHTML + chunk[0];
                        } else {
                            return state.text;
                        }
                    };

                    applicationElement.select2({
                        placeholder: "Select an application.",
                        allowClear: false,
                        formatResult: formatOptionText,
                        formatSelection: formatOptionText,
                        escapeMarkup: function (m) {
                            return m;
                        }
                    });
                    if ($routeParams.application) {
                        applicationElement.select2('val', $routeParams.application);
                        scope.application = $routeParams.application;
                        broadcast();
                    }
                    applicationElement.on("change", function (e) {
                        scope.application = e.val;
                        broadcast();
                        // 참고1 : http://jimhoskins.com/2012/12/17/angularjs-and-apply.html
                        // 참고2 : http://jsfiddle.net/CDvGy/2/
                        scope.$digest();
                    });
                };

                /**
                 * period click
                 * @param val
                 */
                scope.periodClick = function (val) {
                    if (scope.period !== val) {
                        scope.period = val;
                        broadcast();
                    } else {
                        scope.now();
                    }
                };

                /**
                 * watches
                 */
                scope.$watch('useAnchorDate', function (newVal, oldVal) {
                    scope.classAnchorDate = newVal ? 'input-append' : '';
//						if(newVal === false && oldVal === true){
//							var now = new Date();
                    // 초단위 무시.
//							now.setSeconds(0);
                    // 5분 단위로 조회
//							now.setMinutes(Math.floor(now.getMinutes() / 5 + 0.9) * 5);
//							elDatetimepicker.datetimepicker('setLocalDate', now);
//							broadcast();
//						}
                });

                /**
                 * executes
                 */
                getApplicationList();
            }
        };
    } ]);
