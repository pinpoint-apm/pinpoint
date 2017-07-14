var gulp = require('gulp');
var concat = require('gulp-concat');
var uglify = require('gulp-uglify');
var banner = require('gulp-banner');
var jshint = require('gulp-jshint');
var sourcemaps = require('gulp-sourcemaps');

var COMPONENT_PATH = 'main/webapp/components/';
var TARGET_PATH = 'main/webapp/lib/';
var STYLE_PATH = 'main/webapp/styles/';
var bannerStart = 'pinpoint - ' + new Date() + ' include ';

gulp.task('vendor-css', function() {
	var aSrc = concatPath([
		// need components/bootstrap/fonts...
		'bootstrap/dist/css/bootstrap.min.css',
		'jquery.jslider/css/jslider.css',
		// need components/jquery.jslider/img/jslider.round.plastic.png, jslider.blue.png, slider.plastic.png, jslider.png, jslider.round.png
		'jquery.jslider/css/jslider.round.plastic.css',
		// need components/jquery-ui/themes/smoothness/images/files...
		'jquery-ui/themes/smoothness/jquery-ui.css',
		// need components/select2/select2.png, select2-spinner.gif, select2x2.png
		'select2/select2.css',
		'slickgrid/slick.grid.css',
		'jquery-timepicker-addon/jquery-ui-timepicker-addon.css',
		'jquery.layout/dist/layout-default-latest.css',
		'dragToSelect/jquery.dragToSelect.css',
		'angular-slider/angular-slider.min.css',
		'tooltipster/css/tooltipster.css',
		'google-code-prettify/prettify.css',
		'google-code-prettify/sunburst.css'
	], COMPONENT_PATH);

	return gulp.src(aSrc)
		.pipe(concat('vendor-gulp.css'))
		.pipe(uglify())
		.pipe(banner(['/*', bannerStart, getSrcNames( aSrc ), ' */\n'].join('')))
		.pipe(gulp.dest( TARGET_PATH + 'css'));
});
gulp.task('pinpoint-css', function() {
	var aSrc = concatPath([
		'main.css',
		'inspector.css',
		'navbar.css',
		'nodeInfoDetails.css',
		'linkInfoDetails.css',
		'transactionTable.css',
		'callStacks.css',
		'timeSlider.css',
		'agentChartGroup.css',
		'sidebarTitle.css',
		'serverMap.css',
		'filterInformation.css',
		'distributedCallFlow.css',
		'loading.css',
		'jquery.BigScatterChart.css',
		'timer.css',
		'configuration.css',
		'xeicon.min.css'
	], STYLE_PATH);

	return gulp.src(aSrc)
		.pipe(concat('pinpoint-gulp.css'))
		.pipe(uglify())
		.pipe(banner(['/*', bannerStart, getSrcNames( aSrc ), ' */\n'].join('')))
		.pipe(gulp.dest( TARGET_PATH + 'css'));
});

