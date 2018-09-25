import { Component, OnInit, Input } from '@angular/core';
import { TimelineInteractionService } from 'app/core/components/timeline/timeline-interaction.service';

@Component({
    selector: 'pp-timeline-command-group',
    templateUrl: './timeline-command-group.component.html',
    styleUrls: ['./timeline-command-group.component.css'],
})
export class TimelineCommandGroupComponent implements OnInit {
    @Input() pointingTime: string;
    constructor(private timelineInteractionService: TimelineInteractionService) {}
    ngOnInit() {}
    onClickZoomIn(): void {
        this.timelineInteractionService.setZoomIn();
    }
    onClickZoomOut(): void {
        this.timelineInteractionService.setZoomOut();
    }
    onClickPrev(): void {
        this.timelineInteractionService.setPrev();
    }
    onClickNext(): void {
        this.timelineInteractionService.setNext();
    }
    onClickNow(): void {
        this.timelineInteractionService.setNow();
    }
}
