import { InjectionToken } from '@angular/core';

import { ServerMapDiagram } from 'app/core/components/server-map/class/server-map-diagram.class';
import { ServerMapDiagramWithGojs } from 'app/core/components/server-map/class/server-map-diagram-with-gojs.class';
import { ServerMapDiagramWithVisjs } from 'app/core/components/server-map/class/server-map-diagram-with-visjs.class';

export const SERVER_MAP_TYPE = new InjectionToken('server-map-type');
export const enum ServerMapType {
    GOJS = 'gojs',
    VISJS = 'visjs'
}
export interface IServerMapOption {
    container: HTMLDivElement;
    funcServerMapImagePath: Function;
}

export class ServerMapFactory {
    static createServerMap(type: ServerMapType, option: IServerMapOption): ServerMapDiagram {
        switch (type) {
            case ServerMapType.GOJS:
                return new ServerMapDiagramWithGojs(option);
            case ServerMapType.VISJS:
                return new ServerMapDiagramWithVisjs(option);
        }
    }
}
