import resolveConfig from 'tailwindcss/resolveConfig';
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore
import tailwindConfig from '../../tailwind.config';

export const fullConfigs = resolveConfig(tailwindConfig);

export const colors = fullConfigs.theme.colors;
export const screens = fullConfigs.theme.screens;
