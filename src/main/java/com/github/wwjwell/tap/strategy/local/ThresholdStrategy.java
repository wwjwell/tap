package com.github.wwjwell.tap.strategy.local;


import com.github.wwjwell.tap.strategy.AbstractStrategy;
import com.github.wwjwell.tap.strategy.StrategyResult;
import com.google.common.util.concurrent.RateLimiter;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 根据qps限流
 * 使用google的令牌桶算法
 * @author wwj
 * Create 2017/11/03
 **/
public class ThresholdStrategy extends AbstractStrategy {
    private RateLimiter rateLimiter;
    /**
     * 默认QPS=90
     */
    private static final int DEFAULT_QPS = 90;

    /**
     * 单次获取令牌数默认值=1
     */
    private static final int DEFAULT_PERMIT = 1;

    /**
     * 超时时间
     */
    private int timeoutMilliseconds = 200;

    /**
     * QPS
     */
    private int qps = DEFAULT_QPS;

    /**
     * 单次获取令牌数，默认是1
     */
    private int permits = DEFAULT_PERMIT;


    @Override
    public void init() {
        rateLimiter = RateLimiter.create(qps);
    }

    @Override
    public void doAcquire(StrategyResult strategyResult, String resource, Map<String, Object> attachment) {
        if (rateLimiter.tryAcquire(permits, timeoutMilliseconds, TimeUnit.MILLISECONDS)) {
            strategyResult.access();
            strategyResult.setMsg("pass");
        }else{
            strategyResult.reject();
            strategyResult.setMsg("reject");
        }
    }

    public int getQps() {
        return qps;
    }

    public void setQps(int qps) {
        this.qps = qps;
    }

    public int getTimeoutMilliseconds() {
        return timeoutMilliseconds;
    }

    public void setTimeoutMilliseconds(int timeoutMilliseconds) {
        this.timeoutMilliseconds = timeoutMilliseconds;
    }

    public int getPermits() {
        return permits;
    }

    public void setPermits(int permits) {
        this.permits = permits;
    }

    @Override
    public String toString() {
        return "ThresholdStrategy{" +
                "timeoutMilliseconds=" + timeoutMilliseconds +
                ", qps=" + qps +
                ", permits=" + permits +
                '}';
    }
}
