import { Component, OnInit, Input } from '@angular/core';

@Component({
    selector: 'pp-help-viewer-popup',
    templateUrl: './help-viewer-popup.component.html',
    styleUrls: ['./help-viewer-popup.component.css']
})
export class HelpViewerPopupComponent implements OnInit {
    @Input() data: {[key: string]: any}[];

    constructor() {}
    ngOnInit() {}
}
