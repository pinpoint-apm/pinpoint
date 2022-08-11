import { fireEvent, render, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event';
import Dropdown, { DropdownProps } from './Dropdown';

function renderDropdown(props?: Partial<DropdownProps>) {
  const user = userEvent.setup();
  const rendered = render(
    <Dropdown {...props}>
      <Dropdown.Trigger>Trigger</Dropdown.Trigger>
      <Dropdown.Content>Content</Dropdown.Content>
    </Dropdown>
  )

  const Trigger = () => rendered.getByText('Trigger'); 
  const Content = () => rendered.getByText('Content');

  function clickTrigger() {
    user.click(Trigger());
  }

  function clickOutside() {
    user.click(document.body);
  }
  
  function pressEscapeKey() {
    user.keyboard('{Escape}');
  }

  return {
    Trigger,
    Content,
    clickTrigger,
    clickOutside,
    pressEscapeKey,
  }
}

describe('Dropdown', () => {
  it('open Content when click Trigger.', async () => {
    const {
      Content,
      clickTrigger,
    } = renderDropdown();
    clickTrigger();
    await waitFor(() => expect(Content()).toBeVisible());
  })

  it('close Content when click out side', async () => {
    const {
      Content,
      clickTrigger,
      clickOutside,
    } = renderDropdown();

    clickTrigger();
    await waitFor(() => expect(Content()).toBeVisible());
    clickOutside();
    await waitFor(() => expect(Content()).not.toBeVisible());
  })

  it('close Content when press ESC key', async () => {
    const {
      Content,
      clickTrigger,
      pressEscapeKey,
    } = renderDropdown();

    clickTrigger();
    await waitFor(() => expect(Content()).toBeVisible());
    pressEscapeKey();
    await waitFor(() => expect(Content()).not.toBeVisible());
  })
}) 
