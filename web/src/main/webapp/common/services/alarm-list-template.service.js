(function($) {
	'use strict';
	
	/**
	 * (en) Alarm 설정의 공통된 리스트 구현 코드를 공유함. 
	 * @ko Alarm 설정의 공통된 리스트 구현 코드를 공유함.
	 * @group Service
	 * @name AlarmListTemplateService
	 * @class
	 */	
	pinpointApp.constant('AlarmListTemplateServiceConfig', {
		"hideClass": "hide-me",
		"hasNotEditClass": "has-not-edit"
	});
	
	pinpointApp.service('AlarmListTemplateService', [ 'AlarmListTemplateServiceConfig', 'AlarmAjaxService', function ($config, $ajaxService) {
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
		this.sendCRUD = function( funcName, data, successCallback, $elAlert ) {
			$ajaxService[funcName]( data, function( resultData ) {
				if ( resultData.errorCode ) {
					self.showAlert( $elAlert, resultData.errorMessage );
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
		this.isNotValidElement = function( el, tagName, className ) {
			console.log( el, tagName );
			if ( arguments.length == 2 ) {
				return el.tagName != tagName;
			} else {
				return el.tagName != tagName && $(el).hasClass( className ) == false;
			}
		};
		this.setGuideEvent = function( scope, $el, guideData ) {
			for( var i = 0 ; i < guideData.length ; i++ ) {
				(function( selector, messageName ) {
					$el.on("mouseenter", selector, function() {
						scope.onEnter(messageName);
					}).on("mouseleave", selector, function() {
						scope.onLeave(messageName);
					})
				})(guideData[i].selector, guideData[i].name)
			}
		};
		this.setGuide = function( $elGuideMessage, listType, messageType, length ) {
			var message = "";
			if ( arguments.length == 2 ) {
				// arguments is "$elGuideMessage" and "message"
				$elGuideMessage.html( arguments[1] );
				return;
			}
			if ( listType == "userGroup" ) {
				switch( messageType ) {
				case "title":
					message = "'" + length + "'은 생성된 사용자 그룹의 수입니다.";	
					break;
				case "refresh":
					message = "사용자 그룹 목록을 갱신합니다.";	
					break;
				case "create":
					message = "새로운 사용자 그룹을 생성합니다.";
					break;
				case "filterInput":
					message = "찾으려는 사용자 그룹을 입력하세요.";
					break;	
				case "filterSearch":
					message = "원하는 사용자 그룹을 검색하세요.";
					break;
				case "filterEmpty":
					message = "검색어를 삭제하고 모든 사용자 그룹을 표시합니다.";
					break;
				case "contents":
					message = "더블 클릭하면 수정할 수 있습니다.";
					break;
				case "remove":
					message = "사용자 그룹을 삭제합니다.";
					break;
				case "removeCancel":
					message = "삭제를 취소합니다.";
					break;
				case "removeConfirm":
					message = "사용자 그룹을 삭제합니다.";
					break;	
				case "notEmpty":
					message = "공백은 입력 할 수 없습니다.";
					break;
				case "greater2":
					message = "3글자 이상 입력하세요.";
					break;
				default:
					message = "사용자 그룹 목록입니다.";
					break;	
				}
			} else if ( listType == "pinpointUser" ) {
				switch( messageType ) {
				case "title":
					message = "'" + length + "'은 Pinpoint에 등록된 사용자의 수입니다.";	
					break;
				case "refresh":
					message = "사용자 목록을 갱신합니다.";	
					break;
				case "create":
					message = "새로운 사용자를 생성합니다.";
					break;
				case "filterInput":
					message = "찾으려는 사용자를 입력하세요.";
					break;	
				case "filterSearch":
					message = "원하는 사용자를 검색하세요.";
					break;
				case "filterEmpty":
					message = "검색어를 삭제하고 모든 사용자를 표시합니다.";
					break;
				case "contents":
					message = "더블 클릭하면 수정할 수 있습니다.";
					break;
				case "remove":
					message = "사용자를 삭제합니다.";
					break;
				case "removeCancel":
					message = "삭제를 취소합니다.";
					break;
				case "removeConfirm":
					message = "사용자를 삭제합니다.";
					break;	
				case "notEmpty":
					message = "공백은 입력 할 수 없습니다.";
					break;
				case "greater2":
					message = "3글자 이상 입력하세요.";
					break;
				default:
					message = "Pinpoint 사용자 목록입니다.";	
					break;
					
				}
			} else if ( listType == "rule" ) {
				switch( messageType ) {
				case "title":
					message = "'" + length + "'은 Pinpoint에 등록된 사용자의 수입니다.";	
					break;
				case "refresh":
					message = "사용자 목록을 갱신합니다.";	
					break;
				case "create":
					message = "새로운 사용자를 생성합니다.";
					break;
				case "filterInput":
					message = "찾으려는 사용자를 입력하세요.";
					break;	
				case "filterSearch":
					message = "원하는 사용자를 검색하세요.";
					break;
				case "filterEmpty":
					message = "검색어를 삭제하고 모든 사용자를 표시합니다.";
					break;
				case "contents":
					message = "더블 클릭하면 수정할 수 있습니다.";
					break;
				case "remove":
					message = "사용자를 삭제합니다.";
					break;
				case "removeCancel":
					message = "삭제를 취소합니다.";
					break;
				case "removeConfirm":
					message = "사용자를 삭제합니다.";
					break;	
				case "notEmpty":
					message = "공백은 입력 할 수 없습니다.";
					break;
				case "greater2":
					message = "3글자 이상 입력하세요.";
					break;
				default:
					message = "Alarm 설정된 룰 목록입니다.";
					break;
				}
			} else if ( listType == "groupMember" ) {
				switch( messageType ) {
				case "title":
					message = "'" + length + "'은 그룹에 등록된 사용자 수입니다.";	
					break;
				case "refresh":
					message = "멤버 목록을 갱신합니다.";	
					break;
				case "filterInput":
					message = "찾으려는 멤버를 입력하세요.";
					break;	
				case "filterSearch":
					message = "원하는 멤버를 검색하세요.";
					break;
				case "filterEmpty":
					message = "검색어를 삭제하고 모든 멤버를 표시합니다.";
					break;
				case "remove":
					message = "멤버를 삭제합니다.";
					break;
				case "removeCancel":
					message = "삭제를 취소합니다.";
					break;
				case "removeConfirm":
					message = "멤버를 삭제합니다.";
					break;	
				case "notEmpty":
					message = "공백은 입력 할 수 없습니다.";
					break;
				case "greater2":
					message = "3글자 이상 입력하세요.";
					break;
				default:
					message = "등록된 멤버 목록입니다.";
					break;
				}
			}
			$elGuideMessage.html(message);
		};
	}]);
})(jQuery);