import {Component, OnInit, Input} from '@angular/core';

@Component({
    selector: 'pp-transaction-timeline-v2',
    templateUrl: './transaction-timeline-v2.component.html',
    styleUrls: ['./transaction-timeline-v2.component.css']
})

export class TransactionTimelineV2Component implements OnInit {
    @Input() traceViewerDataURL: string;

    constructor() {}
    ngOnInit() {
        const script = document.createElement('script');
        // ref: web/src/main/angular/angular.json
        script.src = "../../../assets/perfetto-ui/frontend_bundle.js";
        document.getElementById('timeline_main').append(script);
    }

    public getTraceViewerDataURL() {
        return this.traceViewerDataURL;
    }
}
