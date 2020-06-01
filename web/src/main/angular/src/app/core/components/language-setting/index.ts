import { NgModule } from '@angular/core';

import { SharedModule } from 'app/shared';
import { LanguageSettingContainerComponent } from './language-setting-container.component';
import { LanguageSettingComponent } from './language-setting.component';

@NgModule({
    imports: [
        SharedModule
    ],
    exports: [
        LanguageSettingContainerComponent
    ],
    declarations: [
        LanguageSettingContainerComponent,
        LanguageSettingComponent
    ],
    providers: [],
})
export class LanguageSettingModule {}
