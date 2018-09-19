import { Component, OnInit, Output, EventEmitter, AfterViewInit } from '@angular/core';

import { WebAppSettingDataService, DynamicPopup } from 'app/shared/services';

@Component({
    selector: 'pp-configuration-popup-container',
    templateUrl: './configuration-popup-container.component.html',
    styleUrls: ['./configuration-popup-container.component.css']
})
export class ConfigurationPopupContainerComponent implements OnInit, AfterViewInit, DynamicPopup {
    @Output() outClose = new EventEmitter<void>();
    @Output() outCreated = new EventEmitter<ICoordinate>();

    funcImagePath: Function;

    constructor(
        private webAppSettingDataService: WebAppSettingDataService,
    ) {}

    ngOnInit() {
        this.funcImagePath = this.webAppSettingDataService.getIconPathMakeFunc();
    }

    ngAfterViewInit() {
        this.outCreated.emit({ coordX: 0, coordY: 0 });
    }

    onClosePopup(): void {
        this.outClose.emit();
    }
}
