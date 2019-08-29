import { Component, OnInit, Input, Output, ViewChild, ElementRef, EventEmitter } from '@angular/core';

@Component({
    selector: 'pp-scatter-chart-setting-popup',
    templateUrl: './scatter-chart-setting-popup.component.html',
    styleUrls: ['./scatter-chart-setting-popup.component.css']
})
export class ScatterChartSettingPopupComponent implements OnInit {
    @ViewChild('minInput', { static: true }) minInput: ElementRef;
    @ViewChild('maxInput', { static: true }) maxInput: ElementRef;
    @Input() instanceKey: string;
    @Input() min: number;
    @Input() max: number;
    @Output() outApply: EventEmitter<{key: string, min: number, max: number}> = new EventEmitter();
    @Output() outCancel: EventEmitter<null> = new EventEmitter();
    constructor() {}
    ngOnInit() {}
    onApply(): void {
        this.min = Number.parseInt(this.minInput.nativeElement.value, 10);
        this.max = Number.parseInt(this.maxInput.nativeElement.value, 10);
        this.outApply.emit({
            key: this.instanceKey,
            min: this.min,
            max: this.max
        });
    }
    onCancel(): void {
        this.minInput.nativeElement.value = this.min;
        this.maxInput.nativeElement.value = this.max;
        this.outCancel.emit();
    }
}
