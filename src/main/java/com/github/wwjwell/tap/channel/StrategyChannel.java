package com.github.wwjwell.tap.channel;

import com.github.wwjwell.tap.strategy.StrategyResult;

import java.util.List;
import java.util.Map;

/**
 * 策略channel
 * @author : wwj
 * Create 2017/11/15
 **/
public interface StrategyChannel {
    /**
     * 初始化
     */
    void init();

    /**
     * 申请资源，如果资源不被限流策略允许放行，则直接放行
     * 如果某一个被某一个限流策略限制，则返回限流
     * @param resource
     * @param attachment
     * @return
     */
    List<StrategyResult> acquire(String resource, Map<String, Object> attachment);

    /**
     * 获得资源信息
     * @return
     */
    String getResource();

    /**
     * 开关状态
     * @return
     */
    boolean isEnable();

    /**
     * 销毁清理工作
     * @throws Exception
     */
    void destroy() throws Exception;
}
