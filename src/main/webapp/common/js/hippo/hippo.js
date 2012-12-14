function drawSpringy(graphdata, targetId, width, height) {
	var graph = new Graph();
	var nodes = [];
	
	for(var i = 0; i < graphdata.nodes.length; i++) {
		nodes[i] = graph.newNode({
			label : graphdata.nodes[i].name,
			serviceType : graphdata.nodes[i].serviceType
		});
	}
	
	for(var i = 0; i < graphdata.links.length; i++) {
		var src = nodes[graphdata.links[i].source];
		var target = nodes[graphdata.links[i].target];
        graph.newEdge(src, target, {
        	color : '#7DBE3C',
        	label : graphdata.links[i].value
		});
	}
	
	jQuery(function(){
		var springy = jQuery(targetId).springy({
			graph: graph,
			nodeSelected: function(node){
				console.log('Node selected: ' + JSON.stringify(node.data));
			}
		});
	});
}

function drawTree(graphdata, targetId, width, height) {
	/*
	JSON format is...
	
	AS-IS
	"nodes" : [
	    {	"name" : "hippo",
		    "recursiveCallCount" : "0",
		    "agentIds" : [ ],
		    "serviceType" : "MYSQL",
		    "terminal" : "true"
	    } ,
	],
	"links" : [
	    {	"source" : 1,
			"target" : 3,
			"value" : 2,
			"histogram" : []
		} ,
	]

	TO-BE
	{
		 "name": "flare",
		 "children": [
		  {
		   "name": "analytics",
		   "children": [
		    {
		     "name": "cluster",
		     "children": [
		      {"name": "AgglomerativeCluster", "size": 3938},
		      {"name": "CommunityStructure", "size": 3812},
		      {"name": "HierarchicalCluster", "size": 6714},
		      {"name": "MergeEdge", "size": 743}
		     ]
		    },
		    ...
	*/
	function buildTree() {
		var nodes = graphdata.nodes;
		var links = graphdata.links;
		
		// find root node
		var rootIndex = -1;
		var rootName = $("#application").val();
		nodes.forEach(function(e, i) {
			if (e.name == rootName) {
				rootIndex = i;
			}
		});
		
		if (rootIndex < 0) {
			return { name : "Can't find the root", children : [ { name : "Can't find children" }, { name : "Can't find children" } ] };
		}
		
		// build tree
		var tree = makeNodes(rootIndex, nodes, links);
		
		function makeNodes(parent, nodes, links) {
			var node = {};
			node.name = nodes[parent].name;
			node.children = [];
			
			links.forEach(function(link, i) {
				if (link.source == parent) {
					var child = {};
                    child.name = nodes[link.target].name + ", (" + link.value + "calls)";
					
					var children = makeNodes(link.target, nodes, links).children;
					if (children.length > 0) {
						child.children = children;
					}
					child.side = 10;
					node.children.push(child);
				}
			});
			return node;
		}
		return tree;
	}
	
	var json = buildTree();
	
	var m = [20, 120, 20, 120],
	    w = 1280 - m[1] - m[3],
	    h = 800 - m[0] - m[2],
	    i = 0,
	    root;

	var tree = d3.layout.tree()
	    .size([h, w]);

	var diagonal = d3.svg.diagonal()
	    .projection(function(d) { return [d.y, d.x]; });

	var vis = d3.select(targetId).append("svg:svg")
	    .attr("width", w + m[1] + m[3])
	    .attr("height", h + m[0] + m[2])
	  .append("svg:g")
	    .attr("transform", "translate(" + m[3] + "," + m[0] + ")");

	root = json;
	root.x0 = h / 2;
	root.y0 = 0;

	function toggleAll(d) {
		if (d.children) {
			d.children.forEach(toggleAll);
			toggle(d);
		}
	}

	update(root);

	function update(source) {
	  var duration = d3.event && d3.event.altKey ? 5000 : 500;

	  // Compute the new tree layout.
	  var nodes = tree.nodes(root).reverse();

	  // Normalize for fixed-depth.
	  nodes.forEach(function(d) { d.y = d.depth * 180; });

	  // Update the nodes…
	  var node = vis.selectAll("g.node")
	      .data(nodes, function(d) { return d.id || (d.id = ++i); });

	  // Enter any new nodes at the parent's previous position.
	  var nodeEnter = node.enter().append("svg:g")
	      .attr("class", "node")
	      .attr("transform", function(d) { return "translate(" + source.y0 + "," + source.x0 + ")"; })
	      .on("click", function(d) { toggle(d); update(d); });

	  nodeEnter.append("svg:circle")
	      .attr("r", 1e-6)
	      .style("fill", function(d) { return d._children ? "lightsteelblue" : "#fff"; });

	  nodeEnter.append("svg:text")
	      .attr("x", function(d) { return d.children || d._children ? -10 : 10; })
	      .attr("dy", ".35em")
	      .attr("text-anchor", function(d) { return d.children || d._children ? "end" : "start"; })
	      .text(function(d) { return d.name; })
	      .style("fill-opacity", 1e-6);

	  // Transition nodes to their new position.
	  var nodeUpdate = node.transition()
	      .duration(duration)
	      .attr("transform", function(d) { return "translate(" + d.y + "," + d.x + ")"; });

	  nodeUpdate.select("circle")
	      .attr("r", 4.5)
	      .style("fill", function(d) { return d._children ? "lightsteelblue" : "#fff"; });

	  nodeUpdate.select("text")
	      .style("fill-opacity", 1);

	  // Transition exiting nodes to the parent's new position.
	  var nodeExit = node.exit().transition()
	      .duration(duration)
	      .attr("transform", function(d) { return "translate(" + source.y + "," + source.x + ")"; })
	      .remove();

	  nodeExit.select("circle")
	      .attr("r", 1e-6);

	  nodeExit.select("text")
	      .style("fill-opacity", 1e-6);

	  // Update the links…
	  var link = vis.selectAll("path.link")
	      .data(tree.links(nodes), function(d) { return d.target.id; });

	  // Enter any new links at the parent's previous position.
	  link.enter().insert("svg:path", "g")
	      .attr("class", "link")
	      .attr("d", function(d) {
	        var o = {x: source.x0, y: source.y0};
	        return diagonal({source: o, target: o});
	      })
	    .transition()
	      .duration(duration)
	      .attr("d", diagonal);

	  // Transition links to their new position.
	  link.transition()
	      .duration(duration)
	      .attr("d", diagonal);

	  // Transition exiting nodes to the parent's new position.
	  link.exit().transition()
	      .duration(duration)
	      .attr("d", function(d) {
	        var o = {x: source.x, y: source.y};
	        return diagonal({source: o, target: o});
	      })
	      .remove();

	  // Stash the old positions for transition.
	  nodes.forEach(function(d) {
	    d.x0 = d.x;
	    d.y0 = d.y;
	  });
	}

	// Toggle children.
	function toggle(d) {
	  if (d.children) {
	    d._children = d.children;
	    d.children = null;
	  } else {
	    d.children = d._children;
	    d._children = null;
	  }
	}
}

