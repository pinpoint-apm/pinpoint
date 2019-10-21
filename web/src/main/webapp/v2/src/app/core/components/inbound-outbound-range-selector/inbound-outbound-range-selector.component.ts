import { Component, EventEmitter, Input, Output, OnInit, OnChanges, SimpleChanges } from '@angular/core';

@Component({
    selector: 'pp-inbound-outbound-range-selector',
    templateUrl: './inbound-outbound-range-selector.component.html',
    styleUrls: ['./inbound-outbound-range-selector.component.css']
})
export class InboundOutboundRangeSelectorComponent implements OnInit, OnChanges {
    hideList = true;
    prevSelectedInbound: string;
    prevSelectedOutbound: string;
    @Input() selectedInbound: string;
    @Input() selectedOutbound: string;
    @Input() inboundList: string[];
    @Input() outboundList: string[];
    @Output() outSelected = new EventEmitter<string[]>();

    constructor() {}

    ngOnChanges(changes: SimpleChanges) {
        if (changes['selectedInbound']) {
            this.prevSelectedInbound = this.selectedInbound = changes['selectedInbound'].currentValue;
        }
        if (changes['selectedOutbound']) {
            this.prevSelectedOutbound = this.selectedOutbound = changes['selectedOutbound'].currentValue;
        }
    }

    ngOnInit() { }
    onSelectInbound(inbound: string): void {
        this.selectedInbound = inbound;
    }

    onSelectOutbound(outbound: string): void {
        this.selectedOutbound = outbound;
    }

    onApply(): void {
        if (!(this.selectedInbound === this.prevSelectedInbound && this.selectedOutbound === this.prevSelectedOutbound)) {
            this.prevSelectedInbound = this.selectedInbound;
            this.prevSelectedOutbound = this.selectedOutbound;
            this.outSelected.emit([this.selectedInbound, this.selectedOutbound]);
        }
        this.close();
    }

    onCancel(): void {
        this.selectedInbound = this.prevSelectedInbound;
        this.selectedOutbound = this.prevSelectedOutbound;
        this.close();
    }

    toggleList(): void {
        this.hideList = !this.hideList;
    }

    onClose(): void {
        if (!this.hideList) {
            this.onCancel();
            this.close();
        }
    }

    private close(): void {
        this.hideList = true;
    }
}
