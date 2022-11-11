
import React from 'react';
import styled from '@emotion/styled';
import { useRouter } from 'next/router';
import { GetServerSideProps } from 'next';
import { format, parse } from 'date-fns';

import { DATE_FORMAT } from '@pinpoint-fe/constants';
import { ApplicationType, DateRangePicker, StyledMainHeader } from '@pinpoint-fe/ui';
import { getParsedDateRange, isValidDateRangeInTwoDays, getApplicationTypeAndName } from '@pinpoint-fe/utils';
import { NextPageWithLayout } from '../../_app';
import { ServerMap }  from '../../../components/ServerMap/ServerMap';
import { getLayoutWithSideNavigation } from '../../../components/Layout/LayoutWithSideNavigation';
import { ApplicationSelector } from '../../../components/ApplicationSelector/ApplicationSelector';

export interface ServerMapActiveProps {
}

const ServerMapActive: NextPageWithLayout<ServerMapActiveProps> = (props: ServerMapActiveProps) => {
  const router = useRouter();
  const query = router.query;
  const applicationQueryParam = query.application;
  const fromQueryParam = query.from;
  const toQueryParam = query.to;
  const application = getApplicationTypeAndName(applicationQueryParam);

  const getServerMapPath = ({ applicationName, serviceType }: ApplicationType, parameters?: { [key: string]: string }) => {
    const queryString = parameters ? `?${new URLSearchParams(parameters).toString()}` : '';

    return `/serverMap/${applicationName}@${serviceType}${queryString}`
  }

  return (
    <StyledContainer>
      <StyledHeader>
        <ApplicationSelector
          application={application}
          onClick={({ application }) => router.push(getServerMapPath(application))}
        />
        {application && (
          <DateRangePicker
            from={fromQueryParam}
            to={toQueryParam}
            onChange={
              ({ formattedDate }) => {
                router.push(getServerMapPath(application, formattedDate))
              }
            } />
        )}
      </StyledHeader>
      <StyledMainContainer>
        <ServerMap 
          application={application}
          dateRange={getParsedDateRange({from: fromQueryParam, to: toQueryParam})}
        />
      </StyledMainContainer>
    </StyledContainer>
  )
}

const StyledContainer = styled.div`
  display: flex;
  flex: 1;
  flex-direction: column;
  height: 100%;
`

const StyledMainContainer = styled.div`
  flex:1;
  display: grid;
  /* grid-template-columns: auto 500px; */
`

const StyledHeader = styled(StyledMainHeader)`
  gap: 10px;
`

ServerMapActive.getLayout = (page) => getLayoutWithSideNavigation(page);

export const getServerSideProps: GetServerSideProps = async ({ query }) => {
  const { application } = query;
  const from = query.from as string;
  const to = query.to as string;

  const basePath = `/serverMap/${application}`;
  const conditions = Object.keys(query).filter((key) => key !== 'application');

  const currentDate = new Date();
  const parsedDateRange = { from: parse(from, DATE_FORMAT, currentDate), to: parse(to, DATE_FORMAT, currentDate) }
  const defaultParsedDateRange = getParsedDateRange({ from, to });
  const defaultFormattedDateRange = {
    from: format(defaultParsedDateRange.from, DATE_FORMAT),
    to: format(defaultParsedDateRange.to, DATE_FORMAT),
  }
  const defaultDatesQueryString = new URLSearchParams(defaultFormattedDateRange).toString();
  const defaultDestination = `${basePath}?${defaultDatesQueryString}`;

  const getResult = ({ props = {}, destination }: { props?: object, destination?: string }) => {
    const redirect = destination ? { redirect: { destination } } : {}

    return {
      ...redirect,
      props: {
        ...props
      },
    }
  }

  if (conditions.length === 0) {
    return getResult({ destination: defaultDestination });
  } else if (conditions.includes('realtime')) {
    if (query.realtime !== 'true') {
      return getResult({ destination: defaultDestination });
    } else if (conditions.filter(condition => condition !== 'realtime').length > 0) {
      // realtime 외 queryparmamters 드랍
      return getResult({ destination: `${basePath}?realtime=true` });
    }
  } else if (conditions.includes('from')) {
    if (conditions.includes('to') && isValidDateRangeInTwoDays(parsedDateRange)) {
      return getResult({});
    } else {
      return getResult({ destination: defaultDestination });
    }
  } else {
    return getResult({ destination: defaultDestination });
  }
  return getResult({});
}

export default ServerMapActive;
