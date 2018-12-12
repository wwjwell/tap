package com.github.wwjwell.tap;

import com.alibaba.fastjson.JSONObject;
import com.github.wwjwell.tap.channel.StrategyChannel;
import com.github.wwjwell.tap.config.StrategyChannelConfiguration;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.github.wwjwell.tap.channel.EmptyStrategyChannel;
import com.github.wwjwell.tap.config.RateLimiterConfiguration;
import com.github.wwjwell.tap.strategy.StrategyResult;
import com.github.wwjwell.tap.utils.CommonUtil;
import com.github.wwjwell.tap.utils.NetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 流量控制
 * RateLimiter 根据json配置生成 生成resource组合的限流策略
 * 配置更新，只需要调用refreshConfig接口即可刷新
 * 当执行rateLimiter.acquire(resource,attachment)时，会
 * @author wwj
 * Create 2017/10/27
 **/
public class RateLimiter{
    private static final Logger logger = LoggerFactory.getLogger(RateLimiter.class);

    /**
     * 默认资源缓存最大缓存条数
     */
    private final int DEFAULT_RESOURCE_CHANNEL_CACHE_SIZE = 10000;
    /**
     * 默认缓存失效时间 60s
     */
    private final int DEFAULT_RESOURCE_CHANNEL_CACHE_DURATION = 60;
    /**
     * 灰度上线IP分隔符{@code ,或，}
     */
    public static final String GETED_IP_SEPARATER = ",|，";

    /**
     * 缓存最大缓存条数
     */
    private int resourceChannelCacheSize = DEFAULT_RESOURCE_CHANNEL_CACHE_SIZE;
    /**
     * 缓存失效时间
     */
    private int resourceChannelCacheDuration = DEFAULT_RESOURCE_CHANNEL_CACHE_DURATION;

    /**
     * url路径前缀
     */
    private static final String PREFIX_PATH = "/";

    private String name;
    /**
     * 限流策略
     */
    private boolean enable = false;
    /**
     * cache 缓存,LRUCache
     */
    private Cache<String, StrategyChannel> resourceChannelLRUCache;

    /**
     * 决策map，用于equal匹配
     */
    private Map<String,StrategyChannel> channelMap;

    /**
     * 空策略
     */
    private final EmptyStrategyChannel emptyStrategyChannel = new EmptyStrategyChannel();

//    private AntPathMatcher antPathMatcher = new AntPathMatcher();
    private StrategyTrie strategyTrie;

    /**
     * 本机IP地址
     */
    private Set<String> localIpSet;

    /**
     * 需要灰度发布的IP
     */
    private Set<String> gatedIps;
    /**
     * 刷新配置
     * @param config ：json string
     */
    public boolean refreshConfig(String config){
        try {
            RateLimiterConfiguration tapConfiguration = JSONObject.parseObject(config, RateLimiterConfiguration.class);
            boolean result = refreshConfig(tapConfiguration);
            return result;
        } catch (Exception e) {
            logger.error("RateLimiter refresh config error, config={}", config, e);
        }
        return false;
    }

