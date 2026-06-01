import { atomWithStorage } from 'jotai/utils';

export const DEFAULT_SERVICE = 'DEFAULT';

export const RESERVED_SERVICE_NAMES = ['DEFAULT', 'TEST', 'ERROR', 'UNKNOWN', 'NULL'];

export const isReservedServiceName = (name: string) =>
  RESERVED_SERVICE_NAMES.includes(name.toUpperCase());

export const selectedServiceAtom = atomWithStorage<string>('selectedService', DEFAULT_SERVICE);
