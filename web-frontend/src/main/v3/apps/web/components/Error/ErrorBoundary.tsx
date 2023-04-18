// ErrorBoundary.tsx
import React, { Component, ErrorInfo, ReactNode } from 'react';

interface Props {
  children: ReactNode;
  fallback: ReactNode;
}

interface ErrorState {
  hasError: boolean;
  error: Error | null;
}

class ErrorBoundary extends Component<Props, ErrorState> {
  public state: ErrorState = {
    hasError: false,
    error: null,
  };

  public static getDerivedStateFromError(error: Error): ErrorState {
    // Update state so the next render will show the fallback UI.
    return { hasError: true, error };
  }

  public componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    // console.error("Uncaught error:", error, errorInfo);
    // error 기록
  }

  public render() {
    if (this.state.hasError) {
      return this.props.fallback;
    }

    return this.props.children;
  }
}

export default ErrorBoundary;
