(function() {
	'use strict';
	/**
	 * (en)PreferenceService 
	 * @ko PreferenceService
	 * @group Service
	 * @name PreferenceService
	 * @class
	 */
	pinpointApp.constant('PreferenceServiceConfig', {
		name : {
			depth: "preference.depth"
		},
		DEFAULT_DEPTH: 1
	});
	
	pinpointApp.service('PreferenceService', [ 'PreferenceServiceConfig', function(cfg) {
		var oDefault = {};
		var bAddedFavorite = false;
		var aFavoriteApplicatName = [];
		
		loadPreference();
		
		
		this.setDepth = function( d ) {
			localStorage.setItem(cfg.name.depth, d);
			oDefault.depth = d;
		}
		this.getDepth = function() {
			// @TODO
			return oDefault.depth;
		};
		this.setUsedApplicationName = function( applicationName ) {
			bAdded = true;
			var oFavoriate = JSON.parse( localStorage.getItem("favoriate") || "{}" );
			if ( angular.isDefined( oFavoriate[applicationName] ) ) {
				oFavoriate[applicationName] += 1;
			} else {
				oFavoriate[applicationName] = 1;
			}
			localStorage.setItem("favoriate", JSON.string(oFavoriate) );
		};
		this.getFavoriteApplicationName = function() {
			if ( bAddedFavorite ) {
				// 반환 값 계산 ( 상위 5개 추리기 )
			}
			return aFavoriteApplicationName;

		};
		
		function loadPreference() {
			oDefault.depth = parseInt( localStorage.getItem( cfg.name.depth ) || cfg.DEFAULT_DEPTH );
			//oDefault.favoriate = JSON.parse( localStorage.getItem("favoriate") || "{}" );
		};
		
	}]);
})();