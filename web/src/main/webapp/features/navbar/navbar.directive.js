(function() {
	'use strict';
	/**
	 * (en)navbarDirective 
	 * @ko navbarDirective
	 * @group Directive
	 * @name navbarDirective
	 * @class
	 */	
	pinpointApp.constant('cfg', {
	    applicationUrl: '/applications.pinpoint',
	    serverTimeUrl: '/serverTime.pinpoint',
	    periodTypePrefix: '.navbar.periodType'
	});
	
	pinpointApp.directive('navbarDirective', [ 'cfg', '$rootScope', '$http','$document', '$timeout', '$window',  'webStorage', 'helpContentTemplate', 'helpContentService',
	    function (cfg, $rootScope, $http, $document, $timeout, $window, webStorage, helpContentTemplate, helpContentService) {
	        return {
	            restrict: 'EA',
	            replace: true,
	            templateUrl: 'features/navbar/navbar.html',
	            link: function (scope, element) {
	
	            	var DEFAULT_RANGE = 2;
	            	var MAX_RANGE = 8;
	                // define private variables
	                var $application, $fromPicker, $toPicker, oNavbarVoService, aReadablePeriodList;
	
	                // define private variables of methods
	                var initialize, initializeDateTimePicker, initializeApplication, setDateTime, getQueryEndTimeFromServer,
	                    broadcast, getApplicationList, getQueryStartTime, getQueryEndTime, parseApplicationList, emitAsChanged,
	                    initializeWithStaticApplication, getPeriodType, setPeriodTypeAsCurrent, getDate, startUpdate,
	                    resetTimeLeft, getRangeFromStorage, setRangeToStorage, getMilliSecondByReadablePeriod, movePeriod;
	
	                /**
	                 * getRangeFromStorage
	                 */
	                getRangeFromStorage = function(app) {
                		return webStorage.get( app ) || DEFAULT_RANGE;
	                };
	                /**
	                 * setRangeToStorage
	                 */
	                setRangeToStorage = function(app, range) {
	                	if (angular.isUndefined(app) || app == null || angular.isUndefined(range) || range == null) {
	                		return;
	                	}
	                	webStorage.add(app, range);
	                };
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
	                scope.range = getRangeFromStorage(scope.applicatoin);
	                scope.rangeList = (function() {
	                	var a = [];
	                	for( var i = 1 ; i <= MAX_RANGE ; i++ ) {
	                		a.push( i );
	                	}
	                	return a;
	                })();
	                scope.applications = [
	                    {
	                        text: 'Select an application.',
	                        value: ''
	                    }
	                ];
	
	
	                element.bind('selectstart', function (e) {
	                    return false;
	                });
	                
	                jQuery('.navbarTooltip').tooltipster({
                    	content: function() {
                    		return helpContentTemplate(helpContentService.navbar.applicationSelector) + helpContentTemplate(helpContentService.navbar.depth) + helpContentTemplate(helpContentService.navbar.periodSelector);
                    	},
                    	position: "bottom",
                    	trigger: "click"
                    });
	
	                /**
	                 * initialize
	                 * @param navbarVo
	                 */
	                initialize = function (navbarVoService) {
	                    oNavbarVoService = navbarVoService;
	
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
	                    scope.application = oNavbarVoService.getApplication() || '';
	                    scope.disableApplication = true;
	                    scope.readablePeriod = oNavbarVoService.getReadablePeriod() || '20m';
	                    scope.queryEndTime = oNavbarVoService.getQueryEndTime() || '';
	
	                    initializeApplication();
	                    initializeDateTimePicker();
	                    getApplicationList();
	                };
	
	                /**
	                 * initialize with static application
	                 * @param navbarVo
	                 */
	                initializeWithStaticApplication = function (navbarVoService) {
	                    oNavbarVoService = navbarVoService;
	
	                    scope.periodType = getPeriodType();
	                    scope.showNavbar = true;
	                    scope.showStaticApplication = true;
	                    $application = element.find('.application');
	                    scope.application = oNavbarVoService.getApplication() || '';
	                    scope.applicationName = oNavbarVoService.getApplicationName() || '';
	                    scope.readablePeriod = oNavbarVoService.getReadablePeriod() || '20m';
	                    scope.queryEndTime = oNavbarVoService.getQueryEndTime() || '';
	
	                    initializeDateTimePicker();
	                };
	
	                /**
	                 * initialize date time picker
	                 */
	                initializeDateTimePicker = function () {
	                    $fromPicker = element.find('#from-picker');
	                    $fromPicker.datetimepicker({
	                        dateFormat: "yy-mm-dd",
	                        timeFormat: "HH:mm",
	                        controlType: "select",
	                        onSelect: function () {
	                        	var momentFrom = moment(getDate($fromPicker));
	                        	var momentTo = moment(getDate($toPicker));
	                        	if ( momentFrom.isBefore(momentTo.subtract(2, "days")) || momentFrom.isAfter(momentTo) ) {
	                        		setDateTime($toPicker, momentFrom.add(2, "days").format());
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
	                    setDateTime($fromPicker, oNavbarVoService.getQueryStartTime() || moment().subtract(20, "minute").valueOf());
	
	                    $toPicker = element.find('#to-picker');
	                    $toPicker.datetimepicker({
	                        dateFormat: "yy-mm-dd",
	                        timeFormat: "HH:mm",
	                        controlType: "select",
	                        onSelect: function () {
	                        	var momentFrom = moment(getDate($fromPicker));
	                        	var momentTo = moment(getDate($toPicker));
	                        	if ( momentFrom.isBefore(momentTo.subtract(2, "days")) || momentFrom.isAfter(momentTo) ) {
	                        		setDateTime($fromPicker, momentTo.subtract(2, "days").format());
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
	                    setDateTime($toPicker, oNavbarVoService.getQueryEndTime());
	
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
	                    if ($window.name && webStorage.get($window.name + cfg.periodTypePrefix)) {
	                        periodType = webStorage.get($window.name + cfg.periodTypePrefix);
	                    } else {
	                        periodType = oNavbarVoService.getApplication() ? 'range' : 'last';
	                    }
	                    if (oNavbarVoService.getReadablePeriod() && _.indexOf(aReadablePeriodList, oNavbarVoService.getReadablePeriod()) < 0) {
	                        periodType = 'range';
	                    }
	                    return periodType;
	                };
	
	                setPeriodTypeAsCurrent = function () {
	                    $window.name = $window.name || 'window.' + _.random(100000, 999999);
	                    webStorage.add($window.name + cfg.periodTypePrefix, scope.periodType);
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
	                    oNavbarVoService.setApplication(scope.application);
	                    
	                    scope.range = getRangeFromStorage(scope.application);
	                    oNavbarVoService.setCallerRange( scope.range );
	                    oNavbarVoService.setCalleeRange( scope.range );
	                    
	                    if (scope.periodType === 'last' && scope.readablePeriod) {
	                        getQueryEndTimeFromServer(function (currentServerTime) {
	                            oNavbarVoService.setReadablePeriod(scope.readablePeriod);
	                            oNavbarVoService.setQueryEndDateTime(moment(currentServerTime).format('YYYY-MM-DD-HH-mm-ss'));
	                            oNavbarVoService.autoCalculateByQueryEndDateTimeAndReadablePeriod();
	                            emitAsChanged();
	                            setDateTime($fromPicker, oNavbarVoService.getQueryStartTime());
	                            setDateTime($toPicker, oNavbarVoService.getQueryEndTime());
	                        });
	                    } else if (getQueryStartTime() && getQueryEndTime()) {
	                        oNavbarVoService.setQueryStartTime(getQueryStartTime());
	                        oNavbarVoService.setQueryEndTime(getQueryEndTime());
	                        oNavbarVoService.autoCalcultateByQueryStartTimeAndQueryEndTime();
	                        emitAsChanged();
	                    }
	                };
	
	                /**
	                 * emit as changed
	                 */
	                emitAsChanged = function () {
	                    setPeriodTypeAsCurrent();
	                    scope.$emit('navbarDirective.changed', oNavbarVoService);
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
	                                    if (oNavbarVoService.getApplication()) {
	                                        $application.select2('val', oNavbarVoService.getApplication());
	                                        scope.application = oNavbarVoService.getApplication();
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
	                        searchInputPlaceholder: "Input your application name.",
	                        allowClear: false,
	                        formatResult: formatOptionText,
	                        formatSelection: formatOptionText,
	                        escapeMarkup: function (m) {
	                            return m;
	                        }
	                    }).on("change", function (e) {
	                    	$at( $at.MAIN, $at.CLK_APPLICATION );
	                        scope.application = e.val;
	                        scope.$digest();
	                        broadcast();
	                        // ref1 : http://jimhoskins.com/2012/12/17/angularjs-and-apply.html
	                        // ref2 : http://jsfiddle.net/CDvGy/2/
	                    });
	                    console.log( $application.select2 );
	                };
	                getMilliSecondByReadablePeriod = function( period ) {
	                	var time = parseInt( period );
	                	switch( period.substring( period.length - 1) ) {
		                	case "m":
		                		time *= 60 * 1000;
		                		break;
		                	case "h":
		                		time *= 60 * 60 * 1000;
		                		break;
		                	case "d":
		                		time *= 60 * 60 * 24 * 1000;
		                		break;
	                	}
	                	return time;
	                };
	                movePeriod = function( movedTime ) {
	                	if ( scope.periodType === "last" ) {
		                	oNavbarVoService.setQueryEndDateTime(moment(oNavbarVoService.getQueryEndTime() + movedTime).format('YYYY-MM-DD-HH-mm-ss'));
		                    oNavbarVoService.autoCalculateByQueryEndDateTimeAndReadablePeriod();
		                    emitAsChanged();
		                    setDateTime($fromPicker, oNavbarVoService.getQueryStartTime());
		                    setDateTime($toPicker, oNavbarVoService.getQueryEndTime());
	                	} else {
		                    setDateTime($fromPicker, oNavbarVoService.getQueryStartTime() + movedTime);
		                    setDateTime($toPicker, oNavbarVoService.getQueryEndTime() + movedTime );
	                        oNavbarVoService.setQueryStartTime(getQueryStartTime());
	                        oNavbarVoService.setQueryEndTime(getQueryEndTime());
	                        oNavbarVoService.autoCalcultateByQueryStartTimeAndQueryEndTime();
	                        emitAsChanged();
	                	}
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
	                	$at($at.MAIN, $at.CLK_TIME, readablePeriod);
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
	                scope.getPreviousClass = function() {
	                	return "";
	                };
	                scope.getNextClass = function() {
	                	return "";
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
	                	$at($at.MAIN, $at.CLK_UPDATE_TIME, time + "s");
	                    scope.timeCountDown = time;
	                    scope.timeLeft = time;
	                };
	                scope.setNodeRange = function(range) {
	                	$at($at.MAIN, $at.CLK_CALLEE_RANGE, range);
	                	$at($at.MAIN, $at.CLK_CALLER_RANGE, range);
	                	scope.range = range;
	                	setRangeToStorage(scope.application, range);
	                	broadcast();
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
	                	$at($at.MAIN, $at.TG_DATE, type);
	                    scope.periodType = type;
	                    scope.autoUpdate = false;
	                };
	
	                /**
	                 * watch auto update
	                 */
	                scope.$watch('autoUpdate', function (newVal, oldVal) {
	                    if (newVal) {
	                    	$at($at.MAIN, $at.TG_UPDATE_ON);
	                        $timeout(startUpdate, 1000);
	                    } else {
	                        resetTimeLeft();
	                    }
	                });
	
	                /**
	                 * scope event on navbarDirective.initialize
	                 */
	                scope.$on('navbarDirective.initialize', function (event, navbarVo) {
	                    initialize(navbarVo);
	                });
	
	                /**
	                 * scope event on navbarDirective.initializeWithStaticApplication
	                 */
	                scope.$on('navbarDirective.initializeWithStaticApplication', function (event, navbarVo) {
	                    initializeWithStaticApplication(navbarVo);
	                });
	                
	                scope.$on('navbarDirective.moveThePast', function (event) {
	                	if ( scope.periodType === "last" ) {
	                		movePeriod(-getMilliSecondByReadablePeriod( scope.readablePeriod ));
	                	} else {
	                		movePeriod(-(oNavbarVoService.getQueryEndTime() - oNavbarVoService.getQueryStartTime()));
	                	}
	                });
	                
	                scope.$on('navbarDirective.moveTheFuture', function (event) {
	                	if ( scope.periodType === "last" ) {
	                		movePeriod(getMilliSecondByReadablePeriod( scope.readablePeriod ));
	                	} else {
	                		movePeriod(oNavbarVoService.getQueryEndTime() - oNavbarVoService.getQueryStartTime());
	                	}
	                });
	            }
	        };
	    }
	]);
})();