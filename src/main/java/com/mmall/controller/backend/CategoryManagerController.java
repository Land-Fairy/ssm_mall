package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Category;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import com.mmall.service.IUserService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisPoolUtil;
import com.mmall.util.RedisSharedPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sun.security.util.Length;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/manage/category")
public class CategoryManagerController {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private ICategoryService iCategoryService;

    /**
     * 获取同级的品类
     * @param categoryId
     * @return
     */
    @RequestMapping(value = "get_category.do", method = RequestMethod.GET)
    public ServerResponse getCategory(
            @RequestParam(value = "categoryId", defaultValue = "0", required = false) Integer categoryId) {
        return iCategoryService.getCategoryByParentId(categoryId);
    }

    /**
     * 新增分类节点
     * @param parentId
     * @param categoryName
     * @param request
     * @return
     */
    @RequestMapping(value = "add_category.do", method = RequestMethod.POST)
    public ServerResponse<String> addCategory(
            @RequestParam(value = "parentId", defaultValue = "0", required = false) Integer parentId,
            String categoryName, HttpServletRequest request) {
        return iCategoryService.addCategory(parentId, categoryName);
    }

    /**
     * 更新分类名称
     * @param categoryId
     * @param categoryName
     * @return
     */
    @RequestMapping(value = "set_category_name.do", method = RequestMethod.POST)
    public ServerResponse<String> setCategoryName(Integer categoryId, String categoryName) {
        return iCategoryService.setCategoryName(categoryId, categoryName);
    }

    /**
     * 递归获取所有子分类id
     * @param categoryId
     * @return
     */
    @RequestMapping(value = "get_deep_category.do", method = RequestMethod.GET)
    public ServerResponse getDeepCategory(Integer categoryId) {
        return iCategoryService.getDeepCategory(categoryId);
    }


}
