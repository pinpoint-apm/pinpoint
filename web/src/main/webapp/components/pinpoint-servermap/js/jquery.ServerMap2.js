(function (window, go, $, _) {
    "use strict";

    /**
     * ServerMap
     *
     * @class ServerMap
     * @version 1.0.0
     * @since Sep, 2013
     * @author Denny Lim<hello@iamdenny.com, iamdenny@nhn.com>
     * @license MIT License
     * @copyright 2014 NAVER Corp.
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
                "sBigFont": "11pt avn85,NanumGothic,ng,dotum,AppleGothic,sans-serif",
                "sSmallFont": "10pt avn55,NanumGothic,ng,dotum,AppleGothic,sans-serif",
                "sImageDir": './images/',
                "sBoldKey": null,
                "htIcons": {
                    'APACHE': 'APACHE.png',
                    'ARCUS': 'ARCUS.png',
                    'BACKEND': 'BACKEND.png',
                    'BLOC': 'BLOC.png',
                    'CASSANDRA': 'CASSANDRA.png',
                    'CUBRID': 'CUBRID.png',
                    'JAVA': 'JAVA.png',
                    'MEMCACHED': 'MEMCACHED.png',
                    'MONGODB': 'MONGODB.png',
                    'MSSQLSERVER': 'MSSQLSERVER.png',
                    'MYSQL': 'MYSQL.png',
                    'NBASE': 'NBASE.png',
                    'NGINX': 'NGINX.png',
                    'ORACLE': 'ORACLE.png',
                    'QUEUE': 'QUEUE.png',
                    'STAND_ALONE': 'STAND_ALONE.png',
                    'TOMCAT': 'TOMCAT.png',
                    'UNKNOWN': 'UNKNOWN.png',
                    'UNKNOWN_GROUP': 'UNKNOWN_GROUP.png',
                    'REDIS': 'REDIS.png',
                    'NBASE_ARC': 'NBASE_ARC.png',
                    'USER': 'USER.png'
                },
                "htNodeTheme": {
                    "default": {
                        "backgroundColor": "#ffffff",
                        "borderColor": "#C5C5C5",
                        "borderWidth": 1,
                        "fontColor": "#1F1F21"
                    },
                    "bold": {
                        "backgroundColor": "#f2f2f2",
                        "borderColor": "#666975",
                        "borderWidth": 2
                    }
                },
                "htLinkType": {
                    "sRouting": "AvoidsNodes", // Normal, Orthogonal, AvoidNodes
                    "sCurve": "JumpGap" // Bezier, JumpOver, JumpGap
                },
                "htLinkTheme": {
                    "default": {
                        "backgroundColor": "#ffffff",
                        "borderColor": "#c5c5c5",
                        "fontFamily": "11pt avn55,NanumGothic,ng,dotum,AppleGothic,sans-serif",
                        "fontColor": "#000000",
                        "fontAlign": "center",
                        "margin": 1
                    },
                    "bad": {
                        "backgroundColor": "#ffc9c9",
                        "borderColor": "#7d7d7d",
                        "fontFamily": "11pt avn55,NanumGothic,ng,dotum,AppleGothic,sans-serif",
                        "fontColor": "#FF1300",
                        "fontAlign": "center",
                        "margin": 1
                    }
                },
                "htHighlightNode": {
                    "borderColor": "#25AFF4",
                    "backgroundColor": "#289E1D",
                    "fontColor": "#ffffff"
                },
                "htHighlightLink": {
                    "borderColor": "#25AFF4"
                },
                "htPadding": {
                    "top": 10,
                    "right": 10,
                    "bottom": 10,
                    "left": 10
                },
                "fOnNodeClicked": function (eMouseEvent, htData) {
                },
                "fOnNodeContextClicked": function (eMouseEvent, htData) {
                },
                "fOnLinkClicked": function (eMouseEvent, htData) {
                },
                "fOnLinkContextClicked": function (eMouseEvent, htData) {
                },
                "fOnBackgroundClicked": function (eMouseEvent, htData) {
                },
                "fOnBackgroundDoubleClicked": function (eMouseEvent, htData) {
                },
                "fOnBackgroundContextClicked": function (eMouseEvent, htData) {
                }
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
            this.nodeClickEventOnce = false;
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

            var infoTableTemplate = self.$(
                go.Panel,
                go.Panel.TableRow,
                {

                },
                self.$(
                    go.TextBlock,
                    {
                        margin: new go.Margin(0, 2),
                        column: 1,
                        stroke: "#848484",
                        font: self.option('sSmallFont')
                    },
                    new go.Binding('text', 'k')
                ),
                self.$(
                    go.TextBlock,
                    {
                        margin: new go.Margin(0, 2),
                        column: 2,
                        stroke: "#848484",
                        font: self.option('sSmallFont')
                    },
                    new go.Binding('text', 'v')
                )
            );

            var getNodeTemplate = function (sImageName) {
                return self.$(
                    go.Node,
                    new go.Binding("category", "serviceType"),
                    go.Panel.Auto,
                    {
                        selectionAdorned: false,
                        cursor: "pointer",
                        name: "NODE",
                        click: function (e, obj) {
                            self._onNodeClicked(e, obj);
                        },
                        contextClick: self._onNodeContextClicked.bind(this)
                    },
                    self.$(
                        go.Shape,
                        {
                            alignment: go.Spot.TopLeft,
                            alignmentFocus: go.Spot.TopLeft,
                            figure: "RoundedRectangle",
                            strokeWidth: 1,
//                            margin: new go.Margin(10, 10, 10, 10),
                            margin: 0,
                            isPanelMain: true,
//                            maxSize: new go.Size(150, NaN),
                            minSize: new go.Size(120, NaN),
                            name: "NODE_SHAPE",
                            portId: ""
                        },
                        new go.Binding("strokeWidth", "key", function (key) {
                            var type = 'default';
                            if (self.option('sBoldKey') && self.option('sBoldKey') === key) {
                                type = 'bold';
                            }
                            return self.option('htNodeTheme')[type].borderWidth;
                        }),
                        new go.Binding("stroke", "key", function (key) {
                            var type = 'default';
                            if (self.option('sBoldKey') && self.option('sBoldKey') === key) {
                                type = 'bold';
                            }
                            return self.option('htNodeTheme')[type].borderColor;
                        }),
                        new go.Binding("fill", "key", function (key) {
                            var type = 'default';
                            if (self.option('sBoldKey') && self.option('sBoldKey') === key) {
                                type = 'bold';
                            }
                            return self.option('htNodeTheme')[type].backgroundColor;
                        }),
                        new go.Binding("key", "key")
                    ),
                    self.$(
                        go.Panel,
                        go.Panel.Spot,
                        {
                            alignment: go.Spot.TopLeft,
                            alignmentFocus: go.Spot.TopLeft
                        },
                        self.$(
                            go.Panel,
                            go.Panel.Vertical,
                            {
                                alignment: go.Spot.TopLeft,
                                alignmentFocus: go.Spot.TopLeft,
                                minSize: new go.Size(120, NaN)
                            },
                            self.$(
                                go.Picture,
                                {
                                    source: sImageDir + sImageName,
                                    margin: new go.Margin(18, 0, 5, 0),
                                    desiredSize: new go.Size(80, 40),
                                    imageStretch: go.GraphObject.Uniform
                                }
                            ),
                            self.$(
                                go.TextBlock,
                                new go.Binding("text", "applicationName").makeTwoWay(),
                                {
                                    alignment: go.Spot.Center,
                                    name: "NODE_TEXT",
                                    margin: 6,
                                    font: self.option('sBigFont'),
//                                    wrap: go.TextBlock.WrapFit,
//                                    width: 130,
                                    editable: false
                                }
                            ),

                            // http://www.gojs.net/latest/samples/entityRelationship.html
                            // http://www.gojs.net/latest/samples/records.html
                            self.$(
                                go.Panel,
                                go.Panel.Table,
                                {
                                    padding: 2,
                                    minSize: new go.Size(100, 10),
                                    defaultStretch: go.GraphObject.Horizontal,
                                    itemTemplate: infoTableTemplate
                                },
                                new go.Binding("itemArray", "infoTable")
                            )
                        ),
                        self.$(
                            go.Panel,
                            go.Panel.Horizontal,
                            {
                                alignment: go.Spot.TopLeft,
                                alignmentFocus: go.Spot.TopLeft,
                                margin: 0
                            },
                            self.$(
                                go.Picture,
                                {
                                    source: sImageDir + 'ERROR.png',
                                    desiredSize: new go.Size(20, 20),
//                                    visible: false,
                                    imageStretch: go.GraphObject.Uniform,
                                    margin: new go.Margin(1, 5, 0, 1)
                                },
                                new go.Binding("visible", "hasAlert")
                            ),
                            self.$(
                                go.Picture,
                                {
                                    source: sImageDir + 'FILTER.png',
                                    desiredSize: new go.Size(17, 17),
                                    visible: false,
                                    imageStretch: go.GraphObject.Uniform
                                },
                                new go.Binding("visible", "isFiltered")
                            )
                        ),
                        self.$(
                            go.Panel,
                            go.Panel.Auto,
                            {
                                alignment: go.Spot.TopRight,
                                alignmentFocus: go.Spot.TopRight,
                                visible: false
                            },
                            new go.Binding("visible", "instanceCount", function (v) {
                                return v > 1 ? true : false;
                            }),
                            self.$(
                                go.Shape,
                                {
                                    figure: "RoundedRectangle",
                                    fill: "#848484",
                                    strokeWidth: 1,
                                    stroke: "#848484"
                                }
                            ),
                            self.$(
                                go.Panel,
                                go.Panel.Auto,
                                {
                                    margin: new go.Margin(0, 3, 0, 3)
                                },
                                self.$(
                                    go.TextBlock,
                                    new go.Binding("text", "instanceCount"),
                                    {
                                        stroke: "#FFFFFF",
                                        textAlign: "center",
                                        height: 16,
                                        font: self.option('sSmallFont'),
                                        editable: false
                                    }
                                )
                            )
                        )
                    )
                );
            };

            var unknownTableTemplate = self.$(
                go.Panel,
                go.Panel.TableRow,
                {
                },
                self.$(
                    go.Picture,
                    {
                        source: sImageDir + 'ERROR.png',
                        margin: new go.Margin(1, 2),
                        desiredSize: new go.Size(10, 10),
                        visible: false,
                        column: 1,
                        imageStretch: go.GraphObject.Uniform
                    },
                    new go.Binding("visible", "hasAlert")
                ),
                self.$(
                    go.TextBlock,
                    {
                        margin: new go.Margin(1, 2),
                        column: 2,
                        font: self.option('sSmallFont'),
//                        height:30,
                        alignment: go.Spot.Left
                    },
                    new go.Binding('text', 'applicationName'),
                    new go.Binding('click', 'key', function (key) {
                        return function (e, obj) {
                            e.bubbles = false;
                            self._onNodeClicked(e, obj, key);
                            return false;
                        };
                    })
                ),
                self.$(
                    go.TextBlock,
                    {
                        margin: new go.Margin(1, 2),
                        column: 3,
                        alignment: go.Spot.Right,
                        font: self.option('sSmallFont')
                    },
                    new go.Binding('text', 'totalCount', function (val) {
                        return Number(val, 10).toLocaleString();
                    })
                )
            );

            var getUnknownGroupTemplate = function (sImageName) {
                return self.$(
                    go.Node,
                    go.Panel.Auto,
                    {
                        selectionAdorned: false,
                        cursor: "pointer",
                        name: "NODE",
                        click: function (e, obj) {
                            if (e.bubbles) {
                                self._onNodeClicked(e, obj);
                            }
                        },
                        contextClick: self._onNodeContextClicked.bind(this)
                    },
                    self.$(
                        go.Shape,
                        {
                            figure: "RoundedRectangle",
//                            margin: new go.Margin(10, 10, 10, 10),
                            isPanelMain: true,
//                            maxSize: new go.Size(150, NaN),
                            minSize: new go.Size(100, 100),
                            name: "NODE_SHAPE",
                            portId: "",
                            strokeWidth: self.option('htNodeTheme').default.borderWidth,
                            stroke: self.option('htNodeTheme').default.borderColor,
                            fill: self.option('htNodeTheme').default.backgroundColor
                        },
                        new go.Binding("key", "key")
                    ),
                    self.$(
                        go.Panel,
                        go.Panel.Spot,
                        {
                        },
                        self.$(
                            go.Panel,
                            go.Panel.Vertical,
                            {
                                alignment: go.Spot.TopLeft,
                                alignmentFocus: go.Spot.TopLeft,
                                minSize: new go.Size(130, NaN)
                            },
                            self.$(
                                go.Picture,
                                {
                                    source: sImageDir + sImageName,
                                    margin: new go.Margin(0, 0, 5, 0),
                                    desiredSize: new go.Size(100, 40),
                                    imageStretch: go.GraphObject.Uniform
                                }
                            ),
                            self.$(
                                go.Panel,
                                go.Panel.Table,
                                {
                                    padding: 2,
                                    minSize: new go.Size(100, 10),
                                    defaultStretch: go.GraphObject.Horizontal,
                                    itemTemplate: unknownTableTemplate,
                                    name: "NODE_TEXT"
                                },
                                new go.Binding("itemArray", "unknownNodeGroup")
                            )
                        )
                    )
                );
            };

            _.each(htIcons, function (sVal, sKey) {
                if (sKey === "UNKNOWN_GROUP") {
                    this._oDiagram.nodeTemplateMap.add(sKey, getUnknownGroupTemplate(sVal));
                } else {
                    this._oDiagram.nodeTemplateMap.add(sKey, getNodeTemplate(sVal));
                }
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
//                    click: this._onLinkClicked.bind(this),
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
                    cursor: "pointer"

                    // curve: go.Link[htLinkType.sCurve],
                    // curve: go.Link.JumpOver
                    // curve: go.Link.JumpGap
                    // curve: go.Link.Bezier
                },
                htLinkTheme = this.option("htLinkTheme"),
                sImageDir = this.option('sImageDir'),
                htDefault = htLinkTheme.default,
                bad = htLinkTheme.bad;

            var getLinkTemplate = function (htOption) {
                return self.$(
                    go.Link,  // the whole link panel
                    // { routing: go.Link.Normal, curve: go.Link.Bezier, toShortLength: 2 },
                    option,
                    new go.Binding("routing", "routing", function (val) {
                        return go.Link[val];
                    }),
                    new go.Binding("curve", "curve", function (val) {
                        return go.Link[val];
                    }),
                    new go.Binding("curviness", "curviness"),
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
                            name: "ARROW",
                            toArrow: "standard",  // toArrow : kite, standard, OpenTriangle
                            fill: htOption.borderColor,
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
                                fill: "#ffffff",
                                stroke: "#ffffff",
                                portId: "",
                                fromLinkable: true,
                                toLinkable: true
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
                                    source: sImageDir + 'FILTER.png',
                                    width: 14,
                                    height: 14,
                                    margin: 1,
                                    visible: false,
                                    imageStretch: go.GraphObject.Uniform
                                },
                                new go.Binding("visible", "isFiltered")
                            ),
                            self.$(
                                go.TextBlock,  // the label
                                {
                                    textAlign: htOption.fontAlign,
                                    font: htOption.fontFamily,
                                    margin: htOption.margin
                                },
//                                new go.Binding("text", "count", function (val) {
                                new go.Binding("text", "totalCount", function (val) {
                                    return Number(val, 10).toLocaleString();
                                }) ,
                                new go.Binding("stroke", "hasAlert", function (hasAlert) {
                                    return (hasAlert) ? bad.fontColor : htDefault.fontColor;
                                })
                            )
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
            var htPadding = this.option('htPadding');
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
            this._oDiagram.padding = new go.Margin(htPadding.top, htPadding.right, htPadding.bottom, htPadding.left);
            this._oDiagram.layout = this.$(
                go.LayeredDigraphLayout,
                { // rdirection: 90,
                    isOngoing: false,
                    layerSpacing: 100,
                    columnSpacing: 30,
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
                var selection = self._oDiagram.selection.first();
                if (selection) {
                    if (selection instanceof go.Node) {
                        if (!self.nodeClickEventOnce) {
                            self._onNodeClicked(e, selection);
                            self.nodeClickEventOnce = true;
                        }
                    } else if (selection instanceof go.Link) {
                        self._onLinkClicked(e, selection);
                    }
                }
                self._updateHightlights();
            });
            this._oDiagram.addDiagramListener("BackgroundSingleClicked", function (e) {
                var fOnBackgroundClicked = self.option('fOnBackgroundClicked');
                if (_.isFunction(fOnBackgroundClicked)) {
                    fOnBackgroundClicked.call(self, e);
                }
            });
            this._oDiagram.addDiagramListener("BackgroundDoubleClicked", function (e) {
                var fOnBackgroundDoubleClicked = self.option('fOnBackgroundDoubleClicked');
                if (_.isFunction(fOnBackgroundDoubleClicked)) {
                    fOnBackgroundDoubleClicked.call(self, e);
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
            this.nodeClickEventOnce = false;
            this._sLastModelData = str;
            this._oDiagram.model = go.Model.fromJson(str);
            this._oDiagram.undoManager.isEnabled = true;
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
            if (selection === null) {
                return;
            }

            this._resetHighlights();
            selection.highlight = 'self';
            if (selection instanceof go.Node) {
//                this._linksTo(selection, 'from');
//                this._linksFrom(selection, 'to');
            } else if (selection instanceof go.Link) {
//                this._nodesTo(selection, 'from');
//                this._nodesFrom(selection, 'to');
            }

            // iterators containing all nodes and links in the diagram
            var allNodes = this._oDiagram.nodes,
                allLinks = this._oDiagram.links;

            // nodes, including groups
            while (allNodes.next()) {
                this._hightlightNode(allNodes.value.findObject("NODE_SHAPE"), allNodes.value.findObject("NODE_TEXT"), allNodes.value.highlight);
            }
            // links
            while (allLinks.next()) {
                this._highlightLink(allLinks.value.findObject("LINK"), allLinks.value.highlight);
//                this._highlightLink(allLinks.value.findObject("LINK2"), allLinks.value.highlight);
                this._highlightLink(allLinks.value.findObject("ARROW"), allLinks.value.highlight, true);
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

        /**
         * highlight node
         * @param nodeShape
         * @param nodeText
         * @param theme
         * @private
         */
        _hightlightNode: function (nodeShape, nodeText, theme) {
            if (nodeShape === null || nodeText === null) {
                return;
            }
            if (theme) {
                nodeShape.stroke = this.option('htHighlightNode').borderColor;
//                nodeText.stroke = this.option('htHighlightNode')[theme].fontColor;
            } else {
                var type = (nodeShape.key === this.option('sBoldKey')) ? 'bold' : 'default';
                nodeShape.stroke = this.option('htNodeTheme')[type].borderColor;
//                nodeText.stroke = this.option('htNodeTheme').default.fontColor;
            }
        },

        /**
         * highlight link
         * @param shape
         * @param theme
         * @private
         */
        _highlightLink: function (shape, theme, toFill) {
            if (shape === null) {
                return;
            }
            var color;
            if (theme) {
                color = this.option('htHighlightLink').borderColor;
            } else {
                color = this.option('htLinkTheme').default.borderColor;
            }
            if (toFill) {
                shape.fill = color;
            } else {
                shape.stroke = color;
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
         * @param {String} unknownKey
         */
        _onNodeClicked: function (e, obj, unknownKey) {
            var node = obj.part,
                htData = node.data,
                fOnNodeClicked = this.option('fOnNodeClicked');
            if (_.isFunction(fOnNodeClicked)) {
                fOnNodeClicked.call(this, e, htData, unknownKey);
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
            var link = obj.part,
                htData = link.data,
                fOnLinkClicked = this.option('fOnLinkClicked');
            if (_.isFunction(fOnLinkClicked)) {
                htData.fromNode = obj.fromNode.part.data;
                htData.toNode = obj.toNode.part.data;
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
            var link = obj.part,
                htData = link.data,
                fOnLinkContextClicked = this.option('fOnLinkContextClicked');
            if (_.isFunction(fOnLinkContextClicked)) {
                htData.fromNode = obj.fromNode.part.data;
                htData.toNode = obj.toNode.part.data;
                fOnLinkContextClicked.call(this, e, htData);
            }
        },

        /**
         * refresh
         */
        refresh: function () {
//            while (this._oDiagram.undoManager.canUndo()) {
//                this._oDiagram.undoManager.undo();
//            }
//            this._oDiagram.zoomToFit();
            this.load(this._sLastModelData);
        },

        /**
         * zoom to fit
         */
        zoomToFit: function () {
            this._oDiagram.zoomToFit();
            this._oDiagram.contentAlignment = go.Spot.Center;
            this._oDiagram.contentAlignment = go.Spot.None;
        }

    });

})(window, go, jQuery, _);