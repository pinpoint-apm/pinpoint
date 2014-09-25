package com.nhn.pinpoint.web.vo.linechart;

/**
 * @author hyungil.jeong
 */
public class SampledDoubleLineChart extends LineChart<Long, Double> {

    int sampleRate;
    int sampleIndex;
    Double[] sampleBuffer;

    public SampledDoubleLineChart(int sampleRate) {
        this.sampleRate = sampleRate;
        this.sampleBuffer = new Double[sampleRate];
        this.sampleIndex = 0;
    }

    @Override
    public void addPoint(Long xVal, Double yVal) {
        sampleBuffer[sampleIndex++] = yVal;

        // FIXME 선택 가능하게. 모두 다 하는 경우에는 그냥 메소드 한번으로 끝낼 수도 있지만 일단...
        if (sampleIndex == sampleRate) {
            // point[x, minY, maxY, avgY]
            Number[] samplePoint = new Number[4];
            samplePoint[0] = xVal;
            samplePoint[1] = DownSamplers.MIN.sampleDouble(sampleBuffer);
            samplePoint[2] = DownSamplers.MAX.sampleDouble(sampleBuffer);
            samplePoint[3] = DownSamplers.AVG.sampleDouble(sampleBuffer);

            getPoints().add(samplePoint);
            sampleIndex = 0;
        }
    }


}
