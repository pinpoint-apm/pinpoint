// Tailwind 4 가 쓰는 at-rule. 표준 CSS 에 없어서 stylelint 가 모른다.
const TAILWIND_AT_RULES = [
  'apply',
  'config',
  'custom-variant',
  'plugin',
  'reference',
  'source',
  'theme',
  'utility',
  'variant',
];

module.exports = {
  extends: ['stylelint-config-standard'],
  ignoreFiles: ['**/coverage/**', '**/dist/**', '**/storybook-static/**'],
  rules: {
    // 클래스는 BEM(block__element--modifier)이다. react-datepicker 가 내보내는 클래스도 같은 형태다.
    'selector-class-pattern': [
      '^[a-z0-9]*(-[a-z0-9]+)*(__[a-z0-9]+(-[a-z0-9]+)*)?(--[a-z0-9]+(-[a-z0-9]+)*)?$',
      { message: 'Expected class selector to be BEM (block__element--modifier)' },
    ],
    'at-rule-no-unknown': [true, { ignoreAtRules: TAILWIND_AT_RULES }],
    // Tailwind 4 는 `@import 'tailwindcss'` 형태를 쓴다.
    'import-notation': 'string',
    // 브라우저 호환을 위해 의도적으로 남긴 접두사다.
    'property-no-vendor-prefix': null,
    'selector-no-vendor-prefix': null,
    // 기존 스타일시트가 쓰는 색 표기. 손으로 쓴 곳은 legacy(`rgba(0, 0, 0, 0.5)`)이고
    // Tailwind 가 만들어 낸 값을 붙여넣은 곳은 modern(`rgb(59 130 246 / 0.5)`)이라 한쪽으로
    // 강제하면 한쪽을 반드시 고쳐야 한다. 둘 다 유효한 CSS 라 표기는 보지 않는다.
    'color-function-notation': null,
    'color-function-alias-notation': null,
    // 알파값 표기(0.5 / 50%)도 같은 이유로 보지 않는다. 이 패키지는 rich-datetime-picker 를
    // 그대로 복사해 쓰므로, 원본 스타일시트를 고치지 않고 다시 덮어쓸 수 있게 규칙 쪽을 맞춘다.
    'alpha-value-notation': null,
    // `@config` 는 바로 위 `@import` 와 한 묶음이라 빈 줄 없이 붙여 쓴다(src/components/tailwind.css).
    'at-rule-empty-line-before': [
      'always',
      {
        except: ['blockless-after-same-name-blockless', 'first-nested'],
        ignore: ['after-comment'],
        ignoreAtRules: ['config'],
      },
    ],
  },
  overrides: [
    {
      files: ['**/*.scss'],
      extends: ['stylelint-config-standard-scss'],
      rules: {
        'scss/at-rule-no-unknown': [true, { ignoreAtRules: TAILWIND_AT_RULES }],
      },
    },
  ],
};
