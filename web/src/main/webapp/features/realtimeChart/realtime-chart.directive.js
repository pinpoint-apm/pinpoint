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
	            	var aChartColor = scope[attrs["chartColor"]];
	            	var useLabel = attrs["useLabel"] === "true";
	            	var namespace = attrs["namespace"];
	            	var svgWidth = parseInt( attrs["width"] );
	            	var svgHeight = parseInt( attrs["height"] );
	            	
	            	var svg, svgX, svgY, yAxis, path, area, vLine, labels, tooltip, tooltipDate;
	            	var x_column = 10;
	            	var DEFAULT_Y_MAX = 50;
	            	var options = {
            	        labels: [ "Fast", "Normal", "Slow", "Very Slow"],
            	        domain: [1, x_column - 2],
            	        interpolation: "basis",
            	        margin: useLabel ? {top: 6, right: 80, bottom: 6, left: 30} : {top: 6, right: 80, bottom: 6, left: 20} 
            	    };
            	    var latelyXPosition = -1;
            	    var latelyIndex = -1;
            	    var xAxisLength = options.domain[1];
            	    var aInnerStack = options.datum || getInitData();
            	    var aRealTimeData = [];
            	    var transition = d3.select({}).transition().duration(1000).ease("linear");
            	    var width = svgWidth - ( useLabel ? options.margin.left : 0 ) - ( useLabel ? options.margin.right : 0 );
            	    var height = svgHeight - options.margin.top - options.margin.bottom;
            	    var sumOfMaxY = DEFAULT_Y_MAX;
//            	    var sumOfMaxY = sumOfMax(aInnerStack);
            	    var stack = d3.layout.stack().y(function(d) { return d.y; });
            	    stack(aInnerStack);

            	    initGraph();
            	    resetAxis();
            	    resetArea();
            	    resetPath();
            	    resetTooltipLine();
            	    resetLabels();

            	    function initGraph() {
            	        svg = d3.select( element.get(0) )
            	            .attr("width", svgWidth)
            	            .attr("height", svgHeight)
            	            .append("g")
            	            .attr("transform", "translate(" + (useLabel ? options.margin.left : 0) + "," + options.margin.top + ")");

            	        svg.append("defs").append("clipPath")
            	            .attr("id", "clip")
            	            .append("rect")
            	            .attr("width", width)
            	            .attr("height", height);
            	    }
            	    function getInitData() {
            	    	var a = [];
            	        for( var i = 0 ; i < options.labels.length ; i++ ) {
            	            a.push( d3.range(xAxisLength + 2).map(function() { return { y: 0 }; }) );
            	        }
            	        return a;
            	    }
//            	    function resetSumOfMax() {
//            	        sumOfMaxY = parseInt( sumOfMax(aInnerStack) ) + 10;
//            	    }
//            	    function sumOfMax(datum) {
//            	        var sum = 0;
//            	        for (var i = 0 ; i < datum.length ; i++ ) {
//            	            sum += Math.ceil( d3.max( datum[i], function( d ) {
//            	                return d.y;
//            	            }) );
//            	        }
//            	        return sum === 0 ? DEFAULT_Y_MAX : sum;
//            	    }
            	    function resetAxis() {
            	        svgX = d3.scale.linear().domain(options.domain).range([0, width]);
            	        svgY = d3.scale.linear().domain([0, sumOfMaxY]).range([height, 0]);
            	        if ( useLabel ) {
	            	        yAxis = d3.svg.axis().scale(svgY).ticks(3).orient("left");
	            	        svg.append("g").attr("class", "y axis");
            	        }
            	        resetYAxis();
            	    }
            	    function resetYAxis() {
            	        svgY = d3.scale.linear().domain([0, sumOfMaxY]).range([height, 0]);
            	        if ( useLabel ) {
	            	        yAxis = d3.svg.axis().scale(svgY).ticks(3).orient("left");
	            	        svg.selectAll("g.y.axis").call(yAxis);
            	        }
            	    }
            	    function resetArea() {
            	        area = d3.svg.area()
            	            .interpolate(options.interpolation)
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
        	            .attr("class", "area ")
        	            .attr("fill", function(d, i) { return aChartColor[i]; })
        	            .attr("d", area);
            	    }
            	    function redrawPath() {
            	        path.attr("d", area)
            	            .attr("transform", null)
            	            .transition()
            	            .attr("transform", "translate(" + svgX(0) + ")");
            	    }
