package com.mmall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IOrderService;
import com.mmall.service.IUserService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisPoolUtil;
import com.mmall.util.RedisSharedPoolUtil;
import com.mmall.vo.OrderVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/manage/order")
public class OrderManagerController {

    @Autowired
    private IUserService iUserService;
    @Autowired
    private IOrderService iOrderService;

    /**
     * 获取订单列表
     * @param request
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "list.do", method = RequestMethod.GET)
    public ServerResponse<PageInfo> orderList(HttpServletRequest request,
                                              @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                              @RequestParam(value = "pageSize",defaultValue = "10")int pageSize){

        String loginToken = CookieUtil.readLoginToken(request);
        if (StringUtils.isEmpty(loginToken)) {
            return ServerResponse.createByErrorMessage("用户未登录，无法获取当前用户的信息");
        }

        String s = RedisSharedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(s, User.class);
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录管理员");

        }
        if(iUserService.checkAdminRole(user).isSuccess()){
            //填充我们增加产品的业务逻辑
            return iOrderService.manageList(pageNum,pageSize);
        }else{
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

    /**
     * 获取订单详情
     * @param request
     * @param orderNo
     * @return
     */
    @RequestMapping(value = "detail.do", method = RequestMethod.GET)
    public ServerResponse<OrderVo> orderDetail(HttpServletRequest request, Long orderNo){

        return iOrderService.manageDetail(orderNo);
    }


    /**
     * 搜索订单
     * @param request
     * @param orderNo
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "search.do", method = RequestMethod.GET)
    public ServerResponse<PageInfo> orderSearch(HttpServletRequest request, Long orderNo,
                                                @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                                @RequestParam(value = "pageSize",defaultValue = "10")int pageSize){
        return iOrderService.manageSearch(orderNo,pageNum,pageSize);
    }


    /**
     * 设置订单状态为 发货
     * @param request
     * @param orderNo
     * @return
     */
    @RequestMapping(value = "send_goods.do", method = RequestMethod.POST)
    public ServerResponse<String> orderSendGoods(HttpServletRequest request, Long orderNo){
        return iOrderService.manageSendGoods(orderNo);
    }
}
