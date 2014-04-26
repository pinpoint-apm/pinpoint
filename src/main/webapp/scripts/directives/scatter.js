'use strict';

pinpointApp.constant('scatterConfig', {
    get: {
        scatterData: '/getScatterData.pinpoint',
        lastScatterData: '/getLastScatterData.pinpoint'
    },
    useIntervalForFetching: false,
    nFetchingInterval: 2000,
    nFetchLimit: 5000,
    options: {
        sContainerId: '',
        nWidth: 400,
        nHeight: 250,
        nXMin: 0, nXMax: 1000,
        nYMin: 0, nYMax: 10000,
        nZMin: 0, nZMax: 5,
        nBubbleSize: 3,
        sXLabel: '',
        sYLabel: '(ms)',
        sTitle: '',
        'htGuideLine': {
            'nLineWidth': 1,
            'aLineDash': [1, 0],
            'nGlobalAlpha': 1,
            'sLineColor' : '#e3e3e3'
        },
        htTypeAndColor: {
            // type name : color
            'Success': '#2ca02c',
            // 'Warning' : '#f5d025',
            'Failed': '#d62728'
        },
        htOption: {
            headers: {
                accept: 'application/json'
            },
            dataType: 'json'
        },
        index: {
            x: 0,
            y: 1,
            transactionId: 2,
            type: 3
        },
        type: {
            '0': 'Failed',
            '1': 'Success'
        },
        'htCheckBoxImage': {
            'checked' : 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAOCAYAAAAfSC3RAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyJpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMC1jMDYxIDY0LjE0MDk0OSwgMjAxMC8xMi8wNy0xMDo1NzowMSAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENTNS4xIFdpbmRvd3MiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6ODk0MjRENUI2Qjk2MTFFM0E3NkNCRkIyQTkxMjZFQjMiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6ODk0MjRENUM2Qjk2MTFFM0E3NkNCRkIyQTkxMjZFQjMiPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDo4OTQyNEQ1OTZCOTYxMUUzQTc2Q0JGQjJBOTEyNkVCMyIgc3RSZWY6ZG9jdW1lbnRJRD0ieG1wLmRpZDo4OTQyNEQ1QTZCOTYxMUUzQTc2Q0JGQjJBOTEyNkVCMyIvPiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/PkJ02akAAAEfSURBVHjalJI/aoRQEMbnrU8RRVYsBAXJASxEtLGRXEDIJXKTpPcOHiDl1gsp1QvEIiD2AbGLmpm3f7Is68YMjOOb9/0Y/Rg2zzPEcTzDP6IsS7Y5QXTAsibFIH4BQVVVi1Mcx9liyTFN13W/+JpPI0iSpL1hGEHf9+E0TSBAxtifkGVZgSzLgBkMwwCbNZNOEIVpmo3v+78gigLMt+O/3IR0XW/yPH/uuu4AEqQoComeSIznhyUoDMP3cRwPoKZpLyjaqqoKJOacfy5B6Mc39QRYFMUrOtbQO4lt24Z70BlMkqSkSxJdmrMEiYiiSGwOrh6v6/oxTdMP6lGlM/Wv3RYMPY55hrMs292DKNn1kqOb4HketG0L5N5CsB8BBgCZjoUNsxfiYwAAAABJRU5ErkJggg==',
            'unchecked' : 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAOCAYAAAAfSC3RAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyJpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMC1jMDYxIDY0LjE0MDk0OSwgMjAxMC8xMi8wNy0xMDo1NzowMSAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENTNS4xIFdpbmRvd3MiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6OEQxQ0ZERUQ2Qjk2MTFFMzg5MjNGMjAzRjdCQ0FEMjkiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6OEQxQ0ZERUU2Qjk2MTFFMzg5MjNGMjAzRjdCQ0FEMjkiPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDo4RDFDRkRFQjZCOTYxMUUzODkyM0YyMDNGN0JDQUQyOSIgc3RSZWY6ZG9jdW1lbnRJRD0ieG1wLmRpZDo4RDFDRkRFQzZCOTYxMUUzODkyM0YyMDNGN0JDQUQyOSIvPiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/Pq1+Js8AAABTSURBVHjaYvz//z+DiYnJfwYSwJkzZxgZjY2NYZoYidQHVs9Eoia4WhY0JxDUBfQWA7KNRGlCVsfEQCYY1UhLjf9hEUtEAgAnOUZyEjlIH0CAAQDn/BlKI9rDJAAAAABJRU5ErkJggg=='
        },
        'sConfigImage': 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAOCAYAAAAfSC3RAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyJpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMC1jMDYxIDY0LjE0MDk0OSwgMjAxMC8xMi8wNy0xMDo1NzowMSAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENTNS4xIFdpbmRvd3MiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6QTI2NzMzRDI2QTlGMTFFM0E1RENBRjZGODkwRDBCMEIiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6QTI2NzMzRDM2QTlGMTFFM0E1RENBRjZGODkwRDBCMEIiPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDpBMjY3MzNEMDZBOUYxMUUzQTVEQ0FGNkY4OTBEMEIwQiIgc3RSZWY6ZG9jdW1lbnRJRD0ieG1wLmRpZDpBMjY3MzNEMTZBOUYxMUUzQTVEQ0FGNkY4OTBEMEIwQiIvPiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/Pn/ejkcAAAFwSURBVHjanFKxSsNQFE2CFEtaTNsMCbSQ4lA6GSghi2igo/o/GVz6AfmD/EFGJ7cKDiVLO0hFEByzpG1EA5LUxntC80jFKhi4LzfnnnPfzXmPz7KM+9ezT6iq6gnF+T4Nny88/110VK1WbwRB4OM4vgyC4PVXIQlW9JqRwGo2mzm2XC65zWYzplSnBo1CeFDu1Ov1XhaLhVWpVBimKAqXJInVarWmJGQ42xHjybIcQSSK4rNt29cgOI5jR1Gkk5gLw1DC2LkvWGBCu90eDwaDDOF53hXhhwjf908LHByYBo2ArqZpfnY6HbEYw3XdJ5riAzEajR4LHJxut6syhyhq5c6apt1hdER5EnCIKzFzqPM7fUwkSZrhf+r1+lmaphFqjUZuJIeaYRgT4q7ZqFvxmn7+GAQYBDcRyIGhBs6PN4dyjKLP5/OL4XA4RSAHhlpZs3OO1PF+W3ggwxQ6u7d+v3+7s9Nfd3VrQm3fXf0SYADyptv3yy4A0QAAAABJRU5ErkJggg==',
        'sDownloadImage': 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAOCAYAAAAfSC3RAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyJpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMC1jMDYxIDY0LjE0MDk0OSwgMjAxMC8xMi8wNy0xMDo1NzowMSAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENTNS4xIFdpbmRvd3MiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6RUU3MjQyMUQ2QTlGMTFFM0IxRTY4MjI3MUU5MUUyMzMiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6RUU3MjQyMUU2QTlGMTFFM0IxRTY4MjI3MUU5MUUyMzMiPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDpFRTcyNDIxQjZBOUYxMUUzQjFFNjgyMjcxRTkxRTIzMyIgc3RSZWY6ZG9jdW1lbnRJRD0ieG1wLmRpZDpFRTcyNDIxQzZBOUYxMUUzQjFFNjgyMjcxRTkxRTIzMyIvPiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/PlnySxAAAADPSURBVHjaxFLBDYMwDLQjMgEf4M8E4Y0YgzHaTTpGFuDfARAdoH3DiwmQSLmqRIZSpPLpSTH4krOjc9g5R0cQIDDzgozj2Ffrum6xOTcKtqolSUJCuNlR0UH8WTiZcpPGzEaB3xVWVXVO0/QhOeTgP1rKOU7/QdM0RZ7nd2OMwxc5eHkeixEm+71aKUVhGJLWmoZhoL7vaRxHX7xtW/ZzlHOTgDiKIirL8oTcWnuZ914dsyzbfXfoCuAmdV3z15ezBgRr8Nuc4ocRXhGeAgwAFHJVgfQ6KdUAAAAASUVORK5CYII=',
        'sFullScreenImage': 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAA4AAAAOCAYAAAAfSC3RAAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAAyJpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuMC1jMDYxIDY0LjE0MDk0OSwgMjAxMC8xMi8wNy0xMDo1NzowMSAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENTNS4xIFdpbmRvd3MiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6QkMwOTc3QTg2QTlGMTFFMzk5MzhBOTM4OEFCMzg3MTciIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6QkMwOTc3QTk2QTlGMTFFMzk5MzhBOTM4OEFCMzg3MTciPiA8eG1wTU06RGVyaXZlZEZyb20gc3RSZWY6aW5zdGFuY2VJRD0ieG1wLmlpZDpCQzA5NzdBNjZBOUYxMUUzOTkzOEE5Mzg4QUIzODcxNyIgc3RSZWY6ZG9jdW1lbnRJRD0ieG1wLmRpZDpCQzA5NzdBNzZBOUYxMUUzOTkzOEE5Mzg4QUIzODcxNyIvPiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/PvZjiPIAAAE/SURBVHjanFKxaoRAEHXhbM4UopViGWzv0C5VUvsJqdP7A/6Dfb7DMmCVSuRCunClaKUYSKUQM2+zY1SsMjDuuDtv9u3ME9M0af+xAz6u687opmnENslxnFX1uq6FhhvDMKRluofvVeczuMr9vREmhMhRmRy/F3IukhOjM7Mh4B9VNkqQq67rR9u25VnbtsdxHPkZ6zcWRfFAN2qGYVzx3/e9X1XVC2LLsnzTNK8MQK5kCL4AwcqylPTiOH4m8C1igNI0fUIcBEGu3rwGRlFkK3qvRM9XtD+I9h3iLMvaXaDneRdF78T0cHPXdW+Iif6ZgavmALClB9q0nBRw3RyMAa0mWnJzGIbvJEneOeb9hRgEK0e2mjtG9kX+qeJH8hs163lkh2UlVWAruYIlp8ShzeNYqEQqaE9ym638R4ABADZiqF446UJLAAAAAElFTkSuQmCC'
    }
});

