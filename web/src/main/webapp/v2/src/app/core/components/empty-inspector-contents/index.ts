
import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared';
import { EmptyInspectorContentsContainerComponent } from './empty-inspector-contents-container.component';

@NgModule({
    declarations: [
        EmptyInspectorContentsContainerComponent
    ],
    imports: [
        SharedModule
    ],
    exports: [
        EmptyInspectorContentsContainerComponent
    ],
    providers: []
})
export class EmptyInspectorContentsModule { }
