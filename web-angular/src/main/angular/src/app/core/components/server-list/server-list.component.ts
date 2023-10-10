import {Component, OnInit, Input, Output, EventEmitter, Renderer2} from '@angular/core';
import {from} from 'rxjs';

const enum ListStyle {
    RADIO_BTN_WIDTH = 23
}

@Component({
    selector: 'pp-server-list',
    templateUrl: './server-list.component.html',
    styleUrls: ['./server-list.component.css'],
})
export class ServerListComponent implements OnInit {
    @Input() serverList: IServerAndAgentDataV2[];
    @Input() agentData: any = {};
    @Input() isWas: boolean;
    @Input() selectedAgent: string;
    @Input() funcImagePath: Function;
    @Output() outSelectAgent = new EventEmitter<string>();
    @Output() outOpenInspector = new EventEmitter<string>();

    constructor(
        private renderer: Renderer2
    ) {
    }

    ngOnInit() {
    }

    getAgentLabel({agentId, agentName}: IAgentDataV2): string {
        return `${agentId} (${this.getAgentName({agentName} as IAgentDataV2)})`;
    }

    getAgentName({agentName}: IAgentDataV2): string {
        return agentName ? agentName : 'N/A';
    }

    hasError({agentId}: IAgentDataV2): boolean {
        return this.agentData[agentId] && this.agentData[agentId]['Error'] > 0;
    }

    getAlertImgPath(): string {
        return this.funcImagePath('icon-alert');
    }

    onSelectAgent({agentId}: IAgentDataV2) {
        if (this.selectedAgent === agentId) {
            return;
        }

        this.outSelectAgent.emit(agentId);
    }

    onOpenInspector({agentId}: IAgentDataV2): void {
        this.outOpenInspector.emit(agentId);
    }

    isSelectedAgent({agentId}: IAgentDataV2): boolean {
        return this.selectedAgent === agentId;
    }

    getLabelMaxWidth(btnWrapper: HTMLElement): string {
        return `calc(100% - ${btnWrapper.offsetWidth}px)`;
    }

    getLeftPosition(labelWrapperElement: HTMLElement, labelElement: HTMLElement): string {
        const labelWrapperWidth = labelWrapperElement.offsetWidth;
        const labelWidth = labelElement.offsetWidth;

        return labelWidth + ListStyle.RADIO_BTN_WIDTH >= labelWrapperWidth
            ? `${labelWrapperWidth - 25}px`
            : `${ListStyle.RADIO_BTN_WIDTH + labelWidth - 4}px`;
    }

    onClickCopy(event: Event, agentLabel: string): void {
        event.stopPropagation();
        from(navigator.clipboard.writeText(agentLabel)).subscribe(() => {
            const copyBtnElement = event.target as HTMLElement;
            const wrapperElement = copyBtnElement.parentElement;
            const pElement = this.renderer.createElement('p');
            const copiedTextElement = this.renderer.createText('Copied');

            this.renderer.setStyle(copyBtnElement, 'display', 'none');
            this.renderer.setStyle(pElement, 'font-size', '10px');
            this.renderer.appendChild(pElement, copiedTextElement);
            this.renderer.appendChild(wrapperElement, pElement);

            setTimeout(() => {
                this.renderer.removeChild(wrapperElement, pElement);
                this.renderer.setStyle(copyBtnElement, 'display', 'inline-block');
            }, 1500);
        });
    }
}
