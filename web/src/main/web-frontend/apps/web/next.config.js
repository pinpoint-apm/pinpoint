const withPlugins = require('next-compose-plugins');
const withTM = require('next-transpile-modules');

const TM = withTM([
  '@pinpoint-fe/ui'
])

const nextConfig = {
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: 'http://localhost:8080/:path*',
      },
    ];
  },
}

module.exports = withPlugins([TM], nextConfig);
// module.exports = nextConfig;