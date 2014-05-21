/**
 * angular-strap
 * @version v2.0.2 - 2014-04-27
 * @link http://mgcrea.github.io/angular-strap
 * @author Olivier Louvignes (olivier@mg-crea.com)
 * @license MIT License, http://www.opensource.org/licenses/MIT
 */
'use strict';
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