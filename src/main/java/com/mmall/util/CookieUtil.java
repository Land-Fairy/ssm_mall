package com.mmall.util;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * TODO: Cookie 操作 Utils
 */
public class CookieUtil {
    private final static String COOKIE_DOMAIN = ".day.com";
    private final static String COOKIE_NAME = "mmall_login_token";

    /**
     * 写入 token
     * @param response
     * @param token
     */
    public static void writeLoginToken(HttpServletResponse response, String token) {
        Cookie ck = new Cookie(COOKIE_NAME, token);
        ck.setDomain(COOKIE_DOMAIN);
        ck.setPath("/");
        ck.setHttpOnly(true);
        ck.setMaxAge(60 * 30);
        response.addCookie(ck);
    }

    /**
     * 获取 token
     * @param request
     * @return
     */
    public static String readLoginToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie ck : cookies) {
               if (StringUtils.equals(ck.getName(), COOKIE_NAME)) {
                   return ck.getValue();
               }
            }
        }
        return null;
    }

    /**
     * 删除 token
     * @param request
     * @param response
     */
    public static void delLoginToken(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie ck : cookies) {
                if (StringUtils.equals(ck.getName(), COOKIE_NAME)) {
                    ck.setDomain(COOKIE_DOMAIN);
                    ck.setPath("/");
                    /* 有效期为0，表示删除 */
                    ck.setMaxAge(0);
                    response.addCookie(ck);
                    return;
                }
            }
        }
    }
}
