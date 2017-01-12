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
		"hideClass": "hide-me"
	});
	
	pinpointApp.service( "AlarmUtilService", [ "AlarmUtilServiceConfig", "$timeout", "AlarmAjaxService", "SystemConfigurationService", function ( $config, $timeout, $ajaxService, SystemConfigService ) {
		var self = this;
		this.show = function( $el ) {
			$el.removeClass( $config.hideClass );
		};
		this.hide = function() {
			for( var i = 0 ; i < arguments.length ; i++ ) {
				arguments[i].addClass( $config.hideClass );
			}
		};
		this.sendCRUD = function( funcName, data, successCallback, failCallback ) {
			if ( ( angular.isUndefined( data ) || data === "" ) ) {
				data = {
					"userId" : ( SystemConfigService.get("userId") || "" )
				}; 
			}

			$timeout(function() {
				$ajaxService[funcName](data, function (resultData) {
					if (resultData.errorCode || resultData.status) {
						failCallback(resultData);
					} else {
						successCallback(resultData);
					}
				});
			});
		};
		this.setTotal = function( $elTotal, n ) {
			$elTotal.html( "(" + n + ")");
		};
		this.hasDuplicateItem = function( list, func ) {
			var len = list.length;
			var has = false;
			for( var i = 0 ; i < len ; i++ ) {
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
		this.getNode = function( $event, tagName ) {
			return $( $event.toElement || $event.target ).parents( tagName );
		};
	}]);
})(jQuery);