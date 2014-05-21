WebStorage Service for AngularJS
================================

The webStorage service has both a generic and direct API. The generic API will check for client support and preferred order before altering a specific storage value, trying to degrade gracefully according to a set heuristic. The direct APIs works with either the client's local, session or the module's own in-memory storage engines.

The selection heuristics for the generic API is mainly dictated by the order specified in the module constant `order` (defaults to `['local', 'session', 'memory']`.) If the client has no support for the specified storage engine then the service will try to fall back on the next specified engine and so forth.

NOTE: The in-memory storage should really be seen as a last resort since all its values will be lost on page reload (somewhat negating the whole idea of client web storage!)

If the client doesn't support local or session web storage the module will try to mimic them by setting cookies on the current document.

All errors will be broadcast via the $rootScope under the name specified in the module constant `errorName` (defaults to: `webStorage.notification.error`.)

The service provides the following generic methods:

`webStorage`
* `isSupported`     -- boolean flag indicating client support status (local or session storage)
* `add(key, value)` -- add a value to storage under the specific key (storage according to 'order')
* `get(key)`        -- return the specified value (storage according to 'order')
* `remove(key)`     -- remove a key/value pair from storage (storage according to 'order')
* `clear()`         -- remove all key/value pairs from storage (storage according to 'order')


It also provides the following direct APIs:

`webStorage.local`
* `isSupported`     -- boolean flag indicating client support status (local storage)
* `add(key, value)` -- add a value to storage under the specific key (local storage)
* `get(key)`        -- return the specified value (local storage)
* `remove(key)`     -- remove a key/value pair from storage (local storage)
* `clear()`         -- remove all key/value pairs from storage (local storage)

`webStorage.session`
* `isSupported`     -- boolean flag indicating client support status (session storage)
* `add(key, value)` -- add a value to storage under the specific key (session storage)
* `get(key)`        -- return the specified value (session storage)
* `remove(key)`     -- remove a key/value pair from storage (session storage)
* `clear()`         -- remove all key/value pairs from storage (session storage)

`webStorage.memory`
* `isSupported`     -- boolean true, the in-memory storage is always supported
* `add(key, value)` -- add a value to storage under the specific key (in-memory storage)
* `get(key)`        -- return the specified value (in-memory storage)
* `remove(key)`     -- remove a key/value pair from storage (in-memory storage)
* `clear()`         -- remove all key/value pairs from storage (in-memory storage)

## Author
Fredric Rylander, https://github.com/fredricrylander/angular-webstorage

## Date
2013-12-18

## Module Version
0.9.5

## Requirements
This module was built for AngularJS v1.0.5.

## Usage
Add `webStorageModule` to your app's dependencies. Then inject `webStorage` into any controller that needs to use it, e.g.:

    var myApp = angular.module('myApp', ['webStorageModule']);
    myApp.controller('myController', function ($scope, webStorage) { ... });

## License
    The MIT License
    Copyright (c) 2013 Fredric Rylander

    Permission is hereby granted, free of charge, to any person obtaining a
    copy of this software and associated documentation files (the "Software"),
    to deal in the Software without restriction, including without limitation
    the rights to use, copy, modify, merge, publish, distribute, sublicense,
    and/or sell copies of the Software, and to permit persons to whom the
    Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in
    all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
    THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
    FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
    IN THE SOFTWARE.
