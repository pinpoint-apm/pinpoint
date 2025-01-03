export namespace ConfigInstallationInfo {
  export type Response = {
    code: number;
    message: {
      downloadUrl: string;
      installationArgument: string;
      version: string;
    };
  };
}
