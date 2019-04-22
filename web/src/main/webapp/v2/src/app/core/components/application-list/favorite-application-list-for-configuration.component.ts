import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

@Component({
    selector: 'pp-favorite-application-list-for-configuration',
    templateUrl: './favorite-application-list-for-configuration.component.html',
    styleUrls: ['./favorite-application-list-for-configuration.component.css']
})
export class FavoriteApplicationListForConfigurationComponent implements OnInit {
    @Input() applicationList: IApplication[];
    @Input() emptyText: string;
    @Input() funcImagePath: Function;
    @Input() iconBtnClassName: string;
    @Output() outSelectApp = new EventEmitter<IApplication>();

    constructor() {}
    ngOnInit() {}
    getIconPath(serviceType: string): string {
        return this.funcImagePath(serviceType);
    }
    onSelectApp(app: IApplication): void {
        this.outSelectApp.emit(app);
    }
    isEmpty(): boolean {
        return this.applicationList && this.applicationList.length === 0;
    }
}
