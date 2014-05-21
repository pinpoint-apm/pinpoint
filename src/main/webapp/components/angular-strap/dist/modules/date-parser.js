/**
 * angular-strap
 * @version v2.0.2 - 2014-04-27
 * @link http://mgcrea.github.io/angular-strap
 * @author Olivier Louvignes (olivier@mg-crea.com)
 * @license MIT License, http://www.opensource.org/licenses/MIT
 */
'use strict';
angular.module('mgcrea.ngStrap.helpers.dateParser', []).provider('$dateParser', [
  '$localeProvider',
  function ($localeProvider) {
    var proto = Date.prototype;
    function isNumeric(n) {
      return !isNaN(parseFloat(n)) && isFinite(n);
    }
    var defaults = this.defaults = {
        format: 'shortDate',
        strict: false
      };
    this.$get = [
      '$locale',
      function ($locale) {
        var DateParserFactory = function (config) {
          var options = angular.extend({}, defaults, config);
          var $dateParser = {};
          var regExpMap = {
              'sss': '[0-9]{3}',
              'ss': '[0-5][0-9]',
              's': options.strict ? '[1-5]?[0-9]' : '[0-9]|[0-5][0-9]',
              'mm': '[0-5][0-9]',
              'm': options.strict ? '[1-5]?[0-9]' : '[0-9]|[0-5][0-9]',
              'HH': '[01][0-9]|2[0-3]',
              'H': options.strict ? '1?[0-9]|2[0-3]' : '[01]?[0-9]|2[0-3]',
              'hh': '[0][1-9]|[1][012]',
              'h': options.strict ? '[1-9]|1[012]' : '0?[1-9]|1[012]',
              'a': 'AM|PM',
              'EEEE': $locale.DATETIME_FORMATS.DAY.join('|'),
              'EEE': $locale.DATETIME_FORMATS.SHORTDAY.join('|'),
              'dd': '0[1-9]|[12][0-9]|3[01]',
              'd': options.strict ? '[1-9]|[1-2][0-9]|3[01]' : '0?[1-9]|[1-2][0-9]|3[01]',
              'MMMM': $locale.DATETIME_FORMATS.MONTH.join('|'),
              'MMM': $locale.DATETIME_FORMATS.SHORTMONTH.join('|'),
              'MM': '0[1-9]|1[012]',
              'M': options.strict ? '[1-9]|1[012]' : '0?[1-9]|1[012]',
              'yyyy': '[1]{1}[0-9]{3}|[2]{1}[0-9]{3}',
              'yy': '[0-9]{2}',
              'y': options.strict ? '-?(0|[1-9][0-9]{0,3})' : '-?0*[0-9]{1,4}'
            };
          var setFnMap = {
              'sss': proto.setMilliseconds,
              'ss': proto.setSeconds,
              's': proto.setSeconds,
              'mm': proto.setMinutes,
              'm': proto.setMinutes,
              'HH': proto.setHours,
              'H': proto.setHours,
              'hh': proto.setHours,
              'h': proto.setHours,
              'dd': proto.setDate,
              'd': proto.setDate,
              'a': function (value) {
                var hours = this.getHours();
                return this.setHours(value.match(/pm/i) ? hours + 12 : hours);
              },
              'MMMM': function (value) {
                return this.setMonth($locale.DATETIME_FORMATS.MONTH.indexOf(value));
              },
              'MMM': function (value) {
                return this.setMonth($locale.DATETIME_FORMATS.SHORTMONTH.indexOf(value));
              },
              'MM': function (value) {
                return this.setMonth(1 * value - 1);
              },
              'M': function (value) {
                return this.setMonth(1 * value - 1);
              },
              'yyyy': proto.setFullYear,
              'yy': function (value) {
                return this.setFullYear(2000 + 1 * value);
              },
              'y': proto.setFullYear
            };
          var regex, setMap;
          $dateParser.init = function () {
            $dateParser.$format = $locale.DATETIME_FORMATS[options.format] || options.format;
            regex = regExpForFormat($dateParser.$format);
            setMap = setMapForFormat($dateParser.$format);
          };
          $dateParser.isValid = function (date) {
            if (angular.isDate(date))
              return !isNaN(date.getTime());
            return regex.test(date);
          };
          $dateParser.parse = function (value, baseDate) {
            if (angular.isDate(value))
              return value;
            var matches = regex.exec(value);
            if (!matches)
              return false;
            var date = baseDate || new Date(0, 0, 1);
            for (var i = 0; i < matches.length - 1; i++) {
              setMap[i] && setMap[i].call(date, matches[i + 1]);
            }
            return date;
          };
          // Private functions
          function setMapForFormat(format) {
            var keys = Object.keys(setFnMap), i;
            var map = [], sortedMap = [];
            // Map to setFn
            var clonedFormat = format;
            for (i = 0; i < keys.length; i++) {
              if (format.split(keys[i]).length > 1) {
                var index = clonedFormat.search(keys[i]);
                format = format.split(keys[i]).join('');
                if (setFnMap[keys[i]])
                  map[index] = setFnMap[keys[i]];
              }
            }
            // Sort result map
            angular.forEach(map, function (v) {
              sortedMap.push(v);
            });
            return sortedMap;
          }
          function escapeReservedSymbols(text) {
            return text.replace(/\//g, '[\\/]').replace('/-/g', '[-]').replace(/\./g, '[.]').replace(/\\s/g, '[\\s]');
          }
          function regExpForFormat(format) {
            var keys = Object.keys(regExpMap), i;
            var re = format;
            // Abstract replaces to avoid collisions
            for (i = 0; i < keys.length; i++) {
              re = re.split(keys[i]).join('${' + i + '}');
            }
            // Replace abstracted values
            for (i = 0; i < keys.length; i++) {
              re = re.split('${' + i + '}').join('(' + regExpMap[keys[i]] + ')');
            }
            format = escapeReservedSymbols(format);
            return new RegExp('^' + re + '$', ['i']);
          }
          $dateParser.init();
          return $dateParser;
        };
        return DateParserFactory;
      }
    ];
  }
]);