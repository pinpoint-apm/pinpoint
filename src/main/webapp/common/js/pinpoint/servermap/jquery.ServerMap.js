var ServerMap = $.Class({
	$init : function(htOption){
		this.option({
			"sContainerId" : '',
			"sBigFont" : "12pt Helvetica, Arial, sans-serif",
			"sSmallFont" : "11pt Helvetica, Arial, sans-serif",
			"htNodeBackgroundColor" : { 0: "rgba(136, 194, 251, 0)", 0.5: "rgba(64, 169, 253, 0.1)", 1: "rgba(122, 231, 255, 0)"},
			"sImageDir" : './images/icons',
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
		this._oDiagram = this.$(go.Diagram, this.option('sContainerId'));
		this._oNodeBackground = this.$(go.Brush, go.Brush.Linear, this.option('htNodeBackgroundColor'));
	},

	_initNodeTemplates : function(){
		var sImageDir = this.option('sImageDir'),
			htIcons = this.option('htIcons');

		this._oDefaultAdornment =
	      this.$(go.Adornment, go.Panel.Spot,
	        this.$(go.Panel, go.Panel.Auto,
	          this.$(go.Shape, "RoundedRectangle",
	            { fill: null, stroke: "dodgerblue", strokeWidth: 8 }),
	          this.$(go.Placeholder)),
	        this.$("Button",
	            { alignment: go.Spot.TopRight, alignmentFocus: go.Spot.TopLeft,
	              click: this._onNodeClick.bind(this) },  // define click behavior for this Button in the Adornment
	            // $(go.TextBlock, "Info",  // the Button content
	            //   { font: "bold 6pt sans-serif" }))
	            this.$(go.Shape, 
	              { name : "SHAPE",
	                figure : "BpmnEventConditional",
	                width : 15, height: 15,
	                fill : this.$(go.Brush, go.Brush.Linear, { 0.0: "white", 1.0: "gray" }), strokeWidth : 1
	              })
	          )
	        // the button to create a "next" node, at the top-right corner
	      );		

	    this._oDiagram.nodeTemplate =
	      this.$(go.Node, go.Panel.Auto,
	        {
	    	  	/*selectionAdornmentTemplate: this._oDefaultAdornment,*/
	    	  	click : this._onNodeClick.bind(this),
	        	contextClick : this._onNodeContextClick.bind(this)
	        },
	        // new go.Binding("location", "loc", go.Point.parse).makeTwoWay(go.Point.str ingify),
	        // define the node's outer shape, which will surround the TextBlock
	        this.$(go.Shape, new go.Binding("figure", "fig"),
	          { fill: this._oNodeBackground, stroke: "gray",
	            portId: "", cursor: "pointer" }),
	        this.$(go.Panel, go.Panel.Horizontal, {margin:4},
	          this.$(go.Picture, 
	            { source : sImageDir + "ico_UNKNOWN_CLOUD.png", 
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
//			        	selectionAdornmentTemplate: this._oDefaultAdornment,
		        		click : this._onNodeClick.bind(this),
			        	contextClick : this._onNodeContextClick.bind(this)
		        	},
			        this.$(go.Shape, new go.Binding("figure", "fig"),
		          	{ fill: this._oNodeBackground, stroke: "gray", portId: "", cursor: "pointer" }),
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
    	this._oDefaultAdornmentForLink =
	      this.$(go.Adornment, go.Panel.Spot,
	        this.$(go.Panel, go.Panel.Auto,
	          this.$(go.Placeholder))/*,
	        this.$("Button",
	            { alignment: go.Spot.TopLeft, alignmentFocus: go.Spot.BottomLeft,
	              click: this._onLinkClick.bind(this)
	            },  // define click behavior for this Button in the Adornment
	            // $(go.TextBlock, "Info",  // the Button content
	            //   { font: "bold 6pt sans-serif" }))
	            this.$(go.Shape, 
	              { name : "SHAPE",
	                figure : "BpmnEventConditional",
	                width : 15, height: 15,
	                fill : this.$(go.Brush, go.Brush.Linear, { 0.0: "white", 1.0: "gray" }), strokeWidth : 1
	              })
	          )*/
	        // the button to create a "next" node, at the top-right corner
	      );		

	    var htLinkType = this.option('htLinkType');
	    var option = { selectionAdorned: true,
				selectionAdornmentTemplate: this._oDefaultAdornmentForLink,
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
//	    option = {};

	    var htLinkTheme = this.option("htLinkTheme"),
	    	htDefault = htLinkTheme.default;
	    this._oDiagram.linkTemplate =
	      this.$(go.Link,  // the whole link panel
	        //{ routing: go.Link.Normal, curve: go.Link.Bezier, toShortLength: 2 },
	        option,
	        new go.Binding("curviness", "curviness"),
	        this.$(go.Shape,  // the link shape
	          { isPanelMain: true,
	            stroke: "gray", strokeWidth: 1.5 }),
	        this.$(go.Shape,  // the arrowhead
	          { toArrow: "standard", fill: '#2F4F4F', // toArrow : kite, standard, OpenTriangle
	            stroke: null, scale: 1.5 }),
	        this.$(go.Panel, go.Panel.Auto,
	          this.$(go.Shape,  // the link shape
	            "RoundedRectangle",
	            { fill: this.$(go.Brush, go.Brush.Linear, htDefault.background), stroke: htDefault.border,
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
		  		        //{ routing: go.Link.Normal, curve: go.Link.Bezier, toShortLength: 2 },
		  		        option,
		  		        new go.Binding("curviness", "curviness"),
		  		        this.$(go.Shape,  // the link shape
		  		          { isPanelMain: true,
		  		           stroke: "gray", strokeWidth: 1.5 }),
		  		        this.$(go.Shape,  // the arrowhead
		  		          { toArrow: "standard", fill: '#2F4F4F', // toArrow : kite, standard, OpenTriangle
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
	    this._oDiagram.initialAutoScale = go.Diagram.Uniform; // None, Uniform, UniformToFill
	    // this._oDiagram.toolManager.linkingTool.direction = go.LinkingTool.ForwardsOnly;
	    this._oDiagram.toolManager.draggingTool.doCancel();
	    this._oDiagram.toolManager.draggingTool.doDeactivate();
	    this._oDiagram.initialContentAlignment = go.Spot.Center;
	    this._oDiagram.layout = this.$(go.LayeredDigraphLayout, //{ isOngoing: false, layerSpacing: 50 });
	      { //rdirection: 90,
	           isOngoing: false,
	           layerSpacing: 150,
	           columnSpacing: 50,
	           setsPortSpots: false,
	          // packOption : 7 // 1(PackExpand), 2(PackStraighten), 4(PackMedian)의 합

//	          direction : 0,
//	          cycleRemoveOption : go.LayeredDigraphLayout.CycleDepthFirst,
//	          layeringOption : go.LayeredDigraphLayout.LayerOptimalLinkLength,
//	          initializeOption : go.LayeredDigraphLayout.InitDepthFirstOut,
//	          aggressiveOption : go.LayeredDigraphLayout.AggressiveLess,
//	          packOption : 7,
//	          setsPortSpots : true
	      }
	    );	    
	},

	load : function(str){
		this._oDiagram.model = go.Model.fromJson(str);
		this._oDiagram.undoManager.isEnabled = true;
	},

	clear : function(){
		this._oDiagram.model = go.Model.fromJson({});
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