import { makeArgs } from './__fixtures__/loaderArgs';
import { alarmRouteLoader } from './alarm';

jest.mock('@pinpoint-fe/ui/src/constants', () => ({
  ...jest.requireActual('@pinpoint-fe/ui/src/constants'),
  BASE_PATH: '/pinpoint',
}));

jest.mock('react-router', () => ({
  ...jest.requireActual('react-router'),
  redirect: (url: string) => ({ __isRedirect: true, url }),
}));

jest.mock('@pinpoint-fe/ui/src/hooks', () => ({
  getConfiguration: jest.fn(() => Promise.resolve({})),
  getRequestService: jest.fn(() => 'selected-svc'),
}));

import { getConfiguration, getRequestService } from '@pinpoint-fe/ui/src/hooks';

describe('alarmRouteLoader', () => {
  afterEach(() => {
    jest.clearAllMocks();
    (getRequestService as jest.Mock).mockReturnValue('selected-svc');
  });

  test('moves a link sent before the service segment to the service of the rule', async () => {
    const result = await alarmRouteLoader(
      makeArgs(
        'http://localhost/config/alarm?ruleId=7&serviceName=shop%2Ffront&applicationName=my+app',
      ),
    );

    expect(result).toEqual({
      __isRedirect: true,
      url: '/config/alarm/shop%2Ffront?ruleId=7&applicationName=my+app',
    });
  });

  test('fills in the current service when the path carries none', async () => {
    const result = await alarmRouteLoader(makeArgs('http://localhost/config/alarm/'));

    expect(result).toEqual({ __isRedirect: true, url: '/config/alarm/selected-svc' });
  });

  test('leaves a path that carries the service as it is', async () => {
    const result = await alarmRouteLoader(makeArgs('http://localhost/config/alarm/svc?ruleId=7'));

    expect(result).toBeNull();
    expect(getConfiguration).not.toHaveBeenCalled();
  });

  test('leaves the path as it is when the service map is off', async () => {
    (getRequestService as jest.Mock).mockReturnValue(undefined);

    const result = await alarmRouteLoader(
      makeArgs('http://localhost/config/alarm?ruleId=7&serviceName=svc'),
    );

    expect(result).toBeNull();
  });

  test('still decides by the stored setting when the configuration cannot be read', async () => {
    (getConfiguration as jest.Mock).mockRejectedValueOnce(new Error('down'));

    const result = await alarmRouteLoader(
      makeArgs('http://localhost/config/alarm?serviceName=svc'),
    );

    expect(result).toEqual({ __isRedirect: true, url: '/config/alarm/svc' });
  });

  test('keeps the query as it was when the link names no service', async () => {
    const result = await alarmRouteLoader(
      makeArgs('http://localhost/config/alarm?view=rule-edit&applicationName=my%20app'),
    );

    expect(result).toEqual({
      __isRedirect: true,
      url: '/config/alarm/selected-svc?view=rule-edit&applicationName=my%20app',
    });
  });

  // The redirect goes without the base path: react-router adds it to a relative redirect.
  test('reads the page after the base path', async () => {
    const result = await alarmRouteLoader(
      makeArgs('http://localhost/pinpoint/config/alarm?serviceName=svc&ruleId=7'),
    );

    expect(result).toEqual({ __isRedirect: true, url: '/config/alarm/svc?ruleId=7' });
  });
});
