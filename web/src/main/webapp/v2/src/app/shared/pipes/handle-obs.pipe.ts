import { Pipe, PipeTransform } from '@angular/core';
import { isObservable, of } from 'rxjs';
import { map, startWith, catchError } from 'rxjs/operators';

// TODO: Apply it throught the app
@Pipe({
    name: 'handleObs',
})
export class HandleObsPipe implements PipeTransform {
    transform(val: any) {
        return isObservable(val)
            ? val.pipe(
                map((value: any) => ({loading: false, value})),
                startWith({loading: true}),
                catchError((error: {[key: string]: any}) => of({loading: false, error}))
            )
            : val;
    }
}
