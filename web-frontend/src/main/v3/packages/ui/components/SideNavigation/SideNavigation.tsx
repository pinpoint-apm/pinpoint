/* eslint-disable @next/next/no-img-element */
import React from 'react';
import styled from '@emotion/styled';
import { FaMinus, FaPlus } from 'react-icons/fa';

import SideNavigationContext from './SideNavigationContext';
import { NextLink } from '../NextLink/NextLink';
import { StyleFlexVHCentered } from '../Styled/styles';

export interface SideNavigationContainerProps {
  children: React.ReactNode | React.ReactNode[];
  isSmall?: boolean;
  onClickScaleButton: ({ small }: { small: boolean }) => void;
}

export const SideNavigation = ({
  children,
  isSmall = false,
  onClickScaleButton,
}: SideNavigationContainerProps) => {
  const [small, setSmall] = React.useState(isSmall);

  function handleClickScaleButton() {
    const changeScale = !small;
    setSmall(changeScale);
    onClickScaleButton({ small: changeScale });
  }

  return (
    <SideNavigationContext.Provider value={{ small, setSmall: React.useMemo(() => setSmall, [setSmall]) }}>
      <StyledContainer {...{ small }}>
        <StyledHeaderContainer {...{ small }}>
          <NextLink href={'/serverMap'} replace>
            <img
              src={small
                ? '/assets/img/mini-logo.png'
                : '/assets/img/logo.png'
              }
              alt={'pinpoint-logo'}
            />
          </NextLink>
          <StyledScaleButton
            {...{ small }}
            className='scale-button-wrapper'
            onClick={handleClickScaleButton}
          >
            {small
              ? <FaPlus />
              : <FaMinus />
            }
          </StyledScaleButton>
        </StyledHeaderContainer>
        {children}
      </StyledContainer>
    </SideNavigationContext.Provider>
  );
};

const StyledHeaderContainer = styled.div<{ small?: boolean; }>`
  position: relative;
  padding: 30px 0 40px 20px;
  ${({ small }) => {
    if (small) {
      return {
        paddingLeft: '0px',
        textAlign: 'center',
      }
    }
  }}

  &:hover {
    background-color: var(--blue-700);
  }

  &:hover > .scale-button-wrapper {
    display: flex;
  }
`

const StyledScaleButton = styled.button<{ small?: boolean; }>`
  ${StyleFlexVHCentered};
  position: absolute;
  display: none;
  top: 10px;
  right: 10px;
  width: 24px;
  height: 24px;
  cursor: pointer;
  border-radius: 5px;

  ${({ small }) => {
    if (small) {
      return {
        top: 'auto',
        bottom: 2,
        right: 12,
      }
    }
  }}

  &:hover {
    font-weight: bold;
    background-color: var(--blue-900);
  }
`

const StyledContainer = styled.nav<{ small?: boolean }>`
  --snb-background: linear-gradient(170deg, var(--blue-500), var(--blue-900));
  --snb-border: var(--header-border);
  --snb-text: var(--text-knockout);
  --snb-logo-hover: var(--blue-700);
  --snb-scale-button-hover: var(--blue-900);
  --snb-link-item-hover: var(--blue-800);
  --snb-child-link-background: var(--blue-700);
  --snb-child-link-item-hover: var(--blue-900);
  --snb-child-link-title: var(--blue-900);
  --snb-theme-hover: var(--snb-child-link-background);
  
  .dark-mode {
    --snb-background: linear-gradient(170deg, var(--background-primary-darker), var(--background-primary));
    --snb-border: var(--border-primary-lighter);
    --snb-text: var(--text-primary);
    --snb-logo-hover: var(--blue-grey-50);
    --snb-scale-button-hover: var(--blue-grey-100);
    --snb-link-item-hover: var(--grey-300);
    --snb-child-link-background: var(--background-layer);
    --snb-child-link-item-hover: var(--grey-400);
    --snb-child-link-title: var(--blue-grey-100);
    --snb-theme-hover: var(--snb-child-link-background);
  }
  
  border-right: 1px solid var(--snb-border);
  display: flex;
  flex-direction: column;
  background: var(--snb-background);
  width: ${({ small }) => small ? '50px' : '200px'};
  height: 100vh;
  padding: 0 0 50px;
  color: var(--snb-text);
  z-index: 99;
`;