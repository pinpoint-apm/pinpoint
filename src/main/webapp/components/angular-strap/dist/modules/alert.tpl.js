/**
 * angular-strap
 * @version v2.0.0-rc.4 - 2014-03-07
 * @link http://mgcrea.github.io/angular-strap
 * @author Olivier Louvignes (olivier@mg-crea.com)
 * @license MIT License, http://www.opensource.org/licenses/MIT
 */
angular.module('mgcrea.ngStrap.alert').run(['$templateCache', function($templateCache) {
  'use strict';

  $templateCache.put('alert/alert.tpl.html',
    "<div class=\"alert alert-dismissable\" tabindex=\"-1\" ng-class=\"[type ? 'alert-' + type : null]\"><button type=\"button\" class=\"close\" ng-click=\"$hide()\">&times;</button> <strong ng-bind=\"title\"></strong>&nbsp;<span ng-bind-html=\"content\"></span></div>"
  );

}]);
