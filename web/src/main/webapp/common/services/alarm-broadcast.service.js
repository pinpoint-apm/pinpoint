(function($) {
	'use strict';
	
	/**
	 * (en)  
	 * @ko 
	 * @group Service
	 * @name AlarmBroadcastService
	 * @class
	 */	
	
	pinpointApp.service("AlarmBroadcastService", [ "$rootScope", "globalConfig", function ( $rootScope, globalConfig ) {
		var self = this;
		// from userGroup
		this.sendInit = function( userGroupId ) {
			self.sendReloadWithUserGroupID( userGroupId, globalConfig.userId ? {
				userId: globalConfig.userId,
				name: globalConfig.userName,
				department: globalConfig.userDepartment
			} : {} );
		};
		this.sendLoadPinpointUser = function() {
			// if ( globalConfig.userId ) {
				$rootScope.$broadcast("alarmPinpointUser.configuration.load", globalConfig.userDepartment );
			// }
		};
		// from userGroup
		this.sendReloadWithUserGroupID = function( userGroupId, willBeAddedUser ) {
			$rootScope.$broadcast( "alarmGroupMember.configuration.load", userGroupId, willBeAddedUser );
			$rootScope.$broadcast( "alarmRule.configuration.load", userGroupId );
		};
		// from userGroup
		this.sendSelectionEmpty = function() {
			$rootScope.$broadcast( "alarmGroupMember.configuration.selectNone" );
			$rootScope.$broadcast( "alarmPinpointUser.configuration.selectNone" );
			$rootScope.$broadcast( "alarmRule.configuration.selectNone" );
		};
		// groupMember -> pinpointUser
		this.sendCallbackAddedUser = function( bIsSuccess ) {
			$rootScope.$broadcast( "alarmPinpointUser.configuration.addUserCallback", bIsSuccess );
		};
		// pinpointUser - > groupMember
		this.sendUserAdd = function( oUser ) {
			$rootScope.$broadcast( "alarmGroupMember.configuration.addUser", oUser );
		};
		// pinpointUser - > groupMember
		this.sendUserUpdated = function( oUser ) {
			$rootScope.$broadcast( "alarmGroupMember.configuration.updateUser", oUser );
		};
		// pinpointUser - > groupMember
		this.sendUserRemoved = function( userId ) {
			$rootScope.$broadcast( "alarmGroupMember.configuration.removeUser", userId );
		};
		this.sendGroupMemberLoaded = function( oGroupMemberList ) {
			$rootScope.$broadcast( "alarmPinpointUser.configuration.groupLoaded", oGroupMemberList );
		};
		this.sendGroupMemberRemoved = function( oGroupMemberList, userId ) {
			$rootScope.$broadcast( "alarmPinpointUser.configuration.groupUserRemoved", oGroupMemberList, userId );
		};
	}]);
})(jQuery);