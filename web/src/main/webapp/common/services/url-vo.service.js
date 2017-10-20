(function() {
	'use strict';

	 pinpointApp.service( "UrlVoService", [ "$location", "$routeParams", "PreferenceService", "UserConfigurationService", "CommonUtilService", function( $location, $routeParams, PreferenceService, UserConfigService, CommonUtilService ) {
		 var application = "";			// applicationName@serviceType 		- common
		 var periodType = "";			// last or range or realtime		- common
		 var filter = "";				// #/filteredMap
		 var hint = "";					// #/filteredMap
		 var agentId = "";				// #/Inspector
		 var minutePeriod = 5;			// 기본 5분
		 var millisecondPeriod = -1;	// queryPeriod ( url에서 readablePeriod 시간을 밀리초로 환산한 값 )
		 var queryStartTime = -1;		// ??
		 var readablePeriod = "";		// ??
		 var queryEndDateTime = "";		// str type
		 var queryEndTime = -1;			// milli-second type
		 var transactionInfo = "";		// #/transactionList
		 var agentList = "";			// #/scatterFullScreenMode

		 var callee = UserConfigService.getCallee();
		 var caller = UserConfigService.getCaller();
		 var bidirectional = UserConfigService.getBidirectional();
		 var wasOnly = UserConfigService.getWasOnly();
		 var oPeriodType = PreferenceService.getPeriodType();
		 var aPeriodTime = PreferenceService.getPeriodTime();

		 this.initUrlVo = function( pageName, $routeParams ) {
			 switch( pageName ) {
				 case "main":
					 if ( $routeParams.readablePeriod === oPeriodType.REALTIME ) {
						 this.setApplication( $routeParams.application )
							 .setReadablePeriod( $routeParams.readablePeriod );
					 } else {
						 this.setApplication( $routeParams.application )
							 .setReadablePeriod( $routeParams.readablePeriod )
							 .setQueryEndDateTime( $routeParams.queryEndDateTime )
							 .setCallee( PreferenceService.getCalleeByApp($routeParams.application) )
							 .setCaller( PreferenceService.getCallerByApp($routeParams.application) )
					   		 .setBidirectional( PreferenceService.getBidirectionalByApp($routeParams.application) );
					 }
					 break;
				 case "filteredMap":
					 this.setApplication( $routeParams.application )
					 	.setReadablePeriod( $routeParams.readablePeriod )
					 	.setQueryEndDateTime( $routeParams.queryEndDateTime )
					 	.setFilter( $routeParams.filter )
					 	.setHint( $routeParams.hint );
					 break;
				 case "inspector":
					 this.setApplication( $routeParams.application )
						 .setReadablePeriod( $routeParams.readablePeriod )
						 .setQueryEndDateTime( $routeParams.queryEndDateTime )
						 .setAgentId( $routeParams.agentId );
					 break;
				 case "transactionList":
					 this.setApplication( $routeParams.application )
						 .setReadablePeriod( $routeParams.readablePeriod )
						 .setQueryEndDateTime( $routeParams.queryEndDateTime )
					 	 .setTransactionInfo( $routeParams.transactionInfo );
					 break;
				 case "scatterFullScreenMode":
					 this.setApplication( $routeParams.application )
						 .setReadablePeriod( $routeParams.readablePeriod )
						 .setQueryEndDateTime( $routeParams.queryEndDateTime )
					 	 .setAgentList( $routeParams.agentList );
					 break;
			 }
			 return this;
		 };
		 // applicationName@serviceType
		 this.getApplication = function() {
			 return application;
		 };
		 this.setApplication = function( app ) {
			 application = angular.isString( app ) && app.indexOf( "@" ) > 0 ? app : application;
			 return this;
		 };
		 this.getApplicationName = function() {
			 return application.split("@")[0];
		 };
		 this.getServiceType = function() {
			 return application.split("@")[1];
		 };
		 this.getCallee = function() {
			 return callee;
		 };
		 this.setCallee = function( c ) {
			 callee = c;
			 return this;
		 };
		 this.getCaller = function() {
			 return caller;
		 };
		 this.setCaller = function( c ) {
			 caller = c;
			 return this;
		 };
		 this.getBidirectional = function() {
		 	return bidirectional;
		 };
		 this.setBidirectional = function( c ) {
		 	bidirectional = c;
		 	return this;
		 };
		 this.setWasOnly = function( c ) {
		 	wasOnly = c;
		 	return this;
		 }
		 // 검색 시간 범위
		 this.getPeriod = function() {
			 return minutePeriod;
		 };
		 this.getQueryEndTime = function() {
			 return queryEndTime;
		 };
		 this.setQueryEndTime = function( time ) {
			 queryEndTime = angular.isNumber(time) && time > 0 ? time : queryEndTime;
			 queryEndDateTime = CommonUtilService.formatDate( time );
			 return this;
		 };
		 this.getQueryStartTime = function() {
			 return queryStartTime;
		 };
		 //@REMOVE will
		 this.setQueryStartTime = function( time ) {
			 queryStartTime = time;
			 return this;
		 };
		 this.getFilter = function() {
			return filter;
		 };
		 this.setFilter = function( f ) {
			 filter = angular.isString( f ) ? f : filter;
			 return this;
		 };
		 this.getFilterAsJson = function() {
			 return JSON.parse( filter );
		 };
		 this.getHint = function() {
			 return hint;
		 };
		 this.setHint = function( h ) {
			 hint = angular.isString( h ) ? h : hint;
			 return this;
		 };
		 this.getAgentId = function() {
			 return agentId;
		 };
		 this.setAgentId = function( id ) {
			 agentId = id;
			 return this;
		 };
		 this.getPeriodType = function( p ) {
			 return periodType;
		 };
		 this.setPeriodType = function( p ) {
			 periodType = p;
			 if ( periodType === oPeriodType.REALTIME ) {
				 minutePeriod = parseInt( PreferenceService.getRealtimeScatterXRangeStr() );
				 millisecondPeriod = minutePeriod * 60 * 1000;
			 }
			 return this;
		 };
		 this.setTransactionInfo = function( t ) {
			 transactionInfo = angular.isString( t ) ? t : transactionInfo;
			 return this;
		 };
		 this.getTransactionInfo = function() {
			 return transactionInfo;
		 };
		 this.setAgentList = function( al ) {
			 agentList = angular.isString( al ) ? al.split(",") : agentList;
			 return this;
		 };
		 this.getAgentList = function() {
			 return agentList;
		 };
		 this.isRealtime = function() {
			 return periodType === oPeriodType.REALTIME;
		 };
		 this.setReadablePeriod = function( periodStr ) {
			 if ( periodStr === oPeriodType.REALTIME ) {
				 periodType = oPeriodType.REALTIME;
				 readablePeriod = "5m";
				 minutePeriod = 5;
				 millisecondPeriod = 5 * 60 * 1000;
			 } else {
				 var regex = /^(\d)+(s|m|h|d|w|M|y)$/;
				 var match = regex.exec(periodStr);
				 if (match) {
					 readablePeriod = periodStr;
					 millisecondPeriod = parseInt( periodStr, 10 ) * getTimeByStr( match[2] ) * 1000;
					 if ( aPeriodTime.indexOf( periodStr ) > -1 ) {
						 periodType = oPeriodType.LAST;
					 } else {
						 periodType = oPeriodType.RANGE;
					 }
				 }
			 }
			 return this;
		 };
		 this.getReadablePeriod = function() {
			 return readablePeriod;
		 };
		 this.setQueryEndDateTime = function( timeStr ) {
			 var regex = /^(19[7-9][0-9]|20\d{2})-(0[0-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])-(0[0-9]|1[0-9]|2[0-3])-([0-5][0-9])-([0-5][0-9])$/;
			 if ( regex.test( timeStr ) ) {
				 queryEndDateTime = timeStr;
				 this.setQueryEndTime( parseQueryEndDateTimeToTimestamp( timeStr ) );
			 }
			 return this;
		 };
		 this.getQueryEndDateTime = function() {
			 return queryEndDateTime;
		 };
		 this.autoCalcultateByQueryStartTimeAndQueryEndTime = function () {
			 millisecondPeriod = queryEndTime - queryStartTime;
			 minutePeriod = millisecondPeriod / 1000 / 60;
			 readablePeriod = minutePeriod + 'm';
			 queryEndDateTime = CommonUtilService.formatDate( queryEndTime );
			 return this;
		 };
		 this.autoCalculateByQueryEndDateTimeAndReadablePeriod = function () {
			 if ( millisecondPeriod === -1 ) {
				 millisecondPeriod = minutePeriod * 1000 * 60;
			 }
			 queryStartTime = queryEndTime - millisecondPeriod;
			 return this;
		 };
		 this.getPartialURL = function( bAddApplication, bAddFilter) {
			 return (bAddApplication ? application + "/" : "" ) + readablePeriod + "/" + queryEndDateTime + ( bAddFilter ? ( filter ? "/" + filter : "" ) : "" );
		 };
		 function parseQueryEndDateTimeToTimestamp( timeStr ) {
			 return CommonUtilService.getMilliSecond( timeStr );
		 }
		 function getTimeByStr( s ) {
			 switch ( s ) {
				 case 'm':
					 return 60;
				 case 'h':
					 return 60 * 60;
				 case 'd':
					 return 60 * 60 * 24;
				 case 'w':
					 return 60 * 60 * 24 * 7;
				 case 'M':
					 return 60 * 60 * 24 * 30;
				 case 'y':
					 return 60 * 60 * 24 * 30 * 12;
				 default:
					 return 1;
			 }
		 }

		 this.initUrlVo( $location.path().split("/")[1], $routeParams );
		 this.autoCalculateByQueryEndDateTimeAndReadablePeriod();
	 }]);
})();