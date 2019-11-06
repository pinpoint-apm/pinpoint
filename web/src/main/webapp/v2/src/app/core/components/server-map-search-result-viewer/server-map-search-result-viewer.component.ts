import { Component, Input, Output, EventEmitter, OnInit, OnChanges, SimpleChanges } from '@angular/core';

@Component({
    selector: 'pp-server-map-search-result-viewer',
    templateUrl: './server-map-search-result-viewer.component.html',
    styleUrls: ['./server-map-search-result-viewer.component.css']
})
export class ServerMapSearchResultViewerComponent implements OnInit, OnChanges {
    static I18NTEXT = {
        PLACE_HOLDER: 'PLACE_HOLDER',
        EMPTY_RESULT: 'EMPTY_RESULT'
    };
    selectedApplication: IApplication;
    hiddenList = true;
    listCountZero = false;
    @Input() i18nText = {
        [ServerMapSearchResultViewerComponent.I18NTEXT.PLACE_HOLDER]: 'Favorite List'
    };
    @Input() hiddenComponent = true;
    @Input() applicationResultList: IApplication[];
    @Output() outSearch: EventEmitter<string> = new EventEmitter();
    @Output() outSelectApplication: EventEmitter<IApplication> = new EventEmitter();
    constructor() {}
    ngOnChanges(changes: SimpleChanges) {
        if (changes['applicationResultList']) {
            if (changes['applicationResultList'].firstChange) {
                this.hiddenList = true;
            } else {
                this.hiddenList = false;
                this.listCountZero = this.applicationResultList.length === 0;
            }
        }
    }
    ngOnInit() {}
    onSearch(query: string): void {
        if (query !== '') {
            this.outSearch.emit(query);
        }
    }
    onCloseResult() {
        this.hiddenList = true;
    }
    onSelectApplication(application: IApplication): void {
        this.selectedApplication = application;
        this.outSelectApplication.emit(application);
    }
}
