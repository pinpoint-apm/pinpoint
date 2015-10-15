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
		
	});
	
	pinpointApp.directive('realtimeChartDirective', [ 'realtimeChartDirectiveConfig', '$location',  
	    function (cfg, $location) {
	        return {
	            restrict: 'EA',
	            replace: true,
	            template: '<svg width="" height=""></svg>',
	            link: function postLink(scope, element, attrs) {
	            	var aRequestLabel = scope[attrs["requestLabel"]];
	            	var aRequestColor = scope[attrs["chartColor"]];
	            	var xAxisCount = parseInt( attrs["xcount"] );
	            	var namespace = attrs["namespace"];
	            	var svgHeight = parseInt( attrs["height"] );
	            	var svgWidth = parseInt( attrs["width"] );
	            	var showExtraInfo = attrs["showExtraInfo"] === "true";
	            	
	            	var svg, svgX, svgY, yAxis, path, area, vLine, labels, tooltip, tooltipDate, errorLabel;
	            	var d3Options = {
            	        domain: [1, xAxisCount - 2],
            	        interpolation: "basis",
            	        margin: {
            	        	left: showExtraInfo ? 38 : 20,
            	        	right: 80, 
    	        			top: 6,
            	        	bottom: 6 
            	        }
            	    };
            	    var lastPosition = -1;
            	    var lastIndex = -1;
            	    var aInnerStack = getInitData();
            	    var aRealTimeData = [];
            	    var transition = d3.select({}).transition().duration(1000).ease("linear");
            	    var width = svgWidth - ( showExtraInfo ? d3Options.margin.left : 0 ) - ( showExtraInfo ? d3Options.margin.right : 0 );
            	    var height = svgHeight - d3Options.margin.top - d3Options.margin.bottom;
            	    var sumOfMaxY = 0;
            	    var yAxisFormat = d3.format("d");
            	    var stack = d3.layout.stack().y(function(d) { return d.y; });
            	    stack(aInnerStack);

            	    initGraph();
            	    resetAxis();
            	    resetArea();
            	    resetPath();
            	    resetTooltipLine();
            	    resetLabels();
            	    resetErrorLabel();

            	   
            	    function initGraph() {
            	        svg = d3.select( element.get(0) )
            	            .attr({
            	            	"width": svgWidth,
            	            	"height": svgHeight
            	            })
            	            .append("g")
            	            .attr({
            	            	"class": "base",
            	            	"transform": "translate(" + (showExtraInfo ? d3Options.margin.left : 0) + "," + d3Options.margin.top + ")"
            	            });

            	        svg.append("defs")
            	        	.append("clipPath")
            	            .attr("id", "clip")
            	            .append("rect")
            	            .attr({
            	            	"x": 1,
            	            	"y": 0,
            	            	"width": width,
            	            	"height": height
            	            });
            	    }
            	    function getInitData() {
            	    	var a = [];
            	    	var now = Date.now();
            	        for( var i = 0 ; i < aRequestLabel.length ; i++ ) {
            	            a.push( d3.range(xAxisCount).map(function() { return { y: 0, d: now  }; }) );
            	        }
            	        return a;
            	    }
            	    function resetAxis() {
            	        svgX = d3.scale.linear().domain(d3Options.domain).range([0, width]);
            	        svgY = d3.scale.linear().domain([0, sumOfMaxY]).range([height, 0]);
            	        if ( showExtraInfo ) {
	            	        yAxis = d3.svg.axis().scale(svgY).ticks(3).orient("left").tickFormat(yAxisFormat);
	            	        svg.append("g").attr("class", "y axis");
            	        }
            	        resetYAxis();
            	    }
            	    function resetYAxis() {
            	        svgY = d3.scale.linear().domain([0, sumOfMaxY]).range([height, 0]);
            	        if ( showExtraInfo ) {
	            	        yAxis = d3.svg.axis().scale(svgY).ticks(3).orient("left");
	            	        svg.selectAll("g.y.axis").call(yAxis);
            	        }
            	    }
            	    function resetArea() {
            	        area = d3.svg.area()
            	            .interpolate(d3Options.interpolation)
            	            .x(function(d, i) { return svgX(i); })
            	            .y0(function(d, i) { return svgY(d.y0); })
            	            .y1(function(d, i) { return svgY(d.y + d.y0); });
            	    }
            	    function resetPath() {
        	    		path = svg.append("g")
        	            .attr("class", "pathArea")
        	            .attr("clip-path", "url(#clip)")
        	            .selectAll("path")
        	            .data(aInnerStack)
        	            .enter()
        	            .append("path")
        	            .attr({
        	            	"class": "area ",
        	            	"fill": function(d, i) { return aRequestColor[i]; },
        	            	"d": area
        	            });
            	    }
            	    function redrawPath() {
            	        path.attr("d", area)
            	            .attr("transform", null)
            	            .transition()
            	            .attr("transform", "translate(" + svgX(0) + ")");
            	    }
            	    function resetTooltipLine() {
            	    	if ( showExtraInfo === false ) return;
            	        var vLineOutPosition = -(d3Options.margin.left + 100);
            	        svg
            	            .on("mouseout", function() {
            	                var position = d3.mouse(this);
            	                vLine
            	                	.attr("x1", vLineOutPosition)
            	                    .attr("x2", vLineOutPosition);
            	                tooltip
            	                    .attr("transform", "translate(" + vLineOutPosition + ", " + vLineOutPosition + ")");
            	                tooltipDate.text("");

            	                lastPosition = -1;
            	                lastIndex = -1;
            	            })
            	            .on("mousemove", function(d) {
            	                lastIndex = parseInt( svgX.invert( d3.mouse(this)[0] ) );
            	                lastPosition = parseInt( d3.mouse(this)[0] );
            	                lastPosition = lastPosition > width ? vLineOutPosition : lastPosition;
            	                vLine
            	                    .transition()
            	                    .delay(0)
            	                    .duration(0)
            	                    .attr("x1", lastPosition)
            	                    .attr("x2", lastPosition);
            	                resetTooltipLabel( getPositionData( lastIndex ) );
            	            });

            	        vLine = svg
            	            .append("line")
            	            .attr({
            	            	"class": "guideLine",
            	            	"x1": 0,
            	            	"y1": 10,
            	            	"x2": 0,
            	            	"y2": height
            	            });

            	        tooltip = svg
            	            .append("g")
            	            .attr("transform", function(d, i) {
            	                return "translate(-100,-100)";
            	            });
            	        tooltip
            	            .append("rect")
            	            .attr({
            	            	"width": 40,
            	            	"height": 90,
            	            	"fill": "#000",
            	            	"fill-opacity": "0.7"
            	            });

            	        tooltip
            	            .selectAll("text")
            	            .data( aRequestLabel )
            	            .enter()
            	            .append("text")
            	            .attr("x", function(d, i) {
            	                return 37;
            	            })
            	            .attr("y", function(d, i) {
            	                return ((aRequestColor.length - i - 1) * 20 + 20) + "px";
            	            })
            	            .attr("fill", function(d, i) {
            	                return aRequestColor[i];
            	            })
            	            .style("font-size", "12px")
            	            .text(function(d, i) {
            	                return aRequestLabel[i];
            	            });
            	        tooltipDate = svg.append("text")
            	        	.attr({
            	        		"x": "29%",
            	        		"y": "6px",
            	        		"fill": "#000",
            	        		"text-anchor": "middle",
            	        		"font-size": "14px"
            	        	})
            	            .text("");
            	    }
            	    function getPositionData( index ) {
            	        var a = [];
            	        if ( index === -1 ) return a;
            	        for( var i = 0 ; i < aInnerStack.length ; i++ ) {
            	            a.push( {
            	                y: aInnerStack[i][index] ? aInnerStack[i][index].y : 0,
            	                d: aInnerStack[i][index] ? aInnerStack[i][index].d : Date.now()
            	            });
            	        }
            	        return a;
            	    }
            	    function resetTooltipLabel( datum ) {
            	    	if ( showExtraInfo === false ) return;
            	        if ( lastPosition === -1 || lastIndex === -1 ) return;
            	        tooltip
            	            .attr("transform", "translate(" + (lastPosition - 44) + ", 10)")
            	            .selectAll("text")
            	            .attr("text-anchor", "end")
            	            .text(function(d, i) {
            	                return datum[i].y;
            	            });
            	        tooltipDate.text( d3.time.format("%Y.%m.%d %H:%M:%S")(new Date(datum[0].d)) );
            	    }
            	    function resetLabels( datum ) {
            	        if ( showExtraInfo === false ) return;
        	            labels = svg.append("g")
        	                .attr("transform", function(d, i) {
        	                    return "translate(" + (svgWidth - d3Options.margin.left - d3Options.margin.right + 4) + ",10)";
        	                })
        	                .selectAll("text")
        	                .data( aRequestLabel )
        	                .enter()
        	                .append("text")
        	                .attr("y", function(d, i) {
        	                    return ( (aRequestLabel.length - i - 1) / aRequestLabel.length) * 100 + "%";
        	                })
        	                .attr("fill", function(d, i) {
        	                    return aRequestColor[i];
        	                })
        	                .attr("font-size", "12px")
        	                .attr("font-weight", "bold")
        	                .text(function(d, i) {
        	                    return aRequestLabel[i];
        	                });
            	    }
            	    function resetErrorLabel() {
            	    	errorLabel = svg.append("text")
            	    		.attr({
            	    			"y": "40%",
            	    			"x": "50%",
            	    			"font-weight": "bold",
            	    			"text-anchor": "middle",
            	    			"fill": "#F00"
            	    		});
            	    }
            	    function resetLabelData( datum ) {
            	        if ( showExtraInfo === false ) return;
        	            labels
        	                .data( datum )
        	                .text(function(d, i) {
        	                    return typeof d.y !== "undefined" ? ( d.y + " : " + aRequestLabel[i] ) : aRequestLabel[i];
        	                });
            	    }
            	    function tick() {
            	        transition = transition.each(function() {
            	            if ( aRealTimeData.length === 0 ) return;

            	            var aNewData = aRealTimeData.shift();
            	            resetLabelData( aNewData );
            	            resetYAxis();

            	            var i = 0;
            	            for( i = 0 ; i < aInnerStack.length ; i++ ) {
            	                aInnerStack[i].push( aNewData[i] );
            	            }
            	            stack(aInnerStack)
            	            redrawPath();
            	            for( i = 0 ; i < aInnerStack.length ; i++ ) {
            	                aInnerStack[i].shift();
            	            }
            	            resetTooltipLabel( getPositionData( lastIndex ) );
            	        }).transition().each("start", function() {
            	            tick();
            	        });
            	    }
            	    tick();
	            	    
	            	scope.$on('realtimeChartDirective.onData.' + namespace, function (event, aNewRequestCount, timeStamp, maxY) {
	            		sumOfMaxY = maxY;
	            		errorLabel.text("");
	            		aRealTimeData.push( aNewRequestCount.map(function(v, i) {
	            			return {
	            				y: parseInt( v ),
	            				d: timeStamp
	            			}
	            		}) );
	    	        });
	            	scope.$on('realtimeChartDirective.onError.' + namespace, function (event, errorMessage, timeStamp, maxY) {
	            		sumOfMaxY = maxY;
	            		errorLabel.text( errorMessage );
	            		aRealTimeData.push( aInnerStack[aInnerStack.length - 1] );
	            	});
	            	scope.$on('realtimeChartDirective.clear.' + namespace, function (event, aNewRequestCount, timeStamp) {
	            		aRealTimeData.length = 0;
	            		aInnerStack.length = 0;
	            		element.html("");
	            		
	            		resetLabelData([{}, {}, {}, {}]);
	            		aInnerStack = getInitData();
	            		stack(aInnerStack);
	            		
	            		initGraph();
	            		resetAxis();
	            	    resetArea();
	            	    resetPath();
	            	    resetTooltipLine();
	            	    resetLabels();
	    	        });
	            }
	        };
	    }
	]);
})();