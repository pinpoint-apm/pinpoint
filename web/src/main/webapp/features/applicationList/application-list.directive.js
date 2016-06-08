(function( $ ) {
	'use strict';
	pinpointApp.constant( "applicationListDirectiveConfig", {
		ID: "APPLICATION_LIST_DRTV_",
		AT: "@"
	});

	pinpointApp.directive( "applicationListDirective", [ "applicationListDirectiveConfig", "$rootScope", "$http", "$timeout", "UrlVoService", "AnalyticsService", "PreferenceService", "CommonAjaxService", "CommonUtilService",
		function ( cfg, $rootScope, $http, $timeout, UrlVoService, AnalyticsService, PreferenceService, CommonAjaxService, CommonUtilService ) {
			return {
				restrict: 'EA',
				replace: true,
				templateUrl: 'features/applicationList/applicationList.html?v=' + G_BUILD_TIME,
				link: function (scope, element) {
					cfg.ID += CommonUtilService.getRandomNum();

					var $application = element.find( ".application" );
					var applicationOriginalData;
					var iconPath = PreferenceService.getIconPath();

					init();

					function init() {
						scope.applicationList = [{
							text: 'Loading...',
							value: ''
						}];
						scope.selectedApplication = "";
						scope.disableApplication = true;
						scope.hideFakeApplication = false;

						initSelect2();
						getApplicationList();
					}
					function initSelect2() {
						$application.select2({
							allowClear: false,
							placeholder: "Select an application",
							searchInputPlaceholder: "Input your application name",
							templateResult: formatOptionText,
							templateSelection: formatOptionText,
							escapeMarkup: function (m) {
								return m;
							}
						}).on("select2:select", function () {
							changeApplication();
						});
					}
					function changeApplication() {
						AnalyticsService.send( AnalyticsService.CONST.MAIN, AnalyticsService.CONST.CLK_APPLICATION );
						scope.selectedApplication = $application.val();
						UrlVoService.setApplication( scope.selectedApplication );
						UrlVoService.setAgentId( "" );
						scope.$emit( "up.changed.application.url", cfg.ID );
					}
					function broadcastApplicationListChanged() {
						$rootScope.$broadcast("alarmRule.applications.set", scope.applicationList);
						$rootScope.$broadcast("configuration.general.applications.set", scope.applicationList);
					}
					function getApplicationList() {
						CommonAjaxService.getApplicationList( function( data ) {
							if ( angular.isArray( data ) === false || data.length === 0 ) {
								scope.applicationList[0].text = 'Application not found.';
								broadcastApplicationListChanged();
							} else {
								scope.disableApplication = false;
								applicationOriginalData = data;
								parseApplicationList();
								$timeout( function () {
									if ( UrlVoService.getApplication() ) {
										$application.val( UrlVoService.getApplication() ).trigger( "change" );
										scope.selectedApplication = UrlVoService.getApplication();
									} else {
										//$application.select2("open");
										$applicatoinSelect2("open");
									}
									broadcastApplicationListChanged();
								});
							}
							scope.hideFakeApplication = true;
						}, function() {
							scope.applicationList[0].text = 'Application error.';
							scope.hideFakeApplication = true;
						});
					}
					function parseApplicationList() {
						var aSavedFavoriteList = PreferenceService.getFavoriteList();
						scope.favoriteCount = aSavedFavoriteList.length;
						scope.applicationList = [{
							text: '',
							value: ''
						}];
						var aFavoriteList = [];
						var aGeneralList = [];
						angular.forEach( applicationOriginalData, function ( oValue ) {
							var fullName = oValue.applicationName + cfg.AT + oValue.serviceType;
							var value = {
								text: fullName,
								value: oValue.applicationName + cfg.AT + oValue.code
							};
							if ( aSavedFavoriteList.indexOf( fullName ) === -1 ) {
								aGeneralList.push( value );
							} else {
								aFavoriteList.push( value );
							}
						});
						scope.applicationList = aFavoriteList.concat( aGeneralList );
					}
					function formatOptionText( state ) {
						if ( !state.id ) {
							return state.text;
						}
						var chunk = state.id.split( cfg.AT );
						if ( chunk.length > 1 ) {
							return $("<img>")
								.attr( "src", iconPath + chunk[1] + ".png" )
								.css({
									"height": "25px",
									"paddingRight": "3px"
								}).get(0).outerHTML + "<span>" + chunk[0] + "</span>";
						} else {
							return state.text;
						}
					}
					// scope.$on( "down.initialize", function() {
					// 	init();
					// });
					scope.$on( "down.changed.favorite", function() {
						console.log( "down.changed.favorite" );
						parseApplicationList();
					});
				}
			};
		}
	]);
})( jQuery );
