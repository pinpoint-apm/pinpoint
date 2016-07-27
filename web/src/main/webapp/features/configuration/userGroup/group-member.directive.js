(function($) {
	'use strict';
	/**
	 * (en)alarmGroupMemberDirective 
	 * @ko alarmGroupMemberDirective
	 * @group Directive
	 * @name alarmGroupMemberDirective
	 * @class
	 */	
	pinpointApp.directive( "groupMemberDirective", [ "$timeout", "helpContentTemplate", "helpContentService", "AlarmUtilService", "AnalyticsService",
	    function ( $timeout, helpContentTemplate, helpContentService, AlarmUtilService, AnalyticsService ) {
	        return {
	            restrict: 'EA',
	            replace: true,
	            templateUrl: 'features/configuration/userGroup/groupMember.html?v=' + G_BUILD_TIME,
	            scope: true,
	            link: function (scope, element) {
					scope.prefix = "groupMember_";
					scope.groupMemberList = [];

	            	var $element = $(element);
					var $elGuide = $element.find(".some-guide");
	    			var $elTotal = $element.find(".total");
	    			var $elLoading = $element.find(".some-loading");
	    			var $elAlert = $element.find(".some-alert");

	    			var currentUserGroupId = "";
					var $workingNode = null;
					var oGroupMemberList = [];

					function cancelPreviousWork() {
						RemoveGroupMember.cancelAction( AlarmUtilService, $workingNode );
					}
					function showAlert( oServerError ) {
						$elAlert.find( ".message" ).html( oServerError.errorMessage );
						AlarmUtilService.hide( $elLoading );
						AlarmUtilService.show( $elAlert );
					}
					function initData() {
						oGroupMemberList = [];
						scope.groupMemberList = [];
						AlarmUtilService.setTotal( $elTotal, oGroupMemberList.length );
					}
					function hasUser( userID ) {
						return $( "#" + scope.prefix + userID ).length > 0;
					}

					function loadData( willBeAddedUser ) {
						AlarmUtilService.show( $elLoading );
						AlarmUtilService.sendCRUD( "getGroupMemberListInGroup", { "userGroupId": currentUserGroupId }, function( oServerData ) {
							oGroupMemberList = oServerData;
							scope.groupMemberList = oGroupMemberList;
							AlarmUtilService.setTotal( $elTotal, oGroupMemberList.length );
							AlarmUtilService.hide( $elLoading );
							scope.$emit( "groupMember.loaded", oGroupMemberList );
							// if ( angular.isDefined( willBeAddedUser ) ) {
							// 	addMember( willBeAddedUser );
							// }
						}, showAlert );
					}
					function isSameNode( $current ) {
						return AlarmUtilService.extractID( $workingNode ) === AlarmUtilService.extractID( $current );
					}

					// remove process
					scope.onRemoveGroupMember = function( $event ) {
						if ( $workingNode !== null && isSameNode( AlarmUtilService.getNode( $event, "li" ) ) === false ) {
							cancelPreviousWork();
						}
						$workingNode = AlarmUtilService.getNode( $event, "li" );
						RemoveGroupMember.onAction( AlarmUtilService, $workingNode );
					};
					scope.onApplyRemoveGroupMember = function() {
						RemoveGroupMember.applyAction( AlarmUtilService, currentUserGroupId, $workingNode, $elLoading, function( memberId ) {
							removeGroupMember( memberId );
						}, showAlert );
					};
					function removeGroupMember( memberId ) {
						for( var i = 0 ; i < oGroupMemberList.length ; i++ ) {
							if ( oGroupMemberList[i].memberId == memberId ) {
								oGroupMemberList.splice(i, 1);
								break;
							}
						}
						scope.$apply(function() {
							scope.groupMemberList = oGroupMemberList;
						});
						scope.$emit( "groupMember.removedMember", oGroupMemberList, memberId );
						AlarmUtilService.setTotal( $elTotal, oGroupMemberList.length );
					}
					scope.onCancelRemoveGroupMember = function() {
						RemoveGroupMember.cancelAction( AlarmUtilService, $workingNode );
					};

					// sort
					scope.onSortGroupMember = function() {
						if ( currentUserGroupId === "" ) return;

						cancelPreviousWork();
						var oSortedGroupMemberList = [];
						var len = oGroupMemberList.length - 1;
						for( var j = 0, i = len ; i >= 0 ; j++, i-- ) {
							oSortedGroupMemberList[j] = oGroupMemberList[i];
						}
						oGroupMemberList = oSortedGroupMemberList;
						scope.groupMemberList = oGroupMemberList;
					};

					// other
					scope.onCloseAlert = function() {
						AlarmUtilService.closeAlert( $elAlert, $elLoading );
					};
					scope.canSort = function() {
						return currentUserGroupId === "" ? "0.5" : "1.0";
					};
					scope.$on( "groupMember.load", function( event, userGroupID, willBeAddedUser )  {
						currentUserGroupId = userGroupID;
						cancelPreviousWork();
						initData();
						AlarmUtilService.hide( $elGuide );
    					loadData( willBeAddedUser );
	    			});
	    			scope.$on( "groupMember.selectNone", function()  {
	    				currentUserGroupId = "";
						initData();
						AlarmUtilService.show( $elGuide );
	    			});
	    			scope.$on( "groupMember.addMember", function( event, oUser )  {
						if ( currentUserGroupId === "" ) {
							scope.$emit( "groupMember.sendCallbackAddedUser", false );
							return;
						}
						cancelPreviousWork();
						AddGroupMember.applyAction( AlarmUtilService, oUser, currentUserGroupId, $elLoading, hasUser, function( oUser ) {
							AnalyticsService.send( AnalyticsService.CONST.MAIN, AnalyticsService.CONST.CLK_ALARM_ADD_USER );
							oGroupMemberList.push({
								"name": oUser.name,
								"memberId": oUser.userId,
								"department": oUser.department,
								"userGroupId": currentUserGroupId
							});
							scope.groupMemberList = oGroupMemberList;
							scope.$emit( "groupMember.sendCallbackAddedUser", true, oUser.userId );
							AlarmUtilService.setTotal( $elTotal, oGroupMemberList.length );
							AlarmUtilService.hide( $elLoading );
						}, function() {
							showAlert({
								message: CONSTS.EXIST_A_SAME
							});
							scope.$emit( "groupMember.sendCallbackAddedUser", false, oUser.userId );
						});
	    			});
	    			scope.$on( "groupMember.updateUser", function( event, oUser )  {
	    				if ( hasUser( oUser.userId ) ) {
							cancelPreviousWork();
	    					updateGroupMember( oUser );
	    				}
	    			});
					function updateGroupMember( oUser ) {
						for( var i = 0 ; i < oGroupMemberList.length ; i++ ) {
							if ( oGroupMemberList[i].memberId == oUser.userId ) {
								oGroupMemberList[i].name = oUser.name;
								oGroupMemberList[i].department = oUser.department;
								break;
							}
						}
						scope.groupMemberList = oGroupMemberList;
					}
	    			scope.$on( "groupMember.removeUser", function( event, userId )  {
	    				if ( hasUser( userId ) ) {
							cancelPreviousWork();
	    					// scope.$apply(function() {
	    						removeGroupMember( userId );
	    					// });
	    				}
	    			});
	            }
	        };
	}]);

	var CONSTS = {
		EXIST_A_SAME: "Exist a same user in the lists.",
		DIV_NORMAL: "div._normal",
		DIV_EDIT: "div._edit",
		DIV_REMOVE: "div._remove"
	};

	var AddGroupMember = {
		applyAction: function( AlarmUtilService, oUser, currentUserGroupId, $elLoading, cbHasUser, cbSuccess, cbFail ) {
			AlarmUtilService.show( $elLoading );
			if ( cbHasUser( oUser.userId ) === true ) {
				cbFail();
			} else {
				AlarmUtilService.sendCRUD( "addMemberInGroup", {
					"userGroupId": currentUserGroupId,
					"memberId": oUser.userId
				}, function( oServerData ) {
					cbSuccess( oUser );
					AlarmUtilService.hide( $elLoading );
				}, function( oServerError ) {
					cbFail( oServerError );
				} );
			}
		}
	};

	var RemoveGroupMember = {
		_bIng: false,
		onAction: function( AlarmUtilService, $node ) {
			this._bIng = true;
			$node.addClass( "remove" );
			AlarmUtilService.hide( $node.find( CONSTS.DIV_NORMAL ) );
			AlarmUtilService.show( $node.find( CONSTS.DIV_REMOVE ) );
		},
		cancelAction: function( AlarmUtilService, $node ) {
			if ( this._bIng === true ) {
				$node.removeClass( "remove" );
				AlarmUtilService.hide($node.find( CONSTS.DIV_REMOVE ));
				AlarmUtilService.show($node.find( CONSTS.DIV_NORMAL ));
				this._bIng = false;
			}
		},
		applyAction: function( AlarmUtilService, currentUserGroupId, $node, $elLoading, cbSuccess, cbFail ) {
			AlarmUtilService.show( $elLoading );
			var self = this;
			var memberId = AlarmUtilService.extractID( $node );
			AlarmUtilService.sendCRUD( "removeMemberInGroup", {
				"userGroupId": currentUserGroupId,
				"memberId": memberId
			}, function( oServerData ) {
				cbSuccess( memberId );
				self.cancelAction( AlarmUtilService, $node );
				AlarmUtilService.hide( $elLoading );
			}, function (oServerError) {
				cbFail( oServerError );
			});

		}
	};
})(jQuery);