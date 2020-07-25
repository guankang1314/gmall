package com.atguan.gmall.config;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebMvcConfiguration extends WebMvcConfigurerAdapter {


    @Autowired
    private AuthInterceptor authInterceptor;

    //拦截器
    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        //配置拦截器
        registry.addInterceptor(authInterceptor).addPathPatterns("/**");

        //添加拦截器
        super.addInterceptors(registry);
    }

}
