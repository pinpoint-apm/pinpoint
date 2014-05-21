/**
 * angular-strap
 * @version v2.0.2 - 2014-04-27
 * @link http://mgcrea.github.io/angular-strap
 * @author Olivier Louvignes (olivier@mg-crea.com)
 * @license MIT License, http://www.opensource.org/licenses/MIT
 */
'use strict';
angular.module('mgcrea.ngStrap.popover').run([
  '$templateCache',
  function ($templateCache) {
    $templateCache.put('popover/popover.tpl.html', '<div class="popover"><div class="arrow"></div><h3 class="popover-title" ng-bind="title" ng-show="title"></h3><div class="popover-content" ng-bind="content"></div></div>');
  }
]);