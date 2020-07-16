import { EventEmitter } from '@angular/core';

import { ServerMapData } from './server-map-data.class';
import { NodeGroup } from './node-group.class';
import { Application } from 'app/core/models';

export abstract class ServerMapDiagram {
    protected serverMapData: ServerMapData;
    protected baseApplicationKey: string;

    outClickNode = new EventEmitter<any>();
    outClickLink = new EventEmitter<any>();
    outContextClickLink = new EventEmitter<any>();
    outContextClickNode = new EventEmitter<any>();
    outContextClickBackground = new EventEmitter<ICoordinate>();
    outRenderCompleted = new EventEmitter<any>();

    abstract setMapData(mapData: ServerMapData, baseApplicationKey?: string): void;
    abstract selectNodeBySearch(appKey: string): void;
    abstract redraw(): void;
    abstract refresh(): void;
    abstract clear(): void;

    setMergeState(mergeState: IServerMapMergeState): void {
        this.serverMapData.setMergeState(mergeState);
    }

    resetMergeState(): void {
        this.serverMapData.resetMergeState();
        this.redraw();
    }
}
