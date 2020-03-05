import { Pipe, PipeTransform } from '@angular/core';
import { isObservable } from 'rxjs';
import { pluck } from 'rxjs/operators';

@Pipe({
    name: 'pickProps',
})
export class PickPropsPipe implements PipeTransform {
    transform(val: any, prop: string) {
        return isObservable(val)
            ? val.pipe(
                pluck(prop)
            )
            : val[prop];
    }
}
