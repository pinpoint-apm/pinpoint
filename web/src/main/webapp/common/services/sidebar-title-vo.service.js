(function() {
	'use strict';
	/**
	 * (en)SidebarTitleVoService 
	 * @ko SidebarTitleVoService
	 * @group Service
	 * @name SidebarTitleVoService
	 * @class
	 */
	pinpointApp.factory( "SidebarTitleVoService", [ "PreferenceService", function ( PreferenceService ) {
	    return function () {
			var iconPath = PreferenceService.getIconPath();
	        var self = this;
	
	        this._sImage = false;
	        this._sImageType = false;
	        this._sTitle = false;
	        this._sImage2 = false;
	        this._sImageType2 = false;
	        this._sTitle2 = false;
	
	        this.setImageType = function (imageType) {
	            if (angular.isString(imageType)) {
	                self._sImageType = imageType;
	                self._sImage = self._parseImageTypeToImageUrl(imageType);
	            } else {
	                throw 'ImageType should be string in SidebarTitleVo';
	            }
	            return self;
	        };
	        this._parseImageTypeToImageUrl = function (imageType) {
	            if (angular.isString(imageType)) {
	                var imageUrl = "";
	                switch (imageType) {
	                    case "UNKNOWN_GROUP" :
							imageUrl = iconPath + "UNKNOWN.png";
	                        break;
	                    default :
							imageUrl = iconPath + imageType + '.png';
	                        break;
	                }
	                return imageUrl;
	            } else {
	                throw 'ImageType should be string in SidebarTitleVo';
	            }
	        };
	        this.getImageType = function () {
	            return self._sImageType;
	        };
	        this.getImage = function () {
	            return self._sImage;
	        };
	        this.setTitle = function (title) {
	            if (angular.isString(title)) {
	                self._sTitle = title;
	            } else {
	                throw 'Title should be string in SidebarTitleVo';
	            }
	            return self;
	        };
	        this.getTitle = function () {
	            return self._sTitle;
	        };
	
	        this.setImageType2 = function (imageType2) {
	            if (angular.isString(imageType2)) {
	                self._sImageType2 = imageType2;
	                self._sImage2 = self._parseImageTypeToImageUrl(imageType2);
	            } else {
	                throw 'ImageType2 should be string in SidebarTitleVo';
	            }
	            return self;
	        };
	        this.getImageType2 =function () {
	            return self._sImageType2;
	        };
	        this.getImage2 = function () {
	            return self._sImage2;
	        };
	        this.setTitle2 = function (title2) {
	            if (angular.isString(title2)) {
	                self._sTitle2 = title2;
	            } else {
	                throw 'Title2 should be string in SidebarTitleVo';
	            }
	            return self;
	        };
	        this.getTitle2 = function () {
	            return self._sTitle2;
	        };
	    };
	}]);
})();