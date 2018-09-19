import { Component, OnInit, OnDestroy } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { StoreHelperService } from 'app/shared/services';
import { Actions } from 'app/shared/store';
import { AgentListDataService } from './agent-list-data.service';

@Component({
    selector: 'pp-agent-stat-contents-container',
    templateUrl: './agent-stat-contents-container.component.html',
    styleUrls: ['./agent-stat-contents-container.component.css']
})
export class AgentStatContentsContainerComponent implements OnInit, OnDestroy {
    private unsubscribe: Subject<void> = new Subject();

    constructor(
        private storeHelperService: StoreHelperService,
        private agentListDataService: AgentListDataService
    ) {}

    ngOnInit() {
        this.agentListDataService.retrieve().pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((agentList: { [key: string]: IAgent[] }) => {
            this.storeHelperService.dispatch(new Actions.UpdateAdminAgentList(agentList));
        });
    }
    ngOnDestroy() {
        this.unsubscribe.next();
        this.unsubscribe.complete();
    }
}
