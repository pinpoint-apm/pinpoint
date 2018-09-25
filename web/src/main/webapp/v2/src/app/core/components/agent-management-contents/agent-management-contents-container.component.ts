import { Component, OnInit } from '@angular/core';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { StoreHelperService } from 'app/shared/services';
import { Actions } from 'app/shared/store';
import { ApplicationListDataService } from 'app/core/components/application-list/application-list-data.service';

@Component({
    selector: 'pp-agent-management-contents-container',
    templateUrl: './agent-management-contents-container.component.html',
    styleUrls: ['./agent-management-contents-container.component.css']
})
export class AgentManagementContentsContainerComponent implements OnInit {
    private unsubscribe: Subject<void> = new Subject();
    constructor(
        private storeHelperService: StoreHelperService,
        private applicationListDataService: ApplicationListDataService
    ) {

    }
    ngOnInit() {
        this.applicationListDataService.getApplicationList().pipe(
            takeUntil(this.unsubscribe)
        ).subscribe((applicationList: IApplication[]) => {
            this.storeHelperService.dispatch(new Actions.UpdateApplicationList(applicationList));
        });
    }
}
