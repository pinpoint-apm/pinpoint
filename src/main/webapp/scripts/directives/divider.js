'use strict';

pinpointApp
  .directive('divider', ['$rootScope', function ($rootScope) {
    return {
      restrict: 'EA',
      replace: true,
      templateUrl: 'views/divider.html', 
      link: function postLink(scope, element, attrs) {
      	scope.onoff = 'divider-open';
      	scope.toggle = function(){
      		if(scope.onoff === 'divider-open'){
      			scope.onoff = 'divider-closed';
      			$rootScope.$broadcast('dividerClosed');
      		}else{
      			scope.onoff = 'divider-open';
      			$rootScope.$broadcast('dividerOpen');
      		}
      	};
      }
    };
  }]);
