module.exports = {
  // parser: 'typescript',
  printWidth: 100,
  tabWidth: 2,
  singleQuote: true,
  trailingComma: 'all',
  semi: true,
  endOfLine: 'auto',
  plugins: ['prettier-plugin-tailwindcss'],
  // Tailwind 4 has no JS config to find the `rdp` prefix and theme in; read them from the CSS entry.
  tailwindStylesheet: './src/components/tailwind.css',
};
