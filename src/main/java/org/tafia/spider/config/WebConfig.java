package org.tafia.spider.config;

import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter4;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;

/**
 * Created by Dason on 2017/6/17.
 */
@Configuration
public class WebConfig {

    @Bean
    public FastJsonHttpMessageConverter4 fastJsonHttpMessageConverter() {
        FastJsonHttpMessageConverter4 messageConverter = new FastJsonHttpMessageConverter4();
        messageConverter.setDefaultCharset(StandardCharsets.UTF_8);
        return messageConverter;
    }
}
