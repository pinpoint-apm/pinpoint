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
    outContextClickBackground = new EventEmitter<ICoordinate>();
    outRenderCompleted = new EventEmitter<any>();

    abstract setMapData(mapData: ServerMapData, baseApplicationKey?: string): void;
    abstract selectNodeBySearch(appKey: string): void;
    abstract redraw(): void;
    abstract refresh(): void;
    abstract clear(): void;

    searchNode(query: string): IApplication[] {
        return this.serverMapData.getNodeList()
            .reduce((prev: {[key: string]: any}[], curr: {[key: string]: any}) => {
                const { key, mergedNodes } = curr;

                return NodeGroup.isGroupKey(key) ? [...prev, ...mergedNodes] : [...prev, curr];
            }, [])
            .filter(({applicationName}: {applicationName: string}) => {
                const regCheckQuery = new RegExp(query, 'i');

                return regCheckQuery.test(applicationName);
            })
            .map(({key, applicationName, serviceType}: {key: string, applicationName: string, serviceType: string}) => {
                return new Application(applicationName, serviceType, 0, key);
            });
    }

    setMergeState(mergeState: IServerMapMergeState): void {
        this.serverMapData.setMergeState(mergeState);
    }

    resetMergeState(): void {
        this.serverMapData.resetMergeState();
        this.redraw();
    }
}
