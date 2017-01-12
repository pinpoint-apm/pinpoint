(function($) {
	'use strict';
	/**
	 * (en)alarmUserGroupDirective 
	 * @ko alarmUserGroupDirective
	 * @group Directive
	 * @name alarmUserGroupDirective
	 * @class
	 */	
	
	pinpointApp.directive("userGroupDirective", [ "$timeout", "helpContentService", "AlarmUtilService", "AnalyticsService", "SystemConfigurationService",
	    function ( $timeout, helpContentService, AlarmUtilService, AnalyticsService, SystemConfigService ) {
	        return {
	            restrict: 'EA',
	            replace: true,
	            templateUrl: 'features/configuration/userGroup/userGroup.html?v=' + G_BUILD_TIME,
	            scope: true,
	            link: function (scope, element) {
	            	scope.prefix = "userGroup_";

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
						AlarmUtilService.show( $elLoading );
						AlarmUtilService.sendCRUD( "getUserGroupList", sParam, function( aServerData ) {
							oUserGroupList = scope.userGroupList = aServerData;
							AlarmUtilService.setTotal( $elTotal, oUserGroupList.length );
							scope.$emit( "userGroup.selectedNone" );
							bIsLoaded = true;
							AlarmUtilService.hide( $elLoading );
						}, showAlert );
					}
	    			function selectGroup( $el ) {
						if ( $el.find( CONSTS.DIV_NORMAL ).hasClass( "hide-me" ) ) {
							return;
						}
	    				cancelPreviousWork();
	    				addSelectClass( AlarmUtilService.extractID( $el ) );
						scope.$emit( "userGroup.selectedUserGroup", $el.find(".contents").html() );
	    			}
					function addSelectClass( newSelectedGroupNumber ) {
						$( "#" + scope.prefix + selectedGroupNumber ).removeClass("selected");
						$( "#" + scope.prefix + newSelectedGroupNumber ).addClass("selected");
						selectedGroupNumber = newSelectedGroupNumber;
					}
					function isSameNode( $current ) {
						return AlarmUtilService.extractID( $workingNode ) === AlarmUtilService.extractID( $current );
					}
					function cancelPreviousWork() {
						AddUserGroup.cancelAction( AlarmUtilService, $elNewGroup );
						RemoveUserGroup.cancelAction( AlarmUtilService, $workingNode );
					}
					function showAlert( oServerError ) {
						$elAlert.find( ".message" ).html( oServerError.errorMessage );
						AlarmUtilService.hide( $elLoading );
						AlarmUtilService.show( $elAlert );
					}
					// search
					scope.onSearch = function() {
						AlarmUtilService.show( $elLoading );
						cancelPreviousWork();
						var query = $.trim( $elSearchInput.val() );
						if ( query === "" ) {
							loadData();
						} else {
							if ( query.length < CONSTS.MIN_GROUPNAME_LENGTH ) {
								$elSearchInput.val("");
								AlarmUtilService.hide( $elLoading );
								return;
							}
							AnalyticsService.send( AnalyticsService.CONST.MAIN, AnalyticsService.CONST.CLK_ALARM_FILTER_USER_GROUP );
							loadData( { "userGroupId": query } );
						}
					};

					// add process
	    			scope.onAddUserGroup = function() {
						if ( AddUserGroup.isOn() ) {
							return;
						}
						cancelPreviousWork();
						AddUserGroup.onAction( AlarmUtilService, $elNewGroup );
	    			};
					scope.onCancelAddUserGroup = function() {
						AddUserGroup.cancelAction( AlarmUtilService, $elNewGroup );
					};
					scope.onApplyAddUserGroup = function() {
						applyAddUserGroup();
					};
					function applyAddUserGroup() {
						AddUserGroup.applyAction( AlarmUtilService, $elNewGroup, $elLoading, SystemConfigService.get("userId"), function( oServerData, groupId ) {
							oUserGroupList.push({
								id: groupId,
								number: oServerData.number
							});
							scope.userGroupList = oUserGroupList;
							AlarmUtilService.setTotal( $elTotal, oUserGroupList.length );
						}, showAlert );
					}

					// remove process
					scope.onRemoveUserGroup = function( $event ) {
						var $node = AlarmUtilService.getNode( $event, "li" );
						if ( $workingNode !== null && isSameNode( $node ) === false ) {
							cancelPreviousWork( $node );
						}
						$workingNode = $node;
						RemoveUserGroup.onAction( AlarmUtilService, $workingNode );
					};
					scope.onCancelRemoveUserGroup = function() {
						RemoveUserGroup.cancelAction( AlarmUtilService, $workingNode );
					};
					scope.onApplyRemoveUserGroup = function() {
						RemoveUserGroup.applyAction( AlarmUtilService, $workingNode, $elLoading, SystemConfigService.get("userId"), function( groupId ) {
							for (var i = 0; i < oUserGroupList.length; i++) {
								if ( oUserGroupList[i].id == groupId ) {
									oUserGroupList.splice(i, 1);
									break;
								}
							}
							scope.$apply(function () {
								scope.userGroupList = oUserGroupList;
							});
							AlarmUtilService.setTotal($elTotal, oUserGroupList.length);
						}, showAlert );
					};

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
							AddUserGroup.cancelAction( AlarmUtilService, $elNewGroup );
							$event.stopPropagation();
						}
					};
					scope.onCloseAlert = function() {
						AlarmUtilService.hide( $elAlert );
					};
					scope.$on("configuration.userGroup.show", function() {
	    				if ( bIsLoaded === false ) {
	    					loadData();
	    				}
	    			});
	            }
	        };
	    }
	]);
	var CONSTS = {
		MIN_GROUP_NAME_LENGTH : 3,
		NEW_GROUP: "New Group",
		EXIST_A_SAME: "Exist a same group name",
		ENTER_AT_LEAST: "Enter at least 3 letters to search",
		CANNOT_BE_USED_GUEST: "The name 'guest' can not be used.",
		DIV_EDIT: "div._edit",
		DIV_NORMAL: "div._normal",
		DIV_REMOVE: "div._remove"
	};

	var AddUserGroup = {
		_bIng: false,
		isOn: function() {
			return this._bIng;
		},
		onAction: function( AlarmUtilService, $newNode ) {
			this._bIng = true;
			AlarmUtilService.show( $newNode );
			$newNode.find("input").val("").focus();
		},
		cancelAction: function( AlarmUtilService, $newNode ) {
			if ( this._bIng === true ) {
				this._bIng = false;
				AlarmUtilService.hide( $newNode );
				$newNode.removeClass( "blink-blink" ).find( "input" ).attr( "placeholder", CONSTS.NEW_GROUP ).val( "" );
			}
		},
		applyAction: function( AlarmUtilService, $newNode, $elLoading, userId, cbSuccess, cbFail ) {
			AlarmUtilService.show( $elLoading );
			var groupId = $newNode.find("input").val();
			if ( groupId.trim() === "guest" ) {
				AlarmUtilService.hide( $elLoading );
				$newNode.addClass( "blink-blink" ).find( "input" ).attr( "placeholder", CONSTS.CANNOT_BE_USED_GUEST ).val( "" ).focus();
				return;
			}
			if ( groupId.length < CONSTS.MIN_GROUP_NAME_LENGTH ) {
				AlarmUtilService.hide( $elLoading );
				$newNode.addClass( "blink-blink" ).find( "input" ).attr( "placeholder", CONSTS.ENTER_AT_LEAST ).val( "" ).focus();
				return;
			}
			AlarmUtilService.sendCRUD( "createUserGroup", {
				"id": groupId,
				"userId": userId
			}, function( oServerData ) {
				cbSuccess( oServerData, groupId );
				AddUserGroup.cancelAction( AlarmUtilService, $newNode );
				AlarmUtilService.hide( $elLoading );
			}, function( oServerError ) {
				cbFail( oServerError );
			});

		}
	};
	var RemoveUserGroup = {
		_bIng: false,
		onAction: function( AlarmUtilService, $node ) {
			this._bIng = true;
			$node.addClass("remove");
			AlarmUtilService.hide( $node.find( CONSTS.DIV_NORMAL ) );
			AlarmUtilService.show( $node.find( CONSTS.DIV_REMOVE ) );
		},
		cancelAction: function( AlarmUtilService, $node ) {
			if ( this._bIng === true ) {
				$node.removeClass("remove");
				AlarmUtilService.hide($node.find( CONSTS.DIV_REMOVE ));
				AlarmUtilService.show($node.find( CONSTS.DIV_NORMAL ));
				this._bIng = false;
			}
		},
		applyAction: function( AlarmUtilService, $node, $elLoading, userId, cbSuccess, cbFail ) {
			AlarmUtilService.show( $elLoading );
			var self = this;
			var groupId = $node.find(".contents").html();
			AlarmUtilService.sendCRUD("removeUserGroup", {
				"id": groupId,
				"userId": userId
			}, function () {
				self.cancelAction( AlarmUtilService, $node );
				cbSuccess( groupId );
				AlarmUtilService.hide( $elLoading );
			}, function (oServerError) {
				cbFail( oServerError );
			});
		}
	};
})(jQuery);