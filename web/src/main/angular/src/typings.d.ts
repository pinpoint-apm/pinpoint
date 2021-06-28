// Typings reference file, you can add your own global typings here
// https://www.typescriptlang.org/docs/handbook/writing-declaration-files.html
declare var ga: Function;
declare var module: NodeModule;
declare var intlTelInputGlobals: any;
declare module '*.json' {
    const value: any;
    export default value;
  }
interface NodeModule {
    id: string;
}
