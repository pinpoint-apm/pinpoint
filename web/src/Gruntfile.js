/**
 * http://gruntjs.com/configuring-tasks
 */
module.exports = function (grunt) {
    var path = require('path');
    var DOC_PATH = 'jsdoc/';
	var COMPONENT_PATH = 'main/webapp/components/';
	var STYLE_PATH = 'main/webapp/styles/';
	var DEST_LIB_PATH = 'main/webapp/lib/';

	function makePath( a, prefix) {
		return a.map( function( value, index ) {
			return prefix + value;
		});
	}

    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),

        clean: {
			vendorJS: [
				'<%= concat.baseLib.dest %>',
				'<%= concat.utilLib.dest %>'
			],
			pinpointComponentJS: [
				DEST_LIB_PATH + 'js/*.js',
				'!' + DEST_LIB_PATH + 'js/*.min.js'
			]
		},
        jsdoc: {
            dist: {
                src: [
                      'main/webapp/common/**/*.js', 
                      'main/webapp/features/**/*.js', 
                      'main/webapp/pages/**/*.js', 
                      'main/webapp/scripts/**/*.js'
                ],
                options: {
                    verbose: true,
                    destination: DOC_PATH,
                    template: 'node_modules/egjs-jsdoc-template/',
                    configure: 'conf.json',
                    private: false
                }
            }
        },
		concat: {
			baseLib: {
				options: {
					stripBanner: true,
					banner: '/*! @preserve <%= pkg.name %> - v<%= pkg.version %> - <%= grunt.template.today("yyyy-mm-dd")%> - jquery + jquery.ui + underscore*/',
					separator: '\n'
				},
				src: makePath([
					'jquery/dist/jquery.min.js',
					'jquery-ui/ui/jquery-ui.js',
					'underscore/underscore-min.js'
				], COMPONENT_PATH ),
				dest: DEST_LIB_PATH + 'js/base-lib.js'
			},
			drawLib: {
				options: {
					stripBanner: true,
					banner: '/*! <%= pkg.name %> - v<%= pkg.version %> - <%= grunt.template.today("yyyy-mm-dd")%> - go + snap.svg + d3 + amcharts*/',
					separator: '\n'
				},
				src: makePath( [
					'gojs/go.js',
					'snap.svg/snap.svg.min.js',
					'd3/d3.min.js',
					'amcharts/amcharts.js',
					'amcharts/serial.js',
					'amcharts/themes/light.js'
				], COMPONENT_PATH ),
				dest: DEST_LIB_PATH + 'js/draw-lib.min.js'
			},
			utilLib: {
				options: {
					stripBanner: true,
					banner: '/*! <%= pkg.name %> - v<%= pkg.version %> - <%= grunt.template.today("yyyy-mm-dd") %> - bootstrap, preffify, moment, select2, jquery.Class, jquery.layout, jslider, slickgrid, tooltipster, handlebars */',
					separator: '\n'
				},
				src: makePath( [
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
				], COMPONENT_PATH ),
				dest: DEST_LIB_PATH + 'js/util-lib.js'
			},
			angularLib: {
				options: {
					stripBanner: true,
					banner: '/*! <%= pkg.name %> - v<%= pkg.version %> - <%= grunt.template.today("yyyy-mm-dd") %> - angular + -resource + -cookies + -webstorage + -strap + -animate + -route + -sanitize + -slider + -base64 + -timer */',
					separator: '\n'
				},
				src: makePath( [
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
				], COMPONENT_PATH ),
				dest: DEST_LIB_PATH + 'js/angular-lib.min.js'
			},
			timeSlider: {
				options: {
					stripBanner: true,
					banner: '/*! <%= pkg.name %> - v<%= pkg.version %> - <%= grunt.template.today("yyyy-mm-dd") %> */',
					separator: '\n'
				},
				src: makePath( [
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
				], COMPONENT_PATH ),
				dest: DEST_LIB_PATH + 'js/time-slider.js'
			},
			bigScatter: {
				options: {
					stripBanner: true,
					banner: '/*! <%= pkg.name %> - v<%= pkg.version %> - <%= grunt.template.today("yyyy-mm-dd") %> */',
					separator: '\n'
				},
				src: makePath( [
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
				], COMPONENT_PATH ),
				dest: DEST_LIB_PATH + 'js/big-scatter-chart2.js'
			},
			infiniteScroll: {
				options: {
					stripBanner: true,
					banner: '/*! <%= pkg.name %> - v<%= pkg.version %> - <%= grunt.template.today("yyyy-mm-dd") %> */',
					separator: '\n'
				},
				src: [
					COMPONENT_PATH + 'infiniteCircularScroll/InfiniteCircularScroll.js'
				],
				dest: DEST_LIB_PATH + 'js/infinite-circular-scroll.js'
			},
			pinpointComponent: {
				options: {
					stripBanner: true,
					banner: '/*! <%= pkg.name %> - v<%= pkg.version %> - <%= grunt.template.today("yyyy-mm-dd") %> - TimeSlider + BigScatterChart2 + InfiniteCircularScroll*/',
					separator: '\n'
				},
				src: makePath( [
					'js/time-slider.js',
					'js/big-scatter-chart2.js',
					'js/infinite-circular-scroll.js'
				], DEST_LIB_PATH ),
				dest: DEST_LIB_PATH + 'js/pinpoint-component.min.js'
			},
			vernderCSS: {
				options: {
					stripBanner: true,
					//banner: '/*! <%= pkg.name %> - v<%= pkg.version %> - ''<%= grunt.template.today("yyyy-mm-dd") %> */',
					separator: '\n'
				},
				src: makePath( [
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
				], COMPONENT_PATH ),
				dest: DEST_LIB_PATH + 'css/vendor.css'
			},
			pinpointCSS: {
				options: {
					stripBanner: true,
					//banner: '/*! <%= pkg.name %> - v<%= pkg.version %> - ''<%= grunt.template.today("yyyy-mm-dd") %> */',
					separator: '\n'
				},
				src: makePath( [
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
				], STYLE_PATH ),
				dest: 'main/webapp/lib/css/pinpoint.css'
			}
		},
		uglify: {
			baseLib: {
				options: {
					preserveComments: false,
					banner: '/*! @preserve <%= pkg.name %> - v<%= pkg.version %> - <%= grunt.template.today("yyyy-mm-dd") %> - jquery + jquery.ui + underscore*/'
				},
				files: {
					'main/webapp/lib/js/base-lib.min.js': 'main/webapp/lib/js/base-lib.js'
				}
			},
			utilLib: {
				options: {
					preserveComments: false,
					banner: '/*! <%= pkg.name %> - v<%= pkg.version %> - <%= grunt.template.today("yyyy-mm-dd") %> - bootstrap, preffify, moment, select2, jquery.Class, jquery.layout, jslider, slickgrid, tooltipster, handlebars */'
				},
				files: {
					'main/webapp/lib/js/util-lib.min.js': 'main/webapp/lib/js/util-lib.js'
				}
			},
			timeSlider : {
				options: {
					preserveComments: false,
					banner: '/*! <%= pkg.name %> - v<%= pkg.version %> - <%= grunt.template.today("yyyy-mm-dd") %>*/'
				},
				files: {
					'main/webapp/lib/js/time-slider.min.js': 'main/webapp/lib/js/time-slider.js'
				}
			},
			bigScatter : {
				options: {
					preserveComments: false,
					banner: '/*! <%= pkg.name %> - v<%= pkg.version %> - <%= grunt.template.today("yyyy-mm-dd") %>*/'
				},
				files: {
					'main/webapp/lib/js/big-scatter-chart2.min.js': 'main/webapp/lib/js/big-scatter-chart2.js'
				}
			},
			infiniteScroll : {
				options: {
					preserveComments: false,
					banner: '/*! <%= pkg.name %> - v<%= pkg.version %> - <%= grunt.template.today("yyyy-mm-dd") %>*/'
				},
				files: {
					'main/webapp/lib/js/infinite-circular-scroll.min.js': 'main/webapp/lib/js/infinite-circular-scroll.js'
				}
			}
		},
		watch: {
			cssDev: {
				files: makePath([
					'*.js'
				], STYLE_PATH ),
				tasks: [ 'buildPinpointCSS']
			},
			jsDev: {
				files: makePath([
					'time-slider/*.js',
					'big-scatter-chart/*.js',
					'infiniteCircularScroll/*.js'
				], COMPONENT_PATH),
				tasks: ['buildPinpointComponentJS' ]
			}
		},
		jshint: {
			options: {
				sub: true,
				newcap: false,
				loopfunc: true
			},
			files: ['main/webapp/common/**/*.js', 'main/webapp/features/**/*.js', 'main/webapp/pages/**/*.js']
		},
        karma: {
        	unit: {
        		configFile: 'karma.conf.js',
        		autoWatch: true
        	}
        },
        copy: {
            plugin: {
              files: [{
                src: 'node_modules/egjs-jsdoc-template/jsdoc-plugin/*.js',
                dest: 'node_modules/grunt-jsdoc/node_modules/jsdoc/plugins/',
                flatten : true,
                expand : true
              }]
            }
        }        
    });

    // Load task libraries
    [
		'grunt-contrib-concat',
		'grunt-contrib-uglify',
		'grunt-contrib-watch',
     	'grunt-contrib-clean',
     	'grunt-contrib-copy',
		'grunt-contrib-jshint',
        'grunt-jsdoc',
        'grunt-karma'
    ].forEach(function (taskName) {
        grunt.loadNpmTasks(taskName);
    });

    //grunt.registerTask('jsdocClean', 'Clear document folder', [ 'clean' ]);
	//
    //grunt.registerTask('jsdocBuild', 'Create documentations', [
    //    'clean:doc',
    //    'copy:plugin',
    //    'jsdoc:dist'
    //]);
	grunt.registerTask('buildBaseLibJS', '', [
		'concat:baseLib',
		'uglify:baseLib'
	]);
	grunt.registerTask('buildDrawLibJS', '', [
		'concat:drawLib'
	]);
	grunt.registerTask('buildUtilLibJS', '', [
		'concat:utilLib',
		'uglify:utilLib'
	]);
	grunt.registerTask('buildAngularJS', '', [
		'concat:angularLib'
	]);
	grunt.registerTask('buildTimeSlider', '', [
		'concat:timeSlider',
		'uglify:timeSlider'
	]);
	grunt.registerTask('buildBigScatter', '', [
		'concat:bigScatter',
		'uglify:bigScatter'
	]);
	grunt.registerTask('buildInfiniteScroll', '', [
		'concat:infiniteScroll',
		'uglify:infiniteScroll'
	]);

	grunt.registerTask('buildAllJS', 'Create js file', [
		'buildVendorJS',
		'buildPinpointComponentJS'
	]);
	grunt.registerTask('buildVendorJS', 'Create vendor js file', [
		'buildBaseLibJS',
		'buildDrawLibJS',
		'buildUtilLibJS',
		'buildAngularJS',
		'clean:vendorJS'
	]);
	grunt.registerTask('buildPinpointComponentJS', 'Create pinpoint js file', [
		'buildTimeSlider',
		'buildBigScatter',
		'buildInfiniteScroll',
		'concat:pinpointComponent',
		'clean:pinpointComponentJS'
	]);

	grunt.registerTask('buildAllCSS', 'Create css file', [
		'concat:vernderCSS',
		'concat:pinpointCSS'
	]);
	grunt.registerTask('buildVendorCSS', 'Create css file', [
		'concat:vernderCSS'
	]);
	grunt.registerTask('buildPinpointCSS', 'Create css file', [
		'concat:pinpointCSS'
	]);
	grunt.registerTask('watchDev', 'Watch js, css', [
		'watch:jsDev',
		'watch:cssDev'
	]);
	grunt.registerTask('jshintDev', '', [
		'jshint'
	]);
};
