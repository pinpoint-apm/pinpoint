import { Pipe, PipeTransform } from '@angular/core';
import { WebAppSettingDataService } from 'app/shared/services';
/**
 * sson에서 icon, image등의 키워드를 캐치해서
 * 맞는 icon, image로 해당 텍스트를 대체 또는 추가.
 * [Type]:{property=value}
 * Chaining with '|'
 * ex: [ICON]:{className=fa-clock-o\\style=font-size:17px}|[TEXT]:{value=X-Axis}
 */
@Pipe({ name: 'jsonTextParser' })
export class JSONTextParserPipe implements PipeTransform {
    constructor(
        private webAppSettingDataService: WebAppSettingDataService
    ) {}

    transform(text: string): string {
        if (text) {
            return text.split('|').map((textElem: string) => {
                const i = textElem.indexOf(':');
                const textType = textElem.substr(0, i).replace(/\[|\]/g, '');
                const textInfoArr = textElem.substr(i + 1).replace(/\{|\}/g, '').split('\\').map((textInfo: string) => {
                    return textInfo.split('=')[1];
                });

                switch (textType) {
                    case 'ICON':
                        return `<span class="fas ${textInfoArr[0]}" style="${textInfoArr[1]}"></span>`;
                    case 'TEXT':
                        return textInfoArr[0];
                    case 'IMAGE':
                        const path = this.webAppSettingDataService.getImagePath();
                        const extension = this.webAppSettingDataService.getImageExt();

                        return `<img src="${path}${textInfoArr[0]}${extension}">`;
                    case 'LINK':
                        return `<a href="${textInfoArr[0]}" target="${textInfoArr[1]}" style="${textInfoArr[2]}">${textInfoArr[3]}</a>`;
                    default:
                        return text;
                }
            }).join(' ');
        } else {
            return '';
        }
    }
}
