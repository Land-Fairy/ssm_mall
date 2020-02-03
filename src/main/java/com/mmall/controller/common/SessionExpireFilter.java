package com.mmall.controller.common;


import com.mmall.common.Const;
import com.mmall.pojo.User;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisPoolUtil;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 作用: 相关的所有请求，如果带有用户信息的，就需要更新 Redis中 token 的过期时间
 */
public class SessionExpireFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest servletRequest = (HttpServletRequest) request;

        String loginToken = CookieUtil.readLoginToken(servletRequest);
        if (StringUtils.isNotEmpty(loginToken)) {
            String s = RedisPoolUtil.get(loginToken);
            User user = JsonUtil.string2Obj(s, User.class);
            if (user != null) {
                /* 如果 user 不为空，则重置 session 的时间 */
                RedisPoolUtil.expire(loginToken, Const.RedisCacheExtime.REDIS_SESSION_EXTIME);
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}
