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