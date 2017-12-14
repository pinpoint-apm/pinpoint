(function( $ ) {
	"use strict";
	pinpointApp.constant("UserConfigurationServiceConfig", {
		"URL": "userConfiguration.pinpoint",
		"FAVORITE_LIST_LOCAL_KEY": "preference.favorite",
		"MAX_FAVORITE": 5000,
		"TIME_ZONE_LOCAL_KEY": "pinpoint-timezone",
		"PERIOD_LOCAL_KEY": "period",
		"PERIOD_DEFAULT": "5m",
		"CALLER_LOCAL_KEY": "caller",
		"CALLER_DEFAULT": 1,
		"CALLEE_LOCAL_KEY": "callee",
		"CALLEE_DEFAULT": 1,
		"BIDIRECTIONAL_LOCAL_KEY": "bidirectional",
		"BIDIRECTIONAL_DEFAULT": false,
		"WAS_ONLY_LOCAL_KEY": "wasOnly",
		"WAS_ONLY_DEFAULT": false
	});

	pinpointApp.service( "UserConfigurationService", [ "UserConfigurationServiceConfig", "$http", "webStorage", function( cfg, $http, webStorage ) {
		var aFavoriteList = [];

		this.addFavorite = function( applicationName, appCode, cb ) {
			var oResult = checkInclude( applicationName );
			if ( aFavoriteList.length >= cfg.MAX_FAVORITE || oResult.include ) {
				return;
			}
			aFavoriteList.push({
				"applicationName": oResult.name,
				"serviceType": oResult.type,
				"code": appCode || 0
			});
			setFavoriteList( cb );
		};
		function checkInclude( appName, appType ) {
			var oReturn = {
				include: false,
				index: -1
			};
			if ( arguments.length === 1 ) {
				var aValue = appName.split("@");
				oReturn.name = aValue[0];
				oReturn.type = aValue[1];
			} else {
				oReturn.name = appName;
				oReturn.type = appType;
			}
			for( var i = 0 ; i < aFavoriteList.length ; i++ ) {
				if ( aFavoriteList[i].applicationName === oReturn.name && aFavoriteList[i].serviceType === oReturn.type ) {
					oReturn.included = true;
					oReturn.index = i;
					break;
				}
			}
			return oReturn;
		}
		this.removeFavorite = function( appName, appType, cb ) {
			var oResult = checkInclude( appName, appType );
			if ( oResult.include ) return;
			aFavoriteList.splice( oResult.index, 1 );
			setFavoriteList( cb );
		};
		function setFavoriteList( cb ) {
			webStorage.add(cfg.FAVORITE_LIST_LOCAL_KEY, JSON.stringify(aFavoriteList) );
			cb();
		}
		this.getFavoriteList = function( cb ) {
			cb( aFavoriteList );
		};
		this.getPeriod = function() {
			return webStorage.get( cfg.PERIOD_LOCAL_KEY ) || cfg.PERIOD_DEFAULT;
		};
		this.setPeriod = function( v ) {
			webStorage.add( cfg.PERIOD_LOCAL_KEY, v );
		};
		this.getCaller = function() {
			return webStorage.get( cfg.CALLER_LOCAL_KEY ) || cfg.CALLER_DEFAULT;
		};
		this.setCaller = function( c ) {
			webStorage.add( cfg.CALLER_LOCAL_KEY, c );
		};
		this.getCallee = function() {
			return webStorage.get( cfg.CALLEE_LOCAL_KEY ) || cfg.CALLEE_DEFAULT;
		};
		this.setCallee = function( c ) {
			webStorage.add( cfg.CALLEE_LOCAL_KEY, c );
		};
		this.getBidirectional = function() {
			return webStorage.get( cfg.BIDIRECTIONAL_LOCAL_KEY ) || cfg.BIDIRECTIONAL_DEFAULT;
		};
		this.setBidirectional = function( c ) {
			webStorage.add( cfg.BIDIRECTIONAL_LOCAL_KEY, c );
		};
		this.getWasOnly = function() {
			return webStorage.get( cfg.WAS_ONLY_LOCAL_KEY ) || cfg.WAS_ONLY_DEFAULT;
		};
		this.setWasOnly = function( c ) {
			webStorage.add( cfg.WAS_ONLY_LOCAL_KEY, c );
		};
		this.getTimezone = function() {
			return webStorage.get( cfg.TIME_ZONE_LOCAL_KEY ) || moment.tz.guess();
		};
		this.setTimezone = function( timezone ) {
			webStorage.add( cfg.TIME_ZONE_LOCAL_KEY, timezone );
		};
		try {
			var aLocalList = JSON.parse(webStorage.get(cfg.FAVORITE_LIST_LOCAL_KEY) || "[]");
			if ( aLocalList.length > 0 && typeof aLocalList[0] === "string" ) {
				for (var i = 0; i < aLocalList.length; i++) {
					var oValue = aLocalList[i].split("@");
					aFavoriteList.push({
						"applicationName": oValue[0],
						"serviceType": oValue[1],
						"code": 0
					});
				}
				setFavoriteList(function(){});
			} else {
				aFavoriteList = aLocalList;
			}
		}catch(e){
			aFavoriteList = [];
			console.log( e );
		}
	}]);
})( jQuery );