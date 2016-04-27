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
				
				scope.getCreateAllow = function() {
					return globalConfig.editUserInfo;
				};
				function cancelPreviousWork() {
					AddPinpointUser.cancelAction( aEditNode, hideEditArea );
					RemovePinpointUser.cancelAction( alarmUtilService, $workingNode );
					UpdatePinpointUser.cancelAction( alarmUtilService, aEditNode, $workingNode, hideEditArea );
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
					aEditNode[0].find("input").removeAttr("disabled");
					alarmUtilService.hide( aEditNode[len].find( CONSTS.DIV_EDIT ) );
					alarmUtilService.show( aEditNode[len].find( CONSTS.DIV_ADD ) );
					$.each( aEditNode, function( index, $el ) {
						alarmUtilService.show( $el );
					});
					aEditNode[0].find("input").focus();
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
					AddPinpointUser.cancelAction( aEditNode, function() {
						hideEditArea();
					});
				};
				scope.onApplyAddPinpointUser = function() {
					applyAddPinpointUser();
				};
				function applyAddPinpointUser() {
					AddPinpointUser.applyAction( alarmUtilService, getNewPinpointUser(), aEditNode, $elLoading, function( oNewPinpointUser  ) {
						analyticsService.send( analyticsService.CONST.MAIN, analyticsService.CONST.CLK_ALARM_CREATE_PINPOINT_USER );
						oPinpointUserList.push( oNewPinpointUser );
						scope.pinpointUserList = oPinpointUserList;
						hideEditArea();
						alarmUtilService.setTotal( $elTotal, getTotal() );
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
						scope.$apply(function() {
							scope.pinpointUserList = oPinpointUserList;
						});
						alarmUtilService.setTotal( $elTotal, getTotal() );
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
					UpdatePinpointUser.cancelAction( alarmUtilService, aEditNode, $workingNode, hideEditArea );
				};
				scope.onApplyUpdatePinpointUser = function() {
					UpdatePinpointUser.applyAction( alarmUtilService, getNewPinpointUser(), aEditNode, $workingNode, $elLoading, function( oPinpointUser ) {

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
						hideEditArea();
						alarmBroadcastService.sendUserUpdated( oPinpointUser );
					}, showAlert );
				};

				scope.onEditPinpointUserKeydown = function( $event ) {
					if ( $event.keyCode == 13 ) { // Enter
						if ( AddPinpointUser.isOn() ) {
							scope.onApplyAddPinpointUser();
						} else {
							scope.onApplyUpdatePinpointUser();
						}
					} else if ( $event.keyCode == 27 ) { // ESC
						if ( AddPinpointUser.isOn() ) {
							scope.onCancelAddPinpointUser();
						} else {
							scope.onCancelUpdatePinpointUser();
						}
						$event.stopPropagation();
					}
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

					if ( query.length < CONSTS.MIN_SEARCH_LENGTH ) {
						$elSearch.focus();
						return;
					}
					alarmUtilService.show( $elLoading );
					analyticsService.send( analyticsService.CONST.MAIN, analyticsService.CONST.CLK_ALARM_FILTER_PINPOINT_USER );
					loadData({ "searchKey": query });
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
					resetList( list );
					scope.$apply(function() {
						scope.pinpointUserList = oPinpointUserList;
					});
					alarmUtilService.setTotal( $elTotal, getTotal() );
				});
				scope.$on("alarmPinpointUser.configuration.groupLoaded", function( event, list ) {
					$elWrapper.removeClass( "_disable-check" );
					console.log( list );
					resetList( list );
					scope.pinpointUserList = oPinpointUserList;
					alarmUtilService.setTotal( $elTotal, getTotal() );
				});
				function resetList( list ) {
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
				}
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
						loadData( angular.isUndefined( department ) ? {} : { "searchKey": department } );
					}
				});
            }
        };
    }]);

	var CONSTS = {
		MIN_SEARCH_LENGTH : 2,
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
		cancelAction: function( aEditNode, cbCancel ) {
			if ( this._bIng === true ) {
				removeBlink(aEditNode );
				cbCancel();
				this._bIng = false;
			}
		},
		applyAction: function( alarmUtilService, oNewPinpointUser, aEditNode, $elLoading, cbSuccess, cbFail ) {
			var self = this;
			alarmUtilService.show( $elLoading );
			if ( oNewPinpointUser.userId === "" || oNewPinpointUser.name === "" ) {
				addBlink( aEditNode );
				cbFail({ errorMessage: CONSTS.INPUT_USERID_AND_NAME });
				return;
			}
			if ( oNewPinpointUser.phoneNumber === "" && oNewPinpointUser.email === "" ) {
				addBlink( aEditNode );
				cbFail({ errorMessage: CONSTS.INPUT_PHONE_OR_EMAIL });
				return;
			}
			if ( oNewPinpointUser.phoneNumber !== "" && validatePhone( oNewPinpointUser.phoneNumber ) === false ) {
				addBlink( aEditNode );
				cbFail({ errorMessage: CONSTS.YOU_CAN_ONLY_INPUT_NUMBERS });
				return;
			}
			if ( oNewPinpointUser.email !== "" && validateEmail( oNewPinpointUser.email ) === false ) {
				addBlink( aEditNode );
				cbFail({ errorMessage: CONSTS.INVALID_EMAIL_FORMAT });
				return;
			}
			
			alarmUtilService.sendCRUD( "createPinpointUser", oNewPinpointUser, function( oServerData ) {
				oNewPinpointUser.number = oServerData.number;
				cbSuccess( oNewPinpointUser );
				self.cancelAction( aEditNode, function() {} );
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
				self.cancelAction( alarmUtilService, $node );
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
		cancelAction: function( alarmUtilService, aEditNode, $node, cbCancel ) {
			if ( this._bIng === true ) {
				cbCancel();
				removeBlink( aEditNode );
				alarmUtilService.show( $node );
				this._bIng = false;
			}
		},
		applyAction: function (alarmUtilService, oPinpointUser, aEditNode, $node, $elLoading, cbSuccess, cbFail) {
			var self = this;
			alarmUtilService.show($elLoading);

			if ( oPinpointUser.name === "" ) {
				addBlink( aEditNode );
				cbFail({ errorMessage: CONSTS.INPUT_USERID_AND_NAME });
				return;
			}
			if ( oPinpointUser.phoneNumber === "" && oPinpointUser.email === "" ) {
				addBlink( aEditNode );
				cbFail({ errorMessage: CONSTS.INPUT_PHONE_OR_EMAIL });
				return;
			}
			if ( oPinpointUser.phoneNumber !== "" && validatePhone( oPinpointUser.phoneNumber ) === false ) {
				addBlink( aEditNode );
				cbFail({ errorMessage: CONSTS.YOU_CAN_ONLY_INPUT_NUMBERS });
				return;
			}
			if ( oPinpointUser.email !== "" && validateEmail( oPinpointUser.email ) === false ) {
				addBlink( aEditNode );
				cbFail({ errorMessage: CONSTS.INVALID_EMAIL_FORMAT });
				return;
			}

			alarmUtilService.sendCRUD( "updatePinpointUser", oPinpointUser, function( oServerData ) {
				self.cancelAction( alarmUtilService, aEditNode, $node, function () {});
				cbSuccess( oPinpointUser );
				alarmUtilService.hide($elLoading);
			}, function( oServerError ) {
				cbFail( oServerError );
			} );
		}
	};
	function validateEmail( email ) {
		var reg = /^(([^<>()[\]\.,;:\s@\"]+(\.[^<>()[\]\.,;:\s@\"]+)*)|(\".+\"))@(([^<>()[\]\.,;:\s@\"]+\.)+[^<>()[\]\.,;:\s@\"]{2,})$/i;
		return reg.test(email);
	}
	function validatePhone( phone ) {
		var reg = /^\d+$/;
		return reg.test(phone);
	}
	function addBlink( a ) {
		$.each( a, function( index, $el ) {
			$el.addClass("blink-blink");
		});
	}
	function removeBlink( a ) {
		$.each( a, function( index, $el ) {
			$el.removeClass("blink-blink");
		});
	}

})(jQuery);