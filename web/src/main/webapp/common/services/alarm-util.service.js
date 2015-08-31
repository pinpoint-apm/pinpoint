(function($) {
	'use strict';
	
	/**
	 * (en) Alarm 설정의 공통된 리스트 구현 코드를 공유함. 
	 * @ko Alarm 설정의 공통된 리스트 구현 코드를 공유함.
	 * @group Service
	 * @name AlarmUtilService
	 * @class
	 */	
	pinpointApp.constant('AlarmUtilServiceConfig', {
		"hideClass": "hide-me",
		"hasNotEditClass": "has-not-edit"
	});
	
	pinpointApp.service('AlarmUtilService', [ 'AlarmUtilServiceConfig', 'AlarmAjaxService', function ($config, $ajaxService) {
		var self = this;
		this.show = function( $el ) {
			$el.removeClass( $config.hideClass );
		};
		this.hide = function() {
			for( var i = 0 ; i < arguments.length ; i++ ) {
				arguments[i].addClass( $config.hideClass );
			}
		};
		this.showLoading = function( $elLoading, isEdit ) {
			$elLoading[ isEdit ? "removeClass" : "addClass" ]( $config.hasNotEditClass );
			$elLoading.removeClass( $config.hideClass );
		};
		this.showAlert = function( $elAlert, message ) {
			$elAlert.find(".message").html( message ).end().removeClass( $config.hideClass ).animate({
				height: 300
			}, 500, function() {});
		};
		this.sendCRUD = function( funcName, data, successCallback, failCallback, $elAlert ) {
			$ajaxService[funcName]( data, function( resultData ) {
				if ( resultData.errorCode || resultData.status ) {
					self.showAlert( $elAlert, resultData.errorMessage || resultData.statusText );
					failCallback( resultData );
				} else {
					successCallback( resultData );
				}
			});
		};
		this.setTotal = function( $elTotal, n ) {
			$elTotal.html( "(" + n + ")");
		};
		this.setFilterBackground = function( $elWrapper ) {
			$elWrapper.css("background-color", "#FFFFF1");
		};
		this.unsetFilterBackground = function( $elWrapper ) {
			$elWrapper.css("background-color", "#FFF");
		};
		this.hasDuplicateItem = function( list, func ) {
			var len = list.length;
			var has = false;
			for( var i = 0 ; i < list.length ; i++ ) {
				if ( func( list[i] ) ) {
					has = true;
					break;
				}
			}
			return has;
		};
		this.closeAlert = function( $elAlert, $elLoading ) {
			$elAlert.animate({
				height: 50,
			}, 100, function() {
				self.hide( $elAlert, $elLoading );
			});
		};
		this.extractID = function( $el ) {
			return $el.prop("id").split("_")[1];
		};
	}]);
})(jQuery);