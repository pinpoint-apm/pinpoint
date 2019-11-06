import { Component, OnInit, Input } from '@angular/core';

@Component({
    selector: 'pp-application-name-issue-popup',
    templateUrl: './application-name-issue-popup.component.html',
    styleUrls: ['./application-name-issue-popup.component.css']
})
export class ApplicationNameIssuePopupComponent implements OnInit {
    @Input() data: {[key: string]: any};

    constructor() {}
    ngOnInit() {}
}
