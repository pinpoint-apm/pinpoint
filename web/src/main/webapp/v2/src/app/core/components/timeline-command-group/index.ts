
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TimelineCommandGroupComponent } from './timeline-command-group.component';
import { TimelineCommandGroupContainerComponent } from './timeline-command-group-container.component';

@NgModule({
    declarations: [
        TimelineCommandGroupComponent,
        TimelineCommandGroupContainerComponent
    ],
    imports: [
        CommonModule
    ],
    exports: [
        TimelineCommandGroupContainerComponent
    ],
    providers: [

    ]
})
export class TimelineCommandGroupModule { }
