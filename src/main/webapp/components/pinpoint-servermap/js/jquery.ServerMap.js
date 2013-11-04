(function (window, go, $, _) {
    "use strict";

    /**
     * ServerMap
     *
     * @class ServerMap
     * @version 0.1.1
     * @since Sep, 2013
     * @author Denny Lim<hello@iamdenny.com, iamdenny@nhn.com>
     * @license MIT License
     * @copyright 2013 NHN Corp.
     */
    window.ServerMap = $.Class({

        /**
         * constructor
         *
         * @constructor
         * @method $init
         * @param {Hash Table} options
         */
        $init: function (htOption) {
            this.option({
                "sContainerId": '',
                "sBigFont": "12pt NanumGothic,ng,dotum,AppleGothic,avn55,sans-serif",
                "sSmallFont": "11pt NanumGothic,ng,dotum,AppleGothic,avn55,sans-serif",
                "sImageDir": './images/',
                "nBoldKey": null,
                "htIcons": {
                    'APACHE': 'APACHE.png',
                    'ARCUS': 'ARCUS.png',
                    'CUBRID': 'CUBRID.png',
                    'ETC': 'ETC.png',
                    'MEMCACHED': 'MEMCACHED.png',
                    'MYSQL': 'MYSQL.png',
                    'QUEUE': 'QUEUE.png',
                    'TOMCAT': 'TOMCAT.png',
                    'UNKNOWN_CLOUD': 'UNKNOWN_CLOUD.png',
                    'USER': 'USER.png'
                },
                "htNodeTheme": {
                    "default": {
                        "backgroundColor": "#ffffff",
                        "borderColor" : "#adadad",
                        "fontColor": "#333333"
                    },
                    "bold": {
                        "borderColor": "#d43f3a"
                    }
                },
                "htLinkType": {
                    "sRouting": "AvoidsNodes", // Normal, Orthogonal, AvoidNodes
                    "sCurve": "JumpGap" // Bezier, JumpOver, JumpGap
                },
                "htLinkTheme": {
                    "default": {
                        "backgroundColor": "#f9f9f9",
                        "borderColor" : "#7d7d7d",
                        "fontFamily": "10pt helvetica, arial, sans-serif",
                        "fontColor": "#5a5a5a",
                        "fontAlign": "center",
                        "margin": 1
                    },
                    "good": {
                        "backgroundColor": "rgb(240, 1, 240)",
                        "borderColor" : "#7d7d7d",
                        "fontFamily": "10pt helvetica, arial, sans-serif",
                        "fontColor": "#919191",
                        "fontAlign": "center",
                        "margin": 1
                    },
                    "bad": {
                        "backgroundColor": "#ffc9c9",
                        "borderColor" : "#7d7d7d",
                        "fontFamily": "10pt helvetica, arial, sans-serif",
                        "fontColor": "#d72f30",
                        "fontAlign": "center",
                        "margin": 1
                    }
                },
                "htHighlightNode": {
                    "self" : {
                        "backgroundColor": "#28a1f7",
                        "fontColor": "#ffffff"
                    },
                    "from": {
                        "backgroundColor": "#247cbb",
                        "fontColor": "#ffffff"
                    },
                    "to": {
                        "backgroundColor": "#f78e41",
                        "fontColor": "#ffffff"
                    }
                },
                "htHighlightLink": {
                    "self" : {
                        "borderColor": "#28a1f7"
                    },
                    "from": {
                        "borderColor": "#1270b4"
                    },
                    "to": {
                        "borderColor": "#f77128"
                    }
                },
                "fOnNodeClicked": function (eMouseEvent, htData) {},
                "fOnNodeContextClicked": function (eMouseEvent, htData) {},
                "fOnLinkClicked": function (eMouseEvent, htData) {},
                "fOnLinkContextClicked": function (eMouseEvent, htData) {},
                "fOnBackgroundClicked": function (eMouseEvent, htData) {},
                "fOnBackgroundContextClicked": function (eMouseEvent, htData) {}
            });

            this.option(htOption);

            this._initVariables();
            this._initNodeTemplates();
            this._initLinkTemplates();
            this._initDiagramEnvironment();
        },

        /**
         * 변수 초기화
         *
         * @method _initVariables
         */
        _initVariables: function () {
            this.$ = go.GraphObject.make;
            this._oDiagram = this.$(
                go.Diagram,
                this.option('sContainerId'),
                {
                    initialContentAlignment: go.Spot.Center,
                    maxSelectionCount: 1,
                    allowDelete: false
                }
            );
        },

        /**
         * 노드 템플릿 초기화
         *
         * @method _initNodeTemplates
         */
        _initNodeTemplates: function () {
            var self = this,
                sImageDir = this.option('sImageDir'),
                htIcons = this.option('htIcons');

            var oSelectionAdornmentTemplate = this.$(
                go.Adornment,
                go.Panel.Auto,
                this.$(
                    go.Shape,
                    new go.Binding("figure", "fig"),
                    {
                        fill: null,
                        stroke: '#629b58',
                        strokeWidth: 1
                    }
                ),
                this.$(go.Placeholder)
            );

            var fNodeBackgroundColor = function (key) {
//                return self.$(
//                    go.Brush,
//                    go.Brush.Linear,
//                    self.option('htNodeTheme')['default'].backgroundColor
//                );
                return self.option('htNodeTheme')['default'].backgroundColor;
            };
            var fNodeBorderColor = function (key) {
                var type = 'default';
                if (self.option('nBoldKey') && self.option('nBoldKey') === key) {
                    type = 'bold';
                }
                return self.option('htNodeTheme')[type].borderColor;
            };
            var fNodeFontColor = function (key) {
                return self.option('htNodeTheme')['default'].fontColor;
            };

            var getNodeTemplate = function (sImageName) {
                return self.$(
                    go.Node,
                    go.Panel.Auto,
                    {
                        selectionAdorned: false,
//                        selectionAdornmentTemplate: oSelectionAdornmentTemplate,
                        click: self._onNodeClicked.bind(self),
                        contextClick: self._onNodeContextClicked.bind(self)
                    },
                    self.$(
                        go.Shape,
                        new go.Binding("figure", "fig"),
                        new go.Binding("fill", "key", fNodeBackgroundColor),
                        new go.Binding("stroke", "key", fNodeBorderColor),
                        {
                            name: "NODE",
                            portId: "",
                            cursor: "pointer",
                            strokeWidth: 2
                        }
                    ),
                    self.$(
                        go.Panel,
                        go.Panel.Horizontal,
                        {
                            margin: 4
                        },
                        self.$(
                            go.Picture,
                            {
                                source: sImageDir + sImageName,
                                width: 20,
                                height: 20,
                                margin: 1,
                                imageStretch: go.GraphObject.Uniform
                            }
                        ),
                        self.$(
                            go.TextBlock,
                            {
                                name: "NODE_TEXT",
                                margin: 6,
                                font: self.option('sBigFont'),
                                editable: false
                            },
                            new go.Binding("stroke", "key", fNodeFontColor),
                            new go.Binding("text", "text").makeTwoWay()
                        )
                    )
                );
            };

            this._oDiagram.nodeTemplate = getNodeTemplate("UNKNOWN_CLOUD.png");

            _.each(htIcons, function (sVal, sKey) {
                this._oDiagram.nodeTemplateMap.add(sKey, getNodeTemplate(sVal));
            }, this);

        },

        /**
         * 링크 템플릿 초기화
         *
         * @method _initLinkTemplates
         */
        _initLinkTemplates: function () {
            var self = this,
                htLinkType = this.option('htLinkType'),
                option = {
                    selectionAdorned: false,
                    // selectionAdornmentTemplate: this._oDefaultAdornmentForLink,
                    click: this._onLinkClicked.bind(this),
                    contextClick: this._onLinkContextClicked.bind(this),
                    layerName: "Foreground",
                    reshapable: false, // 연결선 reshape핸들 없애려고.
                    
                    // fromSpot: go.Spot.RightSide,
                    // toSpot: go.Spot.LeftSide,
                    
                    // routing: go.Link[htLinkType.sRouting],
                    // routing : go.Link.Normal,
                    // routing: go.Link.Orthogonal,
                    // routing: go.Link.AvoidsNodes,
                    
                    corner: 10,
                    
                    // curve: go.Link[htLinkType.sCurve],
                    // curve: go.Link.JumpOver
                    // curve: go.Link.JumpGap
                    // curve: go.Link.Bezier
                },
                htLinkTheme = this.option("htLinkTheme"),
                htDefault = htLinkTheme.default;

            var getLinkTemplate = function (htOption) {
                return self.$(
                    go.Link,  // the whole link panel
                    // { routing: go.Link.Normal, curve: go.Link.Bezier, toShortLength: 2 },
                    option,
                    new go.Binding("routing", "routing", function (val) { return go.Link[val]; }).makeTwoWay(),
                    new go.Binding("curve", "curve", function (val) { return go.Link[val]; }).makeTwoWay(),
                    new go.Binding("curviness", "curviness").makeTwoWay(),
                    self.$(
                        go.Shape,  // the link shape
                        {
                            name: "LINK",
                            isPanelMain: true,
                            stroke: htOption.borderColor,
                            strokeWidth: 1.5
                        }
                    ),
                    self.$(
                        go.Shape,  // the arrowhead
                        {
                            toArrow: "standard",
                            fill: '#2F4F4F', // toArrow : kite, standard, OpenTriangle
                            stroke: null,
                            scale: 1.5
                        }
                    ),
                    self.$(
                        go.Panel,
                        go.Panel.Auto,
                        self.$(
                            go.Shape,  // the link shape
                            "RoundedRectangle",
                            {
                                name: "LINK2",
                                fill: htOption.backgroundColor,
                                stroke: htOption.borderColor,
                                portId: "",
                                fromLinkable: true,
                                toLinkable: true,
                                cursor: "pointer"
                            }
                        ),
                        self.$(
                            go.TextBlock,  // the label
                            {
                                textAlign: htOption.fontAlign,
                                font: htOption.fontFamily,
                                stroke: htOption.fontColor,
                                margin: htOption.margin
                            },
                            new go.Binding("text", "text",  function (val) {
                                return Number(val, 10).toLocaleString();
                            })
                        )
                    )
                );
            };

            this._oDiagram.linkTemplate = getLinkTemplate(htDefault);

            _.each(htLinkTheme, function (sVal, sKey) {
                if (sKey === "default") {
                    return;
                }
                this._oDiagram.linkTemplateMap.add(sKey, getLinkTemplate(sVal));
            }, this);
        },

        /**
         * 다이어그램 환경설정
         *
         * @method _initDiagramEnvironment
         */
        _initDiagramEnvironment: function () {
            // have mouse wheel events zoom in and out instead of scroll up and
            // down
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
            this._oDiagram.toolManager.dragSelectingTool.isEnabled = false;
            this._oDiagram.initialContentAlignment = go.Spot.Center;
            this._oDiagram.layout = this.$(
                go.LayeredDigraphLayout,
                { // rdirection: 90,
                    isOngoing: false,
                    layerSpacing: 150,
                    columnSpacing: 50,
                    setsPortSpots: false
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
            // whenever selection changes, run updateHighlights
            this._oDiagram.addDiagramListener("ChangedSelection", function (e) {
                self._updateHightlights();
            });
            this._oDiagram.addDiagramListener("BackgroundSingleClicked", function (e) {
                var fOnBackgroundClicked = self.option('fOnBackgroundClicked');
                if (_.isFunction(fOnBackgroundClicked)) {
                    fOnBackgroundClicked.call(self, e);
                }
            });
            this._oDiagram.addDiagramListener("BackgroundContextClicked", function (e) {
                var fOnBackgroundContextClicked = self.option('fOnBackgroundContextClicked');
                if (_.isFunction(fOnBackgroundContextClicked)) {
                    fOnBackgroundContextClicked.call(self, e);
                }
            });
        },

        /**
         * 불러오기
         *
         * @method load
         * @param {Hash Table} str
         */
        load: function (str) {
            this._oDiagram.model = go.Model.fromJson(str);
            this._oDiagram.undoManager.isEnabled = true;

            if (this.option('nBoldKey')) {
                this.highlightNodeByKey(this.option('nBoldKey'));
            }
        },

        /**
         * 다이어그램 초기화
         *
         * @method clear
         */
        clear: function () {
            this._oDiagram.model = go.Model.fromJson({});
        },

        /**
         * 하이라이팅 초기화
         *
         * @method _resetHighlights
         */
        _resetHighlights: function () {
            var allNodes = this._oDiagram.nodes;
            var allLinks = this._oDiagram.links;
            while (allNodes.next()) {
                allNodes.value.highlight = false;
            }
            while (allLinks.next()) {
                allLinks.value.highlight = false;
            }
        },

        /**
         * 하이라이팅 변경
         *
         * @method _updateHighlights
         * @param {go.Node} selection
         */
        _updateHightlights: function (selection) {
            selection = selection || this._oDiagram.selection.first();
            if (selection === null) { return; }

            this._resetHighlights();
            selection.highlight = 'self';
            if (selection instanceof go.Node) {
                this._linksTo(selection, 'from');
                this._linksFrom(selection, 'to');
            } else if (selection instanceof go.Link) {
                this._nodesTo(selection, 'from');
                this._nodesFrom(selection, 'to');
            }

            // iterators containing all nodes and links in the diagram
            var allNodes = this._oDiagram.nodes,
                allLinks = this._oDiagram.links;

            // nodes, including groups
            while (allNodes.next()) {
                this._hightlightNode(allNodes.value.findObject("NODE"), allNodes.value.findObject("NODE_TEXT"), allNodes.value.highlight);
            }
            // links
            while (allLinks.next()) {
                this._highlightLink(allLinks.value.findObject("LINK"), allLinks.value.highlight);
                this._highlightLink(allLinks.value.findObject("LINK2"), allLinks.value.highlight);
            }
        },

        /**
         * 노드의 키로 노드 하이라이팅
         *
         * @method highlightNodeByKey
         * @param {String} sKey 노드키
         */
        highlightNodeByKey: function (sKey) {
            var node = this._oDiagram.findNodeForKey(sKey);
            if (node) {
                var part = this._oDiagram.findPartForKey(sKey);
                this._oDiagram.select(part);
                this._updateHightlights(node);
            }
        },

        /**
         * 링크의 시작,끝점으로 링크 하이라이팅
         *
         * @method highlightLinkByFromTo
         * @param {String,Number} from
         * @param {String,Number} to
         */
        highlightLinkByFromTo: function (from, to) {
            var htLink = this._getLinkObjectByFromTo(from, to);
            if (htLink) {
                this._oDiagram.select(this._oDiagram.findPartForData(htLink));
                this._updateHightlights(this._oDiagram.findLinkForData(htLink));
            }
        },

        /**
         * 시작, 끝점을 이용하여 링크객체 가져오기
         *
         * @method _getLinkObjectByFromTo
         * @param {String,Number} from
         * @param {String,Number} to
         */
        _getLinkObjectByFromTo: function (from, to) {
            var aLink = this._oDiagram.model.linkDataArray;
            for (var i = 0, len = aLink.length; i < len; i += 1) {
                var htLink = aLink[i];
                if (htLink.from === from && htLink.to === to) {
                    return htLink;
                }
            }
            return false;
        },

        _hightlightNode : function (node, nodeText, theme) {
            if (node === null || nodeText === null) { return; }
            if (theme) {
                node.fill = this.option('htHighlightNode')[theme].backgroundColor;
                nodeText.stroke = this.option('htHighlightNode')[theme].fontColor;
            } else {
                node.fill = this.option('htNodeTheme').default.backgroundColor;
                nodeText.stroke = this.option('htNodeTheme').default.fontColor;
            }
        },

        _highlightLink : function (shape, theme) {
            if (shape === null) { return; }
            if (theme) {
                shape.stroke = this.option('htHighlightLink')[theme].borderColor;
            } else {
                shape.stroke = this.option('htLinkTheme').default.borderColor;
            }
        },

        /**
         * if the link connects to this node, highlight it
         *
         * @method _linksTo
         * @param {go.Node} x
         * @param {Number} i
         */
        _linksTo: function (x, i) {
            if (x instanceof go.Node) {
                var links = x.findLinksInto();
                while (links.next()) {
                    links.value.highlight = i;
                }
            }
        },

        /**
         * if the link comes from this node, highlight it
         *
         * @method _linksFrom
         * @param {go.Node} x
         * @param {Number} i
         */
        _linksFrom: function (x, i) {
            if (x instanceof go.Node) {
                var links = x.findLinksOutOf();
                while (links.next()) {
                    links.value.highlight = i;
                }
            }
        },

        /**
         * if selected object is a link, highlight its fromNode, otherwise,
         * highlight the fromNode of each link coming into the selected node
         *
         * @method _nodesTo
         * @param {go.Node} x
         * @param {Number} i
         * @return a List of the keys of the nodes
         */
        _nodesTo: function (x, i) {
            var nodesToList = new go.List("string");
            if (x instanceof go.Link) {
                x.fromNode.highlight = i;
                nodesToList.add(x.data.from);
            } else {
                var nodes = x.findNodesInto();
                while (nodes.next()) {
                    nodes.value.highlight = i;
                    nodesToList.add(nodes.value.data.key);
                }
            }
            return nodesToList;
        },

        /**
         * same as nodesTo, but from instead of to
         *
         * @method _nodesFrom
         * @param {go.Node} x
         * @param {Number} i
         */
        _nodesFrom: function (x, i) {
            var nodesFromList = new go.List("string");
            if (x instanceof go.Link) {
                x.toNode.highlight = i;
                nodesFromList.add(x.data.to);
            } else {
                var nodes = x.findNodesOutOf();
                while (nodes.next()) {
                    nodes.value.highlight = i;
                    nodesFromList.add(nodes.value.data.key);
                }
            }
            return nodesFromList;
        },

        /**
         * event of node click
         *
         * @method _onNodeClicked
         * @param {Event} e
         * @param {ojb} ojb
         */
        _onNodeClicked: function (e, obj) {
            var node = obj.part,
                htData = node.data,
                fOnNodeClicked = this.option('fOnNodeClicked');
            if (_.isFunction(fOnNodeClicked)) {
                fOnNodeClicked.call(this, e, htData);
            }
            // node.diagram.startTransaction("onNodeClick");
            // node.diagram.commitTransaction("onNodeClick");
        },

        /**
         * event of node context click
         *
         * @method _onNodeContextClick
         * @param {Event} e
         * @param {ojb} ojb
         */
        _onNodeContextClicked: function (e, obj) {
            var node = obj.part,
                htData = node.data,
                fOnNodeContextClicked = this.option('fOnNodeContextClicked');
            if (_.isFunction(fOnNodeContextClicked)) {
                fOnNodeContextClicked.call(this, e, htData);
            }
        },

        /**
         * event of link click
         *
         * @method _onLinkClicked
         * @param {Event} e
         * @param {ojb} ojb
         */
        _onLinkClicked: function (e, obj) {
            var node = obj.part,
                htData = node.data,
                fOnLinkClicked = this.option('fOnLinkClicked');
            if (_.isFunction(fOnLinkClicked)) {
                fOnLinkClicked.call(this, e, htData);
            }
        },

        /**
         * event of link context click
         *
         * @method _onLinkContextClicked
         * @param {Event} e
         * @param {ojb} ojb
         */
        _onLinkContextClicked: function (e, obj) {
            var node = obj.part,
                htData = node.data,
                fOnLinkContextClicked = this.option('fOnLinkContextClicked');
            if (_.isFunction(fOnLinkContextClicked)) {
                fOnLinkContextClicked.call(this, e, htData);
            }
        }

    });

})(window, go, jQuery, _);