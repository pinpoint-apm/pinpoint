/**
 * angular-strap
 * @version v2.0.0-rc.4 - 2014-03-07
 * @link http://mgcrea.github.io/angular-strap
 * @author Olivier Louvignes (olivier@mg-crea.com)
 * @license MIT License, http://www.opensource.org/licenses/MIT
 */
angular.module('mgcrea.ngStrap.tooltip').run(['$templateCache', function($templateCache) {
  'use strict';

  $templateCache.put('tooltip/tooltip.tpl.html',
    "<div class=\"tooltip in\" ng-show=\"title\"><div class=\"tooltip-arrow\"></div><div class=\"tooltip-inner\" ng-bind=\"title\"></div></div>"
  );

}]);
