(function() {
	'use strict';
	/**
	 * (en)timeSliderDirective 
	 * @ko timeSliderDirective
	 * @group Directive
	 * @name timeSliderDirective
	 * @class
	 */
	pinpointApp.constant('timeSliderDirectiveConfig', {
	    scaleCount: 10
	});
	
	pinpointApp.directive('timeSliderDirective', [ 'timeSliderDirectiveConfig', '$timeout', 'AnalyticsService', function (cfg, $timeout, analyticsService) {
	    return {
	        restrict: 'EA',
	        replace: true,
	        templateUrl: 'features/timeSlider/timeSlider.html?v=' + G_BUILD_TIME,
	        link: function postLink(scope, element, attrs) {
	        	var bAutoLoading = attrs["autoLoading"] === "true";
	        	var bAutoLoadingPause = false;
	            var $elSlider = element.find('.timeslider_input');
	
	            scope.oTimeSliderVoService = null;
	            scope.disableMore = false;
	            scope.done = false;

	            function initSlider(timeSliderVoService) {
	                scope.oTimeSliderVoService = timeSliderVoService;
	
	                if (scope.oTimeSliderVoService.getReady() === false) {
	                    return;
	                }
	
	                $timeout(function () {
	                    $elSlider.jslider(
	                        {
	                            from: scope.oTimeSliderVoService.getFrom(),
	                            to: scope.oTimeSliderVoService.getTo(),
	                            scale: parseScaleAsTimeFormat(getScale()),
	                            skin: "round_plastic",
	                            calculate: function (value) {
	                                return parseTimestampToTimeFormat(value);
	                            },
	                            beforeMouseDown: function (event, value) {
	                                return false;
	                            },
	                            beforeMouseMove: function (event, value) {
	                                return false;
	                            }
	                        }
	                    );
	                    element.find('.jslider-pointer-from').addClass('jslider-transition');
	                    element.find('.jslider-bg .v').addClass('jslider-transition');
	                }, 100);
	                checkDisableMore();
	            }
	
	            function checkDisableMore() {
	                if (scope.oTimeSliderVoService.getCount() && scope.oTimeSliderVoService.getTotal()) {
	                    if (scope.oTimeSliderVoService.getCount() >= scope.oTimeSliderVoService.getTotal()) {
	                        scope.disableMore = true;
	                    }
	                }
	            }

	            function getScale() {
	                // assumes maximum gap is 3 days - show in hours:minutes
	                var from =  scope.oTimeSliderVoService.getFrom(),
	                    to = scope.oTimeSliderVoService.getTo(),
	                    gap = to - from,
	                    unit = gap / (cfg.scaleCount - 1),
	                    tempScale = [];
	
	                _.times(cfg.scaleCount, function (n) {
	                    tempScale.push(from + (unit * n));
	                });
	                return tempScale;
	            }
	
	            function parseScaleAsTimeFormat(scale) {
	                var timeScale = [];
	                $.each( scale, function( index, value ) {
	                    timeScale.push(parseTimestampToTimeFormat(value));
	                });
	                return timeScale;
	            }
	
	            function parseTimestampToTimeFormat(timestamp) {
	                return moment(timestamp).format('HH:mm');
	            }
	
	            function setInnerFromTo(innerFrom, innerTo) {
	                $elSlider.jslider("value", innerFrom, innerTo);
	            }
	
	            scope.more = function () {
	            	analyticsService.send(analyticsService.CONST.CALLSTACK, analyticsService.CONST.CLK_MORE);
	            	if ( bAutoLoading ) {
						bAutoLoadingPause = !bAutoLoadingPause;
						scope.$emit('timeSliderDirective.changeLoadingStatus', bAutoLoadingPause);
					} else {
						scope.$emit('timeSliderDirective.moreClicked', scope.oTimeSliderVoService);
					}
	            };

	            scope.showSlider = function() {
					if ( scope.oTimeSliderVoService ) {
						return scope.oTimeSliderVoService.getInnerFrom() && scope.oTimeSliderVoService.getInnerTo();
					} else {
						return false;
					}
				};
	            scope.showCount = function() {
	            	if ( scope.oTimeSliderVoService ) {
						return scope.oTimeSliderVoService.getCount() && scope.oTimeSliderVoService.getTotal();
					} else {
	            		return false;
					}
				};
	            scope.getButtonClass = function() {
	            	if ( scope.disableMore ) {
	            		if ( scope.done ) {
	            			return "";
						} else {
	            			return "wait";
						}
					} else {
	            		return "active tada";
					}
				};
	            scope.getButtonText = function() {
	            	if ( bAutoLoading ) {
						return scope.done ? "Done" : bAutoLoadingPause ? "Resume" : "Pause";
					} else {
						return scope.done ? "Done" : "More";
					}
				};
	
	            scope.$on('timeSliderDirective.initialize', function (event, timeSliderVoService) {
	                initSlider(timeSliderVoService);
	                // checkDisableMore();
	            });
	
	            scope.$on('timeSliderDirective.setInnerFromTo', function (event, timeSliderVoService) {
	                scope.oTimeSliderVoService = timeSliderVoService;
	                setInnerFromTo(timeSliderVoService.getInnerFrom(), timeSliderVoService.getInnerTo());
	                checkDisableMore();
	            });
	            scope.getButtonStatus = function() {
	            	if ( bAutoLoading ) {
	            		return scope.done;
					} else {
						return scope.disableMore;
					}
				};
	
	            scope.$on('timeSliderDirective.enableMore', function (event) {
	                scope.disableMore = false;
	            });
	
	            scope.$on('timeSliderDirective.disableMore', function (event) {
	                scope.disableMore = true;
	            });
	
	            scope.$on('timeSliderDirective.changeMoreToDone', function (event) {
	                scope.done = true;
	            });
	
	            scope.$on('timeSliderDirective.changeDoneToMore', function (event) {
	                scope.done = false;
	            });
	        }
	    };
	}]);
})();