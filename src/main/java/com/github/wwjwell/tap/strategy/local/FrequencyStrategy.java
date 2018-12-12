package com.github.wwjwell.tap.strategy.local;

import com.github.wwjwell.tap.strategy.AbstractStrategy;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.github.wwjwell.tap.strategy.StrategyResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 单机频率限制
 * TIPS: 如果key特别多的情况，或者路由策略随机的分布式集群，请选择集群限频，用redis来存储各个请求
 * @author wwj
 * Create 2017/11/10
 **/
public class FrequencyStrategy extends AbstractStrategy {
    private static final Logger logger = LoggerFactory.getLogger(FrequencyStrategy.class);
    /**
     * 间隔时间，毫秒为单位
     */
    protected int intervalMilliseconds;
    /**
     * 本地缓存
     */
    protected Cache<String,Long> lastTimeCache;

    /**
     * 缓存队列最大值,在间隔时间内预估 请求量不超过10万个key,
     * 如果key特别多的情况，不适用单机限频，适用redis的集群限频
     */
    protected int maxCacheSize = 100000;

    @Override
    public void init() {
        lastTimeCache = CacheBuilder.newBuilder()
                .maximumSize(maxCacheSize)
                .expireAfterWrite(intervalMilliseconds, TimeUnit.MILLISECONDS)
                .build();
    }

    @Override
    public void doAcquire(StrategyResult strategyResult, String resource, Map<String, Object> attachment) {
        String acquireKey = getAcquireKey(resource, key, attachment);

        long now = System.currentTimeMillis();
        try {
            if(checkIsFrequencyLimit(acquireKey,now)){
                if (logger.isDebugEnabled()) {
                    logger.debug("resource={}, acquireKey={} request too fast,limit intervalMilliseconds={}ms", resource, acquireKey, intervalMilliseconds);
                }
                strategyResult.reject();
                strategyResult.setMsg("resource=" + resource + ", acquireKey=" + acquireKey + " request too fast");
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("resource={}, acquireKey={} pass", resource, acquireKey);
                }
                strategyResult.access();
                strategyResult.setMsg("pass");
                updateLastTime(acquireKey, now);
            }
        } catch (Exception e) {
            logger.error("FrequencyStrategy acquire error", e);
            strategyResult.access();
            strategyResult.setMsg("FrequencyStrategy acquire error, acquireKey="+acquireKey);
            updateLastTime(acquireKey, now);
        }
    }

    /**
     * 检查是否频率限制
     * @param acquireKey
     * @return true表示有限制，false 无限制
     */
    protected boolean checkIsFrequencyLimit(String acquireKey,long now) {
        Long lastTime = lastTimeCache.getIfPresent(acquireKey);
        if(null == lastTime || (now - lastTime > intervalMilliseconds)){
            return false;
        }
        return true;
    }


    protected void updateLastTime(String acquireKey,long now) {
        lastTimeCache.put(acquireKey, now);
    }

    public int getIntervalMilliseconds() {
        return intervalMilliseconds;
    }

    public void setIntervalMilliseconds(int intervalMilliseconds) {
        this.intervalMilliseconds = intervalMilliseconds;
    }

    public void setMaxCacheSize(int maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
    }

    @Override
    public String toString() {
        return "FrequencyStrategy{" +
                "key='" + key + '\'' +
                ", intervalMilliseconds=" + intervalMilliseconds +
                ", maxCacheSize=" + maxCacheSize +
                '}';
    }
}
