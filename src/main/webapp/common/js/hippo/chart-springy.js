function drawSpringy(graphdata, targetId, width, height) {
	$(targetId).attr("width", width);
	$(targetId).attr("height", height);
	
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