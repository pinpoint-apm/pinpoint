import cytoscape from 'cytoscape';
import dagre from 'cytoscape-dagre';
import { from as fromArray, fromEvent, iif, zip, merge, Observable } from 'rxjs';
import { mergeMap, map, pluck, switchMap, take, reduce } from 'rxjs/operators';

import ServerMapTheme from './server-map-theme';
import { ServerMapDiagram } from './server-map-diagram.class';
import { ServerMapData } from './server-map-data.class';
import { IServerMapOption } from './server-map-factory';
import { ServerMapTemplate } from './server-map-template';
import { NodeGroup } from './node-group.class';

export class ServerMapDiagramWithCytoscapejs extends ServerMapDiagram {
    private cy: any;

    constructor(
        private option: IServerMapOption
    ) {
        super();
        ServerMapTheme.general.common.funcServerMapImagePath = this.option.funcServerMapImagePath;
        this.makeDiagram();
    }

    private makeDiagram(): void {
        cytoscape.use(dagre);
        this.cy = cytoscape({
            container: this.option.container,
            elements: null,
            style: [
                {
                    selector: 'core',
                    style: {
                        'active-bg-size': 0,
                        // 'active-bg-color': PropertyValueCore<Colour>;
                        'active-bg-opacity': 0
                        // 'active-bg-size': PropertyValueCore<number>;
                        // 'selection-box-color': PropertyValueCore<Colour>;
                        // 'selection-box-border-color': PropertyValueCore<Colour>;
                        // 'selection-box-border-width': PropertyValueCore<number>;
                        // 'selection-box-opacity': PropertyValueCore<number>;
                        // 'outside-texture-bg-color': PropertyValueCore<Colour>;
                        // 'outside-texture-bg-opacity': PropertyValueCore<number>;
                    }
                },
                {
                    // TODO: Restructure ServerMapTheme and replace the style string with constant from it
                    selector: 'node',
                    style: {
                        width: 100,
                        height: 100,
                        'background-color': '#fff',
                        'border-width': '3',
                        'border-color': '#D0D7DF',
                        'text-valign': 'bottom',
                        'text-halign': 'center',
                        'text-margin-y': 4,
                        'background-clip': 'node',
                        'background-image': (ele: any) => ele.data('imgStr'),
                        label: 'data(label)',
                        'overlay-opacity': 0,
                        'font-family': 'Helvetica, Arial, avn85, NanumGothic, ng, dotum, AppleGothic, sans-serif',
                        'font-size': (ele: any) => ele.id() === this.baseApplicationKey ? '14px' : '12px',
                        'font-weight': (ele: any) => ele.id() === this.baseApplicationKey ? 'bold' : 'normal'
                    }
                },
                {
                    selector: 'edge',
                    style: {
                        width: 1.5,
                        'line-color': '#C0C3C8',
                        'target-arrow-shape': 'triangle',
                        'curve-style': 'bezier',
                        // icon unicode: f0b0
                        label: 'data(label)',
                        // label: String.fromCharCode(0xe903),
                        // 'font-family': 'Font Awesome 5 Free',
                        // 'font-size': 13,
                        // 'font-weight': 900,
                        'text-background-color': getComputedStyle(this.option.container).getPropertyValue('--background-color'),
                        'text-background-opacity': 1,
                        // 'text-wrap': 'wrap',
                        // 'font-family': 'FontAwesome, helvetica neue',
                        // 'font-style': 'normal',
                        'overlay-opacity': 0,
                        color: (ele: any) => ele.data('hasAlert') ? '#FF1300' : '#000'
                    }
                },
                {
                    selector: 'edge:loop',
                    style: {
                        'control-point-step-size': 70,
                        'loop-direction': '0deg',
                        'loop-sweep': '-90deg'
                    }
                }
            ],
            wheelSensitivity: 0.3
        });
    }

    private bindEvent(): void {
        this.cy.on('layoutready', () => {
            this.resetViewport();
            this.selectBaseApp();
            this.outRenderCompleted.emit();
        });

        this.cy.nodes().on('select', ({target}: any) => {
            const nodeKey = target.id();
            const nodeData = this.getNodeData(nodeKey);

            this.outClickNode.emit(nodeData);
            this.initStyle();
            this.setStyle(target, 'node');
        });

        this.cy.edges().on('select', ({target}: any) => {
            const edgeKey = target.id();
            const linkData = this.getLinkData(edgeKey);

            this.outClickLink.emit(linkData);
            this.initStyle();
            this.setStyle(target, 'edge');
        });

        this.cy.on('cxttap', ({target, originalEvent}: any) => {
            const {clientX, clientY} = originalEvent;

            setTimeout(() => {
                if (target === this.cy) {
                    this.outContextClickBackground.emit({coordX: clientX, coordY: clientY});
                } else if (target.isEdge() && !NodeGroup.isGroupKey(target.id())) {
                    this.outContextClickLink.emit({
                        key: target.id(),
                        coord: {coordX: clientX, coordY: clientY}
                    });
                }
            });
        });

        this.cy.on('mousemove', ({target}: any) => {
            this.setCursorStyle(target === this.cy ? 'default' : 'pointer');
        });
    }

