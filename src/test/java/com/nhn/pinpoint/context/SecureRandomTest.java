package com.nhn.pinpoint.context;

import org.junit.Ignore;
import org.junit.Test;

import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 */
@Ignore
public class SecureRandomTest {
    private int count = 5000000;

    @Test
    public void test() {
//        ExecutorService service = Executors.newFixedThreadPool(128);

        jdkUUID();
        customJdk();
        System.out.println("----------------");
        jdkUUID();
        customJdk();
    }
    @Test
    public void jdkUUID() {
        long before = System.currentTimeMillis();
        for(int i =0; i< count; i++) {
            UUID uuid = UUID.randomUUID();
        }
        System.out.println(System.currentTimeMillis() -before);

    }

//    public
    Random random = new Random();
    public void customJdk() {
        long before = System.currentTimeMillis();
        for(int i =0; i< count; i++) {
            random();
        }
        System.out.println(System.currentTimeMillis() -before);

    }

    private void random() {
        byte[] randomBytes = new byte[16];
        random.nextBytes(randomBytes);

        randomBytes[8]  &= 0x3f;  /* clear variant        */
        randomBytes[8]  |= 0x80;  /* set to IETF variant  */
        long msb = 0;
        for (int i=0; i<8; i++) {
            msb = (msb << 8) | (randomBytes[i] & 0xff);
        }


        randomBytes[6]  &= 0x0f;  /* clear version        */
        randomBytes[6]  |= 0x40;  /* set to version 4     */
        long lsb = 0;
        for (int i=8; i<16; i++) {
            lsb = (lsb << 8) | (randomBytes[i] & 0xff);
        }
    }
}
