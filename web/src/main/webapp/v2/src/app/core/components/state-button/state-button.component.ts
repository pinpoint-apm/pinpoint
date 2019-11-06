import { Component, OnInit, Output, OnChanges, EventEmitter, SimpleChanges, Input } from '@angular/core';

import { AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';

export enum BUTTON_STATE {
    PAUSE = 'Pause',
    RESUME = 'Resume',
    COMPLETED = 'Completed',
    MORE = 'More',
    DONE = 'Done'
}

@Component({
    selector: 'pp-state-button',
    templateUrl: './state-button.component.html',
    styleUrls: ['./state-button.component.css']
})
export class StateButtonComponent implements OnInit, OnChanges {
    currentStateText = BUTTON_STATE.PAUSE.toString();
    @Input() width: number;
    @Input() showCountInfo: boolean;
    @Input() countInfo: number[];
    @Input() currentState = BUTTON_STATE.PAUSE;
    @Output() outChangeState: EventEmitter<BUTTON_STATE> = new EventEmitter();
    constructor(
        private analyticsService: AnalyticsService,
    ) {
        this.currentStateText = this.currentState.toString();
    }
    ngOnInit() {}
    ngOnChanges(changes: SimpleChanges) {
        if (changes['currentState'] && changes['currentState']['currentValue']) {
            this.currentStateText = this.currentState.toString();
        }
    }
    private changeState(state: BUTTON_STATE): void {
        this.currentState = state;
        this.currentStateText = this.currentState.toString();
    }
    onClick() {
        switch (this.currentState) {
            case BUTTON_STATE.COMPLETED:
                this.outChangeState.emit(this.currentState);
                this.changeState(BUTTON_STATE.COMPLETED);
                break;
            case BUTTON_STATE.DONE:
                this.outChangeState.emit(this.currentState);
                this.changeState(BUTTON_STATE.DONE);
                break;
            case BUTTON_STATE.PAUSE:
                this.outChangeState.emit(this.currentState);
                this.changeState(BUTTON_STATE.RESUME);
                break;
            case BUTTON_STATE.RESUME:
                this.outChangeState.emit(this.currentState);
                this.changeState(BUTTON_STATE.PAUSE);
                break;
            case BUTTON_STATE.MORE:
                this.analyticsService.trackEvent(TRACKED_EVENT_LIST.CLICK_MORE_STATE_BUTTON);
                this.outChangeState.emit(this.currentState);
                break;
        }
    }
}
