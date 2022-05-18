import { render, screen } from '@testing-library/react'
import { Button } from './Button';

describe('Button', () => {
  it('renders button', () => {
    const { getByText } = render(<Button label={'button'} />)

    const buttonText = getByText('button');

    expect(buttonText).toBeInTheDocument();
  })
})