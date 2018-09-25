
import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared';
import { ServerMapOptionsComponent } from './server-map-options.component';
import { ServerMapOptionsContainerComponent } from './server-map-options-container.component';

@NgModule({
    declarations: [
        ServerMapOptionsComponent,
        ServerMapOptionsContainerComponent
    ],
    imports: [
        SharedModule
    ],
    exports: [
        ServerMapOptionsContainerComponent
    ],
    providers: []
})
export class ServerMapOptionsModule { }
