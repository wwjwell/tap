package com.github.wwjwell.tap.limiter;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author wwj
 * Create 2017/10/30
 **/
public class WindowsLimiterTest {
    @Test
    public void testWindows(){
        LoadingCache<Long, AtomicLong> counter =
                CacheBuilder.newBuilder()
                        .expireAfterWrite(10, TimeUnit.SECONDS)
                        .build(new CacheLoader<Long, AtomicLong>() {
                            @Override
                            public AtomicLong load(Long seconds) throws Exception {
                                return new AtomicLong(0);
                            }

                        });
        long limit = 1;

        for(int i=0;i<100;i++) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //得到当前秒
            long currentSeconds = System.currentTimeMillis() / 1000;
            try {
                for (Map.Entry<Long, AtomicLong> entry : counter.asMap().entrySet()) {
                    System.out.println("time="+entry.getKey()+",count="+entry.getValue());
                }
                System.out.println("\n size="+counter.size());
                if(counter.get(currentSeconds).incrementAndGet() > limit) {
                    System.out.println("限流了:" + currentSeconds+",count="+counter.size());
                    continue;
                }
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            //业务处理

        }
    }
}
