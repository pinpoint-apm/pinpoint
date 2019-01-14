import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

@Component({
    selector: 'pp-side-bar-title',
    templateUrl: './side-bar-title.component.html',
    styleUrls: ['./side-bar-title.component.css']
})
export class SideBarTitleComponent implements OnInit {
    @Input() isWAS: boolean;
    @Input() isNode: boolean;
    @Input() fromAppData: any;
    @Input() toAppData: any;
    @Input() funcImagePath: Function;
    @Output() outChangeAgent: EventEmitter<string> = new EventEmitter();
    constructor() {}
    ngOnInit() {}
    getIconPath(serviceType: string): string {
        return this.funcImagePath(serviceType);
    }
    onChangeAgent($agent: string): void {
        this.outChangeAgent.emit($agent);
    }
    showAgentList(): boolean {
        if (this.toAppData) {
            return this.toAppData.agentList.length > 0;
        } else {
            return false;
        }
    }
    onLoadError(img: HTMLImageElement): void {
        img.src = this.funcImagePath('NO_IMAGE_FOUND');
    }
}
