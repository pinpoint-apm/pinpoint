import { InjectionToken } from '@angular/core';

import { ServerMapDiagram } from 'app/core/components/server-map/class/server-map-diagram.class';
import { ServerMapDiagramWithCytoscapejs } from 'app/core/components/server-map/class/server-map-diagram-with-cytoscapejs.class';

export const SERVER_MAP_TYPE = new InjectionToken('server-map-type');
export const enum ServerMapType {
    CYTOSCAPEJS = 'cytoscapejs'
}
export interface IServerMapOption {
    container: HTMLDivElement;
    funcServerMapImagePath: Function;
}

export class ServerMapFactory {
    static createServerMap(type: ServerMapType, option: IServerMapOption): ServerMapDiagram {
        switch (type) {
            case ServerMapType.CYTOSCAPEJS:
                return new ServerMapDiagramWithCytoscapejs(option);
        }
    }
}
