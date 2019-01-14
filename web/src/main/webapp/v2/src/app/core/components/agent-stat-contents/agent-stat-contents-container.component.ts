import { Component, OnInit } from '@angular/core';

import { StoreHelperService } from 'app/shared/services';
import { Actions } from 'app/shared/store';
import { AgentListDataService } from './agent-list-data.service';

@Component({
    selector: 'pp-agent-stat-contents-container',
    templateUrl: './agent-stat-contents-container.component.html',
    styleUrls: ['./agent-stat-contents-container.component.css']
})
export class AgentStatContentsContainerComponent implements OnInit {
    constructor(
        private storeHelperService: StoreHelperService,
        private agentListDataService: AgentListDataService
    ) {}

    ngOnInit() {
        this.agentListDataService.retrieve().subscribe((agentList: { [key: string]: IAgent[] }) => {
            this.storeHelperService.dispatch(new Actions.UpdateAdminAgentList(agentList));
        });
    }
}
