declare type DeepNonNullable<T> = {
  [P in keyof T]-?: NonNullable<T[P]>;
}