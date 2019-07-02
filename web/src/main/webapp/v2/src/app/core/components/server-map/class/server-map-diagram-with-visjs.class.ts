import { Network, NodeOptions, EdgeOptions, Color, Node, Edge, Options } from 'vis';
import { from as fromArray, fromEvent, iif, zip, merge } from 'rxjs';
import { mergeMap, map, pluck, take, reduce, switchMap, tap } from 'rxjs/operators';

import ServerMapTheme from './server-map-theme';
import { ServerMapDiagram } from './server-map-diagram.class';
import { ServerMapData } from './server-map-data.class';
import { IServerMapOption } from './server-map-factory';
import { ServerMapTemplate } from './server-map-template';
import { NodeGroup } from './node-group.class';

export class ServerMapDiagramWithVisjs extends ServerMapDiagram {
    private diagram: Network;
    private isFirstLoad: boolean;

    constructor(
        private option: IServerMapOption
    ) {
        super();
        ServerMapTheme.general.common.funcServerMapImagePath = this.option.funcServerMapImagePath;
        this.makeDiagram();
        this.setEvent();
    }

    private makeDiagram(): void {
        const container = this.option.container;
        const data = {
            nodes: [] as Node[],
            edges: [] as Edge[]
        };
        const options = {
            interaction: {
                hover: true,
            },
            nodes: {
                borderWidth: 2.5,
                color: {
                    border: 'transparent',
                    highlight: {
                        border: ServerMapTheme.general.node.highlight.border.stroke,
                    }
                } as Color,
                labelHighlightBold: false,
                shape: 'circularImage',
                shapeProperties: {
                    useBorderWithImage: false,
                    useImageSize: true
                },
                size: 65
            } as NodeOptions,
            edges: {
                arrows: {
                    to: {
                        enabled: true,
                        scaleFactor: 0.75
                    }
                },
                // arrowStrikethrough: false,
                color: {
                    color: ServerMapTheme.general.link.normal.line.stroke,
                    highlight: ServerMapTheme.general.link.highlight.line.stroke
                },
                font: {
                    align: 'horizontal',
                    size: 18,
                    background: ServerMapTheme.general.link.normal.textBox.fill,
                },
                smooth: {
                    type: 'curvedCW',
                    roundness: 0.1
                }
            } as EdgeOptions,
            groups: {
                main: {
                    color: {
                        background: ServerMapTheme.general.node.main.fill.top,
                        highlight: {
                            background: ServerMapTheme.general.node.main.fill.top
                        }
                    }
                },
                normal: {
                    color: {
                        background: ServerMapTheme.general.node.normal.fill.top,
                        highlight: {
                            background: ServerMapTheme.general.node.normal.fill.top
                        }
                    }
                }
            },
            layout: {
                hierarchical: {
                    enabled: true,
                    levelSeparation: 300,
                    nodeSpacing: 235,
                    // blockShifting: false,
                    // edgeMinimization: false,
                    direction: 'LR',
                    sortMethod: 'directed'
                }
            },
            physics: {
                enabled: false
            }
        } as Options;
        this.diagram = new Network(container, data, options);
    }

    private setEvent(): void {
        this.diagram.on('afterDrawing', () => {
            if (!this.serverMapData) {
                return;
            }

            this.outRenderCompleted.emit();
        });
        this.diagram.on('click', (({nodes, edges}: {nodes: string[], edges: string[]}) => {
            const isNodeClicked = nodes.length !== 0;
            const isEdgeClicked = !isNodeClicked && edges.length !== 0;
            const isBackgroundClicked = !(isNodeClicked || isEdgeClicked);

            if (isNodeClicked) {
                const nodeId = nodes[0];
                const nodeData = this.getNodeData(nodeId);

                this.outClickNode.emit(nodeData);
            } else if (isEdgeClicked) {
                const edgeId = edges[0];
                const linkData = this.getLinkData(edgeId);

                this.outClickLink.emit(linkData);
            } else if (isBackgroundClicked) {
                this.outClickBackground.emit();
            }
        }));
        this.diagram.on('hoverNode', () => this.changeCursor('pointer'));
        this.diagram.on('hoverEdge', () => this.changeCursor('pointer'));
        this.diagram.on('blurNode', () => this.changeCursor('default'));
        this.diagram.on('blurEdge', () => this.changeCursor('default'));
        this.diagram.on('oncontext', (({event, pointer}: {event: MouseEvent, pointer: {[key: string]: any}}) => {
            event.preventDefault();
            const { x, y } = pointer.DOM;
            const nodeId = this.diagram.getNodeAt({x, y}) as string;
            const edgeId = this.diagram.getEdgeAt({x, y}) as string;

            if (nodeId || NodeGroup.isGroupKey(edgeId)) {
                return;
            }

            edgeId ? (
                this.outContextClickLink.emit({
                    key: edgeId,
                    coord: {
                        coordX: x,
                        coordY: y
                    }
                })
            ) : (
                this.outContextClickBackground.emit({
                    coordX: x,
                    coordY: y
                })
            );
        }));
    }