// FIXME child window에서 접근할 수 있도록 global변수로 일단 빼둠. 나중에 리팩토링할 것.
//var selectdTracesBox = {};

pinpointApp.directive('scatter',
    [ 'scatterConfig', '$rootScope', '$timeout', 'webStorage', 'TransactionDao', '$window',
        function (cfg, $rootScope, $timeout, webStorage, oTransactionDao, $window) {
            return {
//                template: '<div class="scatter"></div>',
                restrict: 'EA',
//                replace: true,
                link: function (scope, element, attrs) {

                    // define private variables
                    var oNavbarVo, htScatterSet, htLastNode;

                    // define private variables of methods
                    var getDataSource, makeScatter, showScatter, showScatterBy, pauseScatterAll, makeScatterWithData;

                    // initialize
                    oNavbarVo = null;
                    htScatterSet = {};
                    htLastNode = {};

                    /**
                     * get data source
                     * @param applicationName
                     * @param from
                     * @param to
                     * @param period
                     * @param filter
                     */
                    getDataSource = function (applicationName, from, to, filter) {
                        var bDrawOnceAll = false;
                        var htDataSource = {
                            sUrl: function (nFetchIndex) {
                                return cfg.get.scatterData;
                            },
                            htParam: function (nFetchIndex, htLastFetchParam, htLastFetchedData) {
                                // calculate parameter
                                var htData;
                                if (nFetchIndex === 0) {
                                    htData = {
                                        'application': applicationName,
                                        'from': from,
                                        'to': to,
                                        'limit': cfg.nFetchLimit,
                                        'v': 2
                                    };
                                } else {
                                    htData = {
                                        'application': applicationName,
                                        // array[0] 이 최근 값, array[len]이 오래된 이다.
                                        'from': from,
                                        'to': htLastFetchedData.resultFrom - 1,
                                        'limit': cfg.nFetchLimit,
                                        'v': 2
                                    };
                                }
                                if (filter) {
                                    htData.filter = filter;
                                }

                                return htData;
                            },
                            nFetch: function (htLastFetchParam, htLastFetchedData) {
                                // -1 : stop, n = 0 : immediately, n > 0 : interval
                                if (htLastFetchedData.resultFrom - 1 > from) {
                                    if (cfg.useIntervalForFetching) {
                                        bDrawOnceAll = true;
                                        return cfg.nFetchingInterval;
                                    }
                                    // TO THE NEXT
                                    return 0;
                                }

                                // STOP
                                return -1;
                            },
                            htOption: {
                                headers: {
                                    accept: 'application/json'
                                },
                                dataType: 'json'
                            },
                            index: {
                                x: 0,
                                y: 1,
                                transactionId: 2,
                                type: 3
                            },
                            type: {
                                '0': 'Failed',
                                '1': 'Success'
                            }
                        };
                        return htDataSource;
                    };

                    /**
                     * make scatter
                     * @param title
                     * @param start
                     * @param end
                     * @param period
                     * @param filter
                     * @param w
                     * @param h
                     * @param scatterData
                     */
                    makeScatter = function (target, title, start, end, period, filter, w, h, scatterData) {
                        if (!Modernizr.canvas) {
                            alert("Can't draw scatter. Not supported browser.");
                        }

                        var options = angular.copy(cfg.options);
                        options.sContainerId = target;
                        options.nWidth = w ? w : 400;
                        options.nHeight = h ? h : 250;
                        options.nXMin = start;
                        options.nXMax = end;
//                        options.sTitle = title;
                        options.fOnSelect = function (htPosition, htXY) {
//                            var transactions = {
//                                htXY: htXY,
//                                aTraces: []
//                            };
                            
                            // by netspider
//                            console.log(target, title, start, end, period, filter, w, h);
//                            var NEW_URL = "/transactionmetadata2.pinpoint?application=" + title;
//                            NEW_URL += "&from=" + htXY.nXFrom;
//                            NEW_URL += "&to=" + htXY.nXTo;
//							NEW_URL += "&responseFrom="+ htXY.nYFrom;
//							NEW_URL += "&responseTo=" + htXY.nYTo;
//							NEW_URL += "&limit=3";
//							if (filter) {
//								NEW_URL += "&filter=" + filter;
//							}
                            // var ww = window.open(NEW_URL);
                            // end

//                            transactions.aTraces = this.getDataByXY(htXY.nXFrom, htXY.nXTo, htXY.nYFrom, htXY.nYTo);
//                            if (transactions.aTraces.length === 0) {
//                                return;
//                            }
                            if (!this.hasDataByXY(htXY.nXFrom, htXY.nXTo, htXY.nYFrom, htXY.nYTo)) {
                                return;
                            }

//                            var token = 'transactionsFromScatter_' + _.random(100000, 999999);
//                            webStorage.session.add(token, transactions);
//                            window[token] = transactions;
//                            window.open("/selectedScatter.pinpoint", token);
//                            oTransactionDao.addData(token, transactions);

                            var token = htLastNode.text + '|' + htXY.nXFrom + '|' + htXY.nXTo + '|' + htXY.nYFrom + '|' + htXY.nYTo;
                            $window.open('#/transactionList/' + oNavbarVo.getApplication() + '/' +
                                oNavbarVo.getPeriod() + '/' + oNavbarVo.getQueryEndTime(), token);
                        };
                        options.fFullScreenMode = function () {
                            var url = '#/scatterFullScreenMode/' + htLastNode.text + '@' + htLastNode.serviceTypeCode + '/' +
                                oNavbarVo.getPeriod() + '/' + oNavbarVo.getQueryEndTime();
                            if (oNavbarVo.getFilter()) {
                                url += '/' + oNavbarVo.getFilter();
                            }
                            $window.open(url, "width=900, height=700, resizable=yes");
                        };

                        var oScatterChart = null;
                        oScatterChart = new BigScatterChart(options);
                        $timeout(function () {
                            if (angular.isUndefined(scatterData)) {
                                oScatterChart.drawWithDataSource(getDataSource(title, start, end, filter));
                            } else {
                                oScatterChart.addBubbleAndMoveAndDraw(scatterData.scatter, scatterData.resultFrom);
                            }
                            $window.htoScatter[htLastNode.text] = oScatterChart;
                        }, 100);

                        return oScatterChart;
                    };

                    /**
                     * show scatter
                     * @param title
                     * @param start
                     * @param end
                     * @param period
                     * @param filter
                     * @param w
                     * @param h
                     * @todo 현재는 캐싱 처리만 해놓았다. 추후 scatter의 data를 pause/resume 기능을 넣어서 서버의 부하를 줄울 수 있다.
                     * 코드 수정은 BigScatterChart.js 에서 pause/resume 메소드를 지원하고, 아래 메소드에서 가져다 쓰면 될 것 같다.
                     */
                    showScatter = function (title, start, end, period, filter, w, h) {
                        element.children().hide();
                        pauseScatterAll();
                        if (angular.isDefined(htScatterSet[title])) {
                            htScatterSet[title].target.show();
                            htScatterSet[title].scatter.resume();
                        } else {
                            var target = angular.element('<div class="scatter">');
                            var oScatter = makeScatter(target, title, start, end, period, filter, w, h);
                            htScatterSet[title] = {
                                target : target,
                                scatter : oScatter
                            };
                            element.append(target);
                        }
                    };

                    /**
                     * show scatter by
                     * @param title
                     */
                    showScatterBy = function (title) {
                        element.children().hide();
                        if (angular.isDefined(htScatterSet[title])) {
                            htScatterSet[title].target.show();
                        }
                    };

                    /**
                     * make scatter with data
                     * @param title
                     * @param start
                     * @param end
                     * @param period
                     * @param filter
                     * @param w
                     * @param h
                     * @param data
                     */
                    makeScatterWithData = function (title, start, end, period, filter, w, h, data) {
                        if (angular.isDefined(htScatterSet[title])) {
                            htScatterSet[title].scatter.addBubbleAndMoveAndDraw(data.scatter, data.resultFrom);
                        } else {
                            var target = angular.element('<div class="scatter">');
                            var oScatter = makeScatter(target, title, start, end, period, filter, w, h, data);
                            htScatterSet[title] = {
                                target : target,
                                scatter : oScatter
                            };
                            element.append(target);
                            target.hide();
                        }
                    };

                    /**
                     * pause scatter all
                     */
                    pauseScatterAll = function () {
                        angular.forEach(htScatterSet, function (scatterSet, key) {
                            scatterSet.scatter.pause();
                        });
                    };

                    /**
                     * scope event on scatter.initialize
                     */
                    scope.$on('scatter.initialize', function (event, navbarVo) {
                        oNavbarVo = navbarVo;
                        htScatterSet = {};
                        element.empty();
                    });

                    /**
                     * scope event on scatter.initializeWithNode
                     */
                    scope.$on('scatter.initializeWithNode', function (event, node, w, h) {
                        htLastNode = node;
                        showScatter(node.text, oNavbarVo.getQueryStartTime(),
                            oNavbarVo.getQueryEndTime(), oNavbarVo.getQueryPeriod(), oNavbarVo.getFilter(), w, h);
                    });

                    /**
                     * scope event on scatter.initializeWithData
                     */
                    scope.$on('scatter.initializeWithData', function (event, applicationName, data) {
                        makeScatterWithData(applicationName, oNavbarVo.getQueryStartTime(),
                            oNavbarVo.getQueryEndTime(), oNavbarVo.getQueryPeriod(), oNavbarVo.getFilter(), null, null, data);
                    });

                    /**
                     * scope event on scatter.showByNode
                     */
                    scope.$on('scatter.showByNode', function (event, node) {
                        htLastNode = node;
                        showScatterBy(node.id);
                    });
                }
            };
        } ]);
