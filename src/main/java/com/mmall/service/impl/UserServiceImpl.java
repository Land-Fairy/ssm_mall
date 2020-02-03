package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import com.mmall.util.RedisPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.print.attribute.standard.JobOriginatingUserName;
import java.util.UUID;

@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {
        int resultCount = userMapper.checkUsername(username);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("用户名不存在");
        }

        /* TODO: 密码登录 MD5 */
        password = MD5Util.MD5EncodeUtf8(password);

        User user = userMapper.selectLogin(username, password);
        if (user == null) {
            return ServerResponse.createByErrorMessage("密码错误");
        }

        /* 将密码 设置为空，防止返回过去 */
        user.setPassword(StringUtils.EMPTY);

        return ServerResponse.createBySuccess("登录成功", user);
    }

    @Override
    public ServerResponse<String> register(User user) {
        /* 校验用户名是否存在 */
        int resultCount = userMapper.checkUsername(user.getUsername());
        if (resultCount > 0) {
            return ServerResponse.createByErrorMessage("用户名已存在");
        }

        /* 校验邮箱是否已经存在 */
        resultCount = userMapper.checkEmail(user.getEmail());
        if (resultCount > 0) {
            return ServerResponse.createByErrorMessage("邮箱已存在");
        }

        user.setRole(Const.Role.ROLE_CUSTOMER);

        /* MD5加密 */
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));

        /* 插入用户 */
        resultCount = userMapper.insert(user);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("注册侧失败");
        }
        return ServerResponse.createBySuccessMessage("注册成功");
    }

    @Override
    public ServerResponse<String> checkValid(String str, String type) {
        if (org.apache.commons.lang3.StringUtils.isNotBlank(type)) {
            if (Const.USERNAME.equals(type)) {
                int resultCount = userMapper.checkUsername(str);
                if (resultCount > 0) {
                    return ServerResponse.createByErrorMessage("用户名已存在");
                }
            }

            if (Const.EMAIL.equals(type)) {
                /* 校验邮箱是否已经存在 */
                int resultCount = userMapper.checkEmail(str);
                if (resultCount > 0) {
                    return ServerResponse.createByErrorMessage("邮箱已存在");
                }
            }
        } else {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        return ServerResponse.createBySuccessMessage("校验成功");
    }

    /**
     * 获取用户名对应的忘记密码问题
     * @param username
     * @return
     */
    @Override
    public ServerResponse<String> selectQuestion(String username) {
        /* 判断用户名是否存在 */
        int resultCount = userMapper.checkUsername(username);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("用户名不存在");
        }

        String question = userMapper.selectQuestionByUsername(username);
        if (org.apache.commons.lang3.StringUtils.isNotBlank(question)) {
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByErrorMessage("忘记密码问题为空");
    }

    /**
     * 校验忘记密码问题 与答案
     * @param username
     * @param question
     * @param answer
     * @return
     */
    @Override
    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
        int resultCount = userMapper.checkAnswer(username, question, answer);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("问题答案错误");
        }
        /* 获取token，并将token 存放在 TokenCache 中 */
        String token = UUID.randomUUID().toString();
        RedisPoolUtil.setEx(Const.TOKEN_PREFIX+username, token, 60 *60 * 12);
        return ServerResponse.createBySuccess(token);
    }

    @Override
    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String token) {
        /* token 判断 */
        if (org.apache.commons.lang3.StringUtils.isBlank(token)) {
            return ServerResponse.createByErrorMessage("参数错误，需要传递 Token");
        }

        /* 判断用户是否存在
        * 防止 username为空， cache Key 为 TOKEN_PREFIX
        * */
        int resultCount = userMapper.checkUsername(username);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("用户名不存在");
        }

        /* 从缓存中获取key */
        String cachedToken = RedisPoolUtil.get(Const.TOKEN_PREFIX + username);
        if (StringUtils.isBlank(cachedToken)) {
            return ServerResponse.createByErrorMessage("Token 无效或者已经过期");
        }

        if (StringUtils.equals(cachedToken, token)) {
            String passwordMd5 = MD5Util.MD5EncodeUtf8(passwordNew);
            int rowCount = userMapper.updatePasswordByUsername(username, passwordMd5);
            if (rowCount > 0) {
                return ServerResponse.createBySuccessMessage("修改密码成功");
            }
        } else {
            return ServerResponse.createByErrorMessage("token不匹配，请重新获取");
        }

        return ServerResponse.createByErrorMessage("修改密码失败");
    }

    @Override
    public ServerResponse<String> resetPassword(String passwordNew, String passwordOld, User user) {
        int count = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld), user.getId());
        if (count == 0) {
            return ServerResponse.createByErrorMessage("旧密码错误");
        }

        /* 设置新密码 */
        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));

        count = userMapper.updateByPrimaryKeySelective(user);
        if (count == 0) {
            return ServerResponse.createByErrorMessage("密码更新失败");
        }
        return ServerResponse.createBySuccessMessage("密码更新成功");
    }

    @Override
    public ServerResponse<User> updateUserInfo(User user) {
        /* Username 不可修改 */
        /* 检查 Email */
        int count = userMapper.checkEmailByUserId(user.getEmail(), user.getId());
        if (count > 0) {
            return ServerResponse.createByErrorMessage("Email 已经存在");
        }

        /* 另外创建一个 session， 只更新这些字段 */
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());

        count = userMapper.updateByPrimaryKeySelective(updateUser);
        if (count > 0) {
            return ServerResponse.createBySuccess("更新个人用户信息成功", updateUser);
        }

        return ServerResponse.createByErrorMessage("更新个人用户信息失败");
    }

    @Override
    public ServerResponse<User> getUserDetialInfo(Integer userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }

    @Override
    public ServerResponse checkAdminRole(User user) {
        if(user != null && user.getRole().intValue() == Const.Role.ROLE_ADMIN){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }


}
