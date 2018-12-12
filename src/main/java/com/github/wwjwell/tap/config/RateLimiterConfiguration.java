package com.github.wwjwell.tap.config;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author wwj
 * Create 2017/11/01
 **/
public class RateLimiterConfiguration implements Serializable{
    private static final long serialVersionUID = 458783342171385529L;
    private String name;
    private boolean enable = false;
    private String gatedIps;
    private Collection<StrategyChannelConfiguration> channels;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public Collection<StrategyChannelConfiguration> getChannels() {
        return channels;
    }

    public void setChannels(Collection<StrategyChannelConfiguration> channels) {
        this.channels = channels;
    }

    public String getGatedIps() {
        return gatedIps;
    }

    public void setGatedIps(String gatedIps) {
        this.gatedIps = gatedIps;
    }

    @Override
    public String toString() {
        return "RateLimiterConfiguration{" +
                "name='" + name + '\'' +
                ", enable=" + enable +
                ", gatedIps='" + gatedIps + '\'' +
                ", channels=" + channels +
                '}';
    }
}