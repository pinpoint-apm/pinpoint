(function() {
	'use strict';

	pinpointApp.service( "CommonUtilService", [ function()  {
		this.getRandomNum = function() {
			return Math.round( Math.random() * 10000 );
		};
		this.formatDate = function( time, format ) {
			return moment( time ).format( format || "YYYY-MM-DD-HH-mm-ss" );
		};
		this.isEmpty = function( target ) {
			return angular.isUndefined( target ) || target === null || target === "";
		};
		this.random = function( start, end ) {
			return Math.floor( Math.random() * ( end - start + 1 ) ) + start;
		}
	}]);
})();