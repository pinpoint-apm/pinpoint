
import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared';
import { GroupMemberComponent } from './group-member.component';
import { GroupMemberContainerComponent } from './group-member-container.component';
import { GroupMemberDataService } from './group-member-data.service';

@NgModule({
    declarations: [
        GroupMemberComponent,
        GroupMemberContainerComponent
    ],
    imports: [
        SharedModule
    ],
    exports: [
        GroupMemberContainerComponent
    ],
    entryComponents: [
        GroupMemberComponent,
        GroupMemberContainerComponent
    ],
    providers: [
        GroupMemberDataService
    ]
})
export class GroupMemberModule { }
