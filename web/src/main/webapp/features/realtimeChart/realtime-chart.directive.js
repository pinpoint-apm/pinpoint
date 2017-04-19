(function() {
	'use strict';
	/**
	 * (en)realtimeChartDirective 
	 * @ko realtimeChartDirective
	 * @group Directive
	 * @name realtimeChartDirective
	 * @class
	 */
	pinpointApp.constant('realtimeChartDirectiveConfig', {
		params: {
			TIMEOUT_MAX_COUNT: "timeoutMaxCount",
			SHOW_EXTRA_INFO: "showExtraInfo",
			REQUEST_LABEL: "requestLabel",
			REQUEST_COLOR: "requestColor",
			CHART_COLOR: "chartColor",
			NAMESPACE: "namespace",
			XCOUNT: "xcount",
			HEIGHT: "height",
			WIDTH: "width"
		},
		responseCode: {
			ERROR_BLACK: 111,
			TIMEOUT: 211
		},
		consts: {
			maxDelayCount: 5,
			verticalGridCount: 5
		},
		message: {
			NO_ACTIVE_THREAD: "No Active Thread",
			NO_RESPONSE: "No Response"
		}
	});
	
	pinpointApp.directive( "realtimeChartDirective", [ "realtimeChartDirectiveConfig", "CommonUtilService",
	    function ( cfg, CommonUtilService ) {
	        return {
	            restrict: 'EA',
	            replace: true,
	            template: '<svg width="" height=""></svg>',
	            link: function postLink(scope, element, attrs) {
					var oOuterOption = {
						timeoutMaxCount:parseInt(attrs[cfg.params.TIMEOUT_MAX_COUNT]),
						showExtraInfo:	attrs[cfg.params.SHOW_EXTRA_INFO] === "true",
						requestLabel:	scope[attrs[cfg.params.REQUEST_LABEL]],
						requestColor: 	scope[attrs[cfg.params.CHART_COLOR]],
						xAxisCount:		parseInt(attrs[cfg.params.XCOUNT]),
						namespace:		attrs[cfg.params.NAMESPACE],
						height:			parseInt(attrs[cfg.params.HEIGHT]),
						width: 			parseInt(attrs[cfg.params.WIDTH])
					};

					var oInnerOption = {
						domain: [1, oOuterOption.xAxisCount - 2],
						margin: {
							top: 6,
							left: oOuterOption.showExtraInfo ? 38 : 20,
							right: 80,
							bottom: 6
						},
						// timeFormat: d3.time.format("%Y.%m.%d %H:%M:%S"),
						transaction: {
							duration: 1000,
							ease: "linear"
						},
						yAxisFormat: d3.format("d"),
						tooltipWidth: 50,
						interpolation: "basis"
					};
					var chartInnerWidth = oOuterOption.width - ( oOuterOption.showExtraInfo ? (oInnerOption.margin.left + oInnerOption.margin.right) : 0 );
					var chartInnerHeight = oOuterOption.height - oInnerOption.margin.top - oInnerOption.margin.bottom;
					var maxY = 0;
					var lastIndex = -1;
					var lastPosition = -1;
					var passingQueue = [];
					var chartDataQueue = [];
					var timeoutCount = 0;
					var delayCount = 0;
					var verticalGridGap = parseInt( chartInnerWidth / (cfg.consts.verticalGridCount - 1) );
					var tickCount = 1;
					initChartData();

					var d3svg, d3svgX, d3svgY, d3HGrid, d3VGrid, d3VGridLines, d3path, d3area, d3labels, d3totalLabel, d3tooltip, d3tooltipTextGroup, d3tooltipDate, d3errorLabel, d3errorLabelSpan1, d3errorLabelSpan2;
					var d3stack = d3.layout.stack().y(function(d) { return d.y; });
					var d3transition = d3.select({}).transition().duration(oInnerOption.transaction.duration).ease(oInnerOption.transaction.ease);
					d3stack(chartDataQueue);

					initChart();
					initAxis();
					initArea();
					initPath();
					initTooltip();
					initTooltipEvent();
					initLabels();
					initErrorLabel();

            	    function initChart() {
            	        d3svg = d3.select( element.get(0) )
            	            .attr("width", oOuterOption.width)
            	            .attr("height", oOuterOption.height)
            	            .append("g")
	            	            .attr("class", "base")
	            	            .attr("transform", "translate(" + (oOuterOption.showExtraInfo ? oInnerOption.margin.left : 0) + "," + oInnerOption.margin.top + ")");
            	        d3svg.append("defs")
            	        	.append("clipPath")
	            	            .attr("id", "clip-" + oOuterOption.namespace)
	            	            .append("rect")
		            	            .attr("x", 1)
		            	            .attr("y", 0)
		            	            .attr("width", chartInnerWidth)
		            	            .attr("height", chartInnerHeight);
            	    }
            	    function initChartData() {
            	    	chartDataQueue.length = 0;
            	    	var now = Date.now();
            	        for( var i = 0 ; i < oOuterOption.requestLabel.length ; i++ ) {
            	            chartDataQueue.push( d3.range(oOuterOption.xAxisCount).map(function() { return { y: 0, d: now }; }) );
            	        }
            	    }
            	    function initAxis() {
            	        d3svgX = d3.scale.linear().domain(oInnerOption.domain).range([0, chartInnerWidth]);
            	        if ( oOuterOption.showExtraInfo ) {
	            	        d3svg.append("g").attr("class", "y axis");
						} else {
							d3VGrid = d3svg.append("g").attr("class", "v-grid");
							resetVGrid();
						}
            	        d3HGrid = d3svg.append("g").attr("class", "h-grid");
            	        resetYAxis();
            	    }
            	    function resetYAxis() {
            	        d3svgY = d3.scale.linear().domain([0, maxY]).range([chartInnerHeight, 0]);
            	        if ( oOuterOption.showExtraInfo ) {
	            	        d3svg.selectAll("g.y.axis")
	            	        	.call( d3.svg.axis().scale(d3svgY).ticks(3).orient("left").tickFormat(oInnerOption.yAxisFormat) );
						}
            	        resetHGrid();
            	    }
					function resetVGrid() {
						var aData = [];
						for( var i = 0 ; i < cfg.consts.verticalGridCount ; i++ ) {
							aData.push( verticalGridGap * i );
						}
						d3VGridLines = d3VGrid.selectAll("line").data(aData).enter().append("line")
							.attr("class", "grid").attr({
								x1: function(d) { return d; },
								x2: function(d) { return d; },
								y1: -10,
								y2: chartInnerHeight
							})[0];

					}
            	    function resetHGrid() {
            	    	var gridValue = [];
            	    	var aTarget = d3svg.select("g.y.axis").selectAll("g.tick");
            	    	if ( aTarget.length === 0 ) return;
            	    	
            	    	jQuery.each( aTarget[0], function( index, ele ) {
            	    		gridValue.push( parseFloat( ele.getAttribute("transform").replace(/translate\(0,(-?[0-9.]*)\)/, "$1") ) );
            	    	});
            	    	var elements = d3HGrid.selectAll("line").data(gridValue);
            	    	
	            	    elements.enter()
	            	    	.append("line")
            	    		.attr("class", "grid")
            	    		.transition()
            	    		.attr("x1", 0)
            	    		.attr("x2", chartInnerWidth)
            	    		.attr("y1", function(d, i) {
            	    			return d;
            	    		})
            	    		.attr("y2", function(d, i) {
            	    			return d;
            	    		});
	            	   
	            	    elements
	            	    	.transition()
	            	    	.attr("x1", 0)
            	    		.attr("x2", chartInnerWidth)
	        	    		.attr("y1", function(d) {
	        	    			return d;
	        	    		})
	        	    		.attr("y2", function(d) {
	        	    			return d;
	        	    		});
	            	    
	            	    elements
	            	    	.exit()
	            	    	.remove();
            	    }
            	    function initArea() {
            	    	d3area = d3.svg.area()
            	            .interpolate(oInnerOption.interpolation)
            	            .x(function(d, i) { return d3svgX(i); })
            	            .y0(function(d, i) { return d3svgY(d.y0); })
            	            .y1(function(d, i) { return d3svgY(d.y + d.y0); });
            	    }
            	    function initPath() {
        	    		d3path = d3svg.append("g")
	        	            .attr("class", "pathArea")
	        	            .attr("clip-path", "url(#clip-" + oOuterOption.namespace + ")")
	        	            .selectAll("path")
	        	            .data(chartDataQueue)
	        	            .enter()
	        	            .append("path")
	        	            .attr("d", d3area)
	        	            .attr("class", "area ")
	        	            .attr("fill", function(d, i) { return oOuterOption.requestColor[i]; });
            	    }
            	    function initTooltip() {
            	    	d3tooltip = d3svg.append("g").attr("class", "chart-tooltip").attr("transform", "translate(-1000, 0)");
            	    	
            	    	d3tooltip.append("line")
            	    		.attr("x1", oInnerOption.tooltipWidth)
	        	            .attr("y1", 10)
	        	            .attr("x2", oInnerOption.tooltipWidth)
	        	            .attr("y2", chartInnerHeight)
	        	            .attr("class", "guideLine");
	        	        
	        	        d3tooltipTextGroup = d3tooltip.append("g").attr("transform", "translate(0, 10)");
	        	        d3tooltipTextGroup.append("rect")
	        	            .attr("width", 40)
	        	            .attr("height", 90)
	        	            .attr("fill", "#000")
	        	            .attr("fill-opacity", "0.7");
	
	        	        d3tooltipTextGroup
	        	            .selectAll("text")
	        	            .data( oOuterOption.requestLabel )
	        	            .enter()
	        	            .append("text")
	        	            .attr("x", function(d, i) {
	        	                return 30;
	        	            })
	        	            .attr("y", function(d, i) {
	        	                return ((oOuterOption.requestColor.length - i - 1) * 20 + 20) + "px";
	        	            })
	        	            .attr("fill", function(d, i) {
	        	                return oOuterOption.requestColor[i];
	        	            })
	        	            .attr("text-anchor", "end")
	        	            .text(function(d, i) {
	        	                return oOuterOption.requestLabel[i];
	        	            });
	        	       d3tooltipDate = d3svg.append("text")
	        	       		.attr("class", "date")
	        	        	.attr("x", "29%")
	        	        	.attr("y", "6px")
	        	            .text("");
            	    }
            	    function initTooltipEvent() {
            	    	if ( oOuterOption.showExtraInfo === false ) return;
            	    	
             	        d3svg.on("mouseout", function() {
         	                d3tooltip.attr("transform", "translate(-1000, 0)");
         	                d3tooltipDate.text("");
         	                lastPosition = -1;
         	                lastIndex = -1;
         	            })
         	            .on("mousemove", function(d) {
         	                lastIndex = parseInt( d3svgX.invert( d3.mouse(this)[0] ) );
         	                lastPosition = parseInt( d3.mouse(this)[0] );
         	                lastPosition = lastPosition > chartInnerWidth ? -1000 : lastPosition;
         	                d3tooltip.attr("transform", "translate(" + (lastPosition -  oInnerOption.tooltipWidth) + ",0)");
         	                resetTooltipLabel( getPositionData( lastIndex ) );
           	            });
            	    }
            	    function getPositionData( index ) {
            	        var a = [];
            	        if ( index === -1 ) return a;
            	        for( var i = 0 ; i < chartDataQueue.length ; i++ ) {
            	            a.push( {
            	                y: chartDataQueue[i][index] ? chartDataQueue[i][index].y : 0,
            	                d: chartDataQueue[i][index] ? chartDataQueue[i][index].d : Date.now()
            	            });
            	        }
            	        return a;
            	    }
            	    function resetTooltipLabel( datum ) {
            	    	if ( oOuterOption.showExtraInfo === false ) return;
            	        if ( lastPosition === -1 || lastIndex === -1 ) return;
            	        d3tooltipTextGroup.selectAll("text").text(function(d, i) {
            	        	return datum[i].y;
            	        });
            	        // d3tooltipDate.text( oInnerOption.timeFormat(new Date(datum[0].d)) );
						d3tooltipDate.text( CommonUtilService.formatDate( datum[0].d ) );
            	    }
            	    function initLabels() {
            	        if ( oOuterOption.showExtraInfo === false ) return;
            	        
            	        d3svg.append("g")
	    	                .attr("transform", function(d, i) {
	    	                    return "translate(" + (oOuterOption.width - oInnerOption.margin.left - oInnerOption.margin.right + 4 + 32) + ",10)";
	    	                })
	    	                .attr("class", "request-label")
	    	                .append("text")
            	        	.attr("text-anchor", "end")
            	        	.attr("fill", "#000")
            	        	.style("font-size", "14px")
            	        	.style("font-weight", "bold")
            	        	.attr("y", "0%")
            	        	.text("Total");
            	        
            	        d3svg.append("g")
	    	                .attr("transform", function(d, i) {
	    	                    return "translate(" + (oOuterOption.width - oInnerOption.margin.left - oInnerOption.margin.right + 4 + 32) + ",10)";
	    	                })
	    	                .attr("class", "request-label")
	    	                .selectAll("text")
	    	                .data( oOuterOption.requestLabel )
	    	                .enter()
	    	                .append("text")
	    	                .attr("text-anchor", "end")
	    	                .attr("fill", "#9B9B9B")
	    	                .attr("y", function(d, i) {
	    	                	return ((4-i) * 20) + "%";
	    	                })
	    	                .text(function(d, i) {
	    	                    return oOuterOption.requestLabel[i];
	    	                });

            	        d3totalLabel = d3svg.append("g")
	    	                .attr("transform", function(d, i) {
	    	                    return "translate(" + (oOuterOption.width - oInnerOption.margin.left - oInnerOption.margin.right + 4 + 70) + ",10)";
	    	                })
	    	                .attr("class", "request-count")
	    	                .append("text")
	        	        	.attr("text-anchor", "end")
	        	        	.attr("fill", "#000")
	        	        	.attr("y", "0%")
	        	        	.text("0");
    	                
            	        d3labels = d3svg.append("g")
        	                .attr("transform", function(d, i) {
        	                    return "translate(" + (oOuterOption.width - oInnerOption.margin.left - oInnerOption.margin.right + 4 + 70) + ",10)";
        	                })
        	                .attr("class", "request-count")
        	                .selectAll("text")
        	                .data( oOuterOption.requestLabel )
        	                .enter()
        	                .append("text")
        	                .attr("text-anchor", "end")
        	                .attr("y", function(d, i) {
        	                	return ((4-i) * 20) + "%";
        	                })
        	                .attr("fill", function(d, i) {
        	                    return oOuterOption.requestColor[i];
        	                })
        	                .text("0");
            	    }
            	    function initErrorLabel() {
            	    	d3errorLabel = d3svg.append("text")
            	    		.attr("class", "error")
            	    		.attr("y", "40%")
            	    		.attr("x", "50%");
            	    	d3errorLabelSpan1 = d3errorLabel.append("tspan").attr("x", "50%").attr("dy", "0%");
            	    	d3errorLabelSpan2 = d3errorLabel.append("tspan").attr("x", "50%").attr("dy", "14px");
            	    }
            	    function setErrorMessage( bError, aMessage ) {

           	    		d3errorLabel.style("fill", bError ? "#F00" : "#000");
            	    	if ( aMessage.length > 1 ) {
            				d3errorLabel.attr("y", "20%" );
            				d3errorLabelSpan1.text( aMessage[0] );
            				d3errorLabelSpan2.text( aMessage[1] );
            			} else {
            				d3errorLabel.attr("y", "40%" );
            				d3errorLabelSpan1.text( aMessage[0] );
            				d3errorLabelSpan2.text( "" );
            			}
            	    }
            	    function resetLabelData( datum ) {
            	        if ( oOuterOption.showExtraInfo === false ) return;
            	        var subSum = 0;
            	        d3labels
        	                .data( datum )
        	                .text(function(d, i) {
        	                	if ( typeof d.y !== "undefined" ) {
        	                		subSum += d.y;
        	                		return d.y;
        	                	}
        	                	return "";
        	                });
            	        
            	        d3totalLabel.text(subSum);
            	    }
					function initDelayCount() {
						delayCount = 0;
					}
            	    function tick() {
            	        d3transition = d3transition.each(function() {
            	        	delayCount++;
            	            if ( passingQueue.length === 0 ) {
            	            	if ( delayCount > cfg.consts.maxDelayCount && oOuterOption.showExtraInfo === false ) {
            	            		setErrorMessage( false, [cfg.message.NO_RESPONSE]);
            	            	}
            	            	return;
            	            }
							initDelayCount();

            	            var aNewData = passingQueue.shift();
							resetLabelData( aNewData );
            	            resetYAxis();
							if ( oOuterOption.showExtraInfo === false ) {
								redrawVGrid();
							}

            	            var i = 0;
            	            for( i = 0 ; i < chartDataQueue.length ; i++ ) {
            	                chartDataQueue[i].push( aNewData[i] );
            	            }
            	            d3stack(chartDataQueue);
							initArea();
							redrawPath();
            	            for( i = 0 ; i < chartDataQueue.length ; i++ ) {
            	                chartDataQueue[i].shift();
            	            }
            	            resetTooltipLabel( getPositionData( lastIndex ) );
            	        }).transition().each("start", function() {
            	            tick();
            	        });
            	    }
					function redrawVGrid() {
						var maxGridX = 0;
						for( var i = 0 ; i < d3VGridLines.length ; i++ ) {
							maxGridX = Math.max( maxGridX, parseInt( d3.select(d3VGridLines[i]).attr("x1") ) );
						}

						var newX = d3svgX(0) * tickCount++;
						var nextGridX = maxGridX + verticalGridGap;
						d3VGrid.transition().attr("transform", "translate("+ newX + ")").each("end", function() {
							for( var i = 0 ; i < d3VGridLines.length ; i++ ) {
								var d3Elem = d3.select(d3VGridLines[i]);
								var x1 = parseInt( d3Elem.attr("x1") );
								if ( x1 < Math.abs( newX ) ) {
									d3Elem.attr({
										x1: nextGridX,
										x2: nextGridX
									});
									nextGridX += verticalGridGap;
								}
							}
						});
					}
            	    function redrawPath() {
            	        d3path.data(chartDataQueue)
							.attr("d", d3area)
            	            .attr("transform", null)
            	            .transition()
            	            .attr("transform", "translate(" + d3svgX(0) + ")");
            	    }
            	    tick();
	            	    
	            	scope.$on('realtimeChartDirective.onData.' + oOuterOption.namespace, function (event, aNewRequestCount, timeStamp, yValue, bAllError) {
	            		maxY = yValue;
	            		timeoutCount = 0;
	            		setErrorMessage(false, ["", ""]);
	            		if ( bAllError === false ) {
		            		passingQueue.push( aNewRequestCount.map(function(v) {
		            			return {
		            				y: parseInt( v ),
		            				d: timeStamp
		            			};
		            		}));
	            		}
	    	        });
	            	scope.$on('realtimeChartDirective.onError.' + oOuterOption.namespace, function (event, oError, timeStamp, yValue) {
	            		maxY = yValue;
	            		if ( oError.code === cfg.responseCode.TIMEOUT ) {
		            		if ( timeoutCount < oOuterOption.timeoutMaxCount ) {
		            			if ( passingQueue.length > 0 ) {
									var oTemp = passingQueue[passingQueue.length - 1];
									passingQueue.push(oTemp.map(function(v) {
										return {
											"d": timeStamp,
											"y": v["y"],
											"y0": v["y0"]
										};
									}));
								}
		            		} else {
								initDelayCount();
		            			setErrorMessage( true, oError.message.split("_") );
		            		}
		            		timeoutCount++;
	            		} else {
							initDelayCount();
	            			setErrorMessage( oError.code !== cfg.responseCode.ERROR_BLACK, oError.message.split("_") );
	            		}
	            		
	            	});
	            	scope.$on('realtimeChartDirective.clear.' + oOuterOption.namespace, function () {
	            		passingQueue.length = 0;
	            		initChartData();

	            		element.html("");
						resetLabelData([{}, {}, {}, {}]);
						d3stack(chartDataQueue);

						initChart();
						initAxis();
	            	    initArea();
	            	    initPath();
	            	    initTooltip();
	            	    initTooltipEvent();
	            	    initLabels();
	            	    initErrorLabel();
	    	        });
					scope.$on('$destroy', function() {});
	            }
	        };
	    }
	]);
})();