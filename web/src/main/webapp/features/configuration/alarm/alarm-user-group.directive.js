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
					var CONSTS = {
						MIN_GROUPNAME_LENGTH : 4,
						ENTER_AT_LEAST: "Enter at least 4 letters to search",
						EXIST_A_SAME: "Exist a same group name"
					};
					var selectedGroupNumber = "";
					var $workingNode = null;
	    			var bIsCreating = false;
	    			var bIsRemoving = false;
					var bIsUpdating = false;
	    			var bIsLoaded = false;
	    			scope.userGroupList = [];

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
							bIsLoaded = true;
							scope.userGroupList = aServerData;
							alarmUtilService.setTotal( $elTotal, scope.userGroupList.length );
							alarmUtilService.hide( $elLoading );
							alarmBroadcastService.sendLoadPinpointUser();
						}, function( oServerError ) {
							alarmUtilService.hide( $elLoading );
							$elAlert.find(".message").html( oServerError.errorMessage );
							alarmUtilService.show( $elAlert );
						}, $elAlert );

					}
	    			function selectGroup( $el ) {
	    				cancelPreviousWork();
	    				addSelectClass( alarmUtilService.extractID( $el ) );
	    				alarmBroadcastService.sendReloadWithUserGroupID( $el.find(".contents").html() );
	    			}
					function addSelectClass( newSelectedGroupNumber ) {
						$( "#" + scope.prefix + selectedGroupNumber ).removeClass("selected");
						$( "#" + scope.prefix + newSelectedGroupNumber ).addClass("selected");
						selectedGroupNumber = newSelectedGroupNumber;
					}

					function cancelPreviousWork() {
						if ( bIsCreating === true ) {
							cancelAddUserGroup();
						} else if ( bIsUpdating === true ) {
							removeUserGroup( $workingNode );
						} else if ( bIsRemoving === true ) {
							cancelRemoveUserGroup( $workingNode );
						}
					}
	    			scope.onAddUserGroup = function() {
						if ( bIsCreating === true ) return;
						cancelPreviousWork();

						bIsCreating = true;
						alarmUtilService.show( $elNewGroup );
						$elNewGroup.find("input").val("").focus();
	    			};
					scope.onCancelAddUserGroup = function() {
						cancelAddUserGroup();
					};
					function cancelAddUserGroup() {
						bIsCreating = false;
						$workingNode = null;
						alarmUtilService.hide( $elNewGroup );
						$elNewGroup.find( "input" ).attr( "placeholder", "New Group" ).val( "" );
					}
					scope.onApplyAddUserGroup = function() {
						alarmUtilService.show( $elLoading );
						var groupId = $elNewGroup.find("input").val();
						if ( groupId.length < CONSTS.MIN_GROUPNAME_LENGTH ) {
							alarmUtilService.hide( $elLoading );
							$elNewGroup.find( "input" ).attr( "placeholder", CONSTS.ENTER_AT_LEAST ).val( "" ).focus();
							return;
						}
						alarmUtilService.sendCRUD( "createUserGroup", { "id": groupId }, function( oServerData ) {
							scope.userGroupList.push({
								id: oServerData.id,
								number: oServerData.number
							});

							alarmBroadcastService.sendInit( name );
							alarmUtilService.setTotal( $elTotal, scope.userGroupList.length );
							cancelAddUserGroup();
							alarmUtilService.hide( $elLoading );
						}, function( oServerError ) {
							alarmUtilService.hide( $elLoading );
							$elAlert.find(".message").html( oServerError.errorMessage );
							alarmUtilService.show( $elAlert );
						});
					};
					scope.onAddUserGroupKeydown = function( $event ) {
						if ( $event.keyCode == 13 ) { // Enter
							scope.onApplyAddUserGroup();
						} else if ( $event.keyCode == 27 ) { // ESC
							cancelAddUserGroup();
							$event.stopPropagation();
						}
					};
					scope.onUpdateUserGroupKeydown = function( $event ) {
						if ( $event.keyCode == 13 ) { // Enter
							scope.onApplyUpdateUserGroup();
						} else if ( $event.keyCode == 27 ) { // ESC
							removeUserGroup( $workingNode );
							$event.stopPropagation();
						}
					};
	    			scope.onSearch = function() {
	    				cancelPreviousWork();
						alarmUtilService.show( $elLoading );
						var query = $.trim( $elSearchInput.val() );
	    				if ( query.length < CONSTS.MIN_GROUPNAME_LENGTH ) {
							$elSearchInput.val("");
							alarmUtilService.hide( $elLoading );
							return;
	    				}
	    				analyticsService.send( analyticsService.CONST.MAIN, analyticsService.CONST.CLK_ALARM_FILTER_USER_GROUP );
						loadData( query );
	    			};
	    			scope.onCloseAlert = function() {
	    				alarmUtilService.hide( $elAlert );
	    			};
					function removeUserGroup( $parent ) {
						if ( bIsUpdating === true ) {
							cancelUpdate( $parent );
						} else {
							if (bIsRemoving === false) {
								alarmUtilService.hide( $parent.find(".edit") );
								alarmUtilService.show( $parent.find(".remove-cancel") );
								$parent.find(".remove").addClass("remove-confirm");
								alarmUtilService.hide( $elLoading );
								bIsRemoving = true;
							} else {
								alarmUtilService.show( $elLoading );
								var groupNumber = alarmUtilService.extractID($parent);
								alarmUtilService.sendCRUD("removeUserGroup", {"id": groupNumber}, function () {
									// scope.$apply(function () {
										for (var i = 0; i < scope.userGroupList.length; i++) {
											if (scope.userGroupList[i].number == groupNumber) {
												scope.userGroupList.splice(i, 1);
												break;
											}
										}
										alarmBroadcastService.sendSelectionEmpty();
									// });
									bIsRemoving = false;
									$workingNode = null;
									alarmUtilService.setTotal($elTotal, scope.userGroupList.length);
									alarmUtilService.hide( $elLoading );
								}, function (oServerError) {
									alarmUtilService.hide( $elLoading );
									$elAlert.find(".message").html( oServerError.errorMessage );
									alarmUtilService.show( $elAlert );
								});
							}
						}
					}
					function cancelRemoveUserGroup( $parent ) {
						bIsRemoving = false;
						$workingNode = null;
						alarmUtilService.hide( $parent.find(".remove-cancel") );
						alarmUtilService.show( $parent.find(".edit") );
						$parent.find("span.remove").removeClass("remove-confirm");
					}
					function isSameNode( $current ) {
						return alarmUtilService.extractID( $workingNode ) === alarmUtilService.extractID( $current );
					}
					scope.onRemoveUserGroup = function( $event ) {
						if ( $workingNode !== null && isSameNode( getNode( $event ) ) === false ) {
							cancelPreviousWork(getNode($event));
						}
						$workingNode = getNode( $event );
						removeUserGroup( $workingNode );
					};
					scope.onCancelRemoveUserGroup = function() {
						cancelRemoveUserGroup( $workingNode );
					};
					scope.onUpdateUserGroup = function( $event ) {
						cancelPreviousWork();
						bIsUpdating = true;
						$workingNode = getNode( $event );
						alarmUtilService.hide( $workingNode.find(".edit") );
						alarmUtilService.show( $workingNode.find(".edit-confirm") );
						alarmUtilService.hide( $workingNode.find(".contents") );
						$workingNode.find("input").val( $workingNode.find(".contents").html() ).show();
						$workingNode.find("input").focus();
					};
					scope.onApplyUpdateUserGroup = function() {
						var groupNumber = alarmUtilService.extractID($workingNode);
						var groupName = $workingNode.find("input").val();

						if ( groupName === "" ) {
							$workingNode.find("input").attr("placeholder", CONSTS.ENTER_AT_LEAST).val("").focus();
							return;
						}
						alarmUtilService.show( $elLoading );
						if ( alarmUtilService.hasDuplicateItem( scope.userGroupList, function( userGroup ) {
							return userGroup.id === groupName;
						}) ) {
							alarmUtilService.hide( $elLoading );
							$workingNode.find("input").attr("placeholder", CONSTS.EXIST_A_SAME).val("").focus();
							return;
						}
						analyticsService.send( analyticsService.CONST.MAIN, analyticsService.CONST.CLK_ALARM_CREATE_USER_GROUP );
						alarmUtilService.sendCRUD( "updateUserGroup", { "number": groupNumber, "id": groupName }, function() {
							$workingNode.find("input").val( groupName );
							scope.$apply(function() {
								for (var i = 0; i < scope.userGroupList.length; i++) {
									if (scope.userGroupList[i].number === groupNumber) {
										scope.userGroupList[i].id = groupName;
									}
								}
							});
							cancelUpdate( $workingNode );
							alarmUtilService.hide( $elLoading );
						}, function( oServerError ) {
							alarmUtilService.hide( $elLoading );
							$elAlert.find( ".message" ).html( oServerError.errorMessage );
							alarmUtilService.show( $elAlert );
						});
					};
	    			scope.$on("alarmUserGroup.configuration.show", function() {
	    				if ( bIsLoaded === false ) {
	    					loadData();
	    				}
	    			});
					function cancelUpdate( $parent ) {
						alarmUtilService.hide( $parent.find(".edit-confirm") );
						alarmUtilService.show( $parent.find(".edit") );
						$parent.find("input").hide();
						alarmUtilService.show( $parent.find(".contents") );
						bIsUpdating = false;
						$workingNode = null;
					}
					function getNode( $event ) {
						return $( $event.toElement || $event.target ).parents("li");
					}
	            }
	        };
	    }
	]);
})(jQuery);