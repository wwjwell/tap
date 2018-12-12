package com.github.wwjwell.tap;

import com.github.wwjwell.tap.constant.Constants;
import com.github.wwjwell.tap.utils.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * @author wwj
 * Create 2017/11/07
 **/
public class Tap {
    private static Logger logger = LoggerFactory.getLogger(Tap.class);
    private RateLimiter rateLimiter;
    private String name;

    private Tap (String name){
        this.name = name;
    }

    public static class Builder{
        private String configLocation;
        /**
         * tap名称
         */
        private String name;


        /**
         * 本地tap配置路径
         * @param configLocation
         * @return
         */
        public Builder configLocation(String configLocation){
            this.configLocation = configLocation;
            return this;
        }




        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Tap build(){
            String config = null;

            //本地配置不为空，使用本地配置
            if (!CommonUtil.isNullOrEmpty(configLocation)) {
                InputStream inputStream = Tap.class.getResourceAsStream(configLocation);
                config = CommonUtil.readAll(inputStream, Constants.UTF_8);
            }

            //初始化redis,如果使用到集群则需要初始化
//            initRedisHelper();

            Tap tap = new Tap(this.name);
            tap.builderRateLimiter(config);
            logger.info("tap name={} initialized", tap.name);
            return tap;
        }
    }

    /**
     * 根据配置来生成
     * @return
     */
    private RateLimiter builderRateLimiter(String config) {
        rateLimiter = new RateLimiter();
        rateLimiter.init(config);
        return rateLimiter;
    }


    /**
     * 获得RateLimiter
     * @return
     */
    public RateLimiter rateLimiter(){
        return rateLimiter;
    }
}
