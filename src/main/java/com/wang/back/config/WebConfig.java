package com.wang.back.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    //Spring的跨域请求配置，前后端分离
    public void addCorsMappings(CorsRegistry registry) {
        //链式调用，每个方法都返回this，即对象自己
        registry.addMapping("/**")  //对哪些路径生效
                .allowedOriginPatterns("*")    //允许哪些来源
                .allowedMethods("*")           //允许哪些HTTP方法
                .allowedHeaders("*");          //允许哪些请求头
    }

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    //域名拦截，并注册登录凭证拦截器
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/todo/**")        //拦截todo相关接口
                .excludePathPatterns("/api/auth/**");   //放行注册登录
    }
}