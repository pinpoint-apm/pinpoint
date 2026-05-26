import { atomWithStorage } from 'jotai/utils';

export const DEFAULT_SERVICE = 'DEFAULT';

export const selectedServiceAtom = atomWithStorage<string>('selectedService', DEFAULT_SERVICE);
