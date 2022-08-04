import React from 'react';
import { css } from '@emotion/react';
import styled from '@emotion/styled';
import { FixedSizeList, FixedSizeListProps, ListChildComponentProps } from 'react-window';
import { FaRegStar, FaStar } from 'react-icons/fa';

import { StyleFlexVCentered, StyleFlexVHCentered } from '../Styled/styles';
import { ApplicationType } from './types';

export type ItemClickHandlerType = ({ application }: { application: ApplicationType}) => void

export interface ItemProps extends ListChildComponentProps<ApplicationType[]> {
  icon: React.ReactNode;
  onClick: ItemClickHandlerType;
  onClickFavorite?: ItemClickHandlerType;
  favoriteList?: ApplicationType[];
}

const Item = ({
  data, index, style, icon, 
  onClick,
  onClickFavorite,
  favoriteList,
}: ItemProps) => {
  const application = data[index]
  const { applicationName } = application;

  function handleClickItem () {
    onClick?.({ application })
  }

  function handleClickFavorite (event: React.MouseEvent) {
    event.stopPropagation();
    onClickFavorite?.({ application })
  }
  
  return (
    <StyledHoveredItem 
      style={style}
      onClick={handleClickItem}
    >
      {icon}
      {applicationName}
      {onClickFavorite && (
        <StyledFavoriteIconWrapper onClick={handleClickFavorite}>
        {favoriteList?.find(favorite => favorite.applicationName === applicationName) 
          ? <FaStar fill='var(--secondary)' /> 
          : <FaRegStar />
        }
        </StyledFavoriteIconWrapper>
      )}
    </StyledHoveredItem>
  );
}

export interface ApplicationListProps extends Omit<FixedSizeListProps<ApplicationType[]>, 'itemCount' | 'height' | 'itemSize' | 'width'> {
  data?: ApplicationType[];
  maxHeight?: number;
  itemHeight?: number;
  displayDataCount?: number;
  filterKeyword?: string;
}

const List: React.FC<ApplicationListProps> = ({
  data,
  maxHeight = 300,
  itemHeight = 42,
  displayDataCount = 8,
  filterKeyword = '',
  children,
  ...props
}) => {
  const filteredData = getFilteredList();

  function getFilteredList() {
    if (filterKeyword) {
      return data?.filter(({ applicationName }) => {
        return new RegExp(filterKeyword, 'i').test(applicationName);
      })
    }
    return data; 
  }
  
  function getHeight() {
    const dataCount = filteredData?.length;
    
    if (dataCount) {
      return dataCount > displayDataCount ? maxHeight : dataCount * itemHeight;
    }
    return itemHeight;
  }

  return (
    <>
    {filteredData?.length! > 0 ? (
      <FixedSizeList
        width={'100%'}
        height={getHeight()}
        itemCount={filteredData?.length || 0}
        itemSize={42}
        itemData={filteredData}
        {...props}
      >
        {children}
      </FixedSizeList>
    ): (
      <EmptyList>
        Cannot find application.
      </EmptyList>  
    )}
    </>
  )
}

export interface ApplicationListContainerProps {
  title: string;
  className?: string;
  children?: React.ReactNode | React.ReactNode[];
  emptyChildren?: React.ReactNode,
}

const Container = ({
  title,
  className,
  children,
  emptyChildren, 
}: ApplicationListContainerProps) => {
  return (
    <div className={className}>
      <StyledTitle>{title}</StyledTitle>
      <div>
        {children || <EmptyList {...{children: emptyChildren}} />}
      </div>
    </div>
  )
}

export default {
  List,
  Container,
  Item,
}

const EmptyList = ({
  children = `We couldn't find anything.`
}: {
  children?: React.ReactNode
}) => {
  return (
    <StyledItem>
      {children}
    </StyledItem>
  )
}

const StyledTitle = styled.div`
  font-weight: bold;
  padding: 12px 15px 15px 15px;
`

export const StyledItem = styled.div`
  position: relative;
  font-size: 0.93em;
  padding: 12px 15px 12px 20px;
  margin-bottom: 1px;
`

const StyledHoveredItem = styled(StyledItem)<{disableHover?: boolean}>`
  ${StyleFlexVCentered};
  cursor: pointer;
  gap: 10px;

  :hover {
    ${({ disableHover }) => {
      return {
        // backgroundColor: 'red',
      }
    }}
  }
`

const StyledFavoriteIconWrapper = styled.div`
  ${StyleFlexVHCentered};
  position: absolute;
  width: 32px;
  height: 32px;
  right: 15px;
  color: var(--text-secondary);

  &:hover {
    color: var(--text-primary);
  }
`