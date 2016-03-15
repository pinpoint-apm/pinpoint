(function($) {
	'use strict';
	/**
	 * (en)alarmRuleDirective 
	 * @ko alarmRuleDirective
	 * @group Directive
	 * @name alarmRuleDirective
	 * @class
	 */	
	
	pinpointApp.directive( "alarmRuleDirective", [ "$rootScope", "$document", "$timeout", "AlarmUtilService", "AnalyticsService", "TooltipService",
	    function ($rootScope, $document, $timeout, alarmUtilService, analyticsService, tooltipService) {
        return {
            restrict: 'EA',
            replace: true,
            templateUrl: 'features/configuration/alarm/alarmRule.html?v=' + G_BUILD_TIME,
            scope: true,
            link: function (scope, element) {

    			var $element = $(element);
    			var $elWrapper = $element.find(".wrapper");
    			var $elTotal = $element.find(".total");
    			var $elLoading = $element.find(".some-loading");
    			var $elAlert = $element.find(".some-alert");
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
    				var $target = $( $event.toElement || $event.target );
    				var tagName = $target.get(0).tagName.toLowerCase();
    				var $tr = $target.parents("tr");
    				
    				if ( tagName == "button" ) {
    					if ( $target.hasClass("edit") ) {
    						scope.onUpdate( $event );
    					} else if ( $target.hasClass("confirm-remove") ) {
    						removeConfirm( $tr );
    					} else if ( $target.hasClass("confirm-cancel") ) {
    						removeCancel( $tr );	
    					}
    				} else if ( tagName == "span" ) {
    					if ( $target.hasClass("remove") ) {
    						if ( isRemoving === true ) return;
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

				tooltipService.init( "alarmRules" );
    			function reset() {
    				if ( isRemoving === true ) {
    					$element.find("tr.remove").each( function() {
    						removeCancel( $(this) );
    					});
    				}
    				scope.onCancelEdit();
    				alarmUtilService.unsetFilterBackground( $elWrapper );
        			$elFilterInputApplication.val("");
        			$elFilterInputRule.val("");
    			}
    			function removeConfirm( $el ) {
    				alarmUtilService.showLoading( $elLoading, false );
    				removeUser( alarmUtilService.extractID( $el ) );
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
    				alarmUtilService.sendCRUD( "createRule", oNewRule, function( resultData ) {
    					// @TODO
    					// 많이 쓰는 놈 기준 3개를 뽑아 내야 함.
    					oNewRule.ruleId = resultData.ruleId;
    					ruleList.push(oNewRule);
    					alarmUtilService.setTotal( $elTotal, ruleList.length );
    					$timeout(function() {
    						initPopover();	
    					});
    					alarmUtilService.hide( $elLoading, $elEdit );					
    				}, function( errorData ) {}, $elAlert );
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
    				alarmUtilService.sendCRUD( "updateRule", oUpdateRule, function( resultData ) {
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
    					alarmUtilService.hide( $elLoading, $elEdit );
    					$timeout(function() {
    						initPopover();	
    					});
    				}, function( errorData ) {}, $elAlert );
    			}
    			function removeUser( ruleID ) {
    				alarmUtilService.sendCRUD( "removeRule", { "ruleId": ruleID }, function( resultData ) {
    					scope.$apply(function() {
	    					for( var i = 0 ; i < ruleList.length ; i++ ) {
	    						if ( ruleList[i].ruleId == ruleID ) {
	    							ruleList.splice(i, 1);
	    							break;
	    						}
	    					}
    					});
    					alarmUtilService.setTotal( $elTotal, ruleList.length );
    					alarmUtilService.hide( $elLoading );
    					isRemoving = false;
    				}, function( errorData ) {}, $elAlert );
    			}
    			function loadList( isFirst ) {
    				alarmUtilService.sendCRUD( "getRuleList", { "userGroupId": currentUserGroupID }, function( resultData ) {
    					// @TODO
    					// 많이 쓰는 놈 기준 3개를 뽑아 내야 함.
    					isLoadedRuleList = true;
    					ruleList = scope.ruleList = resultData;
    					alarmUtilService.setTotal( $elTotal, ruleList.length );
    					alarmUtilService.hide( $elLoading );
    					$timeout(function() {
    						initPopover();	
    					});
    				}, function( errorData ) {}, $elAlert );		
    			}
    			function loadRuleSet() {
    				if ( scope.ruleSets.length > 1 ) return;
    				
    				alarmUtilService.sendCRUD( "getRuleSet", {}, function( resultData ) {
    					// @TODO
    					// 많이 쓰는 놈 기준 3개를 뽑아 내야 함.
    					for( var i = 0 ; i < resultData.length ; i++ ) {
    						scope.ruleSets.push( {
    							"text": resultData[i]
    						});
    					}
    				}, function( errorData ) {}, $elAlert );			
    			}
    			
    			scope.ruleSets = [ {"text": ""} ];
    			scope.onRefresh = function() {
    				if ( isRemoving === true ) return;
    				analyticsService.send( analyticsService.CONST.MAIN, analyticsService.CONST.CLK_ALARM_REFRESH_RULE );
    				reset();
    				alarmUtilService.showLoading( $elLoading, false );
    				loadList( false );
    			};
    			scope.onCreate = function() {
    				if ( isRemoving === true ) return;
    				
    				isCreate = true;
    				$elEditGuide.html( "Create new pinpoint user" );
    				$elEditSelectApplication.select2("val", "");
    				$elEditSelectRules.select2( "val", "");
    				$elEditInputThreshold.val("0");
    				$elEditCheckboxSMS.prop("checked", "");
    				$elEditCheckboxEmail.prop("checked", "");		
    				$elEditTextareaNotes.val("");
    				alarmUtilService.show( $elEdit );
    			};
    			scope.onUpdate = function($event) {
    				if ( isRemoving === true ) return;
    				
    				isCreate = false;
    				var $el = $( $event.toElement || $event.target ).parents("tr");
    				var ruleID = alarmUtilService.extractID( $el );
    				var oRule = searchRule( ruleID );
    				
    				$elEditGuide.html( "Update rule data." );
    				$elEditSelectApplication.select2("val", oRule.applicationId +"@" + oRule.serviceType).parent().prop("id", "updateRule_" + ruleID);
    				$elEditSelectRules.select2( "val", oRule.checkerName);
    				$elEditInputThreshold.val( oRule.threshold );
    				$elEditCheckboxSMS.prop("checked", oRule.smsSend);
    				$elEditCheckboxEmail.prop("checked", oRule.emailSend);		
    				$elEditTextareaNotes.val( oRule.notes );
    				
    				alarmUtilService.show( $elEdit );
    			};
    			scope.onInputFilter = function($event) {
    				if ( isRemoving === true ) return;
    				
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
    				if ( isRemoving === true ) return;
    				var queryApplication = $.trim( $elFilterInputApplication.val() );
    				var queryRule = $.trim( $elFilterInputRule.val() );
    				
    				if ( (queryApplication.length !== 0 && queryApplication.length < 3) || (queryRule.length !== 0 && queryRule.length < 3)  ) {
    					alarmUtilService.showLoading( $elLoading, false );
    					alarmUtilService.showAlert( $elAlert, "You must enter at least three characters.");
    					return;
    				}
    				analyticsService.send( analyticsService.CONST.MAIN, analyticsService.CONST.CLK_ALARM_FILTER_RULE );
    				if ( queryApplication === "" && queryRule === "" ) {
    					if ( scope.ruleList.length != ruleList.length ) {
    						scope.ruleList = ruleList;
    						alarmUtilService.unsetFilterBackground( $elWrapper );
    					}
    					$elFilterEmpty.addClass("disabled");
    				} else {
    					var newFilterRules = [];
    					var length = ruleList.length;
    					for( var i = 0 ; i < ruleList.length ; i++ ) {
    						if ( queryApplication === "" ) {
    							if ( ruleList[i].checkerName.indexOf( queryRule ) != -1 ) {
    								newFilterRules.push( ruleList[i] );
    							}
    						} else if ( queryRule === "" ) {
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
    					alarmUtilService.setFilterBackground( $elWrapper );
    				}
    			};
    			scope.onFilterEmpty = function() {
    				if ( isRemoving === true ) return;
    				if ( $.trim( $elFilterInputApplication.val() ) === "" && $.trim( $elFilterInputRule.val() ) === "" ) return;
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
    				alarmUtilService.hide( $elEdit );
    			};
    			scope.onApplyEdit = function() {
    				var applicationNServiceType = $elEditSelectApplication.select2("val").split("@"); 
    				var ruleID = $elEditSelectRules.select2( "val");
    				var threshold = $elEditInputThreshold.val();
    				var sms = $elEditCheckboxSMS.prop("checked");
    				var email = $elEditCheckboxEmail.prop("checked");	
    				var notes = $elEditTextareaNotes.val();
    				
    				if ( applicationNServiceType[0] === "" || ruleID === "" ) {
    					alarmUtilService.showLoading( $elLoading, true );
    					alarmUtilService.showAlert( $elAlert, "Select application name and rule.");	
    					return;
    				}
    				
    				alarmUtilService.showLoading( $elLoading, true );
    				if ( alarmUtilService.hasDuplicateItem( ruleList, function( rule ) {
    					return rule.applicationId === applicationNServiceType[0] && rule.checkerName === ruleID;
    				}) && isCreate === true) {
    					alarmUtilService.showAlert( $elAlert, "Exist a same rule set in the lists" );
    					return;
    				}
    				if ( isCreate ) {
    					analyticsService.send( analyticsService.CONST.MAIN, analyticsService.CONST.CLK_ALARM_CREATE_RULE );
    					createRule( applicationNServiceType[0], applicationNServiceType[1], ruleID, threshold, sms, email, notes );
    				} else {
    					updateRule( alarmUtilService.extractID( $elEditSelectApplication.parent() ), applicationNServiceType[0], applicationNServiceType[1], ruleID, threshold, sms, email, notes );
    				}
    			};
    			scope.onCloseAlert = function() {
    				alarmUtilService.closeAlert( $elAlert, $elLoading );
    			};
    			scope.$on("alarmRule.configuration.load", function( event, userGroupID ) {
    				currentUserGroupID = userGroupID;
					reset();
					loadList( true );
					loadRuleSet();
    			});
    			scope.$on("alarmRule.configuration.selectNone", function( event ) {
    				reset();
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