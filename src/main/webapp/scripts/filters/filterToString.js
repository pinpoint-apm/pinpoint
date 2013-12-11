'use strict';

pinpointApp.filter('filterToString', [ '$sce', function ($sce) {

    var checkInputData = function (input) {
        return angular.isDefined(input) && angular.isString(input);
    };

    var parseToMultiFilter = function (input) {
        return input.split('^');
    };

    var splitFilter = function (filter) {
        return filter.split('|');
    };

    var checkFilter = function (filter) {
        return angular.isArray(filter) && filter.length >= 4;
    };

    var parseToHtml = function (filter) {
        var html = "<li>" + filter[1] + "(" + filter[0] + ") <i class='icon-arrow-right'></i> " + filter[3] + "(" + filter[2] + ")";
        if (filter[4]) {
            if (filter[4] === 'error' || filter[4].indexOf(',9999999999') > 0) {
                html += " [" + filter[4].replace(',9999999999', 'ms ~') + "]";
            } else if (filter[4].indexOf(',') > 0) {
                var splitValue = filter[4].split(',');
                html += " [" + splitValue[0] + ' ~ ' + splitValue[1] + 'ms]';
            }
        }
        html += "</li>";
        return html;
    };

    return function (input) {
        if (checkInputData(input) === false) {
            return '';
        }

        var multiFilter = parseToMultiFilter(input);
        if (multiFilter.length === 0) {
            return '';
        }

        var html = '';

        angular.forEach(multiFilter, function (val, key) {
            var splitedFilter = splitFilter(val);
            if (checkFilter(splitedFilter) === false) {
                return;
            }

            html += parseToHtml(splitedFilter);
        });
        return $sce.trustAsHtml(html);
    };
}]);