    /**
     * 刷新配置
     */
    public boolean refreshConfig(RateLimiterConfiguration config) {
        if (config == null) {
            logger.warn("RateLimiter RateLimiterConfiguration is null");
            return false;
        }else{
            logger.info("RateLimiter RateLimiterConfiguration : {}", config);
        }
        //开关判断
        boolean result = true;
        boolean tempEnable = config.isEnable();
        //灰度发布的ip地址
        Set<String> tempGatedIps = parseGateIps(config.getGatedIps());
        //如果设置了灰度ip属性，并且本机ip(线下多块网卡，有多个ip)不在灰度ip内，禁用开关
        if (!CommonUtil.isNullOrEmpty(tempGatedIps) && !CommonUtil.isNullOrEmpty(localIpSet)) {
            tempEnable = false;
            for (String localIp : localIpSet) {
                if(tempGatedIps.contains(localIp)){
                    tempEnable = true;
                    break;
                }
            }
        }
        Map<String,StrategyChannel> tempChannelMap = Maps.newHashMap();
        StrategyTrie tempStrategyTrie = new StrategyTrie();
        if (!tempEnable) {
            logger.info("RateLimiter refresh config finish, enable=false");
        }else{
            //channel 加载
            if (!CommonUtil.isNullOrEmpty(config.getChannels())) {
                List<StrategyChannel> channels = Lists.newArrayList();
                for (StrategyChannelConfiguration channelConf : config.getChannels()) {
                    if (CommonUtil.isNullOrEmpty(channelConf.getResource())) {
                        logger.warn("RateLimiter config illegal, resource property is empty");
                        continue;
                    }
                    //策略加载
                    StrategyChannel channel = channelConf.build();
                    try {
                        if (null != channel) {
                            //执行策略初始化
                            channel.init();
                            channels.add(channel);
                            if (logger.isDebugEnabled()) {
                                logger.debug("RateLimiter build resource={} strategyChannel={} finish", channel.getResource(), channel);
                            }
                        } else {
                            logger.warn("RateLimiter build strategyChannel failed, channelConf={}", channelConf);
                        }
                    } catch (Exception e) {
                        logger.error("RateLimiter build strategyChannel error", e);
                    }
                }
                //生成策略缓存map
                tempStrategyTrie.buildTree(channels);
                for (StrategyChannel channel : channels) {
                    tempChannelMap.put(channel.getResource(), channel);
                }
            }else{
                logger.warn("RateLimiter config illegal, config.name={} strategyChannels is empty", config.getName());
            }
        }

        /**
         * 考虑到线程并发情况，刷新策略步骤如下
         * 1、保存原有策略对象到 destroyStrategyList
         * 2、新策略替换原有策略缓存 channelMap = tempChannelMap
         * 4、删除旧resource<->channel缓存
         * 5、销毁旧策略 destroyOldChannel
         */
        Collection<StrategyChannel> destroyStrategyList = null;
        if (null != this.channelMap) {
            destroyStrategyList = this.channelMap.values();
        }
        this.enable = tempEnable;
        this.gatedIps = tempGatedIps;
        this.channelMap = tempChannelMap;
        this.strategyTrie = tempStrategyTrie;
        //删除旧缓存
        resourceChannelLRUCache.invalidateAll();
        //销毁策略
        destroyOldChannel(destroyStrategyList);
        logger.info("RateLimiter refresh success,strategies={}", tempChannelMap.values());
        return result;
    }

    /**
     * 销毁旧策略对象，执行 strategy.destroy()方法
     * @param channels
     */
    private void destroyOldChannel(Collection<StrategyChannel> channels) {
        if (CommonUtil.isNullOrEmpty(channels)) {
            return;
        }
        for (StrategyChannel channel : channels) {
            try {
                channel.destroy();
            } catch (Exception e) {
                logger.error("resource={} destroy error", channel.getResource(), e);
            }
        }
    }

    /**
     * 根据string解析灰度ip地址
     * @param gatedIps
     * @return
     */
    private Set<String> parseGateIps(String gatedIps) {
        if (CommonUtil.isNullOrEmpty(gatedIps)) {
            return null;
        }
        String[] ips = gatedIps.split(GETED_IP_SEPARATER);
        Set<String> ipSet = Sets.newHashSet();
        for (String ip : ips) {
            if (!CommonUtil.isNullOrEmpty(ip)) {
                ipSet.add(ip);
            }
        }
        return ipSet;
    }

    /**
     * 对于resource 进行限流
     */
    public RateLimitResult acquire(String resource) {
        return acquire(resource, null);
    }

