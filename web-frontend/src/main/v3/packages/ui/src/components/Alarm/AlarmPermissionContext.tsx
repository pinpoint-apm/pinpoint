import React from 'react';

export type AlarmPermissionContextType = {
  create: boolean;
  edit: boolean;
  delete: boolean;
};

export const defaultAlarmPermissioContext = { create: true, edit: true, delete: true };

export const AlarmPermissionContext = React.createContext<{
  permissionContext: AlarmPermissionContextType;
  setPermissionContext: React.Dispatch<React.SetStateAction<AlarmPermissionContextType>>;
}>({
  permissionContext: defaultAlarmPermissioContext,
  setPermissionContext: () => {},
});
