
module.exports = function( grunt, options ) {
	return {
		'pinpoint': {
			files: [
				//config.RESOURCE_PATH.style + '/*.css',
				options.RESOURCE_PATH.TIME_SLIDER + '/*.js',
				options.RESOURCE_PATH.BIG_SCATTER + '/*.js',
				options.RESOURCE_PATH.INFINITE_SCROLL + '/*.js',
				options.RESOURCE_PATH.SERVER_MAP + '/*.js',
				options.RESOURCE_PATH.ROOT + 'common/**/*.js',
				options.RESOURCE_PATH.ROOT + 'features/**/*.js',
				options.RESOURCE_PATH.ROOT + 'pages/**/*.js'
			],
			tasks: ['lint']
		},
		// 'pinpoint_css': {
		// 	files: [ config.RESOURCE_PATH.style + '/*.css' ],
		// 	tasks: []
		// },
		'pinpoint_component': {
			files: [
				options.RESOURCE_PATH.TIME_SLIDER + '/*.js',
				options.RESOURCE_PATH.BIG_SCATTER + '/*.js',
				options.RESOURCE_PATH.INFINITE_SCROLL + '/*.js',
				options.RESOURCE_PATH.SERVER_MAP + '/*.js'
			],
			tasks: ['lint']
		},
		'pinpoint_src': {
			files: options.makePath([
				'common/**/*.js',
				'features/**/*.js',
				'pages/**/*.js'
			], options.RESOURCE_PATH.ROOT),
			tasks: ['lint']
		}
	};
};