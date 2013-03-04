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
									serviceType : aNodes[i].serviceType,
									width : 100,
									height : 80,
									server : aNodes[i].agentIds,
									onMouseOver : function(e){
										$('#console').val('Node onMouseOver : ' + this.id + '\r' + $('#console').val())
									},
									onMouseClick : function(e){
										// $('#console').val('Node onMouseClick : ' + this.id + '\r' + $('#console').val())
										if (this.data.server.length > 0) {
											var htOffset = $(targetId).offset();
											$('#ServerBox').tmpl(this.data).css({'top':e.pageY - htOffset.top, 'left':e.pageX - htOffset.left}).appendTo(targetId);
										}
									}
									});
	}
	
	for(var i=0; i<aLinks.length; i++){
		graph.newEdge(aoNodes[aLinks[i]['source']], aoNodes[aLinks[i]['target']], {
																					value : aLinks[i]['value'],
																					width : 100,
																					height : 80,
																					error : aLinks[i]['error'],
																					slow : aLinks[i]['slow'],
																					histogram : aLinks[i]['histogram'],
																					onMouseOver : function(e){
																						$('#console').val('Edge onMouseOver : ' + this.id + '\r' + $('#console').val());
																					},
																					onMouseClick : function(e){
																						var htOffset = $(targetId).offset();
																						$('#console').val('Edge onMouseClick : ' + this.id + '\r' + $('#console').val());
																						$('#EdgeBox').tmpl(this.data).css({'top':e.pageY - htOffset.top, 'left':e.pageX - htOffset.left}).appendTo(targetId);
																					},
																					onMouseOut : function(e){
																						
																					}
																				  });
	}
	
	jQuery(function(){
		function resize(){
			$(targetId).attr({
				'width' : $(window).width(),
				'height' : $(window).height()
			});
		}
		resize();
		var springy = jQuery(targetId).hippoServerMap({
			graph: graph,
			sEdgeColor : '#8f8f8f',
			sEdgeSelectedColor : '#28a1f7',
			sEdgeBoxColor : '#000000',
			nEdgeWeight : 1
		});
	});
}