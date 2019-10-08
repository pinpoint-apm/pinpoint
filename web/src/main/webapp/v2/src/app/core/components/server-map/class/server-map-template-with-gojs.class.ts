import * as go from 'gojs';
import ServerMapTheme from './server-map-theme';
import { ServerMapDiagramWithGojs } from './server-map-diagram-with-gojs.class';
import { ServerMapNodeClickExtraParam } from './server-map-node-click-extra-param.class';

enum CIRCLE_TYPE {
    GREEN = 'GREEN',
    ORANGE = 'ORANGE',
    RED = 'RED'
}
interface ICircleData {
    x: number;
    y: number;
    startAngle: number;
    sweepAngle: number;
}

go.Shape.defineFigureGenerator('RequestCircle', (shape: go.Shape, w: number, h: number) => {
    const param1 = shape && shape.parameter1 ? shape.parameter1 : null;
    let drawAngle: any = null;

    const rad = w / 2;
    const geo = new go.Geometry();
    const defaultStartAngle = -90;

    if (param1 === null) {
        const fig = new go.PathFigure(rad, 0, false, false);
        geo.add(fig);
        fig.add(new go.PathSegment(go.PathSegment.Arc, defaultStartAngle, 0, rad, rad, rad, rad));
        return geo;
    } else {
        drawAngle = param1;
    }
    if (drawAngle.startAngle && drawAngle.sweepAngle) {
        const fig = new go.PathFigure(drawAngle.x, drawAngle.y, false, false);
        geo.add(fig);
        fig.add(new go.PathSegment(go.PathSegment.Arc, drawAngle.startAngle, drawAngle.sweepAngle, rad, rad, rad, rad));
        // geo.spot1 = new go.Spot(0.156, 0.156);
        // geo.spot2 = new go.Spot(0.844, 0.844);
    } else {
        const fig = new go.PathFigure(rad, 0, false, false);
        geo.add(fig);
        fig.add(new go.PathSegment(go.PathSegment.Arc, defaultStartAngle, 0, rad, rad, rad, rad));
    }
    return geo;
});

function deg2rad(x: number): number {
    return Number.parseFloat(((Math.PI / 180) * x).toFixed(5));
}
function calcuX(value: number, radius: number, base: number): number {
    const correction = value > 90 ? value - 90 : 90 - value;
    return Math.cos(deg2rad(correction)) * radius + base;
}
function calcuY(value: number, radius: number, base: number): number {
    const correction = value - 90;
    return Math.sin(deg2rad(correction)) * radius + base;
}
function calcuCircleData(type: CIRCLE_TYPE, histogram: IResponseTime | IResponseMilliSecondTime, radius: number, baseX: number, baseY: number): ICircleData {
    const sum = Object.keys(histogram).reduce((prev: number, key: string) => {
        return prev + histogram[key];
    }, 0);
    if (sum === 0) {
        return {
            x: 0,
            y: 0,
            startAngle: 0,
            sweepAngle: 0
        };
    }
    const orange = histogram['Slow'];
    const red = histogram['Error'];
    const green = sum - orange - red;
    const greenAngle = green === 0 ? 0 : Math.ceil(360 * green / sum);
    const orangeAngle = orange === 0 ? 0 : Math.ceil(360 * orange / sum);
    const redAngle = red === 0 ? 0 : Math.ceil(360 * red / sum);
    const CIRCLE = {
        x: radius,
        y: 0,
        startAngle: -90,
        sweepAngle: 360
    };

    switch (type) {
        case CIRCLE_TYPE.GREEN:
            return green === 0 ? null : CIRCLE;
        case CIRCLE_TYPE.ORANGE:
            if (orange === sum) {
                return CIRCLE;
            } else if (orange === 0) {
                return null;
            } else {
                return {
                    x: calcuX(greenAngle, radius, baseX),
                    y: calcuY(greenAngle, radius, baseY),
                    startAngle: -90 + greenAngle,
                    sweepAngle: orangeAngle
                };
            }
        case CIRCLE_TYPE.RED:
            if (red === sum) {
                return CIRCLE;
            } else if (red === 0) {
                return null;
            } else {
                return {
                    x: calcuX(greenAngle + orangeAngle, radius, baseX),
                    y: calcuY(greenAngle + orangeAngle, radius, baseY),
                    startAngle: -90 + greenAngle + orangeAngle,
                    sweepAngle: redAngle
                };
            }
    }
}

