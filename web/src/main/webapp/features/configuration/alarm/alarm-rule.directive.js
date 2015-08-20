(function($) {
	'use strict';
	/**
	 * (en)alarmRuleDirective 
	 * @ko alarmRuleDirective
	 * @group Directive
	 * @name alarmRuleDirective
	 * @class
	 */	
	
	pinpointApp.directive('alarmRuleDirective', [ '$rootScope', '$document', 'helpContentTemplate', 'helpContentService', 'AlarmListTemplateService',
	    function ($rootScope, $document, helpContentTemplate, helpContentService, $alarmListTemplateService) {
        return {
            restrict: 'EA',
            replace: true,
            templateUrl: 'features/configuration/alarm/alarmRule.html',
            scope: true,
            link: function (scope, element) {

    			var $element = $(element);
    			var $elWrapper = $element.find(".wrapper");
    			var $elTotal = $element.find(".total");
    			var $elLoading = $element.find(".some-loading");
    			var $elAlert = $element.find(".some-alert");
    			var $elGuideMessage = $element.find(".guide-message");
    			var $elFilterInputApplication = $element.find("div.filter-input input[name=filterApplication]");
    			var $elFilterInputRule = $element.find("div.filter-input input[name=filterRule]");
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
    			var ruleList = scope.ruleList = [];
    			
    			var $elTbody = $element.find(".some-list-content .wrapper tbody");
    			$elTbody.on("click", function($event) {
    				var tagName = $event.toElement.tagName.toLowerCase();
    				var $target = $($event.toElement);
    				var $tr = $target.parents("tr");
    				
    				if ( tagName == "button" ) {
    					if ( $target.hasClass("edit") ) {
    						scope.onUpdate( $event );
    					} else if ( $target.hasClass("confirm-remove") ) {
    						removeConfirm( $tr );
    					} else if ( $target.hasClass("confirm-cancel") ) {
    						revmoeCancel( $tr );	
    					}
    				} else if ( tagName == "span" ) {
    					if ( $target.hasClass("remove") ) {
    						isRemoving = true;
    	    				$tr.addClass("remove").find("td:last-child").addClass("remove").find("span.remove").hide().end().append($removeTemplate);		
    					} else if( $target.hasClass("glyphicon-edit") ) {
    						scope.onUpdate( $event );
    					} else if ( $target.hasClass("glyphicon-remove") ) {
    						removeCancel( $tr );
    					} else if ( $target.hasClass("glyphicon-ok") ) {
    						removeConfirm( $tr );
    					}	
    				}
    			});
    			
    			$alarmListTemplateService.setGuideEvent( scope, $elTbody, [
                  { selector: "tr", 					name: "contents" },
                  { selector: "span.contents", 			name: "contents" },
                  { selector: "span.remove", 			name: "remove" },
                  { selector: "button.confirm-cancel", 	name: "removeCancel" },
                  { selector: "button.confirm-remove", 	name: "removeConfirm" }
    			]);
    			function removeConfirm( $el ) {
    				$alarmListTemplateService.showLoading( $elLoading, false );
    				removeUser( $el.prop("id").split("_")[1] );
    			}
    			function removeCancel( $el ) {
    				$el.removeClass("remove")
						.find("span.removeTemplate").remove().end()
						.find("span.remove").show().end()
						.find("td").removeClass("remove");
    				isRemoving = false;
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
    	            });
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
    			function createRule( application, serviceType, rule, threshold, sms, email, notes ) {
    				var oNewRule = { 
    					"applicationId": application,
    					"serviceType": serviceType,
    					"userGroupId": currentUserGroupID,
    					"checkerName": rule,
    					"threshold": threshold,
    					"smsSend": sms,
    					"emailSend": email,
    					"notes": notes
    				}; 
    				$alarmListTemplateService.sendCRUD( "createRule", oNewRule, function( resultData ) {
    					// @TODO
    					// 많이 쓰는 놈 기준 3개를 뽑아 내야 함.
    					oNewRule.ruleId = resultData.ruleId;
    					ruleList.push(oNewRule);
    					$alarmListTemplateService.setTotal( $elTotal, ruleList.length );
    					initPopover();
    					$alarmListTemplateService.hide( $elLoading, $elEdit );					
    				}, $elAlert );
    			}
    			function updateRule( ruleID, application, serviceType, rule, threshold, sms, email, notes ) {
    				var oUpdateRule = { 
    					"ruleId": ruleID,
    					"applicationId": application,
    					"serviceType": serviceType,
    					"userGroupId": currentUserGroupID,
    					"checkerName": rule,
    					"threshold": parseInt(threshold, 10),
    					"smsSend": sms,
    					"emailSend": email,
    					"notes": notes
    				}; 
    				$alarmListTemplateService.sendCRUD( "updateRule", oUpdateRule, function( resultData ) {
    					// @TODO
    					// 많이 쓰는 놈 기준 3개를 뽑아 내야 함.
    					for( var i = 0 ; i < ruleList.length ; i++ ) {
    						if ( ruleList[i].ruleId == ruleID ) {
    							ruleList[i].applicationId = application;
    							ruleList[i].serviceType = serviceType;
    							ruleList[i].checkerName = rule;
    							ruleList[i].threshold = threshold;
    							ruleList[i].smsSend = sms;
    							ruleList[i].emailSend = email;
    							ruleList[i].notes = notes;
    						}
    					}
    					$alarmListTemplateService.hide( $elLoading, $elEdit );
    					
    				}, $elAlert );
    			}
    			function removeUser( ruleID ) {
    				$alarmListTemplateService.sendCRUD( "removeRule", { "ruleId": ruleID }, function( resultData ) {
    					scope.$apply(function() {
	    					for( var i = 0 ; i < ruleList.length ; i++ ) {
	    						if ( ruleList[i].ruleId == ruleID ) {
	    							ruleList.splice(i, 1);
	    							break;
	    						}
	    					}
    					});
    					$alarmListTemplateService.setTotal( $elTotal, ruleList.length );
    					$alarmListTemplateService.hide( $elLoading );
    					isRemoving = false;
    				}, $elAlert );
    			}
    			function loadList( isFirst ) {
    				$alarmListTemplateService.sendCRUD( "getRuleList", { "userGroupId": currentUserGroupID }, function( resultData ) {
    					// @TODO
    					// 많이 쓰는 놈 기준 3개를 뽑아 내야 함.
    					isLoadedRuleList = true;
    					ruleList = scope.ruleList = resultData;
    					$alarmListTemplateService.setTotal( $elTotal, ruleList.length );
    					initPopover();
    					$alarmListTemplateService.hide( $elLoading );
    					scope.onLeave();
    				}, $elAlert );		
    			};
    			function loadRuleSet() {
    				if ( scope.ruleSets.length > 1 ) return;
    				
    				$alarmListTemplateService.sendCRUD( "getRuleSet", {}, function( resultData ) {
    					// @TODO
    					// 많이 쓰는 놈 기준 3개를 뽑아 내야 함.
    					for( var i = 0 ; i < resultData.length ; i++ ) {
    						scope.ruleSets.push( {
    							"text": resultData[i]
    						});
    					}
    				}, $elAlert );			
    			};
    			function loadGroupMember( userGroupID ) {
    				scope.$parent.$broadcast( "alarmGroupMember.configuration.load", userGroupID );
    			}
    			
    			scope.ruleSets = [ {"text": ""} ];
    			scope.onRefresh = function() {
    				if ( isRemoving == true ) return;
    				
        			$elFilterInputApplication.val("");
        			$elFilterInputRule.val("");
    				$alarmListTemplateService.showLoading( $elLoading, false );
    				loadList( false );
    			};
    			scope.onCreate = function() {
    				if ( isRemoving == true ) return;
    				
    				isCreate = true;
    				$elEditGuide.html( "Create new pinpoint user" );
    				$elEditSelectApplication.select2("val", "");
    				$elEditSelectRules.select2( "val", "");
    				$elEditInputThreshold.val("0");
    				$elEditCheckboxSMS.prop("checked", "");
    				$elEditCheckboxEmail.prop("checked", "");		
    				$elEditTextareaNotes.val("");
    				$alarmListTemplateService.show( $elEdit );
    			};
    			scope.onUpdate = function($event) {
    				if ( isRemoving == true ) return;
    				
    				isCreate = false;
    				var $el = $( $event.toElement ).parents("tr");
    				var ruleID = $el.prop("id").split("_")[1];
    				var oRule = searchRule( ruleID );
    				
    				$elEditGuide.html( "아래 항목을 수정할 수 있습니다." );
    				$elEditSelectApplication.select2("val", oRule.applicationId +"@" + oRule.serviceType).parent().prop("id", "updateRule_" + ruleID);
    				$elEditSelectRules.select2( "val", oRule.checkerName);
    				$elEditInputThreshold.val( oRule.threshold );
    				$elEditCheckboxSMS.prop("checked", oRule.smsSend);
    				$elEditCheckboxEmail.prop("checked", oRule.emailSend);		
    				$elEditTextareaNotes.val( oRule.notes );
    				
    				$alarmListTemplateService.show( $elEdit );
    			};
    			scope.onInputFilter = function($event) {
    				if ( isRemoving == true ) return;
    				
    				if ( $event.keyCode == 13 ) { // Enter
    					scope.onFilterGroup();
    					return;
    				}
    				if ($.trim( $elFilterInputApplication.val() ).length >= 3 || $.trim( $elFilterInputRule.val() ).length) {
    					$elFilterEmpty.removeClass("disabled");
    				} else {
    					$elFilterEmpty.addClass("disabled");
    				}
    			};
    			scope.onFilterGroup = function() {
    				if ( isRemoving == true ) return;
    				var queryApplication = $.trim( $elFilterInputApplication.val() );
    				var queryRule = $.trim( $elFilterInputRule.val() );
    				
    				if ( (queryApplication.length != 0 && queryApplication.length < 3) || (queryRule.length != 0 && queryRule.length < 3)  ) {
    					scope.onEnter("greater2");
    					return;
    				}
    				if ( queryApplication == "" && queryRule == "" ) {
    					if ( scope.ruleList.length != ruleList.length ) {
    						scope.ruleList = ruleList;
    						$alarmListTemplateService.unsetFilterBackground( $elWrapper );
    					}
    					$elFilterEmpty.addClass("disabled");
    				} else {
    					var newFilterRules = [];
    					var length = ruleList.length;
    					for( var i = 0 ; i < ruleList.length ; i++ ) {
    						if ( queryApplication == "" ) {
    							if ( ruleList[i].checkerName.indexOf( queryRule ) != -1 ) {
    								newFilterRules.push( ruleList[i] );
    							}
    						} else if ( queryRule == "" ) {
    							if ( ruleList[i].applicationId.indexOf( queryApplication ) != -1 ) {
    								newFilterRules.push( ruleList[i] );
    							}							
    						} else {
    							if ( ruleList[i].applicationId.indexOf( queryApplication ) != -1 && ruleList[i].checkerName.indexOf( queryRule ) != -1 ) {
    								newFilterRules.push( ruleList[i] );
    							}
    						}
    					}
    					scope.ruleList = newFilterRules;
    					$alarmListTemplateService.setFilterBackground( $elWrapper );
    				}
    			};
    			scope.onFilterEmpty = function() {
    				if ( isRemoving == true ) return;
    				if ( $.trim( $elFilterInputApplication.val() ) == "" && $.trim( $elFilterInputRule.val() ) == "" ) return;
    				$elFilterInputApplication.val("");
    				$elFilterInputRule.val("");
    				scope.onFilterGroup();
    			};
    			scope.onInputEdit = function($event) {
    				if ( $event.keyCode == 27 ) { // ESC
    					scope.onCancelEdit();
    					$event.stopPropagation();
    				}
    			};
    			scope.onCancelEdit = function() {
    				$alarmListTemplateService.hide( $elEdit );
    			};
    			scope.onApplyEdit = function() {
    				var applicationNServiceType = $elEditSelectApplication.select2("val").split("@"); 
    				var ruleID = $elEditSelectRules.select2( "val");
    				var threshold = $elEditInputThreshold.val();
    				var sms = $elEditCheckboxSMS.prop("checked");
    				var email = $elEditCheckboxEmail.prop("checked");	
    				var notes = $elEditTextareaNotes.val();
    				
    				if ( applicationNServiceType[0] == "" || ruleID == "" ) {
    					scope.onEnter("notEmpty");
    					return;
    				}				
    				$alarmListTemplateService.showLoading( $elLoading, true );
    				if ( $alarmListTemplateService.hasDuplicateItem( ruleList, function( rule ) {
    					return rule.applicationId == applicationNServiceType[0] && rule.ruldId == ruleID;
    				}) && isCreate == true) {
    					$alarmListTemplateService.showAlert( $elAlert, "동일하게 설정된 ruleSet이 이미 있습니다." );
    					return;
    				}
    				if ( isCreate ) {
    					createRule( applicationNServiceType[0], applicationNServiceType[1], ruleID, threshold, sms, email, notes );
    				} else {
    					updateRule( $elEditSelectApplication.parent().prop("id").split("_")[1], applicationNServiceType[0], applicationNServiceType[1], ruleID, threshold, sms, email, notes );
    				}
    			};
    			scope.onCloseAlert = function() {
    				$alarmListTemplateService.closeAlert( $elAlert, $elLoading );
    			};
    			// onEnter, onLeave
    			scope.onEnter = function( type ) {
    				$alarmListTemplateService.setGuide( $elGuideMessage, "rule", type, ruleList.length );
    			};
    			scope.onLeave = function( type ) {
    				$alarmListTemplateService.setGuide( $elGuideMessage, "rule", "" );
    			}
    			scope.$on("alarmRule.configuration.load", function( event, userGroupID ) {
    				currentUserGroupID = userGroupID;
//    				if ( isLoadedRuleList === false ) {
    					loadList( true );
    					loadRuleSet();
//    				}
    			});
    			scope.$on("alarmRule.configuration.selectNone", function( event ) {
    				currentUserGroupID = "";
    				ruleList = scope.ruleList = [];
    			});

    			scope.$on("alarmRule.applications.set", function( event, applicationData ) {
    				scope.applications = applicationData;
    				initApplicationSelect();
    			});            	
            }
        };
    }]);
})(jQuery);