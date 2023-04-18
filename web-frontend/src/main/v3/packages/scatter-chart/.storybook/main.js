const path = require('path');

module.exports = {
  "stories": [
    "../src/stories/**/*.stories.mdx",
    "../src/stories/**/*.stories.@(js|jsx|ts|tsx)"
  ],
  "addons": [
    "@storybook/addon-links",
    "@storybook/addon-essentials",
    "@storybook/addon-interactions",
    "storybook-css-modules",
  ],
  "framework": "@storybook/html",
  "core": {
    "builder": "@storybook/builder-webpack5"
  },
  // webpackFinal: async (config, { configType }) => {
  //   // `configType` has a value of 'DEVELOPMENT' or 'PRODUCTION'
  //   // You can change the configuration based on that.
  //   // 'PRODUCTION' is used when building the static version of storybook.

  //   // Make whatever fine-grained changes you need
  //   config.module.rules.push({
  //     test: /\.css$/,
  //     use: ['style-loader', 'css-loader'],
  //     include: path.resolve(__dirname, '../'),
  //   });
    
  //   return config;
  // },
}