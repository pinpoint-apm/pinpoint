import React from 'react';
import styled from '@emotion/styled';

import { Dropdown } from '../Dropdown';
import { StyleFlexVCentered, StyleFlexVHCentered } from '../Styled/styles';
import SideNavigationContext from './SideNavigationContext';
import { NextLink } from '../NextLink/NextLink';

export interface NavItemProps {
  children?: React.ReactNode | React.ReactNode[];
  icon?: React.ReactNode;
  href?: string;
  onClick?: () => void;
}

const Item = ({
  icon,
  children,
  href = '',
  onClick,
}: NavItemProps) => {
  const { small } = React.useContext(SideNavigationContext);

  return (
    <Dropdown hoverable={small}>
      {href ? (
        <StyledWrapLink href={href}>
          <StyledTrigger disable>
            <StyledIconWrapper {...{ small }}>{icon}</StyledIconWrapper>
            {!small && children}
          </StyledTrigger>
        </StyledWrapLink>
      ): (
        <StyledTrigger disable>
          <StyledIconWrapper {...{ small }}>{icon}</StyledIconWrapper>
          {!small && children}
        </StyledTrigger>
      )}
      <StyledMenuContainer>
        {small && (
          <StyledMenuHeader>
            {children}
          </StyledMenuHeader>
        )}
      </StyledMenuContainer>
    </Dropdown>
  );
};

const StyledIconWrapper = styled.div<{ small: boolean; }>`
  ${StyleFlexVHCentered}
  width: 50px;
  font-size: 1.1em;
  ${({ small }) => {
    if ( small ) {
      return {
        fontSize: '1.2em',
      }
    }
  }}
`

export interface NavMenuProps extends NavItemProps {
  title: string;
  className?: string;
}

const Menu = ({
  icon,
  title,
  className,
  children,
}: NavMenuProps) => {
  const { small } = React.useContext(SideNavigationContext);
  const menuRef = React.useRef<HTMLDivElement>(null);

  React.useEffect(() => {
    if (menuRef.current) {
      menuRef.current.style.top = '0px';
    }
  }, [ small ])

  function handleDropdownToggle({ open }: { open: boolean }) {
    if (menuRef.current && open) {
      const menuContainerRect = menuRef.current.getBoundingClientRect();
      const isYOverFlow = menuContainerRect.y + menuContainerRect.height > window.innerHeight;
              
      if (isYOverFlow) {
        menuRef.current.style.top = `${-menuContainerRect.height + 40}px`;
      }
    } 
  }

  return (
    <Dropdown 
      hoverable
      className={className} 
      onChange={handleDropdownToggle}
    >
      <StyledTrigger>
        <StyledIconWrapper {...{ small }}>{icon}</StyledIconWrapper>
        {!small && title}
      </StyledTrigger>
      <StyledMenuContainer ref={menuRef}>
        {small && (
          <StyledMenuHeader>
            {title}
          </StyledMenuHeader>
        )}
        {children}
      </StyledMenuContainer>
    </Dropdown>
  );
};

export interface NavMenuItemProps extends NavItemProps { }

const MenuItem = ({
  children,
  href,
}: NavMenuItemProps) => {
  if (href) {
    return (
      <StyledWrapLink href={href}>
        <StyledMenuItem>
          {children}
        </StyledMenuItem>
      </StyledWrapLink>
    )
  } else {
    return (
      <StyledMenuItem>
        {children}
      </StyledMenuItem>
    )
  }
}

const Divider = () => <StyledDivider />;

export default {
  Item,
  Menu,
  MenuItem,
  Divider,
};

const StyledDivider = styled.div`
  height: 0;
  padding: 0;
  border-bottom: 1px solid var(--border-primary-darker);
`

const StyledWrapLink = styled(NextLink)`
  display: block;
  text-decoration: none;
`

const StyledTrigger = styled(Dropdown.Trigger)`
  ${StyleFlexVCentered}
  height: 40px;
  cursor: pointer;

  .active, &:hover {
    font-weight: bold;
    background-color: var(--blue-800);
  }
`

const StyledMenuContainer = styled(Dropdown.Content)`
  position: absolute;
  top: 0;
  left: 100%;
  width: 200px;
  box-shadow: rgb(0 0 0 / 25%) 3px 3px 10px;
`

const StyledMenuHeader = styled.div`
  ${StyleFlexVCentered}
  height: 40px;
  font-weight: bold;
  background-color: var(--blue-900);
  padding-left: 15px;
`

const StyledMenuItem = styled.div`
  ${StyleFlexVCentered}
  height: 40px;
  padding-left: 15px;
  background-color: var(--snb-child-link-background);

  &:hover {
    font-weight: bold;
    background-color: var(--snb-link-item-hover);
  }
`

