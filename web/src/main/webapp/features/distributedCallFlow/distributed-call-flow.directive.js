(function() {
	'use strict';
	/**
	 * (en)distributedCallFlowDirective 
	 * @ko distributedCallFlowDirective
	 * @group Directive
	 * @name distributedCallFlowDirective
	 * @class
	 */	
	pinpointApp.directive( "distributedCallFlowDirective", [ "$timeout", "CommonAjaxService", "CommonUtilService", "SystemConfigurationService",
	    function ( $timeout, CommonAjaxService, CommonUtilService, SystemConfigService ) {
	        return {
	            restrict: "E",
	            replace: true,
	            templateUrl: "features/distributedCallFlow/distributedCallFlow.html?v=" + G_BUILD_TIME,
	            scope : {
	                namespace : "@" // string value
	            },
	            link: function postLink(scope, element, attrs) {
	                // initialize variables
	            	var grid, dataView, lastAgent, startRow;
	
	                // initialize variables of methods
	                var initialize, treeFormatter, treeFilter, parseData, execTimeFormatter,
	                    getColorByString, progressBarFormatter, argumentFormatter, linkFormatter, hasChildNode, searchRowByTime, searchRowByWord, selectRow;
	
	                // bootstrap
	                window.callStacks = []; // Due to Slick.Data.DataView, must use window property to resolve scope-related problems.

					var removeTag = function( text ) {
						if ( text === undefined || text === null ) {
							return "";
						} else {
							return text.replace(/</g, "&lt;").replace(/>/g, "$gt;");
						}
					};
					var getAuthorizeView = function( bIsAuthorized, text ) {

						if ( bIsAuthorized ) {
							return removeTag( text );
						} else {
							return "<i style='color:#AAA;'>" + removeTag( text ) + "</i> <a href='" + SystemConfigService.get("securityGuideUrl") + "' target='_blank' style='color:#AAA;'><span class='glyphicon glyphicon-share'></span></a>";
						}
					};
	                /**
	                 * get color by string
	                 * @param idx
	                 * @returns {string}
	                 */
	                getColorByString = function(str) {
	                	// str to hash
						var i = 0, hash = 0, colour = "#";
	                    for ( i = 0, hash = 0; i < str.length; hash = str.charCodeAt(i++) + ((hash << 5) - hash));
	                    // int/hash to hex
	                    for ( i = 0, colour = "#"; i < 3; colour += ("00" + ((hash >> i++ * 8) & 0xFF).toString(16)).slice(-2));
	                    return colour;
	                };
	
	                /**
	                 * tree formatter
	                 * @param row
	                 * @param cell
	                 * @param value
	                 * @param columnDef
	                 * @param dataContext
	                 * @returns {string}
	                 */
	                treeFormatter = function (row, cell, value, columnDef, dataContext) {
	                    var html = [];
	
	                    // value = value.replace(/&/g,"&amp;").replace(/</g,"&lt;").replace(/>/g,"&gt;");
	                    var item = dataView.getItemById(dataContext.id);
	                    lastAgent = item.agent ? item.agent : lastAgent;
	
	                    var leftBarColor = getColorByString(lastAgent),
	                        idx = dataView.getIdxById(dataContext.id),
	                        divClass = 'dcf-popover';
	
	                    if (item.hasException) {
	                        divClass += ' has-exception';
	                    } else if (!item.isMethod) {
	                        divClass += ' not-method';
	                    }
	
	                    
	                    html.push('<div class="'+divClass+'" data-container=".grid-canvas" data-toggle="popover" data-trigger="manual" data-placement="right" data-content="'+ removeTag( value ) +'">');
	                    html.push("<div style='position:absolute;top:0;left:0;bottom:0;width:5px;background-color:"+ leftBarColor +"'></div>");
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
	                    	if( item.method === "SQL" ) {
	                    		html.push('<button type="button" class="btn btn-default btn-xs btn-success sql" style="padding:0px 2px 0px 2px"><span class="glyphicon glyphicon-eye-open sql"></span></button>&nbsp;');
	                    	} else {
	                    		html.push('<span class="glyphicon glyphicon-info-sign"></span>&nbsp;');
	                    	}
	                        
	                    } else {
	                    	var itemMethodType = parseInt( item.methodType );
	                    	switch( itemMethodType ) {
	                    	case 100:
	                    			html.push('<i class="xi-shipping"></i>&nbsp;');
	                    			break;
	                    	case 200:
	                    			html.push('<span class="glyphicon glyphicon-transfer"></span>&nbsp;');
	                    			break;
	                    	case 900:
	                    			html.push('<i class="xi-info-triangle" style="color:#FF6600"></i>&nbsp;');
	                    			break;
	                    	}
	                    }
	
	                    html.push( getAuthorizeView( dataContext.isAuthorized, value ) );
	                    html.push('</div>');
	
	                    return html.join('');
	                };
	
	                /**
	                 * tree filter
	                 * @param item
	                 * @returns {boolean}
	                 */
	                treeFilter = function (item) {
	                    var result = true;
	                    if ( angular.isDefined( item.parent ) && item.parent !== null ) {
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
	
	                /**
	                 * argument formatter
	                 * @param row
	                 * @param cell
	                 * @param value
	                 * @param columnDef
	                 * @param dataConrtext
	                 * @returns {string}
	                 */
	                argumentFormatter = function (row, cell, value, columnDef, dataContext) {
	                    var html = [];
	                    html.push('<div class="dcf-popover" data-container=".grid-canvas" data-toggle="popover" data-trigger="manual" data-placement="right" data-content="'+ encodeURIComponent(value) +'">');
	                    html.push( getAuthorizeView( dataContext.isAuthorized, value ) );
	                    html.push('</div>');
	                    return html.join('');
	                };
	                
	                
	                linkFormatter = function (row, cell, value, columnDef, dataContext) {
	                	
	                	if (!value || 0 === value.length) {
	                		return;
	                	}
	                	
	                    var html = [];
	                    html.push('<a class="btn btn-default btn-xs"');
	                    html.push('href="');
	                    html.push(value);
	                    html.push('" target="_blank">');
	
	                    var item = dataView.getItemById(dataContext.id);
	                    var logButtonName = item.logButtonName;
	                    html.push(logButtonName);
	                    
	                    html.push('</a>');
	                    
	                    return html.join('');
	                };
	
	                /**
	                 * exec time formatter
	                 * @param row
	                 * @param cell
	                 * @param value
	                 * @param columnDef
	                 * @param dataContext
	                 * @returns {*}
	                 */
	                execTimeFormatter = function (row, cell, value, columnDef, dataContext) {
	                	if ( angular.isUndefined( value ) || value === null ) {
	                		return "";
						} else {
							return CommonUtilService.formatDate(value, "HH:mm:ss SSS");
						}
	                };
	
	                /**
	                 * progress bar formatter
	                 * @param row
	                 * @param cell
	                 * @param value
	                 * @param columnDef
	                 * @param dataContext
	                 * @returns {string}
	                 */
	                progressBarFormatter = function (row, cell, value, columnDef, dataContext) {
	                    if ( angular.isUndefined( value ) || value === null || value === "" || value === 0) {
	                        return "";
	                    }
	                    var color;
	                    if (value < 30) {
	                        color = "#5bc0de";
	                    } else if (value < 70) {
	                        color = "#5bc0de";
	                    } else {
	                        color = "#5bc0de";
	                    }
	                    //return "<span class='percent-complete-bar' style='background:" + color + ";width:" + value + "%'></span>";
	                    //<span class="percent-complete-bar" style="background-color:red;width:40%;height:2px;float:left;margin-top:2px"></span>
	                    return "<span class='percent-complete-bar' style='background:" + color + ";width:" + value + "%'><span class='percent-complete-bar' style='background-color:#4343C8;width:" + dataContext.execPer + "%;height:4px;float:left;margin-top:1px;'></span></span>";
	                };
	
	                /**
	                 * parse data
	                 * @param index
	                 * @param callStacks
	                 * @returns {Array}
	                 */
	                parseData = function(index, callStacks) {
	                    var result = [],
	                        barRatio = 100 / (callStacks[0][index.end] - callStacks[0][index.begin]);
	                    angular.forEach(callStacks, function (val, key) {
	                    	var bAuthorized = typeof val[index['isAuthorized']] === "undefined" ? true : val[index['isAuthorized']];
							if ( val[index['isFocused']] ) {
	                    		startRow = key;
							}
	                        result.push({
	                            id: 'id_' + key,
								isAuthorized: bAuthorized,
	                            parent: val[index['parentId']] ? val[index['parentId']] - 1 : null,
	                            indent: val[index['tab']],
	                            method: val[index['title']],
	                            argument: val[index['arguments']],
	                            execTime: val[index['begin']] > 0 ? val[index['begin']] : null,
	                            gapMs: val[index['gap']],
	                            timeMs: val[index['elapsedTime']],
	                            timePer: val[index['elapsedTime']] ? ((val[index['end']] - val[index['begin']]) * barRatio) + 0.9 : null,
	                            class: val[index['simpleClassName']],
	                            methodType: val[index['methodType']],
	                            apiType: val[index['apiType']],
	                            agent: val[index['agent']],
	                            applicationName: val[index['applicationName']],
	                            hasException: val[index['hasException']],
	                            isMethod: val[index['isMethod']] ,
	                            logLink : val[index['logPageUrl']],
	                            logButtonName : val[index['logButtonName']],
	                            isFocused : val[index['isFocused']],
	                            execMilli : val[index['executionMilliseconds']],
	                            execPer : val[index['elapsedTime']] && val[index['executionMilliseconds']] ? ( parseInt( val[index['executionMilliseconds']].replace(/,/gi, "") ) / parseInt( val[index['elapsedTime']].replace(/,/gi, "") ) ) * 100 : 0
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
	
	                    var options = {
	                        enableCellNavigation: true,
	                        enableColumnReorder: true,
	                        enableTextSelectionOnCells: true,
	//                        autoHeight: true,
	                        topPanelHeight: 30,
	                        rowHeight: 25
	                    };
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
	                    dataView.getItemMetadata = function( row ) {
	                    	var item = dataView.getItemByIdx(row);
	                    	var o = { cssClasses: "" };
	                    	if ( item.hasException === true ) {
	                    		o.cssClasses += " error-point";
	                    	}
	                    	if ( item.isFocused === true ) {
	                    		o.cssClasses += " entry-point";
	                    	}
	                    	if ( item.execTime !== null ) {
	                    		o.cssClasses += " id_" + (row+1);
	                    	}
	                    	return o;
	                    };
	                    dataView.endUpdate();
	
	                    var columns = [
		                    {id: "method", name: "Method", field: "method", width: 400, formatter: treeFormatter},
		                    {id: "argument", name: "Argument", field: "argument", width: 300, formatter: argumentFormatter},
		                    {id: "exec-time", name: "Start Time", field: "execTime", width: 90, formatter: execTimeFormatter},
		                    {id: "gap-ms", name: "Gap(ms)", field: "gapMs", width: 70, cssClass: "right-align"},
		                    {id: "time-ms", name: "Exec(ms)", field: "timeMs", width: 70, cssClass: "right-align"},
		                    {id: "time-per", name: "Exec(%)", field: "timePer", width: 100, formatter: progressBarFormatter},
		                    {id: "exec-milli", name: "Self(ms)", field: "execMilli", width: 75, cssClass: "right-align"},
		                    {id: "class", name: "Class", field: "class", width: 120},
		                    {id: "api-type", name: "API", field: "apiType", width: 90},
		                    {id: "agent", name: "Agent", field: "agent", width: 130},
		                    {id: "application-name", name: "Application", field: "applicationName", width: 150}
	                    ];

	                    grid = new Slick.Grid(element.get(0), dataView, columns, options);
	                    grid.setSelectionModel(new Slick.RowSelectionModel());
	
	                    var isSingleClick = true, clickTimeout = false;
	                    grid.onClick.subscribe(function (e, args) {
							var item;
	                        if ($(e.target).hasClass("toggle")) {
	                            item = dataView.getItem(args.row);
	                            if (item) {
	                                if (!item._collapsed) {
	                                    item._collapsed = true;
	                                } else {
	                                    item._collapsed = false;
	                                }
	                                dataView.updateItem(item.id, item);
	                            }
	                            e.stopImmediatePropagation();
	                        }
	                        if ( $(e.target).hasClass("sql") ) {
	                        	item = dataView.getItem(args.row);
	                        	var itemNext = dataView.getItem(args.row+1);
	                        	var data = "sql=" + encodeURIComponent( item.argument );

								if ( item.isAuthorized ) {
									if ( angular.isDefined( itemNext ) && itemNext.method === "SQL-BindValue" ) {
										data += "&bind=" + encodeURIComponent( itemNext.argument );
										CommonAjaxService.getSQLBind( "sqlBind.pinpoint", data, function( result ) {
											$("#customLogPopup").find("h4").html("SQL").end().find("div.modal-body").html(
													'<h4>Binded SQL <button class="btn btn-default btn-xs sql">Copy</button></h4>' +
													'<div style="position:absolute;left:10000px">' + result + '</div>' +
													'<pre class="prettyprint lang-sql" style="margin-top:0px">' + result.replace(/\t\t/g, "") + '</pre>' +
													'<hr>' +
													'<h4>Original SQL <button class="btn btn-default btn-xs sql">Copy</button></h4>' +
													'<div style="position:absolute;left:10000px">' + item.argument + '</div>' +
													'<pre class="prettyprint lang-sql" style="margin-top:0px">' + item.argument.replace(/\t\t/g, "") + '</pre>' +
													'<h4>SQL Bind Value <button class="btn btn-default btn-xs sql">Copy</button></h4>' +
													'<div style="position:absolute;left:10000px">' + itemNext.argument + '</div>' +
													'<pre class="prettyprint lang-sql" style="margin-top:0px">' + itemNext.argument + '</pre>'
											).end().modal("show");
											prettyPrint();
										});
									} else {
										$("#customLogPopup").find("h4").html("SQL").end().find("div.modal-body").html(
											'<h4>Original SQL <button class="btn btn-default btn-xs sql">Copy</button></h4>' +
											'<div style="position:absolute;left:10000px">' + item.argument + '</div>' +
											'<pre class="prettyprint lang-sql" style="margin-top:0px">' + item.argument.replace(/\t\t/g, "") + '</pre>'
										).end().modal("show");
										prettyPrint();
									}
								} else {
									$("#customLogPopup").find("h4").html("SQL").end().find("div.modal-body").html(
										'<h4>Original SQL</h4>' +
										'<div style="margin-top:0px;padding:6px 10px;border-radius:4px;background-color:#C56A6A;color:#D7FBBA;">' + item.argument.replace(/\t\t/g, "") + '</div>'
									).end().modal("show");
								}
	                        }
	
	                        if (!clickTimeout) {
	                            clickTimeout = $timeout(function () {
	                                if (isSingleClick) {
	                                    element.find(".dcf-popover").each(function() {
	                                    	var $this = $(this);
	                                    	if ( $this.data("popover") ) {
	                                    		$this.popover("hide");
											}
										});
	                                }
	                                isSingleClick = true;
	                                clickTimeout = false;
	                            }, 300);
	                        }
	                    });
	
	                    grid.onDblClick.subscribe(function (e, args) {
	                        isSingleClick = false;
	                        $(e.target).popover({
	                        	content: function() {
									return decodeURIComponent( this.getAttribute("data-content") );
								}
							}).popover('toggle');
	                    });
	
	                    grid.onCellChange.subscribe(function (e, args) {
	                        dataView.updateItem(args.item.id, args.item);
	                    });
	
	                    grid.onActiveCellChanged.subscribe(function (e, args) {
	                        scope.$emit('distributedCallFlowDirective.rowSelected.' + scope.namespace, args.grid.getDataItem(args.row));
	                    });
	
	                    hasChildNode = function (row) {
	                        var nextItem = dataView.getItem(row + 1);
	                        if (nextItem) {
	                            if (row === nextItem.parent) {
	                                return true;
	                            }
	                        }
	                        return false;
	                    };
	
	                    grid.onKeyDown.subscribe(function (e, args) {
	                        var item = dataView.getItem(args.row);
	
	                        if (e.which == 37) { // left
	                            if (hasChildNode(args.row)) {
	                                item._collapsed = true;
	                                dataView.updateItem(item.id, item);
	//                            } else if (item.indent > 0 && item.parent >= 0) {
	//                                var parent = dataView.getItem(item.parent);
	//                                parent._collapsed = true;
	//                                dataView.updateItem(item.id, item);
	//                                grid.setActiveCell(dataView.getRowById(parent.id), 0);
	                            } else {
	                                var prevItem = dataView.getItem(args.row - 1);
	                                if (prevItem) {
	                                    grid.setActiveCell(args.row - 1, 0);
	                                }
	                            }
	                        } else if (e.which == 39) { // right
	                            if (item._collapsed) {
	                                item._collapsed = false;
	                            } else {
	                                var nextItem = dataView.getItem(args.row + 1);
	                                if (nextItem) {
	                                    grid.setActiveCell(args.row + 1, 0);
	                                }
	                            }
	                            dataView.updateItem(item.id, item);
	                        }
	                    });
	
	                    // wire up model events to drive the grid
	                    dataView.onRowCountChanged.subscribe(function (e, args) {
	                        grid.updateRowCount();
	                        grid.render();
	                    });
	
	                    dataView.onRowsChanged.subscribe(function (e, args) {
	                        grid.invalidateRows(args.rows);
	                        grid.render();
	                    });
	                    
	                    $timeout(function() {
							grid.scrollRowToTop( startRow );
						});
	                };
	                $("#customLogPopup").on("click", "button", function() {
	                	var range = document.createRange();
	                	range.selectNode( $(this).parent().next().get(0) );
	                	window.getSelection().addRange( range );
	                	try {
	                		document.execCommand("copy");
	                	}catch(err) {
	                		console.log( "unable to copy :", err);
	                	}
	                	window.getSelection().removeAllRanges();
	                });
	
	                /**
	                 * scope event on distributedCallFlowDirective.initialize
	                 */
	                scope.$on('distributedCallFlowDirective.initialize.' + scope.namespace, function (event, transactionDetail) {
	                    initialize(transactionDetail);
	                });
	
	                /**
	                 * scope event on distributedCallFlowDirective.resize
	                 */
	                scope.$on('distributedCallFlowDirective.resize.' + scope.namespace, function (event) {
						if ( grid ) {
							grid.resizeCanvas();
						}
	                });
	                scope.$on("distributedCallFlowDirective.selectRow." + scope.namespace, function( event, rowId ) {
	                	var gridRow = rowId - 1;
	                	grid.setSelectedRows( [gridRow] );
	                	grid.setActiveCell( gridRow, 0 );
	                	grid.scrollRowToTop( gridRow );
	            	});
	                scope.$on("distributedCallFlowDirective.searchCall." + scope.namespace, function( event, time, index ) {
	                	var row = searchRowByTime(time, index);
	                	if ( row == -1 ) {
	                		if ( index > 0 ) {
	                			selectRow( searchRowByTime(time, 0) );
	                			scope.$emit("transactionDetail.searchActionResult", "Loop" );
	                		} else {
	                			scope.$emit("transactionDetail.searchActionResult", "No call took longer than " + time + "ms." );
	                		}
	                	} else {
	                		selectRow(row);
	                		scope.$emit("transactionDetail.searchActionResult", "" );
	                	}
	            	});
					scope.$on("distributedCallFlowDirective.searchArgument." + scope.namespace, function( event, word, index ) {
						var row = searchRowByWord(word, index);
						if ( row == -1 ) {
							if ( index > 0 ) {
								selectRow( searchRowByWord(word, 0) );
								scope.$emit("transactionDetail.searchActionResult", "Loop" );
							} else {
								scope.$emit("transactionDetail.searchActionResult", "There is no result.." );
							}
						} else {
							selectRow(row);
							scope.$emit("transactionDetail.searchActionResult", "" );
						}
					});
	                searchRowByTime = function( time, index ) {
	                	var count = 0;
	                	var row = -1;
	                	for( var i = 0 ; i < window.callStacks.length ; i++ ) {
	                		if ( parseInt( window.callStacks[i].execMilli.replace(/,/gi, "") ) >= time ) {
	                			if ( count == index ) {
	                				row = i;
	                				break;
	                			} else {
	                				count++;
	                			}
	                		}
	                	}
	                	return row;
	                };
					searchRowByWord = function( word, index ) {
						var count = 0;
						var row = -1;
						for( var i = 0 ; i < window.callStacks.length ; i++ ) {
							if ( window.callStacks[i].argument.indexOf( word ) !== -1 ) {
								if ( count == index ) {
									row = i;
									break;
								} else {
									count++;
								}
							}
						}
						return row;
					};
	                selectRow = function(row) {
	                	grid.setSelectedRows( [row] );
	                	grid.setActiveCell( row, 0 );
	                	grid.scrollRowIntoView( row, true );
	                };
	            }
	        };
	    }
	]);
})();