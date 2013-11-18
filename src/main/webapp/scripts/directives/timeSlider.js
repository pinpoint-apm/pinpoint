'use strict';

pinpointApp.constant('timeSliderConfig', {
    scaleCount: 10
});

pinpointApp.directive('timeSlider', [ 'timeSliderConfig', '$timeout', function (cfg, $timeout) {
    return {
        restrict: 'EA',
        replace: true,
        templateUrl: 'views/timeSlider.html',
        link: function postLink(scope, element, attrs) {

            // define variables
            var $elSlider;
            // define private methods
            var initSlider, getScale, parseScaleAsTimeFormat, parseTimestampToTimeFormat, setInnerFromTo, checkDisableMore;

            // initialize private variables
            $elSlider = element.find('.timeslider_input');

            // initialize scope variables
            scope.oTimeSliderVo = null;
            scope.disableMore = false;
            scope.done = false;

            /**
             * init slider
             * @param timeSliderVo
             */
            initSlider = function (timeSliderVo) {
                scope.oTimeSliderVo = timeSliderVo;

                if (scope.oTimeSliderVo.getReady() === false) {
                    return;
                }

                $timeout(function () {
                    $elSlider.jslider(
                        {
                            from: scope.oTimeSliderVo.getFrom(),
                            to: scope.oTimeSliderVo.getTo(),
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
                });
                checkDisableMore();
            };

            checkDisableMore = function () {
                if (scope.oTimeSliderVo.getCount() && scope.oTimeSliderVo.getTotal()) {
                    if (scope.oTimeSliderVo.getCount() >= scope.oTimeSliderVo.getTotal()) {
                        scope.disableMore = true;
                    }
                }
            };

            /**
             * get scale
             * @returns {Array}
             */
            getScale = function () {
                // gap이 최대 3일이라 가정하고, 전부 시:분 으로 표시한다.
                var from =  scope.oTimeSliderVo.getFrom(),
                    to = scope.oTimeSliderVo.getTo(),
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
             * @returns {Time as String}
             */
            parseTimestampToTimeFormat = function (timestamp) {
                return new Date(timestamp).toString('HH:mm');
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
                scope.$emit('timeSlider.moreClicked', scope.oTimeSliderVo);
            };

            /**
             * scope event on timeSlider.initialize
             */
            scope.$on('timeSlider.initialize', function (event, timeSliderVo) {
                initSlider(timeSliderVo);
                checkDisableMore();
            });

            /**
             * scope event on setInnerFromTo
             */
            scope.$on('timeSlider.setInnerFromTo', function (event, timeSliderVo) {
                scope.oTimeSliderVo = timeSliderVo;
                setInnerFromTo(timeSliderVo.getInnerFrom(), timeSliderVo.getInnerTo());
                checkDisableMore();
            });

            /**
             * scope event on enable more
             */
            scope.$on('timeSlider.enableMore', function (event) {
                scope.disableMore = false;
            });

            /**
             * scope event on disable more
             */
            scope.$on('timeSlider.disableMore', function (event) {
                scope.disableMore = true;
            });

            /**
             * scope event on change more to done
             */
            scope.$on('timeSlider.changeMoreToDone', function (event) {
                scope.done = true;
            });

            /**
             * scope event on change done to more
             */
            scope.$on('timeSlider.changeDoneToMore', function (event) {
                scope.done = false;
            });
        }
    };
}]);
