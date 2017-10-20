(function($) {
	"use strict";

	/**
	 * (en) AnalyticsService
	 * @ko AnalyticsService
	 * @group Service
	 * @name AnalyticsService
	 * @class
	 */

	pinpointApp.service( "AnalyticsService", [ "SystemConfigurationService", function ( SystemConfigService ) {

		this.send = function( category, name, label, count, options ) {
			if ( typeof ga !== "undefined" ) {
				if (SystemConfigService.get("sendUsage") !== true) return;
				if (arguments.length == 1) {
					ga("send", "pageview", arguments[0]);
				} else {
					ga("send", "event", category, name, label, count, options);
				}
			}
		};
		this.sendMain = function( name, label, count, options ) {
			this.send( this.CONST.MAIN, name, label, count, options );
		};

		this.CONST = {};
		this.CONST.MAIN = "Main";
		this.CONST.CONTEXT = "Context";
		this.CONST.CALLSTACK = "CallStack";
		this.CONST.MIXEDVIEW = "MixedView";
		this.CONST.INSPECTOR = "Inspector";
		this.CONST.REALTIME = "RealTime";

		this.CONST.VERSION = "Version";
		this.CONST.CLK_APPLICATION = "ClickApplication";
		this.CONST.CLK_TIME = "ClickTime";
		this.CONST.CLK_SEARCH_NODE = "ClickSearchNode";
		this.CONST.CLK_CLEAR_SEARCH = "ClickClearSearch";
		this.CONST.CLK_NODE = "ClickNode";
		this.CONST.CLK_LINK = "ClickLink";
		this.CONST.CLK_NODE_IN_GROUPED_VIEW = "ClickNodeInGroupedView";
		this.CONST.CLK_LINK_IN_GROUPED_VIEW = "ClickLinkInGroupedView";
		this.CONST.CLK_SHOW_GROUPED_NODE_VIEW = "ClickShowGroupedNodeView";
		this.CONST.CLK_SHOW_GROUPED_LINK_VIEW = "ClickShowGroupedLinkView";
		this.CONST.CLK_UPDATE_TIME = "ClickUpdateTime";
		this.CONST.CLK_HELP = "ClickHelp";
		this.CONST.CLK_SCATTER_SETTING = "ClickScatterSetting";
		this.CONST.CLK_DOWNLOAD_SCATTER = "ClickDownloadScatter";
		this.CONST.CLK_RESPONSE_GRAPH = "ClickResponseGraph";
		this.CONST.CLK_LOAD_GRAPH = "ClickLoadGraph";
		this.CONST.CLK_SHOW_GRAPH = "ClickShowGraph";
		this.CONST.CLK_SHOW_SERVER_LIST = "ClickShowServerList";
		this.CONST.CLK_OPEN_INSPECTOR = "ClickOpenInspector";
		this.CONST.CLK_FILTER_TRANSACTION = "ClickFilterTransaction";
		this.CONST.CLK_FILTER_TRANSACTION_WIZARD = "ClickFilterTransactionWizard";
		this.CONST.CLK_MORE = "ClickMore";
		this.CONST.CLK_DISTRIBUTED_CALL_FLOW = "ClickDistributedCallFlow";
		this.CONST.CLK_SERVER_MAP = "ClickServerMap";
		this.CONST.CLK_RPC_TIMELINE = "ClickRPCTimeline";
		this.CONST.CLK_CALL = "ClickCall";
		this.CONST.CLK_TRANSACTION = "ClickTransaction";
		this.CONST.CLK_HEAP = "ClickHeap";
		this.CONST.CLK_PERM_GEN = "ClickPermGen";
		this.CONST.CLK_CPU_LOAD = "ClickCpuLoad";
		this.CONST.CLK_REFRESH = "ClickRefresh";
		this.CONST.CLK_CALLEE_RANGE = "ClickCalleeRange";
		this.CONST.CLK_CALLER_RANGE = "ClickCallerRange";
		this.CONST.CLK_REALTIME_CHART_HIDE = "ClickRealtimeChartHide";
		this.CONST.CLK_REALTIME_CHART_SHOW = "ClickRealtimeChartShow";
		this.CONST.CLK_REALTIME_CHART_PIN_ON = "ClickRealtimeChartPinOn";
		this.CONST.CLK_REALTIME_CHART_PIN_OFF = "ClickRealtimeChartPinOff";
		this.CONST.CLK_SHOW_SERVER_TYPE_DETAIL = "ClickShowServerTypeDetail";
		this.CONST.CLK_CHANGE_AGENT_INSPECTOR = "ClickChangeAgentInspector";
		this.CONST.CLK_CHANGE_AGENT_MAIN = "ClickChangeAgentMain";
		this.CONST.CLK_START_REALTIME = "ClickStartRealtime";
		this.CONST.CLK_OPEN_THREAD_DUMP_LAYER = "ClickOpenThreadDumpLayer";

		this.CONST.CLK_CONFIGURATION = "ClickConfiguration";
		this.CONST.CLK_GENERAL = "ClickConfigurationGeneral";
		this.CONST.CLK_ALARM = "ClickConfigurationAlarm";
		this.CONST.CLK_HELP = "ClickConfigurationHelp";
		this.CONST.CLK_GENERAL_SET_DEPTH = "ClickGeneralSetDepth";
		this.CONST.CLK_GENERAL_SET_PERIOD = "ClickGeneralSetPeriod";
		this.CONST.CLK_GENERAL_SET_FAVORITE = "ClickGeneralSetFavorite";
		this.CONST.CLK_GENERAL_SET_TIMEZONE = "ClickGeneralSetTimezone";
		this.CONST.CLK_ALARM_CREATE_USER_GROUP = "ClickAlarmCreateUserGroup";
		this.CONST.CLK_ALARM_REFRESH_USER_GROUP = "ClickAlarmRefreshUserGroup";
		this.CONST.CLK_ALARM_FILTER_USER_GROUP = "ClickAlarmFilterUserGroup";
		this.CONST.CLK_ALARM_ADD_USER = "ClickAlarmAddUser";
		this.CONST.CLK_ALARM_REFRESH_USER = "ClickAlarmRefreshUser";
		this.CONST.CLK_ALARM_FILTER_USER = "ClickAlarmFilterUser";
		this.CONST.CLK_ALARM_CREATE_PINPOINT_USER = "ClickAlarmCreatePinpointUser";
		this.CONST.CLK_ALARM_REFRESH_PINPOINT_USER = "ClickAlarmRefreshPinpointUser";
		this.CONST.CLK_ALARM_FILTER_PINPOINT_USER = "ClickAlarmFilterPinpointUser";
		this.CONST.CLK_ALARM_CREATE_RULE = "ClickAlarmCreateUserGroup";
		this.CONST.CLK_ALARM_REFRESH_RULE = "ClickAlarmRefreshUserGroup";
		this.CONST.CLK_ALARM_FILTER_RULE = "ClickAlarmFilterUserGroup";


		this.CONST.TG_DATE = "ToggleDate";
		this.CONST.TG_UPDATE_ON = "ToggleUpdateOn";
		this.CONST.TG_UPDATE_OFF = "ToggleUpdateOff";
		this.CONST.TG_NODE_VIEW = "ToggleNodeView";
		this.CONST.TG_SCATTER_SUCCESS = "ToggleScatterSuccess";
		this.CONST.TG_SCATTER_FAILED = "ToggleScatterFailed";
		this.CONST.TG_MERGE_TYPE = "ToggleMergeType";
		this.CONST.TG_CALL_COUNT = "ToggleCallCount";
		this.CONST.TG_TPS = "ToggleTPS";
		this.CONST.TG_ROUTING = "ToggleRouting";
		this.CONST.TG_CURVE = "ToggleCurve";
		this.CONST.TG_REALTIME_CHART_RESIZE = "ToggleRealtimeChartResize";

		this.CONST.ST_ = "Sort";

		this.CONST.ASCENDING = "ascending";
		this.CONST.DESCENDING = "descending";

		this.CONST.ON = "on";
		this.CONST.OFF = "off";

		this.CONST.MAIN_PAGE = "/main.page";
		this.CONST.FILTEREDMAP_PAGE = "/filteredMap.page";
		this.CONST.INSPECTOR_PAGE = "/inspector.page";
		this.CONST.SCATTER_FULL_SCREEN_PAGE = "/scatterFullScreen.page";
		this.CONST.TRANSACTION_DETAIL_PAGE = "/transactionDetail.page";
		this.CONST.TRANSACTION_LIST_PAGE = "/transactionList.page";
		this.CONST.TRANSACTION_VIEW_PAGE = "/transactionView.page";
	}]);
})(jQuery);