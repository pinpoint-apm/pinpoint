import { Component, EventEmitter, Input, Output, OnInit, OnChanges, SimpleChanges } from '@angular/core';

@Component({
    selector: 'pp-server-map-options',
    templateUrl: './server-map-options.component.html',
    styleUrls: ['./server-map-options.component.css']
})
export class ServerMapOptionsComponent implements OnInit, OnChanges {
    @Input() funcImagePath: Function;
    @Input() selectedWasOnly: boolean;
    @Input() selectedBidirectional: boolean;
    @Input() selectedInbound: number;
    @Input() selectedOutbound: number;
    @Input() inboundList: number[];
    @Input() outboundList: number[];
    @Output() outSelected: EventEmitter<{
        inbound: number,
        outbound: number,
        wasOnly: boolean,
        bidirectional: boolean
    }> = new EventEmitter();

    hideList = true;
    prevWasOnly: boolean;
    prevBidirectional: boolean;
    prevSelectedInbound: number;
    prevSelectedOutbound: number;
    bidirectionalPath: string;

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

    get bidirectionalImgSrc(): string {
        return this.funcImagePath(`bidirect_${this.selectedBidirectional ? 'on' : 'off'}`);
    }

    onChangeWasOnly(): void {
        this.selectedWasOnly = !this.selectedWasOnly;
    }

    onChangeBidirectional(): void {
        this.selectedBidirectional = !this.selectedBidirectional;
    }

    onSelectInbound(inbound: number): void {
        this.selectedInbound = inbound;
    }

    onSelectOutbound(outbound: number): void {
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
