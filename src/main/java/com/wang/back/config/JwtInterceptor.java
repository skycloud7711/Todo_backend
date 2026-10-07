package com.wang.back.config;

import com.wang.back.common.JwtUtil;
import com.wang.back.common.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
//登陆凭证拦截
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    //请求到达Controller之前执行，true放行，false拦截
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        //放行OPTIONS请求（跨域预检）
        //浏览器的跨域预检请求是OPTIONS，不带token。如果不放行，预检失败，真正的请求根本发不出去
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");  //拿请求头
        if (token == null || !token.startsWith("Bearer ")) {      //检查有没有、格式对不对，'!'为取反
            response.setStatus(401);
            return false;
        }

        try {
            Long userId = jwtUtil.getUserId(token.substring(7));    //从第8个字符开始截取，然后解析出userId
            UserContext.setUserId(userId);  //存进userId
            return true;
        } catch (Exception e) {
            response.setStatus(401);
            return false;
        }
    }

    @Override
    //请求结束，把当前线程里存的用户id删掉，即删掉token
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }
}