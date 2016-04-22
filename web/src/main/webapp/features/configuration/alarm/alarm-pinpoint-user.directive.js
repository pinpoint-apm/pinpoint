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

				scope.prefix = "alarmPinpointUser_";
            	var $element = $(element);
    			var $elWrapper = $element.find(".wrapper");
    			var $elTotal = $element.find(".total");
    			var $elLoading = $element.find(".some-loading");
    			var $elAlert = $element.find(".some-alert");
	  			var bIsLoaded = false;
    			var bIsCreate = true; // update
    			var bIsRemoving = false;
    			var oPinpointUserList = [];
				var oGroupMemberList = [];
				scope.pinpointUserList = [];
				scope.isAllowedCreate = globalConfig.editUserInfo;

				function showAlert( oServerError ) {
					$elAlert.find( ".message" ).html( oServerError.errorMessage );
					alarmUtilService.hide( $elLoading );
					alarmUtilService.show( $elAlert );
				}
				function loadData( oParam ) {
					alarmUtilService.show( $elLoading );
					alarmUtilService.sendCRUD( "getPinpointUserList", oParam || {}, function( oServerData ) {
						$.each( oServerData, function( index, obj ) {
							obj["has"] = false;
						});
						oPinpointUserList = scope.pinpointUserList = oServerData;
						alarmUtilService.setTotal( $elTotal, getTotal() );
						alarmUtilService.hide( $elLoading );
					}, showAlert );
				}
				function getNode( $event ) {
					return $( $event.toElement || $event.target ).parents("li");
				}
				function getTotal() {
					return oGroupMemberList.length + "/" + oPinpointUserList.length;
				}
				function getUser( userId ) {
					for( var i = 0 ; i < oPinpointUserList.length ; i++ ) {
						if ( oPinpointUserList[i].userId == userId ) {
							return oPinpointUserList[i];
						}
					}
				}

				/*
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
								function createUser( userId, userName, userDepartment, userPhone, userEmail ) {
									var oNewUser = {
										"userId": userId,
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
								function updateUser( userId, userName, userDepartment, userPhone, userEmail ) {
									var oUpdateUser = {
										"userId": userId,
										"name": userName,
										"department": userDepartment,
										"phoneNumber": userPhone,
										"email": userEmail
									};
									alarmUtilService.sendCRUD( "updatePinpointUser", oUpdateUser, function( resultData ) {
										for( var i = 0 ; i < pinpointUserList.length ; i++ ) {
											if ( pinpointUserList[i].userId == userId ) {
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
								function removeUser( userId ) {
									alarmUtilService.sendCRUD( "removePinpointUser", { "userId": userId }, function( resultData ) {
										scope.$apply(function() {
											for( var i = 0 ; i < pinpointUserList.length ; i++ ) {
												if ( pinpointUserList[i].userId == userId ) {
													pinpointUserList.splice(i, 1);
													break;
												}
											}
										});
										alarmUtilService.setTotal( $elTotal, pinpointUserList.length );
										alarmUtilService.hide( $elLoading );
										isRemoving = false;
										alarmBroadcastService.sendUserRemoved( userId );
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
									var userId = $.trim( $elEditInputUserID.val() );
									var userName = $.trim( $elEditInputName.val() );
									var userDepartment = $.trim( $elEditInputDepartment.val() );
									var userPhone = $.trim( $elEditInputPhone.val() );
									var userEmail = $.trim( $elEditInputEmail.val() );

									if ( userId === "" || userName === "" ) {
										alarmUtilService.showLoading( $elLoading, true );
										alarmUtilService.showAlert( $elAlert, "You must input user id and user name.");
										return;
									}
									alarmUtilService.showLoading( $elLoading, true );
									if ( alarmUtilService.hasDuplicateItem( pinpointUserList, function( pinpointUser ) {
										return pinpointUser.userId == userId;
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
										createUser( userId, userName, userDepartment, userPhone, userEmail );
									} else {
										updateUser( userId, userName, userDepartment, userPhone, userEmail );
									}
								};

								*/
				scope.checkUser = function( $event ) {
					alarmUtilService.show( $elLoading );
					var $node = getNode( $event );
					var userId =  alarmUtilService.extractID( $node );
					if ( $node.find("input").get(0).checked ) {
						alarmBroadcastService.sendUserAdd( getUser( userId ) );
					} else {
						alarmBroadcastService.sendUserRemoved( userId );
					}
				};
				scope.$on("alarmPinpointUser.configuration.groupUserRemoved", function( event, list, userId ) {
					oGroupMemberList = list;
					$.each( oPinpointUserList, function( index, oPinpointUser ) {
						oPinpointUser.has = false;
						for( var i = 0 ; i < oGroupMemberList.length ; i++ ) {
							if ( oPinpointUser.userId == oGroupMemberList[i].memberId ) {
								oPinpointUser.has = true;
								break;
							}
						}
					});
					scope.$apply(function() {
						scope.pinpointUserList = oPinpointUserList;
					});
				});
				scope.$on("alarmPinpointUser.configuration.groupLoaded", function( event, list ) {
					$elWrapper.removeClass( "_disable-check" );
					oGroupMemberList = list;
					$.each( oPinpointUserList, function( index, oPinpointUser ) {
						oPinpointUser.has = false;
						for( var i = 0 ; i < oGroupMemberList.length ; i++ ) {
							if ( oPinpointUser.userId == oGroupMemberList[i].memberId ) {
								oPinpointUser.has = true;
								break;
							}
						}
					});
					scope.pinpointUserList = oPinpointUserList;
				});
				scope.$on("alarmPinpointUser.configuration.selectNone", function() {
					$elWrapper.addClass( "_disable-check" );
					$.each( oPinpointUserList, function( index, oPinpointUser ) {
						oPinpointUser.has = false;
					});
					oGroupMemberList = [];
					scope.pinpointUserList = oPinpointUserList;
				});
				scope.$on("alarmPinpointUser.configuration.addUserCallback", function( event, bSuccess ) {
					if ( bSuccess === false ) {

					}
					alarmUtilService.hide( $elLoading );
				});
				scope.onCloseAlert = function() {
					alarmUtilService.closeAlert( $elAlert, $elLoading );
				};
				scope.$on("alarmPinpointUser.configuration.load", function( event, department ) {
					if ( bIsLoaded === false ) {
						loadData( angular.isUndefined( department ) ? {} : { "department": department } );
					}
				});
            }
        };
    }]);
})(jQuery);