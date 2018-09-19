import { Component, EventEmitter, Input, Output, OnInit, OnChanges, SimpleChanges } from '@angular/core';

@Component({
    selector: 'pp-server-map-options',
    templateUrl: './server-map-options.component.html',
    styleUrls: ['./server-map-options.component.css']
})
export class ServerMapOptionsComponent implements OnInit, OnChanges {
    hideList = true;
    prevWasOnly: boolean;
    prevBidirectional: boolean;
    prevSelectedInbound: string;
    prevSelectedOutbound: string;
    bidirectionalPath: string;
    @Input() funcImagePath: Function;
    @Input() selectedWasOnly: boolean;
    @Input() selectedBidirectional: boolean;
    @Input() selectedInbound: string;
    @Input() selectedOutbound: string;
    @Input() inboundList: string[];
    @Input() outboundList: string[];
    @Output() outSelected: EventEmitter<{
        inbound: string,
        outbound: string,
        wasOnly: boolean,
        bidirectional: boolean
    }> = new EventEmitter();

    constructor() {}
    ngOnInit() {}
    ngOnChanges(changes: SimpleChanges) {
        if (changes['selectedBidirectional']) {
            this.prevBidirectional = this.selectedBidirectional = changes['selectedBidirectional'].currentValue;
        }
        if (changes['selectedWasOnly']) {
            this.prevWasOnly = this.selectedWasOnly = changes['selectedWasOnly'].currentValue;
        }
        if (changes['selectedInbound']) {
            this.prevSelectedInbound = this.selectedInbound = changes['selectedInbound'].currentValue;
        }
        if (changes['selectedOutbound']) {
            this.prevSelectedOutbound = this.selectedOutbound = changes['selectedOutbound'].currentValue;
        }
    }
    private close(): void {
        this.hideList = true;
    }
    getBidirectional(): string {
        return this.funcImagePath('bidirect_' + (this.selectedBidirectional ? 'on' : 'off'));
    }
    isWasOnlySelected(): boolean {
        return this.selectedWasOnly;
    }
    onChangeWasOnly(): void {
        this.selectedWasOnly = !this.selectedWasOnly;
    }
    onChangeBidirectional(): void {
        this.selectedBidirectional = !this.selectedBidirectional;
    }
    onSelectInbound(inbound: string): void {
        this.selectedInbound = inbound;
    }

    onSelectOutbound(outbound: string): void {
        this.selectedOutbound = outbound;
    }
    onApply(): void {
        if (!(this.selectedInbound === this.prevSelectedInbound && this.selectedOutbound === this.prevSelectedOutbound && this.selectedWasOnly === this.prevWasOnly && this.selectedBidirectional === this.prevBidirectional)) {
            this.outSelected.emit({
                inbound: this.selectedInbound,
                outbound: this.selectedOutbound,
                wasOnly: this.selectedWasOnly,
                bidirectional: this.selectedBidirectional
            });
        }
        this.close();
    }

    onCancel(): void {
        this.selectedWasOnly = this.prevWasOnly;
        this.selectedBidirectional = this.prevBidirectional;
        this.selectedInbound = this.prevSelectedInbound;
        this.selectedOutbound = this.prevSelectedOutbound;
        this.close();
    }
    toggleList(): void {
        this.hideList = !this.hideList;
    }
    onClose(): void {
        this.onCancel();
    }
}
