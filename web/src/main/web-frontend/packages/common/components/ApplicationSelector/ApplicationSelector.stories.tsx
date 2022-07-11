/* eslint-disable @next/next/no-img-element */
import React from 'react';
import { ComponentStory, ComponentMeta } from '@storybook/react';

import ApplicationSelector from './ApplicationSelector';

export default {
  title: 'PINPOINT/Component/BASE/ApplicationSelector',
  component: ApplicationSelector,
  argTypes: {
    backgroundColor: { control: 'color' },
  },
} as ComponentMeta<typeof ApplicationSelector>;

const Template: ComponentStory<typeof ApplicationSelector> = (args) => (
  <ApplicationSelector {...args}>
    <ApplicationSelector.List title={'Favorite List'}>
      
    </ApplicationSelector.List>
    <ApplicationSelector.List title={'Application List'}>
    {mockData.map((app, i) => {
      return (
        <ApplicationSelector.Item 
          key={i}
          icon={<img 
            src={`/assets/img/icons/${app.serviceType}.png`} 
            width={23}
            height={18}
            alt={'application image'}
          />}
          application={app} 
        />
      )
    })}
    </ApplicationSelector.List>
  </ApplicationSelector>
);

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