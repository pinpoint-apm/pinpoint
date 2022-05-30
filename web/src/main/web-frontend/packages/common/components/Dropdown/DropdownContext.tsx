import React from 'react';

const DropdownContext = React.createContext<{
  show: boolean,
  setShow?: React.Dispatch<React.SetStateAction<boolean>>
}>({
  show: false
})

export default DropdownContext;