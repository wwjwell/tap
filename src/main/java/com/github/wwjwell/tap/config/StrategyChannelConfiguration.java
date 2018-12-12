package com.github.wwjwell.tap.config;

import com.github.wwjwell.tap.channel.StrategyChannel;
import com.github.wwjwell.tap.strategy.Strategy;
import com.github.wwjwell.tap.channel.DefaultStrategyChannel;
import com.github.wwjwell.tap.utils.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author wwj
 * Create 2017/11/01
 **/
public class StrategyChannelConfiguration implements Serializable{
    private static final Logger logger = LoggerFactory.getLogger(StrategyChannelConfiguration.class);
    private static final long serialVersionUID = -1037157621880936024L;
    private String resource;
    private boolean enable = false;
    private Collection<StrategyConfiguration> strategies;

    public StrategyChannel build(){
        if (CommonUtil.isNullOrEmpty(resource)) {
            return null;
        }
        DefaultStrategyChannel channel = new DefaultStrategyChannel();
        channel.setResource(resource);
        channel.setEnable(enable);
        if (isEnable()) {
            if(!CommonUtil.isNullOrEmpty(strategies)){
                for (StrategyConfiguration limiterConfiguration : strategies) {
                    Strategy strategy = limiterConfiguration.build();
                    if (null != strategy) {
                        channel.addStrategy(strategy);
                    }
                }
            }else{
                logger.warn("resource={} channel's strategies is empty", resource);
            }
        }else {
            logger.info("resource={} strategyChannel enable=false", resource);
        }
        return channel;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public Collection<StrategyConfiguration> getStrategies() {
        return strategies;
    }

    public void setStrategies(Collection<StrategyConfiguration> strategies) {
        this.strategies = strategies;
    }

    @Override
    public String toString() {
        return "StrategyChannelConfiguration{" +
                "resource='" + resource + '\'' +
                ", enable=" + enable +
                ", strategies=" + strategies +
                '}';
    }
}
