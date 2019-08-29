
import { NgModule } from '@angular/core';
import { MatTooltipModule } from '@angular/material/tooltip';
import { SharedModule } from 'app/shared';
import { ApplicationInspectorTitleContainerComponent } from './application-inspector-title-container.component';

@NgModule({
    declarations: [
        ApplicationInspectorTitleContainerComponent
    ],
    imports: [
        MatTooltipModule,
        SharedModule
    ],
    exports: [
        ApplicationInspectorTitleContainerComponent
    ],
    providers: []
})
export class ApplicationInspectorTitleModule { }
