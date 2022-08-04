/* eslint-disable @next/next/no-img-element */
import React from 'react';
import { ComponentStory, ComponentMeta } from '@storybook/react';

import ApplicationSelector from './ApplicationSelector';
import ApplicationList from './ApplicationList';
import ApplicationIcon from './ApplicationIcon';
import { ApplicationType } from './types';

export default {
  title: 'PINPOINT/Component/BASE/ApplicationSelector',
  component: ApplicationSelector,
  argTypes: {
    backgroundColor: { control: 'color' },
  },
} as ComponentMeta<typeof ApplicationSelector>;

const Template: ComponentStory<typeof ApplicationSelector> = (args) => {
  const [ keyword, setKeyword ] = React.useState('');
  const [ application, setApplication ] = React.useState<ApplicationType>();

  return (
    <ApplicationSelector 
      selectedApplication={application && (
        <>
          <ApplicationIcon 
            src={`/assets/img/icons/${application.serviceType}.png`} 
          />
          {application.applicationName}
        </>
      )}
      onChangeInput={({ input }) => setKeyword(input)}
      {...args}
    >
      <ApplicationList.Container title={'Favorite List'}>
        
      </ApplicationList.Container>
      <ApplicationList.Container title={'Application List'}>
        <ApplicationList.List 
          {...{data: mockData, filterKeyword: keyword}}
        >
          {(props) => (
            <ApplicationList.Item
              {...props}
              onClick={(param) => setApplication(param.application)}
              icon={(
                <ApplicationIcon
                  src={`/assets/img/icons/${props.data[props.index].serviceType}.png`}  
                />
              )}  
            />
          )}
        </ApplicationList.List>
      </ApplicationList.Container>
    </ApplicationSelector>
  )
};

export const Default = Template.bind({});
Default.args = {
};

const mockData = [
  {
      "applicationName": "-PAPAGO_DOC_TRANSLATE-",
      "serviceType": "STAND_ALONE",
      "code": 1000
  },
  {
      "applicationName": "-moment-stat",
      "serviceType": "SPRING_BOOT",
      "code": 1210
  },
  {
      "applicationName": "ACL-PORTAL-DEV",
      "serviceType": "SPRING_BOOT",
      "code": 1210
  },
  {
      "applicationName": "ACL-PORTAL-DEVI",
      "serviceType": "SPRING_BOOT",
      "code": 1210
  },
  {
      "applicationName": "AILLIS-API-BETA",
      "serviceType": "TOMCAT",
      "code": 1010
  },
  {
      "applicationName": "AILLIS-WEB-BETA",
      "serviceType": "TOMCAT",
      "code": 1010
  },
  {
      "applicationName": "ALBUM-API-BETA",
      "serviceType": "SPRING_BOOT",
      "code": 1210
  },
  {
      "applicationName": "ALBUM-BATCH-BETA",
      "serviceType": "SPRING_BOOT",
      "code": 1210
  },
  {
      "applicationName": "ALBUM-CONSUMER-BETA",
      "serviceType": "SPRING_BOOT",
      "code": 1210
  },
  {
      "applicationName": "ALBUM-INTERNAL-BETA",
      "serviceType": "SPRING_BOOT",
      "code": 1210
  },
  {
      "applicationName": "ALPHA-JR-SCHOOL-ADMIN",
      "serviceType": "SPRING_BOOT",
      "code": 1210
  },
  {
      "applicationName": "ALPHA-NSHORTURL-API",
      "serviceType": "UNDEFINED",
      "code": -1
  },
  {
      "applicationName": "ALPHA_BZC_PC_ADMIN",
      "serviceType": "SPRING_BOOT",
      "code": 1210
  },
  {
      "applicationName": "ALPHA_CLOVALAMP_ADMIN",
      "serviceType": "SPRING_BOOT",
      "code": 1210
  },
  {
      "applicationName": "ALPHA_CMT_ADMIN",
      "serviceType": "SPRING_BOOT",
      "code": 1210
  },
  {
      "applicationName": "ALPHA_CMT_BATCH",
      "serviceType": "SPRING_BOOT",
      "code": 1210
  },
  {
      "applicationName": "ALPHA_DOCKER_SECURITY",
      "serviceType": "TOMCAT",
      "code": 1010
  },
  {
      "applicationName": "ALPHA_DOCKER_SECURITY",
      "serviceType": "SPRING_BOOT",
      "code": 1210
  }
]