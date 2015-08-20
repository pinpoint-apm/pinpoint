(function($) {
	'use strict';
	/**
	 * (en)alarmPinpointUserDirective 
	 * @ko alarmPinpointUserDirective
	 * @group Directive
	 * @name alarmPinpointUserDirective
	 * @class
	 */	
	
	pinpointApp.directive('alarmPinpointUserDirective', [ '$rootScope', 'helpContentTemplate', 'helpContentService', 'AlarmListTemplateService',
	    function ($rootScope, helpContentTemplate, helpContentService, $alarmListTemplateService) {
        return {
            restrict: 'EA',
            replace: true,
            templateUrl: 'features/configuration/alarm/alarmPinpointUser.html',
            scope: true,
            link: function (scope, element) {

            	var $element = $(element);
    			var $elWrapper = $element.find(".wrapper");
    			var $elTotal = $element.find(".total");
    			var $elLoading = $element.find(".some-loading");
    			var $elAlert = $element.find(".some-alert");
    			var $elGuideMessage = $element.find(".guide-message");
    			var $elFilterInput = $element.find("div.filter-input input");
    			var $elFilterEmpty = $element.find("div.filter-input button.trash");
    			var $elEdit = $element.find(".some-edit-content");
    			var $elEditInputUserID = $elEdit.find("input[name=userID]");
    			var $elEditInputName = $elEdit.find("input[name=name]");
    			var $elEditInputDepartment = $elEdit.find("input[name=department]");
    			var $elEditInputPhone = $elEdit.find("input[name=phone]");
    			var $elEditInputEmail = $elEdit.find("input[name=email]");
    			var $elEditGuide = $elEdit.find(".title-message");
    			var $removeTemplate = $([
    	           '<span class="right">',
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
    				var tagName = $event.toElement.tagName.toLowerCase();
    				var $target = $($event.toElement);
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
    			
    			
    			$alarmListTemplateService.setGuideEvent( scope, $elUL, [
                  { selector: "li", 					name: "contents" },
                  { selector: "span.contents", 			name: "contents" },
                  { selector: "span.remove", 			name: "remove" },
                  { selector: "button.confirm-cancel", 	name: "removeCancel" },
                  { selector: "button.confirm-remove", 	name: "removeConfirm" }
    			]);
    			
    			function moveUser( $el ) {
    				$alarmListTemplateService.showLoading( $elLoading, false );
    				addUserToGroup( $el.prop("id").split("_")[1] );
    			}
    			function removeConfirm( $el ) {
    				$alarmListTemplateService.showLoading( $elLoading, false );
    				removeUser( $el.prop("id").split("_")[1] );
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
    			function addUserToGroup( userID ) {
    				scope.$parent.$broadcast( "alarmGroupMember.configuration.addUser", searchUser( userID ) );
    			}
    			function updatedUser( oUser ) {
    				scope.$parent.$broadcast( "alarmGroupMember.configuration.updateUser", oUser );
    			}
    			function removedUser( userID ) {
    				scope.$parent.$broadcast( "alarmGroupMember.configuration.removeUser", userID );
    			}
    			function createUser( userID, userName, userDepartment, userPhone, userEmail ) {
    				var oNewUser = { 
    					"userId": userID,
    					"name": userName,
    					"department": userDepartment,
    					"phoneNumber": userPhone,
    					"email": userEmail
    				}; 
    				$alarmListTemplateService.sendCRUD( "createPinpointUser", oNewUser, function( resultData ) {
    					// @TODO
    					// 많이 쓰는 놈 기준 3개를 뽑아 내야 함.
    					pinpointUserList.push(oNewUser);
    					$alarmListTemplateService.setTotal( $elTotal, pinpointUserList.length );
    					$alarmListTemplateService.hide( $elLoading, $elEdit );
    				}, $elAlert );
    			}
    			function updateUser( userID, userName, userDepartment, userPhone, userEmail ) {
    				var oUpdateUser = { 
    					"userId": userID,
    					"name": userName,
    					"department": userDepartment,
    					"phoneNumber": userPhone,
    					"email": userEmail
    				}; 
    				$alarmListTemplateService.sendCRUD( "updatePinpointUser", oUpdateUser, function( resultData ) {
    					for( var i = 0 ; i < pinpointUserList.length ; i++ ) {
    						if ( pinpointUserList[i].userId == userID ) {
    							pinpointUserList[i].name = userName;
    							pinpointUserList[i].department = userDepartment;
    							pinpointUserList[i].phoneNumber = userPhone;
    							pinpointUserList[i].email = userEmail;
    						}
    					}
    					$alarmListTemplateService.hide( $elLoading, $elEdit );
    					updatedUser( oUpdateUser );
    					
    				}, $elAlert );
    			}
    			function removeUser( userID ) {
    				$alarmListTemplateService.sendCRUD( "removePinpointUser", { "userId": userID }, function( resultData ) {
    					scope.$apply(function() {
	    					for( var i = 0 ; i < pinpointUserList.length ; i++ ) {
	    						if ( pinpointUserList[i].userId == userID ) {
	    							pinpointUserList.splice(i, 1);
	    							break;
	    						}
	    					}
    					});
    					$alarmListTemplateService.setTotal( $elTotal, pinpointUserList.length );
    					$alarmListTemplateService.hide( $elLoading );
    					isRemoving = false;
    					removedUser( userID );
    				}, $elAlert );
    			}
    			function loadList( isFirst ) {
    				$alarmListTemplateService.sendCRUD( "getPinpointUserList", {}, function( resultData ) {
    					// @TODO
    					// 많이 쓰는 놈 기준 3개를 뽑아 내야 함.
    					isLoadedPinpointUserList = true;
    					pinpointUserList = scope.pinpointUserList = resultData;
    					$alarmListTemplateService.setTotal( $elTotal, pinpointUserList.length );
    					$alarmListTemplateService.hide( $elLoading );
    					scope.onLeave();
    				}, $elAlert );			
    			};
    			function loadGroupMember( userGroupID ) {
    				scope.$parent.$broadcast( "alarmGroupMember.configuration.load", userGroupID );
    			}
    			
    			scope.onRefresh = function() {
    				if ( isRemoving == true ) return;
    				
    				$elFilterInput.val("");
    				$alarmListTemplateService.showLoading( $elLoading, false );
    				loadList( false );
    			};
    			scope.onCreate = function() {
    				if ( isRemoving == true ) return;
    				
    				isCreate = true;
    				$elEditGuide.html( "Create new pinpoint user" );
    				$elEditInputUserID.prop("disabled", "");
    				$elEditInputUserID.val("");
    				$elEditInputName.val("");
    				$elEditInputDepartment.val("");
    				$elEditInputPhone.val("");
    				$elEditInputEmail.val("");
    				$alarmListTemplateService.show( $elEdit );
    				$elEditInputUserID.focus();
    			};
    			scope.onUpdate = function($event) {
    				if ( isRemoving == true ) return;
    				
    				isCreate = false;
    				var $el = $( $event.toElement ).parents("li");
    				var oUser = searchUser( $el.prop("id").split("_")[1] );
    				
    				$elEditGuide.html( "아래 항목을 수정할 수 있습니다." );
    				$elEditInputUserID.prop("disabled", "disabled");
    				$elEditInputUserID.val( oUser.userId );
    				$elEditInputName.val( oUser.name );
    				$elEditInputDepartment.val( oUser.department );
    				$elEditInputPhone.val( oUser.phoneNumber );
    				$elEditInputEmail.val( oUser.email );
    				$alarmListTemplateService.show( $elEdit );
    				$elEditInputName.focus().select();
    			};
    			scope.onInputFilter = function($event) {
    				if ( isRemoving == true ) return;
    				
    				if ( $event.keyCode == 13 ) { // Enter
    					scope.onFilterGroup();
    					return;
    				}
    				if ($.trim( $elFilterInput.val() ).length >= 3 ) {
    					$elFilterEmpty.removeClass("disabled");
    				} else {
    					$elFilterEmpty.addClass("disabled");
    				}
    			};
    			scope.onFilterGroup = function() {
    				if ( isRemoving == true ) return;
    				var query = $.trim( $elFilterInput.val() );
    				if ( query.length != 0 && query.length < 3 ) {
    					scope.onEnter("greater2");
    					return;
    				}
    				if ( query == "" ) {
    					if ( scope.pinpointUserList.length != pinpointUserList.length ) {
    						scope.pinpointUserList = pinpointUserList;
    						$alarmListTemplateService.unsetFilterBackground( $elWrapper );
    					}
    					$elFilterEmpty.addClass("disabled");
    				} else {
    					var newFilterUserGroup = [];
    					var length = pinpointUserList.length;
    					for( var i = 0 ; i < pinpointUserList.length ; i++ ) {
    						if ( pinpointUserList[i].name.indexOf( query ) != -1 || pinpointUserList[i].department.indexOf( query ) != -1 ) {
    							newFilterUserGroup.push( pinpointUserList[i] );
    						}
    					}
    					scope.pinpointUserList = newFilterUserGroup;
    					$alarmListTemplateService.setFilterBackground( $elWrapper );
    				}
    			};
    			scope.onFilterEmpty = function() {
    				if ( isRemoving == true ) return;
    				if ( $.trim( $elFilterInput.val() ) == "" ) return;
    				$elFilterInput.val("");
    				scope.onFilterGroup();
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
    				$alarmListTemplateService.hide( $elEdit );
    			};
    			scope.onApplyEdit = function() {
    				var userID = $.trim( $elEditInputUserID.val() );
    				var userName = $.trim( $elEditInputName.val() );
    				var userDepartment = $.trim( $elEditInputDepartment.val() );
    				var userPhone = $.trim( $elEditInputPhone.val() );
    				var userEmail = $.trim( $elEditInputEmail.val() );
    				
    				if ( userID == "" || userName == "" ) {
    					scope.onEnter("notEmpty");
    					return;
    				}
    				// TODO phone - 체크
    				// TODO email 포맷 체크
    				
    				$alarmListTemplateService.showLoading( $elLoading, true );
    				if ( $alarmListTemplateService.hasDuplicateItem( pinpointUserList, function( pinpointUser ) {
    					return pinpointUser.userId == userID;
    				}) && isCreate == true) {
    					$alarmListTemplateService.showAlert( $elAlert, "동일한 userID를 가진 사용자가 이미 있습니다." );
    					return;
    				}
    				if ( isCreate ) {
    					createUser( userID, userName, userDepartment, userPhone, userEmail );
    				} else {
    					updateUser( userID, userName, userDepartment, userPhone, userEmail );
    				}
    			};
    			scope.onCloseAlert = function() {
    				$alarmListTemplateService.closeAlert( $elAlert, $elLoading );
    			};
    			// onEnter, onLeave
    			scope.onEnter = function( type ) {
    				$alarmListTemplateService.setGuide( $elGuideMessage, "pinpointUser", type, pinpointUserList.length );
    			};
    			scope.onLeave = function( type ) {
    				$alarmListTemplateService.setGuide( $elGuideMessage, "pinpointUser", "" );
    			}
    			scope.$on("alarmPinpointUser.configuration.load", function() {
    				if ( isLoadedPinpointUserList === false ) {
    					loadList( true );
    				}
    			});
    			scope.$on("alarmPinpointUser.configuration.addUserCallback", function( event ) {
    				$alarmListTemplateService.hide( $elLoading );
    			});
            }
        };
    }]);
})(jQuery);