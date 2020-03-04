import { ValidatorFn, AbstractControl } from '@angular/forms';

export class CustomFormValidatorService {
    static validate(regExp: RegExp): ValidatorFn {
        return (control: AbstractControl): {[key: string]: any} | null => {
            return !control.value || regExp.test(control.value.trim())
                ? null
                : {'valueRule': {value: control.value}}
        };
    }
}
