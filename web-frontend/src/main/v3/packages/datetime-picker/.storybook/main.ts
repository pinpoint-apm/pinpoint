import type { StorybookConfig } from '@storybook/react-vite';
import { mergeConfig } from 'vite';

const config: StorybookConfig = {
  stories: ['../src/**/*.mdx', '../src/**/*.stories.@(js|jsx|mjs|ts|tsx)'],
  addons: [
    '@storybook/addon-links',
    '@storybook/addon-essentials',
    '@storybook/addon-onboarding',
    '@storybook/addon-interactions',
    {
      name: '@storybook/addon-styling',
      options: {},
    },
  ],
  core: {
    builder: '@storybook/builder-vite',
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
    name: '@storybook/react-vite',
    options: {},
  },
  staticDirs: ['../public'],
  docs: {
    autodocs: 'tag',
  },
  managerHead: (head) => `
    ${head}
    <link rel="icon" type="image/png" href="/rich.png" />
  `,
};
export default config;
