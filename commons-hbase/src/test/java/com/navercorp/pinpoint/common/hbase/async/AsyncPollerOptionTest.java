package com.navercorp.pinpoint.common.hbase.async;

import com.navercorp.pinpoint.common.util.CpuUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AsyncPollerOptionTest {

    @Test
    void getParallelism() {
        AsyncPollerOption option = new AsyncPollerOption();
        Assertions.assertEquals(CpuUtils.cpuCount(), option.getParallelism());
    }


    @Test
    void getParallelism_min() {
        AsyncPollerOption option = new AsyncPollerOption();
        option.setMinCpuCore(2);
        option.setCpuRatio(100);

        Assertions.assertEquals(2, option.getParallelism());
    }

    @Test
    void getParallelism_ratio() {
        int cpu = 16;

        AsyncPollerOption option = new AsyncPollerOption() {
            @Override
            int getCpuCount() {
                return cpu;
            }
        };

        option.setMinCpuCore(4);
        option.setCpuRatio(2);

        Assertions.assertEquals(cpu/2, option.getParallelism());
    }

    @Test
    void getParallelism_ratio_small() {
        int cpu = 2;

        AsyncPollerOption option = new AsyncPollerOption() {
            @Override
            int getCpuCount() {
                return cpu;
            }
        };

        option.setMinCpuCore(4);
        option.setCpuRatio(2);

        Assertions.assertEquals(4, option.getParallelism());
    }
}