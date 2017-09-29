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
		group: "userGroup.pinpoint", //POST, GET, PUT, DELETE
		groupMember: "userGroup/member.pinpoint",
		pinpointUser: "user.pinpoint",
		alarmRule: "application/alarmRule.pinpoint",
		alarmRuleSet: "application/alarmRule/checker.pinpoint"
	});
	
	pinpointApp.service('AlarmAjaxService', [ 'AlarmAjaxServiceConfig', '$http', function ($config, $http) {
		this.getUserGroupList = function(data, callback) {
			retrieve($config.group, data, callback);
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
		this.getPinpointUserList = function(data, callback) {
			retrieve($config.pinpointUser, data, callback);
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
		this.getRuleSet = function(data, callback) {
			retrieve($config.alarmRuleSet, data, callback);
		};
		function create(url, data, callback) {
			$http.post( url, data )
			.then(function(result) {
				callback(result.data);
			}, function(error) {
				callback(error);
			});
		}
		function update(url, data, callback) {
			$http.put(url, data )
			.then(function(result) {
				callback(result.data);
			}, function(error) {
				callback(error);
			});
		}
		function remove(url, data, callback) {
			$.ajax( url, {
				type: "DELETE",
				data: JSON.stringify(data),
				contentType: "application/json"
			}).done(function(result) {
				callback(result);
			}).fail(function(error) {
				callback(error);
			});
//			$http["delete"](url, data)
//			.then(function(result) {
//				callback(result.data);
//			}, function(error) {
//				callback(error);
//			});
		}
		function retrieve(url, data, callback) {
			$http.get( url + "?" + $.param( data ) ).then(function(result) {
				callback(result.data);
			}, function(error) {
				callback(error);
			});
		}
	}]);
})(jQuery);