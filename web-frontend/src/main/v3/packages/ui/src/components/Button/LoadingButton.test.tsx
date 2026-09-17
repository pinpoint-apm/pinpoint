import { render, screen } from '@testing-library/react';
import { LoadingButton } from './LoadingButton';

// A pending button that stays clickable submits twice, whatever `disabled` the caller passes.
describe('LoadingButton', () => {
  const button = () => screen.getByRole('button') as HTMLButtonElement;

  it('stays disabled while pending even when the caller passes disabled={false}', () => {
    render(
      <LoadingButton pending disabled={false}>
        Save
      </LoadingButton>,
    );

    expect(button().disabled).toBe(true);
  });

  it('disables while pending with no disabled prop', () => {
    render(<LoadingButton pending>Save</LoadingButton>);

    expect(button().disabled).toBe(true);
  });

  it('disables when the caller disables it and nothing is pending', () => {
    render(<LoadingButton disabled>Save</LoadingButton>);

    expect(button().disabled).toBe(true);
  });

  it('is enabled when neither pending nor disabled', () => {
    render(<LoadingButton>Save</LoadingButton>);

    expect(button().disabled).toBe(false);
  });
});
