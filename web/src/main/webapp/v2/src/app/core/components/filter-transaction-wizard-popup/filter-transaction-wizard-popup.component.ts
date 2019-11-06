import { Component, OnInit, Input, Output, EventEmitter, HostBinding } from '@angular/core';
import { NouiFormatter } from 'ng2-nouislider';

import { Filter } from 'app/core/models/';

export class TimeFormatter implements NouiFormatter {
    to(value: number): string {
        return value.toLocaleString() + (value === 30000 ? '+' : '') + ' ms';
    }

    from(value: string): number {
        return parseInt(value.replace(/(.) ms/, '$1').replace(/,/, ''), 10);
    }
}
enum RESULT_TYPE {
    SUCCESS_AND_FAIL,
    SUCCESS_ONLY,
    FAIL_ONLY
}
const AGENT_ALL = 'All';

@Component({
    selector: 'pp-filter-transaction-wizard-popup',
    templateUrl: './filter-transaction-wizard-popup.component.html',
    styleUrls: ['./filter-transaction-wizard-popup.component.css']
})
export class FilterTransactionWizardPopupComponent implements OnInit {
    @Input() filterInfo: Filter;
    @Input() link: ILinkInfo;
    @Input() funcImagePath: Function;
    @Output() outRequestFilterOpen = new EventEmitter<any>();
    @Output() outClosePopup = new EventEmitter<void>();
    @HostBinding('class.font-opensans') fontFamily = true;

    resultType: string[] = ['Success + Failed', 'Success Only', 'Failed Only'];
    selectedResultType: RESULT_TYPE = RESULT_TYPE.SUCCESS_AND_FAIL;
    selectedFromAgent = AGENT_ALL;
    selectedToAgent = AGENT_ALL;
    urlPattern = '';
    responseTimeMin = 0;
    responseTimeMax = 30000;
    responseTimeRange = [0, 30000];

    constructor() {}
    ngOnInit() {
        this.resetValue();
    }
    resetValue() {
        if (this.filterInfo) {
            this.selectedFromAgent = this.filterInfo.fromAgentName || AGENT_ALL;
            this.selectedToAgent = this.filterInfo.toAgentName || AGENT_ALL;
            this.responseTimeRange = [this.filterInfo.responseFrom, this.filterInfo.responseTo];
            this.urlPattern = this.filterInfo.urlPattern || '';
            if (this.filterInfo.transactionResult === true) {
                this.selectedResultType = RESULT_TYPE.SUCCESS_ONLY;
            } else if (this.filterInfo.transactionResult === false) {
                this.selectedResultType = RESULT_TYPE.FAIL_ONLY;
            } else {
                this.selectedResultType = RESULT_TYPE.SUCCESS_AND_FAIL;
            }
        }
    }
    onClickFilter(): void {
        this.outRequestFilterOpen.emit({
            filterApplicationName: this.link.filterApplicationName,
            filterApplicationServiceTypeName: this.link.filterApplicationServiceTypeName,
            from: {
                applicationName: this.link.sourceInfo.applicationName,
                serviceType: this.link.sourceInfo.serviceType,
                agent: this.selectedFromAgent === AGENT_ALL ? null : this.selectedFromAgent,
                isWas: this.link.sourceInfo.isWas
            },
            to: {
                applicationName: this.link.targetInfo.applicationName,
                serviceType: this.link.targetInfo.serviceType,
                agent: this.selectedToAgent === AGENT_ALL ? null : this.selectedToAgent,
                isWas: this.link.targetInfo.isWas
            },
            urlPattern: this.urlPattern,
            responseFrom: this.responseTimeRange[0],
            responseTo: this.responseTimeRange[1],
            transactionResult: this.selectedResultType === 0 ? null : this.selectedResultType === 1 ? true : false,
            filterTargetRpcList : this.link.sourceInfo.isWas && this.link.targetInfo.isWas ? this.link.filterTargetRpcList : []
        });
        this.onClickClose();
    }
    onClickClose(): void {
        // this.selectedResultType = RESULT_TYPE.SUCCESS_AND_FAIL;
        // this.selectedFromAgent = AGENT_ALL;
        // this.selectedToAgent = AGENT_ALL;
        // this.responseTimeRange = [this.responseTimeMin, this.responseTimeMax];
        // this.urlPattern = '';

        this.outClosePopup.emit();
    }
    onSelectResultType(type: number): void {
        this.selectedResultType = type;
    }
    onSelectFromAgent(agent: string): void {
        this.selectedFromAgent = agent;
    }
    onSelectToAgent(agent: string): void {
        this.selectedToAgent = agent;
    }
    getIconFullPath(applicationName: string): string {
        return this.funcImagePath(applicationName);
    }
    isSelectedResultType(type: number): boolean {
        return this.selectedResultType === type;
    }
    isSelectedFromAgent(agent: string): boolean {
        return this.selectedFromAgent === agent;
    }
    isSelectedToAgent(agent: string): boolean {
        return this.selectedToAgent === agent;
    }
    getTimeFormatter(): NouiFormatter {
        return <NouiFormatter>new TimeFormatter();
    }
}
