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
	
	pinpointApp.service( "AlarmUtilService", [ "AlarmUtilServiceConfig", "AlarmAjaxService", "globalConfig", function ( $config, $ajaxService, globalConfig ) {
		var self = this;
		this.show = function( $el ) {
			$el.removeClass( $config.hideClass );
		};
		this.hide = function() {
			for( var i = 0 ; i < arguments.length ; i++ ) {
				arguments[i].addClass( $config.hideClass );
			}
		};
		// this.showLoading = function( $elLoading, isEdit ) {
		// 	$elLoading[ isEdit ? "removeClass" : "addClass" ]( $config.hasNotEditClass );
		// 	$elLoading.removeClass( $config.hideClass );
		// };
		this.showAlert = function( $elAlert, message ) {
			$elAlert.find(".message").html( message ).end().removeClass( $config.hideClass ).animate({
				height: 300
			}, 500, function() {});
		};
		this.sendCRUD = function( funcName, data, successCallback, failCallback ) {
			if ( ( angular.isUndefined( data ) || data === "" ) ) {
				data = {
					"userId" : ( globalConfig.userId || "" )
				}; 
			}

			$ajaxService[funcName]( data, function( resultData ) {
				if ( resultData.errorCode || resultData.status ) {
					failCallback( resultData );
				} else {
					successCallback( resultData );
				}
			});
			/*
			switch( funcName ) {
				case "getGroupMemberListInGroup":
					successCallback([{
						memberId: 1,
						department: "Paas",
						name: "정민우"
					},{
						memberId: 2,
						department: "Paas",
						name: "정현길"
					}]);
					break;
				case "removeMemberInGroup":
					successCallback();
					break;
				case "getUserGroupList":
					successCallback([{
						number: 1,
						id: "pinpoint-monitor-group"
					}, {
						number: 2,
						id: "pinpoint-dev-group"
					}]);
					break;
				case "getPinpointUserList":
					successCallback([{
						userId: 1,
						department: "PaaS",
						name: "김성관"
					},{
						userId: 2,
						department: "PaaS",
						name: "문성호"
					},{
						userId: 3,
						department: "PaaS",
						name: "송효종"
					},{
						userId: 4,
						department: "PaaS",
						name: "정민우"
					}]);
					break;
				case "createUserGroup":
					successCallback({
						id: data.id,
						number: parseInt( Math.random() * 10000 )
					});
					break;
				case "updateUserGroup":
					successCallback();
					break;
				case "removeUserGroup":
					successCallback();
					break;
				default:
					break;
			}
			*/
		};
		this.setTotal = function( $elTotal, n ) {
			$elTotal.html( "(" + n + ")");
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