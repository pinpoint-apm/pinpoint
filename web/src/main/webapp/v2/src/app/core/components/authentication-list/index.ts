
import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared';
import { AuthenticationListComponent } from './authentication-list.component';
import { AuthenticationListContainerComponent } from './authentication-list-container.component';


@NgModule({
    declarations: [
        AuthenticationListComponent,
        AuthenticationListContainerComponent,
    ],
    imports: [
        SharedModule
    ],
    exports: [
        AuthenticationListContainerComponent,
    ],
    providers: []
})
export class AuthenticationListModule { }
