import { Component, OnInit, Input, Output, EventEmitter, AfterViewInit } from '@angular/core';

import { DynamicPopup } from 'app/shared/services';

@Component({
    selector: 'pp-message-popup-container',
    templateUrl: './message-popup-container.component.html',
    styleUrls: ['./message-popup-container.component.css'],
})
export class MessagePopupContainerComponent implements OnInit, AfterViewInit, DynamicPopup {
    @Input() data: ITransactionMessage;
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