gulp.task('base-js', function() {
	var aSrc = concatPath([
		'jquery/dist/jquery.min.js',
		'jquery-ui/ui/jquery-ui.js',
		'underscore/underscore-min.js'
	], COMPONENT_PATH);

	return gulp.src(aSrc)
	.pipe(concat('base-lib-gulp.min.js'))
	.pipe(uglify())
	.pipe(banner(['/*', bannerStart, getSrcNames( aSrc ), ' */\n'].join('')))
	.pipe(gulp.dest( TARGET_PATH + 'js'));
});
gulp.task('draw-js', function() {
	var aSrc = concatPath([
		'gojs/go.js',
		'snap.svg/snap.svg.min.js',
		'd3/d3.min.js',
		'amcharts/amcharts.js',
		'amcharts/serial.js',
		'amcharts/themes/light.js'
	], COMPONENT_PATH);

	return gulp.src(aSrc)
		.pipe(concat('draw-lib-gulp.min.js'))
		.pipe(uglify())
		.pipe(banner(['/*', bannerStart, getSrcNames( aSrc ), ' */\n'].join('')))
		.pipe(gulp.dest( TARGET_PATH + 'js'));
});
gulp.task('util-js', function() {
	var aSrc = concatPath([
		'bootstrap/dist/js/bootstrap.min.js',
		'google-code-prettify/prettify.js',
		'google-code-prettify/lang-sql.js',
		'moment/moment.js',
		'select2/select2.min.js',
		'select2/select2_locale_ko.js',
		'jquery-class/jquery.Class.js',
		'jquery.layout/dist/jquery.layout-latest.js',
		'dragToSelect/jquery.dragToSelect.js',
		'jquery-timepicker-addon/jquery-ui-timepicker-addon.js',
		'jquery.jslider/js/draggable-0.1.js',
		'jquery.jslider/js/jshashtable-2.1_src.js',
		'jquery.jslider/js/jquery.dependClass-0.1.js',
		'jquery.jslider/js/jquery.numberformatter-1.2.3.js',
		'jquery.jslider/js/tmpl.js',
		'jquery.jslider/js/jquery.jslider.js',
		'jquery-ui-tabs-paging/js/ui.tabs.paging.js',
		'slickgrid/lib/jquery.event.drag-2.2.js',
		'slickgrid/slick.core.js',
		'slickgrid/slick.formatters.js',
		'slickgrid/slick.editors.js',
		'slickgrid/slick.grid.js',
		'slickgrid/slick.dataview.js',
		'slickgrid/plugins/slick.rowselectionmodel.js',
		'tooltipster/js/jquery.tooltipster.min.js',
		'handlebars/handlebars.min.js',
		'bootstrap/js/bootstrap-modal.js',
		'bootstrap/js/bootstrap-tooltip.js',
		'bootstrap/js/bootstrap-popover.js'
	], COMPONENT_PATH);

	return gulp.src(aSrc)
		.pipe(concat('util-lib-gulp.min.js'))
		.pipe(uglify())
		.pipe(banner(['/*', bannerStart, getSrcNames( aSrc ), ' */\n'].join('')))
		.pipe(gulp.dest( TARGET_PATH + 'js'));
});
gulp.task('angular-js', function() {
	var aSrc = concatPath([
		'angular/angular.min.js',
		'angular-resource/angular-resource.min.js',
		'angular-cookies/angular-cookies.min.js',
		'angular-webstorage/angular-webstorage.js',
		'angular-strap/dist/angular-strap.min.js',
		'angular-strap/dist/angular-strap.tpl.min.js',
		'angular-animate/angular-animate.min.js',
		'angular-route/angular-route.min.js',
		'angular-sanitize/angular-sanitize.min.js',
		'angular-slider/angular-slider.min.js',
		'angular-base64/angular-base64.min.js',
		'angular-timer/dist/angular-timer.min.js'
	], COMPONENT_PATH);

	return gulp.src(aSrc)
		.pipe(concat('angular-lib-gulp.min.js'))
		.pipe(uglify())
		.pipe(banner(['/*', bannerStart, getSrcNames( aSrc ), ' */\n'].join('')))
		.pipe(gulp.dest( TARGET_PATH + 'js'));
});

gulp.task('time-slider-js', function() {
	var aSrc = concatPath([
		'time-slider/time-slider.js',
		'time-slider/time-slider.background.js',
		'time-slider/time-slider.configuration.js',
		'time-slider/time-slider.event-data.js',
		'time-slider/time-slider.events.js',
		'time-slider/time-slider.handler.js',
		'time-slider/time-slider.loading-indicator.js',
		'time-slider/time-slider.position-manager.js',
		'time-slider/time-slider.selection-manager.js',
		'time-slider/time-slider.selection-point.js',
		'time-slider/time-slider.selection-zone.js',
		'time-slider/time-slider.state-line.js',
		'time-slider/time-slider.time-series-signboard.js',
		'time-slider/time-slider.time-signboard.js',
		'time-slider/time-slider.x-axis.js'
	], COMPONENT_PATH);

	return gulp.src(aSrc)
		.pipe(sourcemaps.init())
		.pipe(concat('time-slider-gulp.min.js'))
		.pipe(uglify())
		.pipe(banner(['/*', bannerStart, getSrcNames( aSrc ), ' */\n'].join('')))
		.pipe(sourcemaps.write())
		.pipe(gulp.dest( TARGET_PATH + 'js'));
});

