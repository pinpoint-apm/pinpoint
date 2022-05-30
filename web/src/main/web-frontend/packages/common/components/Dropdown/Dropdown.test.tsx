import { fireEvent, render } from '@testing-library/react'
import userEvent from '@testing-library/user-event';
import Dropdown, { DropdownProps } from './Dropdown';

function renderDropdown(props?: Partial<DropdownProps>) {
  const rendered = render(
    <Dropdown {...props}>
      <Dropdown.Trigger>Trigger</Dropdown.Trigger>
      <Dropdown.Content>Content</Dropdown.Content>
    </Dropdown>
  )

  const Trigger = () => rendered.getByText('Trigger'); 
  const Content = () => rendered.getByText('Content');

  function clickTrigger() {
    userEvent.click(Trigger());
  }

  function clickOutside() {
    userEvent.click(document.body);
  }

  function pressEscapeKey() {
    userEvent.keyboard('{esc}');
    // fireEvent.keyDown(document.body, {
    //   key: 'Escape',
    //   code: 'Escape',
    //   keyCode: 27,
    //   charCode: 27
    // });
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
  it('open Content when click Trigger.', () => {
    const {
      Content,
      clickTrigger,
    } = renderDropdown();

    clickTrigger();
    expect(Content()).toBeVisible();
  })

  it('close Content when click out side', () => {
    const {
      Content,
      clickTrigger,
      clickOutside,
    } = renderDropdown();

    clickTrigger();
    expect(Content()).toBeVisible();
    clickOutside();
    expect(Content()).not.toBeVisible();
  })

  it('close Content when press ESC key', () => {
    const {
      Content,
      clickTrigger,
      pressEscapeKey,
    } = renderDropdown();

    clickTrigger();
    expect(Content()).toBeVisible();
    pressEscapeKey();
    expect(Content()).not.toBeVisible();
  })
}) 
