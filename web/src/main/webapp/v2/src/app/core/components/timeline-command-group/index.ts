
import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { TimelineCommandGroupComponent } from './timeline-command-group.component';
import { TimelineCommandGroupContainerComponent } from './timeline-command-group-container.component';

@NgModule({
    declarations: [
        TimelineCommandGroupComponent,
        TimelineCommandGroupContainerComponent
    ],
    imports: [
        SharedModule
    ],
    exports: [
        TimelineCommandGroupContainerComponent
    ],
    providers: [

    ]
})
export class TimelineCommandGroupModule { }
