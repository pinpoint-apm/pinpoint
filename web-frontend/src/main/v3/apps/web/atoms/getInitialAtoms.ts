import { Atom } from 'jotai'
type AtomTuple<T> = [Atom<T>, T];

export const getInitialAtoms = (props: any): Array<AtomTuple<unknown>> => {
  return [
    // [someAtom, props],
  ]
}
 