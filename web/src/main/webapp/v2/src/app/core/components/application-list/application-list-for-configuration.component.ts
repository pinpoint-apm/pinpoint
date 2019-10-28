import { Component, OnInit, Input, Output, EventEmitter, SimpleChanges, OnChanges } from '@angular/core';

@Component({
    selector: 'pp-application-list-for-configuration',
    templateUrl: './application-list-for-configuration.component.html',
    styleUrls: ['./application-list-for-configuration.component.css']
})
export class ApplicationListForConfigurationComponent implements OnInit, OnChanges {
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
