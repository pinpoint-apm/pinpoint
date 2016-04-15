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
		this.sendInit = function( userGroupID ) {
			self.sendReloadWithUserGroupID( userGroupID, globalConfig.userId ? {
				userId: globalConfig.userId,
				name: globalConfig.userName,
				department: globalConfig.userDepartment
			} : {} );
		};
		this.sendLoadPinpointUser = function() {
			if ( globalConfig.userId ) {
				$rootScope.$broadcast("alarmPinpointUser.configuration.load", globalConfig.userDepartment );
			}
		};
		// from userGroup
		this.sendReloadWithUserGroupID = function( userGroupID, willBeAddedUser ) {
			$rootScope.$broadcast( "alarmGroupMember.configuration.load", userGroupID, willBeAddedUser );
			$rootScope.$broadcast( "alarmRule.configuration.load", userGroupID );
		};
		// from userGroup
		this.sendSelectionEmpty = function() {
			$rootScope.$broadcast( "alarmGroupMember.configuration.selectNone" );
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
		this.sendUserRemoved = function( userID ) {
			$rootScope.$broadcast( "alarmGroupMember.configuration.removeUser", userID );
		};
	}]);
})(jQuery);