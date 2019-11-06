
import { NgModule } from '@angular/core';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { SharedModule } from 'app/shared';
import { UserGroupComponent } from './user-group.component';
import { UserGroupCreateAndUpdateComponent } from './user-group-create-and-update.component';
import { UserGroupContainerComponent } from './user-group-container.component';
import { UserGroupDataService } from './user-group-data.service';

@NgModule({
    declarations: [
        UserGroupComponent,
        UserGroupCreateAndUpdateComponent,
        UserGroupContainerComponent
    ],
    imports: [
        FormsModule,
        ReactiveFormsModule,
        SharedModule
    ],
    exports: [
        UserGroupContainerComponent
    ],
    entryComponents: [
        UserGroupComponent,
        UserGroupContainerComponent
    ],
    providers: [
        UserGroupDataService
    ]
})
export class UserGroupModule { }
