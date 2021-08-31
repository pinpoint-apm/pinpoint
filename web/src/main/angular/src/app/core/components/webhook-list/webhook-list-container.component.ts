import { Component, OnInit } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { WebhookListDataService } from './webhook-list-data.service';
import { MessageQueueService, MESSAGE_TO, AnalyticsService, TRACKED_EVENT_LIST } from 'app/shared/services';
import { ApplicationListInteractionForConfigurationService } from 'app/core/components/application-list/application-list-interaction-for-configuration.service';

@Component({
  selector: 'pp-webhook-list-container',
  templateUrl: './webhook-list-container.component.html',
  styleUrls: ['./webhook-list-container.component.css']
})
export class WebhookListContainerComponent implements OnInit {
  private unsubscribe = new Subject<void>();

  constructor(
    private applicationListInteractionForConfigurationService: ApplicationListInteractionForConfigurationService,
    private webhookListDataService: WebhookListDataService,
    private messageQueueService: MessageQueueService,
  ) { }

  ngOnInit() {
    this.bindToAppSelectionEvent();
  }

  ngOnDestroy() {
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }

  private gg(): void {
    // this.webhookListDataService.retrieve().subscribe()
  }

  private bindToAppSelectionEvent(): void {
    this.applicationListInteractionForConfigurationService.onSelectApplication$.pipe(
      takeUntil(this.unsubscribe)
    ).subscribe((selectedApplication: IApplication) => {
      const { applicationName } = selectedApplication;
      this.webhookListDataService.getWebhookList(applicationName).subscribe(result => {
        console.log(result);
      });
    });
  }

}
