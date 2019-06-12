import { Component, OnInit } from '@angular/core';

import { WebAppSettingDataService, GutterEventService } from 'app/shared/services';

@Component({
    selector: 'pp-transaction-list-page',
    templateUrl: './transaction-list-page.component.html',
    styleUrls: ['./transaction-list-page.component.css']
})
export class TransactionListPageComponent implements OnInit {
    direction = 'vertical';
    handlePosition: number[];

    constructor(
        private webAppSettingDataService: WebAppSettingDataService,
        private gutterEventService: GutterEventService,
    ) {}

    ngOnInit() {
        this.handlePosition = this.webAppSettingDataService.getListHandlePosition();
    }

    onGutterResized({sizes}: {sizes: number[]}): void {
        this.webAppSettingDataService.setListHandlePosition(sizes.map((size: number): number => {
            return Number.parseFloat(size.toFixed(2));
        }));
        this.gutterEventService.resizedGutter(sizes);
    }
}
