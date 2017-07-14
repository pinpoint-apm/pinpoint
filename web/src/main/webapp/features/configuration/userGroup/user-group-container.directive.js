(function( $ ) {
	"use strict";

	pinpointApp.directive( "userGroupContainerDirective", [ "SystemConfigurationService",
		function ( SystemConfigService ) {
			return {
				restrict: "EA",
				replace: true,
				templateUrl: "features/configuration/userGroup/userGroupContainer.html?v=" + G_BUILD_TIME,
				link: function( scope, element, attr ) {
					var $element = element;
					var myName = attr["name"];
					$element[ attr["initState"] ]();

					scope.$on( "configuration.selectMenu", function( event, selectedName ) {
						if ( myName === selectedName ) {
							scope.$broadcast( "configuration.userGroup.show" );
							scope.$broadcast( "pinpointUser.load", SystemConfigService.get("userDepartment") );
							$element.show();
						} else {
							$element.hide();
						}
					});
					// userGroup > groupMember
					scope.$on( "userGroup.selectedUserGroup", function( event, userGroupId ) {
						scope.$broadcast( "groupMember.load", userGroupId );
						event.stopPropagation();
					});
					scope.$on( "userGroup.selectedNone", function( event ) {
						scope.$broadcast( "groupMember.selectNone" );
						scope.$broadcast( "pinpointUser.selectNone" );
						event.stopPropagation();
					});
					// groupMember > pinpointUser
					scope.$on( "groupMember.loaded", function( event, oGroupMemberList ) {
						scope.$broadcast( "pinpointUser.checkSelectedMember", oGroupMemberList );
						event.stopPropagation();
					});
					scope.$on( "groupMember.removedMember", function( event, oGroupMemberList, userId ) {
						scope.$broadcast( "pinpointUser.changeSelectedMember", oGroupMemberList, userId );
						event.stopPropagation();
					});
					scope.$on( "groupMember.sendCallbackAddedUser", function( event, bIsSuccess, userId ) {
						scope.$broadcast( "pinpointUser.addUserCallback", bIsSuccess, userId );
						event.stopPropagation();
					});
					// pinpointUser > groupMember
					scope.$on( "pinpointUser.sendUserRemoved", function( event, userId ) {
						scope.$broadcast( "groupMember.removeUser", userId );
						event.stopPropagation();
					});
					scope.$on( "pinpointUser.sendUserUpdated", function( event, oPinpointUser ) {
						scope.$broadcast( "groupMember.updateUser", oPinpointUser );
						event.stopPropagation();
					});
					scope.$on( "pinpointUser.sendUserAdd", function( event, oPinpointUser ) {
						scope.$broadcast( "groupMember.addMember", oPinpointUser );
						event.stopPropagation();
					});
				}
			};
		}
	]);
})( jQuery );