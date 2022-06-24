package com.navercorp.pinpoint.web.scatter.heatmap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;


public class HeatMapBuilderTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Test
    public void axix() {
        HeatMapBuilder.AxisResolver resolver = new HeatMapBuilder.DefaultAxisResolver(100, 0, 1000);

        Assertions.assertEquals(0L, resolver.getIndex(0));
        Assertions.assertEquals(10L, resolver.getIndex(100));
    }

    @Test
    public void axix_max_overflow() {
        HeatMapBuilder.AxisResolver resolver = new HeatMapBuilder.DefaultAxisResolver(100, 0, 1000);

        Assertions.assertEquals(100, resolver.getIndex(1000));
        Assertions.assertEquals(100, resolver.getIndex(1100));
    }

    @Test
    public void axix_max_overflow_start() {
        HeatMapBuilder.AxisResolver resolver = new HeatMapBuilder.DefaultAxisResolver(100, 100, 1100);

        Assertions.assertEquals(0, resolver.getIndex(100));
        Assertions.assertEquals(100, resolver.getIndex(1200));
    }

    @Test
    public void addPoint1() {

        HeatMapBuilder builder = HeatMapBuilder.newBuilder(0, 1000, 10, 0, 1000, 10);

        builder.addDataPoint(1, 1, true);
        builder.addDataPoint(1, 2, true);

        builder.addDataPoint(101, 101, true);

        HeatMap heatMap = builder.build();
        List<Point> points = heatMap.getData();

        Assertions.assertEquals(points.size(), 2);

        long sum = points.stream().mapToLong(Point::getSuccess).sum();
        Assertions.assertEquals(3L, sum);

        Assertions.assertEquals(1, points.get(0).getSuccess());
        Assertions.assertEquals(2, points.get(1).getSuccess());
    }

    @Test
    public void addPoint2() {

        HeatMapBuilder builder = HeatMapBuilder.newBuilder(0, 1000, 100, 0, 1000, 100);

        builder.addDataPoint(1, 1, true);
        builder.addDataPoint(1, 200, true);

        HeatMap heatMap = builder.build();
        List<Point> points = heatMap.getData();

        Assertions.assertEquals(points.size(), 2);


        Assertions.assertEquals(0, points.get(0).getY());
        Assertions.assertEquals(20, points.get(1).getY());


    }

    @Test
    public void index() {
        HeatMapBuilder.AxisResolver resolver = new HeatMapBuilder.DefaultAxisResolver(10, 0, 1000);
        long[] index = resolver.getIndex();
        logger.debug("{}", Arrays.toString(index));

        Assertions.assertEquals(10, index.length);

        Assertions.assertEquals(0, index[0]);
        Assertions.assertEquals(500, index[5]);
        Assertions.assertEquals(900, index[9]);
    }

    @Test
    public void index2() {
        HeatMapBuilder.AxisResolver resolver = new HeatMapBuilder.DefaultAxisResolver(10, 500, 1000);
        long[] index = resolver.getIndex();
        logger.debug("{}", Arrays.toString(index));

        Assertions.assertEquals(10, index.length);

        Assertions.assertEquals(500, index[0]);
        Assertions.assertEquals(750, index[5]);
        Assertions.assertEquals(950, index[9]);
    }
}