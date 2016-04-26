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
				var $elSearch =  $element.find(".some-list-search input");
				var $workingNode = null;
				var aEditNode = $element.find(".new-group").toArray().map(function( el ) {
					return $(el);
				});

	  			var bIsLoaded = false;
    			var oPinpointUserList = [];
				var oGroupMemberList = [];
				scope.pinpointUserList = [];
				scope.bIsAllowedCreate = globalConfig.editUserInfo;

				function cancelPreviousWork() {
					AddPinpointUser.cancelAction( hideEditArea );
					RemovePinpointUser.cancelAction( alarmUtilService, $workingNode );
				}
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
				function showAddArea() {
					var $ul = $elWrapper.find("ul");
					var len = aEditNode.length - 1;
					for( var i = len ; i >= 0 ; i-- ) {
						$ul.prepend( aEditNode[i] );
					}
					aEditNode[0].find("input").attr("disabled", "");
					alarmUtilService.hide( aEditNode[len].find( CONSTS.DIV_EDIT ) );
					alarmUtilService.show( aEditNode[len].find( CONSTS.DIV_ADD ) );
					$.each( aEditNode, function( index, $el ) {
						alarmUtilService.show( $el );
					});
					aEditNode[0].focus();
				}
				function showEditArea( oPinpointUser ) {
					var len = aEditNode.length - 1;
					for( var i = len ; i >= 0 ; i-- ) {
						$workingNode.after( aEditNode[i] );
					}
					aEditNode[0].find("input").val( oPinpointUser.userId );
					aEditNode[1].find("input").val( oPinpointUser.name );
					aEditNode[2].find("input").val( oPinpointUser.department );
					aEditNode[3].find("input").val( oPinpointUser.phoneNumber );
					aEditNode[4].find("input").val( oPinpointUser.email );
					aEditNode[0].find("input").attr("disabled", "disabled");

					alarmUtilService.hide( aEditNode[len].find( CONSTS.DIV_ADD ) );
					alarmUtilService.show( aEditNode[len].find( CONSTS.DIV_EDIT ) );
					$.each( aEditNode, function( index, $el ) {
						alarmUtilService.show( $el );
					});
					aEditNode[0].find("input").focus();
				}
				function hideEditArea() {
					$.each( aEditNode, function( index, $el ) {
						alarmUtilService.hide( $el );
						$el.find("input").val("");
					});
				}
				function getNewPinpointUser() {
					var userId = $.trim( aEditNode[0].find("input").val() );
					var userName = $.trim( aEditNode[1].find("input").val() );
					var userDepartment = $.trim( aEditNode[2].find("input").val() );
					var userPhone = $.trim( aEditNode[3].find("input").val() );
					var userEmail = $.trim( aEditNode[4].find("input").val() );

					var oPinpointUser = {
						"userId": userId,
						"name": userName,
						"department": userDepartment,
						"phoneNumber": userPhone,
						"email": userEmail
					};
					return oPinpointUser;
				}
				function validateEmail( email ) {
					var reg = /^(([^<>()[\]\.,;:\s@\"]+(\.[^<>()[\]\.,;:\s@\"]+)*)|(\".+\"))@(([^<>()[\]\.,;:\s@\"]+\.)+[^<>()[\]\.,;:\s@\"]{2,})$/i;
					return reg.test(email);
				}
				function validatePhone( phone ) {
					var reg = /^\d+$/;
					return reg.test(phone);
				}
				function isSameNode( $current ) {
					return alarmUtilService.extractID( $workingNode ) === alarmUtilService.extractID( $current );
				}
				function searchPinpointUser( userId ) {
					for( var i = 0 ; i < oPinpointUserList.length ; i++ ) {
						if ( oPinpointUserList[i].userId === userId ) {
							return oPinpointUserList[i];
						}
					}
					return null;
				}
				// add
				scope.onAddPinpointUser = function() {
					if ( AddPinpointUser.isOn() ) {
						return;
					}
					cancelPreviousWork();
					AddPinpointUser.onAction( function() {
						showAddArea();
					});
				};
				scope.onCancelAddPinpointUser = function() {
					AddPinpointUser.cancelAction( function() {
						hideEditArea();
					});
				};
				scope.onApplyAddPinpointUser = function() {
					applyAddPinpointUser();
				};
				function applyAddPinpointUser() {
					AddPinpointUser.applyAction( alarmUtilService, getNewPinpointUser(), $elLoading, function( oNewPinpointUser  ) {
						analyticsService.send( analyticsService.CONST.MAIN, analyticsService.CONST.CLK_ALARM_CREATE_PINPOINT_USER );
						oPinpointUserList.push( oNewPinpointUser );
						scope.pinpointUserList = oPinpointUserList;
						hideEditArea();
						alarmUtilService.setTotal( $elTotal, oPinpointUserList.length );
					}, showAlert );
				}
				// remove
				scope.onRemovePinpointUser = function( $event ) {
					var $node = alarmUtilService.getNode( $event, "li" );
					if ( $workingNode !== null && isSameNode( $node ) === false ) {
						cancelPreviousWork( $node );
					}
					$workingNode = $node;
					RemovePinpointUser.onAction( alarmUtilService, $workingNode );
				};
				scope.onCancelRemovePinpointUser = function() {
					RemovePinpointUser.cancelAction( alarmUtilService, $workingNode );
				};
				scope.onApplyRemovePinpointUser = function() {
					RemovePinpointUser.applyAction( alarmUtilService, $workingNode, $elLoading, function( userId ) {
						for( var i = 0 ; i < oPinpointUserList.length ; i++ ) {
							if ( oPinpointUserList[i].userId == userId ) {
								oPinpointUserList.splice(i, 1);
								break;
							}
						}
						scope.pinpointUserList = oPinpointUserList;
						alarmUtilService.setTotal( $elTotal, oPinpointUserList.length );
						alarmBroadcastService.sendUserRemoved( userId );
					}, showAlert );
				};
				// update
				scope.onUpdatePinpointUser = function( $event ) {
					cancelPreviousWork();
					$workingNode = alarmUtilService.getNode( $event, "li" );
					UpdatePinpointUser.onAction( alarmUtilService, $workingNode, function( userId ) {
						showEditArea( searchPinpointUser( userId ) );
					});
				};
				scope.onCancelUpdatePinpointUser = function() {
					UpdatePinpointUser.cancelAction( alarmUtilService, $workingNode, hideEditArea );
				};
				scope.onApplyUpdatePinpointUser = function() {
					UpdatePinpointUser.applyAction( alarmUtilService, getNewPinpointUser(), $workingNode, $elLoading, function( oPinpointUser ) {

						for( var i = 0 ; i < oPinpointUserList.length ; i++ ) {
							if ( oPinpointUserList[i].userId == oPinpointUser.userId ) {
								oPinpointUserList[i].name = oPinpointUser.name;
								oPinpointUserList[i].department = oPinpointUser.department;
								oPinpointUserList[i].phoneNumber = oPinpointUser.phone;
								oPinpointUserList[i].email = oPinpointUser.email;
								break;
							}
						}
						scope.pinpointUserList = oPinpointUserList;
						alarmBroadcastService.sendUserUpdated( oPinpointUser );
					}, showAlert );
				};

				scope.onSearchKeydown = function( $event ) {
					if ( $event.keyCode == 13 ) { // Enter
						scope.onSearch();
					} else if ( $event.keyCode == 27 ) { // ESC
						$event.stopPropagation();
					}
				};
				scope.onSearch = function() {
					cancelPreviousWork();
					var query = $.trim( $elSearch.val() );

					if ( query.length < 3 ) {
						$elSearch.focus();
						return;
					}
					alarmUtilService.show( $elLoading );
					analyticsService.send( analyticsService.CONST.MAIN, analyticsService.CONST.CLK_ALARM_FILTER_PINPOINT_USER );
					loadData({ "userName": query });
					// { "department" :query }
				};
				scope.checkUser = function( $event ) {
					alarmUtilService.show( $elLoading );
					var $node = alarmUtilService.getNode( $event, "li" );
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
					alarmUtilService.setTotal( $elTotal, getTotal() );
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
					alarmUtilService.setTotal( $elTotal, getTotal() );
				});
				scope.$on("alarmPinpointUser.configuration.selectNone", function() {
					$elWrapper.addClass( "_disable-check" );
					$.each( oPinpointUserList, function( index, oPinpointUser ) {
						oPinpointUser.has = false;
					});
					oGroupMemberList = [];
					scope.pinpointUserList = oPinpointUserList;
					alarmUtilService.setTotal( $elTotal, getTotal() );
				});
				scope.$on("alarmPinpointUser.configuration.addUserCallback", function( event, list ) {
					if ( list.length > oGroupMemberList.length ) {
						//success
					} else {
						//fail
					}
					oGroupMemberList = list;
					alarmUtilService.setTotal( $elTotal, getTotal() );

					alarmUtilService.hide( $elLoading );
				});
				scope.onCloseAlert = function() {
					alarmUtilService.closeAlert( $elAlert, $elLoading );
				};
				scope.$on("alarmPinpointUser.configuration.load", function( event, department ) {
					cancelPreviousWork();
					if ( bIsLoaded === false ) {
						loadData( angular.isUndefined( department ) ? {} : { "department": department } );
					}
				});
            }
        };
    }]);

	var CONSTS = {
		INPUT_USERID_AND_NAME: "Input user id and name",
		INPUT_PHONE_OR_EMAIL: "Input phone number or email",
		YOU_CAN_ONLY_INPUT_NUMBERS: "You can only input numbers",
		INVALID_EMAIL_FORMAT: "Invalid email format.",
		DIV_NORMAL: "div._normal",
		DIV_REMOVE: "div._remove",
		DIV_ADD: "div._add",
		DIV_EDIT: "div._edit"
	};

	var AddPinpointUser = {
		_bIng: false,
		isOn: function() {
			return this._bIng;
		},
		onAction: function( cb ) {
			this._bIng = true;
			cb();
		},
		cancelAction: function( cbCancel ) {
			if ( this._bIng === true ) {
				cbCancel();
				this._bIng = false;
			}
		},
		applyAction: function( alarmUtilService, oNewPinpointUser, $elLoading, cbSuccess, cbFail ) {
			var self = this;
			alarmUtilService.show( $elLoading );
			if ( oNewPinpointUser.userId === "" || oNewPinpointUser.name === "" ) {
				cbFail({ errorMessage: CONSTS.INPUT_USERID_AND_NAME });
				return;
			}
			if ( oNewPinpointUser.phoneNumber === "" && oNewPinpointUser.email === "" ) {
				cbFail({ errorMessage: CONSTS.INPUT_PHONE_OR_EMAIL });
				return;
			}
			if ( oNewPinpointUser.phoneNumber !== "" && validatePhone( oNewPinpointUser.phoneNumber ) ) {
				cbFail({ errorMessage: CONSTS.YOU_CAN_ONLY_INPUT_NUMBERS });
				return;
			}
			if ( oNewPinpointUser.email !== "" && validateEmail( oNewPinpointUser.email ) ) {
				cbFail({ errorMessage: CONSTS.INVALID_EMAIL_FORMAT });
				return;
			}
			
			alarmUtilService.sendCRUD( "createPinpointUser", oNewPinpointUser, function( oServerData ) {
				oNewPinpointUser.number = oServerData.number;
				cbSuccess( oNewPinpointUser );
				self.cancelAction( function() {} );
				alarmUtilService.hide( $elLoading );
			}, function( oServerError ) {
				cbFail( oServerError );
			});
		}
	};
	var RemovePinpointUser = {
		_bIng: false,
		isOn: function () {
			return this._bIng;
		},
		onAction: function ( alarmUtilService, $node ) {
			this._bIng = true;
			$node.addClass("remove");
			alarmUtilService.hide( $node.find( CONSTS.DIV_NORMAL ) );
			alarmUtilService.show( $node.find( CONSTS.DIV_REMOVE ) );
		},
		cancelAction: function ( alarmUtilService, $node ) {
			if ( this._bIng === true ) {
				$node.removeClass("remove");
				alarmUtilService.hide($node.find( CONSTS.DIV_REMOVE ));
				alarmUtilService.show($node.find( CONSTS.DIV_NORMAL ));
				this._bIng = false;
			}
		},
		applyAction: function (alarmUtilService, $node, $elLoading, cbSuccess, cbFail) {
			var self = this;
			var userId = alarmUtilService.extractID( $node );
			alarmUtilService.sendCRUD( "removePinpointUser", { "userId": userId }, function( oServerData ) {
				cbSuccess( userId );
				self.cancel( alarmUtilService, $node );
				alarmUtilService.hide( $elLoading );
			}, function( oServerError ) {
				cbFail( oServerError );
			});
		}
	};
	var UpdatePinpointUser = {
		_bIng: false,
		isOn: function () {
			return this._bIng;
		},
		onAction: function ( alarmUtilService, $node, cb ) {
			this._bIng = true;
			alarmUtilService.hide( $node );
			cb( alarmUtilService.extractID( $node ) );
		},
		cancelAction: function( alarmUtilService, $node, cbCancel ) {
			if ( this._bIng === true ) {
				cbCancel();
				alarmUtilService.show( $node );
				this._bIng = false;
			}
		},
		applyAction: function (alarmUtilService, oPinpointUser, $node, $elLoading, cbSuccess, cbFail) {
			var self = this;
			alarmUtilService.show($elLoading);

			if ( oPinpointUser.name === "" ) {
				cbFail({ errorMessage: CONSTS.INPUT_USERID_AND_NAME });
				return;
			}
			if ( oPinpointUser.phoneNumber === "" && oPinpointUser.email === "" ) {
				cbFail({ errorMessage: CONSTS.INPUT_PHONE_OR_EMAIL });
				return;
			}
			if ( oPinpointUser.phoneNumber !== "" && validatePhone( oPinpointUser.phoneNumber ) ) {
				cbFail({ errorMessage: CONSTS.YOU_CAN_ONLY_INPUT_NUMBERS });
				return;
			}
			if ( oPinpointUser.email !== "" && validateEmail( oPinpointUser.email ) ) {
				cbFail({ errorMessage: CONSTS.INVALID_EMAIL_FORMAT });
				return;
			}

			alarmUtilService.sendCRUD( "updatePinpointUser", oPinpointUser, function( oServerData ) {
				self.cancelAction( alarmUtilService, $node, function () {});
				cbSuccess( oPinpointUser );
				alarmUtilService.hide($elLoading);
			}, function( oServerError ) {
				cbFail( oServerError );
			} );
		}
	}
})(jQuery);