
function drawScatter(data, targetId) {
	var selectedTraceIdSet = {};

	//Brush.
	var brush = d3.svg.brush()
	    .on("brushstart", brushstart)
	    .on("brush", brush)
	    .on("brushend", brushend);
	    
	function brushstart(p) {
	}

	// find selected dot
	function brush(p) {
	  var e = brush.extent();
	  svg.selectAll(".dot").each(function(d) {
	    if( new Date(e[0][0]).getTime() <= d.timestamp && d.timestamp <= new Date(e[1][0]).getTime()
	        && e[0][1] <= d.executionTime && d.executionTime <= e[1][1]) {
	    	selectedTraceIdSet[d.traceId] = d;
	    }
	  });
	}

	function brushend() {
		displaySelectedTraceIdList(selectedTraceIdSet);
		selectedTraceIdSet = {};
	}

	var w = 960;
	var h = 500;
	
	var margin = {top: 20, right: 20, bottom: 30, left: 40},
	    width = w - margin.left - margin.right,
	    height = h - margin.top - margin.bottom;

	var x = d3.time.scale().range([0, width]);
	var y = d3.scale.linear().range([height, 0]);
	var color = d3.scale.category10();

	var xAxis = d3.svg.axis().scale(x).orient("bottom");
	var yAxis = d3.svg.axis().scale(y).orient("left");
	
	 svg = d3.select(targetId).append("svg")
	    .attr("width", width + margin.left + margin.right)
	    .attr("height", height + margin.top + margin.bottom)
	  .append("g")
	    .attr("transform", "translate(" + margin.left + "," + margin.top + ")")
	 	.call(brush.x(x).y(y));
	
	  //data.forEach(function(d) {
	  //  d.timestamp = +d.timestamp;
	  //  d.executionTime = +d.executionTime;
	  //});

	  x.domain(d3.extent(data, function(d) { return d.timestamp; }));//.nice();
	  y.domain(d3.extent(data, function(d) { return d.executionTime; }));//.nice();

	  svg.append("g")
	      .attr("class", "x axis")
	      .attr("transform", "translate(0," + height + ")")
	      .call(xAxis)
	    .append("text")
	      .attr("class", "label")
	      .attr("x", width)
	      .attr("y", -6)
	      .style("text-anchor", "end")
	      .text("Timestamp (HH:mm)");

	  svg.append("g")
	      .attr("class", "y axis")
	      .call(yAxis)
	    .append("text")
	      .attr("class", "label")
	      .attr("transform", "rotate(-90)")
	      .attr("y", 6)
	      .attr("dy", ".71em")
	      .style("text-anchor", "end")
	      .text("Execute time (ms)")

	  svg.selectAll(".dot")
	      .data(data)
	    .enter().append("circle")
	      .attr("class", "dot")
	      .attr("r", 2.5)
	      .attr("cx", function(d) { return x(d.timestamp); })
	      .attr("cy", function(d) { return y(d.executionTime); })
	      .style("fill", function(d) {
	    	  if (d.exception) {
	    		  return "#d62728"; // red
	    	  //} else if (d.executionTime > 500) {
	    	  //	return "#ff7f0e"; // orange
	    	  } else {
	    		  return "#2ca02c"; // green
	    	  }
	    	  //return color(d.name);
	      })
	      .on("click", function(d) { openTrace(d.traceId); });
	  

	  
	  
	  
	  function openTrace(uuid) {
		  window.open("/selectTransaction.hippo?traceId=" + uuid);
	  }
	  
	  function displaySelectedTraceIdList(traces) {
		  var keys = Object.keys(traces);
		  
		  if (keys.length == 0) {
			  return;
		  }
		  
		  
		  if (keys.length == 1) {
			  openTrace(keys[0]);
			  return;
		  }
		  
		  $("#selectedBusinessTransactionsDetail TBODY").empty();
		  
		  var html = [];
		  for (var i = 0; i < keys.length; i++) {
			  html.push("<tr>");
			  
			  html.push("<td>");
			  html.push(i + 1);
			  html.push("</td>");
			  
			  html.push("<td>");
			  html.push(new Date(traces[keys[i]].timestamp));
			  html.push("</td>");
			  
			  html.push("<td><a href='#' onclick='openTrace(\"");
			  html.push(traces[keys[i]].traceId);
			  html.push("\");'>");
			  html.push(traces[keys[i]].traceId);
			  html.push("</a></td>");
			  
			  html.push("<td>");
			  html.push(traces[keys[i]].executionTime);
			  html.push("</td>");
			  
			  html.push("<td>");
			  html.push(traces[keys[i]].name);
			  html.push("</td>");
			  
			  html.push("</tr>");
		  }
		  $("#selectedBusinessTransactionsDetail TBODY").append(html.join(''));
		  
		  $('#traceIdSelectModal').modal({});
	  }
}
