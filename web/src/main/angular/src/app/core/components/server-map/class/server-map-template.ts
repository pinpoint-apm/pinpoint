import ServerMapTheme from './server-map-theme';
import { NodeGroup } from './node-group.class';

export abstract class ServerMapTemplate {
    private static readonly MIN_ARC_RATIO = 0.05;
    private static readonly RADIUS = 47;
    private static readonly DIAMETER = 2 * Math.PI * ServerMapTemplate.RADIUS;

    private static getCompleteSVGCircleString(showDefault: boolean, responseTime: IResponseTime): string {
        if (showDefault) {
            return ServerMapTemplate.getSVGCircleString({
                stroke: ServerMapTheme.general.circle.default.stroke,
                strokeWidth: ServerMapTheme.general.circle.default.strokeWidth
            });
        } else {
            const sum = Object.keys(responseTime).reduce((prev: number, curr: keyof IResponseTime) => prev + responseTime[curr], 0);
            const slowArc = ServerMapTemplate.calcArc(sum, responseTime.Slow);
            const badArc = ServerMapTemplate.calcArc(sum, responseTime.Error);
            // 원의 중심을 (0,0)이라고 할때, stroke-dashoffset 시작점이 12시방향(0,r)이 아니라 3시방향(r,0)이라서 3/4지름을 기준으로 사용
            const slowArcOffset = -1 * (0.75 * ServerMapTemplate.DIAMETER - (slowArc + badArc));
            const badArcOffset = -1 * (0.75 * ServerMapTemplate.DIAMETER - badArc);

            return ServerMapTemplate.getSVGCircleString({
                stroke: ServerMapTheme.general.circle.good.stroke,
                strokeWidth: ServerMapTheme.general.circle.good.strokeWidth,
            }) + ServerMapTemplate.getSVGCircleString({
                stroke: ServerMapTheme.general.circle.slow.stroke,
                strokeWidth: ServerMapTheme.general.circle.slow.strokeWidth,
                strokeDashOffset: slowArcOffset,
                strokeDashArray: slowArc
            }) + ServerMapTemplate.getSVGCircleString({
                stroke: ServerMapTheme.general.circle.bad.stroke,
                strokeWidth: ServerMapTheme.general.circle.bad.strokeWidth,
                strokeDashOffset: badArcOffset,
                strokeDashArray: badArc
            });
        }
    }

    private static getSVGCircleString(styleOption: {[key: string]: any}): string {
        const {stroke, strokeWidth, strokeDashOffset = 0, strokeDashArray = 'none'} = styleOption;

        return `<circle cx="50" cy="50" r="${ServerMapTemplate.RADIUS}"
            style="fill:none;
            stroke:${stroke};
            stroke-width:${strokeWidth};
            stroke-dashoffset:${strokeDashOffset};
            stroke-dasharray:${strokeDashArray} 1000">
            </circle>`;
    }

    private static calcArc(sum: number, value: number): number {
        return value === 0 ? 0
            : value / sum < ServerMapTemplate.MIN_ARC_RATIO ? ServerMapTemplate.DIAMETER * ServerMapTemplate.MIN_ARC_RATIO
            : value / sum * ServerMapTemplate.DIAMETER;
    }

    private static getAlertSVGImgString(img: HTMLImageElement): string {
        const dataURL = this.getDataURLFromImg(img);

        return `<image xlink:href="${dataURL}" height="20" width="30" stroke="red" x="50%" y="0" transform="translate(10, 16)" />`;
    }

    private static getDataURLFromImg(img: HTMLImageElement): string {
        const canvas = document.createElement('canvas');

        canvas.width = img.width;
        canvas.height = img.height;
        canvas.getContext('2d').drawImage(img, 0, 0);

        return canvas.toDataURL();
    }

    private static getServiceTypeSVGImgString(img: HTMLImageElement): string {
        const dataURL = ServerMapTemplate.getDataURLFromImg(img);
        const w = img.width <= 100 ? img.width : 100;
        const h = img.height <= 65 ? img.height : 65;

        return `<image xlink:href="${dataURL}" height="${h}" width="${w}" stroke="red" x="50%" y="50%" transform="translate(${-(w / 2)}, ${-(h / 2) - 4})"/>`;
    }

    private static getInstanceCountTextString(instanceCount: number): string {
        return `<text x="50%" y="0" text-anchor="middle" alignment-baseline="central" fill="black" transform="translate(0, 80)" font-size="15px">${instanceCount >= 2 ? instanceCount : ''}</text>`;
    }

    public static getSVGString(img: HTMLImageElement[], nodeData: {[key: string]: any}): string {
        const {key, isAuthorized, isWas, histogram, instanceCount} = nodeData;
        const isMergedNode = NodeGroup.isGroupKey(key);

        return `<svg xmlns="http://www.w3.org/2000/svg" width="100" height="100" xmlns:xlink="http://www.w3.org/1999/xlink">` +
            ServerMapTemplate.getCompleteSVGCircleString(isMergedNode || !(isAuthorized && isWas), histogram) +
            (img[1] ? ServerMapTemplate.getAlertSVGImgString(img[1]) : ``) +
            ServerMapTemplate.getServiceTypeSVGImgString(img[0]) +
            ServerMapTemplate.getInstanceCountTextString(instanceCount) +
            `</svg>`;
    }
}
