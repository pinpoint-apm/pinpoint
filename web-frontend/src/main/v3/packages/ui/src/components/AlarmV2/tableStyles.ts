// Rules and Templates hand-roll their tables (DataTable has no row expansion); these keep them
// looking like DataTable. Tailwind only generates class names it can read literally.

export const TABLE_HEADER_CELLS =
  '[&_th]:h-10 [&_th]:bg-secondary/50 [&_th]:px-2 [&_th]:text-left [&_th]:align-middle [&_th]:font-medium [&_th]:text-muted-foreground';

export const TABLE_HEADER_ROW =
  'flex h-10 items-center gap-4 border-b bg-secondary/50 px-2 text-sm font-medium text-muted-foreground';

// Below this the fixed columns stop shrinking and Name/Conditions collapse into each other.
export const TABLE_MIN_WIDTH = 'min-w-[56rem]';

export const TABLE_CELL = 'p-2';

// An overlay rather than a border, so child rows keep the header's column widths.
export const EXPANSION_ACCENT =
  "relative before:absolute before:inset-y-0 before:left-0 before:w-[3px] before:bg-slate-200 before:content-['']";

// Must stay the same width, so rows without a chevron line up with rows that have one.
export const CHEVRON = 'size-4 shrink-0 text-muted-foreground transition-transform';
export const CHEVRON_SPACER = 'size-4 shrink-0';
