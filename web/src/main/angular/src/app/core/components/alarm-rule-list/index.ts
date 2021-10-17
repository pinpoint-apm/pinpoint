import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { SharedModule } from 'app/shared';
import { AlarmRuleListComponent } from './alarm-rule-list.component';
import { AlarmRuleListContainerComponent } from './alarm-rule-list-container.component';
import { AlarmRuleWebhookListComponent } from './alarm-rule-webhook-list.component';
import { AlarmRuleCreateAndUpdateComponent } from './alarm-rule-create-and-update.component';
import { AlarmRuleDataService } from './alarm-rule-data.service';
import { HelpViewerPopupModule } from 'app/core/components/help-viewer-popup';

@NgModule({
    declarations: [
        AlarmRuleListComponent,
        AlarmRuleListContainerComponent,
        AlarmRuleCreateAndUpdateComponent,
        AlarmRuleWebhookListComponent,
    ],
    imports: [
        FormsModule,
        ReactiveFormsModule,
        SharedModule,
        HelpViewerPopupModule
    ],
    exports: [
        AlarmRuleListContainerComponent,
        AlarmRuleCreateAndUpdateComponent
    ],
    providers: [
        AlarmRuleDataService
    ]
})
export class AlarmRuleListModule { }
