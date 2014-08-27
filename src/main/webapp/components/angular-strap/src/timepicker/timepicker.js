'use strict';

angular.module('mgcrea.ngStrap.timepicker', ['mgcrea.ngStrap.helpers.dateParser', 'mgcrea.ngStrap.tooltip'])

  .provider('$timepicker', function() {

    var defaults = this.defaults = {
      animation: 'am-fade',
      prefixClass: 'timepicker',
      placement: 'bottom-left',
      template: 'timepicker/timepicker.tpl.html',
      trigger: 'focus',
      container: false,
      keyboard: true,
      html: false,
      delay: 0,
      // lang: $locale.id,
      useNative: true,
      timeType: 'date',
      timeFormat: 'shortTime',
      autoclose: false,
      minTime: -Infinity,
      maxTime: +Infinity,
      length: 5,
      hourStep: 1,
      minuteStep: 5
    };

    this.$get = function($window, $document, $rootScope, $sce, $locale, dateFilter, $tooltip) {

      var bodyEl = angular.element($window.document.body);
      var isTouch = 'createTouch' in $window.document;
      var isNative = /(ip(a|o)d|iphone|android)/ig.test($window.navigator.userAgent);
      if(!defaults.lang) defaults.lang = $locale.id;

      function timepickerFactory(element, controller, config) {

        var $timepicker = $tooltip(element, angular.extend({}, defaults, config));
        var parentScope = config.scope;
        var options = $timepicker.$options;
        var scope = $timepicker.$scope;

        // View vars
        var selectedIndex = 0;
        var startDate = controller.$dateValue || new Date();
        var viewDate = {hour: startDate.getHours(), meridian: startDate.getHours() < 12, minute: startDate.getMinutes(), second: startDate.getSeconds(), millisecond: startDate.getMilliseconds()};

        var format = $locale.DATETIME_FORMATS[options.timeFormat] || options.timeFormat;
        var formats = /(h+)[:]?(m+)[ ]?(a?)/i.exec(format).slice(1);
        // Scope methods

        scope.$select = function(date, index) {
          $timepicker.select(date, index);
        };
        scope.$moveIndex = function(value, index) {
          $timepicker.$moveIndex(value, index);
        };
        scope.$switchMeridian = function(date) {
          $timepicker.switchMeridian(date);
        };

        // Public methods

        $timepicker.update = function(date) {
          // console.warn('$timepicker.update() newValue=%o', date);
          if(angular.isDate(date) && !isNaN(date.getTime())) {
            $timepicker.$date = date;
            angular.extend(viewDate, {hour: date.getHours(), minute: date.getMinutes(), second: date.getSeconds(), millisecond: date.getMilliseconds()});
            $timepicker.$build();
          } else if(!$timepicker.$isBuilt) {
            $timepicker.$build();
          }
        };

        $timepicker.select = function(date, index, keep) {
          // console.warn('$timepicker.select', date, scope.$mode);
          if(!controller.$dateValue || isNaN(controller.$dateValue.getTime())) controller.$dateValue = new Date(1970, 0, 1);
          if(!angular.isDate(date)) date = new Date(date);
          if(index === 0) controller.$dateValue.setHours(date.getHours());
          else if(index === 1) controller.$dateValue.setMinutes(date.getMinutes());
          controller.$setViewValue(controller.$dateValue);
          controller.$render();
          if(options.autoclose && !keep) {
            $timepicker.hide(true);
          }
        };

        $timepicker.switchMeridian = function(date) {
          var hours = (date || controller.$dateValue).getHours();
          controller.$dateValue.setHours(hours < 12 ? hours + 12 : hours - 12);
          controller.$setViewValue(controller.$dateValue);
          controller.$render();
        };

        // Protected methods

        $timepicker.$build = function() {
          // console.warn('$timepicker.$build() viewDate=%o', viewDate);
          var i, midIndex = scope.midIndex = parseInt(options.length / 2, 10);
          var hours = [], hour;
          for(i = 0; i < options.length; i++) {
            hour = new Date(1970, 0, 1, viewDate.hour - (midIndex - i) * options.hourStep);
            hours.push({date: hour, label: dateFilter(hour, formats[0]), selected: $timepicker.$date && $timepicker.$isSelected(hour, 0), disabled: $timepicker.$isDisabled(hour, 0)});
          }
          var minutes = [], minute;
          for(i = 0; i < options.length; i++) {
            minute = new Date(1970, 0, 1, 0, viewDate.minute - (midIndex - i) * options.minuteStep);
            minutes.push({date: minute, label: dateFilter(minute, formats[1]), selected: $timepicker.$date && $timepicker.$isSelected(minute, 1), disabled: $timepicker.$isDisabled(minute, 1)});
          }

          var rows = [];
          for(i = 0; i < options.length; i++) {
            rows.push([hours[i], minutes[i]]);
          }
          scope.rows = rows;
          scope.showAM = !!formats[2];
          scope.isAM = ($timepicker.$date || hours[midIndex].date).getHours() < 12;
          $timepicker.$isBuilt = true;
        };

        $timepicker.$isSelected = function(date, index) {
          if(!$timepicker.$date) return false;
          else if(index === 0) {
            return date.getHours() === $timepicker.$date.getHours();
          } else if(index === 1) {
            return date.getMinutes() === $timepicker.$date.getMinutes();
          }
        };

        $timepicker.$isDisabled = function(date, index) {
          var selectedTime;
          if(index === 0) {
            selectedTime = date.getTime() + viewDate.minute * 6e4;
          } else if(index === 1) {
            selectedTime = date.getTime() + viewDate.hour * 36e5;
          }
          return selectedTime < options.minTime * 1 || selectedTime > options.maxTime * 1;
        };

        $timepicker.$moveIndex = function(value, index) {
          var targetDate;
          if(index === 0) {
            targetDate = new Date(1970, 0, 1, viewDate.hour + (value * options.length), viewDate.minute);
            angular.extend(viewDate, {hour: targetDate.getHours()});
          } else if(index === 1) {
            targetDate = new Date(1970, 0, 1, viewDate.hour, viewDate.minute + (value * options.length * options.minuteStep));
            angular.extend(viewDate, {minute: targetDate.getMinutes()});
          }
          $timepicker.$build();
        };

        $timepicker.$onMouseDown = function(evt) {
          // Prevent blur on mousedown on .dropdown-menu
          if(evt.target.nodeName.toLowerCase() !== 'input') evt.preventDefault();
          evt.stopPropagation();
          // Emulate click for mobile devices
          if(isTouch) {
            var targetEl = angular.element(evt.target);
            if(targetEl[0].nodeName.toLowerCase() !== 'button') {
              targetEl = targetEl.parent();
            }
            targetEl.triggerHandler('click');
          }
        };

        $timepicker.$onKeyDown = function(evt) {
          if (!/(38|37|39|40|13)/.test(evt.keyCode) || evt.shiftKey || evt.altKey) return;
          evt.preventDefault();
          evt.stopPropagation();

          // Close on enter
          if(evt.keyCode === 13) return $timepicker.hide(true);

          // Navigate with keyboard
          var newDate = new Date($timepicker.$date);
          var hours = newDate.getHours(), hoursLength = dateFilter(newDate, 'h').length;
          var minutes = newDate.getMinutes(), minutesLength = dateFilter(newDate, 'mm').length;
          var lateralMove = /(37|39)/.test(evt.keyCode);
          var count = 2 + !!formats[2] * 1;

          // Navigate indexes (left, right)
          if (lateralMove) {
            if(evt.keyCode === 37) selectedIndex = selectedIndex < 1 ? count - 1 : selectedIndex - 1;
            else if(evt.keyCode === 39) selectedIndex = selectedIndex < count - 1 ? selectedIndex + 1 : 0;
          }

          // Update values (up, down)
          var selectRange = [0, hoursLength];
          if(selectedIndex === 0) {
            if(evt.keyCode === 38) newDate.setHours(hours - parseInt(options.hourStep, 10));
            else if(evt.keyCode === 40) newDate.setHours(hours + parseInt(options.hourStep, 10));
            selectRange = [0, hoursLength];
          } else if(selectedIndex === 1) {
            if(evt.keyCode === 38) newDate.setMinutes(minutes - parseInt(options.minuteStep, 10));
            else if(evt.keyCode === 40) newDate.setMinutes(minutes + parseInt(options.minuteStep, 10));
            selectRange = [hoursLength + 1, hoursLength + 1 + minutesLength];
          } else if(selectedIndex === 2) {
            if(!lateralMove) $timepicker.switchMeridian();
            selectRange = [hoursLength + 1 + minutesLength + 1, hoursLength + 1 + minutesLength + 3];
          }
          $timepicker.select(newDate, selectedIndex, true);
          createSelection(selectRange[0], selectRange[1]);
          parentScope.$digest();
        };

        // Private

        function createSelection(start, end) {
          if(element[0].createTextRange) {
            var selRange = element[0].createTextRange();
            selRange.collapse(true);
            selRange.moveStart('character', start);
            selRange.moveEnd('character', end);
            selRange.select();
          } else if(element[0].setSelectionRange) {
            element[0].setSelectionRange(start, end);
          } else if(angular.isUndefined(element[0].selectionStart)) {
            element[0].selectionStart = start;
            element[0].selectionEnd = end;
          }
        }

        function focusElement() {
          element[0].focus();
        }

        // Overrides

        var _init = $timepicker.init;
        $timepicker.init = function() {
          if(isNative && options.useNative) {
            element.prop('type', 'time');
            element.css('-webkit-appearance', 'textfield');
            return;
          } else if(isTouch) {
            element.prop('type', 'text');
            element.attr('readonly', 'true');
            element.on('click', focusElement);
          }
          _init();
        };

        var _destroy = $timepicker.destroy;
        $timepicker.destroy = function() {
          if(isNative && options.useNative) {
            element.off('click', focusElement);
          }
          _destroy();
        };

        var _show = $timepicker.show;
        $timepicker.show = function() {
          _show();
          setTimeout(function() {
            $timepicker.$element.on(isTouch ? 'touchstart' : 'mousedown', $timepicker.$onMouseDown);
            if(options.keyboard) {
              element.on('keydown', $timepicker.$onKeyDown);
            }
          });
        };

        var _hide = $timepicker.hide;
        $timepicker.hide = function(blur) {
          $timepicker.$element.off(isTouch ? 'touchstart' : 'mousedown', $timepicker.$onMouseDown);
          if(options.keyboard) {
            element.off('keydown', $timepicker.$onKeyDown);
          }
          _hide(blur);
        };

        return $timepicker;

      }

      timepickerFactory.defaults = defaults;
      return timepickerFactory;

    };

  })


  .directive('bsTimepicker', function($window, $parse, $q, $locale, dateFilter, $timepicker, $dateParser, $timeout) {

    var defaults = $timepicker.defaults;
    var isNative = /(ip(a|o)d|iphone|android)/ig.test($window.navigator.userAgent);
    var requestAnimationFrame = $window.requestAnimationFrame || $window.setTimeout;

    return {
      restrict: 'EAC',
      require: 'ngModel',
      link: function postLink(scope, element, attr, controller) {

        // Directive options
        var options = {scope: scope, controller: controller};
        angular.forEach(['placement', 'container', 'delay', 'trigger', 'keyboard', 'html', 'animation', 'template', 'autoclose', 'timeType', 'timeFormat', 'useNative', 'hourStep', 'minuteStep', 'length'], function(key) {
          if(angular.isDefined(attr[key])) options[key] = attr[key];
        });

        // Initialize timepicker
        if(isNative && (options.useNative || defaults.useNative)) options.timeFormat = 'HH:mm';
        var timepicker = $timepicker(element, controller, options);
        options = timepicker.$options;

        // Initialize parser
        var dateParser = $dateParser({format: options.timeFormat, lang: options.lang});

        // Observe attributes for changes
        angular.forEach(['minTime', 'maxTime'], function(key) {
          // console.warn('attr.$observe(%s)', key, attr[key]);
          angular.isDefined(attr[key]) && attr.$observe(key, function(newValue) {
            if(newValue === 'now') {
              timepicker.$options[key] = new Date().setFullYear(1970, 0, 1);
            } else if(angular.isString(newValue) && newValue.match(/^".+"$/)) {
              timepicker.$options[key] = +new Date(newValue.substr(1, newValue.length - 2));
            } else {
              timepicker.$options[key] = dateParser.parse(newValue, new Date(1970, 0, 1, 0));
            }
            !isNaN(timepicker.$options[key]) && timepicker.$build();
          });
        });

        // Watch model for changes
        scope.$watch(attr.ngModel, function(newValue, oldValue) {
          // console.warn('scope.$watch(%s)', attr.ngModel, newValue, oldValue, controller.$dateValue);
          timepicker.update(controller.$dateValue);
        }, true);

        // viewValue -> $parsers -> modelValue
        controller.$parsers.unshift(function(viewValue) {
          // console.warn('$parser("%s"): viewValue=%o', element.attr('ng-model'), viewValue);
          // Null values should correctly reset the model value & validity
          if(!viewValue) {
            controller.$setValidity('date', true);
            return;
          }
          var parsedTime = dateParser.parse(viewValue, controller.$dateValue);
          if(!parsedTime || isNaN(parsedTime.getTime())) {
            controller.$setValidity('date', false);
          } else {
            var isValid = parsedTime.getTime() >= options.minTime && parsedTime.getTime() <= options.maxTime;
            controller.$setValidity('date', isValid);
            // Only update the model when we have a valid date
            if(isValid) controller.$dateValue = parsedTime;
          }
          if(options.timeType === 'string') {
            return dateFilter(viewValue, options.timeFormat);
          } else if(options.timeType === 'number') {
            return controller.$dateValue.getTime();
          } else if(options.timeType === 'iso') {
            return controller.$dateValue.toISOString();
          } else {
            return new Date(controller.$dateValue);
          }
        });

        // modelValue -> $formatters -> viewValue
        controller.$formatters.push(function(modelValue) {
          // console.warn('$formatter("%s"): modelValue=%o (%o)', element.attr('ng-model'), modelValue, typeof modelValue);
          var date;
          if(angular.isUndefined(modelValue) || modelValue === null) {
            date = NaN;
          } else if(angular.isDate(modelValue)) {
            date = modelValue;
          } else if(options.timeType === 'string') {
            date = dateParser.parse(modelValue);
          } else {
            date = new Date(modelValue);
          }
          // Setup default value?
          // if(isNaN(date.getTime())) date = new Date(new Date().setMinutes(0) + 36e5);
          controller.$dateValue = date;
          return controller.$dateValue;
        });

        // viewValue -> element
        controller.$render = function() {
          // console.warn('$render("%s"): viewValue=%o', element.attr('ng-model'), controller.$viewValue);
          element.val(!controller.$dateValue || isNaN(controller.$dateValue.getTime()) ? '' : dateFilter(controller.$dateValue, options.timeFormat));
        };

        // Garbage collection
        scope.$on('$destroy', function() {
          timepicker.destroy();
          options = null;
          timepicker = null;
        });

      }
    };

  });
