import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';

import { WebAppSettingDataService, GutterEventService } from 'app/shared/services';

@Component({
    selector: 'pp-transaction-list-page',
    templateUrl: './transaction-list-page.component.html',
    styleUrls: ['./transaction-list-page.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class TransactionListPageComponent implements OnInit {
    splitSize: number[];

    constructor(
        private webAppSettingDataService: WebAppSettingDataService,
        private gutterEventService: GutterEventService,
    ) {}

    ngOnInit() {
        this.splitSize = this.webAppSettingDataService.getSplitSize();
    }

    onGutterResized({sizes}: {sizes: number[]}): void {
        this.webAppSettingDataService.setSplitSize(sizes.map((size: number): number => {
            return Number.parseFloat(size.toFixed(2));
        }));
        this.gutterEventService.resizedGutter(sizes);
    }
}
