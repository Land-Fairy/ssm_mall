package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;

public interface IProductService {
    ServerResponse<PageInfo> getList(Integer pageNum, Integer pageSize);
    ServerResponse<PageInfo> searchProduct(Integer productId, String productName, Integer pageNum, Integer pageSize);
    ServerResponse<Product> getProductDetail(Integer productId);
    ServerResponse<String> setProductStatus(Integer productId, Integer status);
    ServerResponse<String> saveOrUpdateProduct(Product product);
}
