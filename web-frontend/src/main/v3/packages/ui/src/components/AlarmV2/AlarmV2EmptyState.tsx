import React from 'react';

/** Empty table body, sized like the shared DataTable's empty row (`h-24`). */
export const AlarmV2EmptyState = ({ children }: { children: React.ReactNode }) => (
  <div className="flex h-24 items-center justify-center rounded-md border text-sm text-muted-foreground">
    {children}
  </div>
);
