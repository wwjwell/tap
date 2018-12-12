package com.github.wwjwell.tap;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author : wwj
 * Create 2017/11/16
 **/
public class NormalTest {
    @Test
    public void testSplit(){
        String ips = "127.0.0.1,127.0.0.2，127.0.0.3";
        String[] split = ips.split(",|，");
        Assert.assertTrue(split.length==3);

        ips="";
        split = ips.split(",|，");
        Assert.assertTrue(split.length==1);
    }
}
