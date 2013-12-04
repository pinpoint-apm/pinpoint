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
                var $application, $fromPicker, $toPicker, oNavbarVo;

                // define private variables of methods
                var initialize, initializeDateTimePicker, initializeApplication, setDateTime, getQueryEndTimeFromServer,
                    broadcast, getApplicationList, getQueryStartTime, getQueryEndTime, parseApplicationList, emitAsChanged,
                    initializeWithStaticApplication, getPeriodType, setPeriodTypeAsCurrent, getDate;

                scope.showNavbar = false;
                scope.periodDelay = false;

                element.bind('selectstart', function (e) {
                    return false;
                });

                /**
                 * initialize
                 * @param navbarVo
                 */
                initialize = function (navbarVo) {
                    oNavbarVo = navbarVo;

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
                    scope.application = oNavbarVo.getApplication() || '';
                    scope.disableApplication = true;
                    scope.period = oNavbarVo.getPeriod() || 20;
                    scope.queryEndTime = oNavbarVo.getQueryEndTime() || '';
//                    $http.defaults.useXDomain = true;

                    initializeDateTimePicker();
                    getApplicationList();
                };

                /**
                 * initialize with static application
                 * @param navbarVo
                 */
                initializeWithStaticApplication = function (navbarVo) {
                    oNavbarVo = navbarVo;

                    scope.periodType = getPeriodType();
                    scope.showNavbar = true;
                    scope.showApplication = false;
                    scope.showStaticApplication = !scope.showApplication;
                    $application = element.find('.application');
                    scope.application = oNavbarVo.getApplication() || '';
                    scope.applicationName = oNavbarVo.getApplicationName() || '';
                    scope.period = oNavbarVo.getPeriod() || 20;
                    scope.queryEndTime = oNavbarVo.getQueryEndTime() || '';

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
//                            $toPicker.datetimepicker('option', 'minDate', $fromPicker.datetimepicker('getDate'));
                            if (getDate($fromPicker).isBefore(getDate($toPicker).add(-2).days()) || getDate($fromPicker).isAfter(getDate($toPicker))) {
                                setDateTime($toPicker, getDate($fromPicker).add(2).days());
                            }
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
                    setDateTime($fromPicker, oNavbarVo.getQueryStartTime() || new Date().addMinutes(-20));

                    $toPicker = element.find('#to-picker');
                    $toPicker.datetimepicker({
                        dateFormat: "yy-mm-dd",
                        timeFormat: "hh:mm tt",
                        onSelect: function () {
//                            $fromPicker.datetimepicker('option', 'maxDate', $toPicker.datetimepicker('getDate'));
                            if (getDate($fromPicker).isBefore(getDate($toPicker).add(-2).days()) || getDate($fromPicker).isAfter(getDate($toPicker))) {
                                setDateTime($fromPicker, getDate($toPicker).add(-2).days());
                            }
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
                    setDateTime($toPicker, oNavbarVo.getQueryEndTime());

//                    $fromPicker.datetimepicker('option', 'maxDate', $toPicker.datetimepicker('getDate'));
//                    $toPicker.datetimepicker('option', 'minDate', $fromPicker.datetimepicker('getDate'));
                };

                getDate = function ($picker) {
                    return $picker.datetimepicker('getDate');
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
                        periodType = oNavbarVo.getApplication() ? 'range' : 'last';
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
                    oNavbarVo.setApplication(scope.application);

                    if (scope.periodType === 'last' && scope.period) {
                        getQueryEndTimeFromServer(function (currentServerTime) {
                            oNavbarVo.setPeriod(scope.period);
                            oNavbarVo.setQueryEndTime(currentServerTime);
                            oNavbarVo.autoCalculateByQueryEndTimeAndPeriod();
                            emitAsChanged();
                        });
                    } else if (getQueryStartTime() && getQueryEndTime()) {
                        oNavbarVo.setQueryStartTime(getQueryStartTime());
                        oNavbarVo.setQueryEndTime(getQueryEndTime());
                        oNavbarVo.autoCalcultateByQueryStartTimeAndQueryEndTime();
                        emitAsChanged();
                    }
                };

                /**
                 * emit as changed
                 */
                emitAsChanged = function () {
                    setPeriodTypeAsCurrent();
                    scope.$emit('navbar.changed', oNavbarVo);
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

                    if (oNavbarVo.getApplication()) {
                        $application.select2('val', oNavbarVo.getApplication());
                        scope.application = oNavbarVo.getApplication();
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
                scope.$on('navbar.initialize', function (event, navbarVo) {
                    initialize(navbarVo);
                });

                /**
                 * scope event on navbar.initializeWithStaticApplication
                 */
                scope.$on('navbar.initializeWithStaticApplication', function (event, navbarVo) {
                    initializeWithStaticApplication(navbarVo);
                });

                /**
                 * set period
                 * @param period
                 */
                scope.setPeriod = function (period) {
                    scope.periodDelay = true;
                    scope.period = period;
                    broadcast();
                    $timeout(function () {
                        scope.periodDelay = false;
                        if (!scope.$$phase) {
                            scope.$digest();
                        }
                    }, 1000);
                };
            }
        };
    } ]);
