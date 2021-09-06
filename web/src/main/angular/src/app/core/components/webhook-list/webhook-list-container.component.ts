import { Component, OnInit } from '@angular/core';
import { Subject, Observable } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { WebhookListDataService } from './webhook-list-data.service';
import { MessageQueueService, MESSAGE_TO, AnalyticsService, TRACKED_EVENT_LIST, WebAppSettingDataService } from 'app/shared/services';
import { ApplicationListInteractionForConfigurationService } from 'app/core/components/application-list/application-list-interaction-for-configuration.service';

@Component({
  selector: 'pp-webhook-list-container',
  templateUrl: './webhook-list-container.component.html',
  styleUrls: ['./webhook-list-container.component.css']
})
export class WebhookListContainerComponent implements OnInit {
  private unsubscribe = new Subject<void>();
  private selectedApplication: IApplication = null;

  webhookList: any[] = [];
  allowedUserEdit = false;
  showPopup = false;
  editWebhook: any;

  constructor(
    private applicationListInteractionForConfigurationService: ApplicationListInteractionForConfigurationService,
    private webAppSettingDataService: WebAppSettingDataService,
    private webhookListDataService: WebhookListDataService,
    private messageQueueService: MessageQueueService,
    private analyticsService: AnalyticsService,
  ) { }

  ngOnInit() {
    this.checkUserEditable();
    this.bindToAppSelectionEvent();
  }

  ngOnDestroy() {
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }

  isApplicationSelected(): boolean {
    return this.selectedApplication !== null;
  }

  private checkUserEditable() {
    this.webAppSettingDataService.useUserEdit().subscribe((allowedUserEdit: boolean) => {
        this.allowedUserEdit = allowedUserEdit;
    });
  }

  private bindToAppSelectionEvent(): void {
    this.applicationListInteractionForConfigurationService.onSelectApplication$.pipe(
      takeUntil(this.unsubscribe)
    ).subscribe((selectedApplication: IApplication) => {
      const { applicationName } = selectedApplication;

      this.selectedApplication = selectedApplication;
      this.getWebhookList(applicationName);
      // this.webhookListDataService.getWebhookList(applicationName).subscribe(result => {

      // });

    });
  }

  private onShowPopup(): void {
    console.log('onShowPopup');
    this.showPopup = true;
  }

  private onClosePopup(): void {
    console.log('onClosePopup');
    this.showPopup = false;
  }
  
  private onEditWebhook(webhookId: string): void {
    this.editWebhook = this.webhookList.find(webhook => webhook.webhookId === webhookId);
    console.log(this.editWebhook);
    this.showPopup = true;
    this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SHOW_WEBHOOK_UPDATE_POPUP);
  }
  
  private onAddWebhook(): void {
    console.log('onAddWebhook');
  }
  
  private onRemoveWebhook(): void {
    console.log('onRemoveWebhook');
  }

  private getWebhookList(applicationName: string) {
    this.webhookList = [
      {
        alias: 'oss',
        webhookId: '99771234',
        url: 'htts://oss.navercorp.com', 
        applicationId: '1',
        serviceApplicationGroupId: '',
      },
      {
        alias: 'git',
        webhookId: '46311234',
        url: 'htts://github.com', 
        applicationId: '2',
        serviceApplicationGroupId: '',
      },
      {
        alias: 'naver',
        webhookId: '68601234',
        url: 'htts://naver.com', 
        applicationId: '3',
        serviceApplicationGroupId: '',
      },
      {
        alias: 'goolgle',
        webhookId: '37791234',
        url: 'htts://google.com', 
        applicationId: '4',
        serviceApplicationGroupId: '',
      },
      {
        alias: 'oss',
        webhookId: '58751234',
        url: 'htts://oss.navercorp.com', 
        applicationId: '5',
        serviceApplicationGroupId: '',
      },
      {
        alias: 'git',
        webhookId: '10831234',
        url: 'htts://github.com', 
        applicationId: '6',
        serviceApplicationGroupId: '',
      },
      {
        alias: 'naver',
        webhookId: '76591234',
        url: 'htts://naver.com', 
        applicationId: '7',
        serviceApplicationGroupId: '',
      },
      {
        alias: 'goolgle',
        webhookId: '70871234',
        url: 'htts://google.com', 
        applicationId: '8',
        serviceApplicationGroupId: '',
      },
      {
        alias: 'oss',
        webhookId: '79961234',
        url: 'htts://oss.navercorp.com', 
        applicationId: '9',
        serviceApplicationGroupId: '',
      },
      {
        alias: 'git',
        webhookId: '41031234',
        url: 'htts://github.com', 
        applicationId: '1',
        serviceApplicationGroupId: '',
      },
      {
        alias: 'naver',
        webhookId: '16791234',
        url: 'htts://naver.com', 
        applicationId: '2',
        serviceApplicationGroupId: '',
      },
      {
        alias: 'goolgle',
        webhookId: '59891234',
        url: 'htts://google.com', 
        applicationId: '3',
        serviceApplicationGroupId: '',
      },
      {
        alias: 'oss',
        webhookId: '22551234',
        url: 'htts://oss.navercorp.com', 
        applicationId: '4',
        serviceApplicationGroupId: '',
      },
      {
        alias: 'git',
        webhookId: '61771234',
        url: 'htts://github.com', 
        applicationId: '5',
        serviceApplicationGroupId: '',
      },
      {
        alias: 'naver',
        webhookId: '75771234',
        url: 'htts://naver.com', 
        applicationId: '6',
        serviceApplicationGroupId: '',
      },
      {
        alias: 'goolgle',
        webhookId: '95241234',
        url: 'htts://google.com', 
        applicationId: '7',
        serviceApplicationGroupId: '',
      },
      {
        alias: 'oss',
        webhookId: '27411234',
        url: 'htts://oss.navercorp.com', 
        applicationId: '8',
        serviceApplicationGroupId: '',
      },
      {
        alias: 'git',
        webhookId: '59481234',
        url: 'htts://github.com', 
        applicationId: '9',
        serviceApplicationGroupId: '',
      },
      {
        alias: 'naver',
        webhookId: '12461234',
        url: 'htts://naver.com', 
        applicationId: '9',
        serviceApplicationGroupId: '',
      },
      {
        alias: 'goolgle',
        webhookId: '65801234',
        url: 'htts://google.com', 
        applicationId: '56',
        serviceApplicationGroupId: '',
      },
      
    ]
    // this.webhookListDataService.getWebhookList(applicationName).subscribe(result => {

    // })
  }
}
