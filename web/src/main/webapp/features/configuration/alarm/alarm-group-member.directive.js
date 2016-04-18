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
	            	var $element = $(element);
	    			var $elTotal = $element.find(".total");
	    			var $elLoading = $element.find(".some-loading");
	    			var $elAlert = $element.find(".some-alert");

	    			var currentUserGroupID = "";
					var $workingNode = null;
					var bIsCreating = false;
	    			var bIsRemoving = false;
					scope.groupMemberList = [];

					function cancelPreviousWork() {
						if ( bIsCreating === true ) {
							// cancelAddGroupMember();
						} else if ( bIsRemoving === true ) {
							cancelRemoveGroupMember( $workingNode );
						}
					}
					function initData() {
						scope.groupMemberList = [];
					}
					function reset() {
						cancelPreviousWork();
						initData();
					}
	    			/*
	    			function updateFromList( oUser ) {
						var i = 0;
	    				if ( scope.groupMemberList != groupMemberList ) {
	    					for( i = 0 ; i < groupMemberList.length ; i++ ) {
	    						if ( groupMemberList[i].memberId == oUser.userId ) {
	    							groupMemberList[i].name = oUser.name;
	    							groupMemberList[i].department = oUser.department;
	    							break;
	    						}
	    					}	
	    				}
	    				for( i = 0 ; i < scope.groupMemberList.length ; i++ ) {
	    					if ( scope.groupMemberList[i].memberId == oUser.userId ) {
	    						scope.groupMemberList[i].name = oUser.name;
	    						scope.groupMemberList[i].department = oUser.department;
	    						break;
	    					}
	    				}
	    			}
	    			function addMember( oUser ) {
	    				alarmUtilService.sendCRUD( "addMemberInGroup", { "userGroupId": currentUserGroupID, "memberId": oUser.userId }, function( resultData ) {
	    					scope.groupMemberList.push({
	    						"userGroupId": currentUserGroupID, 
	    						"memberId": oUser.userId,
	    						"name": oUser.name,
	    						"department": oUser.department
	    					});
	    					alarmUtilService.setTotal( $elTotal, groupMemberList.length );
	    					alarmUtilService.hide( $elLoading );
	    					alarmBroadcastService.sendCallbackAddedUser( true );
	    				}, function( errorData ) {}, $elAlert );
	    			}

	    			function hasUser( userID ) {
	    				return $( "#" + scope.prefix + userID ).length > 0;
	    			}
	    			function selectedUserGroupID() {
	    				if ( currentUserGroupID === "" ) {
		    				alarmUtilService.showLoading( $elLoading, false );
	    					alarmUtilService.showAlert( $elAlert, "Not selected User Group.", true );
	    					return false;
	    				}
    					return true;
	    			}
	    			*/
					function loadData( willBeAddedUser ) {
						alarmUtilService.show( $elLoading );
						alarmUtilService.sendCRUD( "getGroupMemberListInGroup", { "userGroupId": currentUserGroupID }, function( oServerData ) {
							scope.$apply(function() {
								scope.groupMemberList = oServerData;
							});
							alarmUtilService.setTotal( $elTotal, scope.groupMemberList.length );
							alarmUtilService.hide( $elLoading );


							// if ( angular.isDefined( willBeAddedUser ) ) {
							// 	addMember( willBeAddedUser );
							// }
						}, function( oServerError ) {
							alarmUtilService.hide( $elLoading );
							$elAlert.find(".message").html( oServerError.errorMessage );
							alarmUtilService.show( $elAlert );
						}, $elAlert );
					}
					function removeGroupMember( $parent ) {
						alarmUtilService.show( $elLoading );
						if ( bIsRemoving === false ) {
							alarmUtilService.show( $parent.find(".remove-cancel") );
							$parent.find(".remove").addClass("remove-confirm");
							alarmUtilService.hide( $elLoading );
							bIsRemoving = true;
						} else {
							var memberID = alarmUtilService.extractID( $parent );
							alarmUtilService.sendCRUD( "removeMemberInGroup", {
								"userGroupId": currentUserGroupID,
								"memberId": memberID
							}, function( oServerData ) {
								// scope.$apply(function() {
									removeFromList( memberID );
								// });
								alarmUtilService.setTotal( $elTotal, scope.groupMemberList.length );
								alarmUtilService.hide( $elLoading );
								cancelRemoveGroupMember( $workingNode );
							}, function (oServerError) {
								alarmUtilService.hide( $elLoading );
								$elAlert.find(".message").html( oServerError.errorMessage );
								alarmUtilService.show( $elAlert );
							});
						}
					}
					function removeFromList( memberID ) {
						for( var i = 0 ; i < scope.groupMemberList.length ; i++ ) {
							if ( scope.groupMemberList[i].memberId == memberID ) {
								scope.groupMemberList.splice(i, 1);
								break;
							}
						}
					}
					function cancelRemoveGroupMember( $parent ) {
						if ( $parent === null ) return;
						alarmUtilService.hide( $parent.find(".remove-cancel") );
						$parent.find(".remove").removeClass("remove-confirm");
						$workingNode = null;
						bIsRemoving = false;
					}
					function isSameNode( $current ) {
						return alarmUtilService.extractID( $workingNode ) === alarmUtilService.extractID( $current );
					}
	    			
	    			scope.prefix = "alarmGroupMember_";
					/*
	    			scope.onFilterGroup = function() {
	    				if ( bIsRemoving === true ) return;
	    				if ( selectedUserGroupID() === false ) return;
	    				var query = $.trim( $elFilterInput.val() );
	    				if ( query.length !== 0 && query.length < 3 ) {
	    					alarmUtilService.showLoading( $elLoading, false );
	    					alarmUtilService.showAlert( $elAlert, "You must enter at least three characters.");
	    					return;
	    				}
	    				analyticsService.send( analyticsService.CONST.MAIN, analyticsService.CONST.CLK_ALARM_FILTER_USER );
	    				if ( query === "" ) {
	    					if ( scope.groupMemberList.length != groupMemberList.length ) {
	    						scope.groupMemberList = groupMemberList;
	    					}
	    					$elFilterEmpty.addClass("disabled");
	    				} else {
	    					var newFilterGroupMember = [];
	    					var length = groupMemberList.length;
	    					for( var i = 0 ; i < groupMemberList.length ; i++ ) {
	    						if ( groupMemberList[i].memberId.indexOf( query ) != -1 ) {
	    							newFilterGroupMember.push( groupMemberList[i] );
	    						}
	    					}
	    					scope.groupMemberList = newFilterGroupMember;
	    				}
	    			};
	    			*/
					scope.onRemoveGroupMember = function( $event ) {
						if ( $workingNode !== null && isSameNode( getNode( $event ) ) === false ) {
							cancelPreviousWork();
						}
						$workingNode = getNode( $event );
						removeGroupMember( $workingNode );
					};
					scope.onCancelRemoveGroupMember = function() {
						cancelRemoveGroupMember( $workingNode );
					};
					scope.onCloseAlert = function() {
						alarmUtilService.closeAlert( $elAlert, $elLoading );
					};
					scope.$on("alarmGroupMember.configuration.load", function( event, userGroupID, willBeAddedUser )  {
						reset();
						currentUserGroupID = userGroupID;
    					loadData( willBeAddedUser );
	    			});
	    			scope.$on("alarmGroupMember.configuration.selectNone", function()  {
	    				currentUserGroupID = "";
						initData();
	    			});
	    			scope.$on("alarmGroupMember.configuration.addUser", function( event, oUser )  {
	    				// if ( selectedUserGroupID() === false ) {
	    				// 	alarmBroadcastService.sendCallbackAddedUser( false );
	    				// 	return;
	    				// }
	    				// alarmUtilService.showLoading( $elLoading, false );
	    				// if ( hasUser( oUser.userId ) === false ) {
	    				// 	analyticsService.send( analyticsService.CONST.MAIN, analyticsService.CONST.CLK_ALARM_ADD_USER );
	    				// 	reset();
	    				// 	addMember( oUser );
	    				// } else {
	    				// 	alarmUtilService.showAlert( $elAlert, "Exist a same user in the lists.", true );
	    				// 	alarmBroadcastService.sendCallbackAddedUser( true );
	    				// }
	    			});
	    			scope.$on("alarmGroupMember.configuration.updateUser", function( event, oUser )  {
	    				// if ( hasUser( oUser.userId ) ) {
	    				// 	reset();
	    				// 	updateFromList( oUser );
	    				// }
	    			});
	    			scope.$on("alarmGroupMember.configuration.removeUser", function( event, userID )  {
	    				// if ( hasUser( userID ) ) {
	    				// 	reset();
	    				// 	scope.$apply(function() {
	    				// 		removeFromList( userID );
	    				// 	});
	    				// }
	    			});
					function getNode( $event ) {
						return $( $event.toElement || $event.target ).parents("li");
					}
	            }
	        };
	}]);
})(jQuery);