gulp.task('big-scatter2-js', function() {
	var aSrc = concatPath([
		'big-scatter-chart/BigScatterChart2.js',
		'big-scatter-chart/BigScatterChart2.DataBlock.js',
		'big-scatter-chart/BigScatterChart2.DragManager.js',
		'big-scatter-chart/BigScatterChart2.DataLoadManager.js',
		'big-scatter-chart/BigScatterChart2.RendererManager.js',
		'big-scatter-chart/BigScatterChart2.BubbleTypeManager.js',
		'big-scatter-chart/BigScatterChart2.SizeCoordinateManager.js',
		'big-scatter-chart/BigScatterChart2.Util.js',
		'big-scatter-chart/BigScatterChart2.HelpPlugin.js',
		'big-scatter-chart/BigScatterChart2.MessagePlugin.js',
		'big-scatter-chart/BigScatterChart2.SettingPlugin.js',
		'big-scatter-chart/BigScatterChart2.DownloadPlugin.js',
		'big-scatter-chart/BigScatterChart2.WideOpenPlugin.js'
	], COMPONENT_PATH);

	return gulp.src(aSrc)
		.pipe(sourcemaps.init())
		.pipe(concat('big-scatter-chart2-gulp.min.js'))
		.pipe(uglify())
		.pipe(banner(['/*', bannerStart, getSrcNames( aSrc ), ' */\n'].join('')))
		.pipe(sourcemaps.write())
		.pipe(gulp.dest( TARGET_PATH + 'js'));
});
gulp.task('infinite-circular-scroll-js', function() {
	var aSrc = concatPath([
		'infinite-circular-scroll/InfiniteCircularScroll.js'
	], COMPONENT_PATH);

	return gulp.src(aSrc)
		.pipe(sourcemaps.init())
		.pipe(concat('infinite-circular-scroll-gulp.min.js'))
		.pipe(uglify())
		.pipe(banner(['/*', bannerStart, getSrcNames( aSrc ), ' */\n'].join('')))
		.pipe(sourcemaps.write())
		.pipe(gulp.dest( TARGET_PATH + 'js'));
});
gulp.task('server-map-js', function() {
	var aSrc = concatPath([
		'server-map2/jquery.ServerMap2.js'
	], COMPONENT_PATH);

	return gulp.src(aSrc)
		.pipe(sourcemaps.init())
		.pipe(concat('server-map2-gulp.min.js'))
		.pipe(uglify())
		.pipe(banner(['/*', bannerStart, getSrcNames( aSrc ), ' */\n'].join('')))
		.pipe(sourcemaps.write())
		.pipe(gulp.dest( TARGET_PATH + 'js'));
});
gulp.task('pinpoint-component-js', function() {
	var aSrc = concatPath([
		'js/time-slider-gulp.min.js',
		'js/big-scatter-chart2-gulp.min.js',
		'js/infinite-circular-scroll-gulp.min.js',
		'js/server-map2-gulp.min.js'
	], TARGET_PATH);

	return gulp.src(aSrc)
		.pipe(sourcemaps.init())
		.pipe(concat('pinpoint-component-gulp.min.js'))
		.pipe(uglify())
		.pipe(banner(['/*', bannerStart, getSrcNames( aSrc ), ' */\n'].join('')))
		.pipe(sourcemaps.write())
		.pipe(gulp.dest( TARGET_PATH + 'js'));
});

