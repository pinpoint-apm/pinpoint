'use strict';

pinpointApp.constant('timeSliderConfig', {
    scaleCount: 10
});

pinpointApp.directive('timeSlider', [ 'timeSliderConfig', function (cfg) {
    return {
        restrict: 'EA',
        replace: true,
        templateUrl: 'views/timeSlider.html',
        link: function postLink(scope, element, attrs) {

            // define variables
            var $elSlider;
            // define private methods
            var initSlider, getScale, parseScaleAsTimeFormat, parseTimestampToTimeFormat, setInnerFromTo,
                disableMore, enableMore;

            // initialize private variables
            $elSlider = element.find('.timeslider_input');

            // initialize scope variables
            scope.nInnerFrom = null;
            scope.nInnerTo = null;
            scope.disabledMore = false;
            scope.timeSliderDao = null;

            /**
             * init slider
             * @param timeSliderDao
             */
            initSlider = function (timeSliderDao) {
                scope.timeSliderDao = timeSliderDao;
                if (scope.timeSliderDao === null) {
                    return;
                }

                var scale = parseScaleAsTimeFormat(getScale());

                scope.nInnerFrom = scope.timeSliderDao.getInnerFrom();
                scope.nInnerTo = scope.timeSliderDao.getInnerTo();
                scope.$digest();

                $elSlider.jslider(
                    {
                        from: scope.timeSliderDao.getFrom(),
                        to: scope.timeSliderDao.getTo(),
                        scale: scale,
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
            };

            /**
             * get scale
             * @returns {Array}
             */
            getScale = function () {
                // gap이 최대 3일이라 가정하고, 전부 시:분 으로 표시한다.
                var from =  scope.timeSliderDao.getFrom(),
                    to = scope.timeSliderDao.getTo(),
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

            setInnerFromTo = function (innerFrom, innerTo) {
                $elSlider.jslider("value", innerFrom, innerTo);
            };
            disableMore = function () {
                scope.disabledMore = true;
                scope.$digest();
            };
            enableMore = function () {
                scope.disabledMore = false;
                scope.$digest();
            };

            // define scope methods
            scope.more = function () {
                scope.$emit('timeSlider.moreClicked', scope.timeSliderDao);
            };
            scope.$on('timeSlider.initialize', function (event, timeSliderDao) {
                initSlider(timeSliderDao);
            });
            scope.$on('timeSlider.setInnerFromTo', function (event, timeSliderDao) {
                scope.timeSliderDao = timeSliderDao;
                setInnerFromTo(timeSliderDao.getInnerFrom(), timeSliderDao.getInnerTo());
            });
            scope.$on('timeSlider.disableMore', function (event) {
                disableMore();
            });
            scope.$on('timeSlider.enableMore', function (event) {
                enableMore();
            });
        }
    };
}]);
