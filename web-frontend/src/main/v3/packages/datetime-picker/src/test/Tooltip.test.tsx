import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Tooltip } from '../components/Tooltip';

describe('Tooltip', () => {
  it('opens on click and closes on outside click', async () => {
    const user = userEvent.setup();
    render(
      <div>
        <Tooltip openOn="click" content={<div>panel-content</div>}>
          <button>open</button>
        </Tooltip>
        <button>outside</button>
      </div>,
    );

    expect(screen.queryByText('panel-content')).not.toBeInTheDocument();

    await user.click(screen.getByText('open'));
    expect(screen.getByText('panel-content')).toBeInTheDocument();
    expect(screen.getByText('open')).toHaveAttribute('aria-expanded', 'true');

    await user.click(screen.getByText('outside'));
    expect(screen.queryByText('panel-content')).not.toBeInTheDocument();
  });

  it('opens on hover after a delay', async () => {
    const user = userEvent.setup();
    render(
      <Tooltip openOn="hover" content={<div>hovered</div>}>
        <button>trigger</button>
      </Tooltip>,
    );

    await user.hover(screen.getByText('trigger'));
    // 호버 직후에는 아직 열리지 않는다 (500ms 딜레이)
    expect(screen.queryByText('hovered')).not.toBeInTheDocument();
    // 딜레이가 지나면 열린다
    expect(await screen.findByText('hovered')).toBeInTheDocument();
  });
});
