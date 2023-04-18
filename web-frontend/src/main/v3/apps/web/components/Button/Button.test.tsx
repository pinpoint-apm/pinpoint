import { render, screen } from '@testing-library/react'
import { Button } from './Button';

describe('Button', () => {
  it('renders button', () => {
    const { getByText } = render(<Button />)

    const buttonText = getByText('WEB button');

    expect(buttonText).toBeInTheDocument();
  })
})