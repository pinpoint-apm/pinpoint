import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

@Component({
    selector: 'pp-server-error-popup',
    templateUrl: './server-error-popup.component.html',
    styleUrls: ['./server-error-popup.component.css']
})
export class ServerErrorPopupComponent implements OnInit {
    @Input() data: any;
    @Output() outClosePopup = new EventEmitter<void>();
    errorInfo: IServerErrorFormat;
    showHeader = false;
    showParam = false;
    showStackTrace = false;
    constructor() {}
    ngOnInit() {
        this.errorInfo = this.data.contents;
    }
    onClose(): void {
        this.outClosePopup.emit();
    }
    getState(state: boolean): string {
        return state ? 'fa-angle-up' : 'fa-angle-down';
    }
}
