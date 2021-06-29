
import { NgModule } from '@angular/core';
import { ClipboardModule } from 'ngx-clipboard';

import { SharedModule } from 'app/shared';
import { SyntaxHighlightPopupComponent } from './syntax-highlight-popup.component';
import { SyntaxHighlightPopupContainerComponent } from './syntax-highlight-popup-container.component';
import { SyntaxHighlightDataService } from './syntax-highlight-data.service';
import { ServerErrorPopupModule } from 'app/core/components/server-error-popup';

@NgModule({
    declarations: [
        SyntaxHighlightPopupComponent,
        SyntaxHighlightPopupContainerComponent
    ],
    imports: [
        SharedModule,
        ClipboardModule,
        ServerErrorPopupModule
    ],
    exports: [],
    entryComponents: [
        SyntaxHighlightPopupContainerComponent
    ],
    providers: [
        SyntaxHighlightDataService
    ]
})
export class SyntaxHighlightPopupModule { }
