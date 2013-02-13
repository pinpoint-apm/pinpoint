(function() {

jQuery.fn.hippoServerMap = function(params) {
	var graph = this.graph = params.graph || new Graph();

	var stiffness = params.stiffness || 400.0;
	var repulsion = params.repulsion || 400.0;
	var damping = params.damping || 0.5;
	var nodeSelected = params.nodeSelected || null;
	var sLinkColor = params.sLinkColor || '#8f8f8f',
		sLinkInHoverColor = params.sLinkInHoverColor || '#f77128',
		sLinkOutHoverColor = params.sLinkOutHoverColor || '#28a1f7',
		sLinkBoxBorderColor = params.sLinkBoxColor || '#000000',
		sLinkBoxBgColor = params.sLinkBoxBgColor || 'rgba(255, 255, 255, 0.5)',
		sLinkBoxBgHoverColor = params.sLinkBoxBgHoverColor || 'rgba(255, 255, 255, 1)',
		nLinkWeight = params.nLinkWeight || 1;
	var sNodeBorderColor = params.sNodeBorderColor || '#8f8f8f',
		sNodeBorderHoverColor = params.sNodeBorderHoverColor || '#28a1f7',
		sNodeBgColor = params.sNodeBgColor || 'rgba(255, 255, 255, 0.5)',
		sNodeBgHoverColor = params.sNodeBgHoverColor || 'rgba(255, 255, 255, 1)';

	//var canvas = this[0];
	var nThisWidth = $(this).width(),
		nThisHeight = $(this).height();
	var canvas = document.createElement('canvas');
	canvas.width = nThisWidth * (1 + params.graph.nodes.length / 100);
	canvas.height = nThisHeight * (1 + params.graph.nodes.length / 100);
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

	jQuery(canvas).mousedown(function(e) {
		var pos = jQuery(this).offset();
		selectedNode = layout.selectNode({x: e.pageX - pos.left, y: e.pageY - pos.top});		
		if(selectedNode.node !== null){
			selected = true;
		}else{		
			dragged = true;	
			htLastPos.pageY = e.pageY;
			htLastPos.pageX = e.pageX;			
		}
	});

	var lastSelectedNodeId = null;
	jQuery(canvas).mousemove(function(e) {
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

			renderer.start();
		}else{
			var pos = jQuery(this).offset();
			selectedNode = layout.hover({x: e.pageX - pos.left, y: e.pageY - pos.top});	
			//console.log('selectedNode != lastSelectedNode', (selectedNode.node.id != lastSelectedNode.node.id));
			
			if (selectedNode.node !== null) {
				if(selectedNode.node.id != lastSelectedNodeId){
					lastSelectedNodeId = selectedNode.node.id;
					renderer.start();
				}
			}else{
				if(lastSelectedNodeId !== null){
					renderer.start();
				}
				lastSelectedNodeId = null;
			}
		}
	});

	jQuery(window).bind('mouseup',function(e) {
		dragged = null;
		selected = null;
	});

	var self =this;
	jQuery(window).bind('resize', function(e){
		self.trigger('windowResize');
		renderer.start();
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
			var x1 = toScreen(p1).x;
			var y1 = toScreen(p1).y;
			var x2 = toScreen(p2).x;
			var y2 = toScreen(p2).y;

			var direction = new Vector(x2-x1, y2-y1);
			var normal = direction.normal().normalise();

			var from = graph.getEdges(edge.source, edge.target);
			var to = graph.getEdges(edge.target, edge.source);

			var total = from.length + to.length;

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

			var s1 = toScreen(p1).add(offset);
			var s2 = toScreen(p2).add(offset);
			
			var boxWidth = edge.target.getWidth();
			var boxHeight = edge.target.getHeight();

			var intersection = intersect_line_box(s1, s2, {x: x2-boxWidth/2.0, y: y2-boxHeight/2.0}, boxWidth, boxHeight);

			if (!intersection) {
				intersection = s2;
			}

			var sColor, sBoxBgColor, sBoxBorderColor;
			if(edge.source.hover || edge.target.hover){
				if(edge.source.hover){
					sColor = sLinkOutHoverColor;
					sBoxBorderColor = sLinkOutHoverColor;
				}else{
					sColor = sLinkInHoverColor;
					sBoxBorderColor = sLinkInHoverColor;
				}
				sBoxBgColor = sLinkBoxBgHoverColor;
			}else{
				sColor = sLinkColor;
				sBoxBgColor = sLinkBoxBgColor;
				sBoxBorderColor = sLinkBoxBorderColor;
			}

			var arrowWidth;
			var arrowLength;

			//var weight = typeof(edge.data.weight) !== 'undefined' ? edge.data.weight : 1.0;
			var weight = nLinkWeight;

			ctx.lineWidth = Math.max(weight, 0.1);
			arrowWidth = 4 + ctx.lineWidth;
			arrowLength = 10;

			var directional = typeof(edge.data.directional) !== 'undefined' ? edge.data.directional : true;

			// line
			var lineEnd;
			if (directional) {
				lineEnd = intersection.subtract(direction.normalise().multiply(arrowLength * 0.5));
			} else {
				lineEnd = s2;
			}
			var lineStart = intersect_line_box(s1, s2, {x: x1-boxWidth/2.0, y: y1-boxHeight/2.0}, boxWidth, boxHeight);

			ctx.strokeStyle = sColor;
			ctx.beginPath();
			ctx.moveTo(lineStart.x, lineStart.y);
			ctx.lineTo(lineEnd.x, lineEnd.y);
			ctx.stroke();

			// arrow
			if (directional) {
				ctx.save();
				ctx.fillStyle = sColor;
				ctx.translate(intersection.x, intersection.y);
				ctx.rotate(Math.atan2(y2 - y1, x2 - x1));
				ctx.beginPath();
				ctx.moveTo(-arrowLength, arrowWidth);
				ctx.lineTo(0, 0);
				ctx.lineTo(-arrowLength, -arrowWidth);
				ctx.lineTo(-arrowLength * 0.8, -0);
				ctx.closePath();
				ctx.fill();
				ctx.restore();

				var nMiddleX = ((lineStart.x+lineEnd.x)/2),
					nMiddleY = ((lineStart.y+lineEnd.y)/2);

				var nMiddleOfMiddleX = (lineEnd.x+nMiddleX) / 2,
					nMiddleOfMiddleY = (lineEnd.y+nMiddleY) / 2;

				var nValueWidth = edge.getValueWidth(),
					nValueHeight = 18;
				ctx.save();
				ctx.strokeStyle = sBoxBorderColor;
				ctx.fillStyle = sBoxBgColor;
				ctx.roundRect(
					nMiddleOfMiddleX - nValueWidth/2, 
					nMiddleOfMiddleY - nValueHeight/4, 
					nValueWidth, nValueHeight, {upperLeft:0, upperRight:0, lowerLeft:0, lowerRight:0}, true, true);
				
				ctx.textAlign = 'center';
				ctx.textBaseline = 'top';
				ctx.font = '9px Helvetica, sans-serif';
				ctx.fillStyle = '#000';
				ctx.fillText(edge.data.value, nMiddleOfMiddleX, nMiddleOfMiddleY);
				ctx.restore();
			}

			// label
			if (typeof(edge.data.label) !== 'undefined') {
				text = edge.data.label
				ctx.save();
				ctx.textAlign = "center";
				ctx.textBaseline = "top";
				ctx.font = "10px Helvetica, sans-serif";
				ctx.fillStyle = "#5BA6EC";
				ctx.fillText(text, (x1+x2)/2, (y1+y2)/2);
				ctx.restore();
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
			node.top = s.y - boxHeight/2;
			node.left = s.x - boxWidth/2;
			ctx.roundRect(node.left, node.top, boxWidth, boxHeight, {upperLeft:10, upperRight:10, lowerLeft:10, lowerRight:10}, true, true);
			ctx.restore();

			ctx.save();
			//ctx.strokeRect(s.x - boxWidth/2, s.y - 20, boxWidth, 20);

			var image = new Image();
			image.src = "/common/images/hippo/ico_" + node.data.serviceType + ".gif";
			ctx.drawImage(image, s.x - image.width / 2, s.y - image.height / 1, image.width, image.height);

			var textX = s.x - textWidth / 2 + 5;
			var textY = s.y + image.height/3;			

			ctx.textAlign = "left";
			ctx.textBaseline = "top";
			ctx.fillStyle = "#000000";
			ctx.font = "14px Verdana, sans-serif";
			var text = typeof(node.data.label) !== 'undefined' ? node.data.label : node.id;
			ctx.fillText(text, textX, textY);

			ctx.restore();
		}
	);

	renderer.start();

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