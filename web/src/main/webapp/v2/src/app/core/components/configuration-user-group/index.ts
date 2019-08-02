import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { PinpointUserModule } from 'app/core/components/pinpoint-user';
import { GroupMemberModule } from 'app/core/components/group-member';
import { UserGroupModule } from 'app/core/components/user-group';
import { ConfigurationUserGroupContainerComponent } from './configuration-user-group-container.component';

@NgModule({
    declarations: [
        ConfigurationUserGroupContainerComponent
    ],
    imports: [
        SharedModule,
        UserGroupModule,
        GroupMemberModule,
        PinpointUserModule,
    ],
    exports: [
        ConfigurationUserGroupContainerComponent
    ],
    providers: []
})
export class ConfigurationUserGroupModule { }
