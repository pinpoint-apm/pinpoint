'use strict';

pinpointApp.constant('cfg', {
    applicationUrl: '/applications.pinpoint',
    serverTimeUrl: '/serverTime.pinpoint',
    periodTypePrefix: '.navbar.periodType'
});

pinpointApp.directive('navbar', [ 'cfg', '$rootScope', '$http',
    '$document', '$timeout', '$window',  'webStorage',
    function (cfg, $rootScope, $http, $document, $timeout, $window, webStorage) {
        return {
            restrict: 'EA',
            replace: true,
            templateUrl: 'views/navbar.html',
            link: function (scope, element, attrs) {

                // define private variables
                var $application, $fromPicker, $toPicker, oNavbarDao;

                // define private variables of methods
                var initialize, initializeDateTimePicker, initializeApplication, setDateTime, getQueryEndTimeFromServer,
                    broadcast, getApplicationList, getQueryStartTime, getQueryEndTime, parseApplicationList, emitAsChanged,
                    initializeWithStaticApplication, getPeriodType, setPeriodTypeAsCurrent;

                scope.showNavbar = false;

                element.bind('selectstart', function (e) {
                    return false;
                });

                /**
                 * initialize
                 * @param navbarDao
                 */
                initialize = function (navbarDao) {
                    oNavbarDao = navbarDao;

                    scope.periodType = getPeriodType();
                    scope.showNavbar = true;
                    scope.showApplication = true;
                    scope.showStatic = !scope.showApplication;
                    $application = element.find('.application');
                    scope.applications = [
                        {
                            text: 'Loading...',
                            value: ''
                        }
                    ];
                    scope.application = oNavbarDao.getApplication() || '';
                    scope.disableApplication = true;
                    scope.period = oNavbarDao.getPeriod() || 20;
                    scope.queryEndTime = oNavbarDao.getQueryEndTime() || '';
//                    $http.defaults.useXDomain = true;

                    initializeDateTimePicker();
                    getApplicationList();
                };

                /**
                 * initialize with static application
                 * @param navbarDao
                 */
                initializeWithStaticApplication = function (navbarDao) {
                    oNavbarDao = navbarDao;

                    scope.periodType = getPeriodType();
                    scope.showNavbar = true;
                    scope.showApplication = false;
                    scope.showStaticApplication = !scope.showApplication;
                    $application = element.find('.application');
                    scope.application = oNavbarDao.getApplication() || '';
                    scope.applicationName = oNavbarDao.getApplicationName() || '';
                    scope.period = oNavbarDao.getPeriod() || 20;
                    scope.queryEndTime = oNavbarDao.getQueryEndTime() || '';

                    initializeDateTimePicker();
                };

                /**
                 * initialize date time picker
                 */
                initializeDateTimePicker = function () {
                    $fromPicker = element.find('#from-picker');
                    $fromPicker.datetimepicker({
                        dateFormat: "yy-mm-dd",
                        timeFormat: "hh:mm tt",
                        onSelect: function () {
                            $toPicker.datetimepicker('option', 'minDate', $fromPicker.datetimepicker('getDate'));
                        },
                        onClose: function (currentTime, oTime) {
                            if ($toPicker.val() !== '') {
                                if ($fromPicker.datetimepicker('getDate') > $toPicker.datetimepicker('getDate')) {
                                    $toPicker.datetimepicker('setDate', $fromPicker.datetimepicker('getDate'));
                                }
                            } else {
                                $toPicker.val(currentTime);
                            }
                        }
                    });
                    setDateTime($fromPicker, oNavbarDao.getQueryStartTime() || new Date().addMinutes(-20));

                    $toPicker = element.find('#to-picker');
                    $toPicker.datetimepicker({
                        dateFormat: "yy-mm-dd",
                        timeFormat: "hh:mm tt",
                        onSelect: function () {
                            $fromPicker.datetimepicker('option', 'maxDate', $toPicker.datetimepicker('getDate'));
                        },
                        onClose: function (currentTime, oTime) {
                            if ($fromPicker.val() !== '') {
                                if ($fromPicker.datetimepicker('getDate') > $toPicker.datetimepicker('getDate')) {
                                    $fromPicker.datetimepicker('setDate', $toPicker.datetimepicker('getDate'));
                                }
                            } else {
                                $fromPicker.val(currentTime);
                            }
                        }
                    });
                    setDateTime($toPicker, oNavbarDao.getQueryEndTime());

                    $fromPicker.datetimepicker('option', 'maxDate', $toPicker.datetimepicker('getDate'));
                    $toPicker.datetimepicker('option', 'minDate', $fromPicker.datetimepicker('getDate'));
                };

                /**
                 * get preiod type
                 * @returns {*}
                 */
                getPeriodType = function () {
                    var periodType;
                    if ($window.name && webStorage.session.get($window.name + cfg.periodTypePrefix)) {
                        periodType = webStorage.session.get($window.name + cfg.periodTypePrefix);
                    } else {
                        periodType = oNavbarDao.getApplication() ? 'range' : 'last';
                    }
                    return periodType;
                };

                setPeriodTypeAsCurrent = function () {
                    $window.name = $window.name || 'window.' + _.random(100000, 999999);
                    webStorage.session.add($window.name + cfg.periodTypePrefix, scope.periodType);
                };

                /**
                 * set DateTime
                 */
                setDateTime = function ($picker, time) {
                    var date = new Date();
                    if (time) {
                        date.setTime(time);
                    }
                    $picker.datetimepicker('setDate', date);
                };

                /**
                 * broadcast
                 */
                broadcast = function () {
                    if (!scope.application) {
                        return;
                    }
                    oNavbarDao.setApplication(scope.application);

                    if (scope.periodType === 'last' && scope.period) {
                        getQueryEndTimeFromServer(function (currentServerTime) {
                            oNavbarDao.setPeriod(scope.period);
                            oNavbarDao.setQueryEndTime(currentServerTime);
                            oNavbarDao.autoCalculateByQueryEndTimeAndPeriod();
                            emitAsChanged();
                        });
                    } else if (getQueryStartTime() && getQueryEndTime()) {
                        oNavbarDao.setQueryStartTime(getQueryStartTime());
                        oNavbarDao.setQueryEndTime(getQueryEndTime());
                        oNavbarDao.autoCalcultateByQueryStartTimeAndQueryEndTime();
                        emitAsChanged();
                    }
                };

                /**
                 * emit as changed
                 */
                emitAsChanged = function () {
                    setPeriodTypeAsCurrent();
                    scope.$emit('navbar.changed', oNavbarDao);
                };

                /**
                 * get query end time from server
                 * @param cb
                 */
                getQueryEndTimeFromServer = function (cb) {
                    $http.get(cfg.serverTimeUrl).success(function (data, status) {
                        cb(data.currentServerTime);
                    }).error(function (data, status) {

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
                 * get query start time
                 * @returns {*}
                 */
                getQueryStartTime = function () {
                    return $fromPicker.datetimepicker('getDate').getTime();
                };

                /**
                 * get query end time
                 * @returns {*}
                 */
                getQueryEndTime = function () {
                    return $toPicker.datetimepicker('getDate').getTime();
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
                            img.width = 20;
                            img.height = 20;
                            img.style.paddingRight = "3px";
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
                    }
                };

                /**
                 * search
                 */
                scope.search = function () {
                    broadcast();
                };

                /**
                 * scope event on navbar.initialize
                 */
                scope.$on('navbar.initialize', function (event, navbarDao) {
                    initialize(navbarDao);
                });

                /**
                 * scope event on navbar.initializeWithStaticApplication
                 */
                scope.$on('navbar.initializeWithStaticApplication', function (event, navbarDao) {
                    initializeWithStaticApplication(navbarDao);
                });

                /**
                 * scope watch on period
                 */
                scope.$watch('period', function (newValue, oldValue) {
                    if (newValue && oldValue) {
                        broadcast();
                    }
                });
            }
        };
    } ]);
