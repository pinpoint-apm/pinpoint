import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

@Component({
    selector: 'pp-duplication-check',
    templateUrl: './duplication-check.component.html',
    styleUrls: ['./duplication-check.component.css'],
})
export class DuplicationCheckComponent implements OnInit {
    @Input() placeholder: string;
    @Input() message: string;
    @Input() isValueValid: boolean;
    @Output() outCheckValue = new EventEmitter<string>();

    constructor() {}
    ngOnInit() {}
    emitValue(value: string): void {
        this.outCheckValue.emit(value);
    }

    getInputStyleClass(): object {
        return {
            'l-success-input': this.isValueValid,
            'l-fail-input': this.isValueValid === false
        };
    }

    getSpanStyleClass(): object {
        return {
            'fa-check': this.isValueValid,
            'fa-times': this.isValueValid === false
        };
    }
}
