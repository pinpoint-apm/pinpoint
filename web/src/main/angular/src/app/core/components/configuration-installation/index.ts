import { NgModule } from '@angular/core';
import { ClipboardModule } from 'ngx-clipboard';

import { SharedModule } from 'app/shared';
import { DuplicationCheckModule } from 'app/core/components/duplication-check';
import { ConfigurationInstallationContainerComponent } from './configuration-installation-container.component';
import { ConfigurationInstallationDataService } from './configuration-installation-data.service';
import { ConfigurationInstallationJVMArgumentInfoComponent } from './configuration-installation-jvm-argument-info.component';
import { ServerErrorPopupModule } from 'app/core/components/server-error-popup';

@NgModule({
    declarations: [
        ConfigurationInstallationContainerComponent,
        ConfigurationInstallationJVMArgumentInfoComponent
    ],
    imports: [
        SharedModule,
        ClipboardModule,
        DuplicationCheckModule,
        ServerErrorPopupModule
    ],
    exports: [
        ConfigurationInstallationContainerComponent
    ],
    providers: [
        ConfigurationInstallationDataService
    ]
})
export class ConfigurationInstallationModule { }
