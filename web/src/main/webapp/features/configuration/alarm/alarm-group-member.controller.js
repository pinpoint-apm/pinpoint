(function($) {
	'use strict';
	/**
	 * (en)AlarmGroupMemberCtrl 
	 * @ko AlarmGroupMemberCtrl
	 * @group Controller
	 * @name AlarmGroupMemberCtrl
	 * @class
	 */
	pinpointApp.constant('AlarmGroupMemberConfig', {
	});	

	pinpointApp.controller('AlarmGroupMemberCtrl', [ '$scope','$element', 'AlarmGroupMemberConfig', 'AlarmAjaxService',
	    function ($scope, $element, $constant, $ajaxService) {
			//@TODO
			//통계 추가할 것.
			//$at($at.FILTEREDMAP_PAGE);
			console.log( "init AlarmGroupMemberCtrl", $element );

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
			var groupMemberList = $scope.groupMemberList = [];
			
			var $elUL = $element.find(".some-list-content ul");
			$elUL.on("click", "span.remove", function($event) {
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
				removeMember( $el.prop("id").split("_")[1] );
			});
			
			var enterLeaveData = [
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
			function showAlert( message, bIsSend ) {
				$elAlert.find(".message").html( message ).end().removeClass("hide-me").animate({
					height: 300
				}, 500, function() {
					if ( bIsSend ) {
						callbackAddUser();
					}
				});
			}
			function callbackAddUser( bIsSuccess ) {
				$scope.$parent.$broadcast( "alarmPinpointUser.configuration.addUserCallback", bIsSuccess );
			}
			function removeFromList( memberID ) {
				if ( $scope.groupMemberList != groupMemberList ) {
					for( var i = 0 ; i < groupMemberList.length ; i++ ) {
						if ( groupMemberList[i].memberId == memberID ) {
							groupMemberList.splice(i, 1);
							break;
						}
					}	
				}
				for( var i = 0 ; i < $scope.groupMemberList.length ; i++ ) {
					if ( $scope.groupMemberList[i].memberId == memberID ) {
						$scope.groupMemberList.splice(i, 1);
						break;
					}
				}
			}

			function addMember( memberId ) {
				$ajaxService.addMemberInGroup({ "userGroupId": currentUserGroupID, "memberId": memberId }, function( resultData ) {
					if ( resultData.errorcode ) {
						showAlert( resutlData.errormessage, true );
					} else {
						// @TODO
						// 많이 쓰는 놈 기준 3개를 뽑아 내야 함.
						$scope.$apply(function() {
							groupMemberList.push({
								"userGroupId": currentUserGroupID, 
								"memberId": memberId
							});
							setTotal(groupMemberList.length);
							hide( $elLoading );
						});
						callbackAddUser( true );
					}
				});
			}
			function removeMember( memberID ) {
				$ajaxService.removeMemberInGroup({ "userGroupId": currentUserGroupID, "memberId": memberID }, function( resultData ) {
					if ( resultData.errorcode ) {
						showAlert( resutlData.errormessage, false );
					} else {
						// @TODO
						// 많이 쓰는 놈 기준 3개를 뽑아 내야 함.
						$scope.$apply(function() {
							removeFromList( memberID );
							setTotal(groupMemberList.length);
							hide( $elLoading );
							isRemoving = false;
						});
					}
				});
			}
			function loadList() {
				$ajaxService.getGroupMemberListInGroup({ "userGroupId": currentUserGroupID }, function( resultData ) {
					if ( resultData.errorcode ) {
						showAlert( resultData.errormessage, false );
						// @TODO
						// 에러 처리
					} else {
						// @TODO
						// 많이 쓰는 놈 기준 3개를 뽑아 내야 함.
						isLoadedGroupMemberList = true;
						$scope.$apply(function() {
							groupMemberList = $scope.groupMemberList = resultData;
						});
						setTotal(groupMemberList.length);
						hide( $elLoading );
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
			function hasUser( userID ) {
				return $( "#" + $scope.prefix + userID ).length > 0;
			}
			
			$scope.prefix = "alarmGroupMember_"; 
			$scope.onRefresh = function() {
				if ( isRemoving == true ) return;
				
				$elFilterInput.val("");
				showLoading( false );
				loadList();
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
					if ( $scope.groupMemberList.length != groupMemberList.length ) {
						$scope.groupMemberList = groupMemberList;
						unsetFilterBackground();
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
					$scope.groupMemberList = newFilterGroupMember;
					setFilterBackground();
				}
			};
			$scope.onFilterEmpty = function() {
				if ( isRemoving == true ) return;
				if ( $.trim( $elFilterInput.val() ) == "" ) return;
				$elFilterInput.val("");
				$scope.onFilterGroup();
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
					message = "'" + groupMemberList.length + "'은 그룹에 등록된 사용자 수입니다.";	
					break;
				case "refresh":
					message = "멤버 목록을 갱신합니다.";	
					break;
				case "filterInput":
					message = "찾으려는 멤버를 입력하세요.";
					break;	
				case "filterSearch":
					message = "원하는 멤버를 검색하세요.";
					break;
				case "filterEmpty":
					message = "검색어를 삭제하고 모든 멤버를 표시합니다.";
					break;
				case "remove":
					message = "멤버를 삭제합니다.";
					break;
				case "removeCancel":
					message = "삭제를 취소합니다.";
					break;
				case "removeConfirm":
					message = "멤버를 삭제합니다.";
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
				$elGuideMessage.html(currentUserGroupID + "에 등록된 멤버입니다.");
			}
			$scope.$on("alarmGroupMember.configuration.load", function( event, userGroupID )  {
				currentUserGroupID = userGroupID;
//				if ( isLoadedGroupMemberList === false ) {
					loadList();
//				}
			});
			$scope.$on("alarmGroupMember.configuration.addUser", function( event, userID )  {
				showLoading( false );
				if ( hasUser( userID ) == false ) {
					addMember( userID );
				} else {
					showAlert( "이미 등록된 사용자입니다.", true );
				}
			});
			$scope.$on("alarmGroupMember.configuration.updateUser", function( event, oUser )  {
				// 사용자 정보 갱신됨( 부서, 이름 )
				if ( hasUser( oUser.userId ) ) {
					//updateFromList( oUser );
				}
			});
			$scope.$on("alarmGroupMember.configuration.removeUser", function( event, userID )  {
				if ( hasUser( userID ) ) {
					removeFromList( userID );
				}
			});
		}
	]);
})(jQuery);
	