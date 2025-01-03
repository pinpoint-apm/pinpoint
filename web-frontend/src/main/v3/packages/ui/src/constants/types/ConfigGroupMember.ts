export namespace ConfigGroupMember {
  export type Parameters = { userGroupId: string };
  export type Body = {
    memberId: string;
    userGroupId: string;
  };

  export type Response = GroupMember[];
  export interface GroupMember {
    department: string;
    memberId: string;
    name: string;
    number: string;
    userGroupId: string;
  }
}
