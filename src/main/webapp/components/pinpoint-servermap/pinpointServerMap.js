(function() {

jQuery.fn.pinpointServerMap = function(params) {
	var graph = this.graph = params.graph || new Graph();

	var stiffness = params.stiffness || 400.0;
	var repulsion = params.repulsion || 400.0;
	var damping = params.damping || 0.5;
	var sEdgeColor = params.sEdgeColor || '#8f8f8f',
		sEdgeHoverColor = params.sEdgeHoverColor || '#770f73',
		sEdgeInHoverColor = params.sEdgeInHoverColor || '#f77128',
		sEdgeOutHoverColor = params.sEdgeOutHoverColor || '#28a1f7',
		sEdgeBoxBorderColor = params.sEdgeBoxColor || '#000000',
		sEdgeBoxBgColor = params.sEdgeBoxBgColor || 'rgba(255, 255, 255, 1)',
		sEdgeBoxBgHoverColor = params.sEdgeBoxBgHoverColor || 'rgba(255, 255, 255, 1)',
		nEdgeWeight = params.nEdgeWeight || 1,
		nEdgeHoverWeight = params.nEdgeHoverWeight || 20,
		nEdgeArrowLength = params.nEdgeArrowLength || 10,
		nRecursiveEdgeRadius = params.nRecursiveEdgeRadius || 20;
	var sNodeBorderColor = params.sNodeBorderColor || '#8f8f8f',
		sNodeBorderHoverColor = params.sNodeBorderHoverColor || '#28a1f7',
		sNodeBgHoverFromColor = params.sNodeBgHoverFromColor || 'rgba(255, 255, 255, 1)',
		sNodeBorderFromHoverColor = params.sNodeBorderFromHoverColor || '#f77128',
		sNodeBgHoverToColor = params.sNodeBgHoverToColor || 'rgba(255, 255, 255, 1)',
		sNodeBorderToHoverColor = params.sNodeBorderToHoverColor || '#28a1f7',
		sNodeBgColor = params.sNodeBgColor || 'rgba(255, 255, 255, 0.5)',
		sNodeBgHoverColor = params.sNodeBgHoverColor || 'rgba(255, 255, 255, 1)',
		sNodeBgKeyColor = params.sNodeBgKeyColor || 'rgba(255, 255, 255, 1)';
	var nZoomGap = params.nZoomGap || 100,
		nZoomMaxGap = params.nZoomGap || 500;

	var iconPath = params.iconPath || '/images/servermap';
	
	//var canvas = this[0];
	var nThisWidth = $(this).parent().width(), // $(this).width(),
		nThisHeight = $(this).height();
	var canvas = document.createElement('canvas');
	canvas.width = nThisWidth /** (1 + params.graph.nodes.length / 100)*/;
	canvas.height = nThisHeight /** (1 + params.graph.nodes.length / 100)*/;
	$(canvas).css({
		'position' : 'absolute',
		'top' : '0px',
		'left' : '0px'
	});
	$(this).append(canvas);
	var ctx = canvas.getContext("2d");

	var layout = this.layout = new Layout.ForceDirected(graph, stiffness, repulsion, damping);

	// calculate bounding box of graph layout.. with ease-in
	var currentBB = layout.getBoundingBox();
	var targetBB = {bottomleft: new Vector(-2, -2), topright: new Vector(2, 2)};

	// auto adjusting bounding box
	Layout.requestAnimationFrame(function adjust() {
		targetBB = layout.getBoundingBox();
		// current gets 20% closer to target every iteration
		currentBB = {
			bottomleft: currentBB.bottomleft.add( targetBB.bottomleft.subtract(currentBB.bottomleft)
				.divide(10)),
			topright: currentBB.topright.add( targetBB.topright.subtract(currentBB.topright)
				.divide(10))
		};

		Layout.requestAnimationFrame(adjust);
	});

	// convert to/from screen coordinates
	toScreen = function(p) {
		var size = currentBB.topright.subtract(currentBB.bottomleft);
		var sx = p.subtract(currentBB.bottomleft).divide(size.x).x * canvas.width;
		var sy = p.subtract(currentBB.bottomleft).divide(size.y).y * canvas.height;
		return new Vector(sx, sy);
	};

	fromScreen = function(s) {
		var size = currentBB.topright.subtract(currentBB.bottomleft);
		var px = (s.x / canvas.width) * size.x + currentBB.bottomleft.x;
		var py = (s.y / canvas.height) * size.y + currentBB.bottomleft.y;
		return new Vector(px, py);
	};

	// half-assed drag and drop
	var selected = null,
		selectedNode = null,
		nearest = null,
		dragged = null;
	var htLastPos = {pageX : 0, pageY : 0};
	var htGapBetweenThisAndCanvas = {gapX : $(this).width()- $(canvas).width(), gapY : $(this).height() - $(canvas).height()};

	/*
	jQuery(canvas).bind('mousewheel', function(e){
		e.preventDefault();
		if(e.originalEvent.wheelDelta > 0){// �뺣�
			canvas.width = canvas.width + nZoomGap;
			canvas.height = canvas.height + nZoomGap;
		}else{// 異뺤냼
			canvas.width = canvas.width - nZoomGap;
			canvas.height = canvas.height - nZoomGap;
		}
		if(canvas.width < nThisWidth){
			canvas.width = nThisWidth;
		}
		if(canvas.height < nThisHeight){
			canvas.height = nThisHeight;
		}
		if(canvas.width > (nThisWidth + nZoomMaxGap)){
			canvas.width = nThisWidth+ nZoomMaxGap;
			canvas.height = nThisHeight + nZoomMaxGap;
		}
		htGapBetweenThisAndCanvas = {gapX : nThisWidth - canvas.width, gapY : nThisHeight - canvas.height};
		if(htGapBetweenThisAndCanvas.gapX > parseInt(jQuery(this).css('left'), 10)){
			jQuery(this).css('left', htGapBetweenThisAndCanvas.gapX + 'px');
		}
		if(htGapBetweenThisAndCanvas.gapY > parseInt(jQuery(this).css('top'), 10)){
			jQuery(this).css('top', htGapBetweenThisAndCanvas.gapY + 'px');
		}
		renderer.startOnce();
	});
	*/

	//
	// TODO double click 할 때 정보를 보여주도록 수정함.
	//
	jQuery(canvas).mousedown(function(e) {
		e.preventDefault();	
		var pos = jQuery(this).offset();
		selectedNode = layout.selectNode({x: e.pageX - pos.left, y: e.pageY - pos.top});		
		selectedEdge = layout.edgeHover({x: e.pageX - pos.left, y: e.pageY - pos.top}, nEdgeHoverWeight);
		if(selectedNode.node !== null){
			selected = true;
//			if(typeof selectedNode.node.data.onMouseClick === 'function'){
//				selectedNode.node.data.onMouseClick.call(selectedNode.node, e);
//			}
			$(this).css('cursor','pointer');
		}else if(selectedEdge.edge !== null){
//			if(typeof selectedEdge.edge.data.onMouseClick === 'function'){
//				selectedEdge.edge.data.onMouseClick.call(selectedEdge.edge, e);
//			}
		}else{
			// TODO 공백에서 드래그 했을 때 그래프가 사라지는 현상 방지.
			if (selectedNode.node !== null) {
				dragged = true;	
				htLastPos.pageY = e.pageY;
				htLastPos.pageX = e.pageX;
			}
		}
	});
	
	jQuery(canvas).dblclick(function(e) {
		e.preventDefault();	
		var pos = jQuery(this).offset();
		selectedNode = layout.selectNode({x: e.pageX - pos.left, y: e.pageY - pos.top});		
		selectedEdge = layout.edgeHover({x: e.pageX - pos.left, y: e.pageY - pos.top}, nEdgeHoverWeight);
		if(selectedNode.node !== null){
//			selected = true;
			if(typeof selectedNode.node.data.onMouseClick === 'function'){
				selectedNode.node.data.onMouseClick.call(selectedNode.node, e);
			}
//			$(this).css('cursor','pointer');
		}else if(selectedEdge.edge !== null){
			if(typeof selectedEdge.edge.data.onMouseClick === 'function'){
				selectedEdge.edge.data.onMouseClick.call(selectedEdge.edge, e);
			}
		}
//		else{		
//			dragged = true;	
//			htLastPos.pageY = e.pageY;
//			htLastPos.pageX = e.pageX;			
//		}
	});

	var lastSelectedNodeId = null, lastSelectedEdge = {edge : null};
	jQuery(canvas).mousemove(function(e) {
		e.preventDefault();	
		if (dragged){
			var pos = jQuery(this).offset();
			var nTop = parseInt(jQuery(this).css('top'), 10),
				nLeft = parseInt(jQuery(this).css('left'), 10);		
			
			var nNewTop = nTop - (htLastPos.pageY - e.pageY),
				nNewLeft = nLeft - (htLastPos.pageX - e.pageX);

			if(nNewTop > 0) nNewTop = 0;
			if(nNewLeft > 0) nNewLeft = 0;
			if(nNewTop < htGapBetweenThisAndCanvas.gapY) nNewTop = htGapBetweenThisAndCanvas.gapY;
			if(nNewLeft < htGapBetweenThisAndCanvas.gapX) nNewLeft = htGapBetweenThisAndCanvas.gapX;
			jQuery(this).css({
				'top' :  nNewTop,
				'left' : nNewLeft
			});
			htLastPos.pageY = e.pageY;
			htLastPos.pageX = e.pageX;			
		}else if(selected){
			var pos = jQuery(this).offset();
			var p = fromScreen({x: e.pageX - pos.left, y: e.pageY - pos.top});
			
			if (selectedNode.node !== null) {
				selectedNode.point.p.x = p.x;
				selectedNode.point.p.y = p.y;				
			}

			renderer.startOnce();
			$(this).css('cursor','pointer');
		}else{
			var pos = jQuery(this).offset();
			selectedNode = layout.nodeHover({x: e.pageX - pos.left, y: e.pageY - pos.top});	
			
			if (selectedNode.node !== null) {
				if(selectedNode.node.id != lastSelectedNodeId){
					lastSelectedNodeId = selectedNode.node.id;
					if(typeof selectedNode.node.data.onMouseOver === 'function'){
						selectedNode.node.data.onMouseOver.call(selectedNode.node, e);
					}
					renderer.startOnce();
					$(this).css('cursor','pointer');
				}
			}else{
				if(lastSelectedNodeId !== null){
					renderer.startOnce();
					$(this).css('cursor','pointer');
				}
				lastSelectedNodeId = null;
				$(this).css('cursor','auto');
			}

			selectedEdge = layout.edgeHover({x: e.pageX - pos.left, y: e.pageY - pos.top}, nEdgeHoverWeight);
			if(selectedEdge.edge !== null){
				if(lastSelectedEdge.edge === null || selectedEdge.edge.id != lastSelectedEdge.edge.id){
					lastSelectedEdge = selectedEdge;
					if(typeof selectedEdge.edge.data.onMouseOver === 'function'){
						selectedEdge.edge.data.onMouseOver.call(selectedEdge.edge, e);
					}
					renderer.startOnce();
				}
			}else{
				if(lastSelectedEdge.edge !== null){
					renderer.startOnce();
					if(typeof lastSelectedEdge.edge.data.onMouseOut === 'function'){
						lastSelectedEdge.edge.data.onMouseOut.call(lastSelectedEdge.edge, e);
					}
				}
				lastSelectedEdge = {edge : null};				
			}
		}
	});

	jQuery(window).mouseup(function(e) {
		dragged = null;
		selected = null;
		$(this).css('cursor','auto');
	});

	var self =this;
	jQuery(window).resize(function(e){
		self.trigger('windowResize');
		renderer.startOnce();
	});

	Node.prototype.getWidth = function() {
		if(typeof(this.data.width) !== 'undefined'){
			return this.data.width;
		}else{
			var text = typeof(this.data.label) !== 'undefined' ? this.data.label : this.id;
			if (this._width && this._width[text])
				return this._width[text];

			ctx.save();
			ctx.font = "16px Verdana, sans-serif";
			var width = ctx.measureText(text).width + 10;
			ctx.restore();

			this._width || (this._width = {});
			this._width[text] = width;

			return width;
		}
	};

	Node.prototype.getTextWidth = function() {
		var text = typeof(this.data.label) !== 'undefined' ? this.data.label : this.id;
		if (this._width && this._width[text])
			return this._width[text];

		ctx.save();
		ctx.font = "16px Verdana, sans-serif";
		var width = ctx.measureText(text).width + 10;
		ctx.restore();

		this._width || (this._width = {});
		this._width[text] = width;

		return width;
	};

	Node.prototype.getHeight = function() {
		if(typeof(this.data.height) !== 'undefined'){
			return this.data.height;
		}else{
			return 20;
		}
	};

	Edge.prototype.getValueWidth = function(text){
		var text = typeof(this.data.value) !== 'undefined' ? this.data.value : this.id;
		if (this._width && this._width[text])
			return this._width[text];

		ctx.save();
		ctx.font = "9px Verdana, sans-serif";
		var width = ctx.measureText(text).width + 10;
		ctx.restore();

		this._width || (this._width = {});
		this._width[text] = width;

		return width;	
	}

	var renderer = new Renderer(layout,
		function clear() {
			ctx.clearRect(0,0,canvas.width,canvas.height);
		},
		function drawEdge(edge, p1, p2) {
			if(edge.source.id === edge.target.id){
				drawRecursiveLineWithArrow(edge, p1, p2);
			}else{
				drawLineWithArrow(edge, p1, p2);
			}
		},
		function drawNode(node, p) {
			var s = toScreen(p);

			ctx.save();

			var boxWidth = node.getWidth();
			var textWidth = node.getTextWidth();
			var boxHeight = node.getHeight();
			
			// clear background
			//ctx.clearRect(s.x - boxWidth/2, s.y - 10, boxWidth, 20);

			// fill background
			// if (selected !== null && nearest.node !== null && selected.node.id === node.id) {
			// 	ctx.fillStyle = "#FFFFE0";
			// } else if (nearest !== null && nearest.node !== null && nearest.node.id === node.id) {
			// 	ctx.fillStyle = "#EEEEEE";
			// } else {
			// 	ctx.fillStyle = "#FFFFFF";
			// }
			if(node.hover){
				ctx.strokeStyle = sNodeBorderHoverColor;
				ctx.fillStyle = sNodeBgHoverColor;
			}else{
				ctx.strokeStyle = sNodeBorderColor;
				ctx.fillStyle = sNodeBgColor;
			}			

			var edges = layout.graph.edges;
			for(var i=0, nLen=edges.length; i<nLen; i++){
				if(edges[i].hover === true){
					if(edges[i].source.id === node.id){
						ctx.strokeStyle = sNodeBorderFromHoverColor;
						ctx.fillStyle = sNodeBgHoverFromColor;
					}else if(edges[i].target.id === node.id){
						ctx.strokeStyle = sNodeBorderToHoverColor;
						ctx.fillStyle = sNodeBgHoverToColor;
					}
				}
			}

			node.top = s.y - boxHeight/2;
			node.left = s.x - boxWidth/2;
			ctx.roundRect(node.left, node.top, boxWidth, boxHeight, {upperLeft:10, upperRight:10, lowerLeft:10, lowerRight:10}, true, true);
			ctx.restore();

			ctx.save();
			//ctx.strokeRect(s.x - boxWidth/2, s.y - 20, boxWidth, 20);

			var image = new Image();
			image.src = iconPath + "/ico_" + node.data.serviceType + ".png";
			ctx.drawImage(image, s.x - image.width / 2, s.y - image.height / 1, image.width, image.height);

			var textX = s.x - textWidth / 2 + 5;
			var textY = s.y + image.height / 3;			

			// ctx.textAlign = "left";
			ctx.textBaseline = "top";
			ctx.fillStyle = "#222222";
			if(node.data.key) {
				ctx.font = "14px bold Verdana, sans-serif";
			} else {
				ctx.font = "12px Verdana, sans-serif";
			}
			var text = typeof(node.data.label) !== 'undefined' ? node.data.label : node.id;
			ctx.fillText(text, textX, textY);

			ctx.restore();
		}
	);

	renderer.start();

	function getEdgeColors(edge){
		if(edge.source.hover || edge.target.hover){
			if(edge.source.hover){
				sColor = sEdgeOutHoverColor;
				sBoxBorderColor = sEdgeOutHoverColor;
			}else{
				sColor = sEdgeInHoverColor;
				sBoxBorderColor = sEdgeInHoverColor;
			}
			sBoxBgColor = sEdgeBoxBgHoverColor;
		}else{
			sColor = sEdgeColor;
			sBoxBgColor = sEdgeBoxBgColor;
			sBoxBorderColor = sEdgeBoxBorderColor;
		}
		if(edge.hover){
			sColor = sEdgeHoverColor;
			sBoxBorderColor = sEdgeOutHoverColor;
		}
		return {
			sColor : sColor,
			sBoxBorderColor : sBoxBorderColor,
			sBoxBgColor : sBoxBgColor
		};
	}

	function getInfoForLine(edge, p1, p2){
		var x1 = toScreen(p1).x,
			y1 = toScreen(p1).y,
			x2 = toScreen(p2).x,
			y2 = toScreen(p2).y;

		var direction = new Vector(x2-x1, y2-y1),
			normal = direction.normal().normalise();

		var from = graph.getEdges(edge.source, edge.target),
			to = graph.getEdges(edge.target, edge.source)
			total = from.length + to.length;

		// Figure out edge's position in relation to other edges between the same nodes
		var n = 0;
		for (var i=0; i<from.length; i++) {
			if (from[i].id === edge.id) {
				n = i;
			}
		}
		var spacing = 20.0;

		// Figure out how far off center the line should be drawn
		var offset = normal.multiply(-((total - 1) * spacing)/2.0 + (n * spacing));

		var s1 = toScreen(p1).add(offset),
			s2 = toScreen(p2).add(offset);	

		var boxWidth = edge.target.getWidth(),
			boxHeight = edge.target.getHeight();

		var intersection = intersect_line_box(s1, s2, {x: x2-boxWidth/2.0, y: y2-boxHeight/2.0}, boxWidth, boxHeight);

		if (!intersection) {
			intersection = s2;
		}		

		var directional = typeof(edge.data.directional) !== 'undefined' ? edge.data.directional : true;

		// line
		var lineEnd;
		if (directional) {
			lineEnd = intersection.subtract(direction.normalise().multiply(nEdgeArrowLength * 0.5));
		} else {
			lineEnd = s2;
		}

		var lineStart = intersect_line_box(s1, s2, {x: x1-boxWidth/2.0, y: y1-boxHeight/2.0}, boxWidth, boxHeight);
		var htColor = getEdgeColors(edge);

		return {
			x1 : x1,
			y1 : y1,
			x2 : x2,
			y2 : y2,
			direction : direction,
			s1 : s1,
			s2 : s2,
			directional : directional,
			intersection : intersection,
			lineStart : lineStart,
			lineEnd : lineEnd,
			boxWidth : boxWidth,
			boxHeight : boxHeight,
			htColor : htColor
		};
	}

	function drawRecursiveLineWithArrow(edge, p1, p2){
		var htInfo = getInfoForLine(edge, p1, p2),
			x1 = htInfo.x1,
			y1 = htInfo.y1,
			x2 = htInfo.x2,
			y2 = htInfo.y2,
			lineStart = htInfo.lineStart,
			lineEnd = htInfo.lineEnd,
			directional = htInfo.directional,
			intersection = htInfo.intersection,
			boxWidth = htInfo.boxWidth,
			boxHeight = htInfo.boxHeight,
			htColor = htInfo.htColor;

		var htRightBottomPos = {
			x : edge.source.left + boxWidth,
			y : edge.source.top + boxHeight
		};
		var nRadius = nRecursiveEdgeRadius;
		var nStartAngle = 1.5 * Math.PI;
		var nEndAngle = 1 * Math.PI;
		var bCounterClockwise = false;
		edge.recursivePos = htRightBottomPos;
		edge.recursiveRadius = nRadius;

		ctx.beginPath();
		ctx.arc(htRightBottomPos.x, htRightBottomPos.y, nRadius, nStartAngle, nEndAngle, bCounterClockwise);
		ctx.lineWidth = nEdgeWeight;
		ctx.strokeStyle = htColor.sColor;
		ctx.stroke();

		var htCenterBottomPos = {
			x : edge.source.left + boxWidth - nRadius,
			y : edge.source.top + boxHeight
		};
		var nDegree = 1.44 * Math.PI;
		drawArrow(htCenterBottomPos, nDegree, htColor);

		if(edge.data.value){
			var nValueWidth = edge.getValueWidth(),
				nValueHeight = 18;
			var htValuePos = {
				x : edge.source.left + boxWidth + nRadius,
				y : edge.source.top + boxHeight
			};
			drawValueOnLine(htValuePos.x, htValuePos.y, nValueWidth, nValueHeight, edge.data.value, htColor);
		}		
	}

	function drawLineWithArrow(edge, p1, p2, htColor){
		var htInfo = getInfoForLine(edge, p1, p2),
			x1 = htInfo.x1,
			y1 = htInfo.y1,
			x2 = htInfo.x2,
			y2 = htInfo.y2,
			lineStart = htInfo.lineStart,
			lineEnd = htInfo.lineEnd,
			directional = htInfo.directional,
			intersection = htInfo.intersection,
			boxWidth = htInfo.boxWidth,
			boxHeight = htInfo.boxHeight,
			htColor = htInfo.htColor;

		edge.sourcePos = lineStart;
		edge.targetPos = lineEnd;

		var weight = nEdgeWeight;

		ctx.strokeStyle = htColor.sColor;
		ctx.beginPath();
		ctx.lineWidth = Math.max(weight, 0.1);		
		ctx.moveTo(lineStart.x, lineStart.y);
		ctx.lineTo(lineEnd.x, lineEnd.y);
		ctx.stroke();

		// arrow
		if (directional) {
			var nDegree = Math.atan2((y2 - y1), (x2 - x1));
			drawArrow(intersection, nDegree, htColor);
		}

		if(edge.data.value){
			var nMiddleX = ((lineStart.x+lineEnd.x)/2),
				nMiddleY = ((lineStart.y+lineEnd.y)/2);

			var nMiddleOfMiddleX = (lineEnd.x+nMiddleX) / 2,
				nMiddleOfMiddleY = (lineEnd.y+nMiddleY) / 2;		

			var nValueWidth = edge.getValueWidth(),
				nValueHeight = 18;

			drawValueOnLine(nMiddleOfMiddleX, nMiddleOfMiddleY, nValueWidth, nValueHeight, edge.data.value, htColor);
		}
	}

	function drawArrow(pos, degree, htColor){	
		var arrowWidth;
		//var weight = typeof(edge.data.weight) !== 'undefined' ? edge.data.weight : 1.0;
		var weight = nEdgeWeight;
		ctx.lineWidth = Math.max(weight, 0.1);
		arrowWidth = 4 + ctx.lineWidth;

		ctx.save();
		ctx.fillStyle = htColor.sColor;
		ctx.translate(pos.x, pos.y);
		ctx.rotate(degree);
		ctx.beginPath();
		ctx.moveTo(-nEdgeArrowLength, arrowWidth);
		ctx.lineTo(0, 0);
		ctx.lineTo(-nEdgeArrowLength, -arrowWidth);
		ctx.lineTo(-nEdgeArrowLength * 0.8, -0);
		ctx.closePath();
		ctx.fill();
		ctx.restore();		
	}

	function drawValueOnLine(x, y, width, height, value, htColor){
		ctx.save();
		ctx.strokeStyle = htColor.sBoxBorderColor;
		ctx.fillStyle = htColor.sBoxBgColor;
		ctx.roundRect(
			x - width/2, 
			y - height/4, 
			width, height, {upperLeft:0, upperRight:0, lowerLeft:0, lowerRight:0}, true, true);
		
		ctx.textAlign = 'center';
		ctx.textBaseline = 'top';
		ctx.font = '9px Helvetica, sans-serif';
		ctx.fillStyle = '#000';
		ctx.fillText(value, x, y);
		ctx.restore();		
	}

	// helpers for figuring out where to draw arrows
	function intersect_line_line(p1, p2, p3, p4) {
		var denom = ((p4.y - p3.y)*(p2.x - p1.x) - (p4.x - p3.x)*(p2.y - p1.y));

		// lines are parallel
		if (denom === 0) {
			return false;
		}

		var ua = ((p4.x - p3.x)*(p1.y - p3.y) - (p4.y - p3.y)*(p1.x - p3.x)) / denom;
		var ub = ((p2.x - p1.x)*(p1.y - p3.y) - (p2.y - p1.y)*(p1.x - p3.x)) / denom;

		if (ua < 0 || ua > 1 || ub < 0 || ub > 1) {
			return false;
		}

		return new Vector(p1.x + ua * (p2.x - p1.x), p1.y + ua * (p2.y - p1.y));
	}

	function intersect_line_box(p1, p2, p3, w, h) {
		var tl = {x: p3.x, y: p3.y};
		var tr = {x: p3.x + w, y: p3.y};
		var bl = {x: p3.x, y: p3.y + h};
		var br = {x: p3.x + w, y: p3.y + h};

		var result;
		if (result = intersect_line_line(p1, p2, tl, tr)) { return result; } // top
		if (result = intersect_line_line(p1, p2, tr, br)) { return result; } // right
		if (result = intersect_line_line(p1, p2, br, bl)) { return result; } // bottom
		if (result = intersect_line_line(p1, p2, bl, tl)) { return result; } // left

		return false;
	}

	return this;
}

})();