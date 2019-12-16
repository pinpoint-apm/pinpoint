
import { NgModule } from '@angular/core';
import { ClipboardModule } from 'ngx-clipboard';

import { SharedModule } from 'app/shared';
import { SyntaxHighlightPopupComponent } from './syntax-highlight-popup.component';
import { SyntaxHighlightPopupContainerComponent } from './syntax-highlight-popup-container.component';
import { SyntaxHighlightDataService } from './syntax-highlight-data.service';

@NgModule({
    declarations: [
        SyntaxHighlightPopupComponent,
        SyntaxHighlightPopupContainerComponent
    ],
    imports: [
        SharedModule,
        ClipboardModule,
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
