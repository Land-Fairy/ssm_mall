package com.mmall.service;

import com.mmall.common.ServerResponse;

import java.util.Map;

public interface IOrderService {
    ServerResponse pay(Long orderNo, Integer userId, String path);
    ServerResponse<Integer> queryOrderPayStatus(Integer userId, Long orderNo);
    ServerResponse aliCallback(Map<String, String> params);

}
