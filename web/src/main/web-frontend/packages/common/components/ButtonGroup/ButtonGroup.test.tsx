import { fireEvent, render, screen } from '@testing-library/react'
import userEvent from "@testing-library/user-event";
import ButtonGroup, { ButtonGroupContainerProps } from './ButtonGroup';

function renderButtonGroup(props?: Partial<ButtonGroupContainerProps>) {
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

  it('add active classname when click button.', () => {
    const {
      buttonB,
      clickButtonB,
    } = renderButtonGroup();

    clickButtonB();
    expect(buttonB()).toHaveClass(activeClassName);
  })

  it('has no active classname when click disable active button', () => {
    const {
      buttonDisableActive,
      clickButtonDisableActive,
    } = renderButtonGroup();

    clickButtonDisableActive();
    expect(buttonDisableActive()).not.toHaveClass(activeClassName);
  })
})