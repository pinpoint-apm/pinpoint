import { atom } from 'jotai';
import { GetServices } from '@pinpoint-fe/ui/src/constants';

export const servicesAtom = atom<GetServices.Response | undefined>(undefined);

export const isServiceAddSheetOpenAtom = atom<boolean>(false);
