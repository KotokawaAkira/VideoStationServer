package com.lingyi.RootGet.filter;

import com.lingyi.RootGet.tools.Constant;
import com.lingyi.RootGet.tools.RedisTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class Interceptor implements HandlerInterceptor {
    private final RedisTools redisTools;

    @Autowired
    public Interceptor(RedisTools redisTools) {
        this.redisTools = redisTools;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String uid = Constant.getUidInCookie(request);
        String token = Constant.getTokenInCookie(request);
        //判断这两项是否为空
        if (!StringUtils.hasText(uid) || !StringUtils.hasText(token)) {
            response.setStatus(401);
            return false;
        }
        //判断redis中的token是否和cookie中的一致
        String accountToken = redisTools.getAccountToken(Long.parseLong(uid));
        if (!StringUtils.hasText(accountToken) || !token.equals(accountToken)) {
            response.setStatus(401);
            return false;
        }
        return true;
    }
}
