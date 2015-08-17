(function($) {
	'use strict';
	/**
	 * (en)AlarmUserGroupCtrl 
	 * @ko AlarmUserGroupCtrl
	 * @group Controller
	 * @name AlarmUserGroupCtrl
	 * @class
	 */
	pinpointApp.constant('AlarmUserGroupConfig', {
	});	

	pinpointApp.controller('AlarmUserGroupCtrl', [ '$scope','$element', 'AlarmUserGroupConfig', 'AlarmAjaxService',
	    function ($scope, $element, $constant, $ajaxService) {
			//@TODO
			//통계 추가할 것.
			//$at($at.FILTEREDMAP_PAGE);
			console.log( "init AlarmCtrl", $element );

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
			
			var selectedGroupNumber = "";
			var isLoadedUserGroupList = false;
			var isCreate = true; // update
			var isRemoving = false;
			var userGroupList = $scope.userGroupList = [];
			
			var $elUL = $element.find(".some-list-content ul");
			$elUL.on("dblclick", "li", function($event) {
				$scope.onUpdate( $event );
			}).on("dblclick", "span.contents", function($event) {
				$scope.onUpdate( $event );
			}).on("click", "li", function($event) {
				if ( isRemoving == true ) return;
				selectGroup( $($event.toElement).prop("id").split("_")[1] );
				loadGroupMember( $($event.toElement).find("span.contents").html() );
			}).on("click", "span.contents", function($event) {
				if ( isRemoving == true ) return;
				selectGroup( $($event.toElement).parents("li").prop("id").split("_")[1] );
				loadGroupMember( $($event.toElement).html() );
			}).on("click", "span.remove", function($event) {
				isRemoving = true;
				$($event.toElement).parent().addClass("remove").find("span.remove").hide().end().append($removeTemplate);
			}).on("click", "button.confirm-cancel", function($event) {
				$($event.toElement).parents("li.remove")
					.find("span.right").remove().end()
					.find("span.remove").show().end()
					.removeClass("remove");
				isRemoving = false;
			}).on("click", "button.confirm-remove", function($event) {
				showLoading( false );
				var $el = $($event.toElement).parents("li.remove");
				removeGroup( $el.prop("id").split("_")[1], $el.find("span.contents").html() );
			});
			
			var enterLeaveData = [
              { selector: "li", 					name: "contents" },
              { selector: "span.contents", 			name: "contents" },
              { selector: "span.remove", 			name: "remove" },
              { selector: "button.confirm-cancel", 	name: "removeCancel" },
              { selector: "button.confirm-remove", 	name: "removeConfirm" }
			];
			for( var i = 0 ; i < enterLeaveData.length ; i++ ) {
				(function( selector, messageName ) {
					$elUL.on("mouseenter", selector, function() {
						$scope.onEnter(messageName);
					}).on("mouseleave", selector, function() {
						$scope.onLeave(messageName);
					})
				})(enterLeaveData[i].selector, enterLeaveData[i].name)
			}
			
			function selectGroup( newSelectedGroupNumber ) {
				if ( selectedGroupNumber != "" ) {
					$( "#" + $scope.prefix + selectedGroupNumber ).removeClass("selected");
				}
				console.log( $( "#" + $scope.prefix + newSelectedGroupNumber ) );
				$( "#" + $scope.prefix + newSelectedGroupNumber ).addClass("selected");
				selectedGroupNumber = newSelectedGroupNumber;
			}
			function show( $el ) {
				$el.removeClass("hide-me");
			}
			function hide() {
				for( var i = 0 ; i < arguments.length ; i++ ) {
					arguments[i].addClass("hide-me");
				}
			}
			function showLoading( isEdit ) {
				$elLoading[ isEdit ? "removeClass" : "addClass" ]("has-not-edit");
				$elLoading.removeClass("hide-me");
			}
			function showAlert( message ) {
				$elAlert.find(".message").html( message ).end().removeClass("hide-me").animate({
					height: 300
				}, 500, function() {});
			}
			function createGroup( name ) {
				$ajaxService.createUserGroup( { "id": name }, function( resultData ) {
					if ( resultData.errorCode ) {
						showAlert( resultData.errorMessage );
					} else {
						// @TODO
						// 많이 쓰는 놈 기준 3개를 뽑아 내야 함.
						$scope.$apply(function() {
							userGroupList.push({
								number: resultData.number,
								id: name
							});
							setTotal(userGroupList.length);
						});
						hide( $elLoading, $elEdit );
					}
					
				});
			}
			function updateGroup( number, name ) {
				$ajaxService.updateUserGroup( { "number": number, "id": name }, function( resultData ) {
					if ( resultData.errorCode ) {
						showAlert( resultData.errorMessage );
					} else {
						// @TODO
						// 많이 쓰는 놈 기준 3개를 뽑아 내야 함.
						$scope.$apply(function() {
							for( var i = 0 ; i < userGroupList.length ; i++ ) {
								if ( userGroupList[i].number == number ) {
									userGroupList[i].id = name;
								}
							}
						});
						hide( $elLoading, $elEdit );
					}
				});
			}
			function removeGroup( number, name ) {
				$ajaxService.removeUserGroup( { "id": name }, function( resultData ) {
					if ( resultData.errorCode ) {
						showAlert( resultData.errorMessage );
					} else {
						// @TODO
						// 많이 쓰는 놈 기준 3개를 뽑아 내야 함.
						$scope.$apply(function() {
							for( var i = 0 ; i < userGroupList.length ; i++ ) {
								if ( userGroupList[i].number == number ) {
									userGroupList.splice(i, 1);
									break;
								}
							}
							setTotal(userGroupList.length);
							hide( $elLoading );
							isRemoving = false;
						});
					}
				});
			}
			function loadGroupList( isFirst ) {
				$ajaxService.getUserGroupList( function( resultData ) {
					if ( resultData.errorCode ) {
						showAlert( resultData.errorMessage );
						// @TODO
						// 에러 처리
					} else {
						// @TODO
						// 많이 쓰는 놈 기준 3개를 뽑아 내야 함.
						isLoadedUserGroupList = true;
						$scope.$apply(function() {
							userGroupList = $scope.userGroupList = resultData;
						});
						setTotal(userGroupList.length);
						hide( $elLoading );
						if ( isFirst ) {
							selectGroup( userGroupList[0].number );
							loadGroupMember( userGroupList[0].id );
						}
						$scope.onLeave();
					}
				});				
			};
			function setTotal(n) {
				$elTotal.html( "(" + n + ")");
			};
			function setFilterBackground() {
				$elWrapper.css("background-color", "#FFFFF1");
			};
			function unsetFilterBackground() {
				$elWrapper.css("background-color", "#FFF");
			};
			function hasDuplicateName( groupName ) {
				var len = userGroupList.length;
				var has = false;
				for( var i = 0 ; i < userGroupList.length ; i++ ) {
					if ( userGroupList[i].id == groupName ) {
						has = true;
						break;
					}
				}
				return has;
			}
			function loadGroupMember( userGroupID ) {
				$scope.$parent.$broadcast( "alarmGroupMember.configuration.load", userGroupID );
				$scope.$parent.$broadcast( "alarmPinpointUser.configuration.load", userGroupID );
				$scope.$parent.$broadcast( "alarmRule.configuration.load", userGroupID );
			}
			
			$scope.prefix = "alarmUserGroup_";
			$scope.onRefresh = function() {
				if ( isRemoving == true ) return;
				
				$elFilterInput.val("");
				showLoading( false );
				loadGroupList( false );
			};
			$scope.onCreate = function() {
				if ( isRemoving == true ) return;
				
				isCreate = true;
				$elEditGuide.html( "Create new alarm group" );
				$elEditInput.val("");
				show( $elEdit );
				$elEditInput.focus();
			};
			$scope.onUpdate = function($event) {
				if ( isRemoving == true ) return;
				
				var tagName = $event.toElement.tagName.toLowerCase();
				var $el = $( $event.toElement );
				if ( tagName != "li" && tagName != "span") return;
				if ( tagName == "span" && $el.hasClass("remove") ) return;

				isCreate = false;
				$el = ( tagName == "span" ) ? $el.parent() : $el;
				var userGroupNumber = $el.prop("id").split("_")[1];
				var userGroupID = $el.find("span.contents").html();
				
				$elEditGuide.html( "\"" + userGroupID + "\"의 새로운 이름을 입력하세요." );
				$elEditInput.val( userGroupID ).prop("id", "updateUserGroup_" + userGroupNumber);
				show( $elEdit );
				$elEditInput.focus().select();
			};
			$scope.onInputFilter = function($event) {
				if ( isRemoving == true ) return;
				
				if ( $event.keyCode == 13 ) { // Enter
					$scope.onFilterGroup();
					return;
				}
				if ($.trim( $elFilterInput.val() ).length >= 3 ) {
					$elFilterEmpty.removeClass("disabled");
				} else {
					$elFilterEmpty.addClass("disabled");
				}
			};
			$scope.onFilterGroup = function() {
				if ( isRemoving == true ) return;
				var query = $.trim( $elFilterInput.val() );
				if ( query.length != 0 && query.length < 3 ) {
					$scope.onEnter("greater2");
					return;
				}
				if ( query == "" ) {
					if ( $scope.userGroupList.length != userGroupList.length ) {
						$scope.userGroupList = userGroupList;
						unsetFilterBackground();
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
					$scope.userGroupList = newFilterUserGroup;
					setFilterBackground();
				}
			};
			$scope.onFilterEmpty = function() {
				if ( isRemoving == true ) return;
				if ( $.trim( $elFilterInput.val() ) == "" ) return;
				$elFilterInput.val("");
				$scope.onFilterGroup();
			};
			$scope.onInputEdit = function($event) {
				if ( $event.keyCode == 13 ) { // Enter
					$scope.onApplyEdit();
				} else if ( $event.keyCode == 27 ) { // ESC
					$scope.onCancelEdit();
					$event.stopPropagation();
				}
			};
			$scope.onCancelEdit = function() {
				hide( $elEdit );
			};
			$scope.onApplyEdit = function() {
				var groupName = $.trim( $elEditInput.val() );
				if ( groupName == "" ) {
					$scope.onEnter("notEmpty");
					return;
				}
				
				showLoading( true );
				if ( hasDuplicateName( groupName ) && isCreate == true ) {
					showAlert( "동일한 이름을 가진 그룹이 이미 있습니다." );
					return;
				}
				if ( isCreate ) {
					createGroup( groupName );
				} else {
					updateGroup( $elEditInput.prop("id").split("_")[1], groupName );
				}
			};
			$scope.onCloseAlert = function() {
				$elAlert.animate({
					height: 50,
				}, 100, function() {
					hide( $elAlert, $elLoading );
				});
			};
			// onEnter, onLeave
			$scope.onEnter = function( type ) {
				var message;
				switch( type ) {
				case "title":
					message = "'" + userGroupList.length + "'은 생성된 사용자 그룹의 수입니다.";	
					break;
				case "refresh":
					message = "사용자 그룹 목록을 갱신합니다.";	
					break;
				case "create":
					message = "새로운 사용자 그룹을 생성합니다.";
					break;
				case "filterInput":
					message = "찾으려는 사용자 그룹을 입력하세요.";
					break;	
				case "filterSearch":
					message = "원하는 사용자 그룹을 검색하세요.";
					break;
				case "filterEmpty":
					message = "검색어를 삭제하고 모든 사용자 그룹을 표시합니다.";
					break;
				case "contents":
					message = "더블 클릭하면 수정할 수 있습니다.";
					break;
				case "remove":
					message = "사용자 그룹을 삭제합니다.";
					break;
				case "removeCancel":
					message = "삭제를 취소합니다.";
					break;
				case "removeConfirm":
					message = "사용자 그룹을 삭제합니다.";
					break;	
				case "notEmpty":
					message = "공백은 입력 할 수 없습니다.";
					break;
				case "greater2":
					message = "3글자 이상 입력하세요.";
					break;
				}
				$elGuideMessage.html(message);
			};
			$scope.onLeave = function( type ) {
				$elGuideMessage.html("사용자 그룹 목록입니다.");
			}
			$scope.$on("alarmUserGroup.configuration.show", function() {
				if ( isLoadedUserGroupList === false ) {
					loadGroupList( true );
				}
			});
		}
	]);
})(jQuery);
	