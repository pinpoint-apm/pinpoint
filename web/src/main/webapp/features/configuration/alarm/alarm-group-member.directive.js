(function($) {
	'use strict';
	/**
	 * (en)alarmGroupMemberDirective 
	 * @ko alarmGroupMemberDirective
	 * @group Directive
	 * @name alarmGroupMemberDirective
	 * @class
	 */	
	pinpointApp.directive('alarmGroupMemberDirective', [ '$rootScope', '$timeout', 'helpContentTemplate', 'helpContentService', 'AlarmUtilService', 'AlarmBroadcastService', 'AnalyticsService',
	    function ($rootScope, $timeout, helpContentTemplate, helpContentService, alarmUtilService, alarmBroadcastService, analyticsService) {
	        return {
	            restrict: 'EA',
	            replace: true,
	            templateUrl: 'features/configuration/alarm/alarmGroupMember.html?v=' + G_BUILD_TIME,
	            scope: true,
	            link: function (scope, element) {
					scope.prefix = "alarmGroupMember_";
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
						RemoveGroupMember.cancelAction( alarmUtilService, $workingNode );
					}
					function getNode( $event ) {
						return $( $event.toElement || $event.target ).parents("li");
					}
					function showAlert( oServerError ) {
						$elAlert.find( ".message" ).html( oServerError.errorMessage );
						alarmUtilService.hide( $elLoading );
						alarmUtilService.show( $elAlert );
					}
					function initData() {
						oGroupMemberList = [];
						scope.groupMemberList = [];
					}
					function hasUser( userID ) {
						return $( "#" + scope.prefix + userID ).length > 0;
					}

					function loadData( willBeAddedUser ) {
						alarmUtilService.show( $elLoading );
						alarmUtilService.sendCRUD( "getGroupMemberListInGroup", { "userGroupId": currentUserGroupId }, function( oServerData ) {
							oGroupMemberList = oServerData;
							scope.groupMemberList = oGroupMemberList;
							alarmUtilService.setTotal( $elTotal, oGroupMemberList.length );
							alarmUtilService.hide( $elLoading );
							alarmBroadcastService.sendGroupMemberLoaded( oGroupMemberList );
							// if ( angular.isDefined( willBeAddedUser ) ) {
							// 	addMember( willBeAddedUser );
							// }
						}, showAlert );
					}
					function isSameNode( $current ) {
						return alarmUtilService.extractID( $workingNode ) === alarmUtilService.extractID( $current );
					}

					// remove process
					scope.onRemoveGroupMember = function( $event ) {
						if ( $workingNode !== null && isSameNode( getNode( $event ) ) === false ) {
							cancelPreviousWork();
						}
						$workingNode = getNode( $event );
						RemoveGroupMember.onAction( alarmUtilService, $workingNode );
					};
					scope.onApplyRemoveGroupMember = function() {
						RemoveGroupMember.applyAction( alarmUtilService, currentUserGroupId, $workingNode, $elLoading, function( memberId ) {
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
						alarmBroadcastService.sendGroupMemberRemoved( oGroupMemberList, memberId );
						alarmUtilService.setTotal( $elTotal, oGroupMemberList.length );
					}
					scope.onCancelRemoveGroupMember = function() {
						RemoveGroupMember.cancelAction( alarmUtilService, $workingNode );
					};

					// sort
					scope.onSortGroupMember = function() {
						console.log( "sort group member ");
					};

					// other
					scope.onCloseAlert = function() {
						alarmUtilService.closeAlert( $elAlert, $elLoading );
					};
					scope.$on("alarmGroupMember.configuration.load", function( event, userGroupID, willBeAddedUser )  {
						currentUserGroupId = userGroupID;
						cancelPreviousWork();
						initData();
						alarmUtilService.hide( $elGuide );
    					loadData( willBeAddedUser );
	    			});
	    			scope.$on("alarmGroupMember.configuration.selectNone", function()  {
	    				currentUserGroupId = "";
						initData();
						alarmUtilService.show( $elGuide );
	    			});
	    			scope.$on("alarmGroupMember.configuration.addUser", function( event, oUser )  {
						if ( currentUserGroupId === "" ) {
							alarmBroadcastService.sendCallbackAddedUser( false );
							return;
						}
						cancelPreviousWork();
						AddGroupMember.applyAction( alarmUtilService, oUser, currentUserGroupId, $elLoading, hasUser, function( oUser ) {
							analyticsService.send( analyticsService.CONST.MAIN, analyticsService.CONST.CLK_ALARM_ADD_USER );
							scope.groupMemberList.push({
								"name": oUser.name,
								"memberId": oUser.userId,
								"department": oUser.department,
								"userGroupId": currentUserGroupId
							});
							alarmBroadcastService.sendCallbackAddedUser( true );
							alarmUtilService.setTotal( $elTotal, oGroupMemberList.length );
							alarmUtilService.hide( $elLoading );
						}, function() {
							showAlert({
								message: CONSTS.EXIST_A_SAME
							});
							alarmBroadcastService.sendCallbackAddedUser( true );
						});
	    			});
	    			scope.$on("alarmGroupMember.configuration.updateUser", function( event, oUser )  {
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
	    			scope.$on("alarmGroupMember.configuration.removeUser", function( event, userID )  {
	    				if ( hasUser( userID ) ) {
							cancelPreviousWork();
	    					// scope.$apply(function() {
	    						removeGroupMember( userID );
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
		applyAction: function( alarmUtilService, oUser, currentUserGroupId, $elLoading, cbHasUser, cbSuccess, cbFail ) {
			alarmUtilService.show( $elLoading );
			if ( cbHasUser( oUser.userId ) === true ) {
				cbFail();
			} else {
				alarmUtilService.sendCRUD( "addMemberInGroup", {
					"userGroupId": currentUserGroupId, "memberId": oUser.userId
				}, function( oServerData ) {
					cbSuccess( oUser );
					alarmUtilService.hide( $elLoading );
				}, function( oServerError ) {
					cbFail( oServerError );
				} );
			}
		}
	};

	var RemoveGroupMember = {
		_bIng: false,
		onAction: function( alarmUtilService, $node ) {
			this._bIng = true;
			alarmUtilService.hide( $node.find( CONSTS.DIV_NORMAL ) );
			alarmUtilService.show( $node.find( CONSTS.DIV_REMOVE ) );
		},
		cancelAction: function( alarmUtilService, $node ) {
			if ( this._bIng === true ) {
				alarmUtilService.hide($node.find( CONSTS.DIV_REMOVE ));
				alarmUtilService.show($node.find( CONSTS.DIV_NORMAL ));
				this._bIng = false;
			}
		},
		applyAction: function( alarmUtilService, currentUserGroupId, $node, $elLoading, cbSuccess, cbFail ) {
			alarmUtilService.show( $elLoading );
			var self = this;
			var memberId = alarmUtilService.extractID( $node );
			alarmUtilService.sendCRUD( "removeMemberInGroup", {
				"userGroupId": currentUserGroupId,
				"memberId": memberId
			}, function( oServerData ) {
				cbSuccess( memberId );
				self.cancelAction( alarmUtilService, $node );
				alarmUtilService.hide( $elLoading );
			}, function (oServerError) {
				cbFail( oServerError );
			});

		}
	};
})(jQuery);