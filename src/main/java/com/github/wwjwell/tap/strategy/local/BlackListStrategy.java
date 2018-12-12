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
public class BlackListStrategy extends AbstractStrategy {
    private static final Logger logger = LoggerFactory.getLogger(BlackListStrategy.class);
    private static final String DEFAULT_SEPARATER = ",";
    private Set<String> blackListSet;
    /**
     * 黑名单，多个以{@code split}分隔
     */
    private String blackList;

    /**
     * 分隔符 默认是{@code ,}
     */
    private String separater = DEFAULT_SEPARATER;

    @Override
    public void init() {
        if (!CommonUtil.isNullOrEmpty(blackList)) {
            String[] dataArray = blackList.split(separater);
            if (!CommonUtil.isNullOrEmpty(dataArray)) {
                blackListSet = new HashSet<>();
                for (String data : dataArray) {
                    blackListSet.add(data);
                }
            }
        }
    }

    @Override
    public void doAcquire(StrategyResult strategyResult, String resource, Map<String, Object> attachment) {
        //默认通过
        strategyResult.access();
        strategyResult.setMsg("default pass");
        if (!CommonUtil.isNullOrEmpty(blackListSet)) {
            if (CommonUtil.isNullOrEmpty(key) || CommonUtil.isNullOrEmpty(attachment)) {
                logger.warn("resource={} key or attachment is empty, key={},attachment={}", resource, key, attachment);
                strategyResult.access();
                strategyResult.setMsg("key or attachment is null");
                return;
            }

            Object value = attachment.get(key);
            if (value == null) {
                logger.warn("resource={} attachment value is empty, key={}, value={}", resource, key, value);
                strategyResult.access();
                strategyResult.setMsg("value is null");
                return;
            }
            if (blackListSet.contains(value)) {
                strategyResult.reject();
                strategyResult.setMsg("black list");
            }
        }
    }

    public String getBlackList() {
        return blackList;
    }

    public void setBlackList(String blackList) {
        this.blackList = blackList;
    }

    public String getSeparater() {
        return separater;
    }

    public void setSeparater(String separater) {
        this.separater = separater;
    }

    @Override
    public String toString() {
        return "BlackListStrategy{" +
                "blackListSet=" + blackListSet +
                ", key='" + key + '\'' +
                ", separater='" + separater + '\'' +
                '}';
    }
}