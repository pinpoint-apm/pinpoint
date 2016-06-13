module.exports = function( grunt, options ) {
	var commonOptions = {
		preserveComments: false,
		banner: '/*! @preserve ' + options.package.name + ' - ' + 'v' + options.package.version + ' - ' + grunt.template.today("yyyy-mm-dd") + '*/'
	};
	var commonOptionsWidthSourceMap = {
		preserveComments: false,
		sourceMap: true,
		banner: '/*! @preserve ' + options.package.name + ' - ' + 'v' + options.package.version + ' - ' + grunt.template.today("yyyy-mm-dd") + '*/'
	};

	return {
		'base_lib': {
			options: commonOptions,
			files: {
				'main/webapp/lib/js/base-lib.min.js': '<%= concat.base_lib.dest %>'
			}
		},
		'util_lib': {
			options: commonOptions,
			files: {
				'main/webapp/lib/js/util-lib.min.js': '<%= concat.util_lib.dest %>'
			}
		},
		'time_slider': {
			options: commonOptionsWidthSourceMap,
			files: {
				'main/webapp/lib/js/time-slider.min.js': '<%= concat.time_slider.dest %>'
			}
		},
		'big_scatter': {
			options: commonOptionsWidthSourceMap,
			files: {
				'main/webapp/lib/js/big-scatter-chart2.min.js': '<%= concat.big_scatter.dest %>'
			}
		},
		'infinite_scroll': {
			options: commonOptionsWidthSourceMap,
			files: {
				'main/webapp/lib/js/infinite-circular-scroll.min.js': '<%= concat.infinite_scroll.dest %>'
			}
		},
		'server_map': {
			options: commonOptionsWidthSourceMap,
			files: {
				'main/webapp/lib/js/server-map2.min.js': '<%= concat.server_map.dest %>'
			}
		},
		'pinpoint_component': {
			options: commonOptionsWidthSourceMap,
			files: {
				'main/webapp/lib/js/pinpoint-component.min.js': options.RESOURCE_PATH.DEST_JS + '/pinpoint-component.js'
			}
		},
		'pinpoint_src': {
			options: commonOptionsWidthSourceMap,
			files: {
				'main/webapp/lib/js/pinpoint.min.js': options.RESOURCE_PATH.DEST_JS + '/pinpoint.js'
			}
		}
	};
};
