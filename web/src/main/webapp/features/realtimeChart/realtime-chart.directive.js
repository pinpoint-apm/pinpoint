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
			SHOW_EXTRA_INFO: "showExtraInfo",
			REQUEST_LABEL: "requestLabel",
			REQUEST_COLOR: "requestColor",
			CHART_COLOR: "chartColor",
			NAMESPACE: "namespace",
			XCOUNT: "xcount",
			HEIGHT: "height",
			WIDTH: "width"
		}
	});
	
	pinpointApp.directive('realtimeChartDirective', [ 'realtimeChartDirectiveConfig', '$location',  
	    function (cfg, $location) {
	        return {
	            restrict: 'EA',
	            replace: true,
	            template: '<svg width="" height=""></svg>',
	            link: function postLink(scope, element, attrs) {
	            	var oOuterOption = {
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
            	        timeFormat: d3.time.format("%Y.%m.%d %H:%M:%S"),
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
            	    initChartData();

            	    var d3svg, d3svgX, d3svgY, d3grid, d3path, d3area, d3labels, d3tooltip, d3tooltipTextGroup, d3tooltipDate, d3errorLabel;
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
	            	            .attr("id", "clip")
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
            	            chartDataQueue.push( d3.range(oOuterOption.xAxisCount).map(function() { return { y: 0, d: now  }; }) );
            	        }
            	    }
            	    function initAxis() {
            	        d3svgX = d3.scale.linear().domain(oInnerOption.domain).range([0, chartInnerWidth]);
            	        if ( oOuterOption.showExtraInfo ) {
	            	        d3svg.append("g").attr("class", "y axis");
            	        }
            	        d3grid = d3svg.append("g"); 
            	        resetYAxis();
            	    }
            	    function resetYAxis() {
            	        d3svgY = d3.scale.linear().domain([0, maxY]).range([chartInnerHeight, 0]);
            	        if ( oOuterOption.showExtraInfo ) {
	            	        d3svg.selectAll("g.y.axis")
	            	        	.call( d3.svg.axis().scale(d3svgY).ticks(3).orient("left").tickFormat(oInnerOption.yAxisFormat) );
            	        }
            	        resetGrid();
            	    }
            	    function resetGrid() {
            	    	var gridValue = [];
            	    	var aTarget = d3svg.select("g.y.axis").selectAll("g.tick");
            	    	if ( aTarget.length == 0 ) return;
            	    	
            	    	jQuery.each( aTarget[0], function( index, ele ) {
            	    		gridValue.push( parseFloat( ele.getAttribute("transform").replace(/translate\(0,(-?[0-9.]*)\)/, "$1") ) );
            	    	});
            	    	var elements = d3grid.selectAll("line").data(gridValue);
            	    	
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
	        	    		.attr("y1", function(d, i) {
	        	    			return d;
	        	    		})
	        	    		.attr("y2", function(d, i) {
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
	        	            .attr("clip-path", "url(#clip)")
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
	        	            .attr("text-acchor", "end")
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
            	        d3tooltipDate.text( oInnerOption.timeFormat(new Date(datum[0].d)) );
            	    }
            	    function initLabels( datum ) {
            	        if ( oOuterOption.showExtraInfo === false ) return;
            	        d3labels = d3svg.append("g")
        	                .attr("transform", function(d, i) {
        	                    return "translate(" + (oOuterOption.width - oInnerOption.margin.left - oInnerOption.margin.right + 4) + ",10)";
        	                })
        	                .attr("class", "request-count")
        	                .selectAll("text")
        	                .data( oOuterOption.requestLabel )
        	                .enter()
        	                .append("text")
        	                .attr("y", function(d, i) {
        	                    return ( (oOuterOption.requestLabel.length - i - 1) / oOuterOption.requestLabel.length) * 100 + "%";
        	                })
        	                .attr("fill", function(d, i) {
        	                    return oOuterOption.requestColor[i];
        	                })
        	                .text(function(d, i) {
        	                    return oOuterOption.requestLabel[i];
        	                });
            	    }
            	    function initErrorLabel() {
            	    	d3errorLabel = d3svg.append("text")
            	    		.attr("class", "error")
            	    		.attr("y", "40%")
            	    		.attr("x", "50%");
            	    }
            	    function resetLabelData( datum ) {
            	        if ( oOuterOption.showExtraInfo === false ) return;
            	        d3labels
        	                .data( datum )
        	                .text(function(d, i) {
        	                    return typeof d.y !== "undefined" ? ( d.y + " : " + oOuterOption.requestLabel[i] ) : oOuterOption.requestLabel[i];
        	                });
            	    }
            	    function tick() {
            	        d3transition = d3transition.each(function() {
            	            if ( passingQueue.length === 0 ) return;

            	            var aNewData = passingQueue.shift();
            	            resetLabelData( aNewData );
            	            resetYAxis();

            	            var i = 0;
            	            for( i = 0 ; i < chartDataQueue.length ; i++ ) {
            	                chartDataQueue[i].push( aNewData[i] );
            	            }
            	            d3stack(chartDataQueue);
            	            redrawPath();
            	            for( i = 0 ; i < chartDataQueue.length ; i++ ) {
            	                chartDataQueue[i].shift();
            	            }
            	            resetTooltipLabel( getPositionData( lastIndex ) );
            	        }).transition().each("start", function() {
            	            tick();
            	        });
            	    }
            	    function redrawPath() {
            	        d3path.attr("d", d3area)
            	            .attr("transform", null)
            	            .transition()
            	            .attr("transform", "translate(" + d3svgX(0) + ")");
            	    }
            	    tick();
	            	    
	            	scope.$on('realtimeChartDirective.onData.' + oOuterOption.namespace, function (event, aNewRequestCount, timeStamp, yValue) {
	            		maxY = yValue;
	            		d3errorLabel.text("");
	            		passingQueue.push( aNewRequestCount.map(function(v, i) {
	            			return {
	            				y: parseInt( v ),
	            				d: timeStamp
	            			}
	            		}) );
	    	        });
	            	scope.$on('realtimeChartDirective.onError.' + oOuterOption.namespace, function (event, errorMessage, timeStamp, yValue) {
	            		maxY = yValue;
	            		d3errorLabel.text( errorMessage );
	            	});
	            	scope.$on('realtimeChartDirective.clear.' + oOuterOption.namespace, function (event, aNewRequestCount, timeStamp) {
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
	            }
	        };
	    }
	]);
})();