(function( global, $ ) {
	var bSendAllowed = true;
	var $at = function() {};
	if ( typeof ga !== "undefined" && bSendAllowed === true ) {
		$at = function( category, name, label, count, options ) {	
			if ( arguments.length == 1 ) {
				ga( 'send', 'pageview', arguments[0] );
			} else {
				ga( 'send', 'event', category, name, label, count, options );
			}
		};
	} else if ( typeof wcs !== "undefined" ) {
		$at = function( category, name, label ) {		
			if ( arguments.length == 1 ) return;
			if ( typeof label !== "undefined" || typeof label !== "null"  )  {
				name = name + "_" + label;
			}
			wcs.event( category, name );
		};
	}
    $at.MAIN = "Main";
	$at.CONTEXT = "Context";
	$at.CALLSTACK = "CallStack";
	$at.MIXEDVIEW = "MixedView";

	$at.CLK_APPLICATION = "ClickApplication";
	$at.CLK_TIME = "ClickTime";
	$at.CLK_SEARCH_NODE = "ClickSearchNode";
	$at.CLK_CLEAR_SEARCH = "ClickClearSearch";
	$at.CLK_NODE = "ClickNode";
	$at.CLK_LINK = "ClickLink";
	$at.CLK_UPDATE_TIME = "ClickUpdateTime";
	$at.CLK_HELP = "ClickHelp";
	$at.CLK_SCATTER_SETTING = "ClickScatterSetting";
	$at.CLK_DOWNLOAD_SCATTER = "ClickDownloadScatter";
	$at.CLK_RESPONSE_GRAPH = "ClickResponseGraph";
	$at.CLK_LOAD_GRAPH = "ClickLoadGraph";
	$at.CLK_SHOW_GRAPH = "ClickShowGraph";
	$at.CLK_FILTER_TRANSACTION = "ClickFilterTransaction";
	$at.CLK_FILTER_TRANSACTION_WIZARD = "ClickFilterTransactionWizard";
	$at.CLK_MORE = "ClickMore";
	$at.CLK_DISTRIBUTED_CALL_FLOW = "ClickDistributedCallFlow";
	$at.CLK_SERVER_MAP = "ClickServerMap";
	$at.CLK_RPC_TIMELINE = "ClickRPCTimeline";
	$at.CLK_CALL = "ClickCall";
	$at.CLK_TRANSACTION = "ClickTransaction";
	$at.CLK_HEAP = "ClickHeap";
	$at.CLK_PERM_GEN = "ClickPermGen";
	$at.CLK_CPU_LOAD = "ClickCpuLoad";
	$at.CLK_REFRESH = "ClickRefresh";
	$at.CLK_CALLEE_RANGE = "ClickCalleeRange";
	$at.CLK_CALLER_RANGE = "ClickCallerRange";
	
	$at.CLK_CONFIGURATION = "ClickConfiguration";
	$at.CLK_GENERAL = "ClickConfigurationGeneral";
	$at.CLK_ALARM = "ClickConfigurationAlarm";
	$at.CLK_HELP = "ClickConfigurationHelp";
	$at.CLK_ALARM_CREATE_USER_GROUP = "ClickAlarmCreateUserGroup";
	$at.CLK_ALARM_REFRESH_USER_GROUP = "ClickAlarmRefreshUserGroup";
	$at.CLK_ALARM_FILTER_USER_GROUP = "ClickAlarmFilterUserGroup";
	$at.CLK_ALARM_ADD_USER = "ClickAlarmAddUser";
	$at.CLK_ALARM_REFRESH_USER = "ClickAlarmRefreshUser";
	$at.CLK_ALARM_FILTER_USER = "ClickAlarmFilterUser";
	$at.CLK_ALARM_CREATE_PINPOINT_USER = "ClickAlarmCreatePinpointUser";
	$at.CLK_ALARM_REFRESH_PINPOINT_USER = "ClickAlarmRefreshPinpointUser";
	$at.CLK_ALARM_FILTER_PINPOINT_USER = "ClickAlarmFilterPinpointUser";
	$at.CLK_ALARM_CREATE_RULE = "ClickAlarmCreateUserGroup";
	$at.CLK_ALARM_REFRESH_RULE = "ClickAlarmRefreshUserGroup";
	$at.CLK_ALARM_FILTER_RULE = "ClickAlarmFilterUserGroup";
	

	$at.TG_DATE = "ToggleDate";
	$at.TG_UPDATE_ON = "ToggleUpdateOn";
	$at.TG_NODE_VIEW = "ToggleNodeView";
	$at.TG_SCATTER_SUCCESS = "ToggleScatterSuccess";
	$at.TG_SCATTER_FAILED = "ToggleScatterFailed";
	$at.TG_MERGE_TYPE = "ToggleMergeType";
	$at.TG_CALL_COUNT = "ToggleCallCount";
	$at.TG_TPS = "ToggleTPS";
	$at.TG_ROUTING = "ToggleRouting";
	$at.TG_CURVE = "ToggleCurve";

	$at.ST_ = "Sort";
	
	$at.ASCENDING = "ascending";
	$at.DESCENDING = "descending";

	$at.ON = "on";
	$at.OFF = "off";
	
	$at.MAIN_PAGE = "/main.page";
	$at.FILTEREDMAP_PAGE = "/filteredMap.page";
	$at.INSPECTOR_PAGE = "/inspector.page";
	$at.SCATTER_FULL_SCREEN_PAGE = "/scatterFullScreen.page";
	$at.TRANSACTION_DETAIL_PAGE = "/transactionDetail.page";
	$at.TRANSACTION_LIST_PAGE = "/transactionList.page";
	$at.TRANSACTION_VIEW_PAGE = "/transactionView.page";
	
	
	global.$at = $at;
	
	$.ajax({
		url: "/configuration.pinpoint"
	}).done(function( result ) {
		bSendAllowed = result.sendUsage;
	}).fail(function() {
		
	});
})(window, jQuery);