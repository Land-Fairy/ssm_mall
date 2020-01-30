package com.mmall.service;

import com.mmall.common.ServerResponse;

import javax.servlet.http.HttpSession;

public interface ICategoryService {
    ServerResponse getCategoryByParentId(Integer categoryId);
    ServerResponse<String> addCategory(Integer parentId, String categoryName);
    ServerResponse<String> setCategoryName(Integer categoryId, String categoryName);
    ServerResponse getDeepCategory(Integer categoryId);
    Boolean isAdmin(HttpSession session);
}
