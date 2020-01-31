package com.mmall.common;

import org.apache.commons.lang3.StringUtils;

public class Const {
    public static final String CURRENT_USER = "current_user";

    public static final String EMAIL = "email";
    public static final String USERNAME = "username";

    /* 使用 内部接口方式，比枚举更加轻量 */
    public interface Role {
        int ROLE_CUSTOMER = 0; /* 用户 */
        int ROLE_ADMIN = 1; /* 管理员 */
    }

    public interface Cart{
        int CHECKED = 1;//即购物车选中状态
        int UN_CHECKED = 0;//购物车中未选中状态

        String LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL";
        String LIMIT_NUM_SUCCESS = "LIMIT_NUM_SUCCESS";
    }

}
