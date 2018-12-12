package com.github.wwjwell.tap.strategy.local;

import com.github.wwjwell.tap.strategy.AbstractStrategy;
import com.github.wwjwell.tap.strategy.StrategyResult;
import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 根据某个关键值{@code key} 百分比限流
 * @author wwj
 * Create 2017/10/27
 **/
public class KeyPercentStrategy extends AbstractStrategy {
    private static final Logger logger = LoggerFactory.getLogger(KeyPercentStrategy.class);
    /**
     * 精度保留小数点2位
     */
    private final int PRECISION = 100;

    /**
     * 当前百分比，精确到小数点后2位
     */
    private float percent;

    private int percentInt;

    /**
     * hash
     */
    private HashFunction hashFunction = Hashing.murmur3_32();

    private final int HUNDRED = 100;


    @Override
    public void init() {
        percentInt = (int) (percent * PRECISION);
    }

    @Override
    public void doAcquire(StrategyResult strategyResult, String resource, Map<String, Object> attachment) {
        String acquireKey = getAcquireKey(resource, key, attachment);
        if (match(acquireKey)) {
            strategyResult.access();
            strategyResult.setMsg(acquireKey + " access");
        } else{
            strategyResult.reject();
            strategyResult.setMsg(acquireKey + " reject");
        }

    }

    /**
     * 获取对象的hash值，正整数
     * @param value
     * @return
     */
    public int hashInt32Val(String value) {
        return Math.abs(hashFunction.newHasher().putString(value, Charsets.UTF_8).hash().asInt());
    }

    /**
     * 是否在要求精度之内
     * @param acquireKey
     * @return
     */
    public boolean match(String acquireKey) {
        if (percentInt <= 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("acquireKey={} be denied, percent<=0", acquireKey);
            }
            return false;
        }

        return percentInt >= (hashInt32Val(acquireKey) % (HUNDRED*PRECISION));
    }
    
    public float getPercent() {
        return percent;
    }

    public void setPercent(float percent) {
        this.percent = percent;
    }

    @Override
    public String toString() {
        return "KeyPercentStrategy{" +
                "key='" + key + '\'' +
                ", percent=" + percent +
                ", percentInt=" + percentInt +
                '}';
    }
}
