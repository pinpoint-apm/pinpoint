
import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared';
import { ApplicationInspectorTitleContainerComponent } from './application-inspector-title-container.component';

@NgModule({
    declarations: [
        ApplicationInspectorTitleContainerComponent
    ],
    imports: [
        SharedModule
    ],
    exports: [
        ApplicationInspectorTitleContainerComponent
    ],
    providers: []
})
export class ApplicationInspectorTitleModule { }
