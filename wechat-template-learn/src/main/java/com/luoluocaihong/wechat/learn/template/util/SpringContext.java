package com.luoluocaihong.wechat.learn.template.util;

import org.springframework.context.ApplicationContext;

/**
 * Created by xh on 2019/4/25.
 */
public abstract class SpringContext {
    private SpringContext(){}
    /**
     * Spring上下文
     */
    private static ApplicationContext applicationContext;

    /**
     * 得到Spring上下文
     *
     * @return ApplicationContext
     * @author henry
     */
    public static ApplicationContext getApplicationContext() {

        return applicationContext;
    }

    /**
     * 设置Spring上下文
     *
     * @param applicationContext Spring上下文
     * @author henry
     */
    public static void setApplicationContext(ApplicationContext applicationContext) {
        if (SpringContext.applicationContext == null) {
            SpringContext.applicationContext = applicationContext;
        }
    }


    public static <T> T getBean(Class<T> beanClass) {
        return getApplicationContext().getBean(beanClass);
    }
}
