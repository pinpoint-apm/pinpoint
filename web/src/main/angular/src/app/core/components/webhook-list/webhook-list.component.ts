import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';
import { IWebhook } from './webhook-list-data.service';

@Component({
  selector: 'pp-webhook-list',
  templateUrl: './webhook-list.component.html',
  styleUrls: ['./webhook-list.component.css']
})
export class WebhookListComponent implements OnInit {
  @Input() webhookList: any[];
  @Output() outRemove = new EventEmitter<IWebhook>();
  @Output() outEdit = new EventEmitter<string>();

  private removeConfirmWebhook: IWebhook;

  constructor() { }

  ngOnInit() { }

  onRemove(webhook: IWebhook): void {
    this.removeConfirmWebhook = webhook;
  }

  onEdit(webhookId: string): void {
    this.outEdit.emit(webhookId);
  }

  onCancelRemove(): void {
    this.removeConfirmWebhook = null;
  }

  onConfirmRemove(): void {
    this.outRemove.emit(this.removeConfirmWebhook);
    this.removeConfirmWebhook = null;
  }

  hasRemoveTarget(webhookId: string): boolean {
    return this.removeConfirmWebhook && this.removeConfirmWebhook.webhookId === webhookId;
  }
}
