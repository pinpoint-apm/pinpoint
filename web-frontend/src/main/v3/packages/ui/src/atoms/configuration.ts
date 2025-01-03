import { atom } from 'jotai';
import { Configuration } from '@pinpoint-fe/ui/constants';

export const configurationAtom = atom<Configuration | undefined>(undefined);
