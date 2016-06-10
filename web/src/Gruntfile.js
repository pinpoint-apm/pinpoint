/**
 * http://gruntjs.com/configuring-tasks
 */
module.exports = function (grunt) {
	require('time-grunt')(grunt);
    var path = require('path');

	var RESOURCE_ROOT = 'main/webapp';
	var INDEX_HTML = RESOURCE_ROOT + '/index.html';
	var resourcePath = {
		'root': RESOURCE_ROOT,
		'style' : RESOURCE_ROOT + '/styles',
		'dest_js' : RESOURCE_ROOT + '/lib/js',
		'dest_css' : RESOURCE_ROOT + '/lib/css',
		'component': RESOURCE_ROOT + '/components',
		'time_slider': RESOURCE_ROOT + '/components/time-slider',
		'big_scatter': RESOURCE_ROOT + '/components/big-scatter-chart',
		'infinite_scroll': RESOURCE_ROOT + '/components/infinite-circular-scroll',
		'server_map': RESOURCE_ROOT + '/components/server-map2'
	};

	function makePath( a, prefix) {
		return a.map( function( value ) {
			return prefix + value;
		});
	}

	var concatCommonOptions = {
		separator: '\n'
	};
	var uglifyCommonOptions = {
		preserveComments: false,
		banner: '/*! @preserve <%= pkg.name %> - v<%= pkg.version %> - <%= grunt.template.today("yyyy-mm-dd") %> */'
	};
	var uglifyCommonOptionsWidthSourceMap = {
		preserveComments: false,
		sourceMap: true,
		banner: '/*! @preserve <%= pkg.name %> - v<%= pkg.version %> - <%= grunt.template.today("yyyy-mm-dd") %> */'
	};

    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),

        clean: {
			'vendor_js': [
				'<%= concat.base_lib.dest %>',
				'<%= concat.util_lib.dest %>'
			],
			'pinpoint_component_js': [
				'<%= concat.time_slider.dest %>',
				'<%= concat.big_scatter.dest %>',
				'<%= concat.infinite_scroll.dest %>',
				'<%= concat.server_map.dest %>',
				'<%= concat.pinpoint_component.dest %>',
			],
			'pinpoint_src': [
				'<%= concat.pinpoint_src.dest %>'
			],
			'vendor_css': [
				'<%= concat.vendor_css.dest %>'
			],
			'pinpoint_css': [
				'<%= concat.pinpoint_css.dest %>'
			],
			'minify': [
				resourcePath.dest_js + '/*.min.js'
			],
			'original': [

			]
		},
		"regex-replace": {
			css: {
				src: [ INDEX_HTML ],
				actions: [{
					name: 'vendor.css',
					search: /<!--###vendor\.css[^#]*vendor\.css###-->/gm,
					replace: '<link rel="stylesheet" href="lib/css/vendor.css?v=${buildTime}">'
				},{
					name: 'pinpoint.css',
					search: /<!--###pinpoint\.css[^#]*pinpoint\.css###-->/gm,
					replace: '<link rel="stylesheet" href="lib/css/pinpoint.css?v=${buildTime}">'
				}]
			},
			js: {
				src: [ INDEX_HTML ],
				actions: [{
					name: 'base.js',
					search: /<!--###base\-lib\.min\.js[^#]*base\-lib\.min\.js###-->/gm,
					replace: '<script src="lib/js/base-lib.min.js?v=${buildTime}"></script>'
				},{
					name: 'angular.js',
					search: /<!--###angular-lib\.min\.js[^#]*angular-lib\.min\.js###-->/gm,
					replace: '<script src="lib/js/angular-lib.min.js?v=${buildTime}"></script>'
				},{
					name: 'draw.js',
					search: /<!--###draw-lib\.min\.js[^#]*draw-lib\.min\.js###-->/gm,
					replace: '<script src="lib/js/draw-lib.min.js?v=${buildTime}"></script>'
				},{
					name: 'util.js',
					search: /<!--###util-lib\.min\.js[^#]*util-lib\.min\.js###-->/gm,
					replace: '<script src="lib/js/util-lib.min.js?v=${buildTime}"></script>'
				},{
					name: 'pinpoint-component.js',
					search: /<!--###pinpoint-component\.min\.js[^#]*pinpoint-component\.min\.js###-->/gm,
					replace: '<script src="lib/js/pinpoint-component.min.js?v=${buildTime}"></script>'
				},{
					name: 'pinpoint.js',
					search: /<!--###pinpoint\.min\.js[^#]*pinpoint\.min\.js###-->/gm,
					replace: '<script src="lib/js/pinpoint.min.js?v=${buildTime}"></script>'
				}]
			}
		},
		concat: {
			'base_lib': {
				options: concatCommonOptions,
				src: makePath([
					'/jquery/dist/jquery.min.js',
					'/jquery-ui/ui/jquery-ui.js',
					'/underscore/underscore-min.js'
				], resourcePath.component ),
				dest: resourcePath.dest_js + '/base-lib.js'
			},
			'draw_lib': {
				options: concatCommonOptions,
				src: makePath( [
					'/gojs/go.js',
					'/snap.svg/snap.svg.min.js',
					'/d3/d3.min.js',
					'/amcharts/amcharts.js',
					'/amcharts/serial.js',
					'/amcharts/themes/light.js'
				], resourcePath.component ),
				dest: resourcePath.dest_js + '/draw-lib.min.js'
			},
			'util_lib': {
				options: concatCommonOptions,
				src: makePath( [
					'/bootstrap/dist/js/bootstrap.min.js',
					'/google-code-prettify/prettify.js',
					'/google-code-prettify/lang-sql.js',
					'/moment/moment.js',
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
				], resourcePath.component ),
				dest: resourcePath.dest_js + '/util-lib.js'
			},
			'angular_lib': {
				options: concatCommonOptions,
				src: makePath( [
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
				], resourcePath.component ),
				dest: resourcePath.dest_js + '/angular-lib.min.js'
			},
			'time_slider': {
				options: concatCommonOptions,
				src: makePath( [
					'/time-slider.js',
					'/time-slider.background.js',
					'/time-slider.configuration.js',
					'/time-slider.event-data.js',
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
				], resourcePath.time_slider ),
				dest: resourcePath.dest_js + '/time-slider.js'
			},
			'big_scatter': {
				options: concatCommonOptions,
				src: makePath( [
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
				], resourcePath.big_scatter ),
				dest: resourcePath.dest_js + '/big-scatter-chart2.js'
			},
			'infinite_scroll': {
				options: concatCommonOptions,
				src: [ resourcePath.infinite_scroll + '/InfiniteCircularScroll.js' ],
				dest: resourcePath.dest_js + '/infinite-circular-scroll.js'
			},
			'server_map': {
				options: concatCommonOptions,
				src: [ resourcePath.server_map + '/jquery.ServerMap2.js' ],
				dest: resourcePath.dest_js + '/server-map2.js'
			},
			'pinpoint_component': {
				options: concatCommonOptions,
				src: [
					'<%= concat.time_slider.dest %>',
					'<%= concat.big_scatter.dest %>',
					'<%= concat.infinite_scroll.dest %>',
					'<%= concat.server_map.dest %>'
				],
				dest: resourcePath.dest_js + '/pinpoint-component.js'
			},
			'pinpoint_src': {
				options: concatCommonOptions,
				src: makePath( [ 
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
					'/common/services/alarm-broadcast.service.js',
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

					'/features/applicationList/application-list.directive.js',
					'/features/periodSelector/period-selector.directive.js',
					'/features/navbar/navbar.directive.js',
					'/features/navbar2/navbar2.directive.js',
					'/features/server-map/server-map.directive.js',
					'/features/realtimeChart/realtime-chart.directive.js',
					'/features/scatter/scatter.directive.js',
					'/features/nodeInfoDetails/node-info-details.directive.js',
					'/features/linkInfoDetails/link-info-details.directive.js',
					'/features/serverList/server-list.directive.js',
					'/features/agentList/agent-list.directive.js',
					'/features/agentInfo/agent-info.directive.js',
					'/features/time-slider/time-slider.directive.js',
					'/features/transactionTable/transaction-table.directive.js',
					'/features/timeline/timeline.directive.js',
					'/features/agentChartGroup/agent-chart-group.directive.js',
					'/features/sidebar/title/sidebar-title.directive.js',
					'/features/sidebar/filter/filter-information.directive.js',
					'/features/distributedCallFlow/distributed-call-flow.directive.js',
					'/features/responseTimeChart/response-time-chart.directive.js',
					'/features/loadChart/load-chart.directive.js',
					'/features/jvmMemoryChart/jvm-memory-chart.directive.js',
					'/features/cpuLoadChart/cpu-load-chart.directive.js',
					'/features/tpsChart/tps-chart.directive.js',
					'/features/loading/loading.directive.js',
					'/features/configuration/configuration.controller.js',
					'/features/configuration/help/help.controller.js',
					'/features/configuration/general/general.controller.js',
					'/features/configuration/alarm/alarm-user-group.directive.js',
					'/features/configuration/alarm/alarm-group-member.directive.js',
					'/features/configuration/alarm/alarm-pinpoint-user.directive.js',
					'/features/configuration/alarm/alarm-rule.directive.js',
					'/features/realtimeChart/realtime-chart.controller.js',

					'/pages/main/main.controller.js',
					'/pages/inspector/inspector.controller.js',
					'/pages/transactionList/transaction-list.controller.js',
					'/pages/transactionDetail/transaction-detail.controller.js',
					'/pages/filteredMap/filtered-map.controller.js',
					'/pages/transactionView/transaction-view.controller.js',
					'/pages/scatterFullScreenMode/scatter-full-screen-mode.controller.js'

				], resourcePath.root ),
				dest: resourcePath.dest_js + '/pinpoint.js'
			},
			'vendor_css': {
				options: concatCommonOptions,
				src: makePath( [
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
					'/google-code-prettify/sunburst.css'
				], resourcePath.component ),
				dest: resourcePath.dest_css + '/vendor.css'
			},
			'pinpoint_css': {
				options: concatCommonOptions,
				src: makePath( [
					'/main.css',
					'/inspector.css',
					'/navbar.css',
					'/nodeInfoDetails.css',
					'/linkInfoDetails.css',
					'/transactionTable.css',
					'/callStacks.css',
					'/time-slider.css',
					'/agentChartGroup.css',
					'/sidebarTitle.css',
					'/server-map.css',
					'/filterInformation.css',
					'/distributedCallFlow.css',
					'/loading.css',
					'/jquery.BigScatterChart.css',
					'/timer.css',
					'/configuration.css',
					'/xeicon.min.css'
				], resourcePath.style ),
				dest: resourcePath.dest_css + '/pinpoint.css'
			}
		},
		uglify: {
			'base_lib': {
				options: uglifyCommonOptions,
				files: {
					'main/webapp/lib/js/base-lib.min.js': '<%= concat.base_lib.dest %>'
				}
			},
			'util_lib': {
				options: uglifyCommonOptions,
				files: {
					'main/webapp/lib/js/util-lib.min.js': '<%= concat.util_lib.dest %>'
				}
			},
			'time_slider': {
				options: uglifyCommonOptionsWidthSourceMap,
				files: {
					'main/webapp/lib/js/time-slider.min.js': '<%= concat.time_slider.dest %>'
				}
			},
			'big_scatter': {
				options: uglifyCommonOptionsWidthSourceMap,
				files: {
					'main/webapp/lib/js/big-scatter-chart2.min.js': '<%= concat.big_scatter.dest %>'
				}
			},
			'infinite_scroll': {
				options: uglifyCommonOptionsWidthSourceMap,
				files: {
					'main/webapp/lib/js/infinite-circular-scroll.min.js': '<%= concat.infinite_scroll.dest %>'
				}
			},
			'server_map': {
				options: uglifyCommonOptionsWidthSourceMap,
				files: {
					'main/webapp/lib/js/server-map2.min.js': '<%= concat.server_map.dest %>'
				}
			},
			'pinpoint_component': {
				options: uglifyCommonOptionsWidthSourceMap,
				files: {
					'main/webapp/lib/js/pinpoint-component.min.js': 'main/webapp/lib/js/pinpoint-component.js'
				}
			},
			'pinpoint_src': {
				options: uglifyCommonOptionsWidthSourceMap,
				files: {
					'main/webapp/lib/js/pinpoint.min.js': 'main/webapp/lib/js/pinpoint.js'
				}
			}
		},
		watch: {
			'pinpoint': {
				files: [
					resourcePath.style + '/*.css',
					resourcePath.time_slider + '/*.js',
					resourcePath.big_scatter + '/*.js',
					resourcePath.infinite_scroll + '/*.js',
					resourcePath.server_map + '/*.js',
					resourcePath.root + 'common/**/*.js',
					resourcePath.root + 'features/**/*.js',
					resourcePath.root + 'pages/**/*.js'
				],
				tasks: [ 'build_pinpoint_css', 'build_pinpoint_component', 'build_pinpoint_src' ]
			},
			'pinpoint_css': {
				files: [ resourcePath.style + '/*.css' ],
				tasks: [ 'build_pinpoint_css' ]
			},
			'pinpoint_component': {
				files: [
					resourcePath.time_slider + '/*.js',
					resourcePath.big_scatter + '/*.js',
					resourcePath.infinite_scroll + '/*.js',
					resourcePath.server_map + '/*.js'
				],
				tasks: ['build_pinpoint_component' ]
			},
			'pinpoint_src': {
				files: makePath([
					'common/**/*.js',
					'features/**/*.js',
					'pages/**/*.js'
				], resourcePath.root ),
				tasks: ['build_pinpoint_src' ]
			}
		},
		jshint: {
			options: {
				sub: true,
				boss: true,
				newcap: false,
				loopfunc: true
			},
			files: [
				resourcePath.root + '/common/**/*.js',
				resourcePath.root + '/features/**/*.js',
				resourcePath.root + '/pages/**/*.js'
			]
		}
    });

    // Load task libraries
	grunt.loadNpmTasks('grunt-contrib-copy');
	grunt.loadNpmTasks('grunt-contrib-watch');
	grunt.loadNpmTasks('grunt-contrib-clean');
	grunt.loadNpmTasks('grunt-regex-replace');
	grunt.loadNpmTasks('grunt-contrib-concat');
	grunt.loadNpmTasks('grunt-contrib-uglify');
	grunt.loadNpmTasks('grunt-contrib-jshint');

	grunt.registerTask('lint', '', [
		'jshint'
	]);
	grunt.registerTask('build_base_lib', '', [
		'concat:base_lib',
		'uglify:base_lib'
	]);
	grunt.registerTask('build_draw_lib', '', [
		'concat:draw_lib'
	]);
	grunt.registerTask('build_util_lib', '', [
		'concat:util_lib',
		'uglify:util_lib'
	]);
	grunt.registerTask('build_angular_lib', '', [
		'concat:angular_lib'
	]);
	grunt.registerTask('build_vendor_js', 'Create vendor js file', [
		'build_base_lib',
		'build_draw_lib',
		'build_util_lib',
		'build_angular_lib',
		'clean:vendor_js'
	]);


	grunt.registerTask('build_time_slider', '', [
		'concat:time_slider',
		'uglify:time_slider'
	]);
	grunt.registerTask('build_big_scatter', '', [
		'concat:big_scatter',
		'uglify:big_scatter'
	]);
	grunt.registerTask('build_infinite_scroll', '', [
		'concat:infinite_scroll',
		'uglify:infinite_scroll'
	]);
	grunt.registerTask('build_server_map', '', [
		'concat:server_map',
		'uglify:server_map'
	]);
	grunt.registerTask('build_pinpoint_component', 'Create pinpoint component js file', [
		'concat:time_slider',
		'concat:big_scatter',
		'concat:infinite_scroll',
		'concat:server_map',
		'concat:pinpoint_component',
		'uglify:pinpoint_component',
		'clean:pinpoint_component_js'
	]);

	grunt.registerTask('build_pinpoint_src', 'Create pinpoint js file', [
		'concat:pinpoint_src',
		'uglify:pinpoint_src',
		'clean:pinpoint_src'
	]);

	grunt.registerTask('build_all_js', 'Create js file', [
		'build_vendor_js',
		'build_pinpoint_component',
		'build_pinpoint_src'
	]);


	grunt.registerTask('build_vendor_css', 'Create vendor css file', [
		'concat:vendor_css'
	]);
	grunt.registerTask('build_pinpoint_css', 'Create pinpoint css file', [
		'concat:pinpoint_css'
	]);
	grunt.registerTask('build_all_css', 'Create css file', [
		'concat:vendor_css',
		'concat:pinpoint_css'
	]);

	grunt.registerTask('replace_static', 'replace to minified resource', [
		'regex-replace:css',
		'regex-replace:js'
	]);
	grunt.registerTask('build_release', '', [
		'lint',
		'build_all_js',
		'build_all_css',
		'replace_static'
	]);
};
