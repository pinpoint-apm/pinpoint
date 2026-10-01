import React from 'react';

export type AppContextType = {
  seamToken: string;
  timeZone: string;
};

const AppContext = React.createContext<{
  appContext: AppContextType;
}>({
  appContext: { seamToken: '', timeZone: '' },
});

export default AppContext;
