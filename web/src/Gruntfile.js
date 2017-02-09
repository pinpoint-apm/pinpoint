/**
 * http://gruntjs.com/configuring-tasks
 */
var ROOT = 'main/webapp';

module.exports = function (grunt) {

	require('time-grunt')(grunt);

	// load grunt task and split task file
	require('load-grunt-config')( grunt, {
		data: {
			RESOURCE_PATH: {
				'ROOT': ROOT,
				'STYLE': ROOT + '/styles',
				'DEST_JS': ROOT + '/lib/js',
				'DEST_CSS': ROOT + '/lib/css',
				'COMPONENT': ROOT + '/components',
				'PAGE': ROOT + '/pages',
				'COMMON': ROOT + '/common',
				'FEATURE': ROOT + '/features',
				'INDEX_HTML': ROOT + '/index.html',
				'SERVER_MAP': ROOT + '/components/server-map2',
				'TIME_SLIDER': ROOT + '/components/time-slider',
				'BIG_SCATTER': ROOT + '/components/big-scatter-chart',
				'INFINITE_SCROLL': ROOT + '/components/infinite-circular-scroll'
			},
			makePath: function( a, prefix) {
				return a.map( function( value ) {
					return prefix + value;
				});
			}
		}
	});

	grunt.registerTask('lint', [
		'jshint'
	]);
	//vendor js build
	grunt.registerTask('build_base_lib', [
		'concat:base_lib',
		'uglify:base_lib'
	]);
	grunt.registerTask('build_draw_lib', [
		'concat:draw_lib'
	]);
	grunt.registerTask('build_util_lib', [
		'concat:util_lib',
		'uglify:util_lib'
	]);
	grunt.registerTask('build_angular_lib', [
		'concat:angular_lib'
	]);
	grunt.registerTask('build_vendor_js', [
		'build_base_lib',
		'build_draw_lib',
		'build_util_lib',
		'build_angular_lib',
		'clean:vendor_js'
	]);

	// component js build
	grunt.registerTask('build_time_slider', [
		'concat:time_slider',
		'uglify:time_slider'
	]);
	grunt.registerTask('build_big_scatter', [
		'concat:big_scatter',
		'uglify:big_scatter'
	]);
	grunt.registerTask('build_infinite_scroll', [
		'concat:infinite_scroll',
		'uglify:infinite_scroll'
	]);
	grunt.registerTask('build_server_map', [
		'concat:server_map',
		'uglify:server_map'
	]);
	grunt.registerTask('build_pinpoint_component', [
		'concat:time_slider',
		'concat:big_scatter',
		'concat:infinite_scroll',
		'concat:server_map',
		'concat:pinpoint_component',
		'uglify:pinpoint_component',
		'clean:pinpoint_component_js'
	]);

	// js build
	grunt.registerTask('build_pinpoint_src', [
		'concat:pinpoint_src',
		'uglify:pinpoint_src',
		'clean:pinpoint_src'
	]);
	grunt.registerTask('build_all_js', [
		'build_vendor_js',
		'build_pinpoint_component',
		'build_pinpoint_src'
	]);

	// css build
	grunt.registerTask('build_vendor_css', [
		'concat:vendor_css'
	]);
	grunt.registerTask('build_pinpoint_css', [
		'concat:pinpoint_css'
	]);
	grunt.registerTask('build_all_css', [
		'concat:vendor_css',
		'concat:pinpoint_css'
	]);

	//replace js, css link
	grunt.registerTask('replace_static', [
		'regex-replace:css',
		'regex-replace:js',
		'regex-replace:html'
	]);

	grunt.registerTask('build_release_without_lint', [
		'build_all_js',
		'build_all_css',
		'replace_static',
		'clean:origin_folder'
	]);
	grunt.registerTask('build_release', [
		'lint',
		'build_all_js',
		'build_all_css',
		'replace_static',
		'clean:origin_folder'
	]);

	grunt.registerTask('watch_js', [
		'watch:pinpoint'
	]);

};
