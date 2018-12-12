package com.github.wwjwell.tap;

import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author wwj
 * Create 2017/10/27
 **/
public class TapTest {
    @Test
    public void testGatedIps() {
        Tap.Builder builder = new Tap.Builder();
        builder.name("test.tap");
        builder.configLocation("/config_gatedIps.json");
        Tap tap = builder.build();
        RateLimiter rateLimiter = tap.rateLimiter();
        System.out.println(rateLimiter.getGatedIps());
        System.out.println(rateLimiter.getLocalIpSet());
//        Assert.assertTrue(rateLimiter.isEnable());
    }

    @Test
    public void testThreshold(){
        Tap.Builder builder = new Tap.Builder();
        builder.name("test.tap");
        builder.configLocation("/config_Threshold.json");
        Tap tap = builder.build();
        runTestUnit(tap);
    }


    @Test
    public void createFrequency(){
        Tap.Builder builder = new Tap.Builder();
        builder.name("test.tap");
        builder.configLocation("/config_Frequency.json");
        Tap tap = builder.build();
        runTestUnit(tap);
    }


    @Test
    public void createBlankList(){
        Tap.Builder builder = new Tap.Builder();
        builder.name("test.tap");
        builder.configLocation("/config_BlankList.json");

        Tap tap = builder.build();
        blankWriteTest(tap);
    }

    @Test
    public void createWriteList(){
        Tap.Builder builder = new Tap.Builder();
        builder.name("test.tap");
        builder.configLocation("/config_WriteList.json");

        Tap tap = builder.build();
        blankWriteTest(tap);
    }

    private void blankWriteTest(Tap tap){
        for (int i = 0; i < 100; i++) {
            HashMap<String, Object> attachment = Maps.newHashMap();
            attachment.put("uid", String.valueOf(i));
            RateLimitResult result = tap.rateLimiter().acquire("/hello", attachment);
            if(result.isReject()) {
                System.err.println(i + ": reject");
            }else{
                System.out.println(i + ": pass");
            }
            Assert.assertNotNull(result);
        }
    }

    @Test
    public void createKeyPercent(){
        Tap.Builder builder = new Tap.Builder();
        builder.name("test.tap");
        builder.configLocation("/config_KeyPercent.json");
        Tap tap = builder.build();

        try {
            Thread.sleep(1000*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        runTestUnit(tap);
    }

    private void runTestUnit(Tap tap){
        runTestUnit(tap, "uid", true);
    }

    private void runTestUnit(final Tap tap,final String key,final boolean useRandomKey){
        int numThread = 40;
        long startTime = System.currentTimeMillis();
        final AtomicInteger passNum = new AtomicInteger();
        final AtomicInteger rejectNum = new AtomicInteger();
        final CountDownLatch countDownLatch = new CountDownLatch(numThread);
        for (int j = 0; j < numThread; j++) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < 100; i++) {
                        HashMap<String, Object> attachment = Maps.newHashMap();
                        attachment.put(key, useRandomKey?UUID.randomUUID().toString():Thread.currentThread().getName());
                        RateLimitResult result = tap.rateLimiter().acquire("/hello", attachment);
                        if(result.isReject()) {
                            System.out.println(Thread.currentThread().getId() + ": reject ,num=" + rejectNum.getAndIncrement() + ", reason="+ result.getRejectStrategyResult());
                        }else{
                            System.out.println(Thread.currentThread().getId() + ": pass ,num="+passNum.getAndIncrement());
                        }
                        Assert.assertNotNull(result);
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                    countDownLatch.countDown();
                }
            });
            thread.start();
        }



        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long cost = System.currentTimeMillis() - startTime;
        System.out.println("\n汇总数据:\n\tcost="+cost+"ms\n\tpassNum=" + passNum.get()+"\n\trejectNum="+rejectNum.get());
    }
}
