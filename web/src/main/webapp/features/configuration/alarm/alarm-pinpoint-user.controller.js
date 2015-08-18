(function($) {
	'use strict';
	/**
	 * (en)AlarmPinpointUserCtrl 
	 * @ko AlarmPinpointUserCtrl
	 * @group Controller
	 * @name AlarmPinpointUserCtrl
	 * @class
	 */
	pinpointApp.constant('AlarmPinpointUserConfig', {
	});	

	pinpointApp.controller('AlarmPinpointUserCtrl', [ '$scope','$element', 'AlarmPinpointUserConfig', 'AlarmAjaxService',
	    function ($scope, $element, $constant, $ajaxService) {
			//@TODO
			//통계 추가할 것.
			//$at($at.FILTEREDMAP_PAGE);
			console.log( "init AlarmPinpointUserCtrl", $element );

			var $elWrapper = $element.find(".wrapper");
			var $elTotal = $element.find(".total");
			var $elLoading = $element.find(".some-loading");
			var $elAlert = $element.find(".some-alert");
			var $elGuideMessage = $element.find(".guide-message");
			var $elFilterInput = $element.find("div.filter-input input");
			var $elFilterEmpty = $element.find("div.filter-input button.trash");
			var $elEdit = $element.find(".some-edit-content");
			var $elEditInputUserID = $elEdit.find("input[name=userID]");
			var $elEditInputName = $elEdit.find("input[name=name]");
			var $elEditInputDepartment = $elEdit.find("input[name=department]");
			var $elEditInputPhone = $elEdit.find("input[name=phone]");
			var $elEditInputEmail = $elEdit.find("input[name=email]");
			var $elEditGuide = $elEdit.find(".title-message");
			var $removeTemplate = $([
	           '<span class="right">',
	               '<button class="btn btn-danger confirm-cancel"><span class="glyphicon glyphicon-remove" aria-hidden="true"></span></button>',
	               '<button class="btn btn-danger confirm-remove" style="margin-left:2px;"><span class="glyphicon glyphicon-ok" aria-hidden="true"></span></button>',
   			   '</span>'
			].join(""));
			
			var isLoadedPinpointUserList = false;
			var isCreate = true; // update
			var isRemoving = false;
			var pinpointUserList = $scope.pinpointUserList = [];
			
			var $elUL = $element.find(".some-list-content ul");
			$elUL.on("dblclick", "li", function($event) {
				$scope.onUpdate( $event );
			}).on("dblclick", "span.contents", function($event) {
				$scope.onUpdate( $event );
			}).on("click", "button.move", function($event) {
				showLoading( false );
				addUserToGroup( $($event.toElement).parents("li").prop("id").split("_")[1] );
			}).on("click", "span.remove", function($event) {
				isRemoving = true;
				$($event.toElement).parent().addClass("remove").find("span.remove").hide().end().find("button.move").addClass("disabled").end().append($removeTemplate);
			}).on("click", "button.confirm-cancel", function($event) {
				$($event.toElement).parents("li.remove")
					.find("span.right").remove().end()
					.find("span.remove").show().end()
					.find("button.move").removeClass("disabled").end()
					.removeClass("remove");
				isRemoving = false;
			}).on("click", "button.confirm-remove", function($event) {
				showLoading( false );
				removeUser( $($event.toElement).parents("li.remove").prop("id").split("_")[1] );
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
			function searchUser( userID ) {
				var oUser;
				var len = pinpointUserList.length;
				for( var i = 0 ; i < len ; i++ ) {
					if ( pinpointUserList[i].userId == userID ) {
						oUser = pinpointUserList[i];
						break;
					}
				}
				return oUser;
			}
			function addUserToGroup( userID ) {
				$scope.$parent.$broadcast( "alarmGroupMember.configuration.addUser", searchUser( userID ).userId );
			}
			function updatedUser( oUser ) {
				$scope.$parent.$broadcast( "alarmGroupMember.configuration.updateUser", oUser );
			}
			function removedUser( userID ) {
				$scope.$parent.$broadcast( "alarmGroupMember.configuration.removeUser", userID );
			}
			function createUser( userID, userName, userDepartment, userPhone, userEmail ) {
				var oNewUser = { 
					"userId": userID,
					"name": userName,
					"department": userDepartment,
					"phoneNumber": userPhone,
					"email": userEmail
				}; 
				$ajaxService.createPinpointUser(oNewUser, function( resultData ) {
					if ( resultData.errorCode ) {
						showAlert( resultData.errorMessage );
					} else {
						// @TODO
						// 많이 쓰는 놈 기준 3개를 뽑아 내야 함.
						$scope.$apply(function() {
							pinpointUserList.push(oNewUser);
							setTotal(pinpointUserList.length);
						});
						hide( $elLoading, $elEdit );
					}
					
				});
			}
			function updateUser( userID, userName, userDepartment, userPhone, userEmail ) {
				var oUpdateUser = { 
					"userId": userID,
					"name": userName,
					"department": userDepartment,
					"phoneNumber": userPhone,
					"email": userEmail
				}; 
				$ajaxService.updatePinpointUser(oUpdateUser, function( resultData ) {
					if ( resultData.errorCode || resultData.exception ) {
						showAlert( resultData.errorMessage );
					} else {
						// @TODO
						// 많이 쓰는 놈 기준 3개를 뽑아 내야 함.
						$scope.$apply(function() {
							for( var i = 0 ; i < pinpointUserList.length ; i++ ) {
								if ( pinpointUserList[i].userId == userID ) {
									pinpointUserList[i].name = userName;
									pinpointUserList[i].department = userDepartment;
									pinpointUserList[i].phoneNumber = userPhone;
									pinpointUserList[i].email = userEmail;
								}
							}
						});
						hide( $elLoading, $elEdit );
						updatedUser( oUpdateUser );
					}
				});
			}
			function removeUser( userID ) {
				$ajaxService.removePinpointUser( { "userId": userID }, function( resultData ) {
					if ( resultData.errorCode ) {
						showAlert( resultData.errorMessage );
					} else {
						// @TODO
						// 많이 쓰는 놈 기준 3개를 뽑아 내야 함.
						$scope.$apply(function() {
							for( var i = 0 ; i < pinpointUserList.length ; i++ ) {
								if ( pinpointUserList[i].userId == userID ) {
									pinpointUserList.splice(i, 1);
									break;
								}
							}
							setTotal(pinpointUserList.length);
							hide( $elLoading );
							isRemoving = false;
							removedUser( userID );
						});
					}
				});
			}
			function loadList( isFirst ) {
				$ajaxService.getPinpointUserList( function( resultData ) {
					if ( resultData.errorCode ) {
						showAlert( resultData.errorMessage );
						// @TODO
						// 에러 처리
					} else {
						// @TODO
						// 많이 쓰는 놈 기준 3개를 뽑아 내야 함.
						isLoadedPinpointUserList = true;
						$scope.$apply(function() {
							pinpointUserList = $scope.pinpointUserList = resultData;
						});
						setTotal(pinpointUserList.length);
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
			function hasDuplicateID( userID ) {
				var len = pinpointUserList.length;
				var has = false;
				for( var i = 0 ; i < pinpointUserList.length ; i++ ) {
					if ( pinpointUserList[i].userId == userID ) {
						has = true;
						break;
					}
				}
				return has;
			}
			function loadGroupMember( userGroupID ) {
				$scope.$parent.$broadcast( "alarmGroupMember.configuration.load", userGroupID );
			}
			
			$scope.onRefresh = function() {
				if ( isRemoving == true ) return;
				
				$elFilterInput.val("");
				showLoading( false );
				loadList( false );
			};
			$scope.onCreate = function() {
				if ( isRemoving == true ) return;
				
				isCreate = true;
				$elEditGuide.html( "Create new pinpoint user" );
				$elEditInputUserID.removeProp("disabled");
				$elEditInputUserID.val("");
				$elEditInputName.val("");
				$elEditInputDepartment.val("");
				$elEditInputPhone.val("");
				$elEditInputEmail.val("");
				show( $elEdit );
				$elEditInputUserID.focus();
			};
			$scope.onUpdate = function($event) {
				if ( isRemoving == true ) return;
				
				var tagName = $event.toElement.tagName.toLowerCase();
				var $el = $( $event.toElement );
				if ( tagName != "li" && tagName != "span") return;
				if ( tagName == "span" && $el.hasClass("remove") ) return;

				isCreate = false;
				$el = ( tagName == "span" ) ? $el.parent() : $el;
				var oUser = searchUser( $el.prop("id").split("_")[1] );
				
				$elEditGuide.html( "아래 항목을 수정할 수 있습니다." );
				$elEditInputUserID.prop("disabled", "disabled");
				$elEditInputUserID.val( oUser.userId );
				$elEditInputName.val( oUser.name );
				$elEditInputDepartment.val( oUser.department );
				$elEditInputPhone.val( oUser.phoneNumber );
				$elEditInputEmail.val( oUser.email );
				show( $elEdit );
				$elEditInputName.focus().select();
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
					if ( $scope.pinpointUserList.length != pinpointUserList.length ) {
						$scope.pinpointUserList = pinpointUserList;
						unsetFilterBackground();
					}
					$elFilterEmpty.addClass("disabled");
				} else {
					var newFilterUserGroup = [];
					var length = pinpointUserList.length;
					for( var i = 0 ; i < pinpointUserList.length ; i++ ) {
						if ( pinpointUserList[i].name.indexOf( query ) != -1 || pinpointUserList[i].department.indexOf( query ) != -1 ) {
							newFilterUserGroup.push( pinpointUserList[i] );
						}
					}
					$scope.pinpointUserList = newFilterUserGroup;
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
				var userID = $.trim( $elEditInputUserID.val() );
				var userName = $.trim( $elEditInputName.val() );
				var userDepartment = $.trim( $elEditInputDepartment.val() );
				var userPhone = $.trim( $elEditInputPhone.val() );
				var userEmail = $.trim( $elEditInputEmail.val() );
				
				if ( userID == "" || userName == "" ) {
					$scope.onEnter("notEmpty");
					return;
				}
				// TODO phone - 체크
				// TODO email 포맷 체크
				
				showLoading( true );
				if ( hasDuplicateID( userID ) && isCreate == true) {
					showAlert( "동일한 userID를 가진 사용자가 이미 있습니다." );
					return;
				}
				if ( isCreate ) {
					createUser( userID, userName, userDepartment, userPhone, userEmail );
				} else {
					updateUser( userID, userName, userDepartment, userPhone, userEmail );
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
					message = "'" + pinpointUserList.length + "'은 Pinpoint에 등록된 사용자의 수입니다.";	
					break;
				case "refresh":
					message = "사용자 목록을 갱신합니다.";	
					break;
				case "create":
					message = "새로운 사용자를 생성합니다.";
					break;
				case "filterInput":
					message = "찾으려는 사용자를 입력하세요.";
					break;	
				case "filterSearch":
					message = "원하는 사용자를 검색하세요.";
					break;
				case "filterEmpty":
					message = "검색어를 삭제하고 모든 사용자를 표시합니다.";
					break;
				case "contents":
					message = "더블 클릭하면 수정할 수 있습니다.";
					break;
				case "remove":
					message = "사용자를 삭제합니다.";
					break;
				case "removeCancel":
					message = "삭제를 취소합니다.";
					break;
				case "removeConfirm":
					message = "사용자를 삭제합니다.";
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
				$elGuideMessage.html("Pinpoint 사용자 목록입니다.");
			}
			$scope.$on("alarmPinpointUser.configuration.load", function() {
				if ( isLoadedPinpointUserList === false ) {
					loadList( true );
				}
			});
			$scope.$on("alarmPinpointUser.configuration.addUserCallback", function( event ) {
				hide($elLoading);
			});
		}
	]);
})(jQuery);
	