//            	    function clearPath() {
//            	    	path.attr("d", area).attr("transform", null);
//            	    }
            	    function resetTooltipLine() {
            	    	if ( useLabel === false ) return;
            	        var vLineOutPosition = -(options.margin.left + 100);
            	        svg
            	            .on("mouseout", function() {
            	                var position = d3.mouse(this);
            	                vLine
            	                    .transition()
            	                    .attr("x1", vLineOutPosition)
            	                    .attr("x2", vLineOutPosition);
            	                tooltip
            	                    .attr("transform", "translate(" + vLineOutPosition + ", " + vLineOutPosition + ")");
            	                tooltipDate.text("");

            	                latelyXPosition = -1;
            	                latelyIndex = -1;
            	            })
            	            .on("mousemove", function(d) {
            	                latelyIndex = parseInt( svgX.invert( d3.mouse(this)[0] ) );
            	                latelyXPosition = parseInt( d3.mouse(this)[0] );
            	                latelyXPosition = latelyXPosition > width ? vLineOutPosition : latelyXPosition;
            	                vLine
            	                    .transition()
            	                    .delay(0)
            	                    .duration(0)
            	                    .attr("x1", latelyXPosition)
            	                    .attr("x2", latelyXPosition);
            	                resetTooltipLabel( getPositionData( latelyIndex ) );
            	            });

            	        vLine = svg
            	            .append("line")
            	            .attr("class", "guideLine")
            	            .attr("x1", 0)
            	            .attr("y1", 10)
            	            .attr("x2", 0)
            	            .attr("y2", height);

            	        tooltip = svg
            	            .append("g")
            	            .attr("transform", function(d, i) {
            	                return "translate(-100,-100)";
            	            });
            	        tooltip
            	            .append("rect")
            	            .attr("width", 20)
            	            .attr("height", 90)
            	            .attr("fill", "#000");

            	        tooltip
            	            .selectAll("text")
            	            .data( options.labels )
            	            .enter()
            	            .append("text")
            	            .attr("x", function(d, i) {
            	                return 6;
            	            })
            	            .attr("y", function(d, i) {
            	                return ((aChartColor.length - i - 1) * 20 + 20) + "px";
            	            })
            	            .attr("fill", function(d, i) {
            	                return aChartColor[i];
            	            })
            	            .style("font-size", "12px")
            	            .text(function(d, i) {
            	                return options.labels[i];
            	            });
            	        tooltipDate = svg.append("text")
            	            .attr("x", "29%")
            	            .attr("y", "6px")
            	            .attr("fill", "#000")
            	            .attr("text-anchor", "middle")
            	            .attr("font-size", "14px")
            	            .text("");
            	    }
            	    function getPositionData( index ) {
            	        var a = [];
            	        if ( index === -1 ) return a;
            	        for( var i = 0 ; i < aInnerStack.length ; i++ ) {
            	            a.push( {
            	                y: aInnerStack[i][index] ? aInnerStack[i][index].y : 0,
            	                d: aInnerStack[i][index].d
            	            });
            	        }
            	        return a;
            	    }
            	    function resetTooltipLabel( datum ) {
            	    	if ( useLabel === false ) return;
            	        if ( latelyXPosition === -1 || latelyIndex === -1 ) return;
            	        tooltip
            	            .attr("transform", "translate(" + (latelyXPosition - 30) + ", 10)")
            	            .selectAll("text")
            	            .text(function(d, i) {
            	                return datum[i].y;
            	            });
            	        tooltipDate.text( d3.time.format("%Y.%m.%d %H:%M:%S")(new Date(datum[0].d)) );
            	    }
            	    function resetLabels( datum ) {
            	        if ( useLabel === false ) return;
        	            labels = svg.append("g")
        	                .attr("transform", function(d, i) {
        	                    return "translate(" + (svgWidth - options.margin.left - options.margin.right + 4) + ",10)";
        	                })
        	                .selectAll("text")
        	                .data( options.labels )
        	                .enter()
        	                .append("text")
        	                .attr("y", function(d, i) {
        	                    return ( (options.labels.length - i - 1) / options.labels.length) * 100 + "%";
        	                })
        	                .attr("fill", function(d, i) {
        	                    return aChartColor[i];
        	                })
        	                .attr("font-size", "12px")
        	                .attr("font-weight", "bold")
        	                .text(function(d, i) {
        	                    return options.labels[i];
        	                });
            	    }
            	    function resetLabelData( datum ) {
            	        if ( useLabel === false ) return;
        	            labels
        	                .data( datum )
        	                .text(function(d, i) {
        	                    return typeof d.y !== "undefined" ? ( d.y + " : " + options.labels[i] ) : options.labels[i];
        	                });
            	    }
            	    function tick() {
            	        transition = transition.each(function() {
            	            if ( aRealTimeData.length === 0 ) return;

            	            var aNewData = aRealTimeData.shift();
            	            resetLabelData( aNewData );
//            	            resetSumOfMax();
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
            	            resetTooltipLabel( getPositionData( latelyIndex ) );
            	        }).transition().each("start", function() {
            	            tick();
            	        });
            	    }
            	    tick();
	            	    
	            	scope.$on('realtimeChartDirective.onData.' + namespace, function (event, aNewRequestCount, timeStamp, maxY) {
	            		sumOfMaxY = maxY;

	            		aRealTimeData.push( (function() {
	            			var a = [];
	            			for (var i =  0 ; i < aNewRequestCount.length ; i++ ) {
	            				a.push({
//	            					@TestCode
	            					y: parseInt(aNewRequestCount[i] + (Math.random() * 10)),
//	            					y: parseInt( aNewRequestCount[i]),
	            					d: timeStamp
	            				});
	            			}
	            			return a;
	            		})() );
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