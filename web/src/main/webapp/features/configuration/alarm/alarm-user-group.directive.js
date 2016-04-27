(function($) {
	'use strict';
	/**
	 * (en)alarmUserGroupDirective 
	 * @ko alarmUserGroupDirective
	 * @group Directive
	 * @name alarmUserGroupDirective
	 * @class
	 */	
	
	pinpointApp.directive("alarmUserGroupDirective", [ "$rootScope", "$timeout", "helpContentService", "AlarmUtilService", "AlarmBroadcastService", "AnalyticsService",
	    function ($rootScope, $timeout, helpContentService, alarmUtilService, alarmBroadcastService, analyticsService ) {
	        return {
	            restrict: 'EA',
	            replace: true,
	            templateUrl: 'features/configuration/alarm/alarmUserGroup.html?v=' + G_BUILD_TIME,
	            scope: true,
	            link: function (scope, element) {
	            	scope.prefix = "alarmUserGroup_";

					var selectedGroupNumber = "";
					var $workingNode = null;
	    			var bIsLoaded = false;
					var oUserGroupList = scope.userGroupList = [];

					var $element = element;
	    			var $elTotal = $element.find(".total");
					var $elAlert = $element.find(".some-alert");
	    			var $elLoading = $element.find(".some-loading");
					var $elNewGroup = $element.find(".new-group");
	    			var $elSearchInput = $element.find(".some-list-search input");

					$element.find(".some-list-content ul").on("click", function($event) {
	    				var $target = $( $event.toElement || $event.target );
	    				var tagName = $target.get(0).tagName.toLowerCase();

						if ( tagName === "span" && $target.hasClass("contents") ) {
							selectGroup( $target.parents("li") );
						} else if ( tagName === "li" ) {
							selectGroup( $target );
						}
	    			});
					function loadData( sParam ) {
						alarmUtilService.show( $elLoading );
						alarmUtilService.sendCRUD( "getUserGroupList", sParam, function( aServerData ) {
							oUserGroupList = scope.userGroupList = aServerData;
							alarmUtilService.setTotal( $elTotal, oUserGroupList.length );
							alarmBroadcastService.sendSelectionEmpty();
							if ( bIsLoaded === false ) {
								alarmBroadcastService.sendLoadPinpointUser();
							}
							bIsLoaded = true;
							alarmUtilService.hide( $elLoading );
						}, showAlert );
					}
	    			function selectGroup( $el ) {
						if ( $el.find( CONSTS.DIV_NORMAL ).hasClass( "hide-me" ) ) {
							return;
						}
	    				cancelPreviousWork();
	    				addSelectClass( alarmUtilService.extractID( $el ) );
	    				alarmBroadcastService.sendReloadWithUserGroupID( $el.find(".contents").html() );
	    			}
					function addSelectClass( newSelectedGroupNumber ) {
						$( "#" + scope.prefix + selectedGroupNumber ).removeClass("selected");
						$( "#" + scope.prefix + newSelectedGroupNumber ).addClass("selected");
						selectedGroupNumber = newSelectedGroupNumber;
					}
					function isSameNode( $current ) {
						return alarmUtilService.extractID( $workingNode ) === alarmUtilService.extractID( $current );
					}
					function cancelPreviousWork() {
						AddUserGroup.cancelAction( alarmUtilService, $elNewGroup );
						UpdateUserGroup.cancelAction( alarmUtilService, $workingNode );
						RemoveUserGroup.cancelAction( alarmUtilService, $workingNode );
					}
					function showAlert( oServerError ) {
						$elAlert.find( ".message" ).html( oServerError.errorMessage );
						alarmUtilService.hide( $elLoading );
						alarmUtilService.show( $elAlert );
					}
					// search
					scope.onSearch = function() {
						alarmUtilService.show( $elLoading );
						cancelPreviousWork();
						var query = $.trim( $elSearchInput.val() );
						if ( query === "" ) {
							loadData();
						} else {
							if ( query.length < CONSTS.MIN_GROUPNAME_LENGTH ) {
								$elSearchInput.val("");
								alarmUtilService.hide( $elLoading );
								return;
							}
							analyticsService.send( analyticsService.CONST.MAIN, analyticsService.CONST.CLK_ALARM_FILTER_USER_GROUP );
							loadData( { "userGroupId": query } );
						}
					};

					// add process
	    			scope.onAddUserGroup = function() {
						if ( AddUserGroup.isOn() ) {
							return;
						}
						cancelPreviousWork();
						AddUserGroup.onAction( alarmUtilService, $elNewGroup );
	    			};
					scope.onCancelAddUserGroup = function() {
						AddUserGroup.cancelAction( alarmUtilService, $elNewGroup );
					};
					scope.onApplyAddUserGroup = function() {
						applyAddUserGroup();
					};
					function applyAddUserGroup() {
						AddUserGroup.applyAction( alarmUtilService, $elNewGroup, $elLoading, function( oServerData, groupId ) {
							oUserGroupList.push({
								id: groupId,
								number: oServerData.number
							});
							scope.userGroupList = oUserGroupList;
							alarmUtilService.setTotal( $elTotal, oUserGroupList.length );
						}, showAlert );
					}

					// remove process
					scope.onRemoveUserGroup = function( $event ) {
						var $node = alarmUtilService.getNode( $event, "li" );
						if ( $workingNode !== null && isSameNode( $node ) === false ) {
							cancelPreviousWork( $node );
						}
						$workingNode = $node;
						RemoveUserGroup.onAction( alarmUtilService, $workingNode );
					};
					scope.onCancelRemoveUserGroup = function() {
						RemoveUserGroup.cancelAction( alarmUtilService, $workingNode );
					};
					scope.onApplyRemoveUserGroup = function() {
						RemoveUserGroup.applyAction( alarmUtilService, $workingNode, $elLoading, function( groupId ) {
							for (var i = 0; i < oUserGroupList.length; i++) {
								if ( oUserGroupList[i].id == groupId ) {
									oUserGroupList.splice(i, 1);
									break;
								}
							}
							scope.$apply(function () {
								scope.userGroupList = oUserGroupList;
							});
							alarmUtilService.setTotal($elTotal, oUserGroupList.length);
						}, showAlert );
					};

					// update process
					scope.onUpdateUserGroup = function( $event ) {
						cancelPreviousWork();
						$workingNode = alarmUtilService.getNode( $event, "li" );
						UpdateUserGroup.onAction( alarmUtilService, $workingNode );
					};
					scope.onCancelUpdateUserGroup = function() {
						UpdateUserGroup.cancelAction( alarmUtilService, $workingNode );
					};
					scope.onApplyUpdateUserGroup = function() {
						applyUpdateUserGroup();
					};
					function applyUpdateUserGroup() {
						UpdateUserGroup.applyAction( alarmUtilService, $workingNode, $elLoading, function( groupNumber, groupName ) {
							return alarmUtilService.hasDuplicateItem( oUserGroupList, function( userGroup ) {
								return ( ( userGroup.number != groupNumber ) && userGroup.id == groupName ) || ( userGroup.number == groupNumber && userGroup.id == groupName );
							});
						}, function( groupNumber, groupName ) {
							analyticsService.send( analyticsService.CONST.MAIN, analyticsService.CONST.CLK_ALARM_CREATE_USER_GROUP );
							for (var i = 0; i < oUserGroupList.length; i++ ) {
								if ( oUserGroupList[i].number == groupNumber ) {
									oUserGroupList[i].id = groupName;
								}
							}
							scope.userGroupList = oUserGroupList;
						}, showAlert );
					}

					// key down
					scope.onSearchKeydown = function( $event ) {
						if ( $event.keyCode == 13 ) { // Enter
							scope.onSearch();
						} else if ( $event.keyCode == 27 ) { // ESC
							$elSearchInput.val("");
							$event.stopPropagation();
						}
					};
					scope.onAddUserGroupKeydown = function( $event ) {
						if ( $event.keyCode == 13 ) { // Enter
							applyAddUserGroup();
						} else if ( $event.keyCode == 27 ) { // ESC
							AddUserGroup.cancelAction( alarmUtilService, $elNewGroup );
							$event.stopPropagation();
						}
					};
					scope.onUpdateUserGroupKeydown = function( $event ) {
						if ( $event.keyCode == 13 ) { // Enter
							applyUpdateUserGroup();
						} else if ( $event.keyCode == 27 ) { // ESC
							UpdateUserGroup.cancelAction( alarmUtilService, $workingNode );
							$event.stopPropagation();
						}
					};
					scope.onCloseAlert = function() {
						alarmUtilService.hide( $elAlert );
					};
					scope.$on("alarmUserGroup.configuration.show", function() {
	    				if ( bIsLoaded === false ) {
	    					loadData();
	    				}
	    			});
	            }
	        };
	    }
	]);
	var CONSTS = {
		MIN_GROUPNAME_LENGTH : 3,
		NEW_GROUP: "New Group",
		EXIST_A_SAME: "Exist a same group name",
		ENTER_AT_LEAST: "Enter at least 3 letters to search",
		DIV_EDIT: "div._edit",
		DIV_NORMAL: "div._normal",
		DIV_REMOVE: "div._remove"
	};

	var AddUserGroup = {
		_bIng: false,
		isOn: function() {
			return this._bIng;
		},
		onAction: function( alarmUtilService, $newNode ) {
			this._bIng = true;
			alarmUtilService.show( $newNode );
			$newNode.find("input").val("").focus();
		},
		cancelAction: function( alarmUtilService, $newNode ) {
			if ( this._bIng === true ) {
				this._bIng = false;
				alarmUtilService.hide( $newNode );
				$newNode.removeClass( "blink-blink" ).find( "input" ).attr( "placeholder", CONSTS.NEW_GROUP ).val( "" );
			}
		},
		applyAction: function( alarmUtilService, $newNode, $elLoading, cbSuccess, cbFail ) {
			alarmUtilService.show( $elLoading );
			var groupId = $newNode.find("input").val();
			if ( groupId.length < CONSTS.MIN_GROUPNAME_LENGTH ) {
				alarmUtilService.hide( $elLoading );
				$newNode.addClass( "blink-blink" ).find( "input" ).attr( "placeholder", CONSTS.ENTER_AT_LEAST ).val( "" ).focus();
				return;
			}
			alarmUtilService.sendCRUD( "createUserGroup", { "id": groupId }, function( oServerData ) {
				cbSuccess( oServerData, groupId );
				AddUserGroup.cancelAction( alarmUtilService, $newNode );
				alarmUtilService.hide( $elLoading );
			}, function( oServerError ) {
				cbFail( oServerError );
			});

		}
	};
	var RemoveUserGroup = {
		_bIng: false,
		onAction: function( alarmUtilService, $node ) {
			this._bIng = true;
			$node.addClass("remove");
			alarmUtilService.hide( $node.find( CONSTS.DIV_NORMAL ) );
			alarmUtilService.show( $node.find( CONSTS.DIV_REMOVE ) );
		},
		cancelAction: function( alarmUtilService, $node ) {
			if ( this._bIng === true ) {
				$node.removeClass("remove");
				alarmUtilService.hide($node.find( CONSTS.DIV_REMOVE ));
				alarmUtilService.show($node.find( CONSTS.DIV_NORMAL ));
				this._bIng = false;
			}
		},
		applyAction: function( alarmUtilService, $node, $elLoading, cbSuccess, cbFail ) {
			alarmUtilService.show( $elLoading );
			var self = this;
			var groupId = $node.find(".contents").html();
			alarmUtilService.sendCRUD("removeUserGroup", {"id": groupId}, function () {
				self.cancelAction( alarmUtilService, $node );
				cbSuccess( groupId );
				alarmUtilService.hide( $elLoading );
			}, function (oServerError) {
				cbFail( oServerError );
			});
		}
	};
	var UpdateUserGroup = {
		_bIng: false,
		onAction: function( alarmUtilService, $node ) {
			this._bIng = true;
			$node.addClass("edit");
			alarmUtilService.hide( $node.find( CONSTS.DIV_NORMAL ) );
			alarmUtilService.show( $node.find( CONSTS.DIV_EDIT ) );
			alarmUtilService.hide( $node.find(".contents") );
			$node.find("input").val( $node.find(".contents").html() ).show();
			$node.find("input").focus();
		},
		cancelAction: function( alarmUtilService, $node ) {
			if ( this._bIng === true ) {
				$node.removeClass("edit blink-blink");
				$node.find("input").hide();
				alarmUtilService.hide($node.find( CONSTS.DIV_EDIT ));
				alarmUtilService.show($node.find(".contents"));
				alarmUtilService.show($node.find( CONSTS.DIV_NORMAL ));
				this._bIng = false;
			}
		},
		applyAction: function( alarmUtilService, $node, $elLoading, cbHasDuplicate, cbSuccess, cbFail ) {
			alarmUtilService.show( $elLoading );
			var self = this;
			var groupNumber = alarmUtilService.extractID( $node );
			var groupName = $node.find("input").val();

			if ( groupName.length < CONSTS.MIN_GROUPNAME_LENGTH ) {
				alarmUtilService.hide( $elLoading );
				$node.addClass("blink-blink");
				$node.find("input").attr("placeholder", CONSTS.ENTER_AT_LEAST).val("").focus();
				return;
			}
			if ( cbHasDuplicate( groupNumber, groupName ) ) {
				alarmUtilService.hide( $elLoading );
				$node.addClass("blink-blink");
				$node.find("input").attr("placeholder", CONSTS.EXIST_A_SAME).val("").focus();
				return;
			}
			alarmUtilService.sendCRUD( "updateUserGroup", { "number": groupNumber, "id": groupName }, function() {
				cbSuccess( groupNumber, groupName );
				self.cancelAction( alarmUtilService, $node );
				alarmUtilService.hide( $elLoading );
			}, function( oServerError ) {
				cbFail( oServerError );
			});

		}
	};
})(jQuery);