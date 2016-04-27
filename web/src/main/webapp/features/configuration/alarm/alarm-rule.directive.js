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
				var $elGuide = $element.find(".some-guide");
				var $elWrapper = $element.find(".wrapper");
				var $elTotal = $element.find(".total");
				var $elLoading = $element.find(".some-loading");
				var aEditNodes = [ $element.find("tr._edit1"), $element.find("tr._edit2") ];
				var $elAlert = $element.find(".some-alert");
				var $workingNode = null;

				var currentUserGroupId = "";
				var bIsLoaded = false;
				var oRuleList = [];
				scope.prefix = "alarmRule_";
				scope.ruleList = [];
				scope.ruleSets = [];

				function cancelPreviousWork() {
					AddAlarm.cancelAction( aEditNodes, hideEditArea );
					RemoveAlarm.cancelAction( alarmUtilService, $workingNode, hideEditArea );
					UpdateAlarm.cancelAction( alarmUtilService, $workingNode, aEditNodes, hideEditArea );
				}
				function isSameNode( $current ) {
					return alarmUtilService.extractID( $workingNode ) === alarmUtilService.extractID( $current );
				}
				function showAlert( oServerError ) {
					$elAlert.find( ".message" ).html( oServerError.errorMessage );
					alarmUtilService.hide( $elLoading );
					alarmUtilService.show( $elAlert );
				}
				function loadData() {
					alarmUtilService.show( $elLoading );
					alarmUtilService.sendCRUD( "getRuleList", { "userGroupId": currentUserGroupId }, function( oServerData ) {
						bIsLoaded = true;
						oRuleList = oServerData;
						scope.ruleList = oServerData;
						alarmUtilService.setTotal( $elTotal, oRuleList.length );
						alarmUtilService.hide( $elLoading );
					}, showAlert );
				}
				function loadRuleSet() {
					if ( scope.ruleSets.length > 1 ) return;

					alarmUtilService.sendCRUD( "getRuleSet", {}, function( oServerData ) {
						$.each( oServerData, function( index, oRule ) {
							scope.ruleSets.push({
								"text": oRule
							});
						});
						initRuleSelect();
					}, showAlert );
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
					$element.find("select[name=application]").select2({
						placeholder: "Select an application.",
						searchInputPlaceholder: "Input your application name.",
						allowClear: false,
						formatResult: formatOptionText,
						formatSelection: formatOptionText,
						escapeMarkup: function (m) {
							return m;
						}
					}).on("change", function (e) {});
				}
				function initRuleSelect() {
					$element.find("select[name=rule]").select2({
						searchInputPlaceholder: "Input your rule name.",
						placeholder: "Select an rule.",
						allowClear: false,
						formatResult: formatOptionText,
						formatSelection: formatOptionText,
						escapeMarkup: function (m) {
							return m;
						}
					}).on("change", function (e) {});
				}
				function showAddArea() {
					$elWrapper.find("tbody").prepend( aEditNodes[1] ).prepend( aEditNodes[0] );
					alarmUtilService.hide( aEditNodes[0].find( CONSTS.DIV_EDIT ) );
					alarmUtilService.show( aEditNodes[0].find( CONSTS.DIV_ADD ) );
					$.each( aEditNodes, function( index, $el ) {
						alarmUtilService.show( $el );
					});
				}
				function showEditArea( oRule ) {
					alarmUtilService.hide( aEditNodes[0].find( CONSTS.DIV_ADD ) );
					alarmUtilService.show( aEditNodes[0].find( CONSTS.DIV_EDIT ) );
					aEditNodes[0].find("select[name=application]").select2( "val", oRule.applicationId + "@" + oRule.serviceType );
					aEditNodes[0].find("select[name=rule]").select2( "val", oRule.checkerName );
					aEditNodes[0].find("input[name=threshold]").val( oRule.threshold );
					aEditNodes[0].find("select[name=type]").val( (function() {
						if ( oRule.smsSend && oRule.emailSend ) {
							return "all";
						} else {
							if ( oRule.smsSend ) {
								return "sms";
							}
							if ( oRule.emailSend ) {
								return "email";
							}
						}
						return "";
					})() );
					aEditNodes[1].find("input").val( oRule.notes );
					$.each( aEditNodes, function( index, $el ) {
						alarmUtilService.show( $el );
					});
				}
				function hideEditArea() {
					$.each( aEditNodes, function( index, $el ) {
						alarmUtilService.hide( $el );
					});
					aEditNodes[0].find("select[name=application]").select2( "val", "" );
					aEditNodes[0].find("select[name=rule]").select2( "val", "" );
					aEditNodes[0].find("input[name=threshold]").val( "1" );
					aEditNodes[0].find("select[name=type]").val( "all" );
					aEditNodes[1].find("input").val( "" );
				}
				function getNewRule( ruleId ) {

					var oApplicationData = aEditNodes[0].find("select[name=application]").select2("data");
					var ruleData = aEditNodes[0].find("select[name=rule]").select2("data");
					var aApplication = oApplicationData === null || oApplicationData === undefined ? [ "", "" ] : oApplicationData.id.split("@");
					ruleData = ruleData === null || ruleData === undefined ? "" : ruleData.id;

					var application = aEditNodes[0].find("select[name=application]").select2("val").split("@");
					var notificationType = aEditNodes[0].find("select[name=type]").val();

					var oRule = {
						"applicationId": aApplication[0],
						"serviceType": aApplication[1],
						"userGroupId": currentUserGroupId,
						"checkerName": ruleData,
						"threshold":  aEditNodes[0].find("input[name=threshold]").val(),
						"smsSend": ( notificationType === "all" || notificationType === "sms" ? true : false ),
						"emailSend": ( notificationType === "all" || notificationType === "email" ? true : false ),
						"notes": aEditNodes[1].find("input").val()
					};
					if ( angular.isUndefined( ruleId ) === false ) {
						oRule.ruleId = ruleId;
					}
					return oRule;
				}
				function searchRule( ruleId ) {
					for( var i = 0 ; i < oRuleList.length ; i++ ) {
						if ( oRuleList[i].ruleId == ruleId ) {
							return oRuleList[i];
						}
					}
					return null;
				}

				scope.$on("alarmRule.configuration.selectNone", function( event ) {
					cancelPreviousWork();
					currentUserGroupId = "";
					oRuleList = [];
					scope.ruleList = [];
					alarmUtilService.show( $elGuide );
					alarmUtilService.setTotal( $elTotal, oRuleList.length );
				});
				scope.onAddAlarm = function() {
					if ( currentUserGroupId === "" || AddAlarm.isOn() ) {
						return;
					}
					cancelPreviousWork();
					AddAlarm.onAction( function() {
						showAddArea();
					});
				};
				scope.onApplyAddAlarm = function() {
					AddAlarm.applyAction( alarmUtilService, getNewRule(), aEditNodes, $elLoading, function( application, rule ) {
						return alarmUtilService.hasDuplicateItem( oRuleList, function( oRule ) {
							return oRule.applicationId === application && oRule.checkerName === rule;
						});
					},function( oNewRule  ) {
						analyticsService.send( analyticsService.CONST.MAIN, analyticsService.CONST.CLK_ALARM_CREATE_RULE );
						oRuleList.push( oNewRule );
						scope.ruleList = oRuleList;
						hideEditArea();
						alarmUtilService.setTotal( $elTotal, oRuleList.length );
					}, showAlert );
				};
				scope.onCancelAddAlarm = function() {
					AddAlarm.cancelAction( aEditNodes, hideEditArea );
				};
				scope.onRemoveAlarm = function( $event ) {
					var $node = alarmUtilService.getNode( $event, "tr" );
					if ( $workingNode !== null && isSameNode( $node ) === false ) {
						cancelPreviousWork( $node );
					}
					$workingNode = $node;
					RemoveAlarm.onAction( alarmUtilService, $workingNode );
				};
				scope.onCancelRemoveAlarm = function() {
					RemoveAlarm.cancelAction( alarmUtilService, $workingNode );
				};
				scope.onApplyRemoveAlarm = function() {
					RemoveAlarm.applyAction( alarmUtilService, $workingNode, $elLoading, function( ruleId ) {
						for( var i = 0 ; i < oRuleList.length ; i++ ) {
							if ( oRuleList[i].ruleId == ruleId ) {
								oRuleList.splice(i, 1);
								break;
							}
						}
						scope.ruleList = oRuleList;
						alarmUtilService.setTotal( $elTotal, oRuleList.length );
					}, showAlert );
				};
				scope.onUpdateAlarm = function( $event ) {
					cancelPreviousWork();
					$workingNode = alarmUtilService.getNode( $event, "tr" );
					UpdateAlarm.onAction( alarmUtilService, $workingNode, function( ruleId ) {
						$workingNode.after( aEditNodes[1] ).after( aEditNodes[0] );
						showEditArea( searchRule( ruleId ) );
					});
				};
				scope.onCancelUpdateAlarm = function() {
					UpdateAlarm.cancelAction( alarmUtilService, $workingNode, aEditNodes, hideEditArea );
				};
				scope.onApplyUpdateAlarm = function() {
					UpdateAlarm.applyAction( alarmUtilService, getNewRule( alarmUtilService.extractID( $workingNode ) ), aEditNodes, $workingNode, $elLoading, function( ruleId, application, rule ) {
						return alarmUtilService.hasDuplicateItem( oRuleList, function( oRule ) {
							return ( oRule.applicationId === application && oRule.checkerName === rule ) && oRule.ruleId != ruleId;
						});
					},function( oNewRule ) {
						hideEditArea();
						for( var i = 0 ; i < oRuleList.length ; i++ ) {
							if ( oRuleList[i].ruleId == oNewRule.ruleId ) {
								oRuleList[i].applicationId = oNewRule.applicationId;
								oRuleList[i].serviceType = oNewRule.serviceType;
								oRuleList[i].checkerName = oNewRule.checkerName;
								oRuleList[i].threshold = oNewRule.threshold;
								oRuleList[i].smsSend = oNewRule.smsSend;
								oRuleList[i].emailSend = oNewRule.emailSend;
								oRuleList[i].notes = oNewRule.notes;
							}
						}
						scope.ruleList = oRuleList;
					}, showAlert );
				};
				scope.$on("alarmRule.applications.set", function( event, applicationData ) {
					scope.applications = applicationData;
					initApplicationSelect();
				});
				scope.$on("alarmRule.configuration.load", function( event, userGroupID ) {
					currentUserGroupId = userGroupID;
					cancelPreviousWork();
					alarmUtilService.hide( $elGuide );
					loadData( true );
					loadRuleSet();
				});
				scope.onCloseAlert = function() {
					alarmUtilService.hide( $elAlert );
				};
				scope.getType = function( oRule ) {
					if ( oRule.smsSend && oRule.emailSend ) {
						return "Email, SMS";
					} else if ( oRule.smsSend === false && oRule.emailSend === false ) {
						return "";
					} else {
						if ( oRule.smsSend ) {
							return "SMS";
						}
						if ( oRule.emailSend ) {
							return "Email";
						}
					}
				};
            }
        };
    }]);
	var CONSTS = {
		SELECT_APP_OR_RULE: "Select application name or rule.",
		EXIST_A_SAME: "Exist a same rule set in the list",
		DIV_NORMAL: "div._normal",
		DIV_REMOVE: "div._remove",
		DIV_ADD: "div._add",
		DIV_EDIT: "div._edit"
	};

	var AddAlarm = {
		_bIng: false,
		isOn: function() {
			return this._bIng;
		},
		onAction: function( cb ) {
			this._bIng = true;
			cb();
		},
		cancelAction: function( aEditNodes, cbCancel ) {
			if ( this._bIng === true ) {
				$.each( aEditNodes, function( index, $el ) {
					$el.removeClass("blink-blink");
				});
				cbCancel();
				this._bIng = false;
			}
		},
		applyAction: function( alarmUtilService, oNewRule, aEditNodes, $elLoading, cbHasAlarm, cbSuccess, cbFail ) {
			var self = this;
			alarmUtilService.show( $elLoading );
			if ( oNewRule.applicationId === "" || oNewRule.checkerName === "" ) {
				$.each( aEditNodes, function( index, $el ) {
					$el.addClass("blink-blink");
				});
				cbFail({ errorMessage: CONSTS.SELECT_APP_OR_RULE });
				return;
			}
			if ( cbHasAlarm( oNewRule.applicationId, oNewRule.checkerName ) ) {
				$.each( aEditNodes, function( index, $el ) {
					$el.addClass("blink-blink");
				});
				cbFail({ errorMessage: CONSTS.EXIST_A_SAME });
				return;
			}
			alarmUtilService.sendCRUD( "createRule", oNewRule, function( oServerData ) {
				oNewRule.ruleId = oServerData.ruleId;
				cbSuccess( oNewRule );
				self.cancelAction( aEditNodes, function() {} );
				alarmUtilService.hide( $elLoading );
			}, function( oServerError ) {
				cbFail( oServerError );
			});
		}
	};
	var RemoveAlarm = {
		_bIng: false,
		onAction: function( alarmUtilService, $node ) {
			this._bIng = true;
			$node.addClass("remove");
			alarmUtilService.hide( $node.find( CONSTS.DIV_NORMAL ) );
			alarmUtilService.show( $node.find( CONSTS.DIV_REMOVE ) );
		},
		cancelAction: function( alarmUtilService, $node ) {
			if ( this._bIng === true ) {
				$node.removeClass("remove");
				alarmUtilService.hide($node.find( CONSTS.DIV_REMOVE ));
				alarmUtilService.show($node.find( CONSTS.DIV_NORMAL ));
				this._bIng = false;
			}
		},
		applyAction: function( alarmUtilService, $node, $elLoading, cbSuccess, cbFail ) {
			var self = this;
			alarmUtilService.show( $elLoading );
			var ruleId = alarmUtilService.extractID( $node );
			alarmUtilService.sendCRUD( "removeRule", { "ruleId": ruleId }, function( oServerData ) {
				self.cancelAction( alarmUtilService, $node );
				cbSuccess( ruleId );
				alarmUtilService.hide( $elLoading );
			}, function( oServerError ) {
				cbFail( oServerError );
			});
		}
	};
	var UpdateAlarm = {
		_bIng: false,
		onAction: function( alarmUtilService, $node, cb ) {
			this._bIng = true;
			alarmUtilService.hide( $node );
			cb( alarmUtilService.extractID( $node ) );
		},
		cancelAction: function( alarmUtilService, $node, aEditNodes, cbCancel ) {
			if ( this._bIng === true ) {
				cbCancel();
				$.each( aEditNodes, function( index, $el ) {
					$el.removeClass("blink-blink");
				});
				alarmUtilService.show( $node );
				this._bIng = false;
			}
		},
		applyAction: function( alarmUtilService, oUpdateRule, aEditNodes, $node, $elLoading, cbHasAlarm, cbSuccess, cbFail ) {
			var self = this;
			alarmUtilService.show($elLoading);
			if ( cbHasAlarm( oUpdateRule.ruleId, oUpdateRule.applicationId, oUpdateRule.checkerName ) ) {
				$.each( aEditNodes, function( index, $el ) {
					$el.addClass("blink-blink");
				});
				cbFail({errorMessage: CONSTS.EXIST_A_SAME});
				return;
			}
			alarmUtilService.sendCRUD( "updateRule", oUpdateRule, function( oServerData ) {
				self.cancelAction( alarmUtilService, $node, function () {});
				cbSuccess( oUpdateRule );
				alarmUtilService.hide($elLoading);
			}, function( oServerError ) {
				cbFail( oServerError );
			} );
		}
	};
})(jQuery);