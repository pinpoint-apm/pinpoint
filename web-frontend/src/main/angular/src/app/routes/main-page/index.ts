import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';

import { SharedModule } from 'app/shared';
import { NoticeModule } from 'app/core/components/notice';
import { MainContentsModule } from 'app/core/components/main-contents';
import { MainPageComponent } from './main-page.component';
import { routing } from './main-page.routing';
import { HelpViewerPopupModule } from 'app/core/components/help-viewer-popup';
import { MessagePopupModule } from 'app/core/components/message-popup';
import { AppWidgetModule } from 'app/core/components/app-widget';
import { SideNavigationBarModule } from 'app/core/components/side-navigation-bar';

@NgModule({
    declarations: [
        MainPageComponent
    ],
    imports: [
        SideNavigationBarModule,
        SharedModule,
        NoticeModule,
        MainContentsModule,
        MessagePopupModule,
        HelpViewerPopupModule,
        AppWidgetModule,
        RouterModule.forChild(routing)
    ],
    exports: [],
    providers: []
})
export class MainPageModule { }
