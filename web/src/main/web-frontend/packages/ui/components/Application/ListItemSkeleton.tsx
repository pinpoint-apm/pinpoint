import 'react-loading-skeleton/dist/skeleton.css';

import React from 'react';
import Skeleton from 'react-loading-skeleton';

import { StyledItem } from './ApplicationList';

export interface ListItemSkeletonProps {
  count?: number;
}

const ListItemSkeleton = ({
  count = 3
}: ListItemSkeletonProps) => {
  return (
    <>
    {[...Array(count)].map((_, i) => 
      <StyledItem key={i}><Skeleton /></StyledItem>
    )}
    </>
  );
};

export default ListItemSkeleton;
