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
            link: function (scope, element) {

                // define private variables
                var $application, $fromPicker, $toPicker, oNavbarVo, aReadablePeriodList;

                // define private variables of methods
                var initialize, initializeDateTimePicker, initializeApplication, setDateTime, getQueryEndTimeFromServer,
                    broadcast, getApplicationList, getQueryStartTime, getQueryEndTime, parseApplicationList, emitAsChanged,
                    initializeWithStaticApplication, getPeriodType, setPeriodTypeAsCurrent, getDate, startUpdate,
                    resetTimeLeft;

                scope.showNavbar = false;
                scope.periodDelay = false;
                aReadablePeriodList = ['5m', '20m', '1h', '3h', '6h', '12h', '1d', '2d'];
                scope.autoUpdate = false;
                scope.timeLeft = 10;
                scope.timeCountDown = 10;
                scope.timeList = [
                    {
                        time: 10,
                        label: '10 seconds'
                    },
                    {
                        time: 20,
                        label: '20 seconds'
                    },
                    {
                        time: 30,
                        label: '30 seconds'
                    },
                    {
                        time: 60,
                        label: '1 minute'
                    }
                ];
                scope.applications = [
                    {
                        text: 'Select an application.',
                        value: ''
                    }
                ];


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
                    scope.showStaticApplication = false;
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
                    scope.readablePeriod = oNavbarVo.getReadablePeriod() || '20m';
                    scope.queryEndTime = oNavbarVo.getQueryEndTime() || '';

                    initializeApplication();
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
                    scope.showStaticApplication = true;
                    $application = element.find('.application');
                    scope.application = oNavbarVo.getApplication() || '';
                    scope.applicationName = oNavbarVo.getApplicationName() || '';
                    scope.readablePeriod = oNavbarVo.getReadablePeriod() || '20m';
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
                    if (oNavbarVo.getReadablePeriod() && _.indexOf(aReadablePeriodList, oNavbarVo.getReadablePeriod()) < 0) {
                        periodType = 'range';
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

                    if (scope.periodType === 'last' && scope.readablePeriod) {
                        getQueryEndTimeFromServer(function (currentServerTime) {
                            oNavbarVo.setReadablePeriod(scope.readablePeriod);
                            oNavbarVo.setQueryEndDateTime(moment(currentServerTime).format('YYYY-MM-DD-HH-mm-ss'));
                            oNavbarVo.autoCalculateByQueryEndDateTimeAndReadablePeriod();
                            emitAsChanged();
                            setDateTime($fromPicker, oNavbarVo.getQueryStartTime());
                            setDateTime($toPicker, oNavbarVo.getQueryEndTime());
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
//                                    initializeApplication();
                                    if (oNavbarVo.getApplication()) {
                                        $application.select2('val', oNavbarVo.getApplication());
                                        scope.application = oNavbarVo.getApplication();
                                    }
                                });
                            });
                        }
                        scope.hideFakeApplication = true;
                    }).error(function (data, status) {
                        scope.applications[0].text = 'Application error.';
                        scope.hideFakeApplication = true;
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
                            //img.style.width = "20px";
                            img.style.height = "25px";
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
                };

                /**
                 * search
                 */
                scope.search = function () {
                    broadcast();
                };

                /**
                 * set period
                 * @param readablePeriod
                 */
                scope.setPeriod = function (readablePeriod) {
                    scope.periodDelay = true;
                    scope.readablePeriod = readablePeriod;
                    scope.autoUpdate = false;
                    broadcast();
                    $timeout(function () {
                        scope.periodDelay = false;
                        if (!scope.$$phase) {
                            scope.$digest();
                        }
                    }, 1000);
                };

                /**
                 * get period class
                 * @param readablePeriod
                 * @returns {string}
                 */
                scope.getPeriodClass = function (readablePeriod) {
                    var periodClass = '';
                    if (scope.readablePeriod === readablePeriod) {
                        periodClass += 'btn-info';
                    }

                    if (scope.periodDelay) {
                        periodClass += ' wait';
                    }

                    return periodClass;
                };

                /**
                 * show upddate
                 * @returns {boolean}
                 */
                scope.showUpdate = function () {
                    return (_.indexOf(['5m', '20m', '1h', '3h'], scope.readablePeriod) >= 0)
                        && scope.application ? true : false
                };

                /**
                 * start update
                 */
                startUpdate = function () {
                    if (scope.autoUpdate) {
                        scope.timeLeft -= 1;
                        if (scope.timeLeft === 0) {
                            scope.update();
                            scope.timeLeft = scope.timeCountDown;
                        } else {
                            $timeout(startUpdate, 1000);
                        }
                    }
                };

                /**
                 * reset tiem left
                 */
                resetTimeLeft = function () {
                    scope.timeLeft = scope.timeCountDown;
                };

                /**
                 * set auto update time
                 * @param time
                 */
                scope.setAutoUpdateTime = function (time) {
                    scope.timeCountDown = time;
                    scope.timeLeft = time;
                };

                /**
                 * update
                 */
                scope.update = function () {
                    var oldAutoUpdate = scope.autoUpdate;
                    scope.autoUpdate = false;
                    scope.periodDelay = true;
                    broadcast();
                    $timeout(function () {
                        scope.periodDelay = false;
                        resetTimeLeft();
                        scope.autoUpdate = oldAutoUpdate;
                        if (!scope.$$phase) {
                            scope.$digest();
                        }
                    }, 1000);
                };

                /**
                 * toggle period
                 * @param type
                 */
                scope.togglePeriod = function (type) {
                    scope.periodType = type;
                    scope.autoUpdate = false;
                };

                /**
                 * watch auto update
                 */
                scope.$watch('autoUpdate', function (newVal, oldVal) {
                    if (newVal) {
                        $timeout(startUpdate, 1000);
                    } else {
                        resetTimeLeft();
                    }
                });

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
            }
        };
    } ]);
