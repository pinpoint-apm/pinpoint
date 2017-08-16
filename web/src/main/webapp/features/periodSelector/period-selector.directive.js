(function( $ ) {
	'use strict';
	pinpointApp.constant( "periodSelectorDirectiveConfig", {
		ID: "PERIOD_SELECTOR_DRTV_"//,
		//periodTypePrefix: ".navbar.periodType"
	});

	pinpointApp.directive( "periodSelectorDirective", [ "periodSelectorDirectiveConfig", "$http", "$timeout", "$window",  "webStorage", "helpContentService", "UrlVoService", "AnalyticsService", "PreferenceService", "CommonAjaxService", "CommonUtilService",
		function ( cfg, $http, $timeout, $window, webStorage, helpContentService, UrlVoService, AnalyticsService, PreferenceService, CommonAjaxService, CommonUtilService ) {
			return {
				restrict: 'EA',
				replace: true,
				templateUrl: 'features/periodSelector/periodSelector.html?v=' + G_BUILD_TIME,
				link: function ( scope, element, attrs ) {
					cfg.ID += CommonUtilService.getRandomNum();
					var $element = $(element);
					var $toPicker = $element.find("#to-picker");
					var $fromPicker = $element.find("#from-picker");
					var $calendarPopupForRange = $element.find("#ui-datepicker-div");
					var oPeriodType = PreferenceService.getPeriodType();

					scope.useRealtime = attrs.useRealtime === "true";
					scope.useAutoUpdate = attrs.useAutoUpdate === "true";
					scope.disableButton = false;
					scope.aReadablePeriodTime = PreferenceService.getPeriodTime();

					// #autoUpdate
					scope.autoUpdate = false;
					scope.timeLeft = 10;
					scope.timeList = PreferenceService.getUpdateTimes();
					scope.timeCountDown = 10;

					function initialize() {
						scope.periodType = UrlVoService.getPeriodType(); // getPeriodType();
						scope.readablePeriod = UrlVoService.getReadablePeriod();// || PreferenceService.getPeriod();
						scope.periodCalendar = UrlVoService.getReadablePeriod();// || PreferenceService.getPeriod();
						initializeDateTimePicker();
					}
					// function getPeriodType() {
					// 	if ( UrlVoService.isRealtime() ) {
					// 		return oPeriodType.REALTIME;
					// 	}
					// 	var periodType = oPeriodType.LAST;
					// 	if ($window.name && webStorage.get($window.name + cfg.periodTypePrefix)) {
					// 		periodType = webStorage.get($window.name + cfg.periodTypePrefix);
					// 	} else {
					// 		periodType = UrlVoService.getApplication() ? oPeriodType.RANGE : oPeriodType.LAST;
					// 	}
					//
					// 	if (UrlVoService.getReadablePeriod() && _.indexOf(scope.aReadablePeriodTime, UrlVoService.getReadablePeriod()) < 0) {
					// 		periodType = oPeriodType.RANGE;
					// 	}
					// 	return periodType;
					// }
					function initializeDateTimePicker() {
						$calendarPopupForRange.find( ".guide" )
							.html( helpContentService.navbar.searchPeriod.guide.replace(/\{\{day\}\}/, PreferenceService.getMaxPeriod() ) );
						$calendarPopupForRange.find( "button.ui-datepicker-close" ).on( "click", function() {
							$calendarPopupForRange.hide();
						});
						$fromPicker.datetimepicker({
							altField: "#from-picker-alt",
							altFieldTimeOnly: false,
							dateFormat: "yy-mm-dd",
							timeFormat: "HH:mm z",
							controlType: "select",
							showButtonPanel: false,
							timezone: moment().utcOffset(),
							showTimezone: false,
							onSelect: function () {
								var momentFrom = moment( $fromPicker.datetimepicker( "getDate" ) );
								var momentTo = moment( $toPicker.datetimepicker( "getDate" ) );
								if ( momentTo.isAfter( moment( $fromPicker.datetimepicker("getDate") ).add( PreferenceService.getMaxPeriod(), "days" ) ) || momentFrom.isAfter( momentTo ) ) {
									var aPeriodTime = getPeriodForCalendar();
									setDateTime( $toPicker, momentFrom.add( aPeriodTime[0], aPeriodTime[1] ).format());
								}
							},
							onClose: function( currentTime ) {
								if ( $toPicker.val() !== "" ) {
									if ( $fromPicker.datetimepicker( "getDate" ) > $toPicker.datetimepicker( "getDate" ) ) {
										$toPicker.datetimepicker( "setDate", $fromPicker.datetimepicker( "getDate" ) );
									}
								} else {
									$toPicker.val( currentTime );
								}
							}
						});
						setDateTime($fromPicker, UrlVoService.getQueryStartTime() || moment().subtract(20, "minute").valueOf() );

						$toPicker.datetimepicker({
							altField: "#to-picker-alt",
							altFieldTimeOnly: false,
							dateFormat: "yy-mm-dd",
							timeFormat: "HH:mm z",
							controlType: "select",
							showButtonPanel: false,
							timezone: moment().utcOffset(),
							showTimezone: false,
							onSelect: function () {
								var momentFrom = moment( $fromPicker.datetimepicker( "getDate" ) );
								var momentTo = moment( $toPicker.datetimepicker( "getDate" ) );
								if ( momentFrom.isBefore( moment( $toPicker.datetimepicker( "getDate" ) ).subtract( PreferenceService.getMaxPeriod(), "days" ) ) || momentFrom.isAfter( momentTo ) ) {
									var aPeriodTime = getPeriodForCalendar();
									setDateTime($fromPicker, momentTo.subtract( aPeriodTime[0], aPeriodTime[1] ).format());
								}
							},
							onClose: function( currentTime ) {
								if ($fromPicker.val() !== '') {
									if ( $fromPicker.datetimepicker( "getDate" ) > $toPicker.datetimepicker( "getDate" ) ) {
										$fromPicker.datetimepicker( "setDate", $toPicker.datetimepicker( "getDate" ) );
									}
								} else {
									$fromPicker.val( currentTime) ;
								}
							}
						});
						setDateTime($toPicker, UrlVoService.getQueryEndTime());

						$("#from-picker-alt").on("click", function() {
							toggleCalendarPopup();
						});
						$("#to-picker-alt").on("click", function() {
							toggleCalendarPopup();
						});
						$(document).mousedown(function(event) {
							if ( $calendarPopupForRange.is(":visible") === false ) return;
							if ( $(event.target).parents("div#ui-datepicker-div").length === 0 ) {
								$calendarPopupForRange.hide();
							}
						});
					}
					function getPeriodForCalendar() {
						var a = [];
						var s = scope.periodCalendar.substring( scope.periodCalendar.length - 1 );
						a[0] = parseInt( scope.periodCalendar );
						a[1] = s == "d" ? "days" : s == "h" ? "hours" : "minutes";
						return a;
					}
					function toggleCalendarPopup() {
						if ( $calendarPopupForRange.is(":visible") ) {
							$calendarPopupForRange.hide();
						} else {
							$calendarPopupForRange.css( "left", $element.offset().left ).show();
						}
					}
					function startUpdate() {
						if ( scope.autoUpdate ) {
							scope.timeLeft -= 1;
							if ( scope.timeLeft === 0 ) {
								scope.update();
								scope.timeLeft = scope.timeCountDown;
							} else {
								$timeout( startUpdate, 1000 );
							}
						}
					}
					function setDateTime($picker, time) {
						$picker.datetimepicker("setDate", time ? new Date(time) : new Date());
					}
					function broadcast() {
						UrlVoService.setPeriodType( scope.periodType );

						if ( scope.periodType === oPeriodType.LAST ) {
							getQueryEndTimeFromServer( function( currentServerTime ) {
								UrlVoService.setReadablePeriod( scope.readablePeriod );
								UrlVoService.setQueryEndTime( currentServerTime );
								UrlVoService.autoCalculateByQueryEndDateTimeAndReadablePeriod();
								// setDateTime( $fromPicker, UrlVoService.getQueryStartTime() );
								// setDateTime( $toPicker, UrlVoService.getQueryEndTime() );
								emitAsChanged();
								scope.disableButton = false;
							});
						} else if ( scope.periodType === oPeriodType.REALTIME ) {
							getQueryEndTimeFromServer(function (currentServerTime) {
								UrlVoService.setReadablePeriod(PreferenceService.getRealtimeScatterXRangeStr());
								UrlVoService.setQueryEndTime(currentServerTime);
								UrlVoService.autoCalculateByQueryEndDateTimeAndReadablePeriod();
								emitAsChanged();
								// setDateTime($fromPicker, UrlVoService.getQueryStartTime());
								// setDateTime($toPicker, UrlVoService.getQueryEndTime());
							});
						} else if ( scope.periodType === oPeriodType.RANGE ) {
							UrlVoService.setQueryStartTime( $fromPicker.datetimepicker( "getDate" ).getTime() );
							UrlVoService.setQueryEndTime( $toPicker.datetimepicker( "getDate" ).getTime() );
							UrlVoService.autoCalcultateByQueryStartTimeAndQueryEndTime();
							emitAsChanged();
						}
					}
					function getQueryEndTimeFromServer(cb) {
						CommonAjaxService.getServerTime( function( serverTime ) {
							cb( serverTime );
						});
					}
					function emitAsChanged() {
						// setPeriodTypeAsCurrent();
						scope.$emit( "up.changed.period", cfg.ID );
					}
					// function setPeriodTypeAsCurrent() {
					// 	$window.name = $window.name || 'window.' + _.random(100000, 999999);
					// 	webStorage.add($window.name + cfg.periodTypePrefix, scope.periodType);
					// }
					scope.showUpdate = function () {
						if ( scope.useAutoUpdate ) {
							return scope.periodType === oPeriodType.LAST && ( scope.aReadablePeriodTime.indexOf( scope.readablePeriod ) >= 0 ) && scope.application ? true : false;
						} else {
							return false;
						}
					};
					scope.changeUpdateSetting = function() {
						AnalyticsService.send(AnalyticsService.CONST.MAIN, scope.autoUpdate ? AnalyticsService.CONST.TG_UPDATE_OFF : AnalyticsService.CONST.TG_UPDATE_ON );
					};
					scope.search = function () {
						scope.periodType = oPeriodType.RANGE;
						broadcast();
					};
					scope.changePeriodTimeOfLast = function ( readablePeriod ) {
						AnalyticsService.send( AnalyticsService.CONST.MAIN, AnalyticsService.CONST.CLK_TIME, readablePeriod );
						scope.periodType = oPeriodType.LAST;
						scope.disableButton = true;
						scope.readablePeriod = readablePeriod;
						broadcast();
					};
					scope.getPeriodClassInCalendar = function (period) {
						return ( scope.periodCalendar === period ? "btn-success" : "" );
					};
					scope.getPeriodClass = function (readablePeriod) {
						var periodClass = "";
						if ( scope.periodType !== oPeriodType.LAST ) {
							return periodClass;
						}
						if (scope.readablePeriod === readablePeriod) {
							periodClass += "btn-info";
						}

						if (scope.disableButton) {
							periodClass += " wait";
						}

						return periodClass;
					};
					scope.setPeriodTimeOfCalendar = function(period) {
						scope.periodCalendar = period;
					};
					scope.update = function () {
						var oldAutoUpdate = scope.autoUpdate;
						scope.autoUpdate = false;
						scope.disableButton = true;
						broadcast();
						$timeout(function () {
							scope.disableButton = false;
							scope.timeLeft = scope.timeCountDown;
							scope.autoUpdate = oldAutoUpdate;
							if (!scope.$$phase) {
								scope.$digest();
							}
						}, 1000);
					};
					scope.changeToRealtime = function() {
						if ( scope.periodType === oPeriodType.REALTIME ) return;
						AnalyticsService.send( AnalyticsService.CONST.MAIN, AnalyticsService.CONST.CLK_START_REALTIME );
						scope.periodType = oPeriodType.REALTIME;

						scope.autoUpdate = false;
						broadcast();
					};
					scope.changeToRange = function() {
						AnalyticsService.send(AnalyticsService.CONST.MAIN, AnalyticsService.CONST.TG_DATE, oPeriodType.RANGE);
						scope.periodType = oPeriodType.RANGE;
					};
					scope.changeToLast = function() {
						AnalyticsService.send(AnalyticsService.CONST.MAIN, AnalyticsService.CONST.TG_DATE, oPeriodType.LAST);
						scope.periodType = oPeriodType.LAST;
					};
					scope.getRealtimeBtnClass = function() {
						return UrlVoService.isRealtime() ? "btn-info" : "";
					};
					scope.getPeriodLabel = function( period ) {
						return period === "5m" ? "Last " + period : period;
					};
					scope.isRangePeriod = function() {
						return scope.periodType === oPeriodType.RANGE;
					};
					scope.isNotRangePeriod = function() {
						return scope.periodType !== oPeriodType.RANGE;
					};
					scope.$on( "period-selector.initialize", function() {
						initialize();
					});
				}
			};
		}
	]);
})( jQuery );
