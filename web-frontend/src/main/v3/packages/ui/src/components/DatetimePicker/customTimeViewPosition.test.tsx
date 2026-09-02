/**
 * datetime-picker 의 More 패널이 부모 옆에 열리는지 확인한다.
 *
 * 이 패널은 `-z-10` 으로 부모 뒤에 두고 옆으로 미끄러져 나온다. 예전에는 열린 뒤의 위치를
 * `enterTo` 의 `-translate-x-full` 에 맡겼는데, @headlessui/react 1 은 그 클래스를 전환이 끝난
 * 뒤에도 남겨 뒀지만 2 는 지운다. 그래서 2 로 올린 뒤 패널이 제자리로 돌아와 부모 뒤에 가렸다.
 *
 * 지금은 열린 뒤의 위치를 `right-full`/`left-full`/`top-full` 로 정하고 전환은 효과만 담당한다.
 * 다시 전환 클래스에 위치를 맡기면 이 테스트가 실패한다.
 */
import { render, act } from '@testing-library/react';
// 패키지 배럴은 스타일시트와 SVG 를 끌어와 jest 가 읽지 못한다. 이 컴포넌트만 직접 가져온다.
import { CustomTimeView } from '@pinpoint-fe/datetime-picker/src/components/CustomTimeView';
import { enUS } from 'date-fns/locale';

const Harness = ({
  show,
  direction,
}: {
  show: boolean;
  direction: 'left' | 'right' | 'bottom';
}) => (
  <CustomTimeView show={show} direction={direction} locale={enUS} customTimes={{}}>
    <div>more</div>
  </CustomTimeView>
);

const open = async (direction: 'left' | 'right' | 'bottom') => {
  const { container, rerender } = render(<Harness show={false} direction={direction} />);

  await act(async () => {
    rerender(<Harness show={true} direction={direction} />);
  });
  // 전환이 끝난 뒤의 상태를 본다.
  await act(async () => {
    await new Promise((resolve) => setTimeout(resolve, 400));
  });

  return container.querySelector('.rich-datetime-picker__more') as HTMLElement;
};

describe('More 패널의 열린 위치', () => {
  it('왼쪽으로 열 때 부모의 왼쪽 바깥에 붙는다', async () => {
    const panel = await open('left');

    expect(panel).not.toBeNull();
    expect(panel.className).toContain('right-full');
  });

  it('오른쪽으로 열 때 부모의 오른쪽 바깥에 붙는다', async () => {
    const panel = await open('right');

    expect(panel.className).toContain('left-full');
  });

  it('아래로 열 때 부모의 아래에 붙는다', async () => {
    const panel = await open('bottom');

    expect(panel.className).toContain('top-full');
  });

  // 여기가 이 버그의 핵심이다. 전환이 끝나면 변형 클래스는 사라지므로, 위치를 정하는 클래스가
  // 남아 있지 않으면 패널은 부모와 같은 자리(= `-z-10` 때문에 부모 뒤)로 돌아간다.
  it('전환이 끝난 뒤에도 위치 클래스가 남아 있다', async () => {
    const panel = await open('left');

    expect(panel.className).toContain('right-full');
    expect(panel.className).not.toContain('left-0');
  });
});
