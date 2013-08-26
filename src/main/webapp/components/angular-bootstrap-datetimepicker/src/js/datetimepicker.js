/*globals angular, moment, jQuery */
/*jslint vars:true */

/**
 * @license angular-bootstrap-datetimepicker  v0.1.2
 * (c) 2013 Knight Rider Consulting, Inc. http://www.knightrider.com
 * License: MIT
 */

/**
 *
 *    @author        Dale "Ducky" Lotts
 *    @since        2013-Jul-8
 */

angular.module('ui.bootstrap.datetimepicker', [])
    .constant('dateTimePickerConfig', {
        startView: 'day',
        minView: 'minute',
        minuteStep: 5,
        dropdownSelector: null
    })
    .constant('dateTimePickerConfigValidation', function (configuration) {

        var validOptions = ['startView', 'minView', 'minuteStep', 'dropdownSelector'];

        for(var prop in configuration) {
            if(configuration.hasOwnProperty(prop)) {
                if (validOptions.indexOf(prop) < 0) {
                    throw ("invalid option: " + prop);
                }
            }
        }

        // Order of the elements in the validViews array is significant.
        var validViews = ['minute', 'hour', 'day', 'month', 'year'];

        if (validViews.indexOf(configuration.startView) < 0) {
            throw ("invalid startView value: " + configuration.startView);
        }

        if (validViews.indexOf(configuration.minView) < 0) {
            throw ("invalid minView value: " + configuration.minView);
        }

        if (validViews.indexOf(configuration.minView) >  validViews.indexOf(configuration.startView)) {
            throw ("startView must be greater than minView");
        }

        if (!angular.isNumber(configuration.minuteStep)) {
            throw ("minuteStep must be numeric");
        }
        if (configuration.minuteStep <= 0 || configuration.minuteStep >= 60) {
            throw ("minuteStep must be greater than zero and less than 60");
        }
        if (configuration.dropdownSelector !== null && !angular.isString(configuration.dropdownSelector)) {
            throw ("dropdownSelector must be a string");
        }
    }
)
    .directive('datetimepicker', ['dateTimePickerConfig', 'dateTimePickerConfigValidation', function (defaultConfig, validateConfigurationFunction) {
        "use strict";

        return {
            restrict: 'E',
            require: 'ngModel',
            template: "<div class='datetimepicker'>" +
                "<table class='table-condensed'>" +
                "   <thead>" +
                "       <tr>" +
                "           <th class='left'><i class='icon-arrow-left'/></th>" +
                "           <th class='switch' colspan='5'>{{ data.title }}</th>" +
                "           <th class='right'><i class='icon-arrow-right'/></th>" +
                "       </tr>" +
                "       <tr>" +
                "           <th class='dow' data-ng-repeat='day in data.dayNames' >{{ day }}</th>" +
                "       </tr>" +
                "   </thead>" +
                "   <tbody>" +
                '       <tr data-ng-class=\'{ hide: data.currentView == "day" }\' >' +
                "           <td colspan='7' >" +
                "              <span    class='{{ data.currentView }}' " +
                "                       data-ng-repeat='dateValue in data.dates'  " +
                "                       data-ng-class='{active: dateValue.active, past: dateValue.past, future: dateValue.future}' " +
                "                       data-ng-click=\"changeView('{{ data.nextView }}', {{ dateValue.date }}, $event)\">{{ dateValue.display }}</span> " +
                "           </td>" +
                "       </tr>" +
                '       <tr data-ng-show=\'{{ data.currentView == "day" }}\' data-ng-repeat=\'week in data.weeks\'>' +
                "           <td data-ng-repeat='dateValue in week.dates' " +
                "               data-ng-click=\"changeView('{{ data.nextView }}', {{ dateValue.date }}, $event)\"" +
                "               class='day' " +
                "               data-ng-class='{active: dateValue.active, past: dateValue.past, future: dateValue.future}' >{{ dateValue.display }}</td>" +
                "       </tr>" +
                "   </tbody>" +
                "</table></div>",
            scope: {
                ngModel: "=ngModel"
            },
            replace: true,
            link: function (scope, element, attrs) {

                var directiveConfig = {};

                if (attrs.datetimepickerConfig) {
                    directiveConfig = scope.$eval(attrs.datetimepickerConfig);
                }

                var configuration = {};

                angular.extend(configuration, defaultConfig, directiveConfig);

                validateConfigurationFunction(configuration);

                var dataFactory = {
                    year: function (unixDate) {
                        var selectedDate = moment.utc(unixDate).startOf('year');
                        // View starts one year before the decade starts and ends one year after the decade ends
                        // i.e. passing in a date of 1/1/2013 will give a range of 2009 to 2020
                        // Truncate the last digit from the current year and subtract 1 to get the start of the decade
                        var startDecade = (parseInt(selectedDate.year() / 10, 10) * 10);
                        var startDate = moment.utc(selectedDate).year(startDecade - 1).startOf('year');
                        var activeYear = scope.ngModel ? moment(scope.ngModel).year() : 0;

                        var result = {
                            'currentView': 'year',
                            'nextView': configuration.minView === 'year' ? 'setTime' : 'month',
                            'title': startDecade + '-' + (startDecade + 9),
                            'leftDate': moment.utc(startDate).subtract(9, 'year').valueOf(),
                            'rightDate': moment.utc(startDate).add(11, 'year').valueOf(),
                            'dates': []
                        };

                        for (var i = 0; i < 12; i++) {
                            var yearMoment = moment.utc(startDate).add(i, 'years');
                            var dateValue = {
                                'date': yearMoment.valueOf(),
                                'display': yearMoment.format('YYYY'),
                                'past': yearMoment.year() < startDecade,
                                'future': yearMoment.year() > startDecade + 9,
                                'active': yearMoment.year() === activeYear
                            };

                            result.dates.push(dateValue);
                        }

                        return result;
                    },

                    month: function (unixDate) {

                        var startDate = moment.utc(unixDate).startOf('year');

                        var activeDate = scope.ngModel ? moment(scope.ngModel).format('YYYY-MMM') : 0;

                        var result = {
                            'previousView': 'year',
                            'currentView': 'month',
                            'nextView': configuration.minView === 'month' ? 'setTime' : 'day',
                            'currentDate': startDate.valueOf(),
                            'title': startDate.format('YYYY'),
                            'leftDate': moment.utc(startDate).subtract(1, 'year').valueOf(),
                            'rightDate': moment.utc(startDate).add(1, 'year').valueOf(),
                            'dates': []
                        };

                        for (var i = 0; i < 12; i++) {
                            var monthMoment = moment.utc(startDate).add(i, 'months');
                            var dateValue = {
                                'date': monthMoment.valueOf(),
                                'display': monthMoment.format('MMM'),
                                'active': monthMoment.format('YYYY-MMM') === activeDate
                            };

                            result.dates.push(dateValue);
                        }

                        return result;
                    },

                    day: function (unixDate) {

                        var selectedDate = moment.utc(unixDate);
                        var startOfMonth = moment.utc(selectedDate).startOf('month');
                        var endOfMonth = moment.utc(selectedDate).endOf('month');

                        // ToDo: Update to account for starting on days other than Sunday.
                        var startDate = moment.utc(startOfMonth).subtract(startOfMonth.day(), 'days');

                        var activeDate = scope.ngModel ? moment(scope.ngModel).format('YYYY-MMM-DD') : '';

                        var result = {
                            'previousView': 'month',
                            'currentView': 'day',
                            'nextView': configuration.minView === 'day' ? 'setTime' : 'hour',
                            'currentDate': selectedDate.valueOf(),
                            'title': selectedDate.format('YYYY-MMM'),
                            'leftDate': moment.utc(startOfMonth).subtract(1, 'months').valueOf(),
                            'rightDate': moment.utc(startOfMonth).add(1, 'months').valueOf(),
                            'dayNames': [],
                            'weeks': []
                        };

                        for (var dayNumber = 0; dayNumber < 7; dayNumber++) {
                            result.dayNames.push(moment.utc().day(dayNumber).format('dd'));
                        }

                        for (var i = 0; i < 6; i++) {
                            var week = { dates: [] };
                            for (var j = 0; j < 7; j++) {
                                var monthMoment = moment.utc(startDate).add((i * 7) + j, 'days');
                                var dateValue = {
                                    'date': monthMoment.valueOf(),
                                    'display': monthMoment.format('D'),
                                    'active': monthMoment.format('YYYY-MMM-DD') === activeDate,
                                    'past': monthMoment.isBefore(startOfMonth),
                                    'future': monthMoment.isAfter(endOfMonth)
                                };
                                week.dates.push(dateValue);
                            }
                            result.weeks.push(week);
                        }

                        return result;
                    },

                    hour: function (unixDate) {
                        var selectedDate = moment.utc(unixDate).hour(0).minute(0).second(0);

                        var activeFormat = scope.ngModel ? moment(scope.ngModel).format('YYYY-MM-DD H') : '';

                        var result = {
                            'previousView': 'day',
                            'currentView': 'hour',
                            'nextView': configuration.minView === 'hour' ? 'setTime' : 'minute',
                            'currentDate': selectedDate.valueOf(),
                            'title': selectedDate.format('YYYY-MMM-DD'),
                            'leftDate': moment.utc(selectedDate).subtract(1, 'days').valueOf(),
                            'rightDate': moment.utc(selectedDate).add(1, 'days').valueOf(),
                            'dates': []
                        };

                        for (var i = 0; i < 24; i++) {
                            var hourMoment = moment.utc(selectedDate).add(i, 'hours');
                            var dateValue = {
                                'date': hourMoment.valueOf(),
                                'display': hourMoment.format('H:00'),
                                'active': hourMoment.format('YYYY-MM-DD H') === activeFormat
                            };

                            result.dates.push(dateValue);
                        }

                        return result;
                    },

                    minute: function (unixDate) {
                        var selectedDate = moment.utc(unixDate).minute(0).second(0);

                        var activeFormat = scope.ngModel ? moment(scope.ngModel).format('YYYY-MM-DD H:mm') : '';

                        var result = {
                            'previousView': 'hour',
                            'currentView': 'minute',
                            'nextView': 'setTime',
                            'currentDate': selectedDate.valueOf(),
                            'title': selectedDate.format('YYYY-MMM-DD H:mm'),
                            'leftDate': moment.utc(selectedDate).subtract(1, 'hours').valueOf(),
                            'rightDate': moment.utc(selectedDate).add(1, 'hours').valueOf(),
                            'dates': []
                        };

                        var limit = 60 / configuration.minuteStep;

                        for (var i = 0; i < limit; i++) {
                            var hourMoment = moment.utc(selectedDate).add(i * configuration.minuteStep, 'minute');
                            var dateValue = {
                                'date': hourMoment.valueOf(),
                                'display': hourMoment.format('H:mm'),
                                'active': hourMoment.format('YYYY-MM-DD H:mm') === activeFormat
                            };

                            result.dates.push(dateValue);
                        }

                        return result;
                    },

                    setTime: function (unixDate) {
                        var tempDate = new Date(unixDate);
                        scope.ngModel = new Date(tempDate.getTime() + (tempDate.getTimezoneOffset() * 60000));
                        if (configuration.dropdownSelector) {
                            jQuery(configuration.dropdownSelector).dropdown('toggle');
                        }
                        return dataFactory[scope.data.currentView](unixDate);
                    }
                };

                var headerClickHandler = function (event) {
                    scope.changeView(scope.data[event.data.viewProperty], scope.data[event.data.dateProperty], event);
                    scope.$apply();
                };

                // Work around issue where angular does not compile, fire, or update values ng-click events on the elements in the table header
                $('.left', element).on('click', { viewProperty: 'currentView', dateProperty: 'leftDate'}, headerClickHandler);
                $('.switch', element).on('click', { viewProperty: 'previousView', dateProperty: 'currentDate'}, headerClickHandler);
                $('.right', element).on('click', { viewProperty: 'currentView', dateProperty: 'rightDate'}, headerClickHandler);

                var getUTCTime = function () {
                    var tempDate = (scope.ngModel ? moment(scope.ngModel).toDate() : new Date());
                    return tempDate.getTime() - (tempDate.getTimezoneOffset() * 60000);
                };

                scope.changeView = function (viewName, unixDate, event) {
                    if (event) {
                        event.stopPropagation();
                        event.preventDefault();
                    }

                    if (viewName && unixDate && dataFactory[viewName]) {
                        scope.data = dataFactory[viewName](unixDate);
                    }
                };

                scope.$watch('ngModel', function () {
                    scope.changeView(scope.data.currentView, getUTCTime());
                });

                scope.changeView(configuration.startView, getUTCTime());
            }
        };
    }]);
