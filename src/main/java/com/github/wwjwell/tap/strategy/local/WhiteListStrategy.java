package com.github.wwjwell.tap.strategy.local;

import com.github.wwjwell.tap.strategy.AbstractStrategy;
import com.github.wwjwell.tap.strategy.StrategyResult;
import com.github.wwjwell.tap.utils.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 黑白名单
 * @author : wwj
 * Create 2017/11/20
 **/
public class WhiteListStrategy extends AbstractStrategy {
    private static final Logger logger = LoggerFactory.getLogger(WhiteListStrategy.class);
    private static final String DEFAULT_SEPARATER = ",";
    private Set<String> whiteListSet;
    /**
     * 白名单，多个以{@code split}分隔
     */
    private String whiteList;
    /**
     * 白名单是否后续请求是否都通过
     */
    private boolean ignoreAfterStrategy = false;

    /**
     * 白名单分隔符，默认是{@code ,}
     */
    private String separater = DEFAULT_SEPARATER;

    @Override
    public void init() {
        if (!CommonUtil.isNullOrEmpty(whiteList)) {
            String[] dataArray = whiteList.split(separater);
            if (!CommonUtil.isNullOrEmpty(dataArray)) {
                whiteListSet = new HashSet<>();
                for (String data : dataArray) {
                    if(!CommonUtil.isNullOrEmpty(data)) {
                        whiteListSet.add(data);
                    }
                }
            }
        }
    }

    @Override
    public void doAcquire(StrategyResult strategyResult, String resource, Map<String, Object> attachment) {
        //默认拒绝
        strategyResult.reject();
        strategyResult.setMsg("default reject");
        String acquireKey = getAcquireKey(resource, key, attachment);

        if (!CommonUtil.isNullOrEmpty(whiteListSet)) {
            if (whiteListSet.contains(acquireKey)) {
                //白名单啊，直接全部透过
                if (ignoreAfterStrategy) {
                    strategyResult.accessAndIgnoreAfter();
                } else {
                    strategyResult.access();
                }
                strategyResult.setMsg(acquireKey + " in writeList");
            }
        }else{
            logger.warn("writeList is empty, reject resource={},acquireKey={}", resource, acquireKey);
        }
    }

    public Set<String> getWhiteListSet() {
        return whiteListSet;
    }

    public void setWhiteListSet(Set<String> whiteListSet) {
        this.whiteListSet = whiteListSet;
    }

    public String getSeparater() {
        return separater;
    }

    public void setSeparater(String separater) {
        this.separater = separater;
    }

    public boolean isIgnoreAfterStrategy() {
        return ignoreAfterStrategy;
    }

    public void setIgnoreAfterStrategy(boolean ignoreAfterStrategy) {
        this.ignoreAfterStrategy = ignoreAfterStrategy;
    }

    @Override
    public String toString() {
        return "WhiteListStrategy{" +
                "whiteListSet=" + whiteListSet +
                ", key='" + key + '\'' +
                ", ignoreAfterStrategy=" + ignoreAfterStrategy +
                ", separater='" + separater + '\'' +
                '}';
    }
}
