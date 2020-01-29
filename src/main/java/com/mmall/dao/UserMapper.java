package com.mmall.dao;

import com.mmall.pojo.User;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.scheduling.support.SimpleTriggerContext;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    /* 检查用户名是否存在 */
    int checkUsername(String username);

    /* 检查邮箱是否已经存在*/
    int checkEmail(String email);

    /* 根据 用户名 + 密码，查询用户 */
    User selectLogin(@Param("username") String username,
                     @Param("password") String password);

    /* 根据用户名，获取忘记密码问题 */
    String selectQuestionByUsername(String username);

    int checkAnswer(@Param("username") String username,
                    @Param("question") String question,
                    @Param("answer") String answer);

    /* 根据用户名，更新密码 */
    int updatePasswordByUsername(@Param("username") String username,
                                 @Param("password")String password);

    /* 检查密码 */
    int checkPassword(@Param("password") String password,
                      @Param("userId") Integer userId);

    int checkEmailByUserId(@Param("email") String email,
                           @Param("userId") Integer userId);
}