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
	    			var $elWrapper = $element.find(".wrapper");
	    			var $elTotal = $element.find(".total");
	    			var $elLoading = $element.find(".some-loading");
	    			var $elAlert = $element.find(".some-alert");
	    			var $elFilterInput = $element.find("div.filter-input input");
	    			var $elFilterEmpty = $element.find("div.filter-input button.trash");
	    			var $removeTemplate = $([
	    	           '<span class="position:absolute;right;0px">',
	    	               '<button class="btn btn-danger confirm-cancel"><span class="glyphicon glyphicon-remove" aria-hidden="true"></span></button>',
	    	               '<button class="btn btn-danger confirm-remove" style="margin-left:2px;"><span class="glyphicon glyphicon-ok" aria-hidden="true"></span></button>',
	       			   '</span>'
	    			].join(""));

	    			var currentUserGroupID = "";
	    			var isLoadedGroupMemberList = false;
	    			var isRemoving = false;
	    			var groupMemberList = scope.groupMemberList = [];
	    			
	    			var $elUL = $element.find(".some-list-content ul");
	    			$elUL.on("click", function($event) {
	    				var $target = $( $event.toElement || $event.target );
	    				var tagName = $target.get(0).tagName.toLowerCase();
	    				var $li = $target.parents("li");
	    				
	    				if ( tagName == "button" ) {
	    					if ( $target.hasClass("confirm-cancel") ) {
	    						removeCancel( $li );
	    					} else if ( $target.hasClass("confirm-remove") ) {
	    						removeConfirm( $li );
	    					}
	    				} else if ( tagName == "span" ) {
	    					if ( $target.hasClass("remove") ) {
	    						if ( isRemoving === true ) return;
	    						isRemoving = true;
	    	    				$li.addClass("remove").find("span.remove").hide().end().append($removeTemplate);
	    					} else if ( $target.hasClass("glyphicon-remove") ) {
	    						removeCancel( $li );
	    					} else if ( $target.hasClass("glyphicon-ok") ) {
	    						removeConfirm( $li );
	    					}
	    				}
	    			});
	    			function removeConfirm( $el ) {
	    				alarmUtilService.showLoading( $elLoading, false );
	    				removeMember( alarmUtilService.extractID( $el ) );
	    			}
	    			function removeCancel( $el ) {
						$el.find("span.right").remove().end().find("span.remove").show().end().removeClass("remove");
						isRemoving = false;
	    			}
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
	    			function removeFromList( memberID ) {
						var i = 0;
	    				if ( scope.groupMemberList != groupMemberList ) {
	    					for( i = 0 ; i < groupMemberList.length ; i++ ) {
	    						if ( groupMemberList[i].memberId == memberID ) {
	    							groupMemberList.splice(i, 1);
	    							break;
	    						}
	    					}	
	    				}
	    				for( i = 0 ; i < scope.groupMemberList.length ; i++ ) {
	    					if ( scope.groupMemberList[i].memberId == memberID ) {
	    						scope.groupMemberList.splice(i, 1);
	    						break;
	    					}
	    				}
	    			}
	    			function reset() {
	    				if ( isRemoving === true ) {
	    					$element.find("li.remove").each( function() {
	    						removeCancel( $(this) );
	    					});
	    				}
	    				alarmUtilService.unsetFilterBackground( $elWrapper );
	    				$elFilterInput.val("");
	    			}

	    			function addMember( oUser ) {
	    				alarmUtilService.sendCRUD( "addMemberInGroup", { "userGroupId": currentUserGroupID, "memberId": oUser.userId }, function( resultData ) {
	    					// @TODO
	    					// 많이 쓰는 놈 기준 3개를 뽑아 내야 함.
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
	    			function removeMember( memberID ) {
	    				alarmUtilService.sendCRUD( "removeMemberInGroup", { "userGroupId": currentUserGroupID, "memberId": memberID }, function( resultData ) {
	    					// @TODO
	    					// 많이 쓰는 놈 기준 3개를 뽑아 내야 함.
	    					scope.$apply(function() {
	    						removeFromList( memberID );
	    					});
	    					alarmUtilService.setTotal( $elTotal, groupMemberList.length );
	    					alarmUtilService.hide( $elLoading );
	    					isRemoving = false;
	    				}, function( errorData ) {}, $elAlert );
	    			}
	    			function loadList( willBeAddedUser ) {
	    				alarmUtilService.sendCRUD( "getGroupMemberListInGroup", { "userGroupId": currentUserGroupID }, function( resultData ) {
	    					// @TODO
	    					// 많이 쓰는 놈 기준 3개를 뽑아 내야 함.
	    					isLoadedGroupMemberList = true;
	    					groupMemberList = scope.groupMemberList = resultData;
	    					alarmUtilService.setTotal( $elTotal, groupMemberList.length );
	    					alarmUtilService.hide( $elLoading );
	    					
	    					if ( angular.isDefined( willBeAddedUser ) ) {
	    						addMember( willBeAddedUser );
	    					}
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
	    			
	    			scope.prefix = "alarmGroupMember_"; 
	    			scope.onRefresh = function() {
	    				if ( isRemoving === true ) return;
	    				
	    				if ( selectedUserGroupID() === false ) return;
	    				analyticsService.send( analyticsService.CONST.MAIN, analyticsService.CONST.CLK_ALARM_REFRESH_USER );
	    				$elFilterInput.val("");
	    				alarmUtilService.showLoading( $elLoading, false );
	    				loadList();
	    			};
	    			scope.onInputFilter = function($event) {
	    				if ( isRemoving === true ) return;
	    				
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
	    				if ( isRemoving === true ) return;
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
	    						alarmUtilService.unsetFilterBackground( $elWrapper );
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
	    					alarmUtilService.setFilterBackground( $elWrapper );
	    				}
	    			};
	    			scope.onFilterEmpty = function() {
	    				if ( isRemoving === true ) return;
	    				if ( $.trim( $elFilterInput.val() ) === "" ) return;
	    				$elFilterInput.val("");
	    				scope.onFilterGroup();
	    			};
	    			scope.onCloseAlert = function() {
	    				alarmUtilService.closeAlert( $elAlert, $elLoading );
	    			};
	    			scope.$on("alarmGroupMember.configuration.load", function( event, userGroupID, willBeAddedUser )  {	    				
	    				currentUserGroupID = userGroupID;
    					reset();
    					loadList( willBeAddedUser );
	    			});
	    			scope.$on("alarmGroupMember.configuration.selectNone", function( event )  {
	    				currentUserGroupID = "";
	    				groupMemberList = scope.groupMemberList = [];
	    			});
	    			scope.$on("alarmGroupMember.configuration.addUser", function( event, oUser )  {
	    				
	    				if ( selectedUserGroupID() === false ) {
	    					alarmBroadcastService.sendCallbackAddedUser( false );
	    					return;
	    				}
	    				alarmUtilService.showLoading( $elLoading, false );
	    				if ( hasUser( oUser.userId ) === false ) {
	    					analyticsService.send( analyticsService.CONST.MAIN, analyticsService.CONST.CLK_ALARM_ADD_USER );
	    					reset();
	    					addMember( oUser );
	    				} else {
	    					alarmUtilService.showAlert( $elAlert, "Exist a same user in the lists.", true );
	    					alarmBroadcastService.sendCallbackAddedUser( true );
	    				}
	    			});
	    			scope.$on("alarmGroupMember.configuration.updateUser", function( event, oUser )  {
	    				if ( hasUser( oUser.userId ) ) {
	    					reset();
	    					updateFromList( oUser );
	    				}
	    			});
	    			scope.$on("alarmGroupMember.configuration.removeUser", function( event, userID )  {
	    				if ( hasUser( userID ) ) {
	    					reset();
	    					scope.$apply(function() {
	    						removeFromList( userID );
	    					});
	    				}
	    			});
	            }
	        };
	}]);
})(jQuery);