(function($) {
	'use strict';
	/**
	 * (en)alarmPinpointUserDirective 
	 * @ko alarmPinpointUserDirective
	 * @group Directive
	 * @name alarmPinpointUserDirective
	 * @class
	 */	
	
	pinpointApp.directive('alarmPinpointUserDirective', [ '$rootScope', '$timeout', 'helpContentTemplate', 'helpContentService', 'AlarmUtilService', 'AlarmBroadcastService', 'AnalyticsService', 'globalConfig',
	    function ($rootScope, helpContentTemplate, $timeout, helpContentService, alarmUtilService, alarmBroadcastService, analyticsService, globalConfig) {
        return {
            restrict: 'EA',
            replace: true,
            templateUrl: 'features/configuration/alarm/alarmPinpointUser.html?v=' + G_BUILD_TIME,
            scope: true,
            link: function (scope, element) {
            	var $element = $(element);
            	var $elCreatBtn = $element.find(".some-list-header button");
    			var $elWrapper = $element.find(".wrapper");
    			var $elEmpty = $element.find(".empty-list");
    			var $elTotal = $element.find(".total");
    			var $elLoading = $element.find(".some-loading");
    			var $elAlert = $element.find(".some-alert");
    			var $elSearchType = $element.find("select");
    			var $elSearchInput = $element.find("div.filter-input input");
    			var $elEdit = $element.find(".some-edit-content");
    			var $elEditInputUserID = $elEdit.find("input[name=userID]");
    			var $elEditInputName = $elEdit.find("input[name=name]");
    			var $elEditInputDepartment = $elEdit.find("input[name=department]");
    			var $elEditInputPhone = $elEdit.find("input[name=phone]");
    			var $elEditInputEmail = $elEdit.find("input[name=email]");
    			var $elEditGuide = $elEdit.find(".title-message");
    			var $removeTemplate = $([
    	           '<span class="position:absolute;right:0px">',
    	               '<button class="btn btn-danger confirm-cancel"><span class="glyphicon glyphicon-remove" aria-hidden="true"></span></button>',
    	               '<button class="btn btn-danger confirm-remove" style="margin-left:2px;"><span class="glyphicon glyphicon-ok" aria-hidden="true"></span></button>',
       			   '</span>'
    			].join(""));
    			
    			var isLoadedPinpointUserList = false;
    			var isCreate = true; // update
    			var isRemoving = false;
    			var pinpointUserList = scope.pinpointUserList = [];
    			
    			var $elUL = $element.find(".some-list-content ul");
    			$elUL.on("click", function( $event ) {
    				var $target = $( $event.toElement || $event.target );
    				var tagName = $target.get(0).tagName.toLowerCase();
    				var $li = $target.parents("li");
    				
    				if ( tagName == "button" ) {
    					if ( $target.hasClass("confirm-cancel") ) {
    						removeCancel( $li );
    					} else if ( $target.hasClass("confirm-cancel") ) {
    						removeConfirm( $li );
    					} else if ( $target.hasClass("move") ) {
    						moveUser( $li );
    					} else if ( $target.hasClass("edit-user") ) {
    						scope.onUpdate( $event );
    					}
    				} else if ( tagName == "span" ) {
    					if ( $target.hasClass("remove") ) {
    						if ( isRemoving === true ) return;
    	    				isRemoving = true;
    	    				$li.addClass("remove").find("span.remove").hide().end().find("button.move").addClass("disabled").end().append($removeTemplate);
    					} else if ( $target.hasClass("contents") ) {
    					} else if( $target.hasClass("glyphicon-edit") ) {
    						scope.onUpdate( $event );
    					} else if ( $target.hasClass("glyphicon-remove") ) {
    						removeCancel( $li );
    					} else if ( $target.hasClass("glyphicon-ok") ) {
    						removeConfirm( $li );
    					} else if ( $target.hasClass("glyphicon-chevron-left") ) {
    						moveUser( $li );
    					}
    				}
    			});
    			
    			function reset() {
    				scope.onCancelEdit();
    				alarmUtilService.unsetFilterBackground( $elWrapper );
    				$elSearchInput.val("");
    			}
    			function moveUser( $el ) {
    				alarmUtilService.showLoading( $elLoading, false );
    				alarmBroadcastService.sendUserAdd( searchUser( alarmUtilService.extractID( $el ) ) );
    			}
    			function removeConfirm( $el ) {
    				alarmUtilService.showLoading( $elLoading, false );
    				removeUser( alarmUtilService.extractID( $el ) );
    			}
    			function removeCancel( $el ) {
    				$el
						.find("span.right").remove().end()
						.find("span.remove").show().end()
						.find("button.move").removeClass("disabled").end()
						.removeClass("remove");
					isRemoving = false;
    			}
    			
    			function searchUser( userID ) {
    				var oUser;
    				var len = pinpointUserList.length;
    				for( var i = 0 ; i < len ; i++ ) {
    					if ( pinpointUserList[i].userId == userID ) {
    						oUser = pinpointUserList[i];
    						break;
    					}
    				}
    				return oUser;
    			}
    			function createUser( userID, userName, userDepartment, userPhone, userEmail ) {
    				var oNewUser = { 
    					"userId": userID,
    					"name": userName,
    					"department": userDepartment,
    					"phoneNumber": userPhone,
    					"email": userEmail
    				}; 
    				alarmUtilService.sendCRUD( "createPinpointUser", oNewUser, function( resultData ) {
    					alarmUtilService.hide( $elLoading, $elEdit );
    					$elSearchType.val( "userName" );
    					$elSearchInput.val( userName );
    					scope.onSearch();
    				}, function(errorData) {}, $elAlert );
    			}
    			function updateUser( userID, userName, userDepartment, userPhone, userEmail ) {
    				var oUpdateUser = { 
    					"userId": userID,
    					"name": userName,
    					"department": userDepartment,
    					"phoneNumber": userPhone,
    					"email": userEmail
    				}; 
    				alarmUtilService.sendCRUD( "updatePinpointUser", oUpdateUser, function( resultData ) {
    					for( var i = 0 ; i < pinpointUserList.length ; i++ ) {
    						if ( pinpointUserList[i].userId == userID ) {
    							pinpointUserList[i].name = userName;
    							pinpointUserList[i].department = userDepartment;
    							pinpointUserList[i].phoneNumber = userPhone;
    							pinpointUserList[i].email = userEmail;
    						}
    					}
    					alarmUtilService.hide( $elLoading, $elEdit );
    					alarmBroadcastService.sendUserUpdated( oUpdateUser );
    				}, function( errorData ) {}, $elAlert );
    			}
    			function removeUser( userID ) {
    				alarmUtilService.sendCRUD( "removePinpointUser", { "userId": userID }, function( resultData ) {
    					scope.$apply(function() {
	    					for( var i = 0 ; i < pinpointUserList.length ; i++ ) {
	    						if ( pinpointUserList[i].userId == userID ) {
	    							pinpointUserList.splice(i, 1);
	    							break;
	    						}
	    					}
    					});
    					alarmUtilService.setTotal( $elTotal, pinpointUserList.length );
    					alarmUtilService.hide( $elLoading );
    					isRemoving = false;
    					alarmBroadcastService.sendUserRemoved( userID );
    				}, function( errorData ) {}, $elAlert );
    			}
    			function loadList( oParam ) {
    				alarmUtilService.sendCRUD( "getPinpointUserList", oParam || {}, function( resultData ) {
    					pinpointUserList = scope.pinpointUserList = resultData;
    					alarmUtilService.setTotal( $elTotal, pinpointUserList.length );
    					alarmUtilService.hide( $elLoading );
    				}, function( errorData ) {}, $elAlert );			
    			}
    			function validateEmail( email ) {
    				var reg = /^(([^<>()[\]\.,;:\s@\"]+(\.[^<>()[\]\.,;:\s@\"]+)*)|(\".+\"))@(([^<>()[\]\.,;:\s@\"]+\.)+[^<>()[\]\.,;:\s@\"]{2,})$/i;
    			    return reg.test(email);
    			}
    			function validatePhone( phone ) {
    				var reg = /^\d+$/;
    			    return reg.test(phone);
    			}
    			
    			scope.isAllowedCreate = globalConfig.editUserInfo;
    			scope.onCreate = function() {
    				if ( isRemoving === true ) return;
    				
    				isCreate = true;
    				$elEditGuide.html( "Create new pinpoint user" );
    				$elEditInputUserID.prop("disabled", "");
    				$elEditInputUserID.val("");
    				$elEditInputName.val("");
    				$elEditInputDepartment.val("");
    				$elEditInputPhone.val("");
    				$elEditInputEmail.val("");
    				alarmUtilService.show( $elEdit );
    				$elEditInputUserID.focus();
    			};
    			scope.onUpdate = function($event) {
    				if ( isRemoving === true ) return;
    				
    				isCreate = false;
    				var $el = $( $event.toElement || $event.target ).parents("li");
    				var oUser = searchUser( alarmUtilService.extractID( $el ) );
    				
    				$elEditGuide.html( "Update pinpoint user data." );
    				$elEditInputUserID.prop("disabled", "disabled");
    				$elEditInputUserID.val( oUser.userId );
    				$elEditInputName.val( oUser.name );
    				$elEditInputDepartment.val( oUser.department );
    				$elEditInputPhone.val( oUser.phoneNumber );
    				$elEditInputEmail.val( oUser.email );
    				alarmUtilService.show( $elEdit );
    				$elEditInputName.focus().select();
    			};
    			scope.onInputSearch = function($event) {
    				if ( isRemoving === true ) return;
    				
    				if ( $event.keyCode == 13 ) { // Enter
    					scope.onSearch();
    					return;
    				}
    			};
    			scope.onSearch = function() {
    				if ( isRemoving === true ) return;
    				var searchType = $elSearchType.val();
    				var query = $.trim( $elSearchInput.val() );
    				if ( query.length !== 0 && query.length < 3 ) {
    					alarmUtilService.showLoading( $elLoading, false );
    					alarmUtilService.showAlert( $elAlert, "You must enter at least three characters.");
    					return;
    				}
    				analyticsService.send( analyticsService.CONST.MAIN, analyticsService.CONST.CLK_ALARM_FILTER_PINPOINT_USER );
    				alarmUtilService.showLoading( $elLoading, false );
    				var oParam = {};
    				oParam[searchType] = query;
    				loadList( oParam );
    			};
    			scope.onInputEdit = function($event) {
    				if ( $event.keyCode == 13 ) { // Enter
    					scope.onApplyEdit();
    				} else if ( $event.keyCode == 27 ) { // ESC
    					scope.onCancelEdit();
    					$event.stopPropagation();
    				}
    			};
    			scope.onCancelEdit = function() {
    				alarmUtilService.hide( $elEdit );
    			};
    			scope.onApplyEdit = function() {
    				var userID = $.trim( $elEditInputUserID.val() );
    				var userName = $.trim( $elEditInputName.val() );
    				var userDepartment = $.trim( $elEditInputDepartment.val() );
    				var userPhone = $.trim( $elEditInputPhone.val() );
    				var userEmail = $.trim( $elEditInputEmail.val() );
    				
    				if ( userID === "" || userName === "" ) {
    					alarmUtilService.showLoading( $elLoading, true );
    					alarmUtilService.showAlert( $elAlert, "You must input user id and user name.");	
    					return;
    				}
    				alarmUtilService.showLoading( $elLoading, true );
    				if ( alarmUtilService.hasDuplicateItem( pinpointUserList, function( pinpointUser ) {
    					return pinpointUser.userId == userID;
    				}) && isCreate === true) {
    					alarmUtilService.showAlert( $elAlert, "Exist a same user id in the lists." );
    					return;
    				}
    				if ( validatePhone( userPhone ) === false ) {
    					alarmUtilService.showAlert( $elAlert, "You can only input numbers." );
    					return;
    				}
    				if ( validateEmail( userEmail ) === false ) {
    					alarmUtilService.showAlert( $elAlert, "Invalid email format." );
    					return;
    				}
    				if ( isCreate ) {
    					analyticsService.send( analyticsService.CONST.MAIN, analyticsService.CONST.CLK_ALARM_CREATE_PINPOINT_USER );
    					createUser( userID, userName, userDepartment, userPhone, userEmail );
    				} else {
    					updateUser( userID, userName, userDepartment, userPhone, userEmail );
    				}
    			};
    			scope.onCloseAlert = function() {
    				alarmUtilService.closeAlert( $elAlert, $elLoading );
    			};
    			scope.$on("alarmPinpointUser.configuration.load", function( event, department ) {
    				if ( isLoadedPinpointUserList === false ) {
    					loadList( department === "" ? {} : {
    						department: department
    					});
    				}
    			});
    			scope.$on("alarmPinpointUser.configuration.addUserCallback", function( event ) {
    				alarmUtilService.hide( $elLoading );
    			});
            }
        };
    }]);
})(jQuery);