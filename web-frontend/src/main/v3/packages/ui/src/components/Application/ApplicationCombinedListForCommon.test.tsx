import React from 'react';
import { fireEvent, render, screen } from '@testing-library/react';
import { ApplicationType } from '@pinpoint-fe/ui/src/constants';

const mockIsDefaultService = jest.fn();
const mockOnClickApplication = jest.fn();

type ChildrenProps = { children?: React.ReactNode };
type ListMockProps = {
  list?: ApplicationType[];
  itemChild?: (application: ApplicationType) => React.ReactNode;
  getFilteredList?: (filteredList: ApplicationType[]) => void;
  onMouseEnter?: (index: number, application: ApplicationType) => void;
};

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
// 키보드 이벤트는 팝오버 본문에서 받으므로 그 핸들러만 그대로 잇는다.
jest.mock('../ui', () => ({
  Popover: ({ children }: ChildrenProps) => <div>{children}</div>,
  PopoverTrigger: ({ children }: ChildrenProps) => <div>{children}</div>,
  PopoverContent: ({
    children,
    onKeyDown,
    onMouseMove,
  }: ChildrenProps & {
    onKeyDown?: React.KeyboardEventHandler;
    onMouseMove?: React.MouseEventHandler;
  }) => (
    <div data-testid="popover-content" onKeyDown={onKeyDown} onMouseMove={onMouseMove}>
      {children}
    </div>
  ),
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
// 걸러진 결과를 부모에게 돌려주는 것까지 흉내 낸다 — 키보드 선택이 그 결과를 읽기 때문이다.
jest.mock('./ApplicationList', () => {
  const { useEffect } = jest.requireActual('react') as typeof import('react');

  const APPLICATION_LIST: ApplicationType[] = [
    { applicationName: 'app-in-list', serviceType: 'SPRING_BOOT', code: 1210 },
  ];

  const ListMock = ({
    name,
    list,
    itemChild,
    getFilteredList,
    onMouseEnter,
  }: ListMockProps & { name: string }) => {
    useEffect(() => {
      getFilteredList?.(list ?? []);
      // getFilteredList는 매 렌더 새로 만들어지는 함수라 의존성에 넣으면 무한 루프가 된다.
      // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [list]);

    return (
      <div>
        {(list ?? []).map((application, index) => (
          <div
            key={application.applicationName}
            data-testid={`${name}-item`}
            onMouseEnter={() => onMouseEnter?.(index, application)}
          >
            {itemChild?.(application)}
          </div>
        ))}
      </div>
    );
  };

  return {
    ApplicationItem: ({ applicationName }: ApplicationType) => <span>{applicationName}</span>,
    ApplicationVirtualList: (props: ListMockProps) => <ListMock name="favorite" {...props} />,
    ApplicationList: (props: ListMockProps) => (
      <ListMock name="application" {...props} list={APPLICATION_LIST} />
    ),
  };
});

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

// 참조가 매 렌더 바뀌면 목록 mock이 걸러진 결과를 계속 다시 보고하므로 한 번만 만든다.
const FAVORITE_LIST = [favoriteApplication];

// 같은 element 객체를 다시 넘기면 React가 리렌더를 건너뛰므로 호출할 때마다 새로 만든다.
const listElement = () => (
  <ApplicationCombinedListForCommon
    favoriteList={FAVORITE_LIST}
    selectedApplication={selectedApplication}
    onClickApplication={mockOnClickApplication}
  />
);

const getPopoverContent = () => screen.getByTestId('popover-content');

describe('ApplicationCombinedListForCommon favorite list', () => {
  afterEach(() => {
    jest.clearAllMocks();
  });

  // DEFAULT service는 기존 동작 그대로다.
  test('shows the favorite list and the stars on the DEFAULT service', () => {
    mockIsDefaultService.mockReturnValue(true);

    render(listElement());

    expect(screen.queryByText('Favorite List')).not.toBeNull();
    expect(screen.queryByText('favorite-app')).not.toBeNull();
    // 선택된 application, 즐겨찾기 항목, application 목록 항목에 하나씩.
    expect(screen.getAllByTestId('star')).toHaveLength(3);
  });

  // 즐겨찾기는 사용자 단위로만 저장되어 service 개념이 없다. 비DEFAULT service에서는
  // 그 service에 없는 application이 섞여 나오므로 목록도 별 버튼도 노출하지 않는다.
  test('hides the favorite list and every star on a non-DEFAULT service', () => {
    mockIsDefaultService.mockReturnValue(false);

    render(listElement());

    expect(screen.queryByText('Favorite List')).toBeNull();
    expect(screen.queryByText('favorite-app')).toBeNull();
    expect(screen.queryAllByTestId('star')).toHaveLength(0);
  });

  // application 목록 자체는 두 경우 모두 그대로 보여야 한다.
  test('keeps the application list on a non-DEFAULT service', () => {
    mockIsDefaultService.mockReturnValue(false);

    render(listElement());

    expect(screen.queryByText('Application List')).not.toBeNull();
    expect(screen.queryByText('app-in-list')).not.toBeNull();
  });

  // service를 바꿔 즐겨찾기를 감춘 뒤에도 걸러 둔 목록이 남아 있으면, 위쪽 방향키가
  // 화면에 없는 즐겨찾기로 이동하고 Enter가 그것을 고른다. 감출 때 함께 지우는 이유다.
  test('does not let a hidden favorite be selected with the keyboard', () => {
    mockIsDefaultService.mockReturnValue(true);
    const { rerender } = render(listElement());
    expect(screen.queryByTestId('favorite-item')).not.toBeNull();

    mockIsDefaultService.mockReturnValue(false);
    rerender(listElement());

    fireEvent.keyDown(getPopoverContent(), { key: 'ArrowUp' });
    fireEvent.keyDown(getPopoverContent(), { key: 'Enter' });

    expect(mockOnClickApplication).not.toHaveBeenCalledWith(favoriteApplication);
    expect(mockOnClickApplication).toHaveBeenCalledWith(
      expect.objectContaining({ applicationName: 'app-in-list' }),
    );
  });

  // 마우스로 즐겨찾기 항목에 올려 둔 상태에서 service가 바뀌어도 마찬가지다.
  // (이쪽은 favoriteApplications가 비는 순간 isMouseMove도 함께 풀려 focusInfo가 쓰인다.
  //  위 초기화가 없어도 지켜지지만, 사용자에게 보장해야 하는 동작이라 함께 고정해 둔다.)
  test('does not let a hovered favorite be selected after the service changes', () => {
    mockIsDefaultService.mockReturnValue(true);
    const { rerender } = render(listElement());

    fireEvent.mouseMove(getPopoverContent());
    fireEvent.mouseEnter(screen.getByTestId('favorite-item'));

    mockIsDefaultService.mockReturnValue(false);
    rerender(listElement());

    fireEvent.keyDown(getPopoverContent(), { key: 'Enter' });

    expect(mockOnClickApplication).not.toHaveBeenCalledWith(favoriteApplication);
  });

  // DEFAULT service로 돌아오면 목록이 그대로 다시 보여야 한다.
  test('restores the favorite list when the DEFAULT service comes back', () => {
    mockIsDefaultService.mockReturnValue(true);
    const { rerender } = render(listElement());

    mockIsDefaultService.mockReturnValue(false);
    rerender(listElement());
    expect(screen.queryByText('Favorite List')).toBeNull();

    mockIsDefaultService.mockReturnValue(true);
    rerender(listElement());

    expect(screen.queryByText('Favorite List')).not.toBeNull();
    expect(screen.queryByText('favorite-app')).not.toBeNull();
    expect(screen.getAllByTestId('star')).toHaveLength(3);
  });
});
