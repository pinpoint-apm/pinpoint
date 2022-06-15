import { css } from '@emotion/react';
import styled from '@emotion/styled';

export const CSSFlexVCentered = css`
  display: flex;
  align-items: center;
`

export const CSSFlexVHCentered = css`
  display: flex;
  align-items: center;
  justify-content: center;
`

export const StyledFlexVCenteredDiv = styled.div`
  ${CSSFlexVCentered}
`

export const StyledFlexVHCenteredDiv = styled.div`
  ${CSSFlexVHCentered}
`