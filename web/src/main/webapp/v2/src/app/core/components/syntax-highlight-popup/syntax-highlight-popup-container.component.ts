import { Component, OnInit, Input, Output, EventEmitter, AfterViewInit } from '@angular/core';
import { Observable, iif, of } from 'rxjs';
import { map } from 'rxjs/operators';

import { DynamicPopup } from 'app/shared/services';
import { SyntaxHighlightDataService } from './syntax-highlight-data.service';

@Component({
    selector: 'pp-syntax-highlight-popup-container',
    templateUrl: './syntax-highlight-popup-container.component.html',
    styleUrls: ['./syntax-highlight-popup-container.component.css'],
})
export class SyntaxHighlightPopupContainerComponent implements OnInit, AfterViewInit, DynamicPopup {
    @Input() data: ISyntaxHighlightData;
    @Output() outClose = new EventEmitter<void>();
    @Output() outCreated = new EventEmitter<ICoordinate>();

    data$: Observable<ISyntaxHighlightData>;

    constructor(
        private syntaxHighlightDataService: SyntaxHighlightDataService
    ) {}

    ngOnInit() {
        this.data$ = iif(() => !!this.data.bindValue,
            this.syntaxHighlightDataService.getData(this.data).pipe(
                map((bindedContents: string) => {
                    return { ...this.data, bindedContents };
                })
            ),
            of(this.data)
        );
    }

    ngAfterViewInit() {
        this.outCreated.emit({ coordX: 0, coordY: 0 });
    }

    onClosePopup(): void {
        this.outClose.emit();
    }
}
