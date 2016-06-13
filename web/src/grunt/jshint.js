
module.exports = function( grunt, options ) {
	return {
		options: {
			sub: true,
			boss: true,
			newcap: false,
			loopfunc: true
		},
		files: [
			options.RESOURCE_PATH.ROOT + '/common/**/*.js',
			options.RESOURCE_PATH.ROOT + '/features/**/*.js',
			options.RESOURCE_PATH.ROOT + '/pages/**/*.js'
		]
	};
};