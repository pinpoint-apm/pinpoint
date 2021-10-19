import { Component, OnInit, Input, Output, EventEmitter, ChangeDetectionStrategy } from '@angular/core';

import { IWebhook } from 'app/shared/services';

@Component({
    selector: 'pp-alarm-rule-webhook-list',
    templateUrl: './alarm-rule-webhook-list.component.html',
    styleUrls: ['./alarm-rule-webhook-list.component.css'],
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class AlarmRuleWebhookListComponent implements OnInit {
    @Input() webhook: IWebhook;
    @Input() checkedWebhookList: IWebhook['webhookId'][];
    @Output() outCheckWebhook = new EventEmitter<string>();

    constructor() {}
    ngOnInit() {}
    onCheckWebhook(): void {
        this.outCheckWebhook.emit(this.webhook.webhookId);
    }

    get isChecked(): boolean {
        return this.checkedWebhookList.some((webhookId: string) => {
            return webhookId === this.webhook.webhookId;
        });
    }
}
