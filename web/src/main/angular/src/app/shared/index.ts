import { NgModule, ModuleWithProviders } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ClipboardModule } from 'ngx-clipboard';

import { ClickOutsideModule } from 'ng-click-outside';
import { TranslateReplaceService } from './services/translate-replace.service';
import { ServerTimeDataService } from './services/server-time-data.service';
import { WebAppSettingDataService } from './services/web-app-setting-data.service';
import { ComponentDefaultSettingDataService } from './services/component-default-setting-data.service';
import { RouteInfoCollectorService } from './services/route-info-collector.service';
import { ServerTimeResolverService } from './services/server-time-resolver.service';
import { NewUrlStateNotificationService } from './services/new-url-state-notification.service';
import { UrlRouteManagerService } from './services/url-route-manager.service';
import { SystemConfigurationDataService } from './services/system-configuration-data.service';
import { SystemConfigurationResolverService } from './services/system-configuration-resolver.service';
import { SplitRatioService } from './services/split-ratio.service';
import { GutterEventService } from './services/gutter-event.service';
import { ApplicationListResolverService } from './services/application-list-resolver.service';
import { AnalyticsService } from './services/analytics.service';
import { BrowserSupportCheckService } from './services/browser-support-check.service';
import { AgentHistogramDataService } from './services/agent-histogram-data.service';
import { TransactionDetailDataService } from './services/transaction-detail-data.service';
import { StoreHelperService } from './services/store-helper.service';
import { UrlValidateGuard } from './services/url-validate.guard';
import { ThemeService } from './services/theme.service';
import { AuthService } from './services/auth.service';

import { HeaderLogoComponent } from './components/header-logo';
import { EmptyContentsComponent, NoneComponent } from './components/empty-contents';
import { UrlRedirectorComponent } from './components/url-redirector';
import { LoadingComponent } from './components/loading';
import { FilmForDisableComponent } from './components/film-for-disable';
import { SimpleProgressSliderComponent } from './components/simple-progress-slider';
import { FormFieldErrorMessageComponent } from './components/form-field-error-message';
import { ServerErrorMessageComponent } from './components/server-error-message';
import { ContextPopupDirective } from './directives/context-popup.directive';
import { SearchInputDirective } from './directives/search-input.directive';
import { SafeHtmlPipe } from './pipes/safe-html.pipe';
import { JSONTextParserPipe } from './pipes/json-text-parser.pipe';
import { DynamicPopupService } from './services/dynamic-popup.service';
import { MessageQueueService } from './services/message-queue.service';
import { WindowRefService } from './services/window-ref.service';
import { ApplicationListDataService } from './services/application-list-data.service';
import { SafeStylePipe } from './pipes/safe-style.pipe';
import { RetryComponent } from './components/retry';
import { HandleObsPipe } from './pipes/handle-obs.pipe';
import { PickPropsPipe } from './pipes/pick-props.pipe';

@NgModule({
    declarations: [
        NoneComponent,
        HeaderLogoComponent,
        EmptyContentsComponent,
        UrlRedirectorComponent,
        LoadingComponent,
        RetryComponent,
        FilmForDisableComponent,
        SimpleProgressSliderComponent,
        FormFieldErrorMessageComponent,
        ServerErrorMessageComponent,
        SafeHtmlPipe,
        SafeStylePipe,
        JSONTextParserPipe,
        HandleObsPipe,
        PickPropsPipe,
        ContextPopupDirective,
        SearchInputDirective
    ],
    imports: [
        CommonModule,
        RouterModule,
        ClickOutsideModule
    ],
    exports: [
        CommonModule,
        RouterModule,
        FormsModule,
        ClipboardModule,
        ClickOutsideModule,
        HeaderLogoComponent,
        EmptyContentsComponent,
        UrlRedirectorComponent,
        LoadingComponent,
        RetryComponent,
        FilmForDisableComponent,
        SimpleProgressSliderComponent,
        FormFieldErrorMessageComponent,
        ServerErrorMessageComponent,
        SafeHtmlPipe,
        SafeStylePipe,
        JSONTextParserPipe,
        HandleObsPipe,
        PickPropsPipe,
        ContextPopupDirective,
        SearchInputDirective
    ],
    providers: []
})
export class SharedModule {
    static forRoot(): ModuleWithProviders {
        return {
            ngModule: SharedModule,
            providers: [
                SystemConfigurationDataService,
                SystemConfigurationResolverService,
                TranslateReplaceService,
                ServerTimeDataService,
                ServerTimeResolverService,
                ComponentDefaultSettingDataService,
                RouteInfoCollectorService,
                WebAppSettingDataService,
                NewUrlStateNotificationService,
                UrlRouteManagerService,
                StoreHelperService,
                UrlValidateGuard,
                AnalyticsService,
                WindowRefService,
                SplitRatioService,
                GutterEventService,
                BrowserSupportCheckService,
                AgentHistogramDataService,
                TransactionDetailDataService,
                MessageQueueService,
                DynamicPopupService,
                ApplicationListResolverService,
                ApplicationListDataService,
                ThemeService,
                AuthService
            ]
        };
    }
}