function drawSankeyChart(graphdata, targetId, w, h) {
	var margin = {
		    top:1,
		    right:1,
		    bottom:6,
		    left:1
		}, width = w - margin.left - margin.right, height = h - margin.top - margin.bottom;

	var formatNumber = d3.format(",.0f"), format = function (d) {
	    	return formatNumber(d) + " Requests";
		}, color = d3.scale.category20();

    var svg = d3.select(targetId).append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)
            .append("g").attr("transform", "translate(" + margin.left + "," + margin.top + ")");
    var sankey = d3.sankey().nodeWidth(15).nodePadding(10).size([ width, height ]);
    var path = sankey.link();

	sankey.nodes(graphdata.nodes).links(graphdata.links).layout(32);
	
	var link = svg.append("g").selectAll(".link").data(graphdata.links)
	        .enter().append("path")
	        .attr("class", "link")
	        .attr("d", path).style("stroke-width",function (d) {
	            return Math.max(1, d.dy);
	        }).sort(function (a, b) {
	            return b.dy - a.dy;
	        }).on("click", function (a, b) {
	            //console.log(a);
	            //console.log(b);
	        });
	
	link.append("title").text(
	        function (d) {
	        	var histogram = "";
	        	$(d.histogram).each(function(i, e) {
	        		histogram += e.from + "~" + e.to + "ms = " + e.value + "\n";
	        	});
	        	
	            return	d.source.name + " → " + d.target.name + "\n" +
	            		format(d.value) + "\n" + histogram;
	        });
	
	var node = svg.append("g").selectAll(".node").data(graphdata.nodes)
	        .enter().append("g").attr("class", "node").attr(
	        "transform",function (d) {
	            return "translate(" + d.x + "," + d.y + ")";
	        }).call(d3.behavior.drag().origin(function (d) {
	    return d;
	}).on("dragstart",function () {
	            this.parentNode.appendChild(this);
	        }).on("drag", dragmove));
	node.on("click", function (d) {
		alert(d.agentIds);
	});
	
	node.append("rect").attr("height",function (d) {
	    return d.dy;
	}).attr("width", sankey.nodeWidth()).style("fill",function (d) {
	            return d.color = color(d.name.replace(/ .*/, ""));
	        }).style("stroke",function (d) {
	            return d3.rgb(d.color).darker(2);
	        }).append("title").text(function (d) {
                return	"applicationName\n\t" + d.name +
                		"\nTotal Requests\n\t" + format(d.value) +
                		((d.agentIds) ? "\nServers\n\t" + d.agentIds.join('\n\t') : "") +
                		((d.serviceType) ? "\nServiceType\n\t" + d.serviceType : "");
	        });
	
	node.append("text").attr("x", -6).attr("y",function (d) {
	    return d.dy / 2;
	}).attr("dy", ".35em").attr("text-anchor", "end").attr("transform",
	        null).text(function (d) {
	            return d.name + ((d.recursiveCallCount > 0) ? " (recursive=" + d.recursiveCallCount + ")" : "");
	        }).html(function (d) {
	        	return null;
	        }).filter(function (d) {
	            return d.x < width / 2;
	        }).attr("x", 6 + sankey.nodeWidth()).attr("text-anchor", "start");
	
	function dragmove(d) {
	    d3.select(this).attr(
	            "transform",
	            "translate("
	                    + d.x
	                    + ","
	                    + (d.y = Math.max(0, Math.min(height - d.dy,
	                    d3.event.y))) + ")");
	    sankey.relayout();
	    link.attr("d", path);
	}
}

var selectedTraceIdSet = {};

function drawScatter(data, targetId) {
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
	      .attr("r", 3)
	      .attr("cx", function(d) { return x(d.timestamp); })
	      .attr("cy", function(d) { return y(d.executionTime); })
	      .style("fill", function(d) { return color(d.name); })
	      .on("click", function(d) { openTrace(d.traceId); });

	  var legend = svg.selectAll(".legend")
	      .data(color.domain())
	    .enter().append("g")
	      .attr("class", "legend")
	      .attr("transform", function(d, i) { return "translate(0," + i * 20 + ")"; });

	  legend.append("rect")
	      .attr("x", width - 18)
	      .attr("width", 18)
	      .attr("height", 18)
	      .style("fill", color);

	  legend.append("text")
	      .attr("x", width - 24)
	      .attr("y", 9)
	      .attr("dy", ".35em")
	      .style("text-anchor", "end")
	      .text(function(d) { return d; });
}

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