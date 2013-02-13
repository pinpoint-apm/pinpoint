var getRandomColor = function(){
	var nR = getRandom(0, 255),
		nG = getRandom(0, 255),
		nB = getRandom(0, 255);
	return 'rgb('+nR+','+nG+','+nB+')';
}

function getRandom(M, N){
	return Math.floor(M + (1+N-M)*Math.random());
}

function drawSpringy(graphdata, targetId, width, height) {
	$(targetId).attr("width", width);
	$(targetId).attr("height", height);

	var graph = new Graph();
	
	var aNodes = graphdata.nodes,
		aLinks = graphdata.links,
		aoNodes = [],
		aColor = ['#00A0B0', '#6A4A3C', '#CC333F', '#EB6841', '#EDC951', 
				  '#7DBE3C', '#00A0B0', '#6A4A3C', '#CC333F', '#EB6841'];
	
	for(var i=0; i<aNodes.length; i++){
		aoNodes[i] = graph.newNode({
									label: aNodes[i].name, 
									helth : aNodes[i].helth, 
									serviceType : aNodes[i].serviceType,
									width : 100,
									height : 80
									});
	}
	for(var i=0; i<aLinks.length; i++){
		graph.newEdge(aoNodes[aLinks[i]['source']], aoNodes[aLinks[i]['target']], {
																					color: getRandomColor(), 
																					value : aLinks[i]['value'],
																					width : 100,
																					height : 80
																				  });
	}

	jQuery(function(){
		function resize(){
			$('#springydemo').attr({
				'width' : width, // $(window).width(),
				'height' : height // $(window).height()
			});
		}
		resize();
		var springy = jQuery(targetId).hippoServerMap({
			graph: graph,
			sLinkColor : '#8f8f8f',
			sLinkSelectedColor : '#28a1f7',
			sLinkBoxColor : '#000000',
			nLinkWeight : 1,
			nodeSelected: function(node){
				console.log('Node selected: ' + JSON.stringify(node.data));
			}
		});
	});
	
	/*
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
	*/
}