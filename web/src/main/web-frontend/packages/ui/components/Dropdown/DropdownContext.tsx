import React from 'react';

const DropdownContext = React.createContext<{
  open: boolean,
  setOpen: React.Dispatch<React.SetStateAction<boolean>>,
}>({
  open: false, 
  setOpen: () => {},
})

export default DropdownContext;