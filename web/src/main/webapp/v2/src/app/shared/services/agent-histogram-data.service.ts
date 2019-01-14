import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import * as moment from 'moment-timezone';
import { Observable, of } from 'rxjs';
import { shareReplay } from 'rxjs/operators';
import { NewUrlStateNotificationService } from 'app/shared/services/new-url-state-notification.service';

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
        private newUrlStateNotificationService: NewUrlStateNotificationService
    ) {}
    getData(key: string, applicationName: string, serviceTypeCode: string, serverMapData: any, from?: number, to?: number): Observable<any> {
        if (this.isCached(key, applicationName, serviceTypeCode, from, to) === false) {
            this.previousObservable  = this.http.post(this.url, this.makeBodyData(key, serverMapData), this.makeRequestOptionsArgs(applicationName, serviceTypeCode, from, to)).pipe(
                shareReplay(1)
            );
            this.previousFrom = from || this.newUrlStateNotificationService.getStartTimeToNumber();
            this.previousTo = to || this.newUrlStateNotificationService.getEndTimeToNumber();
            this.previousName = applicationName;
            this.previousCode = serviceTypeCode;
            this.previousKey = key;
        }
        return this.previousObservable;
    }
    isCached(key: string, applicationName: string, serviceTypeCode: string, from?: number, to?: number): boolean {
        return this.previousCode === serviceTypeCode &&
            this.previousKey === key &&
            this.previousName === applicationName &&
            this.previousFrom === (from || this.newUrlStateNotificationService.getStartTimeToNumber()) &&
            this.previousTo === (to || this.newUrlStateNotificationService.getEndTimeToNumber());
    }
    private makeRequestOptionsArgs(applicationName: string, serviceTypeCode: string, from?: number, to?: number): object {
        return {
            params: {
                applicationName: applicationName,
                serviceTypeCode: serviceTypeCode,
                from: from || this.newUrlStateNotificationService.getStartTimeToNumber(),
                to: to || this.newUrlStateNotificationService.getEndTimeToNumber()
            }
        };
    }
    private makeBodyData(nodeKey: string, serverMapData: any): any {
        const linkedNodeData: { [key: string]: any } = {
            from: [],
            to: []
        };
        serverMapData.linkList.forEach((link: ILinkInfo) => {
            if ( link.from === nodeKey ) {
                if ( link.targetInfo instanceof Array ) {
                    link.targetInfo.forEach((targetLinkInfo: ILinkInfo) => {
                        linkedNodeData.to.push([targetLinkInfo.targetInfo.applicationName, targetLinkInfo.targetInfo.serviceTypeCode]);
                    });
                } else {
                    linkedNodeData.to.push([link.targetInfo.applicationName, link.targetInfo.serviceTypeCode]);
                }
            } else if ( link.to === nodeKey ) {
                linkedNodeData.from.push([link.sourceInfo.applicationName, link.sourceInfo.serviceTypeCode]);
            }
        });
        return linkedNodeData;
    }
    makeChartDataForResponseSummary(histogramData: IResponseTime | IResponseMilliSecondTime, yMax?: number): any {
        let newData: {
            keys: string[],
            values: number[],
            max?: number
        };
        if (histogramData) {
            newData = {
                keys: Object.keys(histogramData),
                values: []
            };
            newData.keys.forEach((key: string, index: number) => {
                newData['values'][index] = histogramData[key];
            });
        } else {
            return newData;
        }
        if (yMax) {
            newData['max'] = yMax;
        }
        return newData;
    }
    makeChartDataForLoad(histogramData: IHistogram[], timezone: string, dateFormat: string[], yMax?: number): any {
        let newData: {
            keyValues: {
                key: string;
                values: number[];
            }[];
            labels: string[];
            max?: number;
        };
        if (histogramData) {
            newData = {
                labels: [],
                keyValues: []
            };
            histogramData.forEach((histogram: IHistogram, index: number) => {
                newData.keyValues[index] = {
                    key: histogram.key,
                    values: []
                };
                histogram.values.forEach((aValue: number[]) => {
                    newData.keyValues[index].values.push(aValue[1]);
                    if (index === 0) {
                        newData.labels.push(moment(aValue[0]).tz(timezone).format(dateFormat[0]) + '#' + moment(aValue[0]).tz(timezone).format(dateFormat[1]));
                    }
                });
            });
        } else {
            return newData;
        }
        if (yMax) {
            newData['max'] = yMax;
        }
        return newData;
    }
}
