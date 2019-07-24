package com.miaoshaproject.service;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.service.model.ItemModel;

import java.util.List;

public interface ItemService {

    //通过id获取商品
    ItemModel getItemById(Integer id);

    //创建商品
    ItemModel creatItem(ItemModel itemModel) throws BusinessException;

    //商品列表浏览
    List<ItemModel> listItem();

    //库存扣减

    boolean decreaseStock(Integer itemId,Integer amount);

    //下单成功后增加商品销量

    boolean increaseSales(Integer itemId,Integer amount);
}
