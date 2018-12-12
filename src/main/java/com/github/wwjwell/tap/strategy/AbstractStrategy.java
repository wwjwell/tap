package com.github.wwjwell.tap.strategy;

import com.github.wwjwell.tap.utils.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * abstract Strategy ,只需要重写doAcquire 方法
 * @author wwj
 * Create 2017/10/27
 **/
public abstract class AbstractStrategy implements Strategy{
    private static final Logger logger = LoggerFactory.getLogger(AbstractStrategy.class);

    protected String key;

    /**
     * 如果存在key的情况下，resource是否追究resource部分
     * true : resource = resource +=
     */
    protected boolean keyAppendResource = true;

    public AbstractStrategy() {
    }

    /**
     * 在tap初始化，或每次配置变更时，会调用init方法初始化Strategy
     */
    @Override
    public void init() {
    }

    /**
     * 对象销毁时调用，每次配置变更会生成对象，销毁当前对象和对象所产生的资源，防止内存泄露和资源浪费
     * @throws Exception
     */
    @Override
    public void destroy() throws Exception{
    }


    /**
     * 限流逻辑
     * @param resource ：限流资源信息，可以是url，或自定义资源名
     * @param attachment : 附件信息，对于复杂的流控策略，需要附带一些参数，比如根据uid的百分比限流，需要把uid信息带入
     * @return 返回StrategyResult.isReject 表示限流，否则表示通过
     */
    @Override
    public StrategyResult acquire(String resource, Map<String, Object> attachment) {
        StrategyResult strategyResult = new StrategyResult(StrategyResult.Result.ACCESS);
        strategyResult.setStrategy(this);
        //具体限流算法逻辑，交由各个实现类实现
        try {
            doAcquire(strategyResult, resource, attachment);
            return strategyResult;
        } catch (Exception e) {
            strategyResult.access();
            strategyResult.setMsg(e.getMessage());
            String name = CommonUtil.getShortClassName(this);
            logger.error("resource={}, strategy={} error", resource, name, e);
        }
        return strategyResult;
    }


    /**
     * 限流逻辑
     * @param strategyResult : 限流信息放到限流信息里
     * @param resource       ：限流资源信息，可以是url，或自定义资源名，该值必须和管理平台配置的资源名一致
     * @param attachment     : 附件信息，对于复杂的流控策略，需要附带一些参数，比如根据uid的百分比限流，需要把uid信息带入
     * @return
     */
    public abstract void doAcquire(StrategyResult strategyResult, String resource, Map<String, Object> attachment);

    /**
     * 获取最终执行限流的AcquireKey
     * @param resource
     * @param attachment
     * @return
     */
    protected String getAcquireKey(String resource, String key, Map<String, Object> attachment) {
        String acquireKey = resource;
        if (!CommonUtil.isNullOrEmpty(attachment) && !CommonUtil.isNullOrEmpty(key)) {
            Object value = attachment.get(key);
            if(null != value) {
                if(keyAppendResource){
                    acquireKey = resource + ":" + String.valueOf(value);
                }else{
                    acquireKey = resource;
                }
            }
        }
        return acquireKey;
    }

    public boolean isKeyAppendResource() {
        return keyAppendResource;
    }

    public void setKeyAppendResource(boolean keyAppendResource) {
        this.keyAppendResource = keyAppendResource;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
