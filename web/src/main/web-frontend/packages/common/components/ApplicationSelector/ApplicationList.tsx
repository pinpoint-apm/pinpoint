import { css } from '@emotion/react';
import styled from '@emotion/styled';
import React from 'react';
import { StyleFlexVCentered } from '../Styled/styles';

export interface Application {
  code: number;
  serviceType: string;
  applicationName: string;
}

export interface ApplicationItemProps {
  icon: React.ReactNode;
  application: Application;
}

export const Item = ({
  icon,
  application,
}: ApplicationItemProps) => {
  const { applicationName } = application;
  return (
    <StyledItem>
      {icon}
      {applicationName}
    </StyledItem>
  );
};

export interface ApplicationItemContainerProps {
  title: string;
  className?: string;
  children?: React.ReactNode | React.ReactNode[];
}

export const List = ({
  title,
  className,
  children, 
}: ApplicationItemContainerProps) => {
  return (
    <div className={className}>
      <StyledTitle>{title}</StyledTitle>
      <StyledList id="application" role="listbox">
        {children || <EmptyList />}
      </StyledList>
    </div>
  )
}

export const EmptyList = () => {
  return (
    <div css={StyleItem}>
      We couldn't find anything.
    </div>
  )
}

const StyledTitle = styled.div`
  font-weight: bold;
  padding: 12px 15px;
`

const StyledList = styled.ul`
  padding: 5px;
`

const StyleItem = css`
  font-size: 0.93rem;
  padding: 12px 15px 12px 20px;
  margin-bottom: 1px;
`

const StyledItem = styled.li`
  ${StyleItem};
  ${StyleFlexVCentered};
  cursor: pointer;
  gap: 10px;
`