package com.github.wwjwell.tap.config;

import com.alibaba.fastjson.JSONObject;
import com.github.wwjwell.tap.strategy.Strategy;
import com.github.wwjwell.tap.utils.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * @author wwj
 * Create 2017/11/01
 **/
public class StrategyConfiguration implements Serializable{
    private static final Logger logger = LoggerFactory.getLogger(StrategyConfiguration.class);
    private static final long serialVersionUID = -373600571077786752L;
    private String strategyClassName;
    private String config;

    public Strategy build(){
        if (CommonUtil.isNullOrEmpty(strategyClassName)) {
            logger.warn("strategyClassName={} is empty", strategyClassName);
            return null;
        }
        try {
            Class<?> clazz = Class.forName(strategyClassName);
            Object object;
            if (!CommonUtil.isNullOrEmpty(config)) {
                object = JSONObject.parseObject(config, clazz);
            }else{
                object = clazz.newInstance();
                logger.info("strategyClassName={} config is empty, do clazz.newInstance()", strategyClassName);
            }
            if (object instanceof Strategy) {
                return (Strategy) object;
            }else{
                logger.error("strategyClassName={} not instance of Strategy", strategyClassName);
            }
        } catch (Exception e) {
            logger.error("build strategyClassName={} error",strategyClassName, e);
        }
        return null;
    }

    public String getStrategyClassName() {
        return strategyClassName;
    }

    public void setStrategyClassName(String strategyClassName) {
        this.strategyClassName = strategyClassName;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    @Override
    public String toString() {
        return "StrategyConfiguration{" +
                "strategyClassName='" + strategyClassName + '\'' +
                ", config='" + config + '\'' +
                '}';
    }
}
