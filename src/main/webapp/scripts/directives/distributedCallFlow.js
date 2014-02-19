'use strict';

pinpointApp.directive('distributedCallFlow', [ '$filter',
    function ($filter) {
        return {
            restrict: 'E',
            replace: true,
            templateUrl: 'views/distributedCallFlow.html',
            scope : {
                namespace : '@' // string value
            },
            link: function postLink(scope, element, attrs) {


                // initialize variables
                var grid, columns, options, dataView, lastAgent;

                // initialize variables of methods
                var initialize, treeFormatter, treeFilter, parseData, execTimeFormatter, numberFormatter,
                    getColorByString, progressBarFormatter;

                // bootstrap
                window.callStacks = [];

                getColorByString = function(str) {
                    // str to hash
                    for (var i = 0, hash = 0; i < str.length; hash = str.charCodeAt(i++) + ((hash << 5) - hash));
                    // int/hash to hex
                    for (var i = 0, colour = "#"; i < 3; colour += ("00" + ((hash >> i++ * 8) & 0xFF).toString(16)).slice(-2));
                    return colour;
                };

                treeFormatter = function (row, cell, value, columnDef, dataContext) {
                    var html = [];

                    value = value.replace(/&/g,"&amp;").replace(/</g,"&lt;").replace(/>/g,"&gt;");
                    var item = dataView.getItemById(dataContext.id);
                    lastAgent = item.agent ? item.agent : lastAgent;

                    var leftBarColor = getColorByString(lastAgent),
                        idx = dataView.getIdxById(dataContext.id);
                    html.push("<div style='position:absolute;top:0;left:0;bottom:0;width:5px;background-color:"+leftBarColor+"'></div>");
                    html.push("<span style='display:inline-block;height:1px;width:" + (15 * dataContext["indent"]) + "px'></span>");

                    if (window.callStacks[idx + 1] && window.callStacks[idx + 1].indent > window.callStacks[idx].indent) {
                        if (dataContext._collapsed) {
                            html.push(" <span class='toggle expand'></span>&nbsp;");
                        } else {
                            html.push(" <span class='toggle collapse'></span>&nbsp;");
                        }
                    } else {
                        html.push(" <span class='toggle'></span>&nbsp;");
                    }

                    if (item.hasException) {
                        html.push('<span class="glyphicon glyphicon-fire"></span>&nbsp;');
                    } else if (!item.isMethod) {
                        html.push('<span class="glyphicon glyphicon-info-sign"></span>&nbsp;');
                    }

                    html.push(value);

                    return html.join('');
                };

                treeFilter = function (item) {
                    var result = true;
                    if (item.parent != null) {
                        var parent = window.callStacks[item.parent];
                        while (parent) {

                            if (parent._collapsed) {
                                result = false;
                            }
                            parent = window.callStacks[parent.parent];
                        }
                    }
                    return result;
                };

                execTimeFormatter = function (row, cell, value, columnDef, dataContext) {
                    return $filter('date')(value, 'HH:mm:ss sss');
                };

                numberFormatter = function (row, cell, value, columnDef, dataContext) {
                    return $filter('number')(value);
                };

                progressBarFormatter = function (row, cell, value, columnDef, dataContext) {
                    if (value == null || value === "" || value == 0) {
                        return "";
                    }
                    var color;
                    if (value < 30) {
                        color = "#B0E3F1";
                    } else if (value < 70) {
                        color = "#81CFE5";
                    } else {
                        color = "#5bc0de";
                    }
                    return "<span class='percent-complete-bar' style='background:" + color + ";width:" + value + "%'></span>";
                };

                columns = [
                    {id: "method", name: "Method", field: "method", width: 400, formatter: treeFormatter},
                    {id: "argument", name: "Argument", field: "argument", width: 300},
                    {id: "exec-time", name: "Exec Time", field: "execTime", width: 90, formatter: execTimeFormatter},
                    {id: "gap-ms", name: "Gap(ms)", field: "gapMs", width: 60, cssClass: "right-align"},
                    {id: "time-ms", name: "Time(ms)", field: "timeMs", width: 60, cssClass: "right-align"},
                    {id: "time-per", name: "Time(%)", field: "timePer", width: 100, formatter: progressBarFormatter},
                    {id: "class", name: "Class", field: "class", width: 120},
                    {id: "api-type", name: "Api Type", field: "apiType", width: 90},
                    {id: "agent", name: "Agent", field: "agent", width: 130},
                    {id: "application-name", name: "Application Name", field: "applicationName", width: 150}
                ];

                options = {
                    enableCellNavigation: true,
                    enableColumnReorder: false
                };

                parseData = function(index, callStacks) {
                    var result = [],
                        barRatio = 100 / (callStacks[0][index.end] - callStacks[0][index.begin]);
                    angular.forEach(callStacks, function (val, key) {
                        result.push({
                            id: 'id_' + key,
                            parent: val[index['parentId']] ? val[index['parentId']] - 1 : null,
                            indent: val[index['tab']],
                            method: val[index['title']],
                            argument: val[index['arguments']],
                            execTime: val[index['begin']] > 0 ? val[index['begin']] : null,
                            gapMs: val[index['gap']],
                            timeMs: val[index['elapsedTime']],
                            timePer: val[index['elapsedTime']] ? ((val[index['end']] - val[index['begin']]) * barRatio) + 0.9 : null,
                            class: val[index['simpleClassName']],
                            apiType: val[index['apiType']],
                            agent: val[index['agent']],
                            applicationName: val[index['applicationName']],
                            hasException: val[index['hasException']],
                            isMethod: val[index['isMethod']]
                        });
                    });
                    return result;
                };

                /**
                 * initialize
                 * @param t transactionDetail
                 */
                initialize = function (t) {
                    window.callStacks = parseData(t.callStackIndex, t.callStack);
                    // initialize the model

                    dataView = new Slick.Data.DataView({ inlineFilters: true });
                    dataView.beginUpdate();
                    dataView.setItems(window.callStacks);
                    dataView.setFilter(treeFilter);
//                    dataView.getItemMetadata = function (row) {
//                        var item = dataView.getItemByIdx(row);
//                        if (!item.execTime) {
//                            return {
//                                "columns": {
//                                    2: {
//                                        "colspan": "*"
//                                    }
//                                }
//                            };
//                        }
//                    };
                    dataView.endUpdate();

                    grid = new Slick.Grid(element.get(0), dataView, columns, options);

                    grid.onClick.subscribe(function (e, args) {
                        if ($(e.target).hasClass("toggle")) {
                            var item = dataView.getItem(args.row);
                            if (item) {

                                if (!item._collapsed) {
                                    item._collapsed = true;
                                } else {
                                    item._collapsed = false;
                                }
                                console.log('item', item._collapsed);
                                dataView.updateItem(item.id, item);
                            }
                            e.stopImmediatePropagation();
                        }
                    });

                    grid.onCellChange.subscribe(function (e, args) {
                        dataView.updateItem(args.item.id, args.item);
                    });

                    // wire up model events to drive the grid
                    dataView.onRowCountChanged.subscribe(function (e, args) {
                        console.log('onRowCountChanged')
                        grid.updateRowCount();
                        grid.render();
                    });

                    dataView.onRowsChanged.subscribe(function (e, args) {
                        console.log('onRowsChanged')
                        grid.invalidateRows(args.rows);
                        grid.render();
                    });

                };

                /**
                 * scope event on callStacks.initialize
                 */
                scope.$on('distributedCallFlow.initialize.' + scope.namespace, function (event, transactionDetail) {
                    initialize(transactionDetail);
                });
            }
        };
    }
]);
