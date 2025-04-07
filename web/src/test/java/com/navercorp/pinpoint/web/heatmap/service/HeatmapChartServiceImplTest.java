/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.heatmap.service;

import com.navercorp.pinpoint.web.heatmap.dao.HeatmapChartDao;
import com.navercorp.pinpoint.web.heatmap.vo.ElapsedTimeBucketInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author minwoo-jung
 */
@ExtendWith(MockitoExtension.class)
class HeatmapChartServiceImplTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Mock
    private HeatmapChartDao heatmapChartDao;

    private HeatmapChartServiceImpl heatmapChartService;

    @BeforeEach
    public void setUp() {
        heatmapChartService = new HeatmapChartServiceImpl(heatmapChartDao);
    }

    @Test
    public void calculateTimeInterval() {

        assertEquals(200, heatmapChartService.calculateTimeInterval(0, 9000));
        assertEquals(300, heatmapChartService.calculateTimeInterval(0, 15000));
        assertEquals(200, heatmapChartService.calculateTimeInterval(10000, 15000));
        assertEquals(6020, heatmapChartService.calculateTimeInterval(10000, 305000));

        int min = 100;
        int max = 133333;
//        int timeInterval = heatmapChartService.calculateTimeInterval(min, max);
        int timeInterval = 211;

        int yAxis = min;
        yAxis += timeInterval;
        int i = 0;
        while (yAxis < max) {
            logger.info("index " + i + " : " + yAxis);
            yAxis += timeInterval;
            i++;
        }
        logger.info("index " + i + " : " + max);
        logger.info("=========end=========");

        logger.info("max - (yAxis - timeInterval)  : " + (max - (yAxis - timeInterval)));
        logger.info("timeInterval : " + timeInterval);
        logger.info("max : " + max);
        logger.info("yAxis : " + yAxis);
        logger.info("(yAxis - timeInterval)  : " + (yAxis - timeInterval));
    }
    
    @Test
    public void createElapsedTimeBucketInfoTest() {
        ElapsedTimeBucketInfo elapsedTimeBucketInfo = heatmapChartService.createElapsedTimeBucketInfo(0, 10000);
        assertEquals(200, elapsedTimeBucketInfo.timeInterval());
        assertEquals(200, elapsedTimeBucketInfo.min());
        assertEquals(10000, elapsedTimeBucketInfo.max());
        assertEquals(50, elapsedTimeBucketInfo.bucketList().size());
        List<Integer> bucketList = Arrays.asList(200, 400, 600, 800, 1000, 1200, 1400, 1600, 1800, 2000, 2200, 2400, 2600, 2800, 3000, 3200, 3400, 3600, 3800, 4000, 4200, 4400, 4600, 4800, 5000, 5200, 5400, 5600, 5800, 6000, 6200, 6400, 6600, 6800, 7000, 7200, 7400, 7600, 7800, 8000, 8200, 8400, 8600, 8800, 9000, 9200, 9400, 9600, 9800, 10000);
        assertEquals(bucketList, elapsedTimeBucketInfo.bucketList());
    }

    @Test
    public void createElapsedTimeBucketInfoTest2() {
        ElapsedTimeBucketInfo elapsedTimeBucketInfo = heatmapChartService.createElapsedTimeBucketInfo(133, 11234);
        assertEquals(226, elapsedTimeBucketInfo.timeInterval());
        assertEquals(133, elapsedTimeBucketInfo.min());
        assertEquals(11234, elapsedTimeBucketInfo.max());
        assertEquals(51, elapsedTimeBucketInfo.bucketList().size());
        List<Integer> bucketList = Arrays.asList(133, 359, 585, 811, 1037, 1263, 1489, 1715, 1941, 2167, 2393, 2619, 2845, 3071, 3297, 3523, 3749, 3975, 4201, 4427, 4653, 4879, 5105, 5331, 5557, 5783, 6009, 6235, 6461, 6687, 6913, 7139, 7365, 7591, 7817, 8043, 8269, 8495, 8721, 8947, 9173, 9399, 9625, 9851, 10077, 10303, 10529, 10755, 10981, 11207, 11234);
        assertEquals(bucketList, elapsedTimeBucketInfo.bucketList());
    }

    @Test
    public void createElapsedTimeBucketInfoTest3() {
        ElapsedTimeBucketInfo elapsedTimeBucketInfo = heatmapChartService.createElapsedTimeBucketInfo(300, 10000);
        assertEquals(200, elapsedTimeBucketInfo.timeInterval());
        assertEquals(300, elapsedTimeBucketInfo.min());
        assertEquals(10000, elapsedTimeBucketInfo.max());
        assertEquals(50, elapsedTimeBucketInfo.bucketList().size());
        List<Integer> bucketList = Arrays.asList(300, 500, 700, 900, 1100, 1300, 1500, 1700, 1900, 2100, 2300, 2500, 2700, 2900, 3100, 3300, 3500, 3700, 3900, 4100, 4300, 4500, 4700, 4900, 5100, 5300, 5500, 5700, 5900, 6100, 6300, 6500, 6700, 6900, 7100, 7300, 7500, 7700, 7900, 8100, 8300, 8500, 8700, 8900, 9100, 9300, 9500, 9700, 9900, 10000);
        assertEquals(bucketList, elapsedTimeBucketInfo.bucketList());
    }

    @Test
    public void createElapsedTimeBucketInfoTest4() {
        ElapsedTimeBucketInfo elapsedTimeBucketInfo = heatmapChartService.createElapsedTimeBucketInfo(355, 54123);
        assertEquals(1097, elapsedTimeBucketInfo.timeInterval());
        assertEquals(355, elapsedTimeBucketInfo.min());
        assertEquals(54123, elapsedTimeBucketInfo.max());
        assertEquals(51, elapsedTimeBucketInfo.bucketList().size());
        List<Integer> bucketList = Arrays.asList(355, 1452, 2549, 3646, 4743, 5840, 6937, 8034, 9131, 10228, 11325, 12422, 13519, 14616, 15713, 16810, 17907, 19004, 20101, 21198, 22295, 23392, 24489, 25586, 26683, 27780, 28877, 29974, 31071, 32168, 33265, 34362, 35459, 36556, 37653, 38750, 39847, 40944, 42041, 43138, 44235, 45332, 46429, 47526, 48623, 49720, 50817, 51914, 53011, 54108, 54123);
        assertEquals(bucketList, elapsedTimeBucketInfo.bucketList());
    }

    @Test
    public void bucketElapsedTimeTest() {
        int timeInterval = 200;
        int elapsedTime = 0;
        assertEquals(200, (((elapsedTime / timeInterval) + 1) * timeInterval));
        elapsedTime = 100;
        assertEquals(200, (((elapsedTime / timeInterval) + 1) * timeInterval));

        timeInterval = 300;
        elapsedTime = 500;
        assertEquals(600, (((elapsedTime / timeInterval) + 1) * timeInterval));
        elapsedTime = 666;
        assertEquals(900, (((elapsedTime / timeInterval) + 1) * timeInterval));
    }


    @Test
    public void mapToNextIntervalBucketTest() {
        assertEquals(20000, mapToNextIntervalBucket(2000000, 20000, 0, 200));
        assertEquals(50, mapToNextIntervalBucket(10, 20000,50, 200));
        assertEquals(50, mapToNextIntervalBucket(50, 20000,50, 200));


        assertEquals(10000, mapToNextIntervalBucket(9999, 20000,0, 200));
        assertEquals(10000, mapToNextIntervalBucket(10000, 20000,0, 200));
        assertEquals(10000, mapToNextIntervalBucket(10000, 20000,200, 200));
        assertEquals(20000, mapToNextIntervalBucket(20000, 20000,0, 400));
        assertEquals(20200, mapToNextIntervalBucket(20000, 20000,200, 400));
        assertEquals(20200, mapToNextIntervalBucket(20200, 30000,200, 400));
        assertEquals(20200, mapToNextIntervalBucket(20199, 30000,200, 400));

        assertEquals(30240, mapToNextIntervalBucket(30000, 60000,0, 315));
        assertEquals(630, mapToNextIntervalBucket(316, 60000,0, 315));
        assertEquals(30025, mapToNextIntervalBucket(30000, 60000,100, 315));
        assertEquals(415, mapToNextIntervalBucket(101, 60000,100, 315));
        assertEquals(30025, mapToNextIntervalBucket(30025, 60000,100, 315));
        assertEquals(30025, mapToNextIntervalBucket(30024, 60000,100, 315));

        assertEquals(400, mapToNextIntervalBucket(400, 1000,200, 200));
    }

    protected int mapToNextIntervalBucket(int value, int max, int startValue, int interval) {
        if (value > max) {
            return max;
        } else if (value <= startValue) {
            return startValue;
        } else {
            return (((((value - startValue - 1) / interval) + 1) * interval) + startValue);
        }
    }

    @Test
    public void findLargestMultipleBelowTest() {
        assertEquals(9800, findLargestMultipleBelow(10000, 0, 200));
        assertEquals(9800, findLargestMultipleBelow(10000, 200, 200));
        assertEquals(19600, findLargestMultipleBelow(20000, 0, 400));
        assertEquals(19800, findLargestMultipleBelow(20000, 200, 400));
        assertEquals(29925, findLargestMultipleBelow(30000, 0, 315));
        assertEquals(29710, findLargestMultipleBelow(30000, 100, 315));
    }

    protected int findLargestMultipleBelow(int upperBound, int startValue, int interval) {
        if (interval <= 0) {
            throw new IllegalArgumentException("interval must be greater than 0");
        }

        int largestDivisible = startValue + ((upperBound - startValue - 1) / interval) * interval;
        return largestDivisible;
    }

}
