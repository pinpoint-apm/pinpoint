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
		alramRule: "/alarmRule.pinpoint",
		ruleList: ""
	});
	
	pinpointApp.service('AlarmAjaxService', [ 'AlarmAjaxServiceConfig', function ($config) {
		this.getUserGroupList = function(callback) {
			$.ajax($config.group, {
				type: "GET"
			}).done(function(result) {
				callback(result);
			}).fail(function(error) {
				callback(error);
			});
		};
		this.createUserGroup = function(data, callback ) {
			$.ajax($config.group, {
				type: "POST",
				data: JSON.stringify(data),
				contentType: "application/json"
			}).done(function(result) {
				callback(result);
			}).fail(function(error) {
				callback(error);
			});
		};
		this.updateUserGroup = function(data, callback ) {
			$.ajax($config.group, {
				type: "PUT",
				data: JSON.stringify(data),
				contentType: "application/json"
			}).done(function(result) {
				callback(result);
			}).fail(function(error) {
				callback(error);
			});
		};
		this.removeUserGroup = function(data, callback ) {
			$.ajax($config.group, {
				type: "DELETE",
				data: JSON.stringify(data),
				contentType: "application/json"
			}).done(function(result) {
				callback(result);
			}).fail(function(error) {
				callback(error);
			});
		};
		this.addMemberInGroup = function(data, callback ) {
			console.log( "addMember :", data );
			$.ajax($config.groupMember, {
				type: "POST",
				data: JSON.stringify(data),
				contentType: "application/json"
			}).done(function(result) {
				callback(result);
			}).fail(function(error) {
				callback(error);
			});
		};
		this.getGroupMemberListInGroup = function(data, callback) {
			$.ajax($config.groupMember, {
				type: "GET",
				data: data
			}).done(function(result) {
				callback(result);
			}).fail(function(error) {
				callback(error);
			});
		};
		this.removeMemberInGroup = function(data, callback) {
			$.ajax($config.groupMember, {
				type: "DELETE",
				data: JSON.stringify(data),
				contentType: "application/json"
			}).done(function(result) {
				callback(result);
			}).fail(function(error) {
				callback(error);
			});
		};
		this.getPinpointUserList = function(callback) {
			$.ajax($config.pinpointUser, {
				type: "GET"
			}).done(function(result) {
				callback(result);
			}).fail(function(error) {
				callback(error);
			});
		};
		this.createPinpointUser = function(data, callback ) {
			$.ajax($config.pinpointUser, {
				type: "POST",
				data: JSON.stringify(data),
				contentType: "application/json"
			}).done(function(result) {
				callback(result);
			}).fail(function(error) {
				callback(error);
			});
		};
		this.updatePinpointUser = function(data, callback ) {
			$.ajax($config.pinpointUser, {
				type: "PUT",
				data: JSON.stringify(data),
				contentType: "application/json"
			}).done(function(result) {
				callback(result);
			}).fail(function(error) {
				callback(error);
			});
		};
		this.removePinpointUser = function(data, callback ) {
			$.ajax($config.pinpointUser, {
				type: "DELETE",
				data: JSON.stringify(data),
				contentType: "application/json"
			}).done(function(result) {
				callback(result);
			}).fail(function(error) {
				callback(error);
			});
		};		
		this.getRuleList = function(data, callback) {
			$.ajax($config.alramRule, {
				type: "GET",
				data: data
			}).done(function(result) {
				callback(result);
			}).fail(function(error) {
				callback(error);
			});
		};
	}]);
})(jQuery);