package com.github.wwjwell.tap.exception;

import com.github.wwjwell.tap.RateLimitResult;
import com.github.wwjwell.tap.strategy.StrategyResult;
import com.github.wwjwell.tap.utils.CommonUtil;

import java.util.List;

/**
 * @author wwj
 * Create 2017/11/07
 **/
public class TapThrowable extends Throwable{
    private RateLimitResult rateLimitResult;


    public TapThrowable(RateLimitResult rateLimitResult) {
        this(null, rateLimitResult);
    }

    public TapThrowable(String message, RateLimitResult rateLimitResult) {
        super((CommonUtil.isNullOrEmpty(message) ? "" : (message)) + "\t" + rateLimitResult.toString());
        this.rateLimitResult = rateLimitResult;
    }

    public List<StrategyResult> getStrategyResultList(){
        if (null != rateLimitResult) {
            return rateLimitResult.getStrategyResultList();
        }
        return null;
    }

    public RateLimitResult getRateLimitResult() {
        return rateLimitResult;
    }
}
