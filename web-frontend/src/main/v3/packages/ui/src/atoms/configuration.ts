import { atom } from 'jotai';
import { Configuration } from '@pinpoint-fe/ui/src/constants';

export const configurationAtom = atom<Configuration | undefined>(undefined);
