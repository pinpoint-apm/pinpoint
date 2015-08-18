(function($) {
	'use strict';
	
	/**
	 * (en) Alarm 설정의 모든 Ajax 요청을 대리함. 
	 * @ko Alarm 설정의 모든 Ajax 요청을 대리함.
	 * @group Service
	 * @name AlarmAjaxService
	 * @class
	 */	
	pinpointApp.constant('AlarmAjaxServiceConfig', {
		group: "/userGroup.pinpoint", //POST, GET, PUT, DELETE
		groupMember: "/userGroup/member.pinpoint",
		pinpointUser: "/user.pinpoint",
		alarmRule: "/alarmRule.pinpoint",
		alarmRuleSet: "/alarmRule/checker.pinpoint"
	});
	
	pinpointApp.service('AlarmAjaxService', [ 'AlarmAjaxServiceConfig', function ($config) {
		this.getUserGroupList = function(callback) {
			retrieve($config.group, {}, callback);
		};
		this.createUserGroup = function(data, callback ) {
			create($config.group, data, callback);
		};
		this.updateUserGroup = function(data, callback ) {
			update($config.group, data, callback);
		};
		this.removeUserGroup = function(data, callback ) {
			remove($config.group, data, callback);
		};
		this.addMemberInGroup = function(data, callback ) {
			create($config.groupMember, data, callback);
		};
		this.getGroupMemberListInGroup = function(data, callback) {
			retrieve($config.groupMember, data, callback);
		};
		this.removeMemberInGroup = function(data, callback) {
			remove($config.groupMember, data, callback);
		};
		this.getPinpointUserList = function(callback) {
			retrieve($config.pinpointUser, {}, callback);
		};
		this.createPinpointUser = function(data, callback ) {
			create($config.pinpointUser, data, callback);
		};
		this.updatePinpointUser = function(data, callback ) {
			update($config.pinpointUser, data, callback);
		};
		this.removePinpointUser = function(data, callback ) {
			remove($config.pinpointUser, data, callback);
		};		
		this.getRuleList = function(data, callback) {
			retrieve($config.alarmRule, data, callback);
		};
		this.createRule = function(data, callback ) {
			create($config.alarmRule, data, callback);
		};
		this.updateRule = function(data, callback ) {
			update($config.alarmRule, data, callback);
		};
		this.removeRule = function(data, callback ) {
			remove($config.alarmRule, data, callback);
		};	
		this.getRuleSet = function(callback) {
			retrieve($config.alarmRuleSet, {}, callback);
		}
		function create(url, data, callback) {
			$.ajax(url, {
				type: "POST",
				data: JSON.stringify(data),
				contentType: "application/json"
			}).done(function(result) {
				callback(result);
			}).fail(function(error) {
				callback(error);
			});
		}
		function update(url, data, callback) {
			$.ajax(url, {
				type: "PUT",
				data: JSON.stringify(data),
				contentType: "application/json"
			}).done(function(result) {
				callback(result);
			}).fail(function(error) {
				callback(error);
			});
		}
		function remove(url, data, callback) {
			$.ajax(url, {
				type: "DELETE",
				data: JSON.stringify(data),
				contentType: "application/json"
			}).done(function(result) {
				callback(result);
			}).fail(function(error) {
				callback(error);
			});
		}
		function retrieve(url, data, callback) {
			$.ajax(url, {
				type: "GET",
				data: data
			}).done(function(result) {
				callback(result);
			}).fail(function(error) {
				callback(error);
			});
		}
	}]);
})(jQuery);