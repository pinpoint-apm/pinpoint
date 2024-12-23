import { atom } from 'jotai';
import { Configuration } from '@pinpoint-fe/constants';

export const configurationAtom = atom<Configuration | undefined>(undefined);
