
module.exports = function( grunt, options ) {
	return {
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
			options.RESOURCE_PATH.DEST_JS + '/*.min.js'
		],
		'origin_folder': [
			options.RESOURCE_PATH.COMMON,
			options.RESOURCE_PATH.COMPONENT,
			options.RESOURCE_PATH.FEATURE,
			options.RESOURCE_PATH.PAGE,
			options.RESOURCE_PATH.STYLE
		]
	}
};