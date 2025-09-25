import { createRequire } from 'node:module';
import { dirname, join } from 'node:path';
import type { StorybookConfig } from '@storybook/react-vite';
import { mergeConfig } from 'vite';

const require = createRequire(import.meta.url);

const config: StorybookConfig = {
  stories: ['../src/**/*.mdx', '../src/**/*.stories.@(js|jsx|mjs|ts|tsx)'],

  addons: [
    getAbsolutePath('@storybook/addon-links'),
    getAbsolutePath('@storybook/addon-onboarding'),
    getAbsolutePath('@storybook/addon-docs'),
    getAbsolutePath('@storybook/addon-styling'),
  ],

  core: {
    builder: getAbsolutePath('@storybook/builder-vite'),
  },

  async viteFinal(config) {
    // Merge custom configuration into the default config
    return mergeConfig(config, {
      optimizeDeps: {
        include: ['storybook-dark-mode'],
      },
      build: {
        rollupOptions: {
          output: {
            // 동적으로 가져오는 모듈을 정적으로 빌드
            inlineDynamicImports: true,
          },
        },
      },
    });
  },

  framework: {
    name: getAbsolutePath('@storybook/react-vite'),
    options: {},
  },

  staticDirs: ['../public'],

  managerHead: (head) => `
    ${head}
    <link rel="icon" type="image/png" href="/rich.png" />
  `,
};
export default config;

function getAbsolutePath(value: string): any {
  return dirname(require.resolve(join(value, 'package.json')));
}
