/**
 * http://gruntjs.com/configuring-tasks
 */
module.exports = function (grunt) {
    var path = require('path');
    var DOC_PATH = 'jsdoc/';
	var COMPONENT_PATH = 'main/webapp/components/';

    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),

        clean: {
			doc: [ DOC_PATH ],
			lib: [ '<%= concat.timeSlider.dest %>','<%= concat.bigscatter.dest %>', '<%= concat.infiniteScroll.dest %>'  ]
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
                    'private': false
                }
            }
        },
		concat: {
			baseLib: {
				options: {
					stripBanner: true,
					//banner: '/*! <%= pkg.name %> - v<%= pkg.version %> - ''<%= grunt.template.today("yyyy-mm-dd") %> */',
					separator: '\n'
				},
				src: [
					COMPONENT_PATH + 'jquery/dist/jquery.min.js',
					COMPONENT_PATH + 'jquery-ui/ui/jquery-ui.js',
					COMPONENT_PATH + 'underscore/underscore-min.js'
				],
				dest: 'main/webapp/lib/js/base-lib.js'
			},
			drawLib: {
				options: {
					stripBanner: true,
					//banner: '/*! <%= pkg.name %> - v<%= pkg.version %> - ''<%= grunt.template.today("yyyy-mm-dd") %> */',
					separator: '\n'
				},
				src: [
					COMPONENT_PATH + 'gojs/go.js',
					COMPONENT_PATH + 'snap.svg/snap.svg.min.js',
					COMPONENT_PATH + 'd3/d3.min.js',
					COMPONENT_PATH + 'amcharts/amcharts.js',
					COMPONENT_PATH + 'amcharts/serial.js',
					COMPONENT_PATH + 'amcharts/themes/light.js'
				],
				dest: 'main/webapp/lib/js/draw-lib.js'
			},
			utilLib: {
				options: {
					stripBanner: true,
					//banner: '/*! <%= pkg.name %> - v<%= pkg.version %> - ''<%= grunt.template.today("yyyy-mm-dd") %> */',
					separator: '\n'
				},
				src: [
					COMPONENT_PATH + 'bootstrap/dist/js/bootstrap.min.js',
					COMPONENT_PATH + 'google-code-prettify/prettify.js',
					COMPONENT_PATH + 'google-code-prettify/lang-sql.js',
					COMPONENT_PATH + 'moment/moment.js',
					COMPONENT_PATH + 'select2/select2.min.js',
					COMPONENT_PATH + 'select2/select2_locale_ko.js',
					COMPONENT_PATH + 'jquery-class/jquery.Class.js',
					COMPONENT_PATH + 'jquery.layout/dist/jquery.layout-latest.js',
					COMPONENT_PATH + 'dragToSelect/jquery.dragToSelect.js',
					COMPONENT_PATH + 'jquery-timepicker-addon/jquery-ui-timepicker-addon.js',
					COMPONENT_PATH + 'jquery.jslider/js/draggable-0.1.js',
					COMPONENT_PATH + 'jquery.jslider/js/jshashtable-2.1_src.js',
					COMPONENT_PATH + 'jquery.jslider/js/jquery.dependClass-0.1.js',
					COMPONENT_PATH + 'jquery.jslider/js/jquery.numberformatter-1.2.3.js',
					COMPONENT_PATH + 'jquery.jslider/js/tmpl.js',
					COMPONENT_PATH + 'jquery.jslider/js/jquery.jslider.js',
					COMPONENT_PATH + 'jquery-ui-tabs-paging/js/ui.tabs.paging.js',
					COMPONENT_PATH + 'slickgrid/lib/jquery.event.drag-2.2.js',
					COMPONENT_PATH + 'slickgrid/slick.core.js',
					COMPONENT_PATH + 'slickgrid/slick.formatters.js',
					COMPONENT_PATH + 'slickgrid/slick.editors.js',
					COMPONENT_PATH + 'slickgrid/slick.grid.js',
					COMPONENT_PATH + 'slickgrid/slick.dataview.js',
					COMPONENT_PATH + 'slickgrid/plugins/slick.rowselectionmodel.js',
					COMPONENT_PATH + 'tooltipster/js/jquery.tooltipster.min.js',
					COMPONENT_PATH + 'handlebars/handlebars.min.js',
					COMPONENT_PATH + 'bootstrap/js/bootstrap-modal.js',
					COMPONENT_PATH + 'bootstrap/js/bootstrap-tooltip.js',
					COMPONENT_PATH + 'bootstrap/js/bootstrap-popover.js'
				],
				dest: 'main/webapp/lib/js/util-lib.js'
			},
			angularLib: {
				options: {
					stripBanner: true,
					//banner: '/*! <%= pkg.name %> - v<%= pkg.version %> - ''<%= grunt.template.today("yyyy-mm-dd") %> */',
					separator: '\n'
				},
				src: [
					COMPONENT_PATH + 'angular/angular.js',
					COMPONENT_PATH + 'angular-resource/angular-resource.js',
					COMPONENT_PATH + 'angular-cookies/angular-cookies.js',
					COMPONENT_PATH + 'angular-webstorage/angular-webstorage.js',
					COMPONENT_PATH + 'angular-strap/dist/angular-strap.min.js',
					COMPONENT_PATH + 'angular-strap/dist/angular-strap.tpl.min.js',
					COMPONENT_PATH + 'angular-animate/angular-animate.min.js',
					COMPONENT_PATH + 'angular-route/angular-route.js',
					COMPONENT_PATH + 'angular-sanitize/angular-sanitize.min.js',
					COMPONENT_PATH + 'angular-slider/angular-slider.min.js',
					COMPONENT_PATH + 'angular-base64/angular-base64.min.js',
					COMPONENT_PATH + 'angular-timer/dist/angular-timer.min.js'
				],
				dest: 'main/webapp/lib/js/angular-lib.js'
			},
			timeSlider: {
				options: {
					stripBanner: true,
					//banner: '/*! <%= pkg.name %> - v<%= pkg.version %> - ''<%= grunt.template.today("yyyy-mm-dd") %> */',
					separator: '\n'
				},
				src: [
					COMPONENT_PATH + 'time-slider/time-slider.js',
					COMPONENT_PATH + 'time-slider/time-slider.background.js',
					COMPONENT_PATH + 'time-slider/time-slider.configuration.js',
					COMPONENT_PATH + 'time-slider/time-slider.event-data.js',
					COMPONENT_PATH + 'time-slider/time-slider.events.js',
					COMPONENT_PATH + 'time-slider/time-slider.handler.js',
					COMPONENT_PATH + 'time-slider/time-slider.loading-indicator.js',
					COMPONENT_PATH + 'time-slider/time-slider.position-manager.js',
					COMPONENT_PATH + 'time-slider/time-slider.selection-manager.js',
					COMPONENT_PATH + 'time-slider/time-slider.selection-point.js',
					COMPONENT_PATH + 'time-slider/time-slider.selection-zone.js',
					COMPONENT_PATH + 'time-slider/time-slider.state-line.js',
					COMPONENT_PATH + 'time-slider/time-slider.time-series-signboard.js',
					COMPONENT_PATH + 'time-slider/time-slider.time-signboard.js',
					COMPONENT_PATH + 'time-slider/time-slider.x-axis.js'
				],
				dest: 'main/webapp/lib/js/time-slider.js'
			},
			bigscatter: {
				options: {
					stripBanner: true,
					//banner: '/*! <%= pkg.name %> - v<%= pkg.version %> - ''<%= grunt.template.today("yyyy-mm-dd") %> */',
					separator: '\n'
				},
				src: [
					COMPONENT_PATH + 'bigscatter-chart/BigScatterChart2.js',
					COMPONENT_PATH + 'bigscatter-chart/BigScatterChart2.DataBlock.js',
					COMPONENT_PATH + 'bigscatter-chart/BigScatterChart2.DragManager.js',
					COMPONENT_PATH + 'bigscatter-chart/BigScatterChart2.DataLoadManager.js',
					COMPONENT_PATH + 'bigscatter-chart/BigScatterChart2.RendererManager.js',
					COMPONENT_PATH + 'bigscatter-chart/BigScatterChart2.BubbleTypeManager.js',
					COMPONENT_PATH + 'bigscatter-chart/BigScatterChart2.SizeCoordinateManager.js',
					COMPONENT_PATH + 'bigscatter-chart/BigScatterChart2.Util.js',
					COMPONENT_PATH + 'bigscatter-chart/BigScatterChart2.HelpPlugin.js',
					COMPONENT_PATH + 'bigscatter-chart/BigScatterChart2.MessagePlugin.js',
					COMPONENT_PATH + 'bigscatter-chart/BigScatterChart2.SettingPlugin.js',
					COMPONENT_PATH + 'bigscatter-chart/BigScatterChart2.DownloadPlugin.js',
					COMPONENT_PATH + 'bigscatter-chart/BigScatterChart2.WideOpenPlugin.js'
				],
				dest: 'main/webapp/lib/js/BigScatterChart2.js'
			},
			infiniteScroll: {
				src: [
					COMPONENT_PATH + 'infiniteCircularScroll/InfiniteCircularScroll.js'
				],
				dest: 'main/webapp/lib/js/InfiniteCircularScroll.js'
			}
		},
		uglify: {
			dist: {
				files: {
					'main/webapp/lib/js/time-slider.min.js': 'main/webapp/lib/js/time-slider.js',
					'main/webapp/lib/js/BigScatterChart2.min.js': 'main/webapp/lib/js/BigScatterChart2.js',
					'main/webapp/lib/js/InfiniteCircularScroll.min.js': 'main/webapp/lib/js/InfiniteCircularScroll.js'
				}
			}
		},
		watch: {
			cssDev: {
				files: [],
				tasks: []
			},
			jsDev: {
				files: [
					COMPONENT_PATH + 'time-slider/*.js',
					COMPONENT_PATH + 'bigscatter-chart/*.js',
					COMPONENT_PATH + 'infiniteCircularScroll/*.js',
				],
				tasks: ['buildDev']
			}
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
        'grunt-jsdoc',
        'grunt-karma'
    ].forEach(function (taskName) {
        grunt.loadNpmTasks(taskName);
    });

    grunt.registerTask('jsdocClean', 'Clear document folder', [ 'clean' ]);

    grunt.registerTask('jsdocBuild', 'Create documentations', [
        'clean:doc',
        'copy:plugin',
        'jsdoc:dist'
    ]);
	grunt.registerTask('buildDev', 'Create lib file', [
		'concat:timeSlider',
		'concat:bigscatter',
		'concat:infiniteScroll',
		'uglify',
		'clean:lib'
	]);
	grunt.registerTask('buildLib', 'Create lib file', [
		'concat:baseLib',
		'concat:drawLib',
		'concat:utilLib',
		'concat:angularLib'
	]);
	grunt.registerTask('watchDev', 'Watch js, css', [
		'watch:jsDev',
		'watch:cssDev'
	]);
};
