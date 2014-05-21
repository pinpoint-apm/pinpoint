/**
 * angular-strap
 * @version v2.0.2 - 2014-04-27
 * @link http://mgcrea.github.io/angular-strap
 * @author Olivier Louvignes (olivier@mg-crea.com)
 * @license MIT License, http://www.opensource.org/licenses/MIT
 */
'use strict';
angular.module('mgcrea.ngStrap.datepicker', [
  'mgcrea.ngStrap.helpers.dateParser',
  'mgcrea.ngStrap.tooltip'
]).provider('$datepicker', function () {
  var defaults = this.defaults = {
      animation: 'am-fade',
      prefixClass: 'datepicker',
      placement: 'bottom-left',
      template: 'datepicker/datepicker.tpl.html',
      trigger: 'focus',
      container: false,
      keyboard: true,
      html: false,
      delay: 0,
      useNative: false,
      dateType: 'date',
      dateFormat: 'shortDate',
      dayFormat: 'dd',
      strictFormat: false,
      autoclose: false,
      minDate: -Infinity,
      maxDate: +Infinity,
      startView: 0,
      minView: 0,
      startWeek: 0
    };
  this.$get = [
    '$window',
    '$document',
    '$rootScope',
    '$sce',
    '$locale',
    'dateFilter',
    'datepickerViews',
    '$tooltip',
    function ($window, $document, $rootScope, $sce, $locale, dateFilter, datepickerViews, $tooltip) {
      var bodyEl = angular.element($window.document.body);
      var isTouch = 'createTouch' in $window.document;
      var isNative = /(ip(a|o)d|iphone|android)/gi.test($window.navigator.userAgent);
      if (!defaults.lang)
        defaults.lang = $locale.id;
      function DatepickerFactory(element, controller, config) {
        var $datepicker = $tooltip(element, angular.extend({}, defaults, config));
        var parentScope = config.scope;
        var options = $datepicker.$options;
        var scope = $datepicker.$scope;
        if (options.startView)
          options.startView -= options.minView;
        // View vars
        var pickerViews = datepickerViews($datepicker);
        $datepicker.$views = pickerViews.views;
        var viewDate = pickerViews.viewDate;
        scope.$mode = options.startView;
        var $picker = $datepicker.$views[scope.$mode];
        // Scope methods
        scope.$select = function (date) {
          $datepicker.select(date);
        };
        scope.$selectPane = function (value) {
          $datepicker.$selectPane(value);
        };
        scope.$toggleMode = function () {
          $datepicker.setMode((scope.$mode + 1) % $datepicker.$views.length);
        };
        // Public methods
        $datepicker.update = function (date) {
          // console.warn('$datepicker.update() newValue=%o', date);
          if (angular.isDate(date) && !isNaN(date.getTime())) {
            $datepicker.$date = date;
            $picker.update.call($picker, date);
          }
          // Build only if pristine
          $datepicker.$build(true);
        };
        $datepicker.select = function (date, keep) {
          // console.warn('$datepicker.select', date, scope.$mode);
          if (!angular.isDate(controller.$dateValue))
            controller.$dateValue = new Date(date);
          controller.$dateValue.setFullYear(date.getFullYear(), date.getMonth(), date.getDate());
          if (!scope.$mode || keep) {
            controller.$setViewValue(controller.$dateValue);
            controller.$render();
            if (options.autoclose && !keep) {
              $datepicker.hide(true);
            }
          } else {
            angular.extend(viewDate, {
              year: date.getFullYear(),
              month: date.getMonth(),
              date: date.getDate()
            });
            $datepicker.setMode(scope.$mode - 1);
            $datepicker.$build();
          }
        };
        $datepicker.setMode = function (mode) {
          // console.warn('$datepicker.setMode', mode);
          scope.$mode = mode;
          $picker = $datepicker.$views[scope.$mode];
          $datepicker.$build();
        };
        // Protected methods
        $datepicker.$build = function (pristine) {
          // console.warn('$datepicker.$build() viewDate=%o', viewDate);
          if (pristine === true && $picker.built)
            return;
          if (pristine === false && !$picker.built)
            return;
          $picker.build.call($picker);
        };
        $datepicker.$updateSelected = function () {
          for (var i = 0, l = scope.rows.length; i < l; i++) {
            angular.forEach(scope.rows[i], updateSelected);
          }
        };
        $datepicker.$isSelected = function (date) {
          return $picker.isSelected(date);
        };
        $datepicker.$selectPane = function (value) {
          var steps = $picker.steps;
          var targetDate = new Date(Date.UTC(viewDate.year + (steps.year || 0) * value, viewDate.month + (steps.month || 0) * value, viewDate.date + (steps.day || 0) * value));
          angular.extend(viewDate, {
            year: targetDate.getUTCFullYear(),
            month: targetDate.getUTCMonth(),
            date: targetDate.getUTCDate()
          });
          $datepicker.$build();
        };
        $datepicker.$onMouseDown = function (evt) {
          // Prevent blur on mousedown on .dropdown-menu
          evt.preventDefault();
          evt.stopPropagation();
          // Emulate click for mobile devices
          if (isTouch) {
            var targetEl = angular.element(evt.target);
            if (targetEl[0].nodeName.toLowerCase() !== 'button') {
              targetEl = targetEl.parent();
            }
            targetEl.triggerHandler('click');
          }
        };
        $datepicker.$onKeyDown = function (evt) {
          if (!/(38|37|39|40|13)/.test(evt.keyCode) || evt.shiftKey || evt.altKey)
            return;
          evt.preventDefault();
          evt.stopPropagation();
          if (evt.keyCode === 13) {
            if (!scope.$mode) {
              return $datepicker.hide(true);
            } else {
              return scope.$apply(function () {
                $datepicker.setMode(scope.$mode - 1);
              });
            }
          }
          // Navigate with keyboard
          $picker.onKeyDown(evt);
          parentScope.$digest();
        };
        // Private
        function updateSelected(el) {
          el.selected = $datepicker.$isSelected(el.date);
        }
        function focusElement() {
          element[0].focus();
        }
        // Overrides
        var _init = $datepicker.init;
        $datepicker.init = function () {
          if (isNative && options.useNative) {
            element.prop('type', 'date');
            element.css('-webkit-appearance', 'textfield');
            return;
          } else if (isTouch) {
            element.prop('type', 'text');
            element.attr('readonly', 'true');
            element.on('click', focusElement);
          }
          _init();
        };
        var _destroy = $datepicker.destroy;
        $datepicker.destroy = function () {
          if (isNative && options.useNative) {
            element.off('click', focusElement);
          }
          _destroy();
        };
        var _show = $datepicker.show;
        $datepicker.show = function () {
          _show();
          setTimeout(function () {
            $datepicker.$element.on(isTouch ? 'touchstart' : 'mousedown', $datepicker.$onMouseDown);
            if (options.keyboard) {
              element.on('keydown', $datepicker.$onKeyDown);
            }
          });
        };
        var _hide = $datepicker.hide;
        $datepicker.hide = function (blur) {
          $datepicker.$element.off(isTouch ? 'touchstart' : 'mousedown', $datepicker.$onMouseDown);
          if (options.keyboard) {
            element.off('keydown', $datepicker.$onKeyDown);
          }
          _hide(blur);
        };
        return $datepicker;
      }
      DatepickerFactory.defaults = defaults;
      return DatepickerFactory;
    }
  ];
}).directive('bsDatepicker', [
  '$window',
  '$parse',
  '$q',
  '$locale',
  'dateFilter',
  '$datepicker',
  '$dateParser',
  '$timeout',
  function ($window, $parse, $q, $locale, dateFilter, $datepicker, $dateParser, $timeout) {
    var defaults = $datepicker.defaults;
    var isNative = /(ip(a|o)d|iphone|android)/gi.test($window.navigator.userAgent);
    var isNumeric = function (n) {
      return !isNaN(parseFloat(n)) && isFinite(n);
    };
    return {
      restrict: 'EAC',
      require: 'ngModel',
      link: function postLink(scope, element, attr, controller) {
        // Directive options
        var options = {
            scope: scope,
            controller: controller
          };
        angular.forEach([
          'placement',
          'container',
          'delay',
          'trigger',
          'keyboard',
          'html',
          'animation',
          'template',
          'autoclose',
          'dateType',
          'dateFormat',
          'dayFormat',
          'strictFormat',
          'startWeek',
          'useNative',
          'lang',
          'startView',
          'minView'
        ], function (key) {
          if (angular.isDefined(attr[key]))
            options[key] = attr[key];
        });
        // Initialize datepicker
        if (isNative && options.useNative)
          options.dateFormat = 'yyyy-MM-dd';
        var datepicker = $datepicker(element, controller, options);
        options = datepicker.$options;
        // Observe attributes for changes
        angular.forEach([
          'minDate',
          'maxDate'
        ], function (key) {
          // console.warn('attr.$observe(%s)', key, attr[key]);
          angular.isDefined(attr[key]) && attr.$observe(key, function (newValue) {
            // console.warn('attr.$observe(%s)=%o', key, newValue);
            if (newValue === 'today') {
              var today = new Date();
              datepicker.$options[key] = +new Date(today.getFullYear(), today.getMonth(), today.getDate() + (key === 'maxDate' ? 1 : 0), 0, 0, 0, key === 'minDate' ? 0 : -1);
            } else if (angular.isString(newValue) && newValue.match(/^".+"$/)) {
              // Support {{ dateObj }}
              datepicker.$options[key] = +new Date(newValue.substr(1, newValue.length - 2));
            } else if (isNumeric(newValue)) {
              datepicker.$options[key] = +new Date(parseInt(newValue, 10));
            } else {
              datepicker.$options[key] = +new Date(newValue);
            }
            // Build only if dirty
            !isNaN(datepicker.$options[key]) && datepicker.$build(false);
          });
        });
        // Watch model for changes
        scope.$watch(attr.ngModel, function (newValue, oldValue) {
          datepicker.update(controller.$dateValue);
        }, true);
        var dateParser = $dateParser({
            format: options.dateFormat,
            lang: options.lang,
            strict: options.strictFormat
          });
        // viewValue -> $parsers -> modelValue
        controller.$parsers.unshift(function (viewValue) {
          // console.warn('$parser("%s"): viewValue=%o', element.attr('ng-model'), viewValue);
          // Null values should correctly reset the model value & validity
          if (!viewValue) {
            controller.$setValidity('date', true);
            return;
          }
          var parsedDate = dateParser.parse(viewValue, controller.$dateValue);
          if (!parsedDate || isNaN(parsedDate.getTime())) {
            controller.$setValidity('date', false);
            return;
          } else {
            var isValid = (isNaN(datepicker.$options.minDate) || parsedDate.getTime() >= datepicker.$options.minDate) && (isNaN(datepicker.$options.maxDate) || parsedDate.getTime() <= datepicker.$options.maxDate);
            controller.$setValidity('date', isValid);
            // Only update the model when we have a valid date
            if (isValid)
              controller.$dateValue = parsedDate;
          }
          if (options.dateType === 'string') {
            return dateFilter(viewValue, options.dateFormat);
          } else if (options.dateType === 'number') {
            return controller.$dateValue.getTime();
          } else if (options.dateType === 'iso') {
            return controller.$dateValue.toISOString();
          } else {
            return new Date(controller.$dateValue);
          }
        });
        // modelValue -> $formatters -> viewValue
        controller.$formatters.push(function (modelValue) {
          // console.warn('$formatter("%s"): modelValue=%o (%o)', element.attr('ng-model'), modelValue, typeof modelValue);
          var date;
          if (angular.isUndefined(modelValue) || modelValue === null) {
            date = NaN;
          } else if (angular.isDate(modelValue)) {
            date = modelValue;
          } else if (options.dateType === 'string') {
            date = dateParser.parse(modelValue);
          } else {
            date = new Date(modelValue);
          }
          // Setup default value?
          // if(isNaN(date.getTime())) {
          //   var today = new Date();
          //   date = new Date(today.getFullYear(), today.getMonth(), today.getDate(), 0, 0, 0, 0);
          // }
          controller.$dateValue = date;
          return controller.$dateValue;
        });
        // viewValue -> element
        controller.$render = function () {
          // console.warn('$render("%s"): viewValue=%o', element.attr('ng-model'), controller.$viewValue);
          element.val(!controller.$dateValue || isNaN(controller.$dateValue.getTime()) ? '' : dateFilter(controller.$dateValue, options.dateFormat));
        };
        // Garbage collection
        scope.$on('$destroy', function () {
          datepicker.destroy();
          options = null;
          datepicker = null;
        });
      }
    };
  }
]).provider('datepickerViews', function () {
  var defaults = this.defaults = {
      dayFormat: 'dd',
      daySplit: 7
    };
  // Split array into smaller arrays
  function split(arr, size) {
    var arrays = [];
    while (arr.length > 0) {
      arrays.push(arr.splice(0, size));
    }
    return arrays;
  }
  // Modulus operator
  function mod(n, m) {
    return (n % m + m) % m;
  }
  this.$get = [
    '$locale',
    '$sce',
    'dateFilter',
    function ($locale, $sce, dateFilter) {
      return function (picker) {
        var scope = picker.$scope;
        var options = picker.$options;
        var weekDaysMin = $locale.DATETIME_FORMATS.SHORTDAY;
        var weekDaysLabels = weekDaysMin.slice(options.startWeek).concat(weekDaysMin.slice(0, options.startWeek));
        var weekDaysLabelsHtml = $sce.trustAsHtml('<th class="dow text-center">' + weekDaysLabels.join('</th><th class="dow text-center">') + '</th>');
        var startDate = picker.$date || new Date();
        var viewDate = {
            year: startDate.getFullYear(),
            month: startDate.getMonth(),
            date: startDate.getDate()
          };
        var timezoneOffset = startDate.getTimezoneOffset() * 60000;
        var views = [
            {
              format: options.dayFormat,
              split: 7,
              steps: { month: 1 },
              update: function (date, force) {
                if (!this.built || force || date.getFullYear() !== viewDate.year || date.getMonth() !== viewDate.month) {
                  angular.extend(viewDate, {
                    year: picker.$date.getFullYear(),
                    month: picker.$date.getMonth(),
                    date: picker.$date.getDate()
                  });
                  picker.$build();
                } else if (date.getDate() !== viewDate.date) {
                  viewDate.date = picker.$date.getDate();
                  picker.$updateSelected();
                }
              },
              build: function () {
                var firstDayOfMonth = new Date(viewDate.year, viewDate.month, 1), firstDayOfMonthOffset = firstDayOfMonth.getTimezoneOffset();
                var firstDate = new Date(+firstDayOfMonth - mod(firstDayOfMonth.getDay() - options.startWeek, 7) * 86400000), firstDateOffset = firstDate.getTimezoneOffset();
                // Handle daylight time switch
                if (firstDateOffset !== firstDayOfMonthOffset)
                  firstDate = new Date(+firstDate + (firstDateOffset - firstDayOfMonthOffset) * 60000);
                var days = [], day;
                for (var i = 0; i < 42; i++) {
                  // < 7 * 6
                  day = new Date(firstDate.getFullYear(), firstDate.getMonth(), firstDate.getDate() + i);
                  days.push({
                    date: day,
                    label: dateFilter(day, this.format),
                    selected: picker.$date && this.isSelected(day),
                    muted: day.getMonth() !== viewDate.month,
                    disabled: this.isDisabled(day)
                  });
                }
                scope.title = dateFilter(firstDayOfMonth, 'MMMM yyyy');
                scope.showLabels = true;
                scope.labels = weekDaysLabelsHtml;
                scope.rows = split(days, this.split);
                this.built = true;
              },
              isSelected: function (date) {
                return picker.$date && date.getFullYear() === picker.$date.getFullYear() && date.getMonth() === picker.$date.getMonth() && date.getDate() === picker.$date.getDate();
              },
              isDisabled: function (date) {
                return date.getTime() < options.minDate || date.getTime() > options.maxDate;
              },
              onKeyDown: function (evt) {
                var actualTime = picker.$date.getTime();
                var newDate;
                if (evt.keyCode === 37)
                  newDate = new Date(actualTime - 1 * 86400000);
                else if (evt.keyCode === 38)
                  newDate = new Date(actualTime - 7 * 86400000);
                else if (evt.keyCode === 39)
                  newDate = new Date(actualTime + 1 * 86400000);
                else if (evt.keyCode === 40)
                  newDate = new Date(actualTime + 7 * 86400000);
                if (!this.isDisabled(newDate))
                  picker.select(newDate, true);
              }
            },
            {
              name: 'month',
              format: 'MMM',
              split: 4,
              steps: { year: 1 },
              update: function (date, force) {
                if (!this.built || date.getFullYear() !== viewDate.year) {
                  angular.extend(viewDate, {
                    year: picker.$date.getFullYear(),
                    month: picker.$date.getMonth(),
                    date: picker.$date.getDate()
                  });
                  picker.$build();
                } else if (date.getMonth() !== viewDate.month) {
                  angular.extend(viewDate, {
                    month: picker.$date.getMonth(),
                    date: picker.$date.getDate()
                  });
                  picker.$updateSelected();
                }
              },
              build: function () {
                var firstMonth = new Date(viewDate.year, 0, 1);
                var months = [], month;
                for (var i = 0; i < 12; i++) {
                  month = new Date(viewDate.year, i, 1);
                  months.push({
                    date: month,
                    label: dateFilter(month, this.format),
                    selected: picker.$isSelected(month),
                    disabled: this.isDisabled(month)
                  });
                }
                scope.title = dateFilter(month, 'yyyy');
                scope.showLabels = false;
                scope.rows = split(months, this.split);
                this.built = true;
              },
              isSelected: function (date) {
                return picker.$date && date.getFullYear() === picker.$date.getFullYear() && date.getMonth() === picker.$date.getMonth();
              },
              isDisabled: function (date) {
                var lastDate = +new Date(date.getFullYear(), date.getMonth() + 1, 0);
                return lastDate < options.minDate || date.getTime() > options.maxDate;
              },
              onKeyDown: function (evt) {
                var actualMonth = picker.$date.getMonth();
                var newDate = new Date(picker.$date);
                if (evt.keyCode === 37)
                  newDate.setMonth(actualMonth - 1);
                else if (evt.keyCode === 38)
                  newDate.setMonth(actualMonth - 4);
                else if (evt.keyCode === 39)
                  newDate.setMonth(actualMonth + 1);
                else if (evt.keyCode === 40)
                  newDate.setMonth(actualMonth + 4);
                if (!this.isDisabled(newDate))
                  picker.select(newDate, true);
              }
            },
            {
              name: 'year',
              format: 'yyyy',
              split: 4,
              steps: { year: 12 },
              update: function (date, force) {
                if (!this.built || force || parseInt(date.getFullYear() / 20, 10) !== parseInt(viewDate.year / 20, 10)) {
                  angular.extend(viewDate, {
                    year: picker.$date.getFullYear(),
                    month: picker.$date.getMonth(),
                    date: picker.$date.getDate()
                  });
                  picker.$build();
                } else if (date.getFullYear() !== viewDate.year) {
                  angular.extend(viewDate, {
                    year: picker.$date.getFullYear(),
                    month: picker.$date.getMonth(),
                    date: picker.$date.getDate()
                  });
                  picker.$updateSelected();
                }
              },
              build: function () {
                var firstYear = viewDate.year - viewDate.year % (this.split * 3);
                var years = [], year;
                for (var i = 0; i < 12; i++) {
                  year = new Date(firstYear + i, 0, 1);
                  years.push({
                    date: year,
                    label: dateFilter(year, this.format),
                    selected: picker.$isSelected(year),
                    disabled: this.isDisabled(year)
                  });
                }
                scope.title = years[0].label + '-' + years[years.length - 1].label;
                scope.showLabels = false;
                scope.rows = split(years, this.split);
                this.built = true;
              },
              isSelected: function (date) {
                return picker.$date && date.getFullYear() === picker.$date.getFullYear();
              },
              isDisabled: function (date) {
                var lastDate = +new Date(date.getFullYear() + 1, 0, 0);
                return lastDate < options.minDate || date.getTime() > options.maxDate;
              },
              onKeyDown: function (evt) {
                var actualYear = picker.$date.getFullYear(), newDate = new Date(picker.$date);
                if (evt.keyCode === 37)
                  newDate.setYear(actualYear - 1);
                else if (evt.keyCode === 38)
                  newDate.setYear(actualYear - 4);
                else if (evt.keyCode === 39)
                  newDate.setYear(actualYear + 1);
                else if (evt.keyCode === 40)
                  newDate.setYear(actualYear + 4);
                if (!this.isDisabled(newDate))
                  picker.select(newDate, true);
              }
            }
          ];
        return {
          views: options.minView ? Array.prototype.slice.call(views, options.minView) : views,
          viewDate: viewDate
        };
      };
    }
  ];
});