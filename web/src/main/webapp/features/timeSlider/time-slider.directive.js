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
	
	pinpointApp.directive('timeSliderDirective', [ 'timeSliderDirectiveConfig', '$timeout', function (cfg, $timeout) {
	    return {
	        restrict: 'EA',
	        replace: true,
	        templateUrl: 'features/timeSlider/timeSlider.html',
	        link: function postLink(scope, element, attrs) {
	
	            // define variables
	            var $elSlider;
	            // define private methods
	            var initSlider, getScale, parseScaleAsTimeFormat, parseTimestampToTimeFormat, setInnerFromTo, checkDisableMore;
	
	            // initialize private variables
	            $elSlider = element.find('.timeslider_input');
	
	            // initialize scope variables
	            scope.oTimeSliderVoService = null;
	            scope.disableMore = false;
	            scope.done = false;
	
	            /**
	             * init slider
	             * @param timeSliderVoService
	             */
	            initSlider = function (timeSliderVoService) {
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
	            };
	
	            checkDisableMore = function () {
	                if (scope.oTimeSliderVoService.getCount() && scope.oTimeSliderVoService.getTotal()) {
	                    if (scope.oTimeSliderVoService.getCount() >= scope.oTimeSliderVoService.getTotal()) {
	                        scope.disableMore = true;
	                    }
	                }
	            };
	
	            /**
	             * get scale
	             * @returns {Array}
	             */
	            getScale = function () {
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
	            };
	
	            /**
	             * parse scale as time format
	             * @param scale
	             * @returns {Array}
	             */
	            parseScaleAsTimeFormat = function (scale) {
	                var timeScale = [];
	                _.each(scale, function (value) {
	                    timeScale.push(parseTimestampToTimeFormat(value));
	                });
	                return timeScale;
	            };
	
	            /**
	             * parse timestamp to time format
	             * @param timestamp
	             * @returns {String}
	             */
	            parseTimestampToTimeFormat = function (timestamp) {
	                return moment(timestamp).format('HH:mm');
	            };
	
	            /**
	             * set inner from-to
	             * @param innerFrom
	             * @param innerTo
	             */
	            setInnerFromTo = function (innerFrom, innerTo) {
	                $elSlider.jslider("value", innerFrom, innerTo);
	            };
	
	            /**
	             * scope more
	             */
	            scope.more = function () {
	            	$at($at.CALLSTACK, $at.CLK_MORE);
	                scope.$emit('timeSliderDirective.moreClicked', scope.oTimeSliderVoService);
	            };
	
	            /**
	             * scope event on timeSliderDirective.initialize
	             */
	            scope.$on('timeSliderDirective.initialize', function (event, timeSliderVoService) {
	                initSlider(timeSliderVoService);
	                checkDisableMore();
	            });
	
	            /**
	             * scope event on setInnerFromTo
	             */
	            scope.$on('timeSliderDirective.setInnerFromTo', function (event, timeSliderVoService) {
	                scope.oTimeSliderVoService = timeSliderVoService;
	                setInnerFromTo(timeSliderVoService.getInnerFrom(), timeSliderVoService.getInnerTo());
	                checkDisableMore();
	            });
	
	            /**
	             * scope event on enable more
	             */
	            scope.$on('timeSliderDirective.enableMore', function (event) {
	                scope.disableMore = false;
	            });
	
	            /**
	             * scope event on disable more
	             */
	            scope.$on('timeSliderDirective.disableMore', function (event) {
	                scope.disableMore = true;
	            });
	
	            /**
	             * scope event on change more to done
	             */
	            scope.$on('timeSliderDirective.changeMoreToDone', function (event) {
	                scope.done = true;
	            });
	
	            /**
	             * scope event on change done to more
	             */
	            scope.$on('timeSliderDirective.changeDoneToMore', function (event) {
	                scope.done = false;
	            });
	        }
	    };
	}]);
})();