    setMapData(serverMapData: ServerMapData, baseApplicationKey = ''): void {
        this.isFirstLoad = !this.serverMapData ? true : false;
        this.serverMapData = serverMapData;
        this.baseApplicationKey = baseApplicationKey;
        const nodeList = serverMapData.getNodeList();
        const isDataEmpty = nodeList.length === 0;

        if (isDataEmpty) {
            return;
        }

        const edges = serverMapData.getLinkList().map((link: {[key: string]: any}) => {
            const { from, to, key, totalCount, isFiltered, hasAlert } = link;

            return {
                from,
                to,
                id: key,
                // [임시]label에서 이미지를 지원하지않아서, filteredMap페이지에서 필터아이콘을 "Filtered" 텍스트로 대체.
                label: isFiltered ? ` [Filtered]\n${totalCount.toLocaleString()} ` : ` ${totalCount.toLocaleString()} `,
                font: {
                    color: hasAlert ? ServerMapTheme.general.link.normal.fontColor.alert : ServerMapTheme.general.link.normal.fontColor.normal
                }
            };
        });

        fromArray(nodeList).pipe(
            mergeMap((node: {[key: string]: any}) => {
                const { key, applicationName, serviceType, isAuthorized, topCountNodes, hasAlert } = node;
                const isMergedNode = NodeGroup.isGroupKey(key);
                const serviceTypeImg = new Image();

                serviceTypeImg.src = ServerMapTheme.general.common.funcServerMapImagePath(serviceType);

                const serviceTypeImgLoadEvent$ = merge(
                    fromEvent(serviceTypeImg, 'load'),
                    fromEvent(serviceTypeImg, 'error').pipe(
                        switchMap(() => {
                            // 해당 serviceType 이름의 이미지가 없을 경우, NO_IMAGE_FOUND 이미지로 대체
                            const tempImg = new Image();

                            tempImg.src = ServerMapTheme.general.common.funcServerMapImagePath('NO_IMAGE_FOUND');
                            return fromEvent(tempImg, 'load');
                        })
                    )
                );
                const innerObs$ = iif(() => hasAlert && isAuthorized,
                    (() => {
                        const alertImg = new Image();

                        alertImg.src = ServerMapTheme.general.common.funcServerMapImagePath(ServerMapTheme.general.common.icon.error);
                        return zip(
                            serviceTypeImgLoadEvent$.pipe(pluck('target')),
                            fromEvent(alertImg, 'load').pipe(pluck('target'))
                        );
                    })(),
                    serviceTypeImgLoadEvent$.pipe(map((v: Event) => [v.target]))
                );

                return innerObs$.pipe(
                    map((img: HTMLImageElement[]) => {
                        const svg = ServerMapTemplate.getSVGString(img, node);

                        return 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(svg);
                    }),
                    map((image: string) => {
                        return {
                            id: key,
                            label: isMergedNode ? this.getMergedNodeLabel(topCountNodes) : applicationName,
                            image,
                            group: key === baseApplicationKey ? 'main' : 'normal'
                        };
                    })
                );
            }),
            take(nodeList.length),
            tap(({id: key}: {id: string}) => {
                if (key === baseApplicationKey) {
                    this.outClickNode.emit(this.getNodeData(key));
                }
            }),
            reduce((acc: Node[], curr: Node) => {
                return [...acc, curr];
            }, [] as Node[]),
        ).subscribe((nodes: Node[]) => {
            if (this.isFirstLoad) {
                this.diagram.redraw();
            }

            this.diagram.setData({nodes, edges});
            this.diagram.selectNodes([baseApplicationKey]);
        });
    }

    private getMergedNodeLabel(topCountNodes: {[key: string]: any}[]): string {
        // [임시] 일단은 "Total: 4(Merge된 노드 개수)"만 표시
        return topCountNodes[0].applicationName;
    }

    private getNodeData(key: string): {[key: string]: any} {
        return NodeGroup.isGroupKey(key) ? this.serverMapData.getMergedNodeData(key) : this.serverMapData.getNodeData(key);
    }

    private getLinkData(key: string): {[key: string]: any} {
        return NodeGroup.isGroupKey(key) ? this.serverMapData.getMergedLinkData(key) : this.serverMapData.getLinkData(key);
    }

    private isNodeInDiagram(key: string): boolean {
        return this.diagram.findNode(key).length !== 0;
    }

    private setNodeClicked(key: string): void {
        this.diagram.selectNodes([key]);
        this.outClickNode.emit(this.getNodeData(key));
    }

    private changeCursor(cursorStyle: string): void {
        this.option.container.querySelector('canvas').style.cursor = cursorStyle;
    }

    refresh(): void {
        this.setMapData(this.serverMapData, this.baseApplicationKey);
    }

    clear(): void {
        this.diagram.destroy();
    }

    selectNodeBySearch(selectedAppKey: string): void {
        let selectedNodeId = selectedAppKey;
        const isMergedNode = !this.isNodeInDiagram(selectedAppKey);

        if (isMergedNode) {
            const groupKey = selectedAppKey.split('^')[1];
            const selectedMergedNode = this.serverMapData.getNodeList().find(({key}: {key: string}) => {
                return NodeGroup.isGroupKey(key) && key.includes(groupKey);
            });

            selectedNodeId = selectedMergedNode.key;
        }

        this.diagram.focus(selectedNodeId);
        this.setNodeClicked(selectedNodeId);
    }
}
