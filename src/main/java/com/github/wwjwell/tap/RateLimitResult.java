package com.github.wwjwell.tap;

import com.github.wwjwell.tap.strategy.StrategyResult;

import java.io.Serializable;
import java.util.List;

/**
 * 限流结果
 * @author wwj
 * Create 2017/10/27
 **/
public class RateLimitResult implements Serializable {
    private static final long serialVersionUID = -4598144315985642818L;

    /**
     * 拒绝
     */
    public static final boolean RESULT_REJECT = false;
    /**
     * 通过
     */
    public static final boolean RESULT_ACCESS = true;

    public static final RateLimitResult ACCESS = new RateLimitResult(null, null, RESULT_ACCESS);

    /**
     * 是否限流
     */
    private boolean result;

    /**
     * 资源
     */
    private String resource;

    /**
     * 匹配策略Resource
     */
    private String channelResource;

    /**
     * 使用的策略
     */
    private List<StrategyResult> strategyResultList;

    private StrategyResult rejectStrategyResult;

    public RateLimitResult(String resource, String channelResource, boolean result) {
        this.resource = resource;
        this.channelResource = channelResource;
        this.result = result;
    }

    /**
     * 是否通过
     * @return
     */
    public boolean isAccess() {
        return result == RESULT_ACCESS;
    }

    /**
     * 是否限制
     * @return
     */
    public boolean isReject(){
        return !isAccess();
    }
    /**
     * 是否限流
     * @return
     */

    public void setResult(boolean result) {
        this.result = result;
    }

    public List<StrategyResult> getStrategyResultList() {
        return strategyResultList;
    }

    public void setStrategyResultList(List<StrategyResult> strategyResultList) {
        this.strategyResultList = strategyResultList;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getChannelResource() {
        return channelResource;
    }

    public void setChannelResource(String channelResource) {
        this.channelResource = channelResource;
    }

    public void setRejectStrategyResult(StrategyResult rejectStrategyResult) {
        this.rejectStrategyResult = rejectStrategyResult;
    }

    public StrategyResult getRejectStrategyResult() {
        return rejectStrategyResult;
    }

    @Override
    public String toString() {
        return "RateLimitResult{" +
                "result=" + (result==RESULT_ACCESS?"access":"reject") +
                ", resource='" + resource + '\'' +
                ", channelResource='" + channelResource + '\'' +
                ", strategyResultList=" + strategyResultList +
                '}';
    }
}
