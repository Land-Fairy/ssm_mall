package com.mmall.dao;

import com.mmall.pojo.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    /* 检查用户名是否存在 */
    int checkUsername(String username);

    /* 根据 用户名 + 密码，查询用户 */
    User selectLogin(@Param("username") String username,
                     @Param("password") String password);
}