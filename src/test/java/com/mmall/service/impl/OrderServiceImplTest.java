package com.mmall.service.impl;

import com.mmall.common.ServerResponse;
import com.mmall.service.IOrderService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext.xml")
public class OrderServiceImplTest {

    @Autowired
    private IOrderService iOrderService;

    @Test
    public void pay() {
        ServerResponse pay = iOrderService.pay(1491753014256L, 1, "/Users/ios/16_tmp");
        System.out.println("pay---------:" + pay.getData().toString());
    }
}