import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

@Component({
    selector: 'pp-server-error-message',
    templateUrl: './server-error-message.component.html',
    styleUrls: ['./server-error-message.component.css']
})
export class ServerErrorMessageComponent implements OnInit {
    @Input() message: string;
    @Output() outClose = new EventEmitter<void>();

    constructor() {}
    ngOnInit() {}
    onCloseMessage(): void {
        this.outClose.emit();
    }
}
