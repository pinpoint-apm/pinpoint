
import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ServerErrorPopupComponent } from './server-error-popup.component';
import { ServerErrorPopupContainerComponent } from './server-error-popup-container.component';

@NgModule({
    declarations: [
        ServerErrorPopupComponent,
        ServerErrorPopupContainerComponent
    ],
    imports: [
        CommonModule
    ],
    entryComponents: [
        ServerErrorPopupContainerComponent
    ],
    providers: []
})
export class ServerErrorPopupModule {}
export * from './server-error-popup-container.component';
