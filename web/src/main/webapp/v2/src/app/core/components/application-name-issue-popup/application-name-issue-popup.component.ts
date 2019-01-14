import { Component, OnInit, Input } from '@angular/core';

export const enum POPUP_CONSTANT {
    TOOLTIP_TRIANGLE_HEIGHT = 7, // 툴팁 삼각형 높이
    SPACE_FROM_BUTTON = 10 // 클릭한 버튼에서 살짝 떨어뜨려줄 길이
}

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
