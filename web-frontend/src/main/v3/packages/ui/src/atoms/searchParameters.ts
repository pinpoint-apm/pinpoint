import { atom } from 'jotai';
import { ApplicationType } from '@pinpoint-fe/ui/src/constants';

export const searchParametersAtom = atom<{
  application: ApplicationType;
  searchParameters: {
    [k: string]: string;
  };
}>({
  application: {} as ApplicationType,
  searchParameters: {},
});
