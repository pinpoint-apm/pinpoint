(function() {
	"use strict";
	pinpointApp.factory( "SidebarTitleVoService", [ "PreferenceService", function ( PreferenceService ) {
	    return function () {
			var iconPath = PreferenceService.getIconPath();
	        this._sImage = "";
	        this._sTitle = "";
	        this._sImage2 = "";
	        this._sTitle2 = "";

	        this.setImageType = function (imageType) {
				this._sImage = this._parseImageTypeToImageUrl(imageType);
	            return this;
	        };
	        this._parseImageTypeToImageUrl = function (imageType) {
	        	if ( /.*\_GROUP/.test( imageType ) ) {
					return iconPath + imageType.split("_")[0] + ".png";
				} else {
					return iconPath + imageType + ".png";
				}
	        };
	        this.getImage = function () {
	            return this._sImage;
	        };
	        this.setTitle = function (title) {
				this._sTitle = title.replace( "_", " " );
	            return this;
	        };
	        this.getTitle = function () {
	            return this._sTitle;
	        };
	
	        this.setImageType2 = function (imageType2) {
				this._sImage2 = this._parseImageTypeToImageUrl(imageType2);
	            return this;
	        };
	        this.getImage2 = function () {
	            return this._sImage2;
	        };
	        this.setTitle2 = function (title2) {
				this._sTitle2 = title2;
	            return this;
	        };
	        this.getTitle2 = function () {
	            return this._sTitle2;
	        };
	    };
	}]);
})();