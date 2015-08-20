(function($) {
	'use strict';
	/**
	 * (en)alarmUserGroupDirective 
	 * @ko alarmUserGroupDirective
	 * @group Directive
	 * @name alarmUserGroupDirective
	 * @class
	 */	
	
	pinpointApp.directive('alarmUserGroupDirective', [ '$rootScope', '$timeout', 'helpContentTemplate', 'helpContentService', 'AlarmListTemplateService',
	    function ($rootScope, $timeout, helpContentTemplate, helpContentService, $alarmListTemplateService) {
	        return {
	            restrict: 'EA',
	            replace: true,
	            templateUrl: 'features/configuration/alarm/alarmUserGroup.html',
	            scope: true,
	            link: function (scope, element) {
	            	scope.prefix = "alarmUserGroup_";
	            	
	            	var selectedGroupNumber = "";
	    			var isCreate = true; // update
	    			var isRemoving = false;
	    			var isLoadedUserGroupList = false;
	    			var userGroupList = scope.userGroupList = [];
	    			
	            	function selectGroup( newSelectedGroupNumber ) {
	    				if ( selectedGroupNumber != "" ) {
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
	    			var $elGuideMessage = $element.find(".guide-message");
	    			var $elFilterInput = $element.find("div.filter-input input");
	    			var $elFilterEmpty = $element.find("div.filter-input button.trash");
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
	    				var tagName = $event.toElement.tagName.toLowerCase();
	    				var $target = $($event.toElement);
	    				var $li = $target.parents("li");
	    				
	    				if ( tagName == "button" ) {
	    					if ( $target.hasClass("edit") ) {
	    						scope.onUpdate( $event );
	    					} else if ( $target.hasClass("confirm-remove") ) {
	    						removeConfirm( $li );
	    					} else if ( $target.hasClass("confirm-cancel") ) {
	    						revmoeCancel( $li );
	    					}
	    				} else if ( tagName == "span" ) {
	    					if ( $target.hasClass("remove") ) {
	    						isRemoving = true;
	    	    				$li.addClass("remove").find("span.remove").hide().end().append($removeTemplate);
	    					} else if ( $target.hasClass("contents") ) {
	    						if ( isRemoving == true ) return;
	    	    				selectGroup( $li.prop("id").split("_")[1] );
	    	    				loadGroupMember( $target.html() );
	    					} else if( $target.hasClass("glyphicon-edit") ) {
	    						scope.onUpdate( $event );
	    					} else if ( $target.hasClass("glyphicon-remove") ) {
	    						removeCancel( $li );
	    					} else if ( $target.hasClass("glyphicon-ok") ) {
	    						removeConfirm( $li );
	    					}
	    				} else if( tagName == "li" ) {
	    					if ( isRemoving == true ) return;
		    				selectGroup( $target.prop("id").split("_")[1] );	
	    				}
	    			});
	    			
	    			$alarmListTemplateService.setGuideEvent( scope, $elUL, [
	                  { selector: "li", 					name: "contents" },
	                  { selector: "span.contents", 			name: "contents" },
	                  { selector: "span.remove", 			name: "remove" },
	                  { selector: "button.confirm-cancel", 	name: "removeCancel" },
	                  { selector: "button.confirm-remove", 	name: "removeConfirm" }
	    			]);
	    			function removeConfirm( $el ) {
	    				$alarmListTemplateService.showLoading( $elLoading, false );
	    				removeGroup( $el.prop("id").split("_")[1], $el.find("span.contents").html() );
	    			}
	    			function removeCancel( $el ) {
	    				$el.find("span.right").remove().end().find("span.remove").show().end().removeClass("remove");
	    				isRemoving = false;
	    			}
	    			
	    			function createGroup( name ) {
	    				$alarmListTemplateService.sendCRUD( "createUserGroup", { "id": name }, function( resultData ) {
	    					// @TODO
	    					// 많이 쓰는 놈 기준 3개를 뽑아 내야 함.
	    					userGroupList.push({
	    						number: resultData.number,
	    						id: name
	    					});
	    					$alarmListTemplateService.setTotal( $elTotal, userGroupList.length );
	    					$alarmListTemplateService.hide( $elLoading, $elEdit );
	    				}, $elAlert );
	    			}
	    			function updateGroup( number, name ) {
	    				$alarmListTemplateService.sendCRUD( "updateUserGroup", { "number": number, "id": name }, function( resultData ) {
	    					// @TODO
	    					// 많이 쓰는 놈 기준 3개를 뽑아 내야 함.
	    					for( var i = 0 ; i < userGroupList.length ; i++ ) {
	    						if ( userGroupList[i].number == number ) {
	    							userGroupList[i].id = name;
	    						}
	    					}
	    					$alarmListTemplateService.hide( $elLoading, $elEdit );
	    				}, $elAlert );
	    			}
	    			function removeGroup( number, name ) {
	    				$alarmListTemplateService.sendCRUD( "removeUserGroup", { "id": name }, function( resultData ) {
	    					scope.$apply(function() {
		    					for( var i = 0 ; i < userGroupList.length ; i++ ) {
		    						if ( userGroupList[i].number == number ) {
		    							userGroupList.splice(i, 1);
		    							break;
		    						}
		    					}
		    					if ( userGroupList.length > 0 ) {
			    					$timeout(function() {
		    							selectGroup( userGroupList[0].number );
		    						});
			    					loadGroupMember( userGroupList[0].id );
		    					} else {
		    						selectNone();
		    					}
	    					});	    					
	    					$alarmListTemplateService.setTotal( $elTotal, userGroupList.length );
	    					$alarmListTemplateService.hide( $elLoading );
	    					isRemoving = false;					
	    				}, $elAlert );
	    			}
	    			function loadGroupList( isFirst ) {
	    				$alarmListTemplateService.sendCRUD( "getUserGroupList", {}, function( resultData ) {
	    					// @TODO
	    					// 많이 쓰는 놈 기준 3개를 뽑아 내야 함.
	    					isLoadedUserGroupList = true;
	    					userGroupList = scope.userGroupList = resultData;
	    					$alarmListTemplateService.setTotal( $elTotal, userGroupList.length );
	    					$alarmListTemplateService.hide( $elLoading );
	    					if ( isFirst ) {
	    						loadGroupMember( userGroupList[0].id );
	    						$timeout(function() {
	    							selectGroup( userGroupList[0].number );
	    						});
	    					}
	    					scope.onLeave();					
	    				}, $elAlert );			
	    			};
	    			function loadGroupMember( userGroupID ) {
	    				$rootScope.$broadcast( "alarmGroupMember.configuration.load", userGroupID );
	    				$rootScope.$broadcast( "alarmPinpointUser.configuration.load", userGroupID );
	    				$rootScope.$broadcast( "alarmRule.configuration.load", userGroupID );
	    			}
	    			function selectNone() {
	    				$rootScope.$broadcast( "alarmGroupMember.configuration.selectNone" );
	    				$rootScope.$broadcast( "alarmRule.configuration.selectNone" );
	    			}

	    			scope.onRefresh = function() {
	    				if ( isRemoving == true ) return;
	    				
	    				$elFilterInput.val("");
	    				$alarmListTemplateService.showLoading( $elLoading, false );
	    				loadGroupList( false );
	    			};
	    			scope.onCreate = function() {
	    				if ( isRemoving == true ) return;
	    				
	    				isCreate = true;
	    				$elEditGuide.html( "Create new alarm group" );
	    				$elEditInput.val("");
	    				$alarmListTemplateService.show( $elEdit );
	    				$elEditInput.focus();
	    			};
	    			scope.onUpdate = function($event) {
	    				if ( isRemoving == true ) return;
	    				
	    				isCreate = false;
	    				var $el = $( $event.toElement ).parents("li");
	    				var userGroupNumber = $el.prop("id").split("_")[1];
	    				var userGroupID = $el.find("span.contents").html();
	    				
	    				$elEditGuide.html( "\"" + userGroupID + "\"의 새로운 이름을 입력하세요." );
	    				$elEditInput.val( userGroupID ).prop("id", "updateUserGroup_" + userGroupNumber);
	    				$alarmListTemplateService.show( $elEdit );
	    				$elEditInput.focus().select();
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
	    					if ( scope.userGroupList.length != userGroupList.length ) {
	    						scope.userGroupList = userGroupList;
	    						$alarmListTemplateService.unsetFilterBackground( $elWrapper );
	    					}
	    					$elFilterEmpty.addClass("disabled");
	    				} else {
	    					var newFilterUserGroup = [];
	    					var length = userGroupList.length;
	    					for( var i = 0 ; i < userGroupList.length ; i++ ) {
	    						if ( userGroupList[i].id.indexOf( query ) != -1 ) {
	    							newFilterUserGroup.push( userGroupList[i] );
	    						}
	    					}
	    					scope.userGroupList = newFilterUserGroup;
	    					$alarmListTemplateService.setFilterBackground( $elWrapper );
	    				}
	    			};
	    			scope.onFilterEmpty = function() {
	    				if ( isRemoving == true ) return;
	    				if ( $.trim( $elFilterInput.val() ) == "" ) return;
	    				$elFilterInput.val("");
	    				scope.onFilterGroup();
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
	    				$alarmListTemplateService.hide( $elEdit );
	    			};
	    			scope.onApplyEdit = function() {
	    				var groupName = $.trim( $elEditInput.val() );
	    				if ( groupName == "" ) {
	    					scope.onEnter("notEmpty");
	    					return;
	    				}
	    				
	    				$alarmListTemplateService.showLoading( $elLoading, true );
	    				if ( $alarmListTemplateService.hasDuplicateItem( userGroupList, function( userGroup ) {
	    					return userGroup.id == groupName;
	    				}) && isCreate == true ) {
	    					$alarmListTemplateService.showAlert( $elAlert, "동일한 이름을 가진 그룹이 이미 있습니다." );
	    					return;
	    				}
	    				if ( isCreate ) {
	    					createGroup( groupName );
	    				} else {
	    					updateGroup( $elEditInput.prop("id").split("_")[1], groupName );
	    				}
	    			};
	    			scope.onCloseAlert = function() {
	    				$alarmListTemplateService.closeAlert( $elAlert, $elLoading );
	    			};
	    			// onEnter, onLeave
	    			scope.onEnter = function( type ) {
	    				$alarmListTemplateService.setGuide( $elGuideMessage, "userGroup", type, userGroupList.length );
	    			};
	    			scope.onLeave = function( type ) {
	    				$alarmListTemplateService.setGuide( $elGuideMessage, "userGroup", "" );
	    			}
	    			
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