import { NgModule } from '@angular/core';
import { SharedModule } from 'app/shared';
import { ErrorPageRoutingModule } from './error-page.routing';
import { ErrorPageComponent } from './error-page.component';
import { ApplicationListModule } from 'app/core/components/application-list';

@NgModule({
    declarations: [
        ErrorPageComponent
    ],
    imports: [
        SharedModule,
        ErrorPageRoutingModule,
        ApplicationListModule
    ],
    exports: [],
    providers: []
})
export class ErrorPageModule {}
