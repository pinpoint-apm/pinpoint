'use strict';

angular.module('mgcrea.ngStrap.tab', [])

  .run(function($templateCache) {

    $templateCache.put('$pane', '{{pane.content}}');

  })

  .provider('$tab', function() {

    var defaults = this.defaults = {
      animation: 'am-fade',
      template: 'tab/tab.tpl.html'
    };

    this.$get = function() {
      return {defaults: defaults};
    };

  })

  .directive('bsTabs', function($window, $animate, $tab) {

    var defaults = $tab.defaults;

    return {
      restrict: 'EAC',
      scope: true,
      require: '?ngModel',
      templateUrl: function(element, attr) {
        return attr.template || defaults.template;
      },
      link: function postLink(scope, element, attr, controller) {

        // Directive options
        var options = defaults;
        angular.forEach(['animation'/*, 'template'*/], function(key) {
          if(angular.isDefined(attr[key])) options[key] = attr[key];
        });

        // Require scope as an object
        attr.bsTabs && scope.$watch(attr.bsTabs, function(newValue, oldValue) {
          scope.panes = newValue;
        }, true);

        // Add base class
        element.addClass('tabs');

        // Support animations
        if(options.animation) {
          element.addClass(options.animation);
        }

        scope.active = scope.activePane = 0;
        // view -> model
        scope.setActive = function(index, ev) {
          scope.active = index;
          if(controller) {
            controller.$setViewValue(index);
          }
        };

        // model -> view
        if(controller) {
          controller.$render = function() {
            scope.active = controller.$modelValue * 1;
          };
        }

      }
    };

  });
