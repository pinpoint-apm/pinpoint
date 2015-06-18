// Karma configuration

module.exports = function(config) {
	config.set({
		// base path, that will be used to resolve files and exclude
		basePath: '',
		frameworks: ['jasmine'],
		// list of files / patterns to load in the browser
		files: [
		  'main/webapp/components/angular/angular.js',
		  'main/webapp/components/angular-mocks/angular-mocks.js',
		  'main/webapp/components/angular-resource/angular-resource.js',
		  'main/webapp/components/angular-route/angular-route.js',
		  'main/webapp/components/angular-sanitize/angular-sanitize.js',
		  'main/webapp/components/angular-webstorage/angular-webstorage.js',
		  'main/webapp/components/angular-base64/angular-base64.min.js',
		  'main/webapp/components/angular-slider/angular-slider.js',
		  'main/webapp/components/angular-strap/dist/angular-strap.min.js',
		  'main/webapp/components/angular-cookies/angular-cookies.js',
		  'main/webapp/components/angular-animate/angular-animate.min.js',
		  'main/webapp/components/angular-timer/dist/angular-timer.min.js',
		  'main/webapp/components/handlebars/handlebars.js',
		  'main/webapp/components/jquery/jquery.js',
		  'main/webapp/components/jquery-class/jquery.Class.js',
		  'main/webapp/components/gojs/go.js',
		  'main/webapp/components/underscore/underscore.js',
		  'main/webapp/scripts/extra/event-analytics.js',
		  'main/webapp/scripts/app.js',
		  'main/webapp/common/**/*.js',
		  'main/webapp/features/**/*.js',
		  'main/webapp/pages/**/*.js',
		  'test/webapp/**/*.js'
		],
		// list of files to exclude
		exclude: [
		  'main/webapp/common/help/*.js'
		],
		
		// test results reporter to use
		// possible values: dots || progress || growl
		reporters: ['progress'],
		
		// web server port
		port: 8099,
		
		// cli runner port
		runnerPort: 9100,
		
		// enable / disable colors in the output (reporters and logs)
		colors: true,
		
		// level of logging
		// possible values: LOG_DISABLE || LOG_ERROR || LOG_WARN || LOG_INFO || LOG_DEBUG
		logLevel: config.LOG_INFO,
		
		// enable / disable watching file and executing tests whenever any file changes
		autoWatch: true,
		
		// Start these browsers, currently available:
		// - Chrome
		// - ChromeCanary
		// - Firefox
		// - Opera
		// - Safari (only Mac)
		// - PhantomJS
		// - IE (only Windows)
		browsers: ['Chrome'],
		
		// If browser does not capture in given timeout [ms], kill it
		captureTimeout: 5000,
		
		// Continuous Integration mode
		// if true, it capture browsers, run tests and exit
		singleRun: false
	});
};