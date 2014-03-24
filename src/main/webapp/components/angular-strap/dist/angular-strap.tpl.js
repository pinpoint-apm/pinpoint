/**
 * angular-strap
 * @version v2.0.0-rc.4 - 2014-03-07
 * @link http://mgcrea.github.io/angular-strap
 * @author Olivier Louvignes (olivier@mg-crea.com)
 * @license MIT License, http://www.opensource.org/licenses/MIT
 */
(function(window, document, undefined) {
'use strict';
// Source: dist/modules/alert.tpl.js
angular.module('mgcrea.ngStrap.alert').run(['$templateCache', function($templateCache) {
$templateCache.put('alert/alert.tpl.html',
    "<div class=\"alert alert-dismissable\" tabindex=\"-1\" ng-class=\"[type ? 'alert-' + type : null]\"><button type=\"button\" class=\"close\" ng-click=\"$hide()\">&times;</button> <strong ng-bind=\"title\"></strong>&nbsp;<span ng-bind-html=\"content\"></span></div>"
  );

}]);

// Source: dist/modules/aside.tpl.js
angular.module('mgcrea.ngStrap.aside').run(['$templateCache', function($templateCache) {
$templateCache.put('aside/aside.tpl.html',
    "<div class=\"aside\" tabindex=\"-1\" role=\"dialog\"><div class=\"aside-dialog\"><div class=\"aside-content\"><div class=\"aside-header\" ng-show=\"title\"><button type=\"button\" class=\"close\" ng-click=\"$hide()\">&times;</button><h4 class=\"aside-title\" ng-bind=\"title\"></h4></div><div class=\"aside-body\" ng-bind=\"content\"></div><div class=\"aside-footer\"><button type=\"button\" class=\"btn btn-default\" ng-click=\"$hide()\">Close</button></div></div></div></div>"
  );

}]);

// Source: dist/modules/datepicker.tpl.js
angular.module('mgcrea.ngStrap.datepicker').run(['$templateCache', function($templateCache) {
$templateCache.put('datepicker/datepicker.tpl.html',
    "<div class=\"dropdown-menu datepicker\" ng-class=\"'datepicker-mode-' + $mode\" style=\"max-width: 320px\"><table style=\"table-layout: fixed; height: 100%; width: 100%\"><thead><tr class=\"text-center\"><th><button tabindex=\"-1\" type=\"button\" class=\"btn btn-default pull-left\" ng-click=\"$selectPane(-1)\"><i class=\"glyphicon glyphicon-chevron-left\"></i></button></th><th colspan=\"{{ rows[0].length - 2 }}\"><button tabindex=\"-1\" type=\"button\" class=\"btn btn-default btn-block text-strong\" ng-click=\"$toggleMode()\"><strong style=\"text-transform: capitalize\" ng-bind=\"title\"></strong></button></th><th><button tabindex=\"-1\" type=\"button\" class=\"btn btn-default pull-right\" ng-click=\"$selectPane(+1)\"><i class=\"glyphicon glyphicon-chevron-right\"></i></button></th></tr><tr ng-show=\"labels\" ng-bind-html=\"labels\"></tr></thead><tbody><tr ng-repeat=\"(i, row) in rows\" height=\"{{ 100 / rows.length }}%\"><td class=\"text-center\" ng-repeat=\"(j, el) in row\"><button tabindex=\"-1\" type=\"button\" class=\"btn btn-default\" style=\"width: 100%\" ng-class=\"{'btn-primary': el.selected}\" ng-click=\"$select(el.date)\" ng-disabled=\"el.disabled\"><span ng-class=\"{'text-muted': el.muted}\" ng-bind=\"el.label\"></span></button></td></tr></tbody></table></div>"
  );

}]);

// Source: dist/modules/dropdown.tpl.js
angular.module('mgcrea.ngStrap.dropdown').run(['$templateCache', function($templateCache) {
$templateCache.put('dropdown/dropdown.tpl.html',
    "<ul tabindex=\"-1\" class=\"dropdown-menu\" role=\"menu\"><li role=\"presentation\" ng-class=\"{divider: item.divider}\" ng-repeat=\"item in content\"><a role=\"menuitem\" tabindex=\"-1\" ng-href=\"{{item.href}}\" ng-if=\"!item.divider && item.href\" ng-bind=\"item.text\"></a> <a role=\"menuitem\" tabindex=\"-1\" href=\"javascript:void(0)\" ng-if=\"!item.divider && item.click\" ng-click=\"$eval(item.click);$hide()\" ng-bind=\"item.text\"></a></li></ul>"
  );

}]);

// Source: dist/modules/modal.tpl.js
angular.module('mgcrea.ngStrap.modal').run(['$templateCache', function($templateCache) {
$templateCache.put('modal/modal.tpl.html',
    "<div class=\"modal\" tabindex=\"-1\" role=\"dialog\"><div class=\"modal-dialog\"><div class=\"modal-content\"><div class=\"modal-header\" ng-show=\"title\"><button type=\"button\" class=\"close\" ng-click=\"$hide()\">&times;</button><h4 class=\"modal-title\" ng-bind=\"title\"></h4></div><div class=\"modal-body\" ng-bind=\"content\"></div><div class=\"modal-footer\"><button type=\"button\" class=\"btn btn-default\" ng-click=\"$hide()\">Close</button></div></div></div></div>"
  );

}]);

// Source: dist/modules/popover.tpl.js
angular.module('mgcrea.ngStrap.popover').run(['$templateCache', function($templateCache) {
$templateCache.put('popover/popover.tpl.html',
    "<div class=\"popover\"><div class=\"arrow\"></div><h3 class=\"popover-title\" ng-bind=\"title\" ng-show=\"title\"></h3><div class=\"popover-content\" ng-bind=\"content\"></div></div>"
  );

}]);

// Source: dist/modules/select.tpl.js
angular.module('mgcrea.ngStrap.select').run(['$templateCache', function($templateCache) {
$templateCache.put('select/select.tpl.html',
    "<ul tabindex=\"-1\" class=\"select dropdown-menu\" ng-show=\"$isVisible()\" role=\"select\"><li role=\"presentation\" ng-repeat=\"match in $matches\" ng-class=\"{active: $isActive($index)}\"><a style=\"cursor: default\" role=\"menuitem\" tabindex=\"-1\" ng-click=\"$select($index, $event)\"><span ng-bind=\"match.label\"></span> <i class=\"glyphicon glyphicon-ok pull-right\" ng-if=\"$isMultiple && $isActive($index)\"></i></a></li></ul>"
  );

}]);

// Source: dist/modules/tab.tpl.js
angular.module('mgcrea.ngStrap.tab').run(['$templateCache', function($templateCache) {
$templateCache.put('tab/tab.tpl.html',
    "<ul class=\"nav nav-tabs\"><li ng-repeat=\"pane in panes\" ng-class=\"{active: $index == active}\"><a data-toggle=\"tab\" ng-click=\"setActive($index, $event)\" data-index=\"{{$index}}\">{{pane.title}}</a></li></ul><div class=\"tab-content\"><div ng-repeat=\"pane in panes\" class=\"tab-pane\" ng-class=\"[$index == active ? 'active' : '']\" ng-include=\"pane.template || '$pane'\"></div></div>"
  );

}]);

// Source: dist/modules/timepicker.tpl.js
angular.module('mgcrea.ngStrap.timepicker').run(['$templateCache', function($templateCache) {
$templateCache.put('timepicker/timepicker.tpl.html',
    "<div class=\"dropdown-menu timepicker\" style=\"min-width: 0px;width: auto\"><table height=\"100%\"><thead><tr class=\"text-center\"><th><button tabindex=\"-1\" type=\"button\" class=\"btn btn-default pull-left\" ng-click=\"$moveIndex(-1, 0)\"><i class=\"glyphicon glyphicon-chevron-up\"></i></button></th><th>&nbsp;</th><th><button tabindex=\"-1\" type=\"button\" class=\"btn btn-default pull-left\" ng-click=\"$moveIndex(-1, 1)\"><i class=\"glyphicon glyphicon-chevron-up\"></i></button></th></tr></thead><tbody><tr ng-repeat=\"(i, row) in rows\"><td class=\"text-center\"><button tabindex=\"-1\" style=\"width: 100%\" type=\"button\" class=\"btn btn-default\" ng-class=\"{'btn-primary': row[0].selected}\" ng-click=\"$select(row[0].date, 0)\" ng-disabled=\"row[0].disabled\"><span ng-class=\"{'text-muted': row[0].muted}\" ng-bind=\"row[0].label\"></span></button></td><td><span ng-bind=\"i == midIndex ? ':' : ' '\"></span></td><td class=\"text-center\"><button tabindex=\"-1\" ng-if=\"row[1].date\" style=\"width: 100%\" type=\"button\" class=\"btn btn-default\" ng-class=\"{'btn-primary': row[1].selected}\" ng-click=\"$select(row[1].date, 1)\" ng-disabled=\"row[1].disabled\"><span ng-class=\"{'text-muted': row[1].muted}\" ng-bind=\"row[1].label\"></span></button></td><td ng-if=\"showAM\">&nbsp;</td><td ng-if=\"showAM\"><button tabindex=\"-1\" ng-show=\"i == midIndex - !isAM * 1\" style=\"width: 100%\" type=\"button\" ng-class=\"{'btn-primary': !!isAM}\" class=\"btn btn-default\" ng-click=\"$switchMeridian()\" ng-disabled=\"el.disabled\">AM</button> <button tabindex=\"-1\" ng-show=\"i == midIndex + 1 - !isAM * 1\" style=\"width: 100%\" type=\"button\" ng-class=\"{'btn-primary': !isAM}\" class=\"btn btn-default\" ng-click=\"$switchMeridian()\" ng-disabled=\"el.disabled\">PM</button></td></tr></tbody><tfoot><tr class=\"text-center\"><th><button tabindex=\"-1\" type=\"button\" class=\"btn btn-default pull-left\" ng-click=\"$moveIndex(1, 0)\"><i class=\"glyphicon glyphicon-chevron-down\"></i></button></th><th>&nbsp;</th><th><button tabindex=\"-1\" type=\"button\" class=\"btn btn-default pull-left\" ng-click=\"$moveIndex(1, 1)\"><i class=\"glyphicon glyphicon-chevron-down\"></i></button></th></tr></tfoot></table></div>"
  );

}]);

// Source: dist/modules/tooltip.tpl.js
angular.module('mgcrea.ngStrap.tooltip').run(['$templateCache', function($templateCache) {
$templateCache.put('tooltip/tooltip.tpl.html',
    "<div class=\"tooltip in\" ng-show=\"title\"><div class=\"tooltip-arrow\"></div><div class=\"tooltip-inner\" ng-bind=\"title\"></div></div>"
  );

}]);

// Source: dist/modules/typeahead.tpl.js
angular.module('mgcrea.ngStrap.typeahead').run(['$templateCache', function($templateCache) {
$templateCache.put('typeahead/typeahead.tpl.html',
    "<ul tabindex=\"-1\" class=\"typeahead dropdown-menu\" ng-show=\"$isVisible()\" role=\"select\"><li role=\"presentation\" ng-repeat=\"match in $matches\" ng-class=\"{active: $index == $activeIndex}\"><a role=\"menuitem\" tabindex=\"-1\" ng-click=\"$select($index, $event)\" ng-bind=\"match.label\"></a></li></ul>"
  );

}]);

})(window, document);
