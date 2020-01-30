package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.security.util.Length;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CategoryServiceImpl implements ICategoryService {
    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public ServerResponse getCategoryByParentId(Integer categoryId) {
        List<Category> categories = categoryMapper.selectByParentId(categoryId);
        return ServerResponse.createBySuccess(categories);
    }

    @Override
    public ServerResponse<String> addCategory(Integer parentId, String categoryName) {
        Category category = new Category();
        category.setParentId(parentId);
        category.setName(categoryName);
        category.setStatus(true);
        int count = categoryMapper.insert(category);
        if (count > 0) {
            return ServerResponse.createBySuccessMessage("添加成功");
        }
        return ServerResponse.createByErrorMessage("添加失败");
    }

    @Override
    public ServerResponse<String> setCategoryName(Integer categoryId, String categoryName) {
        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);
        int count = categoryMapper.updateByPrimaryKeySelective(category);
        if (count > 0) {
            return ServerResponse.createBySuccessMessage("更新成功");
        }
        return ServerResponse.createByErrorMessage("更新失败");
    }

    @Override
    public ServerResponse getDeepCategory(Integer categoryId) {
        List<Integer> deepIds = new ArrayList<>();
        List<Integer> parentIds = new ArrayList<>();
        parentIds.add(categoryId);

        for (;;) {
           if (parentIds.isEmpty()) {
               break;
           }

           Integer id = parentIds.get(0);
           parentIds.remove(0);
           List<Integer> childCategoryIds = getChildCategory(id);
           if (childCategoryIds == null || childCategoryIds.size() == 0) {
               continue;
           }

           for (Integer childId : childCategoryIds) {
               if (!deepIds.contains(childId)) {
                 deepIds.add(childId);
               }
               parentIds.add(childId);
           }
        }

        return ServerResponse.createBySuccess("成功", deepIds);
    }

    /**
     * 判断是否是管理员
     * @param session
     * @return
     */
    @Override
    public Boolean isAdmin(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return false;
        }
        /* 判断权限 */
        if(user.getRole() == Const.Role.ROLE_ADMIN) {
            return true;
        }
        return false;
    }

    public List<Integer> getChildCategory(Integer categoryId) {
        return categoryMapper.selectIdsByParentId(categoryId);
    }


}
