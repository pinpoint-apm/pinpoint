var concatCommonOptions = {
	separator: '\n'
};

module.exports = function( grunt, options ) {
	return {
		'base_lib': {
			options: concatCommonOptions,
				src: options.makePath([
				'/jquery/dist/jquery.min.js',
				'/jquery-ui/ui/jquery-ui.js',
				'/underscore/underscore-min.js'
			], options.RESOURCE_PATH.COMPONENT ),
			dest: options.RESOURCE_PATH.DEST_JS + '/base-lib.js'
		},
		'draw_lib': {
			options: concatCommonOptions,
			src: options.makePath( [
				'/gojs/go.js',
				'/snap.svg/snap.svg.min.js',
				'/d3/d3.min.js',
				'/amcharts/amcharts.js',
				'/amcharts/serial.js',
				'/amcharts/themes/light.js',
				'/chartjs/Chart.min.js'
			], options.RESOURCE_PATH.COMPONENT ),
			dest: options.RESOURCE_PATH.DEST_JS + '/draw-lib.min.js'
		},
		'util_lib': {
			options: concatCommonOptions,
				src: options.makePath( [
				'/bootstrap/dist/js/bootstrap.min.js',
				'/google-code-prettify/prettify.js',
				'/google-code-prettify/lang-sql.js',
				'/moment/moment-with-locales.min.js',
				'/moment/moment-timezone-with-data.min.js',
				'/select2/dist/js/select2.full.min.js',
				'/jquery-class/jquery.Class.js',
				'/jquery.layout/dist/jquery.layout-latest.js',
				'/dragToSelect/jquery.dragToSelect.js',
				'/jquery-timepicker-addon/jquery-ui-timepicker-addon.js',
				'/jquery.jslider/js/draggable-0.1.js',
				'/jquery.jslider/js/jshashtable-2.1_src.js',
				'/jquery.jslider/js/jquery.dependClass-0.1.js',
				'/jquery.jslider/js/jquery.numberformatter-1.2.3.js',
				'/jquery.jslider/js/tmpl.js',
				'/jquery.jslider/js/jquery.jslider.js',
				'/jquery-ui-tabs-paging/js/ui.tabs.paging.js',
				'/slickgrid/lib/jquery.event.drag-2.2.js',
				'/slickgrid/slick.core.js',
				'/slickgrid/slick.formatters.js',
				'/slickgrid/slick.editors.js',
				'/slickgrid/slick.grid.js',
				'/slickgrid/slick.dataview.js',
				'/slickgrid/plugins/slick.rowselectionmodel.js',
				'/tooltipster/js/jquery.tooltipster.min.js',
				'/handlebars/handlebars.min.js',
				'/bootstrap/js/bootstrap-modal.js',
				'/bootstrap/js/bootstrap-tooltip.js',
				'/bootstrap/js/bootstrap-popover.js'
			], options.RESOURCE_PATH.COMPONENT ),
			dest: options.RESOURCE_PATH.DEST_JS + '/util-lib.js'
		},
		'angular_lib': {
			options: concatCommonOptions,
			src: options.makePath( [
				'/angular/angular.min.js',
				'/angular-resource/angular-resource.min.js',
				'/angular-cookies/angular-cookies.min.js',
				'/angular-webstorage/angular-webstorage.js',
				'/angular-strap/dist/angular-strap.min.js',
				'/angular-strap/dist/angular-strap.tpl.min.js',
				'/angular-animate/angular-animate.min.js',
				'/angular-route/angular-route.min.js',
				'/angular-sanitize/angular-sanitize.min.js',
				'/angular-slider/angular-slider.min.js',
				'/angular-base64/angular-base64.min.js',
				'/angular-timer/dist/angular-timer.min.js'
			], options.RESOURCE_PATH.COMPONENT ),
			dest: options.RESOURCE_PATH.DEST_JS + '/angular-lib.min.js'
		},
		'time_slider': {
			options: concatCommonOptions,
			src: options.makePath( [
				'/time-slider.js',
				'/time-slider.background.js',
				'/time-slider.configuration.js',
				'/time-slider.timeline-data.js',
				'/time-slider.events.js',
				'/time-slider.handler.js',
				'/time-slider.loading-indicator.js',
				'/time-slider.position-manager.js',
				'/time-slider.selection-manager.js',
				'/time-slider.selection-point.js',
				'/time-slider.selection-zone.js',
				'/time-slider.state-line.js',
				'/time-slider.time-series-signboard.js',
				'/time-slider.time-signboard.js',
				'/time-slider.x-axis.js'
			], options.RESOURCE_PATH.TIME_SLIDER ),
			dest: options.RESOURCE_PATH.DEST_JS + '/time-slider.js'
		},
		'big_scatter': {
			options: concatCommonOptions,
			src: options.makePath( [
				'/BigScatterChart2.js',
				'/BigScatterChart2.DataBlock.js',
				'/BigScatterChart2.DragManager.js',
				'/BigScatterChart2.DataLoadManager.js',
				'/BigScatterChart2.RendererManager.js',
				'/BigScatterChart2.BubbleTypeManager.js',
				'/BigScatterChart2.SizeCoordinateManager.js',
				'/BigScatterChart2.Util.js',
				'/BigScatterChart2.HelpPlugin.js',
				'/BigScatterChart2.MessagePlugin.js',
				'/BigScatterChart2.SettingPlugin.js',
				'/BigScatterChart2.DownloadPlugin.js',
				'/BigScatterChart2.WideOpenPlugin.js'
			], options.RESOURCE_PATH.BIG_SCATTER ),
			dest: options.RESOURCE_PATH.DEST_JS + '/big-scatter-chart2.js'
		},
		'infinite_scroll': {
			options: concatCommonOptions,
			src: [ options.RESOURCE_PATH.INFINITE_SCROLL + '/InfiniteCircularScroll.js' ],
			dest: options.RESOURCE_PATH.DEST_JS + '/infinite-circular-scroll.js'
		},
		'server_map': {
			options: concatCommonOptions,
			src: [ options.RESOURCE_PATH.SERVER_MAP + '/jquery.ServerMap2.js' ],
			dest: options.RESOURCE_PATH.DEST_JS + '/server-map2.js'
		},
		'pinpoint_component': {
			options: concatCommonOptions,
			src: [
				'<%= concat.time_slider.dest %>',
				'<%= concat.big_scatter.dest %>',
				'<%= concat.infinite_scroll.dest %>',
				'<%= concat.server_map.dest %>'
			],
			dest: options.RESOURCE_PATH.DEST_JS + '/pinpoint-component.js'
		},
			'pinpoint_src': {
			options: concatCommonOptions,
			src: options.makePath( [
				'/common/filters/icon-url.filter.js',
				'/common/filters/application-name-to-class-name.filter.js',
				'/common/services/time-slider-vo.service.js',
				'/common/services/alerts.service.js',
				'/common/services/progress-bar.service.js',
				'/common/services/navbar-vo.service.js',
				'/common/services/url-vo.service.js',
				'/common/services/transaction-dao.service.js',
				'/common/services/location.service.js',
				'/common/services/server-map-dao.service.js',
				'/common/services/agent-dao.service.js',
				'/common/services/sidebar-title-vo.service.js',
				'/common/services/filtered-map-util.service.js',
				'/common/services/filter.config.js',
				'/common/services/server-map-filter-vo.service.js',
				'/common/services/alarm-ajax.service.js',
				'/common/services/alarm-util.service.js',
				'/common/services/server-map-hint-vo.service.js',
				'/common/services/is-visible.service.js',
				'/common/services/user-locales.service.js',
				'/common/help/help-content-en.js',
				'/common/help/help-content-ko.js',
				'/common/help/help-content-template.js',
				'/common/services/help-content.service.js',
				'/common/services/analytics.service.js',
				'/common/services/realtime-websocket.service.js',
				'/common/services/preference.service.js',
				'/common/services/common-ajax.service.js',
				'/common/services/agent-ajax.service.js',
				'/common/services/tooltip.service.js',
				'/common/services/common-util.service.js',
				'/common/services/local-storage-manager.service.js',
				'/common/services/system-configuration.service.js',
				'/common/services/user-configuration.service.js',

				'/features/applicationList/application-list.directive.js',
				'/features/periodSelector/period-selector.directive.js',
				'/features/navbar/navbar.directive.js',
				'/features/navbar2/navbar2.directive.js',
				'/features/serverMap/server-map.directive.js',
				'/features/realtimeChart/realtime-chart.directive.js',
				'/features/scatter/scatter.directive.js',
				'/features/groupedApplicationList/grouped-application-list.directive.js',
				'/features/nodeInfoDetails/node-info-details.directive.js',
				'/features/linkInfoDetails/link-info-details.directive.js',
				'/features/serverList/server-list.directive.js',
				'/features/agentList/agent-list.directive.js',
				'/features/agentInfo/agent-info.directive.js',
				'/features/timeSlider/time-slider.directive.js',
				'/features/transactionTable/transaction-table.directive.js',
				'/features/timeline/timeline.directive.js',
				'/features/agentChartGroup/agent-chart-group.directive.js',
				'/features/sidebar/title/sidebar-title.directive.js',
				'/features/sidebar/filter/filter-information.directive.js',
				'/features/distributedCallFlow/distributed-call-flow.directive.js',
				'/features/responseTimeSummaryChart/response-time-summary-chart.directive.js',
				'/features/loadChart/load-chart.directive.js',
				'/features/jvmMemoryChart/jvm-memory-chart.directive.js',
				'/features/dataSourceChart/data-source-chart.directive.js',
				'/features/cpuLoadChart/cpu-load-chart.directive.js',
				'/features/tpsChart/tps-chart.directive.js',
				'/features/activeTraceChart/active-trace-chart.directive.js',
				'/features/responseTimeChart/response-time-chart.directive.js',
				'/features/dataSourceChart/data-source--chart.directive.js',
				'/features/loading/loading.directive.js',
				'/features/configuration/configuration.directive.js',
				'/features/configuration/general/general.directive.js',
				'/features/configuration/userGroup/user-group-container.directive.js',
				'/features/configuration/userGroup/user-group.directive.js',
				'/features/configuration/userGroup/group-member.directive.js',
				'/features/configuration/userGroup/pinpoint-user.directive.js',
				'/features/configuration/application/application-config.directive.js',
				'/features/configuration/application/application-group.directive.js',
				'/features/configuration/application/alarm-rule.directive.js',
				'/features/configuration/help/help.directive.js',
				'/features/threadDumpInfoLayer/thread-dump-info-layer.directive.js',
				'/features/realtimeChart/realtime-chart.controller.js',
				'/features/statisticChart/statistic-chart.directive.js',
				'/features/applicationStatistic/application-statistic.directive.js',
				'/pages/main/main.controller.js',
				'/pages/inspector/inspector.controller.js',
				'/pages/transactionList/transaction-list.controller.js',
				'/pages/transactionDetail/transaction-detail.controller.js',
				'/pages/filteredMap/filtered-map.controller.js',
				'/pages/transactionView/transaction-view.controller.js',
				'/pages/scatterFullScreenMode/scatter-full-screen-mode.controller.js',
				'/pages/threadDump/thread-dump.controller.js',
				'/pages/realtime/realtime.controller.js'

			], options.RESOURCE_PATH.ROOT ),
			dest: options.RESOURCE_PATH.DEST_JS + '/pinpoint.js'
		},
		'vendor_css': {
			options: concatCommonOptions,
			src: options.makePath( [
				// need components/bootstrap/fonts...
				'/bootstrap/dist/css/bootstrap.min.css',
				'/jquery.jslider/css/jslider.css',
				// need components/jquery.jslider/img/jslider.round.plastic.png, jslider.blue.png, slider.plastic.png, jslider.png, jslider.round.png
				'/jquery.jslider/css/jslider.round.plastic.css',
				// need components/jquery-ui/themes/smoothness/images/files...
				'/jquery-ui/themes/smoothness/jquery-ui.css',
				// need components/select2/select2.png, select2-spinner.gif, select2x2.png
				'/select2/dist/css/select2.min.css',
				'/slickgrid/slick.grid.css',
				'/jquery-timepicker-addon/jquery-ui-timepicker-addon.css',
				'/jquery.layout/dist/layout-default-latest.css',
				'/dragToSelect/jquery.dragToSelect.css',
				'/angular-slider/angular-slider.min.css',
				'/tooltipster/css/tooltipster.css',
				'/google-code-prettify/prettify.css',
				'/google-code-prettify/sunburst.css',
				'/xeicon.min.css'
			], options.RESOURCE_PATH.COMPONENT ),
			dest: options.RESOURCE_PATH.DEST_CSS + '/vendor.css'
		},
		'pinpoint_css': {
			options: concatCommonOptions,
			src: options.makePath( [
				'/main.css',
				'/inspector.css',
				'/navbar.css',
				'/nodeInfoDetails.css',
				'/linkInfoDetails.css',
				'/transactionTable.css',
				'/callStacks.css',
				'/timeSlider.css',
				'/agentChartGroup.css',
				'/sidebarTitle.css',
				'/serverMap.css',
				'/filterInformation.css',
				'/distributedCallFlow.css',
				'/loading.css',
				'/jquery.BigScatterChart.css',
				'/timer.css',
				'/configuration.css'
			], options.RESOURCE_PATH.STYLE ),
			dest: options.RESOURCE_PATH.DEST_CSS + '/pinpoint.css'
		}
	};
};