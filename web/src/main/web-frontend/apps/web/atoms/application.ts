import { atom } from 'jotai';
import { ApplicationType } from '@pinpoint-fe/common/components/Application/types';

// TODO set application directly from URL path
export const applicationAtom = atom<ApplicationType | null>(null);

