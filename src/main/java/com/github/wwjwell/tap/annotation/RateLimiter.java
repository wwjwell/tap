package com.github.wwjwell.tap.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author wwj
 * Create 2017/11/01
 **/

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimiter {
    /**
     * 资源名称
     * @return
     */
    String resource();

    /**
     * 拒绝方式
     * @return
     */
    RejectType rejectType() default RejectType.Null;

    /**
     * RejectType.FallBack 时必传
     * @return
     */
    String fallBackMethod() default "";
    /**
     * 限流拒绝后执行的策略
     */
    enum RejectType{
        /**
         * 抛出TapException
         */
        Exception,

        /**
         * 直接返回空
         */
        Null,

        /**
         * 后备方法名
         */
        FallBack,
    }
}
