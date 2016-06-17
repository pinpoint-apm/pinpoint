
module.exports = function( grunt, options ) {
	var now = Date.now();
	return {
		css: {
			src: [ options.RESOURCE_PATH.INDEX_HTML ],
				actions: [{
				name: 'vendor.css',
				search: /<!--###vendor\.css[^#]*vendor\.css###-->/gm,
				replace: '<link rel="stylesheet" href="lib/css/vendor.css?v=' + now + '">'
			},{
				name: 'pinpoint.css',
				search: /<!--###pinpoint\.css[^#]*pinpoint\.css###-->/gm,
				replace: '<link rel="stylesheet" href="lib/css/pinpoint.css?v=' + now + '">'
			}]
		},
		js: {
			src: [ options.RESOURCE_PATH.INDEX_HTML ],
				actions: [{
				name: 'base.js',
				search: /<!--###base\-lib\.min\.js[^#]*base\-lib\.min\.js###-->/gm,
				replace: '<script src="lib/js/base-lib.min.js?v=' + now + '"></script>'
			},{
				name: 'angular.js',
				search: /<!--###angular-lib\.min\.js[^#]*angular-lib\.min\.js###-->/gm,
				replace: '<script src="lib/js/angular-lib.min.js?v=' + now + '"></script>'
			},{
				name: 'draw.js',
				search: /<!--###draw-lib\.min\.js[^#]*draw-lib\.min\.js###-->/gm,
				replace: '<script src="lib/js/draw-lib.min.js?v=' + now + '"></script>'
			},{
				name: 'util.js',
				search: /<!--###util-lib\.min\.js[^#]*util-lib\.min\.js###-->/gm,
				replace: '<script src="lib/js/util-lib.min.js?v=' + now + '"></script>'
			},{
				name: 'pinpoint-component.js',
				search: /<!--###pinpoint-component\.min\.js[^#]*pinpoint-component\.min\.js###-->/gm,
				replace: '<script src="lib/js/pinpoint-component.min.js?v=' + now + '"></script>'
			},{
				name: 'pinpoint.js',
				search: /<!--###pinpoint\.min\.js[^#]*pinpoint\.min\.js###-->/gm,
				replace: '<script src="lib/js/pinpoint.min.js?v=' + now + '"></script>'
			}]
		},
		html: {
			src: [ options.RESOURCE_PATH.INDEX_HTML ],
			actions: [{
				name: 'build.time',
				search: /\$\{buildTime\}/gm,
				replace: now + ''
			}]
		}
	};
};