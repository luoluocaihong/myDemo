package com.luoluocaihong.wechat.learn.template.aware;

import com.luoluocaihong.wechat.learn.template.util.SpringContext;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

/**
 * Created by xh on 2019/4/25.
 */
@Service
public class SpringContextAware implements ApplicationContextAware {

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContext.setApplicationContext(applicationContext);
    }
}
