(function($) {
	'use strict';
	/**
	 * (en)AlarmRuleCtrl 
	 * @ko AlarmRuleCtrl
	 * @group Controller
	 * @name AlarmRuleCtrl
	 * @class
	 */
	pinpointApp.constant('AlarmRuleConfig', {
	});	

	pinpointApp.controller('AlarmRuleCtrl', [ '$scope','$document', '$element', 'AlarmRuleConfig', 'AlarmAjaxService',
	    function ($scope, $document, $element, $constant, $ajaxService) {
			//@TODO
			//통계 추가할 것.
			//$at($at.FILTEREDMAP_PAGE);
			console.log( "init AlarmRuleCtrl", $element );
			
			var $elWrapper = $element.find(".wrapper");
			var $elTotal = $element.find(".total");
			var $elLoading = $element.find(".some-loading");
			var $elAlert = $element.find(".some-alert");
			var $elGuideMessage = $element.find(".guide-message");
			var $elFilterInput = $element.find("div.filter-input input");
			var $elFilterEmpty = $element.find("div.filter-input button.trash");
			var $elEdit = $element.find(".some-edit-content");
			var $elEditSelectApplication = $elEdit.find("select[name=application]");
			var $elEditSelectRules = $elEdit.find("select[name=rule]");
			var $elEditInputThreshold = $elEdit.find("input[name=threshold]");
			var $elEditCheckboxSMS = $elEdit.find("input[name=sms]");
			var $elEditCheckboxEmail = $elEdit.find("input[name=email]");
			var $elEditTextareaNotes = $elEdit.find("textarea");
			var $elEditGuide = $elEdit.find(".title-message");
			var $removeTemplate = $([
	           '<span class="removeTemplate">',
	               '<button class="btn btn-danger confirm-cancel"><span class="glyphicon glyphicon-remove" aria-hidden="true"></span></button>',
	               '<button class="btn btn-danger confirm-remove" style="margin-left:2px;"><span class="glyphicon glyphicon-ok" aria-hidden="true"></span></button>',
   			   '</span>'
			].join(""));
			
			var currentUserGroupID = "";
			var isLoadedRuleList = false;
			var isCreate = true; // update
			var isRemoving = false;
			var ruleList = $scope.ruleList = [];
			
			

			var $elUL = $element.find(".some-list-content .wrapper tbody");
			$elUL.on("dblclick", "tr", function($event) {
				$scope.onUpdate( $event );
			}).on("dblclick", "td", function($event) {
				$scope.onUpdate( $event );
			}).on("click", "button.move", function($event) {
			}).on("click", "span.remove", function($event) {
				isRemoving = true;
				$($event.toElement).parent().addClass("remove").parent().addClass("remove").find("span.remove").hide().end().end().append($removeTemplate);
			}).on("click", "button.confirm-cancel", function($event) {
				$($event.toElement).parents("td")
					.find("span.removeTemplate").remove().end()
					.find("span.remove").show().end()
					.removeClass("remove").parent().removeClass("remove");
				isRemoving = false;
			}).on("click", "button.confirm-remove", function($event) {
				showLoading( false );
				removeUser( $($event.toElement).parents("tr").prop("id").split("_")[1] );
			});
			
			var enterLeaveData = [
              { selector: "tr", 					name: "contents" },
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
			function initPopover() {
				$('[data-toggle="alarm-popover"]').popover();
			}
			function formatOptionText(state) {
                if (!state.id) {
                    return state.text;
                }
                var chunk = state.text.split("@");
                if (chunk.length > 1) {
                    var img = $document.get(0).createElement("img");
                    img.src = "/images/icons/" + chunk[1] + ".png";
                    img.style.height = "25px";
                    img.style.paddingRight = "3px";
                    return img.outerHTML + chunk[0];
                } else {
                    return state.text;
                }
            }
			function initApplicationSelect() {
				$elEditSelectApplication.select2({
	                placeholder: "Select an application.",
	                searchInputPlaceholder: "Input your application name.",
	                allowClear: false,
	                formatResult: formatOptionText,
	                formatSelection: formatOptionText,
	                escapeMarkup: function (m) {
	                    return m;
	                }
	            }).on("change", function (e) {
	            	$at( $at.MAIN, $at.CLK_APPLICATION );
	            	console.log("changed : application ");
	            });
				$elEditSelectRules.select2({
	                searchInputPlaceholder: "Input your rule name.",
	                placeholder: "Select an rule.",
	                allowClear: false,
	                formatResult: formatOptionText,
	                formatSelection: formatOptionText,
	                escapeMarkup: function (m) {
	                    return m;
	                }
	            }).on("change", function (e) {
	            	$at( $at.MAIN, $at.CLK_APPLICATION );
	            	console.log("changed : rule ");
	            });
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
			function searchRule( ruleID ) {
				var oRule;
				var len = ruleList.length;
				for( var i = 0 ; i < len ; i++ ) {
					if ( ruleList[i].ruleId == ruleID ) {
						oRule = ruleList[i];
						break;
					}
				}
				return oRule;
			}
			function createRule( application, rule, threshold, sms, email, notes ) {
				var oNewRule = { 
					"applicationId": application,
					"userGroupId": currentUserGroupID,
					"checkerName": rule,
					"threshold": threshold,
					"smsSend": sms,
					"emailSend": email,
					"notes": notes
				}; 
				$ajaxService.createRule(oNewRule, function( resultData ) {
					if ( resultData.errorCode ) {
						showAlert( resultData.errorMessage );
					} else {
						// @TODO
						// 많이 쓰는 놈 기준 3개를 뽑아 내야 함.
						oNewRule.ruleId = resultData.ruleId;
						$scope.$apply(function() {
							ruleList.push(oNewRule);
							setTotal(ruleList.length);
						});
						initPopover();
						hide( $elLoading, $elEdit );
					}
					
				});
			}
			function updateRule( ruleID, application, rule, threshold, sms, email, notes ) {
				var oUpdateRule = { 
					"ruleId": ruleID,
					"applicationId": application,
					"userGroupId": currentUserGroupID,
					"checkerName": rule,
					"threshold": parseInt(threshold, 10),
					"smsSend": sms,
					"emailSend": email,
					"notes": notes
				}; 
				$ajaxService.updateRule(oUpdateRule, function( resultData ) {
					if ( resultData.errorCode ) {
						showAlert( resultData.errorMessage );
					} else {
						// @TODO
						// 많이 쓰는 놈 기준 3개를 뽑아 내야 함.
						$scope.$apply(function() {
							for( var i = 0 ; i < ruleList.length ; i++ ) {
								if ( ruleList[i].ruleId == ruleID ) {
									ruleList[i].applicationId = application;
									ruleList[i].checkerName = rule;
									ruleList[i].threshold = threshold;
									ruleList[i].smsSend = sms;
									ruleList[i].emailSend = email;
									ruleList[i].notes = notes;
								}
							}
						});
						hide( $elLoading, $elEdit );
					}
				});
			}
			function removeUser( ruleID ) {
				$ajaxService.removeRule( { "ruleId": ruleID }, function( resultData ) {
					if ( resultData.errorCode ) {
						showAlert( resultData.errorMessage );
					} else {
						// @TODO
						// 많이 쓰는 놈 기준 3개를 뽑아 내야 함.
						$scope.$apply(function() {
							for( var i = 0 ; i < ruleList.length ; i++ ) {
								if ( ruleList[i].ruleId == ruleID ) {
									ruleList.splice(i, 1);
									break;
								}
							}
							setTotal(ruleList.length);
							hide( $elLoading );
							isRemoving = false;
						});
					}
				});
			}
			function loadList( isFirst ) {
				$ajaxService.getRuleList( { "userGroupId": currentUserGroupID }, function( resultData ) {
					if ( resultData.errorCode ) {
						showAlert( resultData.errorMessage );
						// @TODO
						// 에러 처리
					} else {
						// @TODO
						// 많이 쓰는 놈 기준 3개를 뽑아 내야 함.
						isLoadedRuleList = true;
						$scope.$apply(function() {
							ruleList = $scope.ruleList = resultData;
						});
						setTotal(ruleList.length);
						initPopover();
						hide( $elLoading );
						$scope.onLeave();
						
					}
				});				
			};
			function loadRuleSet() {
				if ( $scope.ruleSets.length > 1 ) return;
				$ajaxService.getRuleSet(function( resultData ) {
					if ( resultData.errorCode ) {
						showAlert( resultData.errorMessage );
						// @TODO
						// 에러 처리
					} else {
						// @TODO
						// 많이 쓰는 놈 기준 3개를 뽑아 내야 함.
						$scope.$apply(function() {
							for( var i = 0 ; i < resultData.length ; i++ ) {
								$scope.ruleSets.push( {
									"text": resultData[i]
								});
							}
						});
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
				var len = ruleList.length;
				var has = false;
				for( var i = 0 ; i < ruleList.length ; i++ ) {
					if ( ruleList[i].userId == userID ) {
						has = true;
						break;
					}
				}
				return has;
			}
			function loadGroupMember( userGroupID ) {
				$scope.$parent.$broadcast( "alarmGroupMember.configuration.load", userGroupID );
			}
			
			$scope.ruleSets = [ {"text": ""} ];
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
				$elEditSelectApplication.select2("val", "");
				$elEditSelectRules.select2( "val", "");
				$elEditInputThreshold.val("0");
				$elEditCheckboxSMS.prop("checked", "");
				$elEditCheckboxEmail.prop("checked", "");		
				$elEditTextareaNotes.val("");
				show( $elEdit );
			};
			$scope.onUpdate = function($event) {
				if ( isRemoving == true ) return;
				
				var tagName = $event.toElement.tagName.toLowerCase();
				var $el = $( $event.toElement );
				if ( tagName != "tr" && tagName != "td") return;
				
				isCreate = false;
				$el = ( tagName == "td" ) ? $el.parent() : $el;
				var ruleID = $el.prop("id").split("_")[1];
				var oRule = searchRule( ruleID );
				
				$elEditGuide.html( "아래 항목을 수정할 수 있습니다." );
				$elEditSelectApplication.select2("val", oRule.applicationId).parent().prop("id", "updateRule_" + ruleID);
				$elEditSelectRules.select2( "val", oRule.checkerName);
				$elEditInputThreshold.val( oRule.threshold );
				$elEditCheckboxSMS.prop("checked", oRule.smsSend);
				$elEditCheckboxEmail.prop("checked", oRule.emailSend);		
				$elEditTextareaNotes.val( oRule.notes );
				
				console.log( oRule );
				console.log( $elEditSelectApplication.select2("val"), $elEditSelectRules.select2( "val") );
				show( $elEdit );
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
					if ( $scope.ruleList.length != ruleList.length ) {
						$scope.ruleList = ruleList;
						unsetFilterBackground();
					}
					$elFilterEmpty.addClass("disabled");
				} else {
					var newFilterUserGroup = [];
					var length = ruleList.length;
					for( var i = 0 ; i < ruleList.length ; i++ ) {
						if ( ruleList[i].name.indexOf( query ) != -1 || ruleList[i].department.indexOf( query ) != -1 ) {
							newFilterUserGroup.push( ruleList[i] );
						}
					}
					$scope.ruleList = newFilterUserGroup;
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
				console.log("-onApplyEdit");
				var application = $elEditSelectApplication.select2("val");
				var rule = $elEditSelectRules.select2( "val");
				console.log( "apply", $elEditSelectApplication.select2("val"), $elEditSelectRules.select2( "val") );
				var threshold = $elEditInputThreshold.val();
				var sms = $elEditCheckboxSMS.prop("checked");
				var email = $elEditCheckboxEmail.prop("checked");	
				var notes = $elEditTextareaNotes.val();
				
				console.log("-onApplyEdit", application, rule);
				if ( application == "" || rule == "" ) {
					$scope.onEnter("notEmpty");
					return;
				}				
				showLoading( true );
//				if ( hasDuplicateID( userID ) && isCreate == true) {
//					showAlert( "동일한 userID를 가진 사용자가 이미 있습니다." );
//					return;
//				}
				if ( isCreate ) {
					createRule( application, rule, threshold, sms, email, notes );
				} else {
					console.log("-onApplyEdit - updateUser");
					updateRule( $elEditSelectApplication.parent().prop("id").split("_")[1], application, rule, threshold, sms, email, notes );
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
					message = "'" + ruleList.length + "'은 Pinpoint에 등록된 사용자의 수입니다.";	
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
			$scope.$on("alarmRule.configuration.load", function( event, userGroupID ) {
				currentUserGroupID = userGroupID;
//				if ( isLoadedRuleList === false ) {
					loadList( true );
					loadRuleSet();
//				}
			});
			$scope.$on("alarmRule.applications.set", function( event, applicationData ) {
				$scope.applications = applicationData;
				initApplicationSelect();
			});
		}
	]);
})(jQuery);
	