import ServerMapTheme from './server-map-theme';
import { NodeGroup } from './node-group.class';

export abstract class ServerMapTemplate {
    private static readonly MIN_ARC_RATIO = 0.05;
    private static readonly RADIUS = 62;
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
                strokeWidth: ServerMapTheme.general.circle.default.strokeWidth,
            }) + ServerMapTemplate.getSVGCircleString({
                stroke: ServerMapTheme.general.circle.slow.stroke,
                strokeWidth: ServerMapTheme.general.circle.default.strokeWidth,
                strokeDashOffset: slowArcOffset,
                strokeDashArray: slowArc
            }) + ServerMapTemplate.getSVGCircleString({
                stroke: ServerMapTheme.general.circle.bad.stroke,
                strokeWidth: ServerMapTheme.general.circle.default.strokeWidth,
                strokeDashOffset: badArcOffset,
                strokeDashArray: badArc
            });
        }
    }

    private static getSVGCircleString(styleOption: {[key: string]: any}): string {
        const { stroke, strokeWidth, strokeDashOffset = 0, strokeDashArray = 'none' } = styleOption;

        return `<circle cx="65" cy="65" r="${ServerMapTemplate.RADIUS}"
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

        return `<image xlink:href="${dataURL}" height="120" width="120" stroke="red" x="50%" y="0" transform="translate(-10, -15)" />`;
    }

    private static getDataURLFromImg(img: HTMLImageElement): string {
        const canvas = document.createElement('canvas');

        canvas.getContext('2d').drawImage(img, 0, 0);
        return canvas.toDataURL();
    }

    private static getSVGImageStyle(width: number, height: number): {[key: string]: any} {
        /**
         * ServerMap Image Size Group
         * 1. 100 * 65
         * 2. 142 * 74
         * 3. 92 * 25
         * 4. 63 * 87?
         */
        // TODO: static한 수치가아니라 비율로?
        return {
            size: width > 100 ? 200 : 300,
            transform: {
                translateX: width > 100 ? -45 : -50,
                translateY: height > 65 ? -20 : height > 30 ? -45 : -20
            }
        };
    }

    private static getServiceTypeSVGImgString(img: HTMLImageElement): string {
        const dataURL = ServerMapTemplate.getDataURLFromImg(img);
        const { size, transform } = ServerMapTemplate.getSVGImageStyle(img.width, img.height);

        return `<image xlink:href="${dataURL}" height="${size}" width="${size}" stroke="red" x="50%" y="0" transform="translate(${transform.translateX}, ${transform.translateY})" />`;
    }

    private static getInstanceCountTextString(instanceCount: number): string {
        return `<text x="50%" y="0" text-anchor="middle" alignment-baseline="central" fill="black" transform="translate(0, 106)" font-size="20px">${instanceCount >= 2 ? instanceCount : ''}</text>`;
    }

    public static getSVGString(img: HTMLImageElement[], nodeData: {[key: string]: any}): string {
        const { key, isAuthorized, isWas, histogram, instanceCount } = nodeData;
        const isMergedNode = NodeGroup.isGroupKey(key);

        return `<svg xmlns="http://www.w3.org/2000/svg" width="130" height="130" xmlns:xlink="http://www.w3.org/1999/xlink">` +
            ServerMapTemplate.getCompleteSVGCircleString(isMergedNode || !(isAuthorized && isWas), histogram) +
            (img[1] ? ServerMapTemplate.getAlertSVGImgString(img[1]) : ``) +
            ServerMapTemplate.getServiceTypeSVGImgString(img[0]) +
            ServerMapTemplate.getInstanceCountTextString(instanceCount) +
            `</svg>`;
    }
}
