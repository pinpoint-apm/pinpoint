import { Component, OnInit, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';

@Component({
    selector: 'pp-favorite-application-list-for-configuration',
    templateUrl: './favorite-application-list-for-configuration.component.html',
    styleUrls: ['./favorite-application-list-for-configuration.component.css']
})
export class FavoriteApplicationListForConfigurationComponent implements OnInit, OnChanges {
    @Input() applicationList: IApplication[];
    @Input() emptyText: string;
    @Input() funcImagePath: Function;
    @Input() iconBtnClassName: string;
    @Output() outSelectApp = new EventEmitter<IApplication>();

    isEmpty: boolean;

    constructor() {}
    ngOnInit() {}
    ngOnChanges(changes: SimpleChanges) {
        const {applicationList} = changes;

        if (applicationList) {
            this.isEmpty = (applicationList.currentValue as IApplication[]).length === 0;
        }
    }

    getIconPath(serviceType: string): string {
        return this.funcImagePath(serviceType);
    }

    onSelectApp(app: IApplication): void {
        this.outSelectApp.emit(app);
    }
}
