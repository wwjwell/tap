package com.github.wwjwell.tap.spring;

import com.github.wwjwell.tap.RateLimitResult;
import com.google.common.collect.Maps;
import com.github.wwjwell.tap.annotation.Attachment;
import com.github.wwjwell.tap.exception.RejectTapThrowable;
import com.github.wwjwell.tap.exception.TapThrowable;
import com.github.wwjwell.tap.utils.CommonUtil;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wwj
 * Create 2017/11/08
 **/
public class RateLimiterAdvisor implements MethodInterceptor {
    private com.github.wwjwell.tap.RateLimiter rateLimiter;
    private static final int MAX_CACHE_SIZE = 1000;
    private ConcurrentHashMap<Method, MethodParamAttachment[]> cacheMapping = new ConcurrentHashMap<>();

    public void setRateLimiter(com.github.wwjwell.tap.RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        com.github.wwjwell.tap.annotation.RateLimiter annotation = method.getAnnotation(com.github.wwjwell.tap.annotation.RateLimiter.class);
        Object[] args = invocation.getArguments();
        Object target = invocation.getThis();
        if (null != annotation) {
            MethodParamAttachment[] paramAttachments = getParamAttachments(method);
            String resource = annotation.resource();
            Map<String, Object> attachment = null;

            for (int i = 0; i < paramAttachments.length; i++) {
                if (paramAttachments[i].attachment != null) {
                    if (null == attachment) {
                        attachment = Maps.newHashMap();
                    }
                    String key = paramAttachments[i].attachment.key();
                    String assignValue = paramAttachments[i].attachment.assignValue();
                    attachment.put(key, CommonUtil.isNullOrEmpty(assignValue) ? args[i] : assignValue);
                }
            }
            RateLimitResult rateLimitResult = rateLimiter.acquire(resource, attachment);
            if (rateLimitResult.isReject()) {
                if (annotation.rejectType() == com.github.wwjwell.tap.annotation.RateLimiter.RejectType.Exception) {
                    throw new RejectTapThrowable(rateLimitResult);
                } else if (annotation.rejectType() == com.github.wwjwell.tap.annotation.RateLimiter.RejectType.Null) {
                    return null;
                } else if (annotation.rejectType() == com.github.wwjwell.tap.annotation.RateLimiter.RejectType.FallBack) {
                    String fallBackMethodName = annotation.fallBackMethod();
                    if (CommonUtil.isNullOrEmpty(fallBackMethodName)) {
                        throw new TapThrowable("fallBackMethod=" + fallBackMethodName + " config is empty", rateLimitResult);
                    }

                    Method fallBackMethod = target.getClass().getDeclaredMethod(fallBackMethodName, method.getParameterTypes());
                    if (null == fallBackMethod) {
                        throw new TapThrowable("fallBackMethod=" + fallBackMethodName + " not exist", rateLimitResult);
                    } else if (fallBackMethod.getReturnType() != method.getReturnType()) {
                        throw new TapThrowable("fallBackMethod=" + fallBackMethodName + " returnType illegal", rateLimitResult);
                    }
                    return fallBackMethod.invoke(target, args);
                }
            }
        }
        return invocation.getMethod().invoke(target, args);
    }

    private static class MethodParamAttachment{
        private Attachment attachment;
    }

    private void putCache(Method method,MethodParamAttachment[] paramAttachments) {
        if (cacheMapping.size() > MAX_CACHE_SIZE) {
            return;
        }
        cacheMapping.put(method, paramAttachments);
    }

    private MethodParamAttachment[] buildMethodMapping(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        MethodParamAttachment[] paramAttachments;
        if (null != parameterTypes && parameterTypes.length>0) {
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            paramAttachments = new MethodParamAttachment[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                MethodParamAttachment paramAttachment = new MethodParamAttachment();
                if (null != parameterAnnotations[i]) {
                    for (Annotation annotation : parameterAnnotations[i]) {
                        if (annotation instanceof Attachment) {
                            paramAttachment.attachment = (Attachment)annotation;
                        }
                    }
                }
                paramAttachments[i] = paramAttachment;
            }
        }else{
            paramAttachments = new MethodParamAttachment[0];
        }
        return paramAttachments;
    }

    private MethodParamAttachment[] getParamAttachments(Method method){
        MethodParamAttachment[] paramAttachments = cacheMapping.get(method);
        if (null == paramAttachments) {
            paramAttachments = buildMethodMapping(method);
            putCache(method, paramAttachments);
        }
        return paramAttachments;
    }
}
