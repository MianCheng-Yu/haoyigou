package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.OrderDOMapper;
import com.miaoshaproject.dao.SequenceDOMapper;
import com.miaoshaproject.dataobject.OrderDO;
import com.miaoshaproject.dataobject.SequenceDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.OrderService;
import com.miaoshaproject.service.PromoService;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.ItemModel;
import com.miaoshaproject.service.model.OrderModel;
import com.miaoshaproject.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderDOMapper orderDOMapper;

    @Autowired
    private SequenceDOMapper sequenceDOMapper;

    @Autowired
    private PromoService promoService;

    @Override
    @Transactional
    public OrderModel creatOrder(Integer userId, Integer promoId, Integer itemId, Integer amount) throws BusinessException {
        //1.校验下单状态 商品是否存在 用户是否合法 购买数量是否正确
        ItemModel itemModel = itemService.getItemById(itemId);
        if (itemModel == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "商品信息不合法");
        }

        UserModel userModel = userService.getUserById(userId);
        if (userId == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "用户信息不合法");
        }

        //校验活动信息
        if (promoId != null) {
            // promoService.getPromoByItemId(itemId);
            //校验活动的是否和当前下单商品匹配
            if (promoId.intValue() != itemModel.getPromoModel().getId()) {
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "活动商品不匹配");
            }else if (itemModel.getPromoModel().getStatus() != 2) {
                //活动是否进行中
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "活动商品不匹配");
            }
        }

        //2.落单减库存
        boolean result = itemService.decreaseStock(itemId, amount);
        if (!result) {
            throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);
        }

        //订单入库
        OrderModel orderModel = new OrderModel();
        orderModel.setAmount(amount);
        orderModel.setItemId(itemId);
        orderModel.setPromoId(promoId);
        orderModel.setUserId(userId);
        if(promoId != null) {
            orderModel.setItemPrice(itemModel.getPromoModel().getPromoPrice());
        } else {
            orderModel.setItemPrice(itemModel.getPrice());
        }
        orderModel.setOrderPrice(orderModel.getItemPrice().multiply(BigDecimal.valueOf(amount)));


        //设置订单号
        orderModel.setId(generateOrderNo());

        OrderDO orderDO = this.conventOrderDOFromOrderModel(orderModel);
        orderDOMapper.insertSelective(orderDO);

        //增加销量
        itemService.increaseSales(itemId, amount);

        //返回前端
        return orderModel;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)//新事务，如回滚仍然处理掉
    public String generateOrderNo() {

        //订单号为16位
        StringBuffer stringBuffer = new StringBuffer();

        //前8位为时间数字 年月日
        LocalDateTime now = LocalDateTime.now();
        String nowDate = now.format(DateTimeFormatter.ISO_DATE).replace("-", "");
        stringBuffer.append(nowDate);

        //中间为6为自增序列
        int sequence = 0;
        SequenceDO sequenceDO = sequenceDOMapper.selectByPrimaryKey("order_info");
        sequence = sequenceDO.getCurrentValue();//获取当前的序列值
        sequenceDO.setCurrentValue(sequenceDO.getCurrentValue() + sequenceDO.getStep());//自增一个序列值 当前值+自增值
        sequenceDOMapper.updateByPrimaryKey(sequenceDO);
        String sequenceStr = String.valueOf(sequence);
        for (int i = 0; i < 6 - sequenceStr.length(); i++) {
            stringBuffer.append(0);
        }

        stringBuffer.append(sequenceStr);
        //最后两位分库分表 暂时写死
        stringBuffer.append("00");
        return stringBuffer.toString();
    }

    public OrderDO conventOrderDOFromOrderModel(OrderModel orderModel) {
        if (orderModel == null) {
            return null;
        }
        OrderDO orderDO = new OrderDO();
        BeanUtils.copyProperties(orderModel, orderDO);
        return orderDO;
    }

}
