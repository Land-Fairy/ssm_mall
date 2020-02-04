package com.mmall.dao;

import com.mmall.pojo.Product;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProductMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Product record);

    int insertSelective(Product record);

    Product selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Product record);

    int updateByPrimaryKey(Product record);

    List<Product> selectList();

    /* 考虑会有商品被删除情况，因此需要标识 null 使用 Integer 而非 int */
    Integer selectStockByProductId(Integer id);

    List<Product> selectByNameAndProductId(@Param("productName") String productName,
                                           @Param("productId") Integer productId);
}