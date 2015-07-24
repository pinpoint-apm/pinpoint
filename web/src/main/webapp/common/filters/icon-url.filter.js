/**
 * @namespace
 */
(function() {
	'use strict';
	
	/**
	 * (en)icon의 URL 경로를 반환 함. 
	 * @ko icon의 URL 경로를 반환 함.
	 * @group Filter
	 * @name pinpointApp#iconUrl
	 * @method pinpointApp#iconUrl
	 * @param {String} iconName
	 * @return {String} icon Url
	 * @example 
	 * ```
	 * expect( iconUrl("TOMCAT") ).toEqual( "/images/icons/TOMCAT.png" );
	 * ```
	 */
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