package com.github.wwjwell.tap;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * @author : wwj
 * Create 2017/12/27
 **/
public class TimeTest {
    @Test
    public void testMill2Second(){
        int millSeconds = 1000;
        int interval = millSeconds;

        System.out.println((int)TimeUnit.MILLISECONDS.toSeconds(interval) + (interval%1000==0?0:1));
        System.out.println(TimeUnit.MILLISECONDS.toSeconds(millSeconds));
        Assert.assertEquals(TimeUnit.MILLISECONDS.toSeconds(millSeconds), millSeconds/1000);

    }


}
