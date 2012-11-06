package com.profiler.sampler;

import java.util.Random;
import java.util.logging.Logger;

/**
 *
 */
public class RandomSampler {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    private final Random random = new Random();
    private int samplingRate = 10;

    public RandomSampler(int samplingRate) {
        if (samplingRate <= 0 || samplingRate >= 100) {
            logger.warning("Invalid sampling rate. Expected range(0~100) " + samplingRate);
            samplingRate = 0;
        }
        this.samplingRate = samplingRate;
    }

    public boolean sample() {
        int i = Math.abs(random.nextInt()) % 101;
        return sample(i);
    }

    public boolean sample(int seed) {
        if (seed == 0) {
            return false;
        }
        if (seed <= samplingRate) {
            return true;
        }
        return false;
    }
}