export class ServerMapTemplateWithGojs {
    public static NO_IMAGE_FOUND = 'NO_IMAGE_FOUND';
    public static CIRCLE_WIDTH = 100;
    public static CIRCLE_HEIGHT = 100;
    public static CIRCLE_RADIUS = 50;
    public static makeNodeTemplate(serverMapComponent: ServerMapDiagramWithGojs) {
        const $ = go.GraphObject.make;
        return $(
            go.Node,
            go.Panel.Auto,
            {
                position: new go.Point(0, 0),
                selectionAdorned: false,
                click: (event: go.InputEvent, obj: go.GraphObject) => {
                    serverMapComponent.onClickNode(event, obj);
                },
                doubleClick: (event: go.InputEvent, obj: go.GraphObject) => {
                    serverMapComponent.onDoubleClickNode(event, obj);
                },
                contextClick: (event: go.InputEvent, obj: go.GraphObject) => {
                    serverMapComponent.onContextClickNode(event, obj);
                }
            },
            new go.Binding('key', 'key'),
            new go.Binding('category', 'serviceType'),
            $(
                go.Shape,
                {
                    strokeWidth: 0,
                    figure: 'Rectangle',
                    fill: null,
                }
            ),
            $(
                go.Panel,
                go.Panel.Table,
                {
                    cursor: 'pointer'
                },
                $(go.RowColumnDefinition, {column: 0, minimum: 108}),
                $(
                    go.Shape,
                    {
                        row: 0,
                        column: 0,
                        width: 108,
                        height: 108,
                        figure: 'Circle',
                        position: new go.Point(-4, -4)
                    },
                    new go.Binding('fill', 'key', (key) => {
                        const type = serverMapComponent.isBaseApplication(key) ? 'main' : 'normal';
                        return ServerMapTheme.general.node[type].fill.top;
                    }),
                    new go.Binding('stroke', 'key', (key, node) => {
                        const type = serverMapComponent.isBaseApplication(key) ? 'main' : 'normal';
                        return ServerMapTheme.general.node[type].stroke;
                    }),
                    new go.Binding('strokeWidth', 'key', (key) => {
                        const type = serverMapComponent.isBaseApplication(key) ? 'main' : 'normal';
                        return ServerMapTheme.general.node[type].strokeWidth;
                    })
                ),
                $(
                    go.Picture,
                    {
                        // row: 0,
                        // column: 0,
                        // width: 100,
                        // height: 100,
                        // imageStretch: go.GraphObject.Uniform,
                        margin: new go.Margin(0, 0, 5, 0),
                        desiredSize: new go.Size(100, 100),
                        imageStretch: go.GraphObject.Uniform,
                        errorFunction: (e: any) => {
                            e.source = ServerMapTheme.general.common.funcServerMapImagePath(ServerMapTemplateWithGojs.NO_IMAGE_FOUND);
                        }
                    },
                    new go.Binding('source', 'serviceType', (type) => {
                        return ServerMapTheme.general.common.funcServerMapImagePath(type);
                    })
                ),
                $(
                    go.Shape,
                    {
                        row: 0,
                        column: 0,
                        fill: null,
                        name: 'BORDER_SHAPE',
                        width: 108,
                        height: 108,
                        figure: 'Circle',
                        portId: '',
                        position: new go.Point(-4, -4),
                        fromLinkable: true, fromLinkableSelfNode: true, fromLinkableDuplicates: true,
                        toLinkable: true, toLinkableSelfNode: true, toLinkableDuplicates: true
                    },
                    new go.Binding('stroke', 'key', (key, node) => {
                        const type = serverMapComponent.isBaseApplication(key) ? 'main' : 'normal';
                        return ServerMapTheme.general.node[type].stroke;
                    }),
                    new go.Binding('strokeWidth', 'key', (key) => {
                        const type = serverMapComponent.isBaseApplication(key) ? 'main' : 'normal';
                        return ServerMapTheme.general.node[type].strokeWidth;
                    })
                ),
                $(
                    go.Shape, 'RequestCircle', {
                        row: 0,
                        column: 0,
                        desiredSize: new go.Size(100, 100),
                        stroke: ServerMapTheme.general.circle.good.stroke,
                        strokeWidth: ServerMapTheme.general.circle.good.strokeWidth,
                        click: (event: go.InputEvent, obj: go.GraphObject) => {
                            serverMapComponent.onClickNode(event, obj, ServerMapNodeClickExtraParam.REQUEST_GREEN);
                            event.handled = true;
                        },
                    },
                    new go.Binding('visible', '', (data) => {
                        return data.isAuthorized && data.isWas;
                    }),
                    new go.Binding('parameter1', '', (data) => {
                        return calcuCircleData(CIRCLE_TYPE.GREEN, data.histogram, 50, 50, 50);
                    })
                ),
                $(
                    go.Shape, 'RequestCircle', {
                        row: 0,
                        column: 0,
                        desiredSize: new go.Size(100, 100),
                        stroke: ServerMapTheme.general.circle.slow.stroke,
                        strokeWidth: ServerMapTheme.general.circle.slow.strokeWidth,
                        click: (event: go.InputEvent, obj: go.GraphObject) => {
                            serverMapComponent.onClickNode(event, obj, ServerMapNodeClickExtraParam.REQUEST_GREEN);
                            event.handled = true;
                        },
                    },
                    new go.Binding('visible', '', (data) => {
                        return data.isAuthorized && data.isWas;
                    }),
                    new go.Binding('parameter1', '', (data) => {
                        return calcuCircleData(CIRCLE_TYPE.ORANGE, data.histogram, 50, 50, 50);
                    })
                ),
                $(
                    go.Shape, 'RequestCircle', {
                        row: 0,
                        column: 0,
                        desiredSize: new go.Size(100, 100),
                        stroke: ServerMapTheme.general.circle.bad.stroke,
                        strokeWidth: ServerMapTheme.general.circle.bad.strokeWidth,
                        click: (event: go.InputEvent, obj: go.GraphObject) => {
                            serverMapComponent.onClickNode(event, obj, ServerMapNodeClickExtraParam.REQUEST_RED);
                            event.handled = true;
                        },
                    },
                    new go.Binding('visible', '', (data) => {
                        return data.isAuthorized && data.isWas;
                    }),
                    new go.Binding('parameter1', '', (data) => {
                        return calcuCircleData(CIRCLE_TYPE.RED, data.histogram, 50, 50, 50);
                    })
                ),
                $(
                    go.TextBlock,
                    {
                        textAlign: 'center',
                        height: 20,
                        row: 1,
                        column: 0,
                        margin: new go.Margin(5, 10, 0, 10)
                    },
                    new go.Binding('font', 'key', (key: string) => {
                        const type = serverMapComponent.isBaseApplication(key) ? 'big' : 'normal';
                        return ServerMapTheme.general.common.font[type];
                    }),
                    new go.Binding('stroke', '', () => {
                        return ServerMapTheme.general.node.normal.text.stroke;
                    }),
                    new go.Binding('text', 'applicationName')
                ),
                $(
                    go.Panel, 'Auto',
                    { row: 0, column: 0, alignment: go.Spot.BottomCenter },
                    $(
                        go.TextBlock,
                        {
                            margin: new go.Margin(0, 0, 10, 0),
                            click: (event: go.InputEvent, obj: go.GraphObject) => {
                                serverMapComponent.onClickNode(event, obj, ServerMapNodeClickExtraParam.INSTANCE_COUNT);
                                event.handled = true;
                            }
                        },
                        new go.Binding('visible', 'instanceCount', (v) => {
                            return v > 1 ? true : false;
                        }),
                        new go.Binding('font', '', () => {
                            return ServerMapTheme.general.common.font.normal;
                        }),
                        new go.Binding('stroke', '', () => {
                            return ServerMapTheme.general.instance.text.stroke;
                        }),
                        new go.Binding('text', 'instanceCount')
                    )
                ),
                $(
                    go.Panel, 'Auto',
                    { row: 0, column: 0, alignment: go.Spot.TopCenter },
                    $(
                        go.Picture,
                        {
                            margin: new go.Margin(5, 0, 0, 0),
                            width: 28,
                            height: 28,
                            source: ServerMapTheme.general.common.funcServerMapImagePath(ServerMapTheme.general.common.icon.filter),
                            visible: false,
                            imageStretch: go.GraphObject.Uniform
                        },
                        new go.Binding('visible', 'isFiltered')
                    )
                )
            ),
            $(
                go.Panel,
                go.Panel.Auto,
                {
                    position: new go.Point(10, 0),
                    width: 120,
                    height: 120
                },
                $(
                    go.Picture,
                    {
                        width: 20,
                        height: 20,
                        source: ServerMapTheme.general.common.funcServerMapImagePath(ServerMapTheme.general.common.icon.error),
                        imageStretch: go.GraphObject.Uniform
                    },
                    new go.Binding('visible', '', (data) => {
                        return data.isAuthorized && data.hasAlert;
                    })
                )
            )
        );
    }
    public static makeNodeGroupTemplate(serverMapComponent: any) {
        const $ = go.GraphObject.make;
        return $(
            go.Node,
            go.Panel.Auto,
            {
                position: new go.Point(0, 0),
                selectionAdorned: false,
                click: (event: go.InputEvent, obj: go.GraphObject) => {
                    serverMapComponent.onClickNode(event, obj);
                },
                doubleClick: (event: go.InputEvent, obj: go.GraphObject) => {
                    serverMapComponent.onDoubleClickNode(event, obj);
                },
                contextClick: (event: go.InputEvent, obj: go.GraphObject) => {
                    serverMapComponent.onContextClickNode(event, obj);
                }
            },
            new go.Binding('key', 'key'),
            new go.Binding('category', 'serviceType'),
            $(
                go.Shape,
                {
                    strokeWidth: 0,
                    figure: 'Rectangle',
                    fill: null,
                }
            ),
            $(
                go.Panel,
                go.Panel.Table,
                {
                    cursor: 'pointer'
                },
                $(go.RowColumnDefinition, {column: 0, minimum: 108}),
                $(
                    go.Shape,
                    {
                        row: 0,
                        column: 0,
                        width: 108,
                        height: 108,
                        figure: 'Circle',
                        position: new go.Point(-4, -4),
                    },
                    new go.Binding('fill', 'key', (key) => {
                        const type = serverMapComponent.isBaseApplication(key) ? 'main' : 'normal';
                        return ServerMapTheme.general.node[type].fill.top;
                    }),
                    new go.Binding('stroke', 'key', (key, node) => {
                        const type = serverMapComponent.isBaseApplication(key) ? 'main' : 'normal';
                        return ServerMapTheme.general.node[type].stroke;
                    }),
                    new go.Binding('strokeWidth', 'key', (key) => {
                        const type = serverMapComponent.isBaseApplication(key) ? 'main' : 'normal';
                        return ServerMapTheme.general.node[type].strokeWidth;
                    })
                ),
                $(
                    go.Picture,
                    {
                        row: 0,
                        column: 0,
                        width: 100,
                        height: 100,
                        imageStretch: go.GraphObject.Uniform,
                        errorFunction: (e: any) => {
                            e.source = ServerMapTheme.general.common.funcServerMapImagePath(ServerMapTemplateWithGojs.NO_IMAGE_FOUND);
                        }
                    },
                    new go.Binding('source', 'serviceType', (type) => {
                        return ServerMapTheme.general.common.funcServerMapImagePath(type);
                    })
                ),
                $(
                    go.Shape,
                    {
                        row: 0,
                        column: 0,
                        fill: null,
                        name: 'BORDER_SHAPE',
                        width: 108,
                        height: 108,
                        figure: 'Circle',
                        portId: '',
                        position: new go.Point(-4, -4),
                        fromLinkable: true, fromLinkableSelfNode: true, fromLinkableDuplicates: true,
                        toLinkable: true, toLinkableSelfNode: true, toLinkableDuplicates: true,
                    },
                    new go.Binding('stroke', 'key', (key, node) => {
                        const type = serverMapComponent.isBaseApplication(key) ? 'main' : 'normal';
                        return ServerMapTheme.general.node[type].stroke;
                    }),
                    new go.Binding('strokeWidth', 'key', (key) => {
                        const type = serverMapComponent.isBaseApplication(key) ? 'main' : 'normal';
                        return ServerMapTheme.general.node[type].strokeWidth;
                    })
                ),
                $(
                    go.TextBlock,
                    {
                        textAlign: 'center',
                        height: 20,
                        row: 1,
                        column: 0,
                        margin: new go.Margin(5, 10, 0, 10)
                    },
                    new go.Binding('font', 'key', (key: string) => {
                        const type = serverMapComponent.isBaseApplication(key) ? 'big' : 'normal';
                        return ServerMapTheme.general.common.font[type];
                    }),
                    new go.Binding('stroke', '', () => {
                        return ServerMapTheme.general.node.normal.text.stroke;
                    }),
                    new go.Binding('text', '', (data) => {
                        return '[' + data.topCountNodes[0].applicationName.split(' ')[1] + ']' + data.serviceType.replace(/(.*)\_.*/ig, '$1');
                    })
                )
            ),
            $(
                go.Panel,
                go.Panel.Auto,
                {
                    position: new go.Point(10, 0),
                    width: 120,
                    height: 120
                },
                $(
                    go.Picture,
                    {
                        width: 20,
                        height: 20,
                        source: ServerMapTheme.general.common.funcServerMapImagePath(ServerMapTheme.general.common.icon.error),
                        imageStretch: go.GraphObject.Uniform
                    },
                    new go.Binding('visible', '', (data) => {
                        return data.isAuthorized && data.hasAlert;
                    })
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
                click: (event: go.InputEvent, obj: go.GraphObject) => {
                    serverMapComponent.onClickLink(event, obj);
                },
                doubleClick: (event: go.InputEvent, obj: go.GraphObject) => {
                    serverMapComponent.onDoubleClickLink(event, obj);
                },
                contextClick: (event: go.InputEvent, obj: go.GraphObject) => {
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
                        fill: getComputedStyle(document.querySelector('.server-map-container')).getPropertyValue('--background'),
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
                        new go.Binding('text', 'totalCount', (val) => {
                            return parseInt(val, 10).toLocaleString();
                        }),
                        new go.Binding('stroke', 'hasAlert', (hasAlert) => {
                            if (hasAlert) {
                                return ServerMapTheme.general.link.normal.fontColor.alert;
                            } else {
                                return ServerMapTheme.general.link.normal.fontColor.normal;
                            }
                        })
                    )
                )
            ),
            new go.Binding('curve', 'curve', (val) => {
                return go.Link[val];
            }),
            new go.Binding('routing', 'routing', (val) => {
                return go.Link[val];
            }),
            new go.Binding('curviness', 'curviness')
        );
    }

}
