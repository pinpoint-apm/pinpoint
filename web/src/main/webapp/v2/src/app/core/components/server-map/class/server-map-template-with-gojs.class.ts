import * as go from 'gojs';
import ServerMapTheme from './server-map-theme';

export class ServerMapTemplateWithGojs {
    public static NO_IMAGE_FOUND = 'NO_IMAGE_FOUND';
    public static circleMaxSize = 360;
    public static circleMinPercentage = 5;
    public static calcuResponseSummaryCircleSize(sum: number, value: number) {
        let size = 0;
        if (value === 0) {
            return 0;
        }
        const percentage = (value * 100) / sum;
        if (percentage < ServerMapTemplateWithGojs.circleMinPercentage) {
            size = Math.floor((ServerMapTemplateWithGojs.circleMaxSize * ServerMapTemplateWithGojs.circleMinPercentage) / 100);
        } else {
            size = Math.floor((ServerMapTemplateWithGojs.circleMaxSize * percentage) / 100);
        }
        return size;
    }
    public static makeNodeTemplate(serverMapComponent: any) {
        /*
            template structure
            Node
                Panel.Auto
                Shape
                    Panel.Table
                    Shape ( row: 0 ) - background
                    Picture ( row: 0, col: 0 ) - icon
                    Shape ( row: 0, col: 0 ) - red circle
                    Shape( yellow circle )
                    Shape( green circle)
                    Shape ( row: 1 ) - background
                    TextBlock ( applicationName )
                    Panel.Auto
                        Shape
                        TextBlock ( instance count )
                    Panel.Vertical
                        Picture ( error.png )
                        Picture ( filter.png )
        */
        const $ = go.GraphObject.make;
        return $(
            go.Node,
            go.Panel.Auto,
            {
                position: new go.Point(0, 0),
                selectionAdorned: false,
                click: function (event: go.DiagramEvent, obj: go.GraphObject) {
                    serverMapComponent.onClickNode(event, obj);
                },
                doubleClick: function(event: go.DiagramEvent, obj: go.GraphObject) {
                    serverMapComponent.onDoubleClickNode(event, obj);
                },
                contextClick: function(event: go.DiagramEvent, obj: go.GraphObject) {
                    serverMapComponent.onContextClickNode(event, obj);
                }
            },
            new go.Binding('key', 'key'),
            new go.Binding('category', 'serviceType'),
            $(
                go.Shape,
                'Rectangle',
                {
                    name: 'BORDER_SHAPE',
                    stroke: '#D0D7DF',
                    strokeWidth: 0,
                    portId: '',
                    fromLinkable: true, fromLinkableSelfNode: true, fromLinkableDuplicates: true,
                    toLinkable: true, toLinkableSelfNode: true, toLinkableDuplicates: true,
                }
            ),
            $(
                go.Panel,
                go.Panel.Table,
                {
                    cursor: 'pointer'
                    // locationSpot: go.Spot.Center,
                    // selectionAdorned: false,
                },
                // new go.Binding('scale', 'isSelected', (isSelected) => {
                //     return isSelected ? 1.2 : 1.0;
                // }).ofObject(),
                $(go.RowColumnDefinition, {column: 0, minimum: 140}),
                $(
                    go.Shape,
                    'Rectangle',
                    {
                        row: 0,
                        column: 0,
                        height: 95,
                        stretch: go.GraphObject.Horizontal,
                    },
                    new go.Binding('fill', 'key', function(key) {
                        const type = serverMapComponent.isBaseApplication(key) ? 'main' : 'normal';
                        return ServerMapTheme.general.node[type].fill.top;
                    }),
                    new go.Binding('stroke', 'key', function(key, node) {
                        const type = serverMapComponent.isBaseApplication(key) ? 'main' : 'normal';
                        return ServerMapTheme.general.node[type].stroke;
                    }),
                    new go.Binding('strokeWidth', 'key', function(key) {
                        const type = serverMapComponent.isBaseApplication(key) ? 'main' : 'normal';
                        return ServerMapTheme.general.node[type].strokeWidth;
                    })
                ),
                $(
                    go.Picture,
                    {
                        row: 0,
                        column: 0,
                        width: 90,
                        height: 90,
                        imageStretch: go.GraphObject.Uniform,
                        errorFunction: function(e: any) {
                            e.source = ServerMapTheme.general.common.funcServerMapImagePath(ServerMapTemplateWithGojs.NO_IMAGE_FOUND);
                        }
                    },
                    new go.Binding('source', 'serviceType', function (type) {
                        return ServerMapTheme.general.common.funcServerMapImagePath(type);
                    })
                ),
                $(
                    go.Shape, {
                        row: 0,
                        column: 0,
                        stroke: ServerMapTheme.general.circle.bad.stroke,
                        strokeWidth: ServerMapTheme.general.circle.bad.strokeWidth
                    },
                    new go.Binding('visible', '', function (data) {
                        return data.isAuthorized && data.isWas;
                    }),
                    new go.Binding('geometry', 'histogram', function (histogram) {
                        return go.Geometry.parse('M30 0 B270 360 30 30 30 30');
                    })
                ),
                $(
                    go.Shape, {
                        row: 0,
                        column: 0,
                        stroke: ServerMapTheme.general.circle.slow.stroke,
                        strokeWidth: ServerMapTheme.general.circle.slow.strokeWidth
                    },
                    new go.Binding('visible', '', function (data) {
                        return data.isAuthorized && data.isWas;
                    }),
                    new go.Binding('geometry', 'histogram', function (histogram) {
                        if (histogram['Slow'] === 0) {
                            return go.Geometry.parse('M30 0');
                        }
                        const sum = Object.keys(histogram).reduce((prevSum: number, curKey: string) => {
                            return prevSum + histogram[curKey];
                        }, 0);
                        return go.Geometry.parse('M30 0 B270 ' + (ServerMapTemplateWithGojs.circleMaxSize - ServerMapTemplateWithGojs.calcuResponseSummaryCircleSize(sum, histogram['Error'])) + ' 30 30 30 30');
                    })
                ),
                $(
                    go.Shape, {
                        row: 0,
                        column: 0,
                        stroke: ServerMapTheme.general.circle.good.stroke,
                        strokeWidth: ServerMapTheme.general.circle.good.strokeWidth
                    },
                    new go.Binding('visible', '', function (data) {
                        return data.isAuthorized && data.isWas;
                    }),
                    new go.Binding('geometry', 'histogram', function (histogram) {
                        const sum = Object.keys(histogram).reduce((prevSum: number, curKey: string) => {
                            return prevSum + histogram[curKey];
                        }, 0);
                        const size = ServerMapTemplateWithGojs.circleMaxSize - ServerMapTemplateWithGojs.calcuResponseSummaryCircleSize(sum, histogram['Slow']) - ServerMapTemplateWithGojs.calcuResponseSummaryCircleSize(sum, histogram['Error']);
                        if (size >= 180) {
                            return go.Geometry.parse('M30 0 B270 ' + size + ' 30 30 30 30');
                        } else {
                            return go.Geometry.parse('M30 -60 B270 ' + size + ' 30 -30 30 30');
                        }
                    })
                ),
                $(
                    go.Shape,
                    'Rectangle',
                    {
                        row: 1,
                        column: 0,
                        height: 34,
                        margin: new go.Margin(-1, 0, 0, 0),
                        stretch: go.GraphObject.Horizontal,
                    },
                    new go.Binding('fill', 'key', function(key) {
                        const type = serverMapComponent.isBaseApplication(key) ? 'main' : 'normal';
                        return ServerMapTheme.general.node[type].fill.bottom;
                    }),
                    new go.Binding('stroke', 'key', function(key) {
                        const type = serverMapComponent.isBaseApplication(key) ? 'main' : 'normal';
                        return ServerMapTheme.general.node[type].stroke;
                    }),
                    new go.Binding('strokeWidth', 'key', function(key) {
                        const type = serverMapComponent.isBaseApplication(key) ? 'main' : 'normal';
                        return ServerMapTheme.general.node[type].strokeWidth;
                    })
                ),
                $(
                    go.TextBlock,
                    {
                        row: 1,
                        column: 0,
                        margin: new go.Margin(0, 10, 0, 10)
                    },
                    new go .Binding('font', '', function() {
                    return ServerMapTheme.general.common.font.normal;
                    }),
                    new go .Binding('stroke', '', function() {
                        return ServerMapTheme.general.node.normal.text.stroke;
                    }),
                    new go.Binding('text', 'applicationName')
                ),
                $(
                    go.Panel,
                    go.Panel.Auto,
                    {
                        minSize: new go.Size(20, 20),
                        alignment: go.Spot.TopRight
                    },
                    new go.Binding('visible', 'instanceCount', function (v) {
                        return v > 1 ? true : false;
                    }),
                    $(
                        go.Shape,
                        {
                            figure: 'Rectangle'
                        },
                        new go .Binding('fill', '', function() {
                            return ServerMapTheme.general.instance.shape.fill;
                        }),
                        new go .Binding('stroke', '', function() {
                            return ServerMapTheme.general.instance.shape.stroke;
                        }),
                        new go .Binding('strokeWidth', '', function() {
                            return ServerMapTheme.general.instance.shape.strokeWidth;
                        })
                    ),
                    $(
                        go.TextBlock,
                        {
                            height: 20,
                            editable: false,
                            textAlign: 'center',
                            verticalAlignment: go.Spot.Center
                        },
                        new go .Binding('font', '', function() {
                            return ServerMapTheme.general.common.font.small;
                        }),
                        new go .Binding('stroke', '', function() {
                            return ServerMapTheme.general.instance.text.stroke;
                        }),
                        new go.Binding('text', 'instanceCount')
                    )
                ),
                $(
                    go.Panel,
                    go.Panel.Vertical,
                    {
                        margin: new go.Margin(0, 0, 0, 0),
                        alignment: go.Spot.TopLeft
                    },
                    $(
                        go.Picture,
                        {
                            width: 20,
                            height: 20,
                            source: ServerMapTheme.general.common.funcServerMapImagePath(ServerMapTheme.general.common.icon.error),
                            imageStretch: go.GraphObject.Uniform
                        },
                        new go.Binding('visible', '', function (data) {
                            return data.isAuthorized && data.hasAlert;
                        })
                    ),
                    $(
                        go.Picture,
                        {
                            width: 28,
                            height: 28,
                            source: ServerMapTheme.general.common.funcServerMapImagePath(ServerMapTheme.general.common.icon.filter),
                            visible: false,
                            imageStretch: go.GraphObject.Uniform
                        },
                        new go.Binding('visible', 'isFiltered')
                    )
                )
            )
        );
    }
    public static makeNodeGroupTemplate(serverMapComponent: any) {
        /*
            template structure
            Node
                Panel.Auto
                Shape
                    Panel.Table
                    Shape ( row: 0, col: 0 ) - background
                    Button
                    Picture ( row: 1, col: 0 ) - icon
                    Shape ( row: 1, col: 0 ) - background
                    Panel.Vertical( row: 1, col: 0 )           : bind>mergedNodes
                        Panel.Table
                            Panel.TableRow
                                Picture ( Error.png )
                                TextBlock ( index )
                                TextBlock ( applicationName )
                                TextBlock ( totalCount )

                    Panel.Vertical( row: 1, col: 0 )            : bind>mergedMultiSourceNodes
                        Panel.Vertical
                            TextBlock - applicaitonName
                            Panel.Table                         : bind>group
                                Panel.TableRow
                                    Picture ( Error.png )
                                    TextBlock ( index )
                                    TextBlock ( applicationName )
                                    TextBlock ( totalCount )
                    Panel
                        Shape
                        TextBlock ( instaceCount )
        */
        const $ = go.GraphObject.make;
        const groupTableTemplate = $(
            go.Panel,
            go.Panel.TableRow,
            $(
                go.Picture, {
                    column: 1,
                    source: ServerMapTheme.general.common.funcServerMapImagePath(ServerMapTheme.general.common.icon.error),
                    margin: new go.Margin(1, 2),
                    visible: false,
                    desiredSize: new go.Size(10, 10),
                    imageStretch: go.GraphObject.Uniform
                },
                new go.Binding('visible', 'hasAlert')
            ),
            $(
                go.TextBlock, {
                    name: 'NODE_APPLICATION_NAME',
                    font: ServerMapTheme.general.common.font.small,
                    margin: new go.Margin(1, 2),
                    column: 2,
                    alignment: go.Spot.Left
                },
                new go.Binding('stroke', 'tableHeader', function( tableHeader ) {
                    return tableHeader === true ? '#1BABF4' : '#000';
                }),
                new go.Binding('text', 'applicationName')
            ),
            $(
                go.TextBlock, {
                    font: ServerMapTheme.general.common.font.small,
                    margin: new go.Margin(1, 2),
                    column: 3,
                    alignment: go.Spot.Right
                },
                new go.Binding('text', 'totalCount', function (val) {
                    return val === '' ? '' : parseInt(val, 10).toLocaleString();
                })
            )
        );
        return $(
            go.Node,
            go.Panel.Auto,
            {
                position: new go.Point(0, 0),
                selectionAdorned: false,
                click: function (event: go.DiagramEvent, obj: go.GraphObject) {
                    serverMapComponent.onClickNode(event, obj);
                },
                doubleClick: function(event: go.DiagramEvent, obj: go.GraphObject) {
                    serverMapComponent.onDoubleClickNode(event, obj);
                },
                contextClick: function(event: go.DiagramEvent, obj: go.GraphObject) {
                    serverMapComponent.onContextClickNode(event, obj);
                }
            },
            $(
                go.Shape,
                'Rectangle',
                {
                    name: 'BORDER_SHAPE',
                    fill: '#FFF',
                    stroke: '#D0D7DF',
                    strokeWidth: 0
                }
            ),
            $(
                go.Panel,
                go.Panel.Table,
                {
                    cursor: 'pointer'
                },
                $(go.RowColumnDefinition, {row: 0, column: 0, width: 140, height: 95}),
                $(go.RowColumnDefinition, {row: 1, minimum: 30, stretch: go.GraphObject.Fill}),
                $(
                    go.Shape,
                    'Rectangle',
                    {
                        row: 0,
                        name: 'TOP_RECT',
                        column: 0,
                        height: 95,
                        strokeWidth: 1,
                        stretch: go.GraphObject.Horizontal,
                        stroke: '#D0D7DF'
                    },
                    new go.Binding('fill', 'key', function(key) {
                        const type = serverMapComponent.isBaseApplication(key) ? 'main' : 'normal';
                        return ServerMapTheme.general.node[type].fill.top;
                    }),
                    new go.Binding('stroke', 'key', function(key) {
                        const type = serverMapComponent.isBaseApplication(key) ? 'main' : 'normal';
                        return ServerMapTheme.general.node[type].stroke;
                    }),
                    new go.Binding('strokeWidth', 'key', function(key) {
                        const type = serverMapComponent.isBaseApplication(key) ? 'main' : 'normal';
                        return ServerMapTheme.general.node[type].strokeWidth;
                    })
                ),
                $(
                    go.Picture,
                    {
                        row: 0,
                        column: 0,
                        width: 90,
                        height: 90,
                        imageStretch: go.GraphObject.Uniform,
                        errorFunction: function(e: any) {
                            e.source = ServerMapTheme.general.common.funcServerMapImagePath(ServerMapTemplateWithGojs.NO_IMAGE_FOUND);
                        }
                    },
                    new go.Binding('source', 'serviceType', function (type) {
                        return ServerMapTheme.general.common.funcServerMapImagePath(type);
                    })
                ),
                $(
                    go.Shape,
                    'Rectangle',
                    {
                        row: 1,
                        name: 'BOTTOM_RECT',
                        column: 0,
                        margin: new go.Margin(-1, 0, 0, 0),
                        minSize: new go.Size(140, 30),
                        stretch: go.GraphObject.Fill
                    },
                    new go.Binding('fill', 'key', function(key) {
                        const type = serverMapComponent.isBaseApplication(key) ? 'main' : 'normal';
                        return ServerMapTheme.general.node[type].fill.bottom;
                    }),
                    new go.Binding('stroke', 'key', function(key) {
                        const type = serverMapComponent.isBaseApplication(key) ? 'main' : 'normal';
                        return ServerMapTheme.general.node[type].stroke;
                    }),
                    new go.Binding('strokeWidth', 'key', function(key) {
                        const type = serverMapComponent.isBaseApplication(key) ? 'main' : 'normal';
                        return ServerMapTheme.general.node[type].strokeWidth;
                    })
                ),
                $(
                    go.Panel,
                    go.Panel.Vertical, {
                        row: 1,
                        column: 0,
                        margin: new go.Margin(3, 0, 3, 0),
                        minSize: new go.Size(138, NaN),
                        alignment: go.Spot.TopLeft,
                        alignmentFocus: go.Spot.TopLeft
                    },
                    $(
                        go.Panel,
                        go.Panel.Table, {
                            padding: 6,
                            visible: true,
                            minSize: new go.Size(138, NaN),
                            itemTemplate: groupTableTemplate,
                            defaultStretch: go.GraphObject.Horizontal
                        },
                        new go.Binding('itemArray', 'topCountNodes')
                    )
                ),
                $(
                    go.Panel,
                    go.Panel.Auto,
                    {
                        minSize: new go.Size(20, 20),
                        alignment: go.Spot.TopRight
                    },
                    new go.Binding('visible', 'instanceCount', function (v) {
                        return v > 1 ? true : false;
                    }),
                    $(
                        go.Shape,
                        {
                            figure: 'Rectangle'
                        },
                        new go .Binding('fill', '', function() {
                            return ServerMapTheme.general.instance.shape.fill;
                        }),
                        new go .Binding('stroke', '', function() {
                            return ServerMapTheme.general.instance.shape.stroke;
                        }),
                        new go .Binding('strokeWidth', '', function() {
                            return ServerMapTheme.general.instance.shape.strokeWidth;
                        })
                    ),
                    $(
                        go.TextBlock,
                        {
                            height: 20,
                            editable: false,
                            textAlign: 'center',
                            verticalAlignment: go.Spot.Center
                        },
                        new go .Binding('font', '', function() {
                            return ServerMapTheme.general.common.font.small;
                        }),
                        new go .Binding('stroke', '', function() {
                            return ServerMapTheme.general.instance.text.stroke;
                        }),
                        new go.Binding('text', 'instanceCount')
                    )
                )
            )
        );
    }
    public static makeLinkTemplate(serverMapComponent: any) {
        const $ = go.GraphObject.make;
        return $(
            go.Link,
            {
                corner: 10,
                cursor: 'pointer',
                layerName: 'Foreground',
                reshapable: false,
                selectionAdorned: false,
                click: function (event: go.DiagramEvent, obj: go.GraphObject) {
                    serverMapComponent.onClickLink(event, obj);
                },
                doubleClick: function(event: go.DiagramEvent, obj: go.GraphObject) {
                    serverMapComponent.onDoubleClickLink(event, obj);
                },
                contextClick: function (event: go.DiagramEvent, obj: go.GraphObject) {
                    serverMapComponent.onContextClickLink(event, obj);
                }
            },
            $(
                go.Shape,
                {
                    name: 'LINK',
                    stroke: ServerMapTheme.general.link.normal.line.stroke,
                    isPanelMain: true,
                    strokeWidth: ServerMapTheme.general.link.normal.line.strokeWidth
                }
            ),
            $(
                go.Shape,
                {
                    name: 'ARROW',
                    fill: ServerMapTheme.general.link.normal.arrow.fill,
                    scale: 1.5,
                    stroke: ServerMapTheme.general.link.normal.arrow.stroke,
                    toArrow: 'standard'
                }
            ),
            $(
                go.Panel,
                go.Panel.Auto,
                $(
                    go.Shape,
                    'Rectangle',
                    {
                        fill: ServerMapTheme.general.link.normal.textBox.fill,
                        stroke: ServerMapTheme.general.link.normal.textBox.stroke,
                        portId: '',
                        fromLinkable: true,
                        toLinkable: true
                    }
                ),
                $(
                    go.Panel,
                    go.Panel.Horizontal,
                    {
                        margin: 4
                    },
                    $(
                        go.Picture,
                        {
                            source: ServerMapTheme.general.common.funcServerMapImagePath(ServerMapTheme.general.common.icon.filter),
                            width: 28,
                            height: 28,
                            margin: 1,
                            visible: false,
                            imageStretch: go.GraphObject.Uniform
                        },
                        new go.Binding('visible', 'isFiltered')
                    ),
                    $(
                        go.TextBlock,
                        {
                            name: 'LINK_TEXT',
                            font: ServerMapTheme.general.common.font.normal,
                            margin: new go.Margin(1),
                            textAlign: 'center'
                        },
                        new go.Binding('text', 'totalCount', function (val) {
                            return parseInt(val, 10).toLocaleString();
                        }),
                        new go.Binding('stroke', 'hasAlert', function (hasAlert) {
                            if ( hasAlert ) {
                                return ServerMapTheme.general.link.normal.fontColor.alert;
                            } else {
                                return ServerMapTheme.general.link.normal.fontColor.normal;
                            }
                        })
                    )
                )
            ),
            new go.Binding('curve', 'curve', function (val) {
                console.log( 'curve', val );
                return go.Link[val];
            }),
            new go.Binding('routing', 'routing', function (val) {
                console.log( 'routing', val );
                return go.Link[val];
            }),
            new go.Binding('curviness', 'curviness')
        );
    }

}
