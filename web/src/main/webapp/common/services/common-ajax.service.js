(function() {
	'use strict';
	/**
	 * (en)CommonAjaxService
	 * @ko CommonAjaxService
	 * @group Service
	 * @name CommonAjaxService
	 * @class
	 */
	pinpointApp.constant( "CommonAjaxServiceConfig", {
		"serverTimeUrl" : "serverTime.pinpoint",
		"applicationListUrl": "applications.pinpoint",
		"realtimeSummaryNLoadDataUrl": "getResponseTimeHistogramData.pinpoint"
	});
	
	pinpointApp.service( "CommonAjaxService", [ "CommonAjaxServiceConfig", "$http", function( cfg, $http ) {

		this.getSQLBind = function(url, data, cb) {
			//$http({
			//	"url": url,
			//	"method": "POST",
			//	"headers": {
			//		"Content-Type": "application/x-www-form-urlencoded; charset=UTF-8"
			//	},
			//	"data": data
			//}).then(function(result) {
			//	cb( result );
			//}, function( error ) {
			//	cb( error );
			//});
			jQuery.ajax({
				type: 'POST',
				url: url,
				data: data,
				cache: false,
				dataType: 'json',
				success: function (result) {
					if (angular.isFunction(cb)) {
						cb(result);
					}
				},
				error: function (xhr, status, error) {
					if (angular.isFunction(cb)) {
						cb(error);
					}
				}
			});
		};

		this.getServerTime = function( cb ) {
			$http.get( cfg.serverTimeUrl ).success(function ( data ) {
				cb( data.currentServerTime );
			}).error( function () {
				cb( Date.now() );
			});
		};
		var appList = null;
		this.getApplicationList = function( cbSuccess, cbFail ) {
			if ( appList === null ) {
				$http.get( cfg.applicationListUrl ).success(function ( data ) {
					appList = data;
					cbSuccess( data );
				}).error(function () {
					cbFail();
				});
			} else {
				cbSuccess( appList );
			}
		};
		this.getResponseTimeHistogramData = function( oRequestData, cbSuccess, cbFail ) {
			$http( {
				"url": cfg.realtimeSummaryNLoadDataUrl + "?" + getParam( oRequestData ),
				"method": "GET"
			}).then(function ( oResult ) {
				cbSuccess( oResult.data );
			}, function () {
				cbFail();
			});
		};
		function getParam( obj ) {
			var aParam = [];
			for( var p in obj ) {
				aParam.push( p + "=" + obj[p] );
			}
			return aParam.join("&");
		}
	}]);
})();