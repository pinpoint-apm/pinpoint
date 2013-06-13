var ServerMap = $.Class({
	$init : function(htOption){
		this.option({
			"sContainerId" : '',
			"sBigFont" : "12pt Helvetica, Arial, sans-serif",
			"sSmallFont" : "11pt Helvetica, Arial, sans-serif",
			"htNodeBackgroundColor" : { 0: "rgba(136, 194, 251, 0)", 0.5: "rgba(64, 169, 253, 0.1)", 1: "rgba(122, 231, 255, 0)"},
			"sImageDir" : './images/icons/',
			"htIcons" : {
				'APACHE' : 'APACHE.png',
				'ARCUS' : 'ARCUS.png',
				'CUBRID' : 'CUBRID.png',
				'ETC' : 'ETC.png',
				'MEMCACHED' : 'MEMCACHED.png',
				'MYSQL' : 'MYSQL.png',
				'QUEUE' : 'QUEUE.png',
				'TOMCAT' : 'TOMCAT.png',
				'UNKNOWN_CLOUD' : 'UNKNOWN_CLOUD.png',
				'USER' : 'USER.png'
			},
			"htLinkType" : {
				"sRouting" : "AvoidsNodes", // Normal, Orthogonal, AvoidNodes
				"sCurve" : "JumpGap" // Bezier, JumpOver, JumpGap
			},
			"htLinkTheme" : {
				"default" : {
					"background" : { 0: "rgb(240, 240, 240)", 0.3: "rgb(240, 240, 240)", 1: "rgba(240, 240, 240, 1)"},
					"border" : "gray",
					"font" : "10pt helvetica, arial, sans-serif",
					"color" : "#919191",
					"align" : "center",
					"margin" : 1
				},
				"good" : {
					"background" : { 0: "rgb(240, 1, 240)", 0.3: "rgb(240, 1, 240)", 1: "rgba(240, 1, 240, 1)"},
					"border" : "green",
					"font" : "10pt helvetica, arial, sans-serif",
					"color" : "#919191",
					"align" : "center",
					"margin" : 1
				},
				"bad" : {
					"background" : { 0: "rgb(1, 240, 240)", 0.3: "rgb(1, 240, 240)", 1: "rgba(1, 240, 240, 1)"},
					"border" : "red",
					"font" : "10pt helvetica, arial, sans-serif",
					"color" : "#919191",
					"align" : "center",
					"margin" : 1
				}
			},
			"htHighlightNode" : {
				"normal" : {
					"stroke" : "gray",
					"strokeWidth" : 1
				},
				"to" : {
					"stroke" : "#28a1f7",
					"strokeWidth" : 1
				},
				"from" : {
					"stroke" : "#f77128",
					"strokeWidth" : 1
				}
			},
			"htHighlightLink" : {
				"normal" : {
					"stroke" : "gray",
					"strokeWidth" : 1
				},
				"to" : {
					"stroke" : "#28a1f7",
					"strokeWidth" : 1
				},
				"from" : {
					"stroke" : "#f77128",
					"strokeWidth" : 1
				}
			},
			"fOnNodeClick" : function(eMouseEvent, htData) {
				console.log("fOnNodeClick", eMouseEvent, htData);
			},
			"fOnLinkClick" : function(eMouseEvent, htData){
				console.log("fOnLinkClick", eMouseEvent, htData);
			},
			"fOnNodeContextClick" : function(eMouseEvent, htData){
				console.log("fOnNodeContextClick", eMouseEvent, htData);
			},
			"fOnLinkContextClick" : function(eMouseEvent, htData){
				console.log("fOnLinkContextClick", eMouseEvent, htData);
			}
		});

		this.option(htOption);

		this._initVariables();
		this._initNodeTemplates();
		this._initLinkTemplates();
		this._initDiagramEnvironment();
	},

	_initVariables : function(){
		this.$ = go.GraphObject.make;
		this._oDiagram = this.$(go.Diagram, this.option('sContainerId'), { initialContentAlignment: go.Spot.Center,maxSelectionCount: 1 });
		this._oNodeBackground = this.$(go.Brush, go.Brush.Linear, this.option('htNodeBackgroundColor'));
	},

	_initNodeTemplates : function(){
		var sImageDir = this.option('sImageDir'),
			htIcons = this.option('htIcons');
	
		this._oDefaultAdornment = this.$(go.Adornment, go.Panel.Auto,
                    this.$(go.Shape, "Rectangle",
                      { fill: null, stroke: "green", strokeWidth: 3 }),
                    this.$(go.Placeholder));

	    this._oDiagram.nodeTemplate =
	      this.$(go.Node, go.Panel.Auto,
	        {
	    	  	selectionAdornmentTemplate: this._oDefaultAdornment,
	    	  	click : this._onNodeClick.bind(this),
	        	contextClick : this._onNodeContextClick.bind(this)
	        },
	        // new go.Binding("location", "loc",
			// go.Point.parse).makeTwoWay(go.Point.str ingify),
	        // define the node's outer shape, which will surround the TextBlock
	        this.$(go.Shape, new go.Binding("figure", "fig"),
	          { name: "NODE", fill: this._oNodeBackground, stroke: "gray",
	            portId: "", cursor: "pointer" }),
	        this.$(go.Panel, go.Panel.Horizontal, {margin:4},
	          this.$(go.Picture, 
	            { source : sImageDir + "UNKNOWN_CLOUD.png", 
	                        width: 20, height: 20, margin: 1, imageStretch: go.GraphObject.Uniform 
	                        }),
	          this.$(go.TextBlock,
	            { margin: 6,
	              font: this.option('sBigFont'),
	              editable: false,
	              text: 'UNKNOWN_CLOUD'
	              
	             },
	            new go.Binding("text", "text").makeTwoWay())
	          )
	        
	        );

	    _.each(htIcons, function(sVal, sKey){
		    this._oDiagram.nodeTemplateMap.add(sKey,
		        this.$(go.Node, go.Panel.Auto,
		        	{ 
		        		selectionAdornmentTemplate: this._oDefaultAdornment,
		        		click : this._onNodeClick.bind(this),
			        	contextClick : this._onNodeContextClick.bind(this)
		        	},
			        this.$(go.Shape, new go.Binding("figure", "fig"),
		          	{ name: "NODE", fill: this._oNodeBackground, stroke: "gray", portId: "", cursor: "pointer" }),
			        this.$(go.Panel, go.Panel.Horizontal, {margin:4},
			        this.$(go.Picture, 
			            { source : sImageDir + sVal, 
			                       width: 20, height: 20, margin: 1, imageStretch: go.GraphObject.Uniform }),
			        this.$(go.TextBlock,
			            { margin: 6,
			              font: this.option('sBigFont'),
			              editable: false,
			              text: sKey
			              
			             },
			        new go.Binding("text", "text").makeTwoWay())
		          )
			    )
			);
	    }, this);

	},

	_initLinkTemplates : function(){

	    var htLinkType = this.option('htLinkType');
	    var option = { selectionAdorned: true,
//				selectionAdornmentTemplate: this._oDefaultAdornmentForLink,
	    		click : this._onLinkClick.bind(this),
				contextClick : this._onLinkContextClick.bind(this),
				layerName: "Foreground",
				reshapable: true,
		        fromSpot: go.Spot.RightSide,
		        toSpot: go.Spot.LeftSide,
				routing : go.Link[htLinkType.sRouting],
				// routing : go.Link.Normal,
				// routing: go.Link.Orthogonal,
				// routing: go.Link.AvoidsNodes,
				corner: 10,
				curve : go.Link[htLinkType.sCurve],
				// curve: go.Link.JumpOver
				// curve: go.Link.JumpGap
				// curve: go.Link.Bezier
	        };
// option = {};

	    var htLinkTheme = this.option("htLinkTheme"),
	    	htDefault = htLinkTheme.default;
	    this._oDiagram.linkTemplate =
	      this.$(go.Link,  // the whole link panel
	        // { routing: go.Link.Normal, curve: go.Link.Bezier, toShortLength:
			// 2 },
	        option,
	        new go.Binding("curviness", "curviness"),
	        this.$(go.Shape,  // the link shape
	          { name: "LINK", isPanelMain: true,
	            stroke: "gray", strokeWidth: 1.5 }),
	        this.$(go.Shape,  // the arrowhead
	          { toArrow: "standard", fill: '#2F4F4F', // toArrow : kite,
														// standard,
														// OpenTriangle
	            stroke: null, scale: 1.5 }),
	        this.$(go.Panel, go.Panel.Auto,
	          this.$(go.Shape,  // the link shape
	            "RoundedRectangle",
	            { name: "LINK2", fill: this.$(go.Brush, go.Brush.Linear, htDefault.background), stroke: htDefault.border,
	            portId: "", fromLinkable: true, toLinkable: true, cursor: "pointer" }),
	          this.$(go.TextBlock,  // the label
	            { textAlign: htDefault.align,
	              font: htDefault.font,
	              stroke: htDefault.color,
	              margin: htDefault.margin},
	            new go.Binding("text", "text")))
	    );
	    
	    _.each(htLinkTheme, function(sVal, sKey){
	    	if(sKey === "default") return;
		    this._oDiagram.linkTemplateMap.add(sKey,
		  	      this.$(go.Link,  // the whole link panel
		  		        // { routing: go.Link.Normal, curve: go.Link.Bezier,
						// toShortLength: 2 },
		  		        option,
		  		        new go.Binding("curviness", "curviness"),
		  		        this.$(go.Shape,  // the link shape
		  		          { name: "LINK", isPanelMain: true,
		  		           stroke: "gray", strokeWidth: 1.5 }),
		  		        this.$(go.Shape,  // the arrowhead
		  		          { toArrow: "standard", fill: '#2F4F4F', // toArrow :
																	// kite,
																	// standard,
																	// OpenTriangle
		  		            stroke: null, scale: 1.5 }),
		  		        this.$(go.Panel, go.Panel.Auto,
		  		          this.$(go.Shape,  // the link shape
		  		            "RoundedRectangle",
		  		            { fill: this.$(go.Brush, go.Brush.Linear, sVal.background), stroke: sVal.border,
		  		            portId: "", fromLinkable: true, toLinkable: true, cursor: "pointer" }),
		  		          this.$(go.TextBlock,  // the label
		  		            { textAlign: sVal.align,
		  		              font: sVal.font,
		  		              stroke: sVal.color,
		  		              margin: sVal.margin},
		  		            new go.Binding("text", "text")))
		  		    )
			);
	    }, this);		    
	},

	_initDiagramEnvironment : function(){
	    // have mouse wheel events zoom in and out instead of scroll up and down
	    this._oDiagram.toolManager.mouseWheelBehavior = go.ToolManager.WheelZoom;
	    this._oDiagram.allowDrop = false;		

	    // read in the JSON-format data from the "mySavedModel" element
	    this._oDiagram.initialAutoScale = go.Diagram.Uniform; // None,
																// Uniform,
																// UniformToFill
	    // this._oDiagram.toolManager.linkingTool.direction =
		// go.LinkingTool.ForwardsOnly;
	    this._oDiagram.toolManager.draggingTool.doCancel();
	    this._oDiagram.toolManager.draggingTool.doDeactivate();
	    this._oDiagram.initialContentAlignment = go.Spot.Center;
	    this._oDiagram.layout = this.$(go.LayeredDigraphLayout, // { isOngoing:
																// false,
																// layerSpacing:
																// 50 });
	      { // rdirection: 90,
	           isOngoing: false,
	           layerSpacing: 150,
	           columnSpacing: 50,
	           setsPortSpots: false,
	          // packOption : 7 // 1(PackExpand), 2(PackStraighten),
				// 4(PackMedian)의 합

// direction : 0,
// cycleRemoveOption : go.LayeredDigraphLayout.CycleDepthFirst,
// layeringOption : go.LayeredDigraphLayout.LayerOptimalLinkLength,
// initializeOption : go.LayeredDigraphLayout.InitDepthFirstOut,
// aggressiveOption : go.LayeredDigraphLayout.AggressiveLess,
// packOption : 7,
// setsPortSpots : true
	      }
	    );
	    
	    var self = this;
	    this._oDiagram.addDiagramListener("ChangedSelection", function() { 
	    	self._updateHightlights(); 
	    }); // whenever selection changes, run updateHighlights
	},

	load : function(str){
		this._oDiagram.model = go.Model.fromJson(str);
		this._oDiagram.undoManager.isEnabled = true;
	},

	clear : function(){
		this._oDiagram.model = go.Model.fromJson({});
	},
	
	_resetHighlights : function(){
	      allNodes = this._oDiagram.nodes;
	      allLinks = this._oDiagram.links;
	      while (allNodes.next()) { allNodes.value.highlight = "normal"; }
	      while (allLinks.next()) { allLinks.value.highlight = "normal"; }
	},
	
	_updateHightlights : function(sel){
		this._resetHighlights();
		sel = sel || this._oDiagram.selection.first();
		if(sel !== null){
			if(sel instanceof go.Node){
				this._linksTo(sel, 'to');
				this._linksFrom(sel, 'from');
			}else if(sel instanceof go.Link){
				this._nodesTo(sel, 'to');
				this._nodesFrom(sel, 'from');	
			}
		}	
		
	    // iterators containing all nodes and links in the diagram
	    allNodes2 = this._oDiagram.nodes;
	    allLinks2 = this._oDiagram.links;
	    
	    // nodes, including groups
	    while (allNodes2.next()) {
	        var shp = allNodes2.value.findObject("NODE");
//	        var grp = allNodes2.value.findObject("GROUPTEXT");
	        var hl = allNodes2.value.highlight;
//	        this._highlight(shp, grp, hl);
	        this._highlight('node', shp, hl);
	    }
	    // links
	    while (allLinks2.next()) {
	        var shp = allLinks2.value.findObject("LINK");
	        var shp2 = allLinks2.value.findObject("LINK2");
//	        var arw = allLinks2.value.findObject("ARWSHAPE");
	        var hl = allLinks2.value.highlight;
//	        this._highlight(shp, arw, hl);
	        this._highlight('link', shp, hl);
	        this._highlight('link', shp2, hl);
	    }		
	},
	
	highlightNodeByKey : function(sKey){
		var node = this._oDiagram.findNodeForKey(sKey);
		if(node){
			var part = this._oDiagram.findPartForKey(sKey);
			this._oDiagram.select(part);
			this._updateHightlights(node);	
		}
	},
	
	highlightLinkByFromTo : function(from, to){
		var htLink = this._getLinkObjectByFromTo(from, to);
		if(htLink){
			var link = this._oDiagram.findLinkForData(htLink);
			var part = this._oDiagram.findPartForData(htLink);
			this._oDiagram.select(part);
			this._updateHightlights(link);
		}
	},
	
	_getLinkObjectByFromTo : function(from, to){
		var aLink = this._oDiagram.model.linkDataArray;
		for(var i=0, len=aLink.length; i<len; i++){
			var htLink = aLink[i];
			console.log(htLink.from, from ,"&&", htLink.to, to);
			if(htLink.from == from && htLink.to == to){
				return htLink;
			}
		}
		return false;
	},
	
	_highlight : function(type, shape, theme){
		var htHighlight;
		if(type === 'node'){
			htHighlight = this.option('htHighlightNode');
		}else if(type === 'link'){
			htHighlight = this.option('htHighlightLink');
		}
		if(shape !== null){
			if(htHighlight[theme]){
				shape.stroke = htHighlight[theme].stroke;
				shape.strokeWidth = parseInt(htHighlight[theme].strokeWidth, 10);
			}
		}
	},
//	// perform the actual highlighting
//    _highlight : function(shp, type) {
//    	var color;
//    	var width = 3;
//    	if (hl === 0) { color = "gray"; width = 1; }
//    	else if (hl === 1) { color = "blue"; }
//    	else if (hl === 2) { color = "green"; }
//    	else if (hl === 3) { color = "orange"; }
//    	else if (hl === 4) { color = "red"; }
//    	else { color = "purple"; }
//    	if(shp !== null){
//	    	shp.stroke = color;
//	    	shp.strokeWidth = width;
//    	}
////    	if (obj2 !== null) {
////    		obj2.stroke = color;
////    		obj2.fill = color;
////    	}
//    },
    
    // if the link connects to this node, highlight it
    _linksTo : function(x, i) {
        if (x instanceof go.Node) {
          var links = x.findLinksInto();
          while (links.next()) links.value.highlight = i; }
    },

    // if the link comes from this node, highlight it
    _linksFrom : function(x, i) {
        if (x instanceof go.Node) {
          var links = x.findLinksOutOf();
          while (links.next()) links.value.highlight = i; }
    },    
	
    // if selected object is a link, highlight its fromNode, otherwise,
	// highlight the fromNode of each link coming into the selected node
    // return a List of the keys of the nodes
    _nodesTo : function(x, i) {
      var nodesToList = new go.List("string");
      if (x instanceof go.Link) { x.fromNode.highlight = i; nodesToList.add(x.data.from); }
      else {
        var nodes = x.findNodesInto();
        while (nodes.next()) {
          nodes.value.highlight = i;
          nodesToList.add(nodes.value.data.key);
        }
      }
      return nodesToList;
    },

    // same as nodesTo, but from instead of to
    _nodesFrom : function(x, i) {
        var nodesFromList = new go.List("string");
        if (x instanceof go.Link) { x.toNode.highlight = i; nodesFromList.add(x.data.to); }
        else {
          var nodes = x.findNodesOutOf();
          while (nodes.next()) {
            nodes.value.highlight = i;
            nodesFromList.add(nodes.value.data.key);
          }
        }
        return nodesFromList;
    },	

	_onNodeClick : function(e, obj){
		var node = obj.part,
			htData = node.data,
			fOnNodeClick = this.option('fOnNodeClick');
		if(_.isFunction(fOnNodeClick)){
			fOnNodeClick.call(this, e.Vi, htData);
		}
    	// node.diagram.startTransaction("onNodeClick");
    	// node.diagram.commitTransaction("onNodeClick");
	},
	
	_onNodeContextClick : function(e, obj){
		var node = obj.part,
			htData = node.data,
			fOnNodeContextClick = this.option('fOnNodeContextClick');
		if(_.isFunction(fOnNodeContextClick)){
			fOnNodeContextClick.call(this, e.Vi, htData);
		}
	},

	_onLinkClick : function(e, obj){
		var node = obj.part,
			htData = node.data,
			fOnLinkClick = this.option('fOnLinkClick');

		if(_.isFunction(fOnLinkClick)){
			fOnLinkClick.call(this, e.Vi, htData);
		}
	},

	_onLinkContextClick : function(e, obj){
		var node = obj.part,
			htData = node.data,
			fOnLinkContextClick = this.option('fOnLinkContextClick');

		if(_.isFunction(fOnLinkContextClick)){
			fOnLinkContextClick.call(this, e.Vi, htData);
		}
	}

});