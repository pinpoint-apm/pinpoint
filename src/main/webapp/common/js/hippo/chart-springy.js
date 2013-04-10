var getRandomColor = function(){
	var nR = getRandom(0, 255),
		nG = getRandom(0, 255),
		nB = getRandom(0, 255);
	return 'rgb('+nR+','+nG+','+nB+')';
}

function getRandom(M, N){
	return Math.floor(M + (1+N-M)*Math.random());
}

function showServerMap(applicationName) {
    $("#springygraph").empty();

	var serverMapCallback = function(data) {
		if (data.graphdata.nodes.length == 0) {
			warning("NO DATA", "");
		} else {
			clearAllWarnings();
		}
		
        drawSpringy(applicationName, data.graphdata, "#springygraph", 1100, 500);
    	$("#springygraph").css("display", "");
    };

    if (isQueryFromNow()) {
        getLastServerMapData2($("#application").val(), getQueryPeriod(), serverMapCallback);
    } else {
        getServerMapData2($("#application").val(), getQueryStartTime(), getQueryEndTime(), serverMapCallback);
    }
}

function drawSpringy(applicationName, graphdata, targetId, width, height) {
	// $(targetId).attr("width", width);
	// $(targetId).attr("height", height);

	var graph = new Graph();
	
	var aNodes = graphdata.nodes,
	aLinks = graphdata.links,
	aoNodes = [],
	aColor = ['#00A0B0', '#6A4A3C', '#CC333F', '#EB6841', '#EDC951', 
			  '#7DBE3C', '#00A0B0', '#6A4A3C', '#CC333F', '#EB6841'];	

	for(var i=0; i<aNodes.length; i++){
		aoNodes[i] = graph.newNode({
									key : (aNodes[i].name == applicationName) ? true : false,
									id : i,
									label: aNodes[i].name, 
									width : 100,
									height : 80,
									rawdata : aNodes[i],
									serviceType : aNodes[i].serviceType,
									hosts : aNodes[i].hosts,
									agents : aNodes[i].agents,
									onMouseOver : function(e){
										$('#console').val('Node onMouseOver : ' + this.id + '\r' + $('#console').val())
									},
									onMouseClick : function(e){
										if (this.data.serviceType == "CLIENT") {
											if ($("DIV.nodeinfo" + this.data.id).length == 0) {
												var htOffset = $(targetId).offset();
												var box = $('#ClientBox')
																.tmpl(this.data)
																.css({'top':e.pageY - htOffset.top, 'left':e.pageX - htOffset.left})
																.attr('class', 'nodeinfo' + this.data.id);
												box.appendTo(targetId);
											}	
										} else {
											if ($("DIV.nodeinfo" + this.data.id).length == 0) {
												var htOffset = $(targetId).offset();
												var box = $('#ServerBox')
												.tmpl(this.data)
												.css({'top':e.pageY - htOffset.top, 'left':e.pageX - htOffset.left})
												.attr('class', 'nodeinfo' + this.data.id);
												box.appendTo(targetId);
											}
										}
									}
									});
	}
	
	for(var i=0; i<aLinks.length; i++){
		graph.newEdge(aoNodes[aLinks[i]['source']], aoNodes[aLinks[i]['target']], {
																					id : i,
																					value : aLinks[i]['value'],
																					width : 100,
																					height : 80,
																					error : aLinks[i]['error'],
																					slow : aLinks[i]['slow'],
																					histogram : aLinks[i]['histogram'],
																					rawdata : aLinks[i],
																					onMouseOver : function(e){
																						$('#console').val('Edge onMouseOver : ' + this.id + '\r' + $('#console').val());
																					},
																					onMouseClick : function(e){
																						// TODO 정보 layer가 중복으로 보이지 않도록 함.
																						if ($("DIV.linkinfo" + this.data.id).length > 0) {
																							return;
																						}
																						
																						var htOffset = $(targetId).offset();
																						var box = $('#EdgeBox')
																									.tmpl(this.data)
																									.css({'top':e.pageY - htOffset.top, 'left':e.pageX - htOffset.left})
																									.attr('class', 'linkinfo' + this.data.id);
																						box.appendTo(targetId);
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
		// resize();
		var springy = jQuery(targetId).hippoServerMap({
			graph: graph,
			sEdgeColor : '#8f8f8f',
			sEdgeSelectedColor : '#28a1f7',
			sEdgeBoxColor : '#000000',
			sNodeBgKeyColor : '#FFCCCC',
			nEdgeWeight : 1
		});
	});
}