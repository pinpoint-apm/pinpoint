import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { shareReplay } from 'rxjs/operators';

import { ServerMapData } from 'app/core/components/server-map/class/server-map-data.class';

@Injectable()
export class AgentHistogramDataService {
    readonly url = 'getResponseTimeHistogramDataV2.pinpoint';

    private previousFrom: number;
    private previousTo: number;
    private previousName: string;
    private previousCode: string;
    private previousKey: string;
    private previousObservable: any;

    constructor(
        private http: HttpClient,
    ) {}

    getData(serverMapData: ServerMapData, [from, to]: number[], target: any): Observable<any> {
        const {key, applicationName, serviceTypeCode} = target;

        if (!this.isCached(key, applicationName, serviceTypeCode, [from, to])) {
            this.previousObservable = this.http.post(this.url, this.makeBodyData(serverMapData, key), this.makeRequestOptionsArgs(applicationName, serviceTypeCode, [from, to])).pipe(
                shareReplay(1)
            );
            this.previousFrom = from;
            this.previousTo = to;
            this.previousName = applicationName;
            this.previousCode = serviceTypeCode;
            this.previousKey = key;
        }

        return this.previousObservable;
    }

    private isCached(key: string, applicationName: string, serviceTypeCode: string, [from, to]: number[]): boolean {
        return this.previousCode === serviceTypeCode &&
            this.previousKey === key &&
            this.previousName === applicationName &&
            this.previousFrom === from &&
            this.previousTo === to;
    }

    private makeRequestOptionsArgs(applicationName: string, serviceTypeCode: string, [from, to]: number[]): object {
        return {
            params: {
                applicationName,
                serviceTypeCode,
                from,
                to
            }
        };
    }

    private makeBodyData(serverMapData: ServerMapData, selectedNodeKey: string): any {
        const linkedNodeData: {[key: string]: any} = {
            from: [],
            to: []
        };

        serverMapData.getOriginalLinkList().forEach((link: ILinkInfo) => {
            if (link.from === selectedNodeKey) {
                if (link.targetInfo instanceof Array) {
                    link.targetInfo.forEach((targetLinkInfo: ILinkInfo) => {
                        linkedNodeData.to.push([targetLinkInfo.targetInfo.applicationName, targetLinkInfo.targetInfo.serviceTypeCode]);
                    });
                } else {
                    linkedNodeData.to.push([link.targetInfo.applicationName, link.targetInfo.serviceTypeCode]);
                }
            } else if (link.to === selectedNodeKey) {
                linkedNodeData.from.push([link.sourceInfo.applicationName, link.sourceInfo.serviceTypeCode]);
            }
        });

        return linkedNodeData;
    }
}