gulp.task('pinpoint-js', function() {
	var aSrc = concatPath([
		'common/filters/icon-url.filter.js',
		'common/filters/application-name-to-class-name.filter.js',
		'common/services/time-slider-vo.service.js',
		'common/services/alerts.service.js',
		'common/services/progress-bar.service.js',
		'common/services/navbar-vo.service.js',
		'common/services/transaction-dao.service.js',
		'common/services/location.service.js',
		'common/services/server-map-dao.service.js',
		'common/services/agent-dao.service.js',
		'common/services/sidebar-title-vo.service.js',
		'common/services/filtered-map-util.service.js',
		'common/services/filter.config.js',
		'common/services/server-map-filter-vo.service.js',
		'common/services/alarm-ajax.service.js',
		'common/services/alarm-util.service.js',
		'common/services/alarm-broadcast.service.js',
		'common/services/server-map-hint-vo.service.js',
		'common/services/is-visible.service.js',
		'common/services/user-locales.service.js',
		'common/help/help-content-en.js',
		'common/help/help-content-ko.js',
		'common/help/help-content-template.js',
		'common/services/help-content.service.js',
		'common/services/analytics.service.js',
		'common/services/realtime-websocket.service.js',
		'common/services/preference.service.js',
		'common/services/common-ajax.service.js',
		'common/services/agent-ajax.service.js',
		'common/services/tooltip.service.js',

		'features/navbar/navbar.directive.js',
		'features/serverMap/server-map.directive.js',
		'features/realtimeChart/realtime-chart.directive.js',
		'features/scatter/scatter.directive.js',
		'features/nodeInfoDetails/node-info-details.directive.js',
		'features/linkInfoDetails/link-info-details.directive.js',
		//'features/serverList/server-list.directive.js',
		'features/agentList/agent-list.directive.js',
		'features/agentInfo/agent-info.directive.js',
		'features/timeSlider/time-slider.directive.js',
		'features/transactionTable/transaction-table.directive.js',
		'features/timeline/timeline.directive.js',
		'features/agentChartGroup/agent-chart-group.directive.js',
		'features/sidebar/title/sidebar-title.directive.js',
		'features/sidebar/filter/filter-information.directive.js',
		'features/distributedCallFlow/distributed-call-flow.directive.js',
		'features/responseTimeSummaryChart/response-time-summary-chart.directive.js',
		'features/loadChart/load-chart.directive.js',
		'features/jvmMemoryChart/jvm-memory-chart.directive.js',
		'features/cpuLoadChart/cpu-load-chart.directive.js',
		'features/tpsChart/tps-chart.directive.js',
		'features/loading/loading.directive.js',
		'features/configuration/configuration.controller.js',
		'features/configuration/help/help.controller.js',
		'features/configuration/general/general.controller.js',
		'features/configuration/alarm/alarm-user-group.directive.js',
		'features/configuration/alarm/alarm-group-member.directive.js',
		'features/configuration/alarm/alarm-pinpoint-user.directive.js',
		'features/configuration/alarm/alarm-rule.directive.js',
		'features/realtimeChart/realtime-chart.controller.js',

		'pages/main/main.controller.js',
		'pages/inspector/inspector.controller.js',
		'pages/transactionList/transaction-list.controller.js',
		'pages/transactionDetail/transaction-detail.controller.js',
		'pages/filteredMap/filtered-map.controller.js',
		'pages/transactionView/transaction-view.controller.js',
		'pages/scatterFullScreenMode/scatter-full-screen-mode.controller.js'
	], COMPONENT_PATH);

	return gulp.src(aSrc)
		.pipe(sourcemaps.init())
		.pipe(concat('pinpoint-gulp.min.js'))
		.pipe(uglify())
		.pipe(banner(['/*', bannerStart, getSrcNames( aSrc ), ' */\n'].join('')))
		.pipe(sourcemaps.write())
		.pipe(gulp.dest( TARGET_PATH + 'js'));
});

gulp.task('jshint', function() {
	gulp.src([
		'main/webapp/features/**/*.js',
		'main/webapp/components/**/*.js',
		'main/webapp/pages/**/*.js'
	])
	.pipe(jshint({
		loopfunc: true,
		newcap: false,
		sub: true
	}))
	.pipe(jshint.reporter('default'))
	.on('error', function(error) {
		console.log( String(error) );
	});
});

gulp.task('default', [
	'jshint',
	'vendor-css',
	'pinpoint-css',
	'base-js',
	'draw-js',
	'util-js',
	'angular-js',
	'time-slider-js',
	'big-scatter2-js',
	'infinite-circular-scroll-js',
	'server-map-js',
	'pinpoint-component-js',
	'pinpoint-js'
]);
gulp.task('user-src', [
	'jshint',
	'vendor-css',
	'pinpoint-css',
	'time-slider-js',
	'big-scatter2-js',
	'infinite-circular-scroll-js',
	'server-map-js',
	'pinpoint-component-js',
	'pinpoint-js'
]);

gulp.task('watch', function() {
	gulp.watch( STYLE_PATH + '*.css', ['pinpoint-css']);
	gulp.watch( 'main/webapp/components/time-slider/*.js',[ 'jshint', 'time-slider-js', 'pinpoint-component-js' ] );
	gulp.watch( 'main/webapp/components/big-scatter-chart/*.js',[ 'jshint', 'big-scatter2-js', 'pinpoint-component-js' ] );
	gulp.watch( 'main/webapp/components/infinite-circular-scroll/*.js',[ 'jshint', 'infinite-circular-scroll-js', 'pinpoint-component-js' ] );
	gulp.watch( 'main/webapp/components/server-map2/*.js',[ 'jshint', 'server-map-js', 'pinpoint-component-js' ] );
	gulp.watch( [
		'main/webapp/common/**/*.js',
		'main/webapp/features/**/*.js',
		'main/webapp/pages/**/*.js'
	],[ 'jshint', 'pinpoint-js' ] );
});

function concatPath( a, prefix ) {
	return a.map(function( value ) {
		return prefix + value;
	});
}
function getSrcNames( a ) {
	var aSrcNames = [];
	a.forEach(function( value ) {
		aSrcNames.push( value.substring( value.lastIndexOf('/') + 1 ) );
	});
	return aSrcNames.join(', ');
}