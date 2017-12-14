(function( $ ) {
	"use strict";

	pinpointApp.directive( "applicationGroupDirective", [ "CommonAjaxService",
		function ( CommonAjaxService ) {
			return {
				restrict: "EA",
				replace: true,
				templateUrl: "features/configuration/application/applicationGroup.html?v=" + G_BUILD_TIME,
				scope: true,
				link: function( scope, element, attr ) {
					var $element =$(element);
					var bLoaded = false;
					var $previous = null;
					scope.applicationList = [];

					init();

					function init() {
						$element.find("ul").on("click", function($event) {
							var $target = $( $event.toElement || $event.target );
							var tagName = $target.get(0).tagName.toLowerCase();

							if ( tagName === "span" || tagName === "img" ) {
								selectApplication( $target.parents("li") );
							} else if ( tagName === "li" ) {
								selectApplication( $target );
							}
						});
					}
					function selectApplication( $ele ) {
						if ( $previous !== null ) {
							$previous.removeClass( "selected" );
						}
						$ele.addClass( "selected" );
						$previous = $ele;
						scope.$emit( "applicationGroup.selectApp", $ele.attr("id") );
					}
					function getApplicationList() {
						CommonAjaxService.getApplicationList( function( data ) {
							if ( angular.isArray( data ) === false || data.length === 0 ) {
							} else {
								scope.applicationList = data;
							}
						}, function() {
						});
					}
					scope.getAppId = function( oApp ) {
						return oApp.applicationName + "@" + oApp.serviceType;
					};
					scope.$on( "applicationGroup.load", function() {
						if ( bLoaded === false ) {
							getApplicationList();
							bLoaded = true;
						}
					});
				}
			};
		}
	]);
})( jQuery );