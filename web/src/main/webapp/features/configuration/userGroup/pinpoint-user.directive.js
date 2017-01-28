(function($) {
	'use strict';
	/**
	 * (en)alarmPinpointUserDirective 
	 * @ko alarmPinpointUserDirective
	 * @group Directive
	 * @name alarmPinpointUserDirective
	 * @class
	 */	
	
	pinpointApp.directive( "pinpointUserDirective", [ "helpContentTemplate", "helpContentService", "AlarmUtilService", "AnalyticsService", "SystemConfigurationService",
	    function ( helpContentTemplate, helpContentService, AlarmUtilService, AnalyticsService, SystemConfigService) {
        return {
            restrict: 'EA',
            replace: true,
            templateUrl: 'features/configuration/userGroup/pinpointUser.html?v=' + G_BUILD_TIME,
            scope: true,
            link: function (scope, element) {

				scope.prefix = "pinpointUser_";
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
					return SystemConfigService.get("editUserInfo");
				};
				function cancelPreviousWork() {
					AddPinpointUser.cancelAction( aEditNode, hideEditArea );
					RemovePinpointUser.cancelAction( AlarmUtilService, $workingNode );
					UpdatePinpointUser.cancelAction( AlarmUtilService, aEditNode, $workingNode, hideEditArea );
				}
				function showAlert( oServerError ) {
					$elAlert.find( ".message" ).html( oServerError.errorMessage );
					AlarmUtilService.hide( $elLoading );
					AlarmUtilService.show( $elAlert );
				}
				function loadData( oParam ) {
					AlarmUtilService.show( $elLoading );
					AlarmUtilService.sendCRUD("getPinpointUserList", oParam || {}, function (oServerData) {
						$.each(oServerData, function (index, obj) {
							obj["has"] = false;
						});
						oPinpointUserList = scope.pinpointUserList = oServerData;
						AlarmUtilService.setTotal($elTotal, getTotal());
						AlarmUtilService.hide($elLoading);
					}, showAlert);
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
					AlarmUtilService.hide( aEditNode[len].find( CONSTS.DIV_EDIT ) );
					AlarmUtilService.show( aEditNode[len].find( CONSTS.DIV_ADD ) );
					$.each( aEditNode, function( index, $el ) {
						AlarmUtilService.show( $el );
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

					AlarmUtilService.hide( aEditNode[len].find( CONSTS.DIV_ADD ) );
					AlarmUtilService.show( aEditNode[len].find( CONSTS.DIV_EDIT ) );
					$.each( aEditNode, function( index, $el ) {
						AlarmUtilService.show( $el );
					});
					aEditNode[0].find("input").focus();
				}
				function hideEditArea() {
					$.each( aEditNode, function( index, $el ) {
						AlarmUtilService.hide( $el );
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
					return AlarmUtilService.extractID( $workingNode ) === AlarmUtilService.extractID( $current );
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
					AddPinpointUser.applyAction( AlarmUtilService, getNewPinpointUser(), aEditNode, $elLoading, function( oNewPinpointUser  ) {
						AnalyticsService.send( AnalyticsService.CONST.MAIN, AnalyticsService.CONST.CLK_ALARM_CREATE_PINPOINT_USER );
						oPinpointUserList.push( oNewPinpointUser );
						scope.pinpointUserList = oPinpointUserList;
						hideEditArea();
						AlarmUtilService.setTotal( $elTotal, getTotal() );
					}, showAlert );
				}
				// remove
				scope.onRemovePinpointUser = function( $event ) {
					var $node = AlarmUtilService.getNode( $event, "li" );
					if ( $workingNode !== null && isSameNode( $node ) === false ) {
						cancelPreviousWork( $node );
					}
					$workingNode = $node;
					RemovePinpointUser.onAction( AlarmUtilService, $workingNode );
				};
				scope.onCancelRemovePinpointUser = function() {
					RemovePinpointUser.cancelAction( AlarmUtilService, $workingNode );
				};
				scope.onApplyRemovePinpointUser = function() {
					RemovePinpointUser.applyAction( AlarmUtilService, $workingNode, $elLoading, function( userId ) {
						for( var i = 0 ; i < oPinpointUserList.length ; i++ ) {
							if ( oPinpointUserList[i].userId == userId ) {
								oPinpointUserList.splice(i, 1);
								break;
							}
						}
						scope.$apply(function() {
							scope.pinpointUserList = oPinpointUserList;
						});
						AlarmUtilService.setTotal( $elTotal, getTotal() );
						scope.$emit( "pinpointUser.sendUserRemoved", userId );
					}, showAlert );
				};
				// update
				scope.onUpdatePinpointUser = function( $event ) {
					cancelPreviousWork();
					$workingNode = AlarmUtilService.getNode( $event, "li" );
					UpdatePinpointUser.onAction( AlarmUtilService, $workingNode, function( userId ) {
						showEditArea( searchPinpointUser( userId ) );
					});
				};
				scope.onCancelUpdatePinpointUser = function() {
					UpdatePinpointUser.cancelAction( AlarmUtilService, aEditNode, $workingNode, hideEditArea );
				};
				scope.onApplyUpdatePinpointUser = function() {
					UpdatePinpointUser.applyAction( AlarmUtilService, getNewPinpointUser(), aEditNode, $workingNode, $elLoading, function( oPinpointUser ) {

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
						scope.$emit( "pinpointUser.sendUserUpdated", oPinpointUser );
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
					AlarmUtilService.show( $elLoading );
					AnalyticsService.send( AnalyticsService.CONST.MAIN, AnalyticsService.CONST.CLK_ALARM_FILTER_PINPOINT_USER );
					loadData({ "searchKey": query });
				};
				scope.checkUser = function( $event ) {
					AlarmUtilService.show( $elLoading );
					var $node = AlarmUtilService.getNode( $event, "li" );
					var userId =  AlarmUtilService.extractID( $node );
					if ( $node.find("input").get(0).checked ) {
						scope.$emit( "pinpointUser.sendUserAdd", getUser( userId ) );
					} else {
						scope.$emit( "pinpointUser.sendUserRemoved", userId );
					}
				};
				scope.$on( "pinpointUser.changeSelectedMember", function( event, list ) {
					resetList( list );
					scope.$apply(function() {
						scope.pinpointUserList = oPinpointUserList;
					});
					AlarmUtilService.setTotal( $elTotal, getTotal() );
				});
				scope.$on( "pinpointUser.checkSelectedMember", function( event, list ) {
					$elWrapper.removeClass( "_disable-check" );
					resetList( list );
					scope.pinpointUserList = oPinpointUserList;
					AlarmUtilService.setTotal( $elTotal, getTotal() );
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
				scope.$on( "pinpointUser.selectNone", function() {
					$elWrapper.addClass( "_disable-check" );
					$.each( oPinpointUserList, function( index, oPinpointUser ) {
						oPinpointUser.has = false;
					});
					oGroupMemberList = [];
					scope.pinpointUserList = oPinpointUserList;
					AlarmUtilService.setTotal( $elTotal, getTotal() );
				});
				scope.$on( "pinpointUser.addUserCallback", function( event, bIsSuccess, userId ) {
					if ( bIsSuccess === false ) {
						for( var i = 0 ; i < scope.pinpointUserList.length ; i++ ) {
							if ( scope.pinpointUserList[i].userId === userId ) {
								scope.pinpointUserList[i].has = false;
							}
						}
					}
					AlarmUtilService.setTotal( $elTotal, getTotal() );
					AlarmUtilService.hide( $elLoading );
				});
				scope.onCloseAlert = function() {
					AlarmUtilService.closeAlert( $elAlert, $elLoading );
				};
				scope.$on( "pinpointUser.load", function( event, department ) {
					cancelPreviousWork();
					if ( bIsLoaded === false ) {
						loadData(angular.isUndefined(department) ? {} : {"searchKey": department});
					}
				});
            }
        };
    }]);

	var CONSTS = {
		MIN_SEARCH_LENGTH : 2,
		INPUT_USER_ID_AND_NAME: "Input user id and name",
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
		applyAction: function( AlarmUtilService, oNewPinpointUser, aEditNode, $elLoading, cbSuccess, cbFail ) {
			var self = this;
			AlarmUtilService.show( $elLoading );
			if ( oNewPinpointUser.userId === "" || oNewPinpointUser.name === "" ) {
				addBlink( aEditNode );
				cbFail({ errorMessage: CONSTS.INPUT_USER_ID_AND_NAME });
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
			
			AlarmUtilService.sendCRUD( "createPinpointUser", oNewPinpointUser, function( oServerData ) {
				oNewPinpointUser.number = oServerData.number;
				cbSuccess( oNewPinpointUser );
				self.cancelAction( aEditNode, function() {} );
				AlarmUtilService.hide( $elLoading );
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
		onAction: function ( AlarmUtilService, $node ) {
			this._bIng = true;
			$node.addClass("remove");
			AlarmUtilService.hide( $node.find( CONSTS.DIV_NORMAL ) );
			AlarmUtilService.show( $node.find( CONSTS.DIV_REMOVE ) );
		},
		cancelAction: function ( AlarmUtilService, $node ) {
			if ( this._bIng === true ) {
				$node.removeClass("remove");
				AlarmUtilService.hide($node.find( CONSTS.DIV_REMOVE ));
				AlarmUtilService.show($node.find( CONSTS.DIV_NORMAL ));
				this._bIng = false;
			}
		},
		applyAction: function (AlarmUtilService, $node, $elLoading, cbSuccess, cbFail) {
			var self = this;
			var userId = AlarmUtilService.extractID( $node );
			AlarmUtilService.sendCRUD( "removePinpointUser", { "userId": userId }, function( oServerData ) {
				cbSuccess( userId );
				self.cancelAction( AlarmUtilService, $node );
				AlarmUtilService.hide( $elLoading );
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
		onAction: function ( AlarmUtilService, $node, cb ) {
			this._bIng = true;
			AlarmUtilService.hide( $node );
			cb( AlarmUtilService.extractID( $node ) );
		},
		cancelAction: function( AlarmUtilService, aEditNode, $node, cbCancel ) {
			if ( this._bIng === true ) {
				cbCancel();
				removeBlink( aEditNode );
				AlarmUtilService.show( $node );
				this._bIng = false;
			}
		},
		applyAction: function (AlarmUtilService, oPinpointUser, aEditNode, $node, $elLoading, cbSuccess, cbFail) {
			var self = this;
			AlarmUtilService.show($elLoading);

			if ( oPinpointUser.name === "" ) {
				addBlink( aEditNode );
				cbFail({ errorMessage: CONSTS.INPUT_USER_ID_AND_NAME });
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

			AlarmUtilService.sendCRUD( "updatePinpointUser", oPinpointUser, function( oServerData ) {
				self.cancelAction( AlarmUtilService, aEditNode, $node, function () {});
				cbSuccess( oPinpointUser );
				AlarmUtilService.hide($elLoading);
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