package com.github.wwjwell.tap.channel;

import com.github.wwjwell.tap.strategy.Strategy;
import com.github.wwjwell.tap.strategy.StrategyResult;
import com.github.wwjwell.tap.utils.CommonUtil;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author : wwj
 * Create 2017/11/15
 **/
public class DefaultStrategyChannel implements StrategyChannel{
    private static final Logger logger = LoggerFactory.getLogger(DefaultStrategyChannel.class);
    /**
     * 资源ID
     */
    private String resource;

    /**
     * 开关状态
     */
    private boolean enable = false;

    /**
     * 策略描述
     */
    private List<Strategy> strategies = new ArrayList<>();

    @Override
    public void init() {
        if (!CommonUtil.isNullOrEmpty(strategies)) {
            Iterator<Strategy> strategyIterator = strategies.iterator();
            while (strategyIterator.hasNext()) {
                Strategy strategy = strategyIterator.next();
                try {
                    strategy.init();
                    if (logger.isDebugEnabled()) {
                        logger.debug("resource={} init strategy={}", resource, CommonUtil.getShortClassName(strategy));
                    }
                } catch (Exception e) {
                    logger.error("resource=" + resource + " init strategy=" + CommonUtil.getShortClassName(strategy) + " error", e);
                    strategyIterator.remove();
                }
            }
        }
    }

    /**
     * 申请资源，如果资源不被限流策略允许放行，则直接放行
     * 如果某一个被某一个限流策略限制，则返回限流
     * @param resource
     * @param attachment
     * @return
     */
    @Override
    public List<StrategyResult> acquire(String resource, Map<String, Object> attachment) {
        if (!isEnable()) {
            if (logger.isDebugEnabled()) {
                logger.debug("resource={} strategy enable=false", resource);
            }
            return null;
        }

        List<StrategyResult> strategyResultList = Lists.newArrayList();
        if (!CommonUtil.isNullOrEmpty(strategies)) {
            for (Strategy strategy : strategies) {
                try {
                    StrategyResult strategyResult = strategy.acquire(resource, attachment);
                    strategyResultList.add(strategyResult);
                    /**
                     * 策略执行结果有3种
                     * 1、拒绝，直接拒绝，不执行后续限流策略
                     * 2、通过并忽略后续限流策略，直接通过，不执行后续限流策略
                     * 3、通过，继续执行后续限流策略
                     */
                    if (strategyResult.isReject()) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("resource={} reject by strategy={}", resource, CommonUtil.getShortClassName(strategyResult.getStrategy()));
                        }
                        break;
                    }else if(strategyResult.isAcessAndIgnoreAfter()) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("resource={} accessAndIgnoreAfter by strategy={}", resource, CommonUtil.getShortClassName(strategyResult.getStrategy()));
                        }
                        break;
                    }else{
                        if (logger.isDebugEnabled()) {
                            logger.debug("resource={} access by strategy={}", resource, CommonUtil.getShortClassName(strategyResult.getStrategy()));
                        }
                        continue;
                    }
                } catch (Exception e) {
                    String name = CommonUtil.getShortClassName(strategy);
                    logger.error("resource=" + resource + ",strategy=" + name + " occur error", e);
                }
            }
        } else {
            logger.warn("resource={} none of strategy", resource);
        }
        return strategyResultList;
    }


    @Override
    public void destroy() throws Exception {
        if (CommonUtil.isNullOrEmpty(strategies)) {
            return;
        }
        for (Strategy strategy : strategies) {
            if (null != strategy) {
                try {
                    strategy.destroy();
                    if (logger.isDebugEnabled()) {
                        logger.debug("strategy={} destroy", CommonUtil.getShortClassName(strategy));
                    }
                } catch (Exception e) {
                    logger.error("strategy=" + CommonUtil.getShortClassName(strategy) + " destroy error", e);
                }
            }
        }
        strategies = null;
    }

    @Override
    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    @Override
    public boolean isEnable() {
        return enable;
    }

    public void addStrategy(Strategy strategy) {
        if (null != strategy) {
            strategies.add(strategy);
        }
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public List<Strategy> getStrategies() {
        return strategies;
    }

    public void setStrategies(List<Strategy> strategies) {
        this.strategies = strategies;
    }

    @Override
    public String toString() {
        return "DefaultStrategyChannel{" +
                "resource='" + resource + '\'' +
                ", enable=" + enable +
                ", strategies=" + strategies +
                '}';
    }
}
