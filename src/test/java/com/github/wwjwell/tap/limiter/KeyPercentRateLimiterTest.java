package com.github.wwjwell.tap.limiter;

import com.github.wwjwell.tap.strategy.local.KeyPercentStrategy;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

/**
 * @author wwj
 * Create 2017/10/27
 **/
public class KeyPercentRateLimiterTest {
    private KeyPercentStrategy keyPercentStrategy;
    private String resource = "/abc";
    @Before
    public void before(){
        keyPercentStrategy = new KeyPercentStrategy();


//        KeyPercentStrategy.TapConfig config = new KeyPercentStrategy.TapConfig(resource, 8365);
//        keyPercentStrategy.addOrRefreshConfig(config);
    }

    /**
     * hash 方法测试
     */
    @Test
    public void testHash(){
        for (int i = 0; i < 10; i++) {
            String uuid = UUID.randomUUID().toString();
            int hash = keyPercentStrategy.hashInt32Val(uuid);
            System.out.println(String.format("hash %s = %s", uuid, hash));
            Assert.assertTrue(hash>0);
        }

    }

    /**
     * match测试
     */
    @Test
    public void testMatch(){
        String uid = "1234567890";
        int hash = keyPercentStrategy.hashInt32Val(uid);
        System.out.println("hash=" + hash);
        keyPercentStrategy.setPercent(65);
        Assert.assertTrue(keyPercentStrategy.match(resource+ uid));
        keyPercentStrategy.setPercent(64);
        Assert.assertFalse(keyPercentStrategy.match(resource+uid));

    }
}
