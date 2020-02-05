package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisPoolUtil;
import com.mmall.util.RedisSharedPoolUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.jws.soap.SOAPBinding;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/manager/user")
public class UserManagerController {
    @Autowired
    private IUserService iUserService;

    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    public ServerResponse<User> login(String username, String password, HttpServletResponse rsp, HttpSession session) {
        ServerResponse<User> response = iUserService.login(username, password);
        if (response.isSuccess()) {
            User user = response.getData();
            /* 判断权限 */
            if(user.getRole() == Const.Role.ROLE_ADMIN) {
                /* cookie 中写入 token */
                CookieUtil.writeLoginToken(rsp, session.getId());
                /* 保存到 Redis 中 */
                RedisSharedPoolUtil.setEx(
                        session.getId(),
                        JsonUtil.obj2String(response.getData()),
                        Const.RedisCacheExtime.REDIS_SESSION_EXTIME);
                return response;
            } else {
                return ServerResponse.createByErrorMessage("不是管理员，无权限登录");
            }
        }
        return response;
    }
}
