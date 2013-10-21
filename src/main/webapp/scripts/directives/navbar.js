'use strict';

pinpointApp.constant('cfg', {
    applicationUrl: '/applications.pinpoint'
});

pinpointApp.directive('navbar', [ 'cfg', '$rootScope', '$http',
    '$document', '$timeout', '$location', '$routeParams',
    function (cfg, $rootScope, $http, $document, $timeout) {
        return {
            restrict: 'EA',
            replace: true,
            templateUrl: 'views/navbar.html',
            link: function (scope, element, attrs) {

                // define private variables
                var $application, $datetimepicker, oNavbarDao;

                // define private variables of methods
                var initialize, initializeDateTimePicker, initializeApplication, setDateTime,
                    broadcast, getApplicationList, getQueryEndTime, parseApplicationList;

                scope.showNavbar = false;

                initialize = function (navbarDao) {
                    oNavbarDao = navbarDao;

                    scope.showNavbar = true;
                    $application = element.find('.application').width(200);
                    scope.applications = [
                        {
                            text: 'Loading...',
                            value: ''
                        }
                    ];
                    scope.application = oNavbarDao.getApplication() || '';
                    scope.disableApplication = true;
                    scope.period = oNavbarDao.getPeriod() || '';
                    scope.queryEndTime = oNavbarDao.getQueryEndTime() || '';
//                    $http.defaults.useXDomain = true;

                    initializeDateTimePicker();
                    getApplicationList();
                };

                initializeDateTimePicker = function () {
                    $datetimepicker = element.find('#datetimepicker');
                    $datetimepicker.datetimepicker({
                        dateFormat: "yy-mm-dd",
                        timeFormat: "hh:mm tt",
                        beforeShow: function () {
                            $datetimepicker.datetimepicker('option', 'maxDate', new Date());
                            $datetimepicker.datetimepicker('option', 'maxDateTime', new Date());
                        },
                        onClose : function (currentTime, oTime) {
                            if (currentTime === oTime.lastVal) {
                                return;
                            }
                            broadcast();
                        }
                    });
                    setDateTime(oNavbarDao.getQueryEndTime());
                };

                /**
                 * set DateTime
                 */
                setDateTime = function (time) {
                    var date = new Date();
                    if (time) {
                        date.setTime(time);
                    }
                    $datetimepicker.datetimepicker('setDate', date);
                };

                /**
                 * _boardcast as applicationChanged with args
                 */
                broadcast = function () {

                    if (!scope.application || !scope.period || !getQueryEndTime()) {
                        return;
                    }

                    oNavbarDao.setApplication(scope.application);
                    oNavbarDao.setPeriod(scope.period);
                    oNavbarDao.setQueryEndTime(getQueryEndTime());

                    $timeout(function () {
                        scope.$emit('navbar.changed', oNavbarDao);
                    });

                };

                /**
                 * get Application List
                 */
                getApplicationList = function () {
                    $http.get(cfg.applicationUrl).success(function (data, status) {
                        if (angular.isArray(data) === false || data.length === 0) {
                            scope.applications[0].text = 'Application not found.';
                        } else {
                            parseApplicationList(data, function () {
                                scope.disableApplication = false;
                                $timeout(function () { // it should be apply after pushing data, so
                                    // it should work like nextTick
                                    initializeApplication();
                                });
                            });
                        }
                    }).error(function (data, status) {
                        scope.applications[0].text = 'Application error.';
                    });
                };

                /**
                 * get query end time
                 * @returns {*}
                 */
                getQueryEndTime = function () {
                    return $datetimepicker.datetimepicker('getDate').getTime();
                };

                /**
                 * parse Application List
                 */
                parseApplicationList = function (data, cb) {
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
                    if (angular.isFunction(cb)) {
                        cb.apply(scope);
                    }
                };

                /**
                 * initialize application
                 */
                initializeApplication = function () {
                    /**
                     * format option text
                     * @param state
                     * @returns {*}
                     */
                    function formatOptionText(state) {
                        if (!state.id) {
                            return state.text;
                        }
                        var chunk = state.text.split("@");
                        if (chunk.length > 1) {
                            var img = $document.get(0).createElement("img");
                            img.src = "/images/icons/" + chunk[1] + ".png";
                            return img.outerHTML + chunk[0];
                        } else {
                            return state.text;
                        }
                    }

                    $application.select2({
                        placeholder: "Select an application.",
                        allowClear: false,
                        formatResult: formatOptionText,
                        formatSelection: formatOptionText,
                        escapeMarkup: function (m) {
                            return m;
                        }
                    }).on("change", function (e) {
                        scope.application = e.val;
                        scope.$digest();
                        broadcast();
                        // 참고1 : http://jimhoskins.com/2012/12/17/angularjs-and-apply.html
                        // 참고2 : http://jsfiddle.net/CDvGy/2/
                    });

                    if (oNavbarDao.getApplication()) {
                        $application.select2('val', oNavbarDao.getApplication());
                        scope.application = oNavbarDao.getApplication();
                        broadcast();
                    }
                };

                /**
                 * scope period click
                 * @param val
                 */
                scope.periodClick = function (val) {
                    if (scope.period !== val) {
                        scope.period = val;
                    } else {
                        setDateTime();
                    }
                    broadcast();
                };

                /**
                 * scope event on navbar.initialize
                 */
                scope.$on('navbar.initialize', function (event, navbarDao) {
                    console.log('navbar.initialize : ', scope);
                    initialize(navbarDao);
                });
            }
        };
    } ]);
