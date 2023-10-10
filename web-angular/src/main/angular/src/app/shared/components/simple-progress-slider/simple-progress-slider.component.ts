import { Component, OnInit, OnChanges, SimpleChanges, Input } from '@angular/core';
import { trigger, state, style, animate, transition } from '@angular/animations';
import * as moment from 'moment-timezone';

interface IRangePosition {
    value: string;
    position: number;
}

@Component({
    selector: 'pp-simple-progress-slider',
    templateUrl: './simple-progress-slider.component.html',
    styleUrls: ['./simple-progress-slider.component.css'],
    animations: [
        trigger('handlerTrigger', [
            state('start', style({
                left: '100%'
            })),
            state(':enter', style({
                left: '100%'
            })),
            state('next', style({
                left: '{{value}}'
            }), {params: {value: '100%'}}),
            state('andNext', style({
                left: '{{value}}'
            }), {params: {value: '100%'}}),
            transition('* => *', [
                animate('0.3s 0.3s ease-out')
            ])
        ]),
        trigger('barTrigger', [
            state('start', style({
                width: '0%'
            })),
            state(':enter', style({
                width: '0%'
            })),
            state('next', style({
                width: '{{value}}'
            }), {params: {value: '0%'}}),
            state('andNext', style({
                width: '{{value}}'
            }), {params: {value: '0%'}}),
            transition('* => *', [
                animate('0.3s 0.3s ease-out')
            ])
        ]),
    ]
})
export class SimpleProgressSliderComponent implements OnInit, OnChanges {
    @Input() timezone: string;
    @Input() dateFormat: string;
    @Input() rangeValue: number[] = [0, 100];
    @Input() selectedRangeValue: number[] = [100, 100];
    @Input() type = 'date'; // count
    @Input() gradationCount = 6;
    private triggerStep: { [key: string]: string } = {
        start: 'next',
        next: 'andNext',
        andNext: 'next'
    };
    barTrigger = 'start';
    handlerTrigger = 'start';
    showLabel = false;
    rangeStartValue = '';
    rangeEndValue = '';
    selectedStartValue = '';
    gradationValue: IRangePosition[] = [];
    selectedStartPosition = 100;
    constructor() {}
    ngOnInit() {}
    ngOnChanges(changes: SimpleChanges) {
        if (changes['rangeValue'] && changes['rangeValue']['currentValue']) {
            this.initXAxis();
        }
        if (changes['selectedRangeValue'] && changes['selectedRangeValue']['currentValue']) {
            this.selectedStartValue = this.convertToType(this.selectedRangeValue[0]);
            this.calcuSelectedStartPosition();
            this.setAnimationNextStep();
        }
        if (changes['timezone'] && changes['timezone'].firstChange === false) {
            this.changeTimeDisplay();
        }
        if (changes['dateFormat'] && changes['dateFormat'].firstChange === false) {
            this.changeTimeDisplay();
        }
    }
    private setAnimationNextStep(): void {
        this.barTrigger = this.triggerStep[this.barTrigger];
        this.handlerTrigger = this.triggerStep[this.handlerTrigger];
    }
    private initXAxis(): void {
        this.rangeStartValue = this.convertToType(this.rangeValue[0]);
        this.rangeEndValue = this.convertToType(this.rangeValue[1]);
        this.calcuGradationValue();
    }
    private changeTimeDisplay(): void {
        this.initXAxis();
        this.selectedStartValue = this.convertToType(this.selectedRangeValue[0]);
    }
    animationStart($event: any): void {
        this.showLabel = false;
    }
    animationDone($event: any): void {
        if ($event.fromState !== 'void') {
            this.showLabel = true;
        }
    }
    hiddenRightArrowLabel(): boolean {
        return this.selectedStartPosition < 50 || this.showLabel === false;
    }
    hiddenLeftArrowLabel(): boolean {
        return this.selectedStartPosition >= 50 || this.showLabel === false;
    }
    convertToType(value: number): string {
        switch (this.type) {
            case 'count':
                return value.toString();
            case 'date':
                return moment(value).tz(this.timezone).format(this.dateFormat);
            default:
                return value.toString();
        }
    }
    calcuGradationValue(): void {
        const gradationCount = this.gradationCount - 1;
        const gap = this.rangeValue[1] - this.rangeValue[0];
        const gapPosition = 100 / gradationCount;
        const gapValue = gap / gradationCount;
        this.gradationValue.length = 0;
        for (let i = 1; i <= gradationCount - 1; i++) {
            this.gradationValue.push({
                value: this.convertToType(this.rangeValue[0] + (gapValue * i)),
                position: (gapPosition * i),
            });
        }
    }
    calcuSelectedStartPosition(): void {
        const gap = this.rangeValue[1] - this.rangeValue[0];
        const selectedGap = this.selectedRangeValue[1] - this.selectedRangeValue[0];
        const tempStartPosition = 100 - (selectedGap * 100) / gap;
        this.selectedStartPosition = Math.min(100, Math.max(0, tempStartPosition));
    }
}
