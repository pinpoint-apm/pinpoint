(function( $ ) {
	'use strict';
	pinpointApp.constant( "applicationListDirectiveConfig", {
		ID: "APPLICATION_LIST_DRTV_",
		AT: "@"
	});

	pinpointApp.directive( "applicationListDirective", [ "applicationListDirectiveConfig", "$rootScope", "$http", "$timeout", "AnalyticsService", "PreferenceService", "UserConfigurationService", "CommonAjaxService", "CommonUtilService",
		function ( cfg, $rootScope, $http, $timeout, AnalyticsService, PreferenceService, UserConfigService, CommonAjaxService, CommonUtilService ) {
			return {
				restrict: 'EA',
				replace: true,
				templateUrl: 'features/applicationList/applicationList.html?v=' + G_BUILD_TIME,
				scope: {
					initAppName: "@"
				},
				link: function (scope, element, attr) {
					cfg.ID += CommonUtilService.getRandomNum();

					var $element = $( element );
					var bUseFavorite = attr[ "useFavorite" ] === "true";
					var bNotSetOpen = attr[ "notSetOpen" ] === "true";
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
						if ( bUseFavorite === false ) {
							$element.find("optgroup[label=Favorite]").remove();
						}
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
							changeApplication( $application.val(), $($application.select2("data")[0].element).attr("data-code") );
						});
					}
					function changeApplication( application, code ) {
						AnalyticsService.sendMain( AnalyticsService.CONST.CLK_APPLICATION );
						scope.selectedApplication = application;
						scope.$emit( "up.changed.application", cfg.ID, scope.selectedApplication, code );
					}
					function getApplicationList() {
						CommonAjaxService.getApplicationList( function( data ) {
							if ( angular.isArray( data ) === false || data.length === 0 ) {
								scope.applicationList[0].text = 'Application not found.';
							} else {
								scope.disableApplication = false;
								applicationOriginalData = data;
								parseApplicationList();
								$timeout( function () {
									if ( attr.initAppName ) {
										$application.val( attr.initAppName ).trigger( "change" );
										scope.selectedApplication = attr.initAppName;
									} else {
										if ( bNotSetOpen ) {
											$application.select2("open");
										}
									}
								});
							}
							scope.hideFakeApplication = true;
						}, function() {
							scope.applicationList[0].text = 'Application error.';
							scope.hideFakeApplication = true;
						});
					}
					function parseApplicationList() {
						scope.applicationList = [{
							text: '',
							value: ''
						}];
						var aGeneralList = [];

						if ( bUseFavorite ) {
							UserConfigService.getFavoriteList(function(aSavedFavoriteList) {
								scope.favoriteCount = aSavedFavoriteList.length;

								var aFavoriteList = [];
								angular.forEach(applicationOriginalData, function (oValue) {
									var bFavorite = false;
									for( var j = 0 ; j < aSavedFavoriteList.length ; j++ ) {
										if ( aSavedFavoriteList[j].applicationName === oValue.applicationName && aSavedFavoriteList[j].serviceType === oValue.serviceType ) {
											bFavorite = true;
											break;
										}
									}
									var value = {
										text: oValue.applicationName + cfg.AT + oValue.serviceType,
										value: oValue.applicationName + cfg.AT + oValue.code,
										code: oValue.code
									};
									if ( bFavorite ) {
										aFavoriteList.push(value);
									} else {
										aGeneralList.push(value);
									}
								});
								scope.applicationList = aFavoriteList.concat(aGeneralList);
							});
						} else {
							scope.favoriteCount = 0;
							angular.forEach(applicationOriginalData, function (oValue) {
								aGeneralList.push({
									text: oValue.applicationName + cfg.AT + oValue.serviceType,
									value: oValue.applicationName + cfg.AT + oValue.code,
									code: oValue.code
								});
							});
							scope.applicationList = aGeneralList;
						}
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
						if ( bUseFavorite ) {
							$timeout(function() {
								parseApplicationList();
								$application.off("select2:select").select2("destroy");
								initSelect2();
								$application.val(scope.selectedApplication).trigger("change");
							});
						}
					});
				}
			};
		}
	]);
})( jQuery );
