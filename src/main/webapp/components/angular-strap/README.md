# [AngularStrap](http://mgcrea.github.io/angular-strap) [![Build Status](https://secure.travis-ci.org/mgcrea/angular-strap.png?branch=master)](http://travis-ci.org/#!/mgcrea/angular-strap) [![Dependency Status](https://gemnasium.com/mgcrea/angular-strap.png)](https://gemnasium.com/mgcrea/angular-strap) [![Coverage Status](https://coveralls.io/repos/mgcrea/angular-strap/badge.png?branch=master)](https://coveralls.io/r/mgcrea/angular-strap?branch=master) [![Analytics](https://ga-beacon.appspot.com/UA-1813303-10/angular-strap/readme?pixel)](https://github.com/igrigorik/ga-beacon)

[![Banner](http://mgcrea.github.io/angular-strap/images/6af654d7.snippet.png)](http://mgcrea.github.io/angular-strap)

AngularStrap is a set of native directives that enables seamless integration of [Twitter Bootstrap 3.0+](https://github.com/twbs/bootstrap) into your [AngularJS 1.2.9+](https://github.com/angular/angular.js) app.

- The only required dependency is [Twitter Bootstrap CSS Styles](https://github.com/twbs/bootstrap/blob/master/dist/css/bootstrap.css)!

>
AngularStrap was initially written to provide AngularJS wrapping directives for Twitter Bootstrap. It used to leverage the javascript code written by Bootstrap's contributors to minimize work, retro-compatibility issues & time to market.
>
While it worked pretty well, it required a big javascript payload: both jQuery & Twitter Bootstrap libraries. When the 1.2 release of AngularJS showed up with the ngAnimate module, greatly simplifying DOM manipulation, we knew it was time for a rewrite!

## Documentation and examples

+ Check the [documentation](http://mgcrea.github.io/angular-strap) and [changelog](https://github.com/mgcrea/angular-strap/releases).



## Quick start

+ Include the required libraries (`ngAnimate` can be [mocked out](https://github.com/mgcrea/angular-strap/wiki/angular-animate-mock.js), only the `$$animateReflow` service is used).

>
``` html
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.2.10/angular.min.js"></script>
<script src="//ajax.googleapis.com/ajax/libs/angularjs/1.2.10/angular-animate.min.js"></script>
<script src="//rawgithub.com/mgcrea/angular-strap/master/dist/angular-strap.min.js"></script>
<script src="//rawgithub.com/mgcrea/angular-strap/master/dist/angular-strap.tpl.min.js"></script>
```


+ Inject the `ngStrap` module into your app

>
``` javascript
angular.module('myApp', ['mgcrea.ngStrap']);
```


## Developers

Clone the repo, `git clone git://github.com/mgcrea/angular-strap.git`, [download the latest release](https://github.com/mgcrea/angular-strap/zipball/master) or install with bower `bower install angular-strap#~2.0.0 --save`.

AngularStrap is tested with `karma` against the latest stable release of AngularJS.

>
	$ npm install grunt-cli --global
	$ npm install --dev
	$ grunt test

You can build the latest version using `grunt`.

>
	$ grunt build

You can quickly hack around (the docs) with:

>
	$ grunt serve



## Contributing

Please submit all pull requests the against master branch. If your unit test contains JavaScript patches or features, you should include relevant unit tests. Thanks!



## Authors

**Olivier Louvignes**

+ http://olouv.com
+ http://github.com/mgcrea



## Copyright and license

	The MIT License

	Copyright (c) 2012 - 2014 Olivier Louvignes

	Permission is hereby granted, free of charge, to any person obtaining a copy
	of this software and associated documentation files (the "Software"), to deal
	in the Software without restriction, including without limitation the rights
	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
	copies of the Software, and to permit persons to whom the Software is
	furnished to do so, subject to the following conditions:

	The above copyright notice and this permission notice shall be included in
	all copies or substantial portions of the Software.

	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
	THE SOFTWARE.
