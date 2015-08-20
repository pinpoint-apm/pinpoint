(function($) {
	'use strict';
	/**
	 * (en)alarmGroupMemberDirective 
	 * @ko alarmGroupMemberDirective
	 * @group Directive
	 * @name alarmGroupMemberDirective
	 * @class
	 */	
	pinpointApp.directive('alarmGroupMemberDirective', [ '$rootScope', 'helpContentTemplate', 'helpContentService', 'AlarmListTemplateService',
	    function ($rootScope, helpContentTemplate, helpContentService, $alarmListTemplateService) {
	        return {
	            restrict: 'EA',
	            replace: true,
	            templateUrl: 'features/configuration/alarm/alarmGroupMember.html',
	            scope: true,
	            link: function (scope, element) {
	            	//@TODO
	    			//통계 추가할 것.
	    			//$at($at.FILTEREDMAP_PAGE);

	            	var $element = $(element);
	    			var $elWrapper = $element.find(".wrapper");
	    			var $elTotal = $element.find(".total");
	    			var $elLoading = $element.find(".some-loading");
	    			var $elAlert = $element.find(".some-alert");
	    			var $elGuideMessage = $element.find(".guide-message");
	    			var $elFilterInput = $element.find("div.filter-input input");
	    			var $elFilterEmpty = $element.find("div.filter-input button.trash");
	    			var $removeTemplate = $([
	    	           '<span class="right">',
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
	    				var tagName = $event.toElement.tagName.toLowerCase();
	    				var $target = $($event.toElement);
	    				var $li = $target.parents("li");
	    				
	    				if ( tagName == "button" ) {
	    					if ( $target.hasClass("confirm-cancel") ) {
	    						removeCancel( $li );
	    					} else if ( $target.hasClass("confirm-remove") ) {
	    						removeConfirm( $li );
	    					}
	    				} else if ( tagName == "span" ) {
	    					if ( $target.hasClass("remove") ) {
	    						isRemoving = true;
	    	    				$li.addClass("remove").find("span.remove").hide().end().append($removeTemplate);
	    					} else if ( $target.hasClass("glyphicon-remove") ) {
	    						removeCancel( $li );
	    					} else if ( $target.hasClass("glyphicon-ok") ) {
	    						removeConfirm( $li );
	    					}
	    				}
	    			});
	    			$alarmListTemplateService.setGuideEvent( scope, $elUL, [
	                  { selector: "span.remove", 			name: "remove" },
	                  { selector: "button.confirm-cancel", 	name: "removeCancel" },
	                  { selector: "button.confirm-remove", 	name: "removeConfirm" }
	    			]);
	    			function removeConfirm( $el ) {
	    				$alarmListTemplateService.showLoading( $elLoading, false );
	    				removeMember( $el.prop("id").split("_")[1] );
	    			}
	    			function removeCancel( $el ) {
						$el.find("span.right").remove().end().find("span.remove").show().end().removeClass("remove");
						isRemoving = false;
	    			}
	    			function callbackAddUser( bIsSuccess ) {
	    				scope.$parent.$broadcast( "alarmPinpointUser.configuration.addUserCallback", bIsSuccess );
	    			}
	    			function updateFromList( oUser ) {
	    				if ( scope.groupMemberList != groupMemberList ) {
	    					for( var i = 0 ; i < groupMemberList.length ; i++ ) {
	    						if ( groupMemberList[i].memberId == oUser.memberId ) {
	    							groupMemberList[i].name = oUser.name;
	    							groupMemberList[i].department = oUser.department;
	    							break;
	    						}
	    					}	
	    				}
	    				for( var i = 0 ; i < scope.groupMemberList.length ; i++ ) {
	    					if ( scope.groupMemberList[i].memberId == oUser.memberId ) {
	    						groupMemberList[i].name = oUser.name;
	    						groupMemberList[i].department = oUser.department;
	    						break;
	    					}
	    				}
	    			}
	    			function removeFromList( memberID ) {
	    				if ( scope.groupMemberList != groupMemberList ) {
	    					for( var i = 0 ; i < groupMemberList.length ; i++ ) {
	    						if ( groupMemberList[i].memberId == memberID ) {
	    							groupMemberList.splice(i, 1);
	    							break;
	    						}
	    					}	
	    				}
	    				for( var i = 0 ; i < scope.groupMemberList.length ; i++ ) {
	    					if ( scope.groupMemberList[i].memberId == memberID ) {
	    						scope.groupMemberList.splice(i, 1);
	    						break;
	    					}
	    				}
	    			}

	    			function addMember( oUser ) {
	    				$alarmListTemplateService.sendCRUD( "addMemberInGroup", { "userGroupId": currentUserGroupID, "memberId": oUser.userId }, function( resultData ) {
	    					// @TODO
	    					// 많이 쓰는 놈 기준 3개를 뽑아 내야 함.
	    					scope.groupMemberList.push({
	    						"userGroupId": currentUserGroupID, 
	    						"memberId": oUser.userId,
	    						"name": oUser.name,
	    						"department": oUser.department
	    					});
	    					$alarmListTemplateService.setTotal( $elTotal, groupMemberList.length );
	    					$alarmListTemplateService.hide( $elLoading );
	    					callbackAddUser( true );
	    				}, $elAlert );
	    			}
	    			function removeMember( memberID ) {
	    				$alarmListTemplateService.sendCRUD( "removeMemberInGroup", { "userGroupId": currentUserGroupID, "memberId": memberID }, function( resultData ) {
	    					// @TODO
	    					// 많이 쓰는 놈 기준 3개를 뽑아 내야 함.
	    					scope.$apply(function() {
	    						removeFromList( memberID );
	    					});
	    					$alarmListTemplateService.setTotal( $elTotal, groupMemberList.length );
	    					$alarmListTemplateService.hide( $elLoading );
	    					isRemoving = false;
	    				}, $elAlert );
	    			}
	    			function loadList() {
	    				$alarmListTemplateService.sendCRUD( "getGroupMemberListInGroup", { "userGroupId": currentUserGroupID }, function( resultData ) {
	    					// @TODO
	    					// 많이 쓰는 놈 기준 3개를 뽑아 내야 함.
	    					isLoadedGroupMemberList = true;
	    					groupMemberList = scope.groupMemberList = resultData;
	    					$alarmListTemplateService.setTotal( $elTotal, groupMemberList.length );
	    					$alarmListTemplateService.hide( $elLoading );
	    					scope.onLeave();
	    					
	    				}, $elAlert );		
	    			};
	    			function hasUser( userID ) {
	    				return $( "#" + scope.prefix + userID ).length > 0;
	    			}
	    			
	    			scope.prefix = "alarmGroupMember_"; 
	    			scope.onRefresh = function() {
	    				if ( isRemoving == true ) return;
	    				
	    				$elFilterInput.val("");
	    				$alarmListTemplateService.showLoading( $elLoading, false );
	    				loadList();
	    			};
	    			scope.onInputFilter = function($event) {
	    				if ( isRemoving == true ) return;
	    				
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
	    				if ( isRemoving == true ) return;
	    				var query = $.trim( $elFilterInput.val() );
	    				if ( query.length != 0 && query.length < 3 ) {
	    					scope.onEnter("greater2");
	    					return;
	    				}
	    				if ( query == "" ) {
	    					if ( scope.groupMemberList.length != groupMemberList.length ) {
	    						scope.groupMemberList = groupMemberList;
	    						$alarmListTemplateService.unsetFilterBackground( $elWrapper );
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
	    					$alarmListTemplateService.setFilterBackground( $elWrapper );
	    				}
	    			};
	    			scope.onFilterEmpty = function() {
	    				if ( isRemoving == true ) return;
	    				if ( $.trim( $elFilterInput.val() ) == "" ) return;
	    				$elFilterInput.val("");
	    				scope.onFilterGroup();
	    			};
	    			scope.onCloseAlert = function() {
	    				$alarmListTemplateService.closeAlert( $elAlert, $elLoading );
	    			};
	    			// onEnter, onLeave
	    			scope.onEnter = function( type ) {
	    				$alarmListTemplateService.setGuide( $elGuideMessage, "groupMember", type, groupMemberList.length );
	    			};
	    			scope.onLeave = function( type ) {
	    				$alarmListTemplateService.setGuide( $elGuideMessage, "groupMember", "" );
	    			}
	    			scope.$on("alarmGroupMember.configuration.load", function( event, userGroupID )  {
	    				currentUserGroupID = userGroupID;
//	    				if ( isLoadedGroupMemberList === false ) {
	    					loadList();
//	    				}
	    			});
	    			scope.$on("alarmGroupMember.configuration.selectNone", function( event )  {
	    				currentUserGroupID = "";
	    				groupMemberList = scope.groupMemberList = [];
	    			});
	    			scope.$on("alarmGroupMember.configuration.addUser", function( event, oUser )  {
	    				$alarmListTemplateService.showLoading( $elLoading, false );
	    				if ( hasUser( oUser.userId ) == false ) {
	    					addMember( oUser );
	    				} else {
	    					$alarmListTemplateService.showAlert( $elAlert, "이미 등록된 사용자입니다.", true );
	    				}
	    			});
	    			scope.$on("alarmGroupMember.configuration.updateUser", function( event, oUser )  {
	    				if ( hasUser( oUser.userId ) ) {
	    					updateFromList( oUser );
	    				}
	    			});
	    			scope.$on("alarmGroupMember.configuration.removeUser", function( event, userID )  {
	    				if ( hasUser( userID ) ) {
	    					removeFromList( userID );
	    				}
	    			});
	            }
	        };
	}]);
})(jQuery);