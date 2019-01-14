
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ClipboardModule } from 'ngx-clipboard';

import { SyntaxHighlightPopupComponent } from './syntax-highlight-popup.component';
import { SyntaxHighlightPopupContainerComponent } from './syntax-highlight-popup-container.component';
import { SyntaxHighlightDataService } from './syntax-highlight-data.service';

@NgModule({
    declarations: [
        SyntaxHighlightPopupComponent,
        SyntaxHighlightPopupContainerComponent
    ],
    imports: [
        CommonModule,
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
