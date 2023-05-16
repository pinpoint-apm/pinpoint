import { Injectable } from '@angular/core';

@Injectable()
export class TranslateReplaceService {
    constructor() { }
    replace(template: string, ...argu: any[]): string {
        let replacedTemplate = template;
        argu.forEach((value: string, index: number) => {
            replacedTemplate = replacedTemplate.replace( new RegExp('\\!\\{' + index + '\\}'), '' + value);
        });
        return replacedTemplate;
    }
}
