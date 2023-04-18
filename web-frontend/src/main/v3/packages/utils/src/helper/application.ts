export const getApplicationTypeAndName = (application: string) => {
  const splittedPath = application?.split('@');
  const applicationName = splittedPath?.[0];
  const serviceType = splittedPath?.[1];

  if (applicationName && serviceType) {
    return { applicationName, serviceType };
  }

  return null;
}

