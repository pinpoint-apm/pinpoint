import {Component, Input, OnChanges} from '@angular/core';

@Component({
    selector: 'pp-transaction-timeline-v2',
    templateUrl: './transaction-timeline-v2.component.html',
    styleUrls: ['./transaction-timeline-v2.component.css']
})

export class TransactionTimelineV2Component implements OnChanges {
    @Input() traceViewerDataURL: string;
    isLoaded: boolean = false;

    ngOnChanges() {
        if (!this.isLoaded && this.traceViewerDataURL !== undefined) {
            this.loadPerfetto();
        }
    }

    public loadPerfetto() {
        this.isLoaded = true;
        const script = document.createElement('script');
        // ref: web/src/main/angular/angular.json
        script.src = "../../../assets/perfetto-ui/frontend_bundle.js";
        document.getElementById('timeline_main').append(script);
    }
}
