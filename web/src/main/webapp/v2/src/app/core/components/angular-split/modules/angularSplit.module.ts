import { NgModule, ModuleWithProviders } from '@angular/core';
import { CommonModule } from '@angular/common';

import { SplitComponent } from '../components/split.component';
import { SplitAreaDirective } from '../components/splitArea.directive';
import { SplitGutterDirective } from '../components/splitGutter.directive';

@NgModule({
    imports: [
        CommonModule
    ],
    declarations: [
        SplitComponent,
        SplitAreaDirective,
        SplitGutterDirective,
    ],
    exports: [
        SplitComponent,
        SplitAreaDirective,
    ]
})
export class AngularSplitModule {

    public static forRoot(): ModuleWithProviders {
        return {
            ngModule: AngularSplitModule,
            providers: []
        };
    }

    public static forChild(): ModuleWithProviders {
        return {
            ngModule: AngularSplitModule,
            providers: []
        };
    }

}
