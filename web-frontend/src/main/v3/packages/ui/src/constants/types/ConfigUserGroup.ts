export namespace ConfigUserGroup {
  // eslint-disable-next-line @typescript-eslint/ban-types
  export type Parameters = {} | { userId: string } | { userGroupId: string };
  export type Body = {
    id: string;
    userId: string;
  };

  export type Response = UserGroup[];
  export interface UserGroup {
    id: string;
    number: string;
  }
}
