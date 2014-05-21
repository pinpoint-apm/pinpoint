/**
 * angular-strap
 * @version v2.0.2 - 2014-04-27
 * @link http://mgcrea.github.io/angular-strap
 * @author Olivier Louvignes (olivier@mg-crea.com)
 * @license MIT License, http://www.opensource.org/licenses/MIT
 */
'use strict';
angular.module('mgcrea.ngStrap.helpers.debounce', []).constant('debounce', function (func, wait, immediate) {
  var timeout, args, context, timestamp, result;
  return function () {
    context = this;
    args = arguments;
    timestamp = new Date();
    var later = function () {
      var last = new Date() - timestamp;
      if (last < wait) {
        timeout = setTimeout(later, wait - last);
      } else {
        timeout = null;
        if (!immediate)
          result = func.apply(context, args);
      }
    };
    var callNow = immediate && !timeout;
    if (!timeout) {
      timeout = setTimeout(later, wait);
    }
    if (callNow)
      result = func.apply(context, args);
    return result;
  };
}).constant('throttle', function (func, wait, options) {
  var context, args, result;
  var timeout = null;
  var previous = 0;
  options || (options = {});
  var later = function () {
    previous = options.leading === false ? 0 : new Date();
    timeout = null;
    result = func.apply(context, args);
  };
  return function () {
    var now = new Date();
    if (!previous && options.leading === false)
      previous = now;
    var remaining = wait - (now - previous);
    context = this;
    args = arguments;
    if (remaining <= 0) {
      clearTimeout(timeout);
      timeout = null;
      previous = now;
      result = func.apply(context, args);
    } else if (!timeout && options.trailing !== false) {
      timeout = setTimeout(later, remaining);
    }
    return result;
  };
});