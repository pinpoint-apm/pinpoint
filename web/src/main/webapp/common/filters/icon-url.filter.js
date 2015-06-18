(function() {
	'use strict';
	
	angular.module('pinpointApp').filter('iconUrl', function () {
		return function(name) {
			var imageUrl = "/images/icons/";
			if (angular.isString(name)) {
	            switch (name) {
	                case 'UNKNOWN_GROUP' :
	                    imageUrl += 'UNKNOWN.png';
	                    break;
	                default :
	                    imageUrl += name + '.png';
	                    break;
	            }
	            return imageUrl;
	        } else {
	            return "";
	        }			
		}
	});
})();