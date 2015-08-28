(function($) {
	'use strict';
	
	/**
	 * (en)  
	 * @ko 
	 * @group Service
	 * @name AlarmBroadcastService
	 * @class
	 */	
	
	pinpointApp.service('AlarmBroadcastService', [ '$rootScope', function ($rootScope) {
		var self = this;
		// from userGroup
		this.sendInit = function( userGroupID ) {
			//$rootScope.$broadcast( "alarmPinpointUser.configuration.load" );
			self.sendReloadWithUserGroupID( userGroupID );
		};
		// from userGroup
		this.sendReloadWithUserGroupID = function( userGroupID ) {
			$rootScope.$broadcast( "alarmGroupMember.configuration.load", userGroupID );
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
		}
	}]);
})(jQuery);