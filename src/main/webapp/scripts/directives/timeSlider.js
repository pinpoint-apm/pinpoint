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
            var initSlider, getScale, parseScaleAsTimeFormat, parseTimestampToTimeFormat, setInnerFromTo;

            // initialize private variables
            $elSlider = element.find('.timeslider_input');

            // initialize scope variables
            scope.oTimeSliderDao = null;

            /**
             * init slider
             * @param timeSliderDao
             */
            initSlider = function (timeSliderDao) {
                scope.oTimeSliderDao = timeSliderDao;

                if (scope.oTimeSliderDao.getReady() === false) {
                    return;
                }

                $timeout(function () {
                    $elSlider.jslider(
                        {
                            from: scope.oTimeSliderDao.getFrom(),
                            to: scope.oTimeSliderDao.getTo(),
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
                });

            };

            /**
             * get scale
             * @returns {Array}
             */
            getScale = function () {
                // gap이 최대 3일이라 가정하고, 전부 시:분 으로 표시한다.
                var from =  scope.oTimeSliderDao.getFrom(),
                    to = scope.oTimeSliderDao.getTo(),
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
                scope.$emit('timeSlider.moreClicked', scope.oTimeSliderDao);
            };

            /**
             * scope event on timeSlider.initialize
             */
            scope.$on('timeSlider.initialize', function (event, timeSliderDao) {
                initSlider(timeSliderDao);
            });

            /**
             * scope event on setInnerFromTo
             */
            scope.$on('timeSlider.setInnerFromTo', function (event, timeSliderDao) {
                scope.oTimeSliderDao = timeSliderDao;
                setInnerFromTo(timeSliderDao.getInnerFrom(), timeSliderDao.getInnerTo());
            });
        }
    };
}]);
