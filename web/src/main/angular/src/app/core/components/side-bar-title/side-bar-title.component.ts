import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

@Component({
    selector: 'pp-side-bar-title',
    templateUrl: './side-bar-title.component.html',
    styleUrls: ['./side-bar-title.component.css']
})
export class SideBarTitleComponent implements OnInit {
    @Input() originalTargetSelected: boolean;
    @Input() selectedAgent: string;
    @Input() isWAS: boolean;
    @Input() isNode: boolean;
    @Input() fromAppData: any;
    @Input() toAppData: any;
    @Input() funcImagePath: Function;
    @Output() outChangeAgent = new EventEmitter<string>();

    constructor() {}
    ngOnInit() {}
    getIconPath(serviceType: string): string {
        return this.funcImagePath(serviceType);
    }

    onSelectionChange(agent: string): void {
        this.outChangeAgent.emit(agent);
    }

    showAgentList(): boolean {
        return this.originalTargetSelected
            ? this.toAppData ? this.toAppData.agentList.length > 1 : false
            : false;
    }

    onLoadError(img: HTMLImageElement): void {
        img.src = this.funcImagePath('NO_IMAGE_FOUND');
    }
}