    /**
     * 对于resource 进行限流
     * @param resource 资源标识
     * @param attachment 附件内容，会传递到流控策略使用
     * @return
     */
    public RateLimitResult acquire(String resource, Map<String, Object> attachment){
        //开关判断
        if (!isEnable()) {
            if (logger.isDebugEnabled()) {
                logger.debug("rateLimiter enable=false, resource={} access", resource);
            }
            return RateLimitResult.ACCESS;
        }

        if (CommonUtil.isNullOrEmpty(resource)) {
            logger.warn("resource is empty string");
            return RateLimitResult.ACCESS;
        }
        StrategyChannel channel = getChannelFromCache(resource);
        if (null == channel) {
            //寻找最优策略通道
            channel = match(resource);
            if (null == channel) {
                channel = emptyStrategyChannel;
            }
            setChannelCache(resource, channel);
        }
        //空的strategy 不限流
        if (channel == emptyStrategyChannel) {
            return new RateLimitResult(resource, "", RateLimitResult.RESULT_ACCESS);
        }

        RateLimitResult rateLimitResult = new RateLimitResult(resource, channel.getResource(), RateLimitResult.RESULT_ACCESS);

        try {
            List<StrategyResult> strategyResultList = channel.acquire(resource, attachment);
            /**
             * 没有限流策略，直接通过，
             * 存在限流策略，有一个限制，则拒绝访问
             */
            rateLimitResult.setResult(RateLimitResult.RESULT_ACCESS);
            if (!CommonUtil.isNullOrEmpty(strategyResultList)) {
                rateLimitResult.setStrategyResultList(strategyResultList);
                //通过与否，直接看最后一个策略的结果就可以
                StrategyResult strategyResult = strategyResultList.get(strategyResultList.size() - 1);
                if (strategyResult.isReject()) {
                    rateLimitResult.setResult(RateLimitResult.RESULT_REJECT);
                    rateLimitResult.setRejectStrategyResult(strategyResult);
                }
            }
        } catch (Exception e) {
            logger.error("", e);
            //异常直接放过
            rateLimitResult.setResult(RateLimitResult.RESULT_ACCESS);
        }
        return rateLimitResult;
    }

    private StrategyChannel getChannelFromCache(String resource){
        return resourceChannelLRUCache.getIfPresent(resource);
    }

    private void setChannelCache(String resource, StrategyChannel channel) {
        resourceChannelLRUCache.put(resource, channel);
    }

    /**
     * 最优匹配策略通道
     * 如果是url则匹配最优路径
     * 如果是普通的resource, equal匹配
     * @param resource
     * @return
     */
    private StrategyChannel match(String resource) {
        StrategyChannel channel = null;
        //如果是url的方式，需要寻找最优匹配路径
        if (resource.startsWith(PREFIX_PATH)) {
            channel = strategyTrie.match(resource);
        }else{
            //非url的方式，直接用equal
            channel = channelMap.get(resource);
        }
        return channel;
    }

    /**
     * 缓存初始化
     */
    private void initResourceStrategyCache() {
        resourceChannelLRUCache = CacheBuilder.newBuilder()
                .maximumSize(resourceChannelCacheSize)
                .expireAfterWrite(resourceChannelCacheDuration, TimeUnit.SECONDS)
                .build();
    }


    /**
     * 初始化
     * @param config
     */
    public void init(String config){
        localIpSet = NetUtil.getAllIpv4Address();
        logger.info("RateLimiter localIpSet={}", localIpSet);
        initResourceStrategyCache();
        refreshConfig(config);
    }

    /**
     * 销毁
     */
    public void destroy(){
        if (null != channelMap) {
            destroyOldChannel(channelMap.values());
        }
    }


    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public int getResourceChannelCacheSize() {
        return resourceChannelCacheSize;
    }

    public int getResourceChannelCacheDuration() {
        return resourceChannelCacheDuration;
    }

    public void setResourceChannelCacheDuration(int resourceChannelCacheDuration) {
        this.resourceChannelCacheDuration = resourceChannelCacheDuration;
    }

    public Set<String> getGatedIps() {
        return gatedIps;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<String> getLocalIpSet() {
        return localIpSet;
    }

    @Override
    public String toString() {
        return "RateLimiter{" +
                "resourceChannelCacheSize=" + resourceChannelCacheSize +
                ", resourceChannelCacheDuration=" + resourceChannelCacheDuration +
                ", name='" + name + '\'' +
                ", enable=" + enable +
                ", localIpSet='" + localIpSet + '\'' +
                ", gatedIps=" + gatedIps +
                '}';
    }
}