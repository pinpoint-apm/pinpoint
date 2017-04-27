(function($) {
	'use strict';
	/**
	 * (en)alarmRuleDirective 
	 * @ko alarmRuleDirective
	 * @group Directive
	 * @name alarmRuleDirective
	 * @class
	 */	
	
	pinpointApp.directive( "alarmRuleDirective", [ "AlarmUtilService", "AnalyticsService",
	    function ( AlarmUtilService, AnalyticsService ) {
        return {
            restrict: 'EA',
            replace: true,
            templateUrl: 'features/configuration/application/alarmRule.html?v=' + G_BUILD_TIME,
            scope: true,
            link: function ( scope, element ) {
				var $element = $(element);
				var $elGuide = $element.find(".some-guide");
				var $elWrapper = $element.find(".wrapper");
				var $elLoading = $element.find(".some-loading");
				var aEditNodes = [ $element.find("tr._edit1"), $element.find("tr._edit2") ];
				var $elAlert = $element.find(".some-alert");
				var $workingNode = null;

				var currentApplicationId = "";
				var bIsLoaded = false;
				var oRuleList = [];
				scope.prefix = "alarmRule_";
				scope.ruleList = [];
				scope.ruleSets = [];
				scope.userGroupList = [];

				function cancelPreviousWork() {
					AddAlarm.cancelAction( aEditNodes, hideEditArea );
					RemoveAlarm.cancelAction( AlarmUtilService, $workingNode, hideEditArea );
					UpdateAlarm.cancelAction( AlarmUtilService, $workingNode, aEditNodes, hideEditArea );
				}
				function isSameNode( $current ) {
					return AlarmUtilService.extractID( $workingNode ) === AlarmUtilService.extractID( $current );
				}
				function showAlert( oServerError ) {
					$elAlert.find( ".message" ).html( oServerError.errorMessage );
					AlarmUtilService.hide( $elLoading );
					AlarmUtilService.show( $elAlert );
				}
				function loadData() {
					AlarmUtilService.show( $elLoading );
					AlarmUtilService.sendCRUD( "getRuleList", { "applicationId": currentApplicationId.split("@")[0] }, function( oServerData ) {
						bIsLoaded = true;
						oRuleList = oServerData;
						scope.ruleList = oServerData;
						AlarmUtilService.hide( $elLoading );
					}, showAlert );
				}
				function loadUserGroup() {
					AlarmUtilService.sendCRUD( "getUserGroupList", "", function( aServerData ) {
						scope.userGroupList = aServerData;
					}, showAlert );
				}
				function loadRuleSet() {
					if ( scope.ruleSets.length > 1 ) return;

					AlarmUtilService.sendCRUD( "getRuleSet", {}, function( aServerData ) {
						scope.ruleSets = aServerData;
					}, showAlert );
				}
				function showAddArea() {
					$elWrapper.find("tbody").prepend( aEditNodes[1] ).prepend( aEditNodes[0] );
					AlarmUtilService.hide( aEditNodes[0].find( CONSTS.DIV_EDIT ) );
					AlarmUtilService.show( aEditNodes[0].find( CONSTS.DIV_ADD ) );
					$.each( aEditNodes, function( index, $el ) {
						AlarmUtilService.show( $el );
					});
				}
				function showEditArea( oRule ) {
					AlarmUtilService.hide( aEditNodes[0].find( CONSTS.DIV_ADD ) );
					AlarmUtilService.show( aEditNodes[0].find( CONSTS.DIV_EDIT ) );
					aEditNodes[0].find("select[name=rule]").val( oRule.checkerName );
					aEditNodes[0].find("select[name=userGroup]").val( oRule.userGroupId );
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
						AlarmUtilService.show( $el );
					});
				}
				function hideEditArea() {
					$.each( aEditNodes, function( index, $el ) {
						AlarmUtilService.hide( $el );
					});
					aEditNodes[0].find("select[name=rule]").val( "" );
					aEditNodes[0].find("select[name=userGroup]").val( "" );
					aEditNodes[0].find("input[name=threshold]").val( "1" );
					aEditNodes[0].find("select[name=type]").val( "all" );
					aEditNodes[1].find("input").val( "" );
				}
				function getNewRule( ruleId ) {
					var notificationType = aEditNodes[0].find("select[name=type]").val();

					var oRule = {
						"applicationId": currentApplicationId.split("@")[0],
						"serviceType": currentApplicationId.split("@")[1],
						"userGroupId":  aEditNodes[0].find("select[name=userGroup]").val(),
						"checkerName": aEditNodes[0].find("select[name=rule]").val(),
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
				
				scope.onAddAlarm = function() {
					if ( currentApplicationId === "" || AddAlarm.isOn() ) {
						return;
					}
					cancelPreviousWork();
					AddAlarm.onAction( function() {
						showAddArea();
					});
				};
				scope.onApplyAddAlarm = function() {
					AddAlarm.applyAction( AlarmUtilService, getNewRule(), aEditNodes, $elLoading, function( userGroupId, rule ) {
						return AlarmUtilService.hasDuplicateItem( oRuleList, function( oRule ) {
							return oRule.userGroupId === userGroupId && oRule.checkerName === rule;
						});
					},function( oNewRule  ) {
						AnalyticsService.sendMain( AnalyticsService.CONST.CLK_ALARM_CREATE_RULE );
						oRuleList.push( oNewRule );
						scope.ruleList = oRuleList;
						hideEditArea();
					}, showAlert );
				};
				scope.onCancelAddAlarm = function() {
					AddAlarm.cancelAction( aEditNodes, hideEditArea );
				};
				scope.onRemoveAlarm = function( $event ) {
					var $node = AlarmUtilService.getNode( $event, "tr" );
					if ( $workingNode !== null && isSameNode( $node ) === false ) {
						cancelPreviousWork( $node );
					}
					$workingNode = $node;
					RemoveAlarm.onAction( AlarmUtilService, $workingNode );
				};
				scope.onCancelRemoveAlarm = function() {
					RemoveAlarm.cancelAction( AlarmUtilService, $workingNode );
				};
				scope.onApplyRemoveAlarm = function() {
					RemoveAlarm.applyAction( AlarmUtilService, $workingNode, $elLoading, function( ruleId ) {
						for( var i = 0 ; i < oRuleList.length ; i++ ) {
							if ( oRuleList[i].ruleId == ruleId ) {
								oRuleList.splice(i, 1);
								break;
							}
						}
						scope.$apply(function() {
							scope.ruleList = oRuleList;
						});
					}, showAlert );
				};
				scope.onUpdateAlarm = function( $event ) {
					cancelPreviousWork();
					$workingNode = AlarmUtilService.getNode( $event, "tr" );
					UpdateAlarm.onAction( AlarmUtilService, $workingNode, function( ruleId ) {
						$workingNode.after( aEditNodes[1] ).after( aEditNodes[0] );
						showEditArea( searchRule( ruleId ) );
					});
				};
				scope.onCancelUpdateAlarm = function() {
					UpdateAlarm.cancelAction( AlarmUtilService, $workingNode, aEditNodes, hideEditArea );
				};
				scope.onApplyUpdateAlarm = function() {
					UpdateAlarm.applyAction( AlarmUtilService, getNewRule( AlarmUtilService.extractID( $workingNode ) ), aEditNodes, $workingNode, $elLoading, function( ruleId, userGroupId, rule ) {
						return AlarmUtilService.hasDuplicateItem( oRuleList, function( oRule ) {
							return ( oRule.userGroupId === userGroupId && oRule.checkerName === rule ) && oRule.ruleId != ruleId;
						});
					},function( oNewRule ) {
						hideEditArea();
						for( var i = 0 ; i < oRuleList.length ; i++ ) {
							if ( oRuleList[i].ruleId == oNewRule.ruleId ) {
								oRuleList[i].checkerName = oNewRule.checkerName;
								oRuleList[i].userGroupId = oNewRule.userGroupId;
								oRuleList[i].threshold = oNewRule.threshold;
								oRuleList[i].smsSend = oNewRule.smsSend;
								oRuleList[i].emailSend = oNewRule.emailSend;
								oRuleList[i].notes = oNewRule.notes;
							}
						}
						scope.ruleList = oRuleList;
					}, showAlert );
				};
				scope.onCaptureUpdateClick = function($event) {
					if ( $event.target.tagName.toUpperCase() === "SPAN" ) {
						var $target = $($event.target);
						if ( $target.hasClass( "update-confirm") ) {
							scope.onApplyUpdateAlarm();
						} else if ( $target.hasClass( "update-cancel") ) {
							scope.onCancelUpdateAlarm();
						} else if ( $target.hasClass( "add-confirm") ) {
							scope.onApplyAddAlarm();
						} else if ( $target.hasClass( "add-cancel" ) ) {
							scope.onCancelAddAlarm();
						}
					}
				};
				scope.$on("applicationGroup.sub.load", function( event, appId, invokeCount ) {
					currentApplicationId = appId;
					cancelPreviousWork();
					AlarmUtilService.hide( $elGuide );
					loadData( true );
					if ( invokeCount === 0 ) {
						loadUserGroup();
					}
					loadRuleSet();
				});
				scope.onCloseAlert = function() {
					AlarmUtilService.hide( $elAlert );
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
		SELECT_USER_GROUP_OR_RULE: "Select user group or rule.",
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
		applyAction: function( AlarmUtilService, oNewRule, aEditNodes, $elLoading, cbHasAlarm, cbSuccess, cbFail ) {
			var self = this;
			AlarmUtilService.show( $elLoading );
			if ( oNewRule.userGroupId === "" || oNewRule.checkerName === "" ) {
				$.each( aEditNodes, function( index, $el ) {
					$el.addClass("blink-blink");
				});
				cbFail({ errorMessage: CONSTS.SELECT_USER_GROUP_OR_RULE });
				return;
			}
			if ( cbHasAlarm( oNewRule.userGroupId, oNewRule.checkerName ) ) {
				$.each( aEditNodes, function( index, $el ) {
					$el.addClass("blink-blink");
				});
				cbFail({ errorMessage: CONSTS.EXIST_A_SAME });
				return;
			}
			AlarmUtilService.sendCRUD( "createRule", oNewRule, function( oServerData ) {
				oNewRule.ruleId = oServerData.ruleId;
				cbSuccess( oNewRule );
				self.cancelAction( aEditNodes, function() {} );
				AlarmUtilService.hide( $elLoading );
			}, function( oServerError ) {
				cbFail( oServerError );
			});
		}
	};
	var RemoveAlarm = {
		_bIng: false,
		onAction: function( AlarmUtilService, $node ) {
			this._bIng = true;
			$node.addClass("remove");
			AlarmUtilService.hide( $node.find( CONSTS.DIV_NORMAL ) );
			AlarmUtilService.show( $node.find( CONSTS.DIV_REMOVE ) );
		},
		cancelAction: function( AlarmUtilService, $node ) {
			if ( this._bIng === true ) {
				$node.removeClass("remove");
				AlarmUtilService.hide($node.find( CONSTS.DIV_REMOVE ));
				AlarmUtilService.show($node.find( CONSTS.DIV_NORMAL ));
				this._bIng = false;
			}
		},
		applyAction: function( AlarmUtilService, $node, $elLoading, cbSuccess, cbFail ) {
			var self = this;
			AlarmUtilService.show( $elLoading );
			var ruleId = AlarmUtilService.extractID( $node );
			AlarmUtilService.sendCRUD( "removeRule", { "ruleId": ruleId }, function( oServerData ) {
				self.cancelAction( AlarmUtilService, $node );
				cbSuccess( ruleId );
				AlarmUtilService.hide( $elLoading );
			}, function( oServerError ) {
				cbFail( oServerError );
			});
		}
	};
	var UpdateAlarm = {
		_bIng: false,
		onAction: function( AlarmUtilService, $node, cb ) {
			this._bIng = true;
			AlarmUtilService.hide( $node );
			cb( AlarmUtilService.extractID( $node ) );
		},
		cancelAction: function( AlarmUtilService, $node, aEditNodes, cbCancel ) {
			if ( this._bIng === true ) {
				cbCancel();
				$.each( aEditNodes, function( index, $el ) {
					$el.removeClass("blink-blink");
				});
				AlarmUtilService.show( $node );
				this._bIng = false;
			}
		},
		applyAction: function( AlarmUtilService, oUpdateRule, aEditNodes, $node, $elLoading, cbHasAlarm, cbSuccess, cbFail ) {
			var self = this;
			AlarmUtilService.show($elLoading);
			if ( cbHasAlarm( oUpdateRule.ruleId, oUpdateRule.applicationId, oUpdateRule.checkerName ) ) {
				$.each( aEditNodes, function( index, $el ) {
					$el.addClass("blink-blink");
				});
				cbFail({errorMessage: CONSTS.EXIST_A_SAME});
				return;
			}
			AlarmUtilService.sendCRUD( "updateRule", oUpdateRule, function( oServerData ) {
				self.cancelAction( AlarmUtilService, $node, aEditNodes, function () {});
				cbSuccess( oUpdateRule );
				AlarmUtilService.hide($elLoading);
			}, function( oServerError ) {
				cbFail( oServerError );
			} );
		}
	};
})(jQuery);