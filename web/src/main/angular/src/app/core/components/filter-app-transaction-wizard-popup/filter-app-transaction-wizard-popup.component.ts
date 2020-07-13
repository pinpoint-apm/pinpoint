import { Component, OnInit, Input, Output, EventEmitter, HostBinding } from '@angular/core';
import { NouiFormatter } from 'ng2-nouislider';

import { Filter } from 'app/core/models/';
import { TimeFormatter } from "../filter-transaction-wizard-popup/filter-transaction-wizard-popup.component";

enum RESULT_TYPE {
    SUCCESS_AND_FAIL,
    SUCCESS_ONLY,
    FAIL_ONLY
}
const AGENT_ALL = 'All';

@Component({
    selector: 'pp-app-filter-transaction-wizard-popup',
    templateUrl: './filter-app-transaction-wizard-popup.component.html',
    styleUrls: ['./filter-app-transaction-wizard-popup.component.css']
})
export class FilterAppTransactionWizardPopupComponent implements OnInit {
    @Input() filterInfo: Filter;
    @Input() app: INodeInfo;
    @Input() funcImagePath: Function;
    @Output() outRequestFilterOpen = new EventEmitter<any>();
    @Output() outClosePopup = new EventEmitter<void>();
    @HostBinding('class.font-opensans') fontFamily = true;

    resultType: string[] = ['Success + Failed', 'Success Only', 'Failed Only'];
    selectedResultType: RESULT_TYPE = RESULT_TYPE.SUCCESS_AND_FAIL;
    selectedAgent = AGENT_ALL;
    // urlPattern = '';
    responseTimeMin = 0;
    responseTimeMax = 30000;
    responseTimeRange = [0, 30000];

    private _urlPattern: string;

    constructor() {}
    ngOnInit() {
        this.resetValue();
    }
    resetValue() {
        if (this.filterInfo) {
            this.selectedAgent = this.filterInfo.agentName || AGENT_ALL;
            this.responseTimeRange = [this.filterInfo.responseFrom, this.filterInfo.responseTo];
            this.urlPattern = this.filterInfo.urlPattern || '';
            if (this.filterInfo.transactionResult === true) {
                this.selectedResultType = RESULT_TYPE.FAIL_ONLY;
            } else if (this.filterInfo.transactionResult === false) {
                this.selectedResultType = RESULT_TYPE.SUCCESS_ONLY;
            } else {
                this.selectedResultType = RESULT_TYPE.SUCCESS_AND_FAIL;
            }
        }
    }
    onClickFilter(): void {
        this.outRequestFilterOpen.emit({
            filterApplicationName: this.app.applicationName,
            filterApplicationServiceTypeName: this.app.serviceType,
            agent: this.selectedAgent === AGENT_ALL ? null : this.selectedAgent,
            isWas: this.app.isWas,
            urlPattern: this.urlPattern,
            responseFrom: this.responseTimeRange[0],
            responseTo: this.responseTimeRange[1],
            transactionResult: this.selectedResultType === RESULT_TYPE.SUCCESS_AND_FAIL ? null : !(this.selectedResultType === RESULT_TYPE.SUCCESS_ONLY),
            filterTargetRpcList : []
        });
        this.onClickClose();
    }
    onClickClose(): void {
        // this.selectedResultType = RESULT_TYPE.SUCCESS_AND_FAIL;
        // this.selectedAgent = AGENT_ALL;
        // this.responseTimeRange = [this.responseTimeMin, this.responseTimeMax];
        // this.urlPattern = '';

        this.outClosePopup.emit();
    }
    onSelectResultType(type: number): void {
        this.selectedResultType = type;
    }
    onSelectAgent(agent: string): void {
        this.selectedAgent = agent;
    }
    getIconFullPath(applicationName: string): string {
        return this.funcImagePath(applicationName);
    }
    isSelectedResultType(type: number): boolean {
        return this.selectedResultType === type;
    }
    isSelectedAgent(agent: string): boolean {
        return this.selectedAgent === agent;
    }
    getTimeFormatter(): NouiFormatter {
        return <NouiFormatter>new TimeFormatter();
    }

    set urlPattern(url: string) {
        this._urlPattern = url.startsWith('/') ? url : `/${url}`;
    }

    get urlPattern(): string {
        return this._urlPattern;
    }
}
