import { Component, OnInit } from '@angular/core';
import { Subject, forkJoin } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { IWebhook, IWebhookCreate, IWebhookRule, WebhookDataService } from 'app/shared/services/webhook-data.service';
import { AnalyticsService, TRACKED_EVENT_LIST, WebAppSettingDataService, TranslateReplaceService } from 'app/shared/services';
import { ApplicationListInteractionForConfigurationService } from 'app/core/components/application-list/application-list-interaction-for-configuration.service';
import { TranslateService } from '@ngx-translate/core';
import { isThatType } from 'app/core/utils/util';

@Component({
  selector: 'pp-webhook-list-container',
  templateUrl: './webhook-list-container.component.html',
  styleUrls: ['./webhook-list-container.component.css']
})
export class WebhookListContainerComponent implements OnInit {
  private unsubscribe = new Subject<void>();
  private selectedApplication: IApplication = null;

  webhookList: IWebhook[] = [];
  editWebhook: IWebhook;
  allowedUserEdit = false;
  showPopup = false;
  useDisable = false;
  showLoading = false;
  i18nText = {
    APP_NOT_SELECTED: '',
    NO_WEBHOOK_RESGISTERED: '',
    ALIAS_PLACEHOLDER: '',
  };
  i18nFormGuide: {[key: string]: IFormFieldErrorType};
  errorMessage: string;

  constructor(
    private translateService: TranslateService,
    private translateReplaceService: TranslateReplaceService,
    private applicationListInteractionForConfigurationService: ApplicationListInteractionForConfigurationService,
    private webAppSettingDataService: WebAppSettingDataService,
    private webhookDataService: WebhookDataService,
    private analyticsService: AnalyticsService,
  ) { }

  ngOnInit() {
    this.initI18NText();
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

  showGuide(): boolean {
    return !this.isApplicationSelected() || this.webhookList.length === 0;
  }
  
  get guideMessage(): string {
    return !this.isApplicationSelected() ? this.i18nText.APP_NOT_SELECTED : this.i18nText.NO_WEBHOOK_RESGISTERED;
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
      this.selectedApplication = selectedApplication;
      this.getWebhookList();
      this.onClosePopup();
    });
  }

  private initI18NText() {
    forkJoin(
        this.translateService.get('COMMON.REQUIRED'),
        this.translateService.get('COMMON.SELECT_YOUR_APP'),
        this.translateService.get('COMMON.EMPTY'),
        this.translateService.get('CONFIGURATION.WEBHOOK.URL'),
        this.translateService.get('CONFIGURATION.WEBHOOK.URL_VALIDATION'),
        this.translateService.get('CONFIGURATION.WEBHOOK.ALIAS'),
    ).subscribe(([requiredMessage, selectApp, empty, urlLabel, urlValidation, alias]: string[]) => {
        this.i18nFormGuide = {
            url: { 
              required: this.translateReplaceService.replace(requiredMessage, urlLabel),
              valueRule: urlValidation,
            },
        };
        this.i18nText = {
          APP_NOT_SELECTED: selectApp,
          NO_WEBHOOK_RESGISTERED: empty,
          ALIAS_PLACEHOLDER: alias,
        }
    });
  }

  onCreateWebhook({ url, alias }: IWebhookRule): void {
    this.showProcessing();
    const webhook: IWebhookCreate = {
      url,
      alias: alias || url,
      serviceName: '',
      applicationId: this.selectedApplication.applicationName,
    }

    this.webhookDataService
      .addWebhook(webhook)
      .subscribe(result => {
        if (isThatType<IServerErrorShortFormat>(result, 'errorCode', 'errorMessage')) {
          this.errorMessage = result.errorMessage
        } else {
          this.getWebhookList();
        }
        this.hideProcessing();
      }, error => {
        this.errorMessage = error.exception.message;
        this.hideProcessing();
      });    
  }

  onUpdateWebhook({ url, alias }: IWebhookRule): void {
    this.showProcessing();
    const webhook: IWebhook = {
      url,
      alias: alias || url,
      webhookId: this.editWebhook.webhookId,
      serviceName: this.editWebhook.serviceName,
      applicationId: this.selectedApplication.applicationName,
    }

    this.webhookDataService
      .editWebhook(webhook)
      .subscribe(result => {
        if (isThatType<IServerErrorShortFormat>(result, 'errorCode', 'errorMessage')) {
          this.errorMessage = result.errorMessage
        } else {
          this.getWebhookList();
        }
        this.hideProcessing();
      }, error => {
        this.errorMessage = error.exception.message;
        this.hideProcessing();
      });   
  }

  onRemoveWebhook(webhook: IWebhook): void {
    this.showProcessing();

    this.webhookDataService
      .removeWebhook(webhook)
      .subscribe(result => {
        if (isThatType<IServerErrorShortFormat>(result, 'errorCode', 'errorMessage')) {
          this.errorMessage = result.errorMessage
        } else {
          this.getWebhookList();
        }
        this.hideProcessing();
      }, error => {
        this.errorMessage = error.exception.message;
        this.hideProcessing();
      });   
  }

  onClosePopup(): void {
    this.showPopup = false;
  }

  onAddWebhook(): void {
    this.editWebhook = null;
    this.showPopup = true;
  }
  
  onEditWebhook(webhookId: string): void {
    this.editWebhook = this.webhookList.find(webhook => webhook.webhookId === webhookId);
    this.showPopup = true;
    this.analyticsService.trackEvent(TRACKED_EVENT_LIST.SHOW_WEBHOOK_UPDATE_POPUP);
  }
  
  private getWebhookList() {
    this.showProcessing();
    this.webhookDataService
      .getWebhookListByAppId(this.selectedApplication.applicationName)
      .subscribe(result => {
        isThatType<IServerErrorShortFormat>(result, 'errorCode', 'errorMessage')
                ? this.errorMessage = result.errorMessage
                : this.webhookList = result;
        this.hideProcessing();
      }, error => {
        this.errorMessage = error.exception.message;
      })
  }

  private showProcessing(): void {
    this.useDisable = true;
    this.showLoading = true;
  }

  private hideProcessing(): void {
      this.useDisable = false;
      this.showLoading = false;
  }

  onCloseErrorMessage(): void {
    this.errorMessage = '';
  }
}
