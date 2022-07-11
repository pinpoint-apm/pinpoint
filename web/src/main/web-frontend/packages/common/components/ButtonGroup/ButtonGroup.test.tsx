import { fireEvent, render, screen, waitFor } from '@testing-library/react'
import userEvent from "@testing-library/user-event";
import { act } from 'react-dom/test-utils';
import ButtonGroup, { ButtonGroupContainerProps } from './ButtonGroup';

function renderButtonGroup(props?: Partial<ButtonGroupContainerProps>) {
  const user = userEvent.setup();
  const rendered = render(
    <ButtonGroup.Container {...props}>
      <ButtonGroup.Button>A</ButtonGroup.Button>
      <ButtonGroup.Button>B</ButtonGroup.Button>
      <ButtonGroup.Button disableActive={true}>C</ButtonGroup.Button>
      <ButtonGroup.Button>D</ButtonGroup.Button>
    </ButtonGroup.Container>
  )

  const buttonA = () => rendered.getByText('A'); 
  const buttonB = () => rendered.getByText('B');
  const buttonDisableActive = () => rendered.getByText('C');
  const buttonD = () => rendered.getByText('D');

  function clickButtonB() {
    userEvent.click(buttonB())
  }

  function clickButtonDisableActive() {
    userEvent.click(buttonDisableActive())
  }

  return {
    buttonA,
    buttonB,
    buttonDisableActive,
    buttonD,
    clickButtonB,
    clickButtonDisableActive,
  }
}

describe('ButtonGroup', () => {
  const activeClassName = 'active';

  it('has active classname if button specified by initActiveIndex prop', () => {
    const {
      buttonD,
    } = renderButtonGroup({ initActiveIndex: 3 });

    expect(buttonD()).toHaveClass(activeClassName);
  })

  it('add active classname when click button.', async () => {
    const {
      buttonB,
      clickButtonB,
    } = renderButtonGroup();

    clickButtonB();
    await waitFor(() => expect(buttonB()).toHaveClass(activeClassName));
  })

  it('has no active classname when click disable active button', async () => {
    const {
      buttonDisableActive,
      clickButtonDisableActive,
    } = renderButtonGroup();

    clickButtonDisableActive();
    await waitFor(() => expect(buttonDisableActive()).not.toHaveClass(activeClassName));
  })
})