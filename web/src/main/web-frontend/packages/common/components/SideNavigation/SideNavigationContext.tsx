import React from 'react';

const SideNavigationContext = React.createContext<{
  small: boolean,
  setSmall: React.Dispatch<React.SetStateAction<boolean>>
}>({
  small: false,
  setSmall: () => {},
})

export default SideNavigationContext;