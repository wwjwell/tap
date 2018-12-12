package com.github.wwjwell.tap.global;

import org.springframework.context.ApplicationContext;

/**
 * @author : wwj
 * Create 2017/11/28
 **/
public class SpringHelper {
    private ApplicationContext context;

    private static volatile SpringHelper instance;

    private SpringHelper(ApplicationContext context){
        this.context = context;
    }

    public static void init(ApplicationContext context) {
        if (null == instance) {
            synchronized (SpringHelper.class) {
                if (null == instance) {
                    instance = new SpringHelper(context);
                }
            }
        }
    }

    public static SpringHelper getInstance() {
        if (instance == null) {
            throw new RuntimeException("SpringHelper not initialized");
        }
        return instance;
    }

    public ApplicationContext getContext() {
        return context;
    }
}
