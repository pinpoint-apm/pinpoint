import { atom } from 'jotai';

export const chartsBoardSizesAtom = atom<[string | number, number]>(['auto', 500]);
