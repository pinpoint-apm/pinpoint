package com.nhn.pinpoint.web.vo.linechart;

/**
 * @author hyungil.jeong
 */
public final class SampledLongLineChart extends LineChart<Long, Long> {

    int sampleRate;
    int sampleIndex;
    Long[] sampleBuffer;

    public SampledLongLineChart(int sampleRate) {
        this.sampleRate = sampleRate;
        this.sampleBuffer = new Long[sampleRate];
        this.sampleIndex = 0;
    }

    @Override
    public void addPoint(Long xVal, Long yVal) {		
        sampleBuffer[sampleIndex++] = yVal;

        // FIXME 선택 가능하게. 모두 다 하는 경우에는 그냥 메소드 한번으로 끝낼 수도 있지만 일단...
        if (sampleIndex == sampleRate) {
            // point[x, minY, maxY, avgY]
            Number[] samplePoint = new Number[4];
            samplePoint[0] = xVal;
            samplePoint[1] = DownSamplers.MIN.sampleLong(sampleBuffer);
            samplePoint[2] = DownSamplers.MAX.sampleLong(sampleBuffer);
            samplePoint[3] = DownSamplers.AVG.sampleLong(sampleBuffer);

            getPoints().add(samplePoint);
            sampleIndex = 0;
        }
    }

}
