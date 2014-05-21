/**
 * angular-strap
 * @version v2.0.2 - 2014-04-27
 * @link http://mgcrea.github.io/angular-strap
 * @author Olivier Louvignes (olivier@mg-crea.com)
 * @license MIT License, http://www.opensource.org/licenses/MIT
 */
(function(window, document, undefined) {
'use strict';
// Source: module.js
angular.module('mgcrea.ngStrap', [
  'mgcrea.ngStrap.modal',
  'mgcrea.ngStrap.aside',
  'mgcrea.ngStrap.alert',
  'mgcrea.ngStrap.button',
  'mgcrea.ngStrap.select',
  'mgcrea.ngStrap.datepicker',
  'mgcrea.ngStrap.timepicker',
  'mgcrea.ngStrap.navbar',
  'mgcrea.ngStrap.tooltip',
  'mgcrea.ngStrap.popover',
  'mgcrea.ngStrap.dropdown',
  'mgcrea.ngStrap.typeahead',
  'mgcrea.ngStrap.scrollspy',
  'mgcrea.ngStrap.affix',
  'mgcrea.ngStrap.tab'
]);

// Source: affix.js
angular.module('mgcrea.ngStrap.affix', [
  'mgcrea.ngStrap.helpers.dimensions',
  'mgcrea.ngStrap.helpers.debounce'
]).provider('$affix', function () {
  var defaults = this.defaults = { offsetTop: 'auto' };
  this.$get = [
    '$window',
    'debounce',
    'dimensions',
    function ($window, debounce, dimensions) {
      var bodyEl = angular.element($window.document.body);
      var windowEl = angular.element($window);
      function AffixFactory(element, config) {
        var $affix = {};
        // Common vars
        var options = angular.extend({}, defaults, config);
        var targetEl = options.target;
        // Initial private vars
        var reset = 'affix affix-top affix-bottom', initialAffixTop = 0, initialOffsetTop = 0, offsetTop = 0, offsetBottom = 0, affixed = null, unpin = null;
        var parent = element.parent();
        // Options: custom parent
        if (options.offsetParent) {
          if (options.offsetParent.match(/^\d+$/)) {
            for (var i = 0; i < options.offsetParent * 1 - 1; i++) {
              parent = parent.parent();
            }
          } else {
            parent = angular.element(options.offsetParent);
          }
        }
        $affix.init = function () {
          $affix.$parseOffsets();
          initialOffsetTop = dimensions.offset(element[0]).top + initialAffixTop;
          // Bind events
          targetEl.on('scroll', $affix.checkPosition);
          targetEl.on('click', $affix.checkPositionWithEventLoop);
          windowEl.on('resize', $affix.$debouncedOnResize);
          // Both of these checkPosition() calls are necessary for the case where
          // the user hits refresh after scrolling to the bottom of the page.
          $affix.checkPosition();
          $affix.checkPositionWithEventLoop();
        };
        $affix.destroy = function () {
          // Unbind events
          targetEl.off('scroll', $affix.checkPosition);
          targetEl.off('click', $affix.checkPositionWithEventLoop);
          windowEl.off('resize', $affix.$debouncedOnResize);
        };
        $affix.checkPositionWithEventLoop = function () {
          setTimeout($affix.checkPosition, 1);
        };
        $affix.checkPosition = function () {
          // if (!this.$element.is(':visible')) return
          var scrollTop = getScrollTop();
          var position = dimensions.offset(element[0]);
          var elementHeight = dimensions.height(element[0]);
          // Get required affix class according to position
          var affix = getRequiredAffixClass(unpin, position, elementHeight);
          // Did affix status changed this last check?
          if (affixed === affix)
            return;
          affixed = affix;
          // Add proper affix class
          element.removeClass(reset).addClass('affix' + (affix !== 'middle' ? '-' + affix : ''));
          if (affix === 'top') {
            unpin = null;
            element.css('position', options.offsetParent ? '' : 'relative');
            element.css('top', '');
          } else if (affix === 'bottom') {
            if (options.offsetUnpin) {
              unpin = -(options.offsetUnpin * 1);
            } else {
              // Calculate unpin threshold when affixed to bottom.
              // Hopefully the browser scrolls pixel by pixel.
              unpin = position.top - scrollTop;
            }
            element.css('position', options.offsetParent ? '' : 'relative');
            element.css('top', options.offsetParent ? '' : bodyEl[0].offsetHeight - offsetBottom - elementHeight - initialOffsetTop + 'px');
          } else {
            // affix === 'middle'
            unpin = null;
            element.css('position', 'fixed');
            element.css('top', initialAffixTop + 'px');
          }
        };
        $affix.$onResize = function () {
          $affix.$parseOffsets();
          $affix.checkPosition();
        };
        $affix.$debouncedOnResize = debounce($affix.$onResize, 50);
        $affix.$parseOffsets = function () {
          // Reset position to calculate correct offsetTop
          element.css('position', options.offsetParent ? '' : 'relative');
          if (options.offsetTop) {
            if (options.offsetTop === 'auto') {
              options.offsetTop = '+0';
            }
            if (options.offsetTop.match(/^[-+]\d+$/)) {
              initialAffixTop = -options.offsetTop * 1;
              if (options.offsetParent) {
                offsetTop = dimensions.offset(parent[0]).top + options.offsetTop * 1;
              } else {
                offsetTop = dimensions.offset(element[0]).top - dimensions.css(element[0], 'marginTop', true) + options.offsetTop * 1;
              }
            } else {
              offsetTop = options.offsetTop * 1;
            }
          }
          if (options.offsetBottom) {
            if (options.offsetParent && options.offsetBottom.match(/^[-+]\d+$/)) {
              // add 1 pixel due to rounding problems...
              offsetBottom = getScrollHeight() - (dimensions.offset(parent[0]).top + dimensions.height(parent[0])) + options.offsetBottom * 1 + 1;
            } else {
              offsetBottom = options.offsetBottom * 1;
            }
          }
        };
        // Private methods
        function getRequiredAffixClass(unpin, position, elementHeight) {
          var scrollTop = getScrollTop();
          var scrollHeight = getScrollHeight();
          if (scrollTop <= offsetTop) {
            return 'top';
          } else if (unpin !== null && scrollTop + unpin <= position.top) {
            return 'middle';
          } else if (offsetBottom !== null && position.top + elementHeight + initialAffixTop >= scrollHeight - offsetBottom) {
            return 'bottom';
          } else {
            return 'middle';
          }
        }
        function getScrollTop() {
          return targetEl[0] === $window ? $window.pageYOffset : targetEl[0] === $window;
        }
        function getScrollHeight() {
          return targetEl[0] === $window ? $window.document.body.scrollHeight : targetEl[0].scrollHeight;
        }
        $affix.init();
        return $affix;
      }
      return AffixFactory;
    }
  ];
}).directive('bsAffix', [
  '$affix',
  '$window',
  function ($affix, $window) {
    return {
      restrict: 'EAC',
      require: '^?bsAffixTarget',
      link: function postLink(scope, element, attr, affixTarget) {
        var options = {
            scope: scope,
            offsetTop: 'auto',
            target: affixTarget ? affixTarget.$element : angular.element($window)
          };
        angular.forEach([
          'offsetTop',
          'offsetBottom',
          'offsetParent',
          'offsetUnpin'
        ], function (key) {
          if (angular.isDefined(attr[key]))
            options[key] = attr[key];
        });
        var affix = $affix(element, options);
        scope.$on('$destroy', function () {
          options = null;
          affix = null;
        });
      }
    };
  }
]).directive('bsAffixTarget', function () {
  return {
    controller: [
      '$element',
      function ($element) {
        this.$element = $element;
      }
    ]
  };
});

// Source: alert.js
// @BUG: following snippet won't compile correctly
// @TODO: submit issue to core
// '<span ng-if="title"><strong ng-bind="title"></strong>&nbsp;</span><span ng-bind-html="content"></span>' +
angular.module('mgcrea.ngStrap.alert', ['mgcrea.ngStrap.modal']).provider('$alert', function () {
  var defaults = this.defaults = {
      animation: 'am-fade',
      prefixClass: 'alert',
      placement: null,
      template: 'alert/alert.tpl.html',
      container: false,
      element: null,
      backdrop: false,
      keyboard: true,
      show: true,
      duration: false,
      type: false
    };
  this.$get = [
    '$modal',
    '$timeout',
    function ($modal, $timeout) {
      function AlertFactory(config) {
        var $alert = {};
        // Common vars
        var options = angular.extend({}, defaults, config);
        $alert = $modal(options);
        // Support scope as string options [/*title, content, */type]
        if (options.type) {
          $alert.$scope.type = options.type;
        }
        // Support auto-close duration
        var show = $alert.show;
        if (options.duration) {
          $alert.show = function () {
            show();
            $timeout(function () {
              $alert.hide();
            }, options.duration * 1000);
          };
        }
        return $alert;
      }
      return AlertFactory;
    }
  ];
}).directive('bsAlert', [
  '$window',
  '$location',
  '$sce',
  '$alert',
  function ($window, $location, $sce, $alert) {
    var requestAnimationFrame = $window.requestAnimationFrame || $window.setTimeout;
    return {
      restrict: 'EAC',
      scope: true,
      link: function postLink(scope, element, attr, transclusion) {
        // Directive options
        var options = {
            scope: scope,
            element: element,
            show: false
          };
        angular.forEach([
          'template',
          'placement',
          'keyboard',
          'html',
          'container',
          'animation',
          'duration'
        ], function (key) {
          if (angular.isDefined(attr[key]))
            options[key] = attr[key];
        });
        // Support scope as data-attrs
        angular.forEach([
          'title',
          'content',
          'type'
        ], function (key) {
          attr[key] && attr.$observe(key, function (newValue, oldValue) {
            scope[key] = $sce.trustAsHtml(newValue);
          });
        });
        // Support scope as an object
        attr.bsAlert && scope.$watch(attr.bsAlert, function (newValue, oldValue) {
          if (angular.isObject(newValue)) {
            angular.extend(scope, newValue);
          } else {
            scope.content = newValue;
          }
        }, true);
        // Initialize alert
        var alert = $alert(options);
        // Trigger
        element.on(attr.trigger || 'click', alert.toggle);
        // Garbage collection
        scope.$on('$destroy', function () {
          alert.destroy();
          options = null;
          alert = null;
        });
      }
    };
  }
]);

// Source: aside.js
angular.module('mgcrea.ngStrap.aside', ['mgcrea.ngStrap.modal']).provider('$aside', function () {
  var defaults = this.defaults = {
      animation: 'am-fade-and-slide-right',
      prefixClass: 'aside',
      placement: 'right',
      template: 'aside/aside.tpl.html',
      contentTemplate: false,
      container: false,
      element: null,
      backdrop: true,
      keyboard: true,
      html: false,
      show: true
    };
  this.$get = [
    '$modal',
    function ($modal) {
      function AsideFactory(config) {
        var $aside = {};
        // Common vars
        var options = angular.extend({}, defaults, config);
        $aside = $modal(options);
        return $aside;
      }
      return AsideFactory;
    }
  ];
}).directive('bsAside', [
  '$window',
  '$sce',
  '$aside',
  function ($window, $sce, $aside) {
    var requestAnimationFrame = $window.requestAnimationFrame || $window.setTimeout;
    return {
      restrict: 'EAC',
      scope: true,
      link: function postLink(scope, element, attr, transclusion) {
        // Directive options
        var options = {
            scope: scope,
            element: element,
            show: false
          };
        angular.forEach([
          'template',
          'contentTemplate',
          'placement',
          'backdrop',
          'keyboard',
          'html',
          'container',
          'animation'
        ], function (key) {
          if (angular.isDefined(attr[key]))
            options[key] = attr[key];
        });
        // Support scope as data-attrs
        angular.forEach([
          'title',
          'content'
        ], function (key) {
          attr[key] && attr.$observe(key, function (newValue, oldValue) {
            scope[key] = $sce.trustAsHtml(newValue);
          });
        });
        // Support scope as an object
        attr.bsAside && scope.$watch(attr.bsAside, function (newValue, oldValue) {
          if (angular.isObject(newValue)) {
            angular.extend(scope, newValue);
          } else {
            scope.content = newValue;
          }
        }, true);
        // Initialize aside
        var aside = $aside(options);
        // Trigger
        element.on(attr.trigger || 'click', aside.toggle);
        // Garbage collection
        scope.$on('$destroy', function () {
          aside.destroy();
          options = null;
          aside = null;
        });
      }
    };
  }
]);

// Source: button.js
angular.module('mgcrea.ngStrap.button', []).provider('$button', function () {
  var defaults = this.defaults = {
      activeClass: 'active',
      toggleEvent: 'click'
    };
  this.$get = function () {
    return { defaults: defaults };
  };
}).directive('bsCheckboxGroup', function () {
  return {
    restrict: 'A',
    require: 'ngModel',
    compile: function postLink(element, attr) {
      element.attr('data-toggle', 'buttons');
      element.removeAttr('ng-model');
      var children = element[0].querySelectorAll('input[type="checkbox"]');
      angular.forEach(children, function (child) {
        var childEl = angular.element(child);
        childEl.attr('bs-checkbox', '');
        childEl.attr('ng-model', attr.ngModel + '.' + childEl.attr('value'));
      });
    }
  };
}).directive('bsCheckbox', [
  '$button',
  '$$rAF',
  function ($button, $$rAF) {
    var defaults = $button.defaults;
    var constantValueRegExp = /^(true|false|\d+)$/;
    return {
      restrict: 'A',
      require: 'ngModel',
      link: function postLink(scope, element, attr, controller) {
        var options = defaults;
        // Support label > input[type="checkbox"]
        var isInput = element[0].nodeName === 'INPUT';
        var activeElement = isInput ? element.parent() : element;
        var trueValue = angular.isDefined(attr.trueValue) ? attr.trueValue : true;
        if (constantValueRegExp.test(attr.trueValue)) {
          trueValue = scope.$eval(attr.trueValue);
        }
        var falseValue = angular.isDefined(attr.falseValue) ? attr.falseValue : false;
        if (constantValueRegExp.test(attr.falseValue)) {
          falseValue = scope.$eval(attr.falseValue);
        }
        // Parse exotic values
        var hasExoticValues = typeof trueValue !== 'boolean' || typeof falseValue !== 'boolean';
        if (hasExoticValues) {
          controller.$parsers.push(function (viewValue) {
            // console.warn('$parser', element.attr('ng-model'), 'viewValue', viewValue);
            return viewValue ? trueValue : falseValue;
          });
          // Fix rendering for exotic values
          scope.$watch(attr.ngModel, function (newValue, oldValue) {
            controller.$render();
          });
        }
        // model -> view
        controller.$render = function () {
          // console.warn('$render', element.attr('ng-model'), 'controller.$modelValue', typeof controller.$modelValue, controller.$modelValue, 'controller.$viewValue', typeof controller.$viewValue, controller.$viewValue);
          var isActive = angular.equals(controller.$modelValue, trueValue);
          $$rAF(function () {
            if (isInput)
              element[0].checked = isActive;
            activeElement.toggleClass(options.activeClass, isActive);
          });
        };
        // view -> model
        element.bind(options.toggleEvent, function () {
          scope.$apply(function () {
            // console.warn('!click', element.attr('ng-model'), 'controller.$viewValue', typeof controller.$viewValue, controller.$viewValue, 'controller.$modelValue', typeof controller.$modelValue, controller.$modelValue);
            if (!isInput) {
              controller.$setViewValue(!activeElement.hasClass('active'));
            }
            if (!hasExoticValues) {
              controller.$render();
            }
          });
        });
      }
    };
  }
]).directive('bsRadioGroup', function () {
  return {
    restrict: 'A',
    require: 'ngModel',
    compile: function postLink(element, attr) {
      element.attr('data-toggle', 'buttons');
      element.removeAttr('ng-model');
      var children = element[0].querySelectorAll('input[type="radio"]');
      angular.forEach(children, function (child) {
        angular.element(child).attr('bs-radio', '');
        angular.element(child).attr('ng-model', attr.ngModel);
      });
    }
  };
}).directive('bsRadio', [
  '$button',
  '$$rAF',
  function ($button, $$rAF) {
    var defaults = $button.defaults;
    var constantValueRegExp = /^(true|false|\d+)$/;
    return {
      restrict: 'A',
      require: 'ngModel',
      link: function postLink(scope, element, attr, controller) {
        var options = defaults;
        // Support `label > input[type="radio"]` markup
        var isInput = element[0].nodeName === 'INPUT';
        var activeElement = isInput ? element.parent() : element;
        var value = constantValueRegExp.test(attr.value) ? scope.$eval(attr.value) : attr.value;
        // model -> view
        controller.$render = function () {
          // console.warn('$render', element.attr('value'), 'controller.$modelValue', typeof controller.$modelValue, controller.$modelValue, 'controller.$viewValue', typeof controller.$viewValue, controller.$viewValue);
          var isActive = angular.equals(controller.$modelValue, value);
          $$rAF(function () {
            if (isInput)
              element[0].checked = isActive;
            activeElement.toggleClass(options.activeClass, isActive);
          });
        };
        // view -> model
        element.bind(options.toggleEvent, function () {
          scope.$apply(function () {
            // console.warn('!click', element.attr('value'), 'controller.$viewValue', typeof controller.$viewValue, controller.$viewValue, 'controller.$modelValue', typeof controller.$modelValue, controller.$modelValue);
            controller.$setViewValue(value);
            controller.$render();
          });
        });
      }
    };
  }
]);

// Source: datepicker.js
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

// Source: dropdown.js
angular.module('mgcrea.ngStrap.dropdown', ['mgcrea.ngStrap.tooltip']).provider('$dropdown', function () {
  var defaults = this.defaults = {
      animation: 'am-fade',
      prefixClass: 'dropdown',
      placement: 'bottom-left',
      template: 'dropdown/dropdown.tpl.html',
      trigger: 'click',
      container: false,
      keyboard: true,
      html: false,
      delay: 0
    };
  this.$get = [
    '$window',
    '$rootScope',
    '$tooltip',
    function ($window, $rootScope, $tooltip) {
      var bodyEl = angular.element($window.document.body);
      var matchesSelector = Element.prototype.matchesSelector || Element.prototype.webkitMatchesSelector || Element.prototype.mozMatchesSelector || Element.prototype.msMatchesSelector || Element.prototype.oMatchesSelector;
      function DropdownFactory(element, config) {
        var $dropdown = {};
        // Common vars
        var options = angular.extend({}, defaults, config);
        var scope = $dropdown.$scope = options.scope && options.scope.$new() || $rootScope.$new();
        $dropdown = $tooltip(element, options);
        // Protected methods
        $dropdown.$onKeyDown = function (evt) {
          if (!/(38|40)/.test(evt.keyCode))
            return;
          evt.preventDefault();
          evt.stopPropagation();
          // Retrieve focused index
          var items = angular.element($dropdown.$element[0].querySelectorAll('li:not(.divider) a'));
          if (!items.length)
            return;
          var index;
          angular.forEach(items, function (el, i) {
            if (matchesSelector && matchesSelector.call(el, ':focus'))
              index = i;
          });
          // Navigate with keyboard
          if (evt.keyCode === 38 && index > 0)
            index--;
          else if (evt.keyCode === 40 && index < items.length - 1)
            index++;
          else if (angular.isUndefined(index))
            index = 0;
          items.eq(index)[0].focus();
        };
        // Overrides
        var show = $dropdown.show;
        $dropdown.show = function () {
          show();
          setTimeout(function () {
            options.keyboard && $dropdown.$element.on('keydown', $dropdown.$onKeyDown);
            bodyEl.on('click', onBodyClick);
          });
        };
        var hide = $dropdown.hide;
        $dropdown.hide = function () {
          options.keyboard && $dropdown.$element.off('keydown', $dropdown.$onKeyDown);
          bodyEl.off('click', onBodyClick);
          hide();
        };
        // Private functions
        function onBodyClick(evt) {
          if (evt.target === element[0])
            return;
          return evt.target !== element[0] && $dropdown.hide();
        }
        return $dropdown;
      }
      return DropdownFactory;
    }
  ];
}).directive('bsDropdown', [
  '$window',
  '$location',
  '$sce',
  '$dropdown',
  function ($window, $location, $sce, $dropdown) {
    return {
      restrict: 'EAC',
      scope: true,
      link: function postLink(scope, element, attr, transclusion) {
        // Directive options
        var options = { scope: scope };
        angular.forEach([
          'placement',
          'container',
          'delay',
          'trigger',
          'keyboard',
          'html',
          'animation',
          'template'
        ], function (key) {
          if (angular.isDefined(attr[key]))
            options[key] = attr[key];
        });
        // Support scope as an object
        attr.bsDropdown && scope.$watch(attr.bsDropdown, function (newValue, oldValue) {
          scope.content = newValue;
        }, true);
        // Initialize dropdown
        var dropdown = $dropdown(element, options);
        // Garbage collection
        scope.$on('$destroy', function () {
          dropdown.destroy();
          options = null;
          dropdown = null;
        });
      }
    };
  }
]);

// Source: date-parser.js
angular.module('mgcrea.ngStrap.helpers.dateParser', []).provider('$dateParser', [
  '$localeProvider',
  function ($localeProvider) {
    var proto = Date.prototype;
    function isNumeric(n) {
      return !isNaN(parseFloat(n)) && isFinite(n);
    }
    var defaults = this.defaults = {
        format: 'shortDate',
        strict: false
      };
    this.$get = [
      '$locale',
      function ($locale) {
        var DateParserFactory = function (config) {
          var options = angular.extend({}, defaults, config);
          var $dateParser = {};
          var regExpMap = {
              'sss': '[0-9]{3}',
              'ss': '[0-5][0-9]',
              's': options.strict ? '[1-5]?[0-9]' : '[0-9]|[0-5][0-9]',
              'mm': '[0-5][0-9]',
              'm': options.strict ? '[1-5]?[0-9]' : '[0-9]|[0-5][0-9]',
              'HH': '[01][0-9]|2[0-3]',
              'H': options.strict ? '1?[0-9]|2[0-3]' : '[01]?[0-9]|2[0-3]',
              'hh': '[0][1-9]|[1][012]',
              'h': options.strict ? '[1-9]|1[012]' : '0?[1-9]|1[012]',
              'a': 'AM|PM',
              'EEEE': $locale.DATETIME_FORMATS.DAY.join('|'),
              'EEE': $locale.DATETIME_FORMATS.SHORTDAY.join('|'),
              'dd': '0[1-9]|[12][0-9]|3[01]',
              'd': options.strict ? '[1-9]|[1-2][0-9]|3[01]' : '0?[1-9]|[1-2][0-9]|3[01]',
              'MMMM': $locale.DATETIME_FORMATS.MONTH.join('|'),
              'MMM': $locale.DATETIME_FORMATS.SHORTMONTH.join('|'),
              'MM': '0[1-9]|1[012]',
              'M': options.strict ? '[1-9]|1[012]' : '0?[1-9]|1[012]',
              'yyyy': '[1]{1}[0-9]{3}|[2]{1}[0-9]{3}',
              'yy': '[0-9]{2}',
              'y': options.strict ? '-?(0|[1-9][0-9]{0,3})' : '-?0*[0-9]{1,4}'
            };
          var setFnMap = {
              'sss': proto.setMilliseconds,
              'ss': proto.setSeconds,
              's': proto.setSeconds,
              'mm': proto.setMinutes,
              'm': proto.setMinutes,
              'HH': proto.setHours,
              'H': proto.setHours,
              'hh': proto.setHours,
              'h': proto.setHours,
              'dd': proto.setDate,
              'd': proto.setDate,
              'a': function (value) {
                var hours = this.getHours();
                return this.setHours(value.match(/pm/i) ? hours + 12 : hours);
              },
              'MMMM': function (value) {
                return this.setMonth($locale.DATETIME_FORMATS.MONTH.indexOf(value));
              },
              'MMM': function (value) {
                return this.setMonth($locale.DATETIME_FORMATS.SHORTMONTH.indexOf(value));
              },
              'MM': function (value) {
                return this.setMonth(1 * value - 1);
              },
              'M': function (value) {
                return this.setMonth(1 * value - 1);
              },
              'yyyy': proto.setFullYear,
              'yy': function (value) {
                return this.setFullYear(2000 + 1 * value);
              },
              'y': proto.setFullYear
            };
          var regex, setMap;
          $dateParser.init = function () {
            $dateParser.$format = $locale.DATETIME_FORMATS[options.format] || options.format;
            regex = regExpForFormat($dateParser.$format);
            setMap = setMapForFormat($dateParser.$format);
          };
          $dateParser.isValid = function (date) {
            if (angular.isDate(date))
              return !isNaN(date.getTime());
            return regex.test(date);
          };
          $dateParser.parse = function (value, baseDate) {
            if (angular.isDate(value))
              return value;
            var matches = regex.exec(value);
            if (!matches)
              return false;
            var date = baseDate || new Date(0, 0, 1);
            for (var i = 0; i < matches.length - 1; i++) {
              setMap[i] && setMap[i].call(date, matches[i + 1]);
            }
            return date;
          };
          // Private functions
          function setMapForFormat(format) {
            var keys = Object.keys(setFnMap), i;
            var map = [], sortedMap = [];
            // Map to setFn
            var clonedFormat = format;
            for (i = 0; i < keys.length; i++) {
              if (format.split(keys[i]).length > 1) {
                var index = clonedFormat.search(keys[i]);
                format = format.split(keys[i]).join('');
                if (setFnMap[keys[i]])
                  map[index] = setFnMap[keys[i]];
              }
            }
            // Sort result map
            angular.forEach(map, function (v) {
              sortedMap.push(v);
            });
            return sortedMap;
          }
          function escapeReservedSymbols(text) {
            return text.replace(/\//g, '[\\/]').replace('/-/g', '[-]').replace(/\./g, '[.]').replace(/\\s/g, '[\\s]');
          }
          function regExpForFormat(format) {
            var keys = Object.keys(regExpMap), i;
            var re = format;
            // Abstract replaces to avoid collisions
            for (i = 0; i < keys.length; i++) {
              re = re.split(keys[i]).join('${' + i + '}');
            }
            // Replace abstracted values
            for (i = 0; i < keys.length; i++) {
              re = re.split('${' + i + '}').join('(' + regExpMap[keys[i]] + ')');
            }
            format = escapeReservedSymbols(format);
            return new RegExp('^' + re + '$', ['i']);
          }
          $dateParser.init();
          return $dateParser;
        };
        return DateParserFactory;
      }
    ];
  }
]);

// Source: debounce.js
angular.module('mgcrea.ngStrap.helpers.debounce', []).constant('debounce', function (func, wait, immediate) {
  var timeout, args, context, timestamp, result;
  return function () {
    context = this;
    args = arguments;
    timestamp = new Date();
    var later = function () {
      var last = new Date() - timestamp;
      if (last < wait) {
        timeout = setTimeout(later, wait - last);
      } else {
        timeout = null;
        if (!immediate)
          result = func.apply(context, args);
      }
    };
    var callNow = immediate && !timeout;
    if (!timeout) {
      timeout = setTimeout(later, wait);
    }
    if (callNow)
      result = func.apply(context, args);
    return result;
  };
}).constant('throttle', function (func, wait, options) {
  var context, args, result;
  var timeout = null;
  var previous = 0;
  options || (options = {});
  var later = function () {
    previous = options.leading === false ? 0 : new Date();
    timeout = null;
    result = func.apply(context, args);
  };
  return function () {
    var now = new Date();
    if (!previous && options.leading === false)
      previous = now;
    var remaining = wait - (now - previous);
    context = this;
    args = arguments;
    if (remaining <= 0) {
      clearTimeout(timeout);
      timeout = null;
      previous = now;
      result = func.apply(context, args);
    } else if (!timeout && options.trailing !== false) {
      timeout = setTimeout(later, remaining);
    }
    return result;
  };
});

// Source: dimensions.js
angular.module('mgcrea.ngStrap.helpers.dimensions', []).factory('dimensions', [
  '$document',
  '$window',
  function ($document, $window) {
    var jqLite = angular.element;
    var fn = {};
    /**
     * Test the element nodeName
     * @param element
     * @param name
     */
    var nodeName = fn.nodeName = function (element, name) {
        return element.nodeName && element.nodeName.toLowerCase() === name.toLowerCase();
      };
    /**
     * Returns the element computed style
     * @param element
     * @param prop
     * @param extra
     */
    fn.css = function (element, prop, extra) {
      var value;
      if (element.currentStyle) {
        //IE
        value = element.currentStyle[prop];
      } else if (window.getComputedStyle) {
        value = window.getComputedStyle(element)[prop];
      } else {
        value = element.style[prop];
      }
      return extra === true ? parseFloat(value) || 0 : value;
    };
    /**
     * Provides read-only equivalent of jQuery's offset function:
     * @required-by bootstrap-tooltip, bootstrap-affix
     * @url http://api.jquery.com/offset/
     * @param element
     */
    fn.offset = function (element) {
      var boxRect = element.getBoundingClientRect();
      var docElement = element.ownerDocument;
      return {
        width: element.offsetWidth,
        height: element.offsetHeight,
        top: boxRect.top + (window.pageYOffset || docElement.documentElement.scrollTop) - (docElement.documentElement.clientTop || 0),
        left: boxRect.left + (window.pageXOffset || docElement.documentElement.scrollLeft) - (docElement.documentElement.clientLeft || 0)
      };
    };
    /**
     * Provides read-only equivalent of jQuery's position function
     * @required-by bootstrap-tooltip, bootstrap-affix
     * @url http://api.jquery.com/offset/
     * @param element
     */
    fn.position = function (element) {
      var offsetParentRect = {
          top: 0,
          left: 0
        }, offsetParentElement, offset;
      // Fixed elements are offset from window (parentOffset = {top:0, left: 0}, because it is it's only offset parent
      if (fn.css(element, 'position') === 'fixed') {
        // We assume that getBoundingClientRect is available when computed position is fixed
        offset = element.getBoundingClientRect();
      } else {
        // Get *real* offsetParentElement
        offsetParentElement = offsetParent(element);
        offset = fn.offset(element);
        // Get correct offsets
        offset = fn.offset(element);
        if (!nodeName(offsetParentElement, 'html')) {
          offsetParentRect = fn.offset(offsetParentElement);
        }
        // Add offsetParent borders
        offsetParentRect.top += fn.css(offsetParentElement, 'borderTopWidth', true);
        offsetParentRect.left += fn.css(offsetParentElement, 'borderLeftWidth', true);
      }
      // Subtract parent offsets and element margins
      return {
        width: element.offsetWidth,
        height: element.offsetHeight,
        top: offset.top - offsetParentRect.top - fn.css(element, 'marginTop', true),
        left: offset.left - offsetParentRect.left - fn.css(element, 'marginLeft', true)
      };
    };
    /**
     * Returns the closest, non-statically positioned offsetParent of a given element
     * @required-by fn.position
     * @param element
     */
    var offsetParent = function offsetParentElement(element) {
      var docElement = element.ownerDocument;
      var offsetParent = element.offsetParent || docElement;
      if (nodeName(offsetParent, '#document'))
        return docElement.documentElement;
      while (offsetParent && !nodeName(offsetParent, 'html') && fn.css(offsetParent, 'position') === 'static') {
        offsetParent = offsetParent.offsetParent;
      }
      return offsetParent || docElement.documentElement;
    };
    /**
     * Provides equivalent of jQuery's height function
     * @required-by bootstrap-affix
     * @url http://api.jquery.com/height/
     * @param element
     * @param outer
     */
    fn.height = function (element, outer) {
      var value = element.offsetHeight;
      if (outer) {
        value += fn.css(element, 'marginTop', true) + fn.css(element, 'marginBottom', true);
      } else {
        value -= fn.css(element, 'paddingTop', true) + fn.css(element, 'paddingBottom', true) + fn.css(element, 'borderTopWidth', true) + fn.css(element, 'borderBottomWidth', true);
      }
      return value;
    };
    /**
     * Provides equivalent of jQuery's height function
     * @required-by bootstrap-affix
     * @url http://api.jquery.com/width/
     * @param element
     * @param outer
     */
    fn.width = function (element, outer) {
      var value = element.offsetWidth;
      if (outer) {
        value += fn.css(element, 'marginLeft', true) + fn.css(element, 'marginRight', true);
      } else {
        value -= fn.css(element, 'paddingLeft', true) + fn.css(element, 'paddingRight', true) + fn.css(element, 'borderLeftWidth', true) + fn.css(element, 'borderRightWidth', true);
      }
      return value;
    };
    return fn;
  }
]);

// Source: parse-options.js
angular.module('mgcrea.ngStrap.helpers.parseOptions', []).provider('$parseOptions', function () {
  var defaults = this.defaults = { regexp: /^\s*(.*?)(?:\s+as\s+(.*?))?(?:\s+group\s+by\s+(.*))?\s+for\s+(?:([\$\w][\$\w]*)|(?:\(\s*([\$\w][\$\w]*)\s*,\s*([\$\w][\$\w]*)\s*\)))\s+in\s+(.*?)(?:\s+track\s+by\s+(.*?))?$/ };
  this.$get = [
    '$parse',
    '$q',
    function ($parse, $q) {
      function ParseOptionsFactory(attr, config) {
        var $parseOptions = {};
        // Common vars
        var options = angular.extend({}, defaults, config);
        $parseOptions.$values = [];
        // Private vars
        var match, displayFn, valueName, keyName, groupByFn, valueFn, valuesFn;
        $parseOptions.init = function () {
          $parseOptions.$match = match = attr.match(options.regexp);
          displayFn = $parse(match[2] || match[1]), valueName = match[4] || match[6], keyName = match[5], groupByFn = $parse(match[3] || ''), valueFn = $parse(match[2] ? match[1] : valueName), valuesFn = $parse(match[7]);
        };
        $parseOptions.valuesFn = function (scope, controller) {
          return $q.when(valuesFn(scope, controller)).then(function (values) {
            $parseOptions.$values = values ? parseValues(values, scope) : {};
            return $parseOptions.$values;
          });
        };
        // Private functions
        function parseValues(values, scope) {
          return values.map(function (match, index) {
            var locals = {}, label, value;
            locals[valueName] = match;
            label = displayFn(scope, locals);
            value = valueFn(scope, locals) || index;
            return {
              label: label,
              value: value
            };
          });
        }
        $parseOptions.init();
        return $parseOptions;
      }
      return ParseOptionsFactory;
    }
  ];
});

// Source: raf.js
angular.version.minor < 3 && angular.version.dot < 14 && angular.module('ng').factory('$$rAF', [
  '$window',
  '$timeout',
  function ($window, $timeout) {
    var requestAnimationFrame = $window.requestAnimationFrame || $window.webkitRequestAnimationFrame || $window.mozRequestAnimationFrame;
    var cancelAnimationFrame = $window.cancelAnimationFrame || $window.webkitCancelAnimationFrame || $window.mozCancelAnimationFrame || $window.webkitCancelRequestAnimationFrame;
    var rafSupported = !!requestAnimationFrame;
    var raf = rafSupported ? function (fn) {
        var id = requestAnimationFrame(fn);
        return function () {
          cancelAnimationFrame(id);
        };
      } : function (fn) {
        var timer = $timeout(fn, 16.66, false);
        // 1000 / 60 = 16.666
        return function () {
          $timeout.cancel(timer);
        };
      };
    raf.supported = rafSupported;
    return raf;
  }
]);  // .factory('$$animateReflow', function($$rAF, $document) {
     //   var bodyEl = $document[0].body;
     //   return function(fn) {
     //     //the returned function acts as the cancellation function
     //     return $$rAF(function() {
     //       //the line below will force the browser to perform a repaint
     //       //so that all the animated elements within the animation frame
     //       //will be properly updated and drawn on screen. This is
     //       //required to perform multi-class CSS based animations with
     //       //Firefox. DO NOT REMOVE THIS LINE.
     //       var a = bodyEl.offsetWidth + 1;
     //       fn();
     //     });
     //   };
     // });

// Source: modal.js
angular.module('mgcrea.ngStrap.modal', ['mgcrea.ngStrap.helpers.dimensions']).provider('$modal', function () {
  var defaults = this.defaults = {
      animation: 'am-fade',
      backdropAnimation: 'am-fade',
      prefixClass: 'modal',
      prefixEvent: 'modal',
      placement: 'top',
      template: 'modal/modal.tpl.html',
      contentTemplate: false,
      container: false,
      element: null,
      backdrop: true,
      keyboard: true,
      html: false,
      show: true
    };
  this.$get = [
    '$window',
    '$rootScope',
    '$compile',
    '$q',
    '$templateCache',
    '$http',
    '$animate',
    '$timeout',
    '$sce',
    'dimensions',
    function ($window, $rootScope, $compile, $q, $templateCache, $http, $animate, $timeout, $sce, dimensions) {
      var forEach = angular.forEach;
      var trim = String.prototype.trim;
      var requestAnimationFrame = $window.requestAnimationFrame || $window.setTimeout;
      var bodyElement = angular.element($window.document.body);
      var htmlReplaceRegExp = /ng-bind="/gi;
      function ModalFactory(config) {
        var $modal = {};
        // Common vars
        var options = $modal.$options = angular.extend({}, defaults, config);
        $modal.$promise = fetchTemplate(options.template);
        var scope = $modal.$scope = options.scope && options.scope.$new() || $rootScope.$new();
        if (!options.element && !options.container) {
          options.container = 'body';
        }
        // Support scope as string options
        forEach([
          'title',
          'content'
        ], function (key) {
          if (options[key])
            scope[key] = $sce.trustAsHtml(options[key]);
        });
        // Provide scope helpers
        scope.$hide = function () {
          scope.$$postDigest(function () {
            $modal.hide();
          });
        };
        scope.$show = function () {
          scope.$$postDigest(function () {
            $modal.show();
          });
        };
        scope.$toggle = function () {
          scope.$$postDigest(function () {
            $modal.toggle();
          });
        };
        // Support contentTemplate option
        if (options.contentTemplate) {
          $modal.$promise = $modal.$promise.then(function (template) {
            var templateEl = angular.element(template);
            return fetchTemplate(options.contentTemplate).then(function (contentTemplate) {
              var contentEl = findElement('[ng-bind="content"]', templateEl[0]).removeAttr('ng-bind').html(contentTemplate);
              // Drop the default footer as you probably don't want it if you use a custom contentTemplate
              if (!config.template)
                contentEl.next().remove();
              return templateEl[0].outerHTML;
            });
          });
        }
        // Fetch, compile then initialize modal
        var modalLinker, modalElement;
        var backdropElement = angular.element('<div class="' + options.prefixClass + '-backdrop"/>');
        $modal.$promise.then(function (template) {
          if (angular.isObject(template))
            template = template.data;
          if (options.html)
            template = template.replace(htmlReplaceRegExp, 'ng-bind-html="');
          template = trim.apply(template);
          modalLinker = $compile(template);
          $modal.init();
        });
        $modal.init = function () {
          // Options: show
          if (options.show) {
            scope.$$postDigest(function () {
              $modal.show();
            });
          }
        };
        $modal.destroy = function () {
          // Remove element
          if (modalElement) {
            modalElement.remove();
            modalElement = null;
          }
          if (backdropElement) {
            backdropElement.remove();
            backdropElement = null;
          }
          // Destroy scope
          scope.$destroy();
        };
        $modal.show = function () {
          scope.$emit(options.prefixEvent + '.show.before', $modal);
          var parent = options.container ? findElement(options.container) : null;
          var after = options.container ? null : options.element;
          // Fetch a cloned element linked from template
          modalElement = $modal.$element = modalLinker(scope, function (clonedElement, scope) {
          });
          // Set the initial positioning.
          modalElement.css({ display: 'block' }).addClass(options.placement);
          // Options: animation
          if (options.animation) {
            if (options.backdrop) {
              backdropElement.addClass(options.backdropAnimation);
            }
            modalElement.addClass(options.animation);
          }
          if (options.backdrop) {
            $animate.enter(backdropElement, bodyElement, null, function () {
            });
          }
          $animate.enter(modalElement, parent, after, function () {
            scope.$emit(options.prefixEvent + '.show', $modal);
          });
          scope.$isShown = true;
          scope.$$phase || scope.$root.$$phase || scope.$digest();
          // Focus once the enter-animation has started
          // Weird PhantomJS bug hack
          var el = modalElement[0];
          requestAnimationFrame(function () {
            el.focus();
          });
          bodyElement.addClass(options.prefixClass + '-open');
          if (options.animation) {
            bodyElement.addClass(options.prefixClass + '-with-' + options.animation);
          }
          // Bind events
          if (options.backdrop) {
            modalElement.on('click', hideOnBackdropClick);
            backdropElement.on('click', hideOnBackdropClick);
          }
          if (options.keyboard) {
            modalElement.on('keyup', $modal.$onKeyUp);
          }
        };
        $modal.hide = function () {
          scope.$emit(options.prefixEvent + '.hide.before', $modal);
          $animate.leave(modalElement, function () {
            scope.$emit(options.prefixEvent + '.hide', $modal);
            bodyElement.removeClass(options.prefixClass + '-open');
            if (options.animation) {
              bodyElement.addClass(options.prefixClass + '-with-' + options.animation);
            }
          });
          if (options.backdrop) {
            $animate.leave(backdropElement, function () {
            });
          }
          scope.$isShown = false;
          scope.$$phase || scope.$root.$$phase || scope.$digest();
          // Unbind events
          if (options.backdrop) {
            modalElement.off('click', hideOnBackdropClick);
            backdropElement.off('click', hideOnBackdropClick);
          }
          if (options.keyboard) {
            modalElement.off('keyup', $modal.$onKeyUp);
          }
        };
        $modal.toggle = function () {
          scope.$isShown ? $modal.hide() : $modal.show();
        };
        $modal.focus = function () {
          modalElement[0].focus();
        };
        // Protected methods
        $modal.$onKeyUp = function (evt) {
          evt.which === 27 && $modal.hide();
        };
        // Private methods
        function hideOnBackdropClick(evt) {
          if (evt.target !== evt.currentTarget)
            return;
          options.backdrop === 'static' ? $modal.focus() : $modal.hide();
        }
        return $modal;
      }
      // Helper functions
      function findElement(query, element) {
        return angular.element((element || document).querySelectorAll(query));
      }
      function fetchTemplate(template) {
        return $q.when($templateCache.get(template) || $http.get(template)).then(function (res) {
          if (angular.isObject(res)) {
            $templateCache.put(template, res.data);
            return res.data;
          }
          return res;
        });
      }
      return ModalFactory;
    }
  ];
}).directive('bsModal', [
  '$window',
  '$location',
  '$sce',
  '$modal',
  function ($window, $location, $sce, $modal) {
    return {
      restrict: 'EAC',
      scope: true,
      link: function postLink(scope, element, attr, transclusion) {
        // Directive options
        var options = {
            scope: scope,
            element: element,
            show: false
          };
        angular.forEach([
          'template',
          'contentTemplate',
          'placement',
          'backdrop',
          'keyboard',
          'html',
          'container',
          'animation'
        ], function (key) {
          if (angular.isDefined(attr[key]))
            options[key] = attr[key];
        });
        // Support scope as data-attrs
        angular.forEach([
          'title',
          'content'
        ], function (key) {
          attr[key] && attr.$observe(key, function (newValue, oldValue) {
            scope[key] = $sce.trustAsHtml(newValue);
          });
        });
        // Support scope as an object
        attr.bsModal && scope.$watch(attr.bsModal, function (newValue, oldValue) {
          if (angular.isObject(newValue)) {
            angular.extend(scope, newValue);
          } else {
            scope.content = newValue;
          }
        }, true);
        // Initialize modal
        var modal = $modal(options);
        // Trigger
        element.on(attr.trigger || 'click', modal.toggle);
        // Garbage collection
        scope.$on('$destroy', function () {
          modal.destroy();
          options = null;
          modal = null;
        });
      }
    };
  }
]);

// Source: navbar.js
angular.module('mgcrea.ngStrap.navbar', []).provider('$navbar', function () {
  var defaults = this.defaults = {
      activeClass: 'active',
      routeAttr: 'data-match-route',
      strict: false
    };
  this.$get = function () {
    return { defaults: defaults };
  };
}).directive('bsNavbar', [
  '$window',
  '$location',
  '$navbar',
  function ($window, $location, $navbar) {
    var defaults = $navbar.defaults;
    return {
      restrict: 'A',
      link: function postLink(scope, element, attr, controller) {
        // Directive options
        var options = angular.copy(defaults);
        angular.forEach(Object.keys(defaults), function (key) {
          if (angular.isDefined(attr[key]))
            options[key] = attr[key];
        });
        // Watch for the $location
        scope.$watch(function () {
          return $location.path();
        }, function (newValue, oldValue) {
          var liElements = element[0].querySelectorAll('li[' + options.routeAttr + ']');
          angular.forEach(liElements, function (li) {
            var liElement = angular.element(li);
            var pattern = liElement.attr(options.routeAttr).replace('/', '\\/');
            if (options.strict) {
              pattern = '^' + pattern + '$';
            }
            var regexp = new RegExp(pattern, ['i']);
            if (regexp.test(newValue)) {
              liElement.addClass(options.activeClass);
            } else {
              liElement.removeClass(options.activeClass);
            }
          });
        });
      }
    };
  }
]);

// Source: select.js
angular.module('mgcrea.ngStrap.select', [
  'mgcrea.ngStrap.tooltip',
  'mgcrea.ngStrap.helpers.parseOptions'
]).provider('$select', function () {
  var defaults = this.defaults = {
      animation: 'am-fade',
      prefixClass: 'select',
      placement: 'bottom-left',
      template: 'select/select.tpl.html',
      trigger: 'focus',
      container: false,
      keyboard: true,
      html: false,
      delay: 0,
      multiple: false,
      sort: true,
      caretHtml: '&nbsp;<span class="caret"></span>',
      placeholder: 'Choose among the following...',
      maxLength: 3,
      maxLengthHtml: 'selected'
    };
  this.$get = [
    '$window',
    '$document',
    '$rootScope',
    '$tooltip',
    function ($window, $document, $rootScope, $tooltip) {
      var bodyEl = angular.element($window.document.body);
      var isTouch = 'createTouch' in $window.document;
      function SelectFactory(element, controller, config) {
        var $select = {};
        // Common vars
        var options = angular.extend({}, defaults, config);
        $select = $tooltip(element, options);
        var parentScope = config.scope;
        var scope = $select.$scope;
        scope.$matches = [];
        scope.$activeIndex = 0;
        scope.$isMultiple = options.multiple;
        scope.$activate = function (index) {
          scope.$$postDigest(function () {
            $select.activate(index);
          });
        };
        scope.$select = function (index, evt) {
          scope.$$postDigest(function () {
            $select.select(index);
          });
        };
        scope.$isVisible = function () {
          return $select.$isVisible();
        };
        scope.$isActive = function (index) {
          return $select.$isActive(index);
        };
        // Public methods
        $select.update = function (matches) {
          scope.$matches = matches;
          $select.$updateActiveIndex();
        };
        $select.activate = function (index) {
          if (options.multiple) {
            scope.$activeIndex.sort();
            $select.$isActive(index) ? scope.$activeIndex.splice(scope.$activeIndex.indexOf(index), 1) : scope.$activeIndex.push(index);
            if (options.sort)
              scope.$activeIndex.sort();
          } else {
            scope.$activeIndex = index;
          }
          return scope.$activeIndex;
        };
        $select.select = function (index) {
          var value = scope.$matches[index].value;
          $select.activate(index);
          if (options.multiple) {
            controller.$setViewValue(scope.$activeIndex.map(function (index) {
              return scope.$matches[index].value;
            }));
          } else {
            controller.$setViewValue(value);
          }
          controller.$render();
          if (parentScope)
            parentScope.$digest();
          // Hide if single select
          if (!options.multiple) {
            $select.hide();
          }
          // Emit event
          scope.$emit('$select.select', value, index);
        };
        // Protected methods
        $select.$updateActiveIndex = function () {
          if (controller.$modelValue && scope.$matches.length) {
            if (options.multiple && angular.isArray(controller.$modelValue)) {
              scope.$activeIndex = controller.$modelValue.map(function (value) {
                return $select.$getIndex(value);
              });
            } else {
              scope.$activeIndex = $select.$getIndex(controller.$modelValue);
            }
          } else if (scope.$activeIndex >= scope.$matches.length) {
            scope.$activeIndex = options.multiple ? [] : 0;
          }
        };
        $select.$isVisible = function () {
          if (!options.minLength || !controller) {
            return scope.$matches.length;
          }
          // minLength support
          return scope.$matches.length && controller.$viewValue.length >= options.minLength;
        };
        $select.$isActive = function (index) {
          if (options.multiple) {
            return scope.$activeIndex.indexOf(index) !== -1;
          } else {
            return scope.$activeIndex === index;
          }
        };
        $select.$getIndex = function (value) {
          var l = scope.$matches.length, i = l;
          if (!l)
            return;
          for (i = l; i--;) {
            if (scope.$matches[i].value === value)
              break;
          }
          if (i < 0)
            return;
          return i;
        };
        $select.$onMouseDown = function (evt) {
          // Prevent blur on mousedown on .dropdown-menu
          evt.preventDefault();
          evt.stopPropagation();
          // Emulate click for mobile devices
          if (isTouch) {
            var targetEl = angular.element(evt.target);
            targetEl.triggerHandler('click');
          }
        };
        $select.$onKeyDown = function (evt) {
          if (!/(9|13|38|40)/.test(evt.keyCode))
            return;
          evt.preventDefault();
          evt.stopPropagation();
          // Select with enter
          if (!options.multiple && (evt.keyCode === 13 || evt.keyCode === 9)) {
            return $select.select(scope.$activeIndex);
          }
          // Navigate with keyboard
          if (evt.keyCode === 38 && scope.$activeIndex > 0)
            scope.$activeIndex--;
          else if (evt.keyCode === 40 && scope.$activeIndex < scope.$matches.length - 1)
            scope.$activeIndex++;
          else if (angular.isUndefined(scope.$activeIndex))
            scope.$activeIndex = 0;
          scope.$digest();
        };
        // Overrides
        var _show = $select.show;
        $select.show = function () {
          _show();
          if (options.multiple) {
            $select.$element.addClass('select-multiple');
          }
          setTimeout(function () {
            $select.$element.on(isTouch ? 'touchstart' : 'mousedown', $select.$onMouseDown);
            if (options.keyboard) {
              element.on('keydown', $select.$onKeyDown);
            }
          });
        };
        var _hide = $select.hide;
        $select.hide = function () {
          $select.$element.off(isTouch ? 'touchstart' : 'mousedown', $select.$onMouseDown);
          if (options.keyboard) {
            element.off('keydown', $select.$onKeyDown);
          }
          _hide(true);
        };
        return $select;
      }
      SelectFactory.defaults = defaults;
      return SelectFactory;
    }
  ];
}).directive('bsSelect', [
  '$window',
  '$parse',
  '$q',
  '$select',
  '$parseOptions',
  function ($window, $parse, $q, $select, $parseOptions) {
    var defaults = $select.defaults;
    return {
      restrict: 'EAC',
      require: 'ngModel',
      link: function postLink(scope, element, attr, controller) {
        // Directive options
        var options = { scope: scope };
        angular.forEach([
          'placement',
          'container',
          'delay',
          'trigger',
          'keyboard',
          'html',
          'animation',
          'template',
          'placeholder',
          'multiple',
          'maxLength',
          'maxLengthHtml'
        ], function (key) {
          if (angular.isDefined(attr[key]))
            options[key] = attr[key];
        });
        // Add support for select markup
        if (element[0].nodeName.toLowerCase() === 'select') {
          var inputEl = element;
          inputEl.css('display', 'none');
          element = angular.element('<button type="button" class="btn btn-default"></button>');
          inputEl.after(element);
        }
        // Build proper ngOptions
        var parsedOptions = $parseOptions(attr.ngOptions);
        // Initialize select
        var select = $select(element, controller, options);
        // Watch ngOptions values before filtering for changes
        var watchedOptions = parsedOptions.$match[7].replace(/\|.+/, '').trim();
        scope.$watch(watchedOptions, function (newValue, oldValue) {
          // console.warn('scope.$watch(%s)', watchedOptions, newValue, oldValue);
          parsedOptions.valuesFn(scope, controller).then(function (values) {
            select.update(values);
            controller.$render();
          });
        }, true);
        // Watch model for changes
        scope.$watch(attr.ngModel, function (newValue, oldValue) {
          // console.warn('scope.$watch(%s)', attr.ngModel, newValue, oldValue);
          select.$updateActiveIndex();
        }, true);
        // Model rendering in view
        controller.$render = function () {
          // console.warn('$render', element.attr('ng-model'), 'controller.$modelValue', typeof controller.$modelValue, controller.$modelValue, 'controller.$viewValue', typeof controller.$viewValue, controller.$viewValue);
          var selected, index;
          if (options.multiple && angular.isArray(controller.$modelValue)) {
            selected = controller.$modelValue.map(function (value) {
              index = select.$getIndex(value);
              return angular.isDefined(index) ? select.$scope.$matches[index].label : false;
            }).filter(angular.isDefined);
            if (selected.length > (options.maxLength || defaults.maxLength)) {
              selected = selected.length + ' ' + (options.maxLengthHtml || defaults.maxLengthHtml);
            } else {
              selected = selected.join(', ');
            }
          } else {
            index = select.$getIndex(controller.$modelValue);
            selected = angular.isDefined(index) ? select.$scope.$matches[index].label : false;
          }
          element.html((selected ? selected : attr.placeholder || defaults.placeholder) + defaults.caretHtml);
        };
        // Garbage collection
        scope.$on('$destroy', function () {
          select.destroy();
          options = null;
          select = null;
        });
      }
    };
  }
]);

// Source: popover.js
angular.module('mgcrea.ngStrap.popover', ['mgcrea.ngStrap.tooltip']).provider('$popover', function () {
  var defaults = this.defaults = {
      animation: 'am-fade',
      placement: 'right',
      template: 'popover/popover.tpl.html',
      contentTemplate: false,
      trigger: 'click',
      keyboard: true,
      html: false,
      title: '',
      content: '',
      delay: 0,
      container: false
    };
  this.$get = [
    '$tooltip',
    function ($tooltip) {
      function PopoverFactory(element, config) {
        // Common vars
        var options = angular.extend({}, defaults, config);
        var $popover = $tooltip(element, options);
        // Support scope as string options [/*title, */content]
        if (options.content) {
          $popover.$scope.content = options.content;
        }
        return $popover;
      }
      return PopoverFactory;
    }
  ];
}).directive('bsPopover', [
  '$window',
  '$location',
  '$sce',
  '$popover',
  function ($window, $location, $sce, $popover) {
    var requestAnimationFrame = $window.requestAnimationFrame || $window.setTimeout;
    return {
      restrict: 'EAC',
      scope: true,
      link: function postLink(scope, element, attr) {
        // Directive options
        var options = { scope: scope };
        angular.forEach([
          'template',
          'contentTemplate',
          'placement',
          'container',
          'delay',
          'trigger',
          'keyboard',
          'html',
          'animation'
        ], function (key) {
          if (angular.isDefined(attr[key]))
            options[key] = attr[key];
        });
        // Support scope as data-attrs
        angular.forEach([
          'title',
          'content'
        ], function (key) {
          attr[key] && attr.$observe(key, function (newValue, oldValue) {
            scope[key] = $sce.trustAsHtml(newValue);
            angular.isDefined(oldValue) && requestAnimationFrame(function () {
              popover && popover.$applyPlacement();
            });
          });
        });
        // Support scope as an object
        attr.bsPopover && scope.$watch(attr.bsPopover, function (newValue, oldValue) {
          if (angular.isObject(newValue)) {
            angular.extend(scope, newValue);
          } else {
            scope.content = newValue;
          }
          angular.isDefined(oldValue) && requestAnimationFrame(function () {
            popover && popover.$applyPlacement();
          });
        }, true);
        // Initialize popover
        var popover = $popover(element, options);
        // Garbage collection
        scope.$on('$destroy', function () {
          popover.destroy();
          options = null;
          popover = null;
        });
      }
    };
  }
]);

// Source: scrollspy.js
angular.module('mgcrea.ngStrap.scrollspy', [
  'mgcrea.ngStrap.helpers.debounce',
  'mgcrea.ngStrap.helpers.dimensions'
]).provider('$scrollspy', function () {
  // Pool of registered spies
  var spies = this.$$spies = {};
  var defaults = this.defaults = {
      debounce: 150,
      throttle: 100,
      offset: 100
    };
  this.$get = [
    '$window',
    '$document',
    '$rootScope',
    'dimensions',
    'debounce',
    'throttle',
    function ($window, $document, $rootScope, dimensions, debounce, throttle) {
      var windowEl = angular.element($window);
      var docEl = angular.element($document.prop('documentElement'));
      var bodyEl = angular.element($window.document.body);
      // Helper functions
      function nodeName(element, name) {
        return element[0].nodeName && element[0].nodeName.toLowerCase() === name.toLowerCase();
      }
      function ScrollSpyFactory(config) {
        // Common vars
        var options = angular.extend({}, defaults, config);
        if (!options.element)
          options.element = bodyEl;
        var isWindowSpy = nodeName(options.element, 'body');
        var scrollEl = isWindowSpy ? windowEl : options.element;
        var scrollId = isWindowSpy ? 'window' : options.id;
        // Use existing spy
        if (spies[scrollId]) {
          spies[scrollId].$$count++;
          return spies[scrollId];
        }
        var $scrollspy = {};
        // Private vars
        var unbindViewContentLoaded, unbindIncludeContentLoaded;
        var trackedElements = $scrollspy.$trackedElements = [];
        var sortedElements = [];
        var activeTarget;
        var debouncedCheckPosition;
        var throttledCheckPosition;
        var debouncedCheckOffsets;
        var viewportHeight;
        var scrollTop;
        $scrollspy.init = function () {
          // Setup internal ref counter
          this.$$count = 1;
          // Bind events
          debouncedCheckPosition = debounce(this.checkPosition, options.debounce);
          throttledCheckPosition = throttle(this.checkPosition, options.throttle);
          scrollEl.on('click', this.checkPositionWithEventLoop);
          windowEl.on('resize', debouncedCheckPosition);
          scrollEl.on('scroll', throttledCheckPosition);
          debouncedCheckOffsets = debounce(this.checkOffsets, options.debounce);
          unbindViewContentLoaded = $rootScope.$on('$viewContentLoaded', debouncedCheckOffsets);
          unbindIncludeContentLoaded = $rootScope.$on('$includeContentLoaded', debouncedCheckOffsets);
          debouncedCheckOffsets();
          // Register spy for reuse
          if (scrollId) {
            spies[scrollId] = $scrollspy;
          }
        };
        $scrollspy.destroy = function () {
          // Check internal ref counter
          this.$$count--;
          if (this.$$count > 0) {
            return;
          }
          // Unbind events
          scrollEl.off('click', this.checkPositionWithEventLoop);
          windowEl.off('resize', debouncedCheckPosition);
          scrollEl.off('scroll', debouncedCheckPosition);
          unbindViewContentLoaded();
          unbindIncludeContentLoaded();
          if (scrollId) {
            delete spies[scrollId];
          }
        };
        $scrollspy.checkPosition = function () {
          // Not ready yet
          if (!sortedElements.length)
            return;
          // Calculate the scroll position
          scrollTop = (isWindowSpy ? $window.pageYOffset : scrollEl.prop('scrollTop')) || 0;
          // Calculate the viewport height for use by the components
          viewportHeight = Math.max($window.innerHeight, docEl.prop('clientHeight'));
          // Activate first element if scroll is smaller
          if (scrollTop < sortedElements[0].offsetTop && activeTarget !== sortedElements[0].target) {
            return $scrollspy.$activateElement(sortedElements[0]);
          }
          // Activate proper element
          for (var i = sortedElements.length; i--;) {
            if (angular.isUndefined(sortedElements[i].offsetTop) || sortedElements[i].offsetTop === null)
              continue;
            if (activeTarget === sortedElements[i].target)
              continue;
            if (scrollTop < sortedElements[i].offsetTop)
              continue;
            if (sortedElements[i + 1] && scrollTop > sortedElements[i + 1].offsetTop)
              continue;
            return $scrollspy.$activateElement(sortedElements[i]);
          }
        };
        $scrollspy.checkPositionWithEventLoop = function () {
          setTimeout(this.checkPosition, 1);
        };
        // Protected methods
        $scrollspy.$activateElement = function (element) {
          if (activeTarget) {
            var activeElement = $scrollspy.$getTrackedElement(activeTarget);
            if (activeElement) {
              activeElement.source.removeClass('active');
              if (nodeName(activeElement.source, 'li') && nodeName(activeElement.source.parent().parent(), 'li')) {
                activeElement.source.parent().parent().removeClass('active');
              }
            }
          }
          activeTarget = element.target;
          element.source.addClass('active');
          if (nodeName(element.source, 'li') && nodeName(element.source.parent().parent(), 'li')) {
            element.source.parent().parent().addClass('active');
          }
        };
        $scrollspy.$getTrackedElement = function (target) {
          return trackedElements.filter(function (obj) {
            return obj.target === target;
          })[0];
        };
        // Track offsets behavior
        $scrollspy.checkOffsets = function () {
          angular.forEach(trackedElements, function (trackedElement) {
            var targetElement = document.querySelector(trackedElement.target);
            trackedElement.offsetTop = targetElement ? dimensions.offset(targetElement).top : null;
            if (options.offset && trackedElement.offsetTop !== null)
              trackedElement.offsetTop -= options.offset * 1;
          });
          sortedElements = trackedElements.filter(function (el) {
            return el.offsetTop !== null;
          }).sort(function (a, b) {
            return a.offsetTop - b.offsetTop;
          });
          debouncedCheckPosition();
        };
        $scrollspy.trackElement = function (target, source) {
          trackedElements.push({
            target: target,
            source: source
          });
        };
        $scrollspy.untrackElement = function (target, source) {
          var toDelete;
          for (var i = trackedElements.length; i--;) {
            if (trackedElements[i].target === target && trackedElements[i].source === source) {
              toDelete = i;
              break;
            }
          }
          trackedElements = trackedElements.splice(toDelete, 1);
        };
        $scrollspy.activate = function (i) {
          trackedElements[i].addClass('active');
        };
        // Initialize plugin
        $scrollspy.init();
        return $scrollspy;
      }
      return ScrollSpyFactory;
    }
  ];
}).directive('bsScrollspy', [
  '$rootScope',
  'debounce',
  'dimensions',
  '$scrollspy',
  function ($rootScope, debounce, dimensions, $scrollspy) {
    return {
      restrict: 'EAC',
      link: function postLink(scope, element, attr) {
        var options = { scope: scope };
        angular.forEach([
          'offset',
          'target'
        ], function (key) {
          if (angular.isDefined(attr[key]))
            options[key] = attr[key];
        });
        var scrollspy = $scrollspy(options);
        scrollspy.trackElement(options.target, element);
        scope.$on('$destroy', function () {
          scrollspy.untrackElement(options.target, element);
          scrollspy.destroy();
          options = null;
          scrollspy = null;
        });
      }
    };
  }
]).directive('bsScrollspyList', [
  '$rootScope',
  'debounce',
  'dimensions',
  '$scrollspy',
  function ($rootScope, debounce, dimensions, $scrollspy) {
    return {
      restrict: 'A',
      compile: function postLink(element, attr) {
        var children = element[0].querySelectorAll('li > a[href]');
        angular.forEach(children, function (child) {
          var childEl = angular.element(child);
          childEl.parent().attr('bs-scrollspy', '').attr('data-target', childEl.attr('href'));
        });
      }
    };
  }
]);

// Source: tab.js
angular.module('mgcrea.ngStrap.tab', []).run([
  '$templateCache',
  function ($templateCache) {
    $templateCache.put('$pane', '{{pane.content}}');
  }
]).provider('$tab', function () {
  var defaults = this.defaults = {
      animation: 'am-fade',
      template: 'tab/tab.tpl.html'
    };
  this.$get = function () {
    return { defaults: defaults };
  };
}).directive('bsTabs', [
  '$window',
  '$animate',
  '$tab',
  function ($window, $animate, $tab) {
    var defaults = $tab.defaults;
    return {
      restrict: 'EAC',
      scope: true,
      require: '?ngModel',
      templateUrl: function (element, attr) {
        return attr.template || defaults.template;
      },
      link: function postLink(scope, element, attr, controller) {
        // Directive options
        var options = defaults;
        angular.forEach(['animation'], function (key) {
          if (angular.isDefined(attr[key]))
            options[key] = attr[key];
        });
        // Require scope as an object
        attr.bsTabs && scope.$watch(attr.bsTabs, function (newValue, oldValue) {
          scope.panes = newValue;
        }, true);
        // Add base class
        element.addClass('tabs');
        // Support animations
        if (options.animation) {
          element.addClass(options.animation);
        }
        scope.active = scope.activePane = 0;
        // view -> model
        scope.setActive = function (index, ev) {
          scope.active = index;
          if (controller) {
            controller.$setViewValue(index);
          }
        };
        // model -> view
        if (controller) {
          controller.$render = function () {
            scope.active = controller.$modelValue * 1;
          };
        }
      }
    };
  }
]);

// Source: timepicker.js
angular.module('mgcrea.ngStrap.timepicker', [
  'mgcrea.ngStrap.helpers.dateParser',
  'mgcrea.ngStrap.tooltip'
]).provider('$timepicker', function () {
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
  this.$get = [
    '$window',
    '$document',
    '$rootScope',
    '$sce',
    '$locale',
    'dateFilter',
    '$tooltip',
    function ($window, $document, $rootScope, $sce, $locale, dateFilter, $tooltip) {
      var bodyEl = angular.element($window.document.body);
      var isTouch = 'createTouch' in $window.document;
      var isNative = /(ip(a|o)d|iphone|android)/gi.test($window.navigator.userAgent);
      if (!defaults.lang)
        defaults.lang = $locale.id;
      function timepickerFactory(element, controller, config) {
        var $timepicker = $tooltip(element, angular.extend({}, defaults, config));
        var parentScope = config.scope;
        var options = $timepicker.$options;
        var scope = $timepicker.$scope;
        // View vars
        var selectedIndex = 0;
        var startDate = controller.$dateValue || new Date();
        var viewDate = {
            hour: startDate.getHours(),
            meridian: startDate.getHours() < 12,
            minute: startDate.getMinutes(),
            second: startDate.getSeconds(),
            millisecond: startDate.getMilliseconds()
          };
        var format = $locale.DATETIME_FORMATS[options.timeFormat] || options.timeFormat;
        var formats = /(h+)[:]?(m+)[ ]?(a?)/i.exec(format).slice(1);
        // Scope methods
        scope.$select = function (date, index) {
          $timepicker.select(date, index);
        };
        scope.$moveIndex = function (value, index) {
          $timepicker.$moveIndex(value, index);
        };
        scope.$switchMeridian = function (date) {
          $timepicker.switchMeridian(date);
        };
        // Public methods
        $timepicker.update = function (date) {
          // console.warn('$timepicker.update() newValue=%o', date);
          if (angular.isDate(date) && !isNaN(date.getTime())) {
            $timepicker.$date = date;
            angular.extend(viewDate, {
              hour: date.getHours(),
              minute: date.getMinutes(),
              second: date.getSeconds(),
              millisecond: date.getMilliseconds()
            });
            $timepicker.$build();
          } else if (!$timepicker.$isBuilt) {
            $timepicker.$build();
          }
        };
        $timepicker.select = function (date, index, keep) {
          // console.warn('$timepicker.select', date, scope.$mode);
          if (!controller.$dateValue || isNaN(controller.$dateValue.getTime()))
            controller.$dateValue = new Date(1970, 0, 1);
          if (!angular.isDate(date))
            date = new Date(date);
          if (index === 0)
            controller.$dateValue.setHours(date.getHours());
          else if (index === 1)
            controller.$dateValue.setMinutes(date.getMinutes());
          controller.$setViewValue(controller.$dateValue);
          controller.$render();
          if (options.autoclose && !keep) {
            $timepicker.hide(true);
          }
        };
        $timepicker.switchMeridian = function (date) {
          var hours = (date || controller.$dateValue).getHours();
          controller.$dateValue.setHours(hours < 12 ? hours + 12 : hours - 12);
          controller.$setViewValue(controller.$dateValue);
          controller.$render();
        };
        // Protected methods
        $timepicker.$build = function () {
          // console.warn('$timepicker.$build() viewDate=%o', viewDate);
          var i, midIndex = scope.midIndex = parseInt(options.length / 2, 10);
          var hours = [], hour;
          for (i = 0; i < options.length; i++) {
            hour = new Date(1970, 0, 1, viewDate.hour - (midIndex - i) * options.hourStep);
            hours.push({
              date: hour,
              label: dateFilter(hour, formats[0]),
              selected: $timepicker.$date && $timepicker.$isSelected(hour, 0),
              disabled: $timepicker.$isDisabled(hour, 0)
            });
          }
          var minutes = [], minute;
          for (i = 0; i < options.length; i++) {
            minute = new Date(1970, 0, 1, 0, viewDate.minute - (midIndex - i) * options.minuteStep);
            minutes.push({
              date: minute,
              label: dateFilter(minute, formats[1]),
              selected: $timepicker.$date && $timepicker.$isSelected(minute, 1),
              disabled: $timepicker.$isDisabled(minute, 1)
            });
          }
          var rows = [];
          for (i = 0; i < options.length; i++) {
            rows.push([
              hours[i],
              minutes[i]
            ]);
          }
          scope.rows = rows;
          scope.showAM = !!formats[2];
          scope.isAM = ($timepicker.$date || hours[midIndex].date).getHours() < 12;
          $timepicker.$isBuilt = true;
        };
        $timepicker.$isSelected = function (date, index) {
          if (!$timepicker.$date)
            return false;
          else if (index === 0) {
            return date.getHours() === $timepicker.$date.getHours();
          } else if (index === 1) {
            return date.getMinutes() === $timepicker.$date.getMinutes();
          }
        };
        $timepicker.$isDisabled = function (date, index) {
          var selectedTime;
          if (index === 0) {
            selectedTime = date.getTime() + viewDate.minute * 60000;
          } else if (index === 1) {
            selectedTime = date.getTime() + viewDate.hour * 3600000;
          }
          return selectedTime < options.minTime * 1 || selectedTime > options.maxTime * 1;
        };
        $timepicker.$moveIndex = function (value, index) {
          var targetDate;
          if (index === 0) {
            targetDate = new Date(1970, 0, 1, viewDate.hour + value * options.length, viewDate.minute);
            angular.extend(viewDate, { hour: targetDate.getHours() });
          } else if (index === 1) {
            targetDate = new Date(1970, 0, 1, viewDate.hour, viewDate.minute + value * options.length * options.minuteStep);
            angular.extend(viewDate, { minute: targetDate.getMinutes() });
          }
          $timepicker.$build();
        };
        $timepicker.$onMouseDown = function (evt) {
          // Prevent blur on mousedown on .dropdown-menu
          if (evt.target.nodeName.toLowerCase() !== 'input')
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
        $timepicker.$onKeyDown = function (evt) {
          if (!/(38|37|39|40|13)/.test(evt.keyCode) || evt.shiftKey || evt.altKey)
            return;
          evt.preventDefault();
          evt.stopPropagation();
          // Close on enter
          if (evt.keyCode === 13)
            return $timepicker.hide(true);
          // Navigate with keyboard
          var newDate = new Date($timepicker.$date);
          var hours = newDate.getHours(), hoursLength = dateFilter(newDate, 'h').length;
          var minutes = newDate.getMinutes(), minutesLength = dateFilter(newDate, 'mm').length;
          var lateralMove = /(37|39)/.test(evt.keyCode);
          var count = 2 + !!formats[2] * 1;
          // Navigate indexes (left, right)
          if (lateralMove) {
            if (evt.keyCode === 37)
              selectedIndex = selectedIndex < 1 ? count - 1 : selectedIndex - 1;
            else if (evt.keyCode === 39)
              selectedIndex = selectedIndex < count - 1 ? selectedIndex + 1 : 0;
          }
          // Update values (up, down)
          var selectRange = [
              0,
              hoursLength
            ];
          if (selectedIndex === 0) {
            if (evt.keyCode === 38)
              newDate.setHours(hours - parseInt(options.hourStep, 10));
            else if (evt.keyCode === 40)
              newDate.setHours(hours + parseInt(options.hourStep, 10));
            selectRange = [
              0,
              hoursLength
            ];
          } else if (selectedIndex === 1) {
            if (evt.keyCode === 38)
              newDate.setMinutes(minutes - parseInt(options.minuteStep, 10));
            else if (evt.keyCode === 40)
              newDate.setMinutes(minutes + parseInt(options.minuteStep, 10));
            selectRange = [
              hoursLength + 1,
              hoursLength + 1 + minutesLength
            ];
          } else if (selectedIndex === 2) {
            if (!lateralMove)
              $timepicker.switchMeridian();
            selectRange = [
              hoursLength + 1 + minutesLength + 1,
              hoursLength + 1 + minutesLength + 3
            ];
          }
          $timepicker.select(newDate, selectedIndex, true);
          createSelection(selectRange[0], selectRange[1]);
          parentScope.$digest();
        };
        // Private
        function createSelection(start, end) {
          if (element[0].createTextRange) {
            var selRange = element[0].createTextRange();
            selRange.collapse(true);
            selRange.moveStart('character', start);
            selRange.moveEnd('character', end);
            selRange.select();
          } else if (element[0].setSelectionRange) {
            element[0].setSelectionRange(start, end);
          } else if (angular.isUndefined(element[0].selectionStart)) {
            element[0].selectionStart = start;
            element[0].selectionEnd = end;
          }
        }
        function focusElement() {
          element[0].focus();
        }
        // Overrides
        var _init = $timepicker.init;
        $timepicker.init = function () {
          if (isNative && options.useNative) {
            element.prop('type', 'time');
            element.css('-webkit-appearance', 'textfield');
            return;
          } else if (isTouch) {
            element.prop('type', 'text');
            element.attr('readonly', 'true');
            element.on('click', focusElement);
          }
          _init();
        };
        var _destroy = $timepicker.destroy;
        $timepicker.destroy = function () {
          if (isNative && options.useNative) {
            element.off('click', focusElement);
          }
          _destroy();
        };
        var _show = $timepicker.show;
        $timepicker.show = function () {
          _show();
          setTimeout(function () {
            $timepicker.$element.on(isTouch ? 'touchstart' : 'mousedown', $timepicker.$onMouseDown);
            if (options.keyboard) {
              element.on('keydown', $timepicker.$onKeyDown);
            }
          });
        };
        var _hide = $timepicker.hide;
        $timepicker.hide = function (blur) {
          $timepicker.$element.off(isTouch ? 'touchstart' : 'mousedown', $timepicker.$onMouseDown);
          if (options.keyboard) {
            element.off('keydown', $timepicker.$onKeyDown);
          }
          _hide(blur);
        };
        return $timepicker;
      }
      timepickerFactory.defaults = defaults;
      return timepickerFactory;
    }
  ];
}).directive('bsTimepicker', [
  '$window',
  '$parse',
  '$q',
  '$locale',
  'dateFilter',
  '$timepicker',
  '$dateParser',
  '$timeout',
  function ($window, $parse, $q, $locale, dateFilter, $timepicker, $dateParser, $timeout) {
    var defaults = $timepicker.defaults;
    var isNative = /(ip(a|o)d|iphone|android)/gi.test($window.navigator.userAgent);
    var requestAnimationFrame = $window.requestAnimationFrame || $window.setTimeout;
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
          'timeType',
          'timeFormat',
          'useNative',
          'hourStep',
          'minuteStep',
          'length'
        ], function (key) {
          if (angular.isDefined(attr[key]))
            options[key] = attr[key];
        });
        // Initialize timepicker
        if (isNative && (options.useNative || defaults.useNative))
          options.timeFormat = 'HH:mm';
        var timepicker = $timepicker(element, controller, options);
        options = timepicker.$options;
        // Initialize parser
        var dateParser = $dateParser({
            format: options.timeFormat,
            lang: options.lang
          });
        // Observe attributes for changes
        angular.forEach([
          'minTime',
          'maxTime'
        ], function (key) {
          // console.warn('attr.$observe(%s)', key, attr[key]);
          angular.isDefined(attr[key]) && attr.$observe(key, function (newValue) {
            if (newValue === 'now') {
              timepicker.$options[key] = new Date().setFullYear(1970, 0, 1);
            } else if (angular.isString(newValue) && newValue.match(/^".+"$/)) {
              timepicker.$options[key] = +new Date(newValue.substr(1, newValue.length - 2));
            } else {
              timepicker.$options[key] = dateParser.parse(newValue, new Date(1970, 0, 1, 0));
            }
            !isNaN(timepicker.$options[key]) && timepicker.$build();
          });
        });
        // Watch model for changes
        scope.$watch(attr.ngModel, function (newValue, oldValue) {
          // console.warn('scope.$watch(%s)', attr.ngModel, newValue, oldValue, controller.$dateValue);
          timepicker.update(controller.$dateValue);
        }, true);
        // viewValue -> $parsers -> modelValue
        controller.$parsers.unshift(function (viewValue) {
          // console.warn('$parser("%s"): viewValue=%o', element.attr('ng-model'), viewValue);
          // Null values should correctly reset the model value & validity
          if (!viewValue) {
            controller.$setValidity('date', true);
            return;
          }
          var parsedTime = dateParser.parse(viewValue, controller.$dateValue);
          if (!parsedTime || isNaN(parsedTime.getTime())) {
            controller.$setValidity('date', false);
          } else {
            var isValid = parsedTime.getTime() >= options.minTime && parsedTime.getTime() <= options.maxTime;
            controller.$setValidity('date', isValid);
            // Only update the model when we have a valid date
            if (isValid)
              controller.$dateValue = parsedTime;
          }
          if (options.timeType === 'string') {
            return dateFilter(viewValue, options.timeFormat);
          } else if (options.timeType === 'number') {
            return controller.$dateValue.getTime();
          } else if (options.timeType === 'iso') {
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
          } else if (options.timeType === 'string') {
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
        controller.$render = function () {
          // console.warn('$render("%s"): viewValue=%o', element.attr('ng-model'), controller.$viewValue);
          element.val(!controller.$dateValue || isNaN(controller.$dateValue.getTime()) ? '' : dateFilter(controller.$dateValue, options.timeFormat));
        };
        // Garbage collection
        scope.$on('$destroy', function () {
          timepicker.destroy();
          options = null;
          timepicker = null;
        });
      }
    };
  }
]);

// Source: tooltip.js
angular.module('mgcrea.ngStrap.tooltip', ['mgcrea.ngStrap.helpers.dimensions']).provider('$tooltip', function () {
  var defaults = this.defaults = {
      animation: 'am-fade',
      prefixClass: 'tooltip',
      prefixEvent: 'tooltip',
      container: false,
      placement: 'top',
      template: 'tooltip/tooltip.tpl.html',
      contentTemplate: false,
      trigger: 'hover focus',
      keyboard: false,
      html: false,
      show: false,
      title: '',
      type: '',
      delay: 0
    };
  this.$get = [
    '$window',
    '$rootScope',
    '$compile',
    '$q',
    '$templateCache',
    '$http',
    '$animate',
    '$timeout',
    'dimensions',
    '$$rAF',
    function ($window, $rootScope, $compile, $q, $templateCache, $http, $animate, $timeout, dimensions, $$rAF) {
      var trim = String.prototype.trim;
      var isTouch = 'createTouch' in $window.document;
      var htmlReplaceRegExp = /ng-bind="/gi;
      function TooltipFactory(element, config) {
        var $tooltip = {};
        // Common vars
        var nodeName = element[0].nodeName.toLowerCase();
        var options = $tooltip.$options = angular.extend({}, defaults, config);
        $tooltip.$promise = fetchTemplate(options.template);
        var scope = $tooltip.$scope = options.scope && options.scope.$new() || $rootScope.$new();
        if (options.delay && angular.isString(options.delay)) {
          options.delay = parseFloat(options.delay);
        }
        // Support scope as string options
        if (options.title) {
          $tooltip.$scope.title = options.title;
        }
        // Provide scope helpers
        scope.$hide = function () {
          scope.$$postDigest(function () {
            $tooltip.hide();
          });
        };
        scope.$show = function () {
          scope.$$postDigest(function () {
            $tooltip.show();
          });
        };
        scope.$toggle = function () {
          scope.$$postDigest(function () {
            $tooltip.toggle();
          });
        };
        $tooltip.$isShown = scope.$isShown = false;
        // Private vars
        var timeout, hoverState;
        // Support contentTemplate option
        if (options.contentTemplate) {
          $tooltip.$promise = $tooltip.$promise.then(function (template) {
            var templateEl = angular.element(template);
            return fetchTemplate(options.contentTemplate).then(function (contentTemplate) {
              var contentEl = findElement('[ng-bind="content"]', templateEl[0]);
              if (!contentEl.length)
                contentEl = findElement('[ng-bind="title"]', templateEl[0]);
              contentEl.removeAttr('ng-bind').html(contentTemplate);
              return templateEl[0].outerHTML;
            });
          });
        }
        // Fetch, compile then initialize tooltip
        var tipLinker, tipElement, tipTemplate, tipContainer;
        $tooltip.$promise.then(function (template) {
          if (angular.isObject(template))
            template = template.data;
          if (options.html)
            template = template.replace(htmlReplaceRegExp, 'ng-bind-html="');
          template = trim.apply(template);
          tipTemplate = template;
          tipLinker = $compile(template);
          $tooltip.init();
        });
        $tooltip.init = function () {
          // Options: delay
          if (options.delay && angular.isNumber(options.delay)) {
            options.delay = {
              show: options.delay,
              hide: options.delay
            };
          }
          // Replace trigger on touch devices ?
          // if(isTouch && options.trigger === defaults.trigger) {
          //   options.trigger.replace(/hover/g, 'click');
          // }
          // Options : container
          if (options.container === 'self') {
            tipContainer = element;
          } else if (options.container) {
            tipContainer = findElement(options.container);
          }
          // Options: trigger
          var triggers = options.trigger.split(' ');
          angular.forEach(triggers, function (trigger) {
            if (trigger === 'click') {
              element.on('click', $tooltip.toggle);
            } else if (trigger !== 'manual') {
              element.on(trigger === 'hover' ? 'mouseenter' : 'focus', $tooltip.enter);
              element.on(trigger === 'hover' ? 'mouseleave' : 'blur', $tooltip.leave);
              nodeName === 'button' && trigger !== 'hover' && element.on(isTouch ? 'touchstart' : 'mousedown', $tooltip.$onFocusElementMouseDown);
            }
          });
          // Options: show
          if (options.show) {
            scope.$$postDigest(function () {
              options.trigger === 'focus' ? element[0].focus() : $tooltip.show();
            });
          }
        };
        $tooltip.destroy = function () {
          // Unbind events
          var triggers = options.trigger.split(' ');
          for (var i = triggers.length; i--;) {
            var trigger = triggers[i];
            if (trigger === 'click') {
              element.off('click', $tooltip.toggle);
            } else if (trigger !== 'manual') {
              element.off(trigger === 'hover' ? 'mouseenter' : 'focus', $tooltip.enter);
              element.off(trigger === 'hover' ? 'mouseleave' : 'blur', $tooltip.leave);
              nodeName === 'button' && trigger !== 'hover' && element.off(isTouch ? 'touchstart' : 'mousedown', $tooltip.$onFocusElementMouseDown);
            }
          }
          // Remove element
          if (tipElement) {
            tipElement.remove();
            tipElement = null;
          }
          // Destroy scope
          scope.$destroy();
        };
        $tooltip.enter = function () {
          clearTimeout(timeout);
          hoverState = 'in';
          if (!options.delay || !options.delay.show) {
            return $tooltip.show();
          }
          timeout = setTimeout(function () {
            if (hoverState === 'in')
              $tooltip.show();
          }, options.delay.show);
        };
        $tooltip.show = function () {
          scope.$emit(options.prefixEvent + '.show.before', $tooltip);
          var parent = options.container ? tipContainer : null;
          var after = options.container ? null : element;
          // Hide any existing tipElement
          if (tipElement)
            tipElement.remove();
          // Fetch a cloned element linked from template
          tipElement = $tooltip.$element = tipLinker(scope, function (clonedElement, scope) {
          });
          // Set the initial positioning.
          tipElement.css({
            top: '0px',
            left: '0px',
            display: 'block'
          }).addClass(options.placement);
          // Options: animation
          if (options.animation)
            tipElement.addClass(options.animation);
          // Options: type
          if (options.type)
            tipElement.addClass(options.prefixClass + '-' + options.type);
          $animate.enter(tipElement, parent, after, function () {
            scope.$emit(options.prefixEvent + '.show', $tooltip);
          });
          $tooltip.$isShown = scope.$isShown = true;
          scope.$$phase || scope.$root.$$phase || scope.$digest();
          $$rAF($tooltip.$applyPlacement);
          // var a = bodyEl.offsetWidth + 1; ?
          // Bind events
          if (options.keyboard) {
            if (options.trigger !== 'focus') {
              $tooltip.focus();
              tipElement.on('keyup', $tooltip.$onKeyUp);
            } else {
              element.on('keyup', $tooltip.$onFocusKeyUp);
            }
          }
        };
        $tooltip.leave = function () {
          clearTimeout(timeout);
          hoverState = 'out';
          if (!options.delay || !options.delay.hide) {
            return $tooltip.hide();
          }
          timeout = setTimeout(function () {
            if (hoverState === 'out') {
              $tooltip.hide();
            }
          }, options.delay.hide);
        };
        $tooltip.hide = function (blur) {
          if (!$tooltip.$isShown)
            return;
          scope.$emit(options.prefixEvent + '.hide.before', $tooltip);
          $animate.leave(tipElement, function () {
            scope.$emit(options.prefixEvent + '.hide', $tooltip);
          });
          $tooltip.$isShown = scope.$isShown = false;
          scope.$$phase || scope.$root.$$phase || scope.$digest();
          // Unbind events
          if (options.keyboard && tipElement !== null) {
            tipElement.off('keyup', $tooltip.$onKeyUp);
          }
          // Allow to blur the input when hidden, like when pressing enter key
          if (blur && options.trigger === 'focus') {
            return element[0].blur();
          }
        };
        $tooltip.toggle = function () {
          $tooltip.$isShown ? $tooltip.leave() : $tooltip.enter();
        };
        $tooltip.focus = function () {
          tipElement[0].focus();
        };
        // Protected methods
        $tooltip.$applyPlacement = function () {
          if (!tipElement)
            return;
          // Get the position of the tooltip element.
          var elementPosition = getPosition();
          // Get the height and width of the tooltip so we can center it.
          var tipWidth = tipElement.prop('offsetWidth'), tipHeight = tipElement.prop('offsetHeight');
          // Get the tooltip's top and left coordinates to center it with this directive.
          var tipPosition = getCalculatedOffset(options.placement, elementPosition, tipWidth, tipHeight);
          // Now set the calculated positioning.
          tipPosition.top += 'px';
          tipPosition.left += 'px';
          tipElement.css(tipPosition);
        };
        $tooltip.$onKeyUp = function (evt) {
          evt.which === 27 && $tooltip.hide();
        };
        $tooltip.$onFocusKeyUp = function (evt) {
          evt.which === 27 && element[0].blur();
        };
        $tooltip.$onFocusElementMouseDown = function (evt) {
          evt.preventDefault();
          evt.stopPropagation();
          // Some browsers do not auto-focus buttons (eg. Safari)
          $tooltip.$isShown ? element[0].blur() : element[0].focus();
        };
        // Private methods
        function getPosition() {
          if (options.container === 'body') {
            return dimensions.offset(element[0]);
          } else {
            return dimensions.position(element[0]);
          }
        }
        function getCalculatedOffset(placement, position, actualWidth, actualHeight) {
          var offset;
          var split = placement.split('-');
          switch (split[0]) {
          case 'right':
            offset = {
              top: position.top + position.height / 2 - actualHeight / 2,
              left: position.left + position.width
            };
            break;
          case 'bottom':
            offset = {
              top: position.top + position.height,
              left: position.left + position.width / 2 - actualWidth / 2
            };
            break;
          case 'left':
            offset = {
              top: position.top + position.height / 2 - actualHeight / 2,
              left: position.left - actualWidth
            };
            break;
          default:
            offset = {
              top: position.top - actualHeight,
              left: position.left + position.width / 2 - actualWidth / 2
            };
            break;
          }
          if (!split[1]) {
            return offset;
          }
          // Add support for corners @todo css
          if (split[0] === 'top' || split[0] === 'bottom') {
            switch (split[1]) {
            case 'left':
              offset.left = position.left;
              break;
            case 'right':
              offset.left = position.left + position.width - actualWidth;
            }
          } else if (split[0] === 'left' || split[0] === 'right') {
            switch (split[1]) {
            case 'top':
              offset.top = position.top - actualHeight;
              break;
            case 'bottom':
              offset.top = position.top + position.height;
            }
          }
          return offset;
        }
        return $tooltip;
      }
      // Helper functions
      function findElement(query, element) {
        return angular.element((element || document).querySelectorAll(query));
      }
      function fetchTemplate(template) {
        return $q.when($templateCache.get(template) || $http.get(template)).then(function (res) {
          if (angular.isObject(res)) {
            $templateCache.put(template, res.data);
            return res.data;
          }
          return res;
        });
      }
      return TooltipFactory;
    }
  ];
}).directive('bsTooltip', [
  '$window',
  '$location',
  '$sce',
  '$tooltip',
  '$$rAF',
  function ($window, $location, $sce, $tooltip, $$rAF) {
    return {
      restrict: 'EAC',
      scope: true,
      link: function postLink(scope, element, attr, transclusion) {
        // Directive options
        var options = { scope: scope };
        angular.forEach([
          'template',
          'contentTemplate',
          'placement',
          'container',
          'delay',
          'trigger',
          'keyboard',
          'html',
          'animation',
          'type'
        ], function (key) {
          if (angular.isDefined(attr[key]))
            options[key] = attr[key];
        });
        // Observe scope attributes for change
        angular.forEach(['title'], function (key) {
          attr[key] && attr.$observe(key, function (newValue, oldValue) {
            scope[key] = $sce.trustAsHtml(newValue);
            angular.isDefined(oldValue) && $$rAF(function () {
              tooltip && tooltip.$applyPlacement();
            });
          });
        });
        // Support scope as an object
        attr.bsTooltip && scope.$watch(attr.bsTooltip, function (newValue, oldValue) {
          if (angular.isObject(newValue)) {
            angular.extend(scope, newValue);
          } else {
            scope.title = newValue;
          }
          angular.isDefined(oldValue) && $$rAF(function () {
            tooltip && tooltip.$applyPlacement();
          });
        }, true);
        // Initialize popover
        var tooltip = $tooltip(element, options);
        // Garbage collection
        scope.$on('$destroy', function () {
          tooltip.destroy();
          options = null;
          tooltip = null;
        });
      }
    };
  }
]);

// Source: typeahead.js
angular.module('mgcrea.ngStrap.typeahead', [
  'mgcrea.ngStrap.tooltip',
  'mgcrea.ngStrap.helpers.parseOptions'
]).provider('$typeahead', function () {
  var defaults = this.defaults = {
      animation: 'am-fade',
      prefixClass: 'typeahead',
      placement: 'bottom-left',
      template: 'typeahead/typeahead.tpl.html',
      trigger: 'focus',
      container: false,
      keyboard: true,
      html: false,
      delay: 0,
      minLength: 1,
      filter: 'filter',
      limit: 6
    };
  this.$get = [
    '$window',
    '$rootScope',
    '$tooltip',
    function ($window, $rootScope, $tooltip) {
      var bodyEl = angular.element($window.document.body);
      function TypeaheadFactory(element, controller, config) {
        var $typeahead = {};
        // Common vars
        var options = angular.extend({}, defaults, config);
        $typeahead = $tooltip(element, options);
        var parentScope = config.scope;
        var scope = $typeahead.$scope;
        scope.$resetMatches = function () {
          scope.$matches = [];
          scope.$activeIndex = 0;
        };
        scope.$resetMatches();
        scope.$activate = function (index) {
          scope.$$postDigest(function () {
            $typeahead.activate(index);
          });
        };
        scope.$select = function (index, evt) {
          scope.$$postDigest(function () {
            $typeahead.select(index);
          });
        };
        scope.$isVisible = function () {
          return $typeahead.$isVisible();
        };
        // Public methods
        $typeahead.update = function (matches) {
          scope.$matches = matches;
          if (scope.$activeIndex >= matches.length) {
            scope.$activeIndex = 0;
          }
        };
        $typeahead.activate = function (index) {
          scope.$activeIndex = index;
        };
        $typeahead.select = function (index) {
          var value = scope.$matches[index].value;
          controller.$setViewValue(value);
          scope.$resetMatches();
          controller.$render();
          if (parentScope)
            parentScope.$digest();
          // Emit event
          scope.$emit('$typeahead.select', value, index);
        };
        // Protected methods
        $typeahead.$isVisible = function () {
          if (!options.minLength || !controller) {
            return !!scope.$matches.length;
          }
          // minLength support
          return scope.$matches.length && angular.isString(controller.$viewValue) && controller.$viewValue.length >= options.minLength;
        };
        $typeahead.$getIndex = function (value) {
          var l = scope.$matches.length, i = l;
          if (!l)
            return;
          for (i = l; i--;) {
            if (scope.$matches[i].value === value)
              break;
          }
          if (i < 0)
            return;
          return i;
        };
        $typeahead.$onMouseDown = function (evt) {
          // Prevent blur on mousedown
          evt.preventDefault();
          evt.stopPropagation();
        };
        $typeahead.$onKeyDown = function (evt) {
          if (!/(38|40|13)/.test(evt.keyCode))
            return;
          evt.preventDefault();
          evt.stopPropagation();
          // Select with enter
          if (evt.keyCode === 13 && scope.$matches.length) {
            $typeahead.select(scope.$activeIndex);
          }  // Navigate with keyboard
          else if (evt.keyCode === 38 && scope.$activeIndex > 0)
            scope.$activeIndex--;
          else if (evt.keyCode === 40 && scope.$activeIndex < scope.$matches.length - 1)
            scope.$activeIndex++;
          else if (angular.isUndefined(scope.$activeIndex))
            scope.$activeIndex = 0;
          scope.$digest();
        };
        // Overrides
        var show = $typeahead.show;
        $typeahead.show = function () {
          show();
          setTimeout(function () {
            $typeahead.$element.on('mousedown', $typeahead.$onMouseDown);
            if (options.keyboard) {
              element.on('keydown', $typeahead.$onKeyDown);
            }
          });
        };
        var hide = $typeahead.hide;
        $typeahead.hide = function () {
          $typeahead.$element.off('mousedown', $typeahead.$onMouseDown);
          if (options.keyboard) {
            element.off('keydown', $typeahead.$onKeyDown);
          }
          hide();
        };
        return $typeahead;
      }
      TypeaheadFactory.defaults = defaults;
      return TypeaheadFactory;
    }
  ];
}).directive('bsTypeahead', [
  '$window',
  '$parse',
  '$q',
  '$typeahead',
  '$parseOptions',
  function ($window, $parse, $q, $typeahead, $parseOptions) {
    var defaults = $typeahead.defaults;
    return {
      restrict: 'EAC',
      require: 'ngModel',
      link: function postLink(scope, element, attr, controller) {
        // Directive options
        var options = { scope: scope };
        angular.forEach([
          'placement',
          'container',
          'delay',
          'trigger',
          'keyboard',
          'html',
          'animation',
          'template',
          'filter',
          'limit',
          'minLength'
        ], function (key) {
          if (angular.isDefined(attr[key]))
            options[key] = attr[key];
        });
        // Build proper ngOptions
        var filter = options.filter || defaults.filter;
        var limit = options.limit || defaults.limit;
        var ngOptions = attr.ngOptions;
        if (filter)
          ngOptions += ' | ' + filter + ':$viewValue';
        if (limit)
          ngOptions += ' | limitTo:' + limit;
        var parsedOptions = $parseOptions(ngOptions);
        // Initialize typeahead
        var typeahead = $typeahead(element, controller, options);
        // Watch model for changes
        scope.$watch(attr.ngModel, function (newValue, oldValue) {
          // console.warn('$watch', element.attr('ng-model'), newValue);
          scope.$modelValue = newValue;
          // Publish modelValue on scope for custom templates
          parsedOptions.valuesFn(scope, controller).then(function (values) {
            if (values.length > limit)
              values = values.slice(0, limit);
            // Do not re-queue an update if a correct value has been selected
            if (values.length === 1 && values[0].value === newValue)
              return;
            typeahead.update(values);
            // Queue a new rendering that will leverage collection loading
            controller.$render();
          });
        });
        // Model rendering in view
        controller.$render = function () {
          // console.warn('$render', element.attr('ng-model'), 'controller.$modelValue', typeof controller.$modelValue, controller.$modelValue, 'controller.$viewValue', typeof controller.$viewValue, controller.$viewValue);
          if (controller.$isEmpty(controller.$viewValue))
            return element.val('');
          var index = typeahead.$getIndex(controller.$modelValue);
          var selected = angular.isDefined(index) ? typeahead.$scope.$matches[index].label : controller.$viewValue;
          selected = angular.isObject(selected) ? selected.label : selected;
          element.val(selected.replace(/<(?:.|\n)*?>/gm, '').trim());
        };
        // Garbage collection
        scope.$on('$destroy', function () {
          typeahead.destroy();
          options = null;
          typeahead = null;
        });
      }
    };
  }
]);

})(window, document);
