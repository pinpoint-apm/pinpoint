import { Component, OnInit, OnDestroy, Output, EventEmitter, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { Subject } from 'rxjs';

import { WebAppSettingDataService, StoreHelperService } from 'app/shared/services';

@Component({
    selector: 'pp-server-list-container',
    templateUrl: './server-list-container.component.html',
    styleUrls: ['./server-list-container.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class ServerListContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<void> = new Subject();
    @Output() outSelectAgent: EventEmitter<string> = new EventEmitter();
    @Output() outOpenInspector: EventEmitter<string> = new EventEmitter();
    serverList: any = {};
    agentData: any = {};
    isWas: boolean;
    funcImagePath: Function;
    constructor(
        private changeDetectorRef: ChangeDetectorRef,
        private storeHelperService: StoreHelperService,
        private webAppSettingDataService: WebAppSettingDataService,
    ) {
        this.funcImagePath = this.webAppSettingDataService.getImagePathMakeFunc();
    }
    ngOnInit() {
        this.storeHelperService.getServerListData(this.unsubscribe).subscribe((agentData: any) => {
            if ( agentData && agentData['serverList'] ) {
                this.serverList = agentData['serverList'];
                this.agentData = agentData['agentHistogram'];
                this.isWas = agentData['isWas'];
                this.changeDetectorRef.detectChanges();
            }
        });
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
    onSelectAgent(agentName: string) {
        this.outSelectAgent.emit(agentName);
    }
    onOpenInspector(agentName: string) {
        this.outOpenInspector.emit(agentName);
    }
}
