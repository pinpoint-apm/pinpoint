(function($) {
	'use strict';
	/**
	 * (en)alarmUserGroupDirective 
	 * @ko alarmUserGroupDirective
	 * @group Directive
	 * @name alarmUserGroupDirective
	 * @class
	 */	
	
	pinpointApp.directive('alarmUserGroupDirective', [ '$rootScope', '$timeout', 'helpContentTemplate', 'helpContentService', 'AlarmUtilService', 'AlarmBroadcastService', 'AnalyticsService', 'globalConfig',
	    function ($rootScope, $timeout, helpContentTemplate, helpContentService, alarmUtilService, alarmBroadcastService, analyticsService, globalConfig) {
	        return {
	            restrict: 'EA',
	            replace: true,
	            templateUrl: 'features/configuration/alarm/alarmUserGroup.html?v=' + G_BUILD_TIME,
	            scope: true,
	            link: function (scope, element) {
	            	scope.prefix = "alarmUserGroup_";
	            	
	            	var selectedGroupNumber = "";
	    			var isCreate = true; // update
	    			var isRemoving = false;
	    			var isLoadedUserGroupList = false;
	    			var userGroupList = scope.userGroupList = [];
	    			
	            	function addSelectClass( newSelectedGroupNumber ) {
	    				if ( selectedGroupNumber !== "" ) {
	    					$( "#" + scope.prefix + selectedGroupNumber ).removeClass("selected");
	    				}
	    				$( "#" + scope.prefix + newSelectedGroupNumber ).addClass("selected");
	    				selectedGroupNumber = newSelectedGroupNumber;
	    			}

	            	var $element = element;
	    			var $elWrapper = $element.find(".wrapper");
	    			var $elTotal = $element.find(".total");
	    			var $elLoading = $element.find(".some-loading");
	    			var $elAlert = $element.find(".some-alert");
	    			var $elSearchInput = $element.find("div.filter-input input");
	    			var $elSearchEmpty = $element.find("div.filter-input button.trash");
	    			var $elEdit = $element.find(".some-edit-content");
	    			var $elEditInput = $elEdit.find("input");
	    			var $elEditGuide = $elEdit.find(".title-message");
	    			var $removeTemplate = $([
	    	           '<span class="right">',
	    	               '<button class="btn btn-danger confirm-cancel"><span class="glyphicon glyphicon-remove" aria-hidden="true"></span></button>',
	    	               '<button class="btn btn-danger confirm-remove" style="margin-left:2px;"><span class="glyphicon glyphicon-ok" aria-hidden="true"></span></button>',
	       			   '</span>'
	    			].join(""));
	    			
	    			var $elUL = $element.find(".some-list-content ul");
	    			$elUL.on("click", function($event) {
	    				var $target = $( $event.toElement || $event.target );
	    				var tagName = $target.get(0).tagName.toLowerCase();
	    				var $li = $target.parents("li");
	    				
	    				if ( tagName === "button" ) {
	    					if ( $target.hasClass("edit") ) {
	    						scope.onUpdate( $event );
	    					} else if ( $target.hasClass("confirm-remove") ) {
	    						removeConfirm( $li );
	    					} else if ( $target.hasClass("confirm-cancel") ) {
	    						revmoeCancel( $li );
	    					}
	    				} else if ( tagName === "span" ) {
	    					if ( $target.hasClass("remove") ) {
	    						if ( isRemoving === true ) return;
	    						isRemoving = true;
	    	    				$li.addClass("remove").find("span.remove").hide().end().append($removeTemplate);
	    					} else if ( $target.hasClass("contents") ) {
	    						selectGroup( $li );
	    					} else if( $target.hasClass("glyphicon-edit") ) {
	    						scope.onUpdate( $event );
	    					} else if ( $target.hasClass("glyphicon-remove") ) {
	    						removeCancel( $li );
	    					} else if ( $target.hasClass("glyphicon-ok") ) {
	    						removeConfirm( $li );
	    					}
	    				} else if( tagName === "li" ) {
	    					selectGroup( $target );
	    				}
	    			});
	    			function reset() {
	    				alarmUtilService.unsetFilterBackground( $elWrapper );
	    				$elSearchInput.val("");
	    			}
	    			
	    			function removeConfirm( $el ) {
	    				alarmUtilService.showLoading( $elLoading, false );
	    				removeGroup( alarmUtilService.extractID( $el ), $el.find("span.contents").html() );
	    			}
	    			function removeCancel( $el ) {
	    				$el.find("span.right").remove().end().find("span.remove").show().end().removeClass("remove");
	    				isRemoving = false;
	    			}
	    			function selectGroup( $el ) {
	    				if ( isRemoving === true ) return;
	    				addSelectClass( alarmUtilService.extractID( $el ) );
	    				alarmBroadcastService.sendReloadWithUserGroupID( $el.find("span.contents").html() );
	    			}
	    			
	    			function createGroup( name ) {
	    				alarmUtilService.sendCRUD( "createUserGroup", { "id": name }, function( resultData ) {
	    					// @TODO
	    					// 많이 쓰는 놈 기준 3개를 뽑아 내야 함.
	    					userGroupList.push({
	    						number: resultData.number,
	    						id: name
	    					});

	    					if ( globalConfig.userId ) {	    						
		    					alarmBroadcastService.sendInit( name, {
	    							userId: globalConfig.userId,
	    							name: globalConfig.userName,
	    							department: globalConfig.userDepartment 
	    						});
	    					} else {
	    						alarmBroadcastService.sendInit( name );
	    					}
	    					
    						$timeout(function() {
    							addSelectClass( resultData.number );
    						});
	    					alarmUtilService.setTotal( $elTotal, userGroupList.length );
	    					alarmUtilService.hide( $elLoading, $elEdit );
	    				}, function( errorData ) {}, $elAlert );
	    			}
	    			function updateGroup( number, name ) {
	    				alarmUtilService.sendCRUD( "updateUserGroup", { "number": number, "id": name }, function( resultData ) {
	    					// @TODO
	    					// 많이 쓰는 놈 기준 3개를 뽑아 내야 함.
	    					for( var i = 0 ; i < userGroupList.length ; i++ ) {
	    						if ( userGroupList[i].number == number ) {
	    							userGroupList[i].id = name;
	    						}
	    					}
	    					alarmUtilService.hide( $elLoading, $elEdit );
	    				}, function( errorData ) {}, $elAlert );
	    			}
	    			function removeGroup( number, name ) {
	    				alarmUtilService.sendCRUD( "removeUserGroup", { "id": name }, function( resultData ) {
	    					scope.$apply(function() {
		    					for( var i = 0 ; i < userGroupList.length ; i++ ) {
		    						if ( userGroupList[i].number == number ) {
		    							userGroupList.splice(i, 1);
		    							break;
		    						}
		    					}
		    					if ( userGroupList.length > 0 ) {
			    					$timeout(function() {
		    							addSelectClass( userGroupList[0].number );
		    						});
			    					alarmBroadcastService.sendReloadWithUserGroupID( userGroupList[0].id );
		    					} else {
		    						alarmBroadcastService.sendSelectionEmpty();
		    					}
	    					});	    					
	    					alarmUtilService.setTotal( $elTotal, userGroupList.length );
	    					alarmUtilService.hide( $elLoading );
	    					isRemoving = false;					
	    				}, function( errorData ) {}, $elAlert );
	    			}
	    			function loadGroupList( isFirst, sParam ) {
	    				alarmUtilService.sendCRUD( "getUserGroupList", 
	    						angular.isUndefined(sParam) || sParam === "" ? { "userId" : (globalConfig.userId || "") } : { "userGroupId" : sParam }, function( resultData ) {
	    					// @TODO
	    					// 많이 쓰는 놈 기준 3개를 뽑아 내야 함.
	    					isLoadedUserGroupList = true;
	    					userGroupList = scope.userGroupList = resultData;
	    					alarmUtilService.setTotal( $elTotal, userGroupList.length );
	    					alarmUtilService.hide( $elLoading );
	    					if( userGroupList.length > 0 ) {
		    					if ( isFirst ) {
		    						alarmBroadcastService.sendInit( userGroupList[0].id );
		    						selectedGroupNumber = userGroupList[0].number;
		    					}
		    					$timeout(function() {
		    						addSelectClass( selectedGroupNumber );
	    						});
	    					}
	    					if ( globalConfig.userId ) {
	    						alarmBroadcastService.sendLoadPinpointUser( globalConfig.userDepartment );
	    					}
	    				}, function( errorData ) {}, $elAlert );			

	    			}
	    			scope.onRefresh = function() {
	    				if ( isRemoving === true ) return;
	    				analyticsService.send( analyticsService.CONST.MAIN, analyticsService.CONST.CLK_ALARM_REFRESH_USER_GROUP );
	    				reset();
	    				alarmUtilService.showLoading( $elLoading, false );
	    				loadGroupList( false, $.trim( $elSearchInput.val() ) );
	    			};
	    			scope.onCreate = function() {
	    				if ( isRemoving === true ) return;
	    				
	    				isCreate = true;
	    				$elEditGuide.html( "Create new alarm group" );
	    				$elEditInput.val("");
	    				alarmUtilService.show( $elEdit );
	    				$elEditInput.focus();
	    			};
	    			scope.onUpdate = function($event) {
	    				if ( isRemoving === true ) return;
	    				
	    				isCreate = false;
	    				var $el = $( $event.toElement || $event.target ).parents("li");
	    				var userGroupNumber = alarmUtilService.extractID( $el );
	    				var userGroupID = $el.find("span.contents").html();
	    				
	    				$elEditGuide.html( "Input new name of \"" + userGroupID + "\"." );
	    				$elEditInput.val( userGroupID ).prop("id", "updateUserGroup_" + userGroupNumber);
	    				alarmUtilService.show( $elEdit );
	    				$elEditInput.focus().select();
	    			};
	    			scope.onSearch = function() {
	    				if ( isRemoving === true ) return;
	    				var query = $.trim( $elSearchInput.val() );
	    				if ( query.length !== 0 && query.length < 3 ) {
	    					alarmUtilService.showLoading( $elLoading, false );
	    					alarmUtilService.showAlert( $elAlert, "You must enter at least three characters.");
	    					return;
	    				}
	    				analyticsService.send( analyticsService.CONST.MAIN, analyticsService.CONST.CLK_ALARM_FILTER_USER_GROUP );
	    				alarmUtilService.showLoading( $elLoading, false );
	    				loadGroupList( false, query );
	    			};
	    			scope.onInputEdit = function($event) {
	    				if ( $event.keyCode == 13 ) { // Enter
	    					scope.onApplyEdit();
	    				} else if ( $event.keyCode == 27 ) { // ESC
	    					scope.onCancelEdit();
	    					$event.stopPropagation();
	    				}
	    			};
	    			scope.onCancelEdit = function() {
	    				alarmUtilService.hide( $elEdit );
	    			};
	    			scope.onApplyEdit = function() {
	    				var groupName = $.trim( $elEditInput.val() );
	    				if ( groupName === "" ) {
	    					alarmUtilService.showLoading( $elLoading, true );
	    					alarmUtilService.showAlert( $elAlert, "Input group id.");	    					
	    					return;
	    				}
	    				
	    				alarmUtilService.showLoading( $elLoading, true );
	    				if ( alarmUtilService.hasDuplicateItem( userGroupList, function( userGroup ) {
	    					return userGroup.id == groupName;
	    				}) && isCreate === true ) {
	    					alarmUtilService.showAlert( $elAlert, "Exist a same group name in the lists." );
	    					return;
	    				}
	    				if ( isCreate ) {
	    					analyticsService.send( analyticsService.CONST.MAIN, analyticsService.CONST.CLK_ALARM_CREATE_USER_GROUP );
	    					createGroup( groupName );
	    				} else {
	    					updateGroup( alarmUtilService.extractID( $elEditInput ), groupName );
	    				}
	    			};
	    			scope.onCloseAlert = function() {
	    				alarmUtilService.closeAlert( $elAlert, $elLoading );
	    			};	    			
	    			scope.$on("alarmUserGroup.configuration.show", function() {
	    				if ( isLoadedUserGroupList === false ) {
	    					loadGroupList( true );
	    				}
	    			});
	            }
	        };
	    }
	]);
})(jQuery);