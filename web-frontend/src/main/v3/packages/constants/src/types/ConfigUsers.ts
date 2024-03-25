export namespace ConfigUsers {
  export type Parameters = { searchKey: string };
  export type AddBody = User;
  export type RemoveBody = {
    userId: string;
  };

  export type Response = User[];
  export interface User {
    userId: string;
    name: string;
    department?: string;
    phoneCountryCode?: string;
    phoneNumber?: string;
    email?: string;
  }
}
