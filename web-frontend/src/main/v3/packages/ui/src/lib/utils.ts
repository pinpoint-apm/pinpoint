import { type ClassValue, clsx } from 'clsx';
import { extendTailwindMerge } from 'tailwind-merge';
import { zIndexLayers } from '../constants/zIndexLayers';

/**
 * tailwind-merge 는 설정의 이름 붙은 층(`z-popup`)을 모른다. 알려 주지 않으면 `z-popup` 위에
 * `z-toast` 를 넘겨도 둘 다 남아 CSS 순서로 승패가 갈린다.
 */
const twMerge = extendTailwindMerge({
  extend: {
    classGroups: {
      z: [{ z: Object.keys(zIndexLayers) }],
    },
  },
});

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}
