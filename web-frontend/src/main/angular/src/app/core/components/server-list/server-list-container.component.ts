import {
    Component,
    OnInit,
    Output,
    EventEmitter,
    Input,
    ChangeDetectionStrategy,
    OnDestroy,
    ChangeDetectorRef
} from '@angular/core';
import {iif, merge, of, Subject} from 'rxjs';
import {switchMap, takeUntil, tap, filter} from 'rxjs/operators';

import {
    MessageQueueService,
    MESSAGE_TO,
    NewUrlStateNotificationService,
    WebAppSettingDataService
} from 'app/shared/services';
import {
    ServerAndAgentListDataService
} from 'app/core/components/server-and-agent-list/server-and-agent-list-data.service';
import {UrlPathId} from 'app/shared/models';
import {isEmpty} from 'app/core/utils/util';

@Component({
    selector: 'pp-server-list-container',
    templateUrl: './server-list-container.component.html',
    styleUrls: ['./server-list-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ServerListContainerComponent implements OnInit, OnDestroy {
    @Input()
    set data({agentHistogram, app, isWas}: any) {
        if (agentHistogram) {
            const urlService = this.newUrlStateNotificationService;
            const range = [urlService.getStartTimeToNumber(), urlService.getEndTimeToNumber()];

            iif(() => isEmpty(this.serverList),
                of(null).pipe(
                    tap(() => this.showLoading = true),
                    switchMap(() => {
                        return this.serverAndAgentListDataService.getData(app, range).pipe(
                            tap((data: IServerAndAgentDataV2[]) => {
                                this.cachedData = data;
                                this.showLoading = false;
                            })
                        )
                    })
                ),
                of(this.cachedData)
            ).subscribe((data: IServerAndAgentDataV2[]) => {
                this.serverList = data;
                this.agentData = agentHistogram;
                this.isWas = isWas;
                this.cd.detectChanges();
            });
        }
    }

    @Input() selectedAgent: string;
    @Output() outSelectAgent = new EventEmitter<string>();
    @Output() outOpenInspector = new EventEmitter<string>();

    private unsubscribe = new Subject<void>();

    showLoading: boolean;
    serverList: IServerAndAgentDataV2[];
    agentData = {};
    isWas: boolean;
    funcImagePath: Function;

    private cachedData: IServerAndAgentDataV2[];

    constructor(
        private webAppSettingDataService: WebAppSettingDataService,
        private serverAndAgentListDataService: ServerAndAgentListDataService,
        private newUrlStateNotificationService: NewUrlStateNotificationService,
        private messageQueueService: MessageQueueService,
        private cd: ChangeDetectorRef,
    ) {
    }

    ngOnInit() {
        this.funcImagePath = this.webAppSettingDataService.getImagePathMakeFunc();
        merge(
            this.messageQueueService.receiveMessage(this.unsubscribe, MESSAGE_TO.SERVER_MAP_TARGET_SELECT),
            this.newUrlStateNotificationService.onUrlStateChange$.pipe(
                takeUntil(this.unsubscribe),
                filter((urlService: NewUrlStateNotificationService) => {
                    const isAppChanged = urlService.isValueChanged(UrlPathId.APPLICATION);
                    const isPeriodChanged = urlService.isValueChanged(UrlPathId.PERIOD) || urlService.isValueChanged(UrlPathId.END_TIME);

                    return isAppChanged || isPeriodChanged;
                })
            )
        ).subscribe(() => {
            this.serverList = null;
            this.cd.detectChanges();
        })
    }

    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }

    onSelectAgent(agent: string) {
        this.selectedAgent = agent;
        this.outSelectAgent.emit(agent);
    }

    onOpenInspector(agentName: string) {
        this.outOpenInspector.emit(agentName);
    }
}
