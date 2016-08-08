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
	 */
	angular.module( "pinpointApp" ).filter( "iconUrl", "PreferenceService", function ( PreferenceService ) {
		return function( name ) {
			var iconPath = PreferenceService.getIconPath();
			var imageUrl = "";
			if ( angular.isString( name ) ) {
	            switch (name) {
	                case "UNKNOWN_GROUP" :
	                    imageUrl = iconPath + "UNKNOWN.png";
	                    break;
	                default :
	                    imageUrl = iconPath + name + ".png";
	                    break;
	            }
	            return imageUrl;
	        } else {
	            return "";
	        }			
		};
	});
})();