/*
 * Copied from: https://github.com/cowtowncoder/java-uuid-generator/blob/31408f5c088d27766269f905896efe383b38a46e/src/main/java/com/fasterxml/uuid/impl/TimeBasedEpochGenerator.java
 */

package com.navercorp.pinpoint.common.uuid;

import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * Implementation of UUID generator that uses time/location based generation
 * method field from the Unix Epoch timestamp source - the number of
 * milliseconds seconds since midnight 1 Jan 1970 UTC, leap seconds excluded.
 * This is usually referred to as "Version 7".
 *
 * @since 4.1
 */
public class TimeBasedEpochGenerator
{
    private static final int ENTROPY_BYTE_LENGTH = 10;

    /*
    /**********************************************************************
    /* Configuration
    /**********************************************************************
     */

    /**
     * Random number generator that this generator uses.
     */
    protected final Random _random;

    /**
     * Underlying {@link UUIDClock} used for accessing current time, to use for
     * generation.
     *
     * @since 4.3
     */
    protected final UUIDClock _clock;

    private long _lastTimestamp = -1;
    private final byte[] _lastEntropy  = new byte[ENTROPY_BYTE_LENGTH];
    private final Lock lock = new ReentrantLock();

    /*
    /**********************************************************************
    /* Construction
    /**********************************************************************
     */

    /**
     * @param rnd Random number generator to use for generating UUIDs; if null,
     *   shared default generator is used. Note that it is strongly recommend to
     *   use a <b>good</b> (pseudo) random number generator; for example, JDK's
     *   {@link SecureRandom}.
     */
    public TimeBasedEpochGenerator(Random rnd) {
        this(rnd, UUIDClock.systemTimeClock());
    }

    /**
     * @param rnd Random number generator to use for generating UUIDs; if null,
     *   shared default generator is used. Note that it is strongly recommend to
     *   use a <b>good</b> (pseudo) random number generator; for example, JDK's
     *   {@link SecureRandom}.
     * @param clock clock Object used for accessing current time to use for generation
     */
    public TimeBasedEpochGenerator(Random rnd, UUIDClock clock)
    {
        if (rnd == null) {
            rnd = LazyRandom.sharedSecureRandom();
        }
        _random = rnd;
        _clock = clock;
    }

    /*
    /**********************************************************************
    /* UUID generation
    /**********************************************************************
     */
    public UUID generate()
    {
        return construct(_clock.currentTimeMillis());
    }

    /**
     * Method that will construct actual {@link UUID} instance for given
     * unix epoch timestamp: called by {@link #generate()} but may alternatively be
     * called directly to construct an instance with known timestamp.
     * NOTE: calling this method directly produces somewhat distinct UUIDs as
     * "entropy" value is still generated as necessary to avoid producing same
     * {@link UUID} even if same timestamp is being passed.
     *
     * @param rawTimestamp unix epoch millis
     *
     * @return unix epoch time based UUID
     *
     * @since 4.3
     */
    public UUID construct(long rawTimestamp)
    {
        lock.lock();
        try {
            if (rawTimestamp == _lastTimestamp) {
                boolean c = true;
                for (int i = ENTROPY_BYTE_LENGTH - 1; i >= 0; i--) {
                    if (c) {
                        byte temp = _lastEntropy[i];
                        temp = (byte) (temp + 0x01);
                        c = _lastEntropy[i] == (byte) 0xff && c;
                        _lastEntropy[i] = temp;
                    }
                }
                if (c) {
                    throw new IllegalStateException("overflow on same millisecond");
                }
            } else {
                _lastTimestamp = rawTimestamp;
                _random.nextBytes(_lastEntropy);
            }
            return constructUUID((rawTimestamp << 16) | _toShort(_lastEntropy, 0), _toLong(_lastEntropy, 2));
        } finally {
            lock.unlock();
        }
    }

    public static UUID constructUUID(long l1, long l2)
    {
        // first, ensure type is ok
        l1 &= ~0xF000L; // remove high nibble of 6th byte
        l1 |= 7 << 12;
        // second, ensure variant is properly set too (8th byte; most-sig byte of second long)
        l2 = ((l2 << 2) >>> 2); // remove 2 MSB
        l2 |= (2L << 62); // set 2 MSB to '10'
        return new UUID(l1, l2);
    }

    /*
    /**********************************************************************
    /* Internal methods
    /**********************************************************************
     */

    protected static long _toLong(byte[] buffer, int offset)
    {
        long l1 = _toInt(buffer, offset);
        long l2 = _toInt(buffer, offset+4);
        return (l1 << 32) + ((l2 << 32) >>> 32);
    }

    private static long _toInt(byte[] buffer, int offset)
    {
        return (buffer[offset] << 24)
                + ((buffer[++offset] & 0xFF) << 16)
                + ((buffer[++offset] & 0xFF) << 8)
                + (buffer[++offset] & 0xFF);
    }

    private static long _toShort(byte[] buffer, int offset)
    {
        return ((buffer[offset] & 0xFF) << 8)
                + (buffer[++offset] & 0xFF);
    }

    public static class UUIDClock
    {
        private final static UUIDClock DEFAULT = new UUIDClock();

        /**
         * @since 4.3
         */
        public static UUIDClock systemTimeClock() {
            return DEFAULT;
        }

        /**
         * Returns the current time in milliseconds.
         */
        public long currentTimeMillis()
        {
            return System.currentTimeMillis();
        }
    }

    public static final class LazyRandom
    {
        private static final Object lock = new Object();
        private static volatile SecureRandom shared;

        public static SecureRandom sharedSecureRandom() {
            synchronized (lock) {
                SecureRandom result = shared;
                if (result == null) {
                    shared = result = new SecureRandom();
                }

                return result;
            }
        }
    }

}
