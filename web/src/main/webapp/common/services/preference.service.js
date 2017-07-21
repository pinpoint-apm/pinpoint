(function( $ ) {
	"use strict";
	pinpointApp.constant("PreferenceServiceConfig", {
		cst: {
			periodType: {
				"RANGE": "range",
				"LAST": "last",
				"REALTIME": "realtime"
			},
			inspectorPeriodTime: [ "5m", "20m", "1h", "3h", "6h", "12h", "1d", "2d", "7d" ],
			periodTime: [ "5m", "20m", "1h", "3h", "6h", "12h", "1d", "2d"],
			depthList: [ 1, 2, 3, 4],
			maxPeriod: 2,
			inspectorMaxPeriod: 7,
			realtimeScatterPeriod: 5 * 60 * 1000,//5m
			responseType: [ "1s", "3s", "5s", "Slow", "Error" ],
			responseTypeColor: [ "#2ca02c", "#3c81fa", "#f8c731", "#f69124", "#f53034" ],
			agentAllStr: "All",
			updateTimes: [{
				time: 10,
				label: "10 seconds"
			},{
				time: 20,
				label: "20 seconds"
			}, {
				time: 30,
				label: "30 seconds"
			}, {
				time: 60,
				label: "1 minute"
			}],
			iconPath: "images/icons/"
		}
	});
	
	pinpointApp.service( "PreferenceService", [ "PreferenceServiceConfig", "$route", "webStorage", "UserConfigurationService", function( cfg, $route, webStorage, UserConfigService ) {
		function isInspector() {
			return $route.current.loadedTemplateUrl.indexOf("/inspector") !== -1;
		}
		this.getDepthList = function() {
			return cfg.cst.depthList;
		};
		this.getPeriodTime = function() {
			return isInspector() ? cfg.cst.inspectorPeriodTime : cfg.cst.periodTime;
		};
		this.getPeriodType = function() {
			return cfg.cst.periodType;
		};
		this.getMaxPeriod = function() {
			return isInspector() ? cfg.cst.inspectorMaxPeriod: cfg.cst.maxPeriod;
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
		this.getUpdateTimes = function() {
			return cfg.cst.updateTimes;
		};
		this.getResponseTypeFormat = function() {
			var o = {};
			$.each( cfg.cst.responseType, function( index, value ) {
				o[value] = 0;
			});
			return o;
		};
		this.getCalleeByApp = function(app) {
			if ( angular.isUndefined( app ) ) {
				return UserConfigService.getCallee();
			} else {
				return webStorage.get( app + "+callee" ) || UserConfigService.getCallee();
			}
		};
		this.getCallerByApp = function(app) {
			if ( angular.isUndefined( app ) ) {
				return UserConfigService.getCaller();
			} else {
				return webStorage.get(app + "+caller") || UserConfigService.getCaller();
			}
		};
		this.getBidirectionalByApp = function(app) {
			if ( angular.isUndefined( app ) ) {
				return UserConfigService.getBidirectional();
			} else {
				return webStorage.get(app + "+bidirectional") || UserConfigService.getBidirectional();
			}
		};
		this.getWasOnlyByApp = function(app) {
			if ( angular.isUndefined( app ) ) {
				return UserConfigService.getWasOnly();
			} else {
				return webStorage.get(app + "+wasOnly") || UserConfigService.getWasOnly();
			}
		};
		this.setDepthByApp = function( app, depth ) {
			if (angular.isUndefined(app) || app === null || angular.isUndefined(depth) || depth === null) {
				return;
			}
			webStorage.add(app, depth);
		};
		this.getIconPath = function() {
			return cfg.cst.iconPath;
		};
	}]);
})( jQuery );