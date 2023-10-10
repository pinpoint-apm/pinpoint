import { ValidatorFn, AbstractControl } from '@angular/forms';

export class CustomFormValidatorService {
    static validate(condi: any): ValidatorFn {
        return (control: AbstractControl): {[key: string]: any} | null => {
            return !control.value || (condi instanceof RegExp ? condi.test(control.value.trim()) : condi)
                ? null
                : {'valueRule': {value: control.value}};
        };
    }
}
