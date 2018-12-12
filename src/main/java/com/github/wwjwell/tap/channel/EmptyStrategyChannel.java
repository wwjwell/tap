package com.github.wwjwell.tap.channel;

import com.github.wwjwell.tap.strategy.StrategyResult;

import java.util.List;
import java.util.Map;

/**
 * @author : wwj
 * Create 2017/11/15
 **/
public class EmptyStrategyChannel implements StrategyChannel {
    @Override
    public void init() {
    }

    @Override
    public List<StrategyResult> acquire(String resource, Map<String, Object> attachment) {
        return null;
    }

    @Override
    public String getResource() {
        return "nullChannel";
    }

    @Override
    public boolean isEnable() {
        return false;
    }

    @Override
    public void destroy() throws Exception {
    }
}