    setMapData(serverMapData: ServerMapData, baseApplicationKey = ''): void {
        this.serverMapData = serverMapData;
        this.baseApplicationKey = baseApplicationKey;

        const edges = serverMapData.getLinkList().map((link: {[key: string]: any}) => {
            const {from, to, key, totalCount, isFiltered, hasAlert} = link;

            return {
                data: {
                    id: key,
                    source: from,
                    target: to,
                    // [임시]label에서 이미지를 지원하지않아서, filteredMap페이지에서 필터아이콘을 "Filtered" 텍스트로 대체.
                    // label: isFiltered ? ` [Filtered]\n${totalCount.toLocaleString()} ` : ` ${totalCount.toLocaleString()} `,
                    // TODO: Filter Icon 처리
                    label: totalCount.toLocaleString(),
                    hasAlert
                }
            };
        });
        const nodeList = serverMapData.getNodeList();
        const nodes$ = this.getNodesObs(nodeList);

        nodes$.subscribe((nodes: {[key: string]: any}[]) => {
            this.cy.elements().remove();
            this.cy.add({nodes, edges});
            this.bindEvent();
            this.cy.layout({
                name: 'dagre',
                rankDir: 'LR',
                fit: false,
                rankSep: 200
            }).run();
        });
    }

    private getMergedNodeLabel(topCountNodes: {[key: string]: any}[]): string {
        return topCountNodes[0].applicationName;
    }

    private getNodesObs(nodeList: {[key: string]: any}[]): Observable<{[key: string]: any}> {
        return fromArray(nodeList).pipe(
            mergeMap((node: {[key: string]: any}) => {
                const {key, applicationName, serviceType, isAuthorized, hasAlert, topCountNodes} = node;
                const isMergedNode = NodeGroup.isGroupKey(key);
                const serviceTypeImg = new Image();

                serviceTypeImg.src = ServerMapTheme.general.common.funcServerMapImagePath(serviceType);

                const serviceTypeImgLoadEvent$ = merge(
                    fromEvent(serviceTypeImg, 'load'),
                    fromEvent(serviceTypeImg, 'error').pipe(
                        switchMap(() => {
                            // If there is no image file for a serviceType, use NO_IMAGE_FOUND image file instead.
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
                    map((imgStr: string) => {
                        return {
                            data: {
                                id: key,
                                label: isMergedNode ? this.getMergedNodeLabel(topCountNodes) : applicationName,
                                imgStr
                            },
                        };
                    })
                );
            }),
            take(nodeList.length),
            reduce((acc: {[key: string]: any}[], curr: {[key: string]: any}) => {
                return [...acc, curr];
            }, [])
        );
    }

    private getNodeData(key: string): {[key: string]: any} {
        return NodeGroup.isGroupKey(key) ? this.serverMapData.getMergedNodeData(key) : this.serverMapData.getNodeData(key);
    }

    private getLinkData(key: string): {[key: string]: any} {
        return NodeGroup.isGroupKey(key) ? this.serverMapData.getMergedLinkData(key) : this.serverMapData.getLinkData(key);
    }

    private isNodeInDiagram(key: string): boolean {
        return this.cy.getElementById(key).length !== 0;
    }

    private setCursorStyle(cursorStyle: string): void {
        this.option.container.style.cursor = cursorStyle;
    }

    private resetViewport(): void {
        this.cy.zoom(1);
        this.cy.center(this.cy.getElementById(this.baseApplicationKey));
    }

    private selectBaseApp(): void {
        this.cy.getElementById(this.baseApplicationKey).emit('select');
    }

    private initStyle(): void {
        this.cy.nodes().style(this.getInActiveNodeStyle());
        this.cy.edges().style(this.getInActiveEdgeStyle());
    }

    private setStyle(target: any, type: string): void {
        if (type === 'node') {
            target.style(this.getActiveNodeStyle());
            target.connectedEdges().style(this.getActiveEdgeStyle());
        } else {
            target.style(this.getActiveEdgeStyle());
            target.connectedNodes().style(this.getActiveNodeStyle());
        }
    }

    private getInActiveNodeStyle(): {[key: string]: any} {
        return {
            'border-color': '#D0D7DF'
        };
    }

    private getInActiveEdgeStyle(): {[key: string]: any} {
        return {
            'font-size': '12px',
            'font-weight': 'normal',
            'line-color': '#C0C3C8',
            'target-arrow-color': '#C0C3C8'
        };
    }

    private getActiveNodeStyle(): {[key: string]: any} {
        return {
            'border-color': '#4A61D1',
        };
    }

    private getActiveEdgeStyle(): {[key: string]: any} {
        return {
            'font-size': '14px',
            'font-weight': 'bold',
            'line-color': '#4763d0',
            'target-arrow-color': '#4763d0'
        };
    }

    redraw(): void {
        this.setMapData(this.serverMapData, this.baseApplicationKey);
    }

    refresh(): void {
        this.resetViewport();
        this.selectBaseApp();
    }

    clear(): void {
        this.cy.destroy();
    }

    selectNodeBySearch(selectedAppKey: string): void {
        let selectedNodeId = selectedAppKey;
        const isMergedNode = !this.isNodeInDiagram(selectedAppKey);

        if (isMergedNode) {
            const selectedMergedNode = this.serverMapData.getNodeList().find(({key, mergedNodes}: {key: string, mergedNodes: {[key: string]: any}[]}) => {
                return NodeGroup.isGroupKey(key) && mergedNodes.some(({key: nodeKey}: {key: string}) => nodeKey === selectedAppKey);
            });

            selectedNodeId = selectedMergedNode.key;
        }

        this.cy.center(this.cy.getElementById(selectedNodeId));
        this.cy.getElementById(selectedNodeId).emit('select');
    }
}
