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
			favorite: "preference.favorite"
		},
		defaults: {
			callee: 1,
			caller: 1,
			period: "5m"
		},
		list: [{
			name: "caller",
			type: "number"
		},{
			name: "callee",
			type: "number"
		},{
			name: "period",
			type: "string"
		}],
		cst: {
			periodTypes: [ '5m', '20m', '1h', '3h', '6h', '12h', '1d', '2d'],
			depthList: [ 1, 2, 3, 4],
			maxFavorite: 5000,
			maxPeriod: 2,
			realtimeScatterPeriod: 5 * 60 * 1000,//5m
			responseType: [ "1s", "3s", "5s", "Slow", "Error" ],
			responseTypeColor: [ "#2ca02c", "#3c81fa", "#f8c731", "#f69124", "#f53034" ],
			agentAllStr: "All"
		}
	});
	
	pinpointApp.service( "PreferenceService", [ "PreferenceServiceConfig", "webStorage", function( cfg, webStorage ) {
		var self = this;
		var oDefault = {};
		var aFavoriteList = [];
		
		loadPreference();
		
		this.addFavorite = function( applicationName ) {
			if ( aFavoriteList.length == cfg.cst.maxFavorite || aFavoriteList.indexOf( applicationName ) !== -1 ) {
				return;
			}
			aFavoriteList.push( applicationName );
			setFavoriteList();
		};
		this.removeFavorite = function( applicationName ) {
			var index = aFavoriteList.indexOf( applicationName ); 
			if ( index === -1 ) return;
			aFavoriteList.splice( index, 1 );
			setFavoriteList();
		};
		function setFavoriteList() {
			webStorage.add(cfg.names.favorite, JSON.stringify(aFavoriteList) );
		}
		this.getFavoriteList = function() {
			return aFavoriteList;
		};
		this.getDepthList = function() {
			return cfg.cst.depthList;
		};
		this.getPeriodTypes = function() {
			return cfg.cst.periodTypes;
		};
		this.getMaxPeriod = function() {
			return cfg.cst.maxPeriod;
		};
		this.getRealtimeScatterXRange = function() {
			return cfg.cst.realtimeScatterPeriod;
		};
		this.getRealtimeScatterXRangeStr = function() {
			return (cfg.cst.realtimeScatterPeriod / 1000 / 60) + "m";
		};
		this.getResponseTypeColor = function() {
			return cfg.cst.responseTypeColor;
		};
		this.getAgentAllStr = function() {
			return cfg.cst.agentAllStr;
		};
		this.getResponseTypeFormat = function() {
			var o = {};
			jQuery.each( cfg.cst.responseType, function( index, value ) {
				o[value] = 0;
			});
			return o;
		};
		this.getCalleeByApp = function(app) {
			if ( angular.isUndefined( app ) ) {
				return this.getCallee();
			} else {
				return webStorage.get( app + "+callee" ) || this.getCallee();
			}
		};
		this.getCallerByApp = function(app) {
			if ( angular.isUndefined( app ) ) {
				return this.getCaller();
			} else {
				return webStorage.get(app + "+caller") || this.getCaller();
			}
		};
		this.setDepthByApp = function( app, depth ) {
			if (angular.isUndefined(app) || app === null || angular.isUndefined(depth) || depth === null) {
				return;
			}
			webStorage.add(app, depth);
		};
		
		
		function loadPreference() {
			// set value of webStorage or default
			// and make getter and setter function
			jQuery.each( cfg.list, function( index, value ) {
				var name = value.name;
				oDefault[name] = webStorage.get( name ) || cfg.defaults[name];
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
					webStorage.add(name, v);
					oDefault[name] = v;
				};
			});
			aFavoriteList = webStorage.get(cfg.names.favorite) || [];
		}
		
	}]);
})();