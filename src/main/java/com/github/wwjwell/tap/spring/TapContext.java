package com.github.wwjwell.tap.spring;

import com.github.wwjwell.tap.RateLimiter;
import com.github.wwjwell.tap.global.SpringHelper;
import com.github.wwjwell.tap.constant.Constants;
import com.github.wwjwell.tap.utils.CommonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * spring 管理tap
 * @author wwj
 * Create 2017/11/07
 **/
public class TapContext extends AbstractAutoProxyCreator implements BeanFactoryPostProcessor, ApplicationContextAware, InitializingBean, DisposableBean{
    private static final Logger logger = LoggerFactory.getLogger(TapContext.class);

    /**
     * 默认rateLimiter 注册spring的 beanName
     */
    public static final String DEFAULT_SPRING_RATE_LIMITER_NAME = "tapRateLimiter";

    private ApplicationContext context;
    private RateLimiter rateLimiter;
    /**
     * name
     */
    private String name;

    /**
     * 基于文件配置
     */
    private String configLocation;


    private String springRateLimiterBeanName = DEFAULT_SPRING_RATE_LIMITER_NAME;

    /**
     * 获得RateLimiter
     * @return
     */
    public RateLimiter rateLimiter(){
        return rateLimiter;
    }

    /**
     * 配置文件初始化
     * @return
     */
    private String initLocationAndLoadConfig() throws Exception {
        if (CommonUtil.isNullOrEmpty(configLocation)) {
            return null;
        }
        ClassPathResource classPathResource = new ClassPathResource(configLocation);
        return CommonUtil.readAll(classPathResource.getInputStream(), Constants.UTF_8);
    }


    private RateLimiter initRateLimiter(String name,String config){
        this.rateLimiter = context.getAutowireCapableBeanFactory().createBean(RateLimiter.class);
        this.rateLimiter.setName(name);
        this.rateLimiter.init(config);
        return this.rateLimiter;

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        //默认从配置文件读取信息，如果配置文件不存在，从mcc读取配置
        String config = initLocationAndLoadConfig();

        initRateLimiter(name, config);
        rateLimiterAdvisor.setRateLimiter(rateLimiter);
    }

    @Override
    public void destroy() throws Exception {
        if (null != rateLimiter) {
            rateLimiter.destroy();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
        SpringHelper.init(applicationContext);
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        beanFactory.registerSingleton(springRateLimiterBeanName, rateLimiter);
    }

    private RateLimiterAdvisor rateLimiterAdvisor = new RateLimiterAdvisor();

    @Override
    protected Object[] getAdvicesAndAdvisorsForBean(Class<?> beanClass, String beanName, TargetSource customTargetSource) throws BeansException {
        Method[] methods = ReflectionUtils.getAllDeclaredMethods(beanClass);
        boolean hasAnnotationRateLimiter = false;
        if (methods != null) {
            for (Method method : methods) {
                com.github.wwjwell.tap.annotation.RateLimiter annotation = AnnotationUtils.findAnnotation(method, com.github.wwjwell.tap.annotation.RateLimiter.class);
                if (null != annotation) {
                    hasAnnotationRateLimiter = true;
                    break;
                }
            }
        }
        if(hasAnnotationRateLimiter) {
            return new Object[]{rateLimiterAdvisor};
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getConfigLocation() {
        return configLocation;
    }

    public void setConfigLocation(String configLocation) {
        this.configLocation = configLocation;
    }

    public RateLimiterAdvisor getRateLimiterAdvisor() {
        return rateLimiterAdvisor;
    }

    public void setRateLimiterAdvisor(RateLimiterAdvisor rateLimiterAdvisor) {
        this.rateLimiterAdvisor = rateLimiterAdvisor;
    }
}
