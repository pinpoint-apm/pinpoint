import { Component, OnInit, AfterViewInit, Input, Output, EventEmitter } from '@angular/core';

@Component({
    selector: 'pp-server-error-popup-container',
    templateUrl: './server-error-popup-container.component.html',
    styleUrls: ['./server-error-popup-container.component.css']
})
export class ServerErrorPopupContainerComponent implements OnInit, AfterViewInit {
    @Input() data: any;
    @Output() outClose = new EventEmitter<void>();
    @Output() outCreated = new EventEmitter<ICoordinate>();

    constructor() {}
    ngOnInit() {}
    ngAfterViewInit() {
        this.outCreated.emit({ coordX: 0, coordY: 0 });
    }

    onClosePopup() {
        this.outClose.emit();
    }
}
