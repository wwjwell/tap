package com.github.wwjwell.tap.utils;

import org.junit.Assert;
import org.junit.Test;

import java.util.Set;

/**
 * @author : wwj
 * Create 2017/11/17
 **/
public class NetUtilTest {

    @Test
    public void localIpTest() {
        Set<String> allLocalAddress = NetUtil.getAllIpv4Address();
        for (String localAddress : allLocalAddress) {
            System.out.println(localAddress);
        }
        Assert.assertTrue(allLocalAddress.size()>0);
    }
}
