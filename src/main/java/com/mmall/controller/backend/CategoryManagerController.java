package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Category;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import sun.security.util.Length;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/backend/category/")
public class CategoryManagerController {

    @Autowired
    private ICategoryService iCategoryService;

    /**
     * 获取同级的品类
     * @param categoryId
     * @param session
     * @return
     */
    @RequestMapping(value = "get_category.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse getCategory(
            @RequestParam(value = "categoryId", defaultValue = "0", required = false) Integer categoryId,
            HttpSession session) {
        /* 权限鉴定 */
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCategoryService.getCategoryByParentId(categoryId);
    }

    /**
     * 新增分类节点
     * @param parentId
     * @param categoryName
     * @param session
     * @return
     */
    @RequestMapping(value = "add_category.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> addCategory(
            @RequestParam(value = "parentId", defaultValue = "0", required = false) Integer parentId,
            String categoryName, HttpSession session) {
        /* 权限鉴定 */
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }

        if (StringUtils.isBlank(categoryName)) {
            return ServerResponse.createByErrorMessage("分类名称不能为空");
        }

        return iCategoryService.addCategory(parentId, categoryName);
    }

    /**
     * 更新分类名称
     * @param categoryId
     * @param categoryName
     * @param session
     * @return
     */
    @RequestMapping(value = "set_category_name.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> setCategoryName(Integer categoryId, String categoryName, HttpSession session) {
        /* 权限鉴定 */
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }

        if (StringUtils.isBlank(categoryName)) {
            return ServerResponse.createByErrorMessage("分类名称不能为空");
        }
        return iCategoryService.setCategoryName(categoryId, categoryName);

    }

    /**
     * 递归获取所有子分类id
     * @param categoryId
     * @param session
     * @return
     */
    @RequestMapping(value = "get_deep_category.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse getDeepCategory(Integer categoryId, HttpSession session) {
        /* 权限鉴定 */
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),
                    ResponseCode.NEED_LOGIN.getDesc());
        }

        return iCategoryService.getDeepCategory(categoryId);
    }


}
