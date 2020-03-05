
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { SharedModule } from 'app/shared';
import { AlarmRuleListComponent } from './alarm-rule-list.component';
import { AlarmRuleListContainerComponent } from './alarm-rule-list-container.component';
import { AlarmRuleCreateAndUpdateComponent } from './alarm-rule-create-and-update.component';
import { AlarmRuleDataService } from './alarm-rule-data.service';

@NgModule({
    declarations: [
        AlarmRuleListComponent,
        AlarmRuleListContainerComponent,
        AlarmRuleCreateAndUpdateComponent
    ],
    imports: [
        FormsModule,
        ReactiveFormsModule,
        SharedModule
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
