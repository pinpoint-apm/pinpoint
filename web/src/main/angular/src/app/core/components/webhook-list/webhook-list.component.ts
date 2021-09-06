import { Component, OnInit, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'pp-webhook-list',
  templateUrl: './webhook-list.component.html',
  styleUrls: ['./webhook-list.component.css']
})
export class WebhookListComponent implements OnInit {
  @Input() webhookList: any[];
  @Output() outRemove = new EventEmitter<string>();
  @Output() outEdit = new EventEmitter<string>();

  private removeConfirmId = '';

  constructor(
  ) { }

  ngOnInit() {
  }

  onRemove(webhookId: string): void {
    this.removeConfirmId = webhookId;
  }

  onEdit(webhookId: string): void {
    this.outEdit.emit(webhookId);
  }

  onCancelRemove(): void {
    this.removeConfirmId = '';
  }

  onConfirmRemove(): void {
    this.outRemove.emit(this.removeConfirmId);
    this.removeConfirmId = '';
  }

  hasRemoveTarget(webhookId: string): boolean {
    return this.removeConfirmId === webhookId;
  }
}
