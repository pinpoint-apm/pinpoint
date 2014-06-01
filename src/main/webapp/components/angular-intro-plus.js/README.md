angular-intro-plus.js
================

An angularjs directive that wraps [intro.js](http://usablica.github.io/intro.js/) functionality. and developed from [angular-intro.js](https://github.com/mendhak/angular-intro.js).

![angularintroplus](https://lh5.googleusercontent.com/-9PWGXS-eL7A/U4nEnun3i1I/AAAAAAAAx-0/4BsOD3JC7aw/w713-h417-no/%25E1%2584%2589%25E1%2585%25B3%25E1%2584%258F%25E1%2585%25B3%25E1%2584%2585%25E1%2585%25B5%25E1%2586%25AB%25E1%2584%2589%25E1%2585%25A3%25E1%2586%25BA+2014-05-31+%25E1%2584%258B%25E1%2585%25A9%25E1%2584%2592%25E1%2585%25AE+8.33.20.png)
![angularintroplus](https://lh4.googleusercontent.com/-YvCjzN3W1WE/U4nEnlUiE5I/AAAAAAAAx-w/fBZyPNR3mRo/w717-h417-no/%25E1%2584%2589%25E1%2585%25B3%25E1%2584%258F%25E1%2585%25B3%25E1%2584%2585%25E1%2585%25B5%25E1%2586%25AB%25E1%2584%2589%25E1%2585%25A3%25E1%2586%25BA+2014-05-31+%25E1%2584%258B%25E1%2585%25A9%25E1%2584%2592%25E1%2585%25AE+8.33.12.png)


See [the demo page](http://angular-intro.iamdenny.com/) for an overview.


## Details

The two main directives are `ng-intro-plus-options` and `ng-intro-plus-show`.

`ng-intro-plus-options` needs to point at a `$scope` object which contains the intro.js options. The options are exactly the same as [the original](https://github.com/usablica/intro.js#options).  This also allows you to modify the options as part of your controller behavior if necessary.

`ng-intro-plus-show` is a method name that you want to use later.  In other words, put any name in there that doesn't exist on the `$scope` already.  The directive will create a method with that name so that you can call it yourself later.

For example, if you set `ng-intro-plus-show="CallMe"`, then you can later call `ng-click="CallMe();"` as long as you are still in the same controller scope.

To start the intro from code, either call `$scope.CallMe();` or set `ng-intro-plus-autostart="true"`.  If the `$scope.CallMe();` doesn't work, it might be because your DOM isn't ready. Put it in a `$timeout`.

There are also directives that link to the intro.js callbacks, namely `ng-intro-plus-oncomplete`, `ng-intro-plus-onexit`, `ng-intro-plus-onchange` `ng-intro-plus-onbeforechange` and `ng-intro-plus-onafterchange`.


## License

As with intro.js and angular-intro.js, this is under the [MIT license](https://github.com/iamdenny/angular-intro-plus.js/blob/master/LICENSE).






