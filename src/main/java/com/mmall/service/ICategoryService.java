package com.mmall.service;

import com.mmall.common.ServerResponse;

public interface ICategoryService {
    ServerResponse getCategoryByParentId(Integer categoryId);
    ServerResponse<String> addCategory(Integer parentId, String categoryName);
    ServerResponse<String> setCategoryName(Integer categoryId, String categoryName);
    ServerResponse getDeepCategory(Integer categoryId);
}
