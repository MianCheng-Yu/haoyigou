package com.miaoshaproject.service;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.service.model.OrderModel;

public interface OrderService {
    //1.页面传入活动id，后台校验活动真是性
    //2.下单接口获取商品的秒杀信息，直接获取活动信息

    OrderModel creatOrder(Integer userId, Integer promoId, Integer itemId, Integer amount) throws BusinessException;
}
