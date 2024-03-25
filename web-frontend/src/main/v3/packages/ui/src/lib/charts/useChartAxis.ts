export const useChartAxis = () => {
  const chartUnitAxisInfo: Record<string, string> = {}; // { "unit1": "y", "unit2": "y2" }
  const getDataAxis = (unit: string) => {
    let dataAxis;

    if (chartUnitAxisInfo[unit]) {
      dataAxis = chartUnitAxisInfo[unit];
    } else {
      const presetAxisList = Object.values(chartUnitAxisInfo);

      chartUnitAxisInfo[unit] = `y${presetAxisList.length === 0 ? '' : presetAxisList.length + 1}`;
      dataAxis = chartUnitAxisInfo[unit];
    }

    return dataAxis;
  };

  return { chartUnitAxisInfo, getDataAxis };
};
