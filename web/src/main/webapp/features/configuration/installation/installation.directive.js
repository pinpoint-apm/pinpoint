(function( $ ) {
	"use strict";

	pinpointApp.directive( "installationDirective", [ "$http", "InstallationAjaxService", "helpContentService",
		function ( $http, InstallationAjaxService, helpContentService ) {
			return {
				restrict: "EA",
				replace: true,
				templateUrl: "features/configuration/installation/installation.html?v=" + G_BUILD_TIME,
				scope: {
					namespace: "@"
				},
				link: function( scope, element, attr ) {
					var MAX_CHAR = 24;
					var $element = $(element);
					var myName = attr["name"];
					var $textarea = $element.find("textarea")[0];
					var $applicationNameInput = $element.find(".application-name-input");
					var $agentIdInput = $element.find(".agent-id-input");
					var jvmArguments = [
						"-Dpinpoint.applicationName=[Application Name]",
						"-Dpinpoint.agentId=[AgentId]"
					];
					var successAppName = "";
					var successAgentId = "";
					var intallationArgument = "";

					var lengthGuide = helpContentService.configuration.installation.lengthGuide.replace(/\{\{MAX\_CHAR\}\}/, MAX_CHAR);
					scope.description = helpContentService.configuration.installation.desc;
					scope.downloadLink = "";
					scope.applicationNameMessage = "";
					scope.agentIdMessage = "";
					scope.inputApplicationName = "";
					scope.inputAgentId = "";

					$element[ attr["initState"] ]();

					function loadInstallationInfo() {
						InstallationAjaxService.getAgentInstallationInfo(function ( res ) {
							if ( res.code === 0 ) {
								scope.downloadLink = res.message["downloadUrl"];
								intallationArgument = res.message["installationArgument"];
								setArguments("", "");
							}
						});
					}
					function setArguments(appName, agentId) {
						$textarea.value = intallationArgument + "\n"
							+ jvmArguments[0].replace(/\[Application Name\]/, appName) + "\n"
							+ jvmArguments[1].replace(/\[AgentId\]/, agentId);
					}
					function setClassName($el, className) {
						$el.get(0).className = className;
					}
					function clearArea() {
						successAppName = "";
						successAgentId = "";
						scope.inputAgentId = "";
						scope.inputApplicationName = "";
						scope.agentIdMessage = "";
						scope.applicationNameMessage = "";
						setClassName( $agentIdInput, "form-group has-feedback" );
						setClassName( $applicationNameInput, "form-group has-feedback" );
						setClassName( $agentIdInput.find("span").hide(), "glyphicon form-control-feedback" );
						setClassName( $applicationNameInput.find("span").hide(), "glyphicon form-control-feedback" );
					}
					function setSuccess( $ele ) {
						$ele.removeClass("has-error").addClass("has-success");
						$ele.find("span").removeClass("glyphicon-remove").addClass("glyphicon-ok").show();
					}
					function setFail( $ele ) {
						$ele.removeClass("has-success").addClass("has-error");
						$ele.find("span").removeClass("glyphicon-ok").addClass("glyphicon-remove").show();
					}
					function isValidLength(query) {
						return query.length === 0 || query.length > MAX_CHAR ? false : true;
					}
					scope.copyArguments = function() {
						$textarea.select();
						try {
							document.execCommand("copy");
						} catch (err) {
						}
					};
					scope.onKeyDown = function($event, type) {
						if ( $event.keyCode === 13 ) { // enter
							if ( type === "applicationName" ) {
								scope.searchApplicationName();
							} else if ( type === "agentId" ) {
								scope.searchAgentId();
							}
						}
					};
					scope.searchApplicationName = function() {
						var query = scope.inputApplicationName.trim();
						if ( isValidLength(query) === false ) {
							scope.applicationNameMessage = lengthGuide;
							setFail( $agentIdInput );
							return;
						}
						InstallationAjaxService.isAvailableApplicationName({
							applicationName: query
						}, function ( res ) {
							if ( res.code === 0 ) {
								successAppName = query;
								scope.applicationNameMessage = "";
								setSuccess( $applicationNameInput );
								setArguments( query, successAgentId );
							} else if ( res.code === -1 ) {
								successAppName = "";
								scope.applicationNameMessage = res.message;
								setFail( $applicationNameInput );
							}
						});

					};
					scope.searchAgentId = function() {
						var query = scope.inputAgentId.trim();
						if ( isValidLength(query) === false ) {
							scope.agentIdMessage = lengthGuide;
							setFail( $agentIdInput );
							return;
						}
						InstallationAjaxService.isAvailableAgentId({
							agentId: query
						}, function ( res ) {
							if ( res.code === 0 ) {
								successAgentId = query;
								scope.agentIdMessage = "";
								setSuccess( $agentIdInput );
								setArguments( successAppName, query );
							} else if ( res.code === -1 ) {
								successAgentId = "";
								scope.agentIdMessage = res.message;
								setFail( $agentIdInput );
							}
						});
					};
					scope.$on( "configuration.selectMenu", function( event, selectedName ) {
						if ( myName === selectedName ) {
							loadInstallationInfo();
							$element.show();
						} else {
							$element.hide();
							clearArea();
						}
					});

				}
			};
		}
	]);
})( jQuery );