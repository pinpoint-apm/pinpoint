import React from 'react';
import { render, screen } from '@testing-library/react';
import { ApplicationType } from '@pinpoint-fe/ui/src/constants';

const mockIsDefaultService = jest.fn();

type ChildrenProps = { children?: React.ReactNode };
type ItemChildProps = { itemChild?: (application: ApplicationType) => React.ReactNode };

jest.mock('@pinpoint-fe/ui/src/hooks', () => ({
  useIsDefaultService: () => mockIsDefaultService(),
}));

jest.mock('@pinpoint-fe/ui/src/lib', () => ({
  useToast: () => ({ toast: jest.fn() }),
}));

jest.mock('i18next', () => ({
  t: (key: string) => key,
}));

// 별(★)은 아이콘이라 텍스트로 찾을 수 없다. 개수를 셀 수 있게 표식만 남긴다.
jest.mock('react-icons/lu', () => ({
  LuStar: () => <span data-testid="star" />,
  LuStarOff: () => <span data-testid="star-off" />,
}));

jest.mock('./ServerIcon', () => ({
  ServerIcon: () => null,
}));

jest.mock('../ui/toaster', () => ({
  Toaster: () => null,
}));

// 팝오버는 열림 상태를 관리할 뿐이라 이 테스트의 관심사가 아니다. 내용은 항상 그린다.
jest.mock('../ui', () => ({
  Popover: ({ children }: ChildrenProps) => <div>{children}</div>,
  PopoverTrigger: ({ children }: ChildrenProps) => <div>{children}</div>,
  PopoverContent: ({ children }: ChildrenProps) => <div>{children}</div>,
  PopoverClose: 'div',
  Separator: () => <hr />,
}));

jest.mock('../VirtualList', () => ({
  ListItemSkeleton: () => null,
  VirtualSearchList: ({
    children,
  }: {
    children: (props: { filterKeyword: string }) => React.ReactNode;
  }) => <div>{children({ filterKeyword: '' })}</div>,
}));

// 가상 목록은 화면 크기에 기대므로, 받은 항목을 그대로 펼치는 것으로 대신한다.
jest.mock('./ApplicationList', () => ({
  ApplicationItem: ({ applicationName }: ApplicationType) => <span>{applicationName}</span>,
  ApplicationVirtualList: ({ list, itemChild }: ItemChildProps & { list?: ApplicationType[] }) => (
    <div>
      {(list ?? []).map((application) => (
        <div key={application.applicationName}>{itemChild?.(application)}</div>
      ))}
    </div>
  ),
  ApplicationList: ({ itemChild }: ItemChildProps) => (
    <div>
      {itemChild?.({ applicationName: 'app-in-list', serviceType: 'SPRING_BOOT', code: 1210 })}
    </div>
  ),
}));

import { ApplicationCombinedListForCommon } from './ApplicationCombinedListForCommon';

const favoriteApplication: ApplicationType = {
  applicationName: 'favorite-app',
  serviceType: 'SPRING_BOOT',
  code: 1210,
};

const selectedApplication: ApplicationType = {
  applicationName: 'selected-app',
  serviceType: 'SPRING_BOOT',
  code: 1210,
};

const renderList = () =>
  render(
    <ApplicationCombinedListForCommon
      favoriteList={[favoriteApplication]}
      selectedApplication={selectedApplication}
    />,
  );

describe('ApplicationCombinedListForCommon favorite list', () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  // DEFAULT service는 기존 동작 그대로다.
  test('shows the favorite list and the stars on the DEFAULT service', () => {
    mockIsDefaultService.mockReturnValue(true);

    renderList();

    expect(screen.queryByText('Favorite List')).not.toBeNull();
    expect(screen.queryByText('favorite-app')).not.toBeNull();
    // 선택된 application, 즐겨찾기 항목, application 목록 항목에 하나씩.
    expect(screen.getAllByTestId('star')).toHaveLength(3);
  });

  // 즐겨찾기는 사용자 단위로만 저장되어 service 개념이 없다. 비DEFAULT service에서는
  // 그 service에 없는 application이 섞여 나오므로 목록도 별 버튼도 노출하지 않는다.
  test('hides the favorite list and every star on a non-DEFAULT service', () => {
    mockIsDefaultService.mockReturnValue(false);

    renderList();

    expect(screen.queryByText('Favorite List')).toBeNull();
    expect(screen.queryByText('favorite-app')).toBeNull();
    expect(screen.queryAllByTestId('star')).toHaveLength(0);
  });

  // application 목록 자체는 두 경우 모두 그대로 보여야 한다.
  test('keeps the application list on a non-DEFAULT service', () => {
    mockIsDefaultService.mockReturnValue(false);

    renderList();

    expect(screen.queryByText('Application List')).not.toBeNull();
    expect(screen.queryByText('app-in-list')).not.toBeNull();
  });
});
