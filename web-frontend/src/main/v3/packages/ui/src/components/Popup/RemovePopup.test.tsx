import { fireEvent, render, screen } from '@testing-library/react';

// The ui lib barrel also loads the chart helpers, and echarts ships ESM only, which jest cannot
// load. No chart is drawn here.
jest.mock('echarts/core', () => ({ use: jest.fn() }));
jest.mock('echarts/charts', () => ({}));
jest.mock('echarts/components', () => ({}));
jest.mock('echarts/renderers', () => ({}));
jest.mock('echarts/features', () => ({}));

import { RemovePopup } from './RemovePopup';

const openPopup = (onClickRemove: () => void, confirmText?: string) => {
  render(
    <RemovePopup
      popupTrigger={<button>open</button>}
      popupTitle="Delete"
      confirmText={confirmText}
      confirmLabel="Type the name"
      removeButtonLabel="Delete it"
      onClickRemove={onClickRemove}
    />,
  );
  fireEvent.click(screen.getByText('open'));
};

describe('RemovePopup', () => {
  it('enables the remove button right away without confirmText', () => {
    const onClickRemove = jest.fn();
    openPopup(onClickRemove);

    expect(screen.queryByLabelText('Type the name')).toBeNull();
    fireEvent.click(screen.getByRole('button', { name: 'Delete it' }));
    expect(onClickRemove).toHaveBeenCalledTimes(1);
  });

  it('enables the remove button only when the input matches confirmText exactly', () => {
    const onClickRemove = jest.fn();
    openPopup(onClickRemove, 'my-service');

    const input = screen.getByLabelText('Type the name');
    const removeButton = screen.getByRole('button', { name: 'Delete it' });
    expect((removeButton as HTMLButtonElement).disabled).toBe(true);

    fireEvent.change(input, { target: { value: 'my-servic' } });
    expect((removeButton as HTMLButtonElement).disabled).toBe(true);

    fireEvent.change(input, { target: { value: 'MY-SERVICE' } });
    expect((removeButton as HTMLButtonElement).disabled).toBe(true);

    fireEvent.change(input, { target: { value: 'my-service' } });
    expect((removeButton as HTMLButtonElement).disabled).toBe(false);
    fireEvent.click(removeButton);
    expect(onClickRemove).toHaveBeenCalledTimes(1);
  });

  it('clears the input when the popup is reopened', () => {
    openPopup(jest.fn(), 'my-service');

    fireEvent.change(screen.getByLabelText('Type the name'), { target: { value: 'my-service' } });
    fireEvent.click(screen.getByRole('button', { name: 'COMMON.CANCEL' }));
    fireEvent.click(screen.getByText('open'));

    expect((screen.getByLabelText('Type the name') as HTMLInputElement).value).toBe('');
    expect((screen.getByRole('button', { name: 'Delete it' }) as HTMLButtonElement).disabled).toBe(
      true,
    );
  });
});
