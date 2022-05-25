import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { Subject, merge } from 'rxjs';
import { filter, switchMap, tap, map, takeUntil } from 'rxjs/operators';

import { MessageQueueService, MESSAGE_TO, NewUrlStateNotificationService } from 'app/shared/services';
import { ApdexScoreDataService } from './apdex-score-data.service';
import { ServerMapData } from 'app/core/components/server-map/class/server-map-data.class';

@Component({
    selector: 'pp-apdex-score-container',
    templateUrl: './apdex-score-container.component.html',
    styleUrls: ['./apdex-score-container.component.css']
})
export class ApdexScoreContainerComponent implements OnInit, OnDestroy {
    private unsubscribe = new Subject<void>();

    private selectedTarget: ISelectedTarget;
    private serverMapData: ServerMapData;
    private previousRange: number[];

    apdexScore: number;

    constructor(
        private apdexScoreDataService: ApdexScoreDataService,
        private messageQueueService: MessageQueueService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private cd: ChangeDetectorRef,
    ) { }

    ngOnInit() {
        this.newUrlStateNotificationService.onUrlStateChange$.pipe(
            takeUntil((this.unsubscribe)),
        ).subscribe(() => {
            this.serverMapData = null;
            this.selectedTarget = null;
        });

        merge(
            this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.SERVER_MAP_DATA_UPDATE).pipe(
                tap(({serverMapData, range}: {serverMapData: ServerMapData, range: number[]}) => {
                    this.serverMapData = serverMapData;
                    this.previousRange = range;
                }),
                filter(() => !!this.selectedTarget),
            ),
            this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.SERVER_MAP_TARGET_SELECT).pipe(
                filter(({isWAS, isMerged}: ISelectedTarget) => isWAS && !isMerged),
                tap((target: ISelectedTarget) => this.selectedTarget = target),
            )
        ).pipe(
            map(() => this.serverMapData.getNodeData(this.selectedTarget.node[0])),
            switchMap(({applicationName, serviceTypeCode}: INodeInfo) => {
                return this.apdexScoreDataService.getApdexScore({applicationName, serviceTypeCode, from: this.previousRange[0], to: this.previousRange[1]});
            })
        ).subscribe(({apdexScore}: {apdexScore: number}) => {
            this.apdexScore = apdexScore;
            this.cd.detectChanges();
        });
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
}
