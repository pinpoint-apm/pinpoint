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
		names: {
			depth: "preference.depth",
			period: "preference.period"
		},
		defaults: {
			depth: 1,
			period: "5m"
		},
		list : [{
			name: "depth",
			type: "number"
		},{
			name: "period",
			type: "string"
		}]
	});
	
	pinpointApp.service('PreferenceService', [ 'PreferenceServiceConfig', function(cfg) {
		var self = this;
		var oDefault = {};
		var bAddedFavorite = false;
		var aFavoriteApplicatName = [];
		
		loadPreference();
		
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
			// set value of localStoraget or default
			// and set getter and setter function
			jQuery.each( cfg.list, function( index, value ) {
				var name = value.name;
				oDefault[name] = localStorage.getItem( cfg.names[name] ) || cfg.defaults[name];
				switch( value.type ) {
					case "number":
						oDefault[name] = parseInt( oDefault[name] );
						break;
				}
				var fnPostfix = name.substring(0, 1).toUpperCase() + name.substring(1);
				self["get" + fnPostfix] = function() {
					return oDefault[name];
				};
				self["set" + fnPostfix] = function(v) {
					localStorage.setItem(name, v);
					oDefault[name] = v;
				};
			});
			//oDefault.favoriate = JSON.parse( localStorage.getItem("favoriate") || "{}" );
		};
		
	}]);
})();