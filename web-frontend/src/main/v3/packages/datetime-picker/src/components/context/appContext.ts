import React from 'react';

export type AppContextType = {
  seamToken: string;
  timeZone: string;
};

const AppContext = React.createContext<{
  appContext: AppContextType;
  setAppContext: React.Dispatch<React.SetStateAction<AppContextType>>;
}>({
  appContext: { seamToken: '', timeZone: '' },
  setAppContext: () => {},
});

export default AppContext;
