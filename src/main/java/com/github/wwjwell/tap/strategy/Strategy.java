package com.github.wwjwell.tap.strategy;

import java.util.Map;

/**
 * 限流策略策略
 * @author : wwj
 * Create 2017/11/15
 **/
public interface Strategy {
    /**
     * 在tap初始化，或每次配置变更时，会调用init方法初始化Strategy
     */
    void init();

    /**
     * 限流逻辑
     * @param resource ：限流资源信息，可以是url，或自定义资源名
     * @param attachment : 附件信息，对于复杂的流控策略，需要附带一些参数，比如根据uid的百分比限流，需要把uid信息带入
     * @return 返回StrategyResult.isReject 表示限流，否则表示通过
     */
    StrategyResult acquire(final String resource, final Map<String, Object> attachment);


    /**
     * 对象销毁时调用，每次配置变更会生成对象，销毁当前对象和对象所产生的资源，防止内存泄露和资源浪费
     * @throws Exception
     */
    void destroy() throws Exception;
}
