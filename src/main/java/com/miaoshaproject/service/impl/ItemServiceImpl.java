package com.miaoshaproject.service.impl;

import com.miaoshaproject.dao.ItemDOMapper;
import com.miaoshaproject.dao.ItemStockDOMapper;
import com.miaoshaproject.dataobject.ItemDO;
import com.miaoshaproject.dataobject.ItemStockDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.PromoService;
import com.miaoshaproject.service.model.ItemModel;
import com.miaoshaproject.service.model.PromoModel;
import com.miaoshaproject.validator.ValidationResult;
import com.miaoshaproject.validator.ValidatorImpl;
import org.hibernate.annotations.Source;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ValidatorImpl validator;

    /**
     * itemmapper
     */
    @Autowired
    private ItemDOMapper itemDOMapper;

    @Autowired
    private ItemStockDOMapper itemStockDOMapper;

    @Autowired
    private PromoService promoService;

    @Resource
    private RedisTemplate<Object, Object> redisTemplate;


    /**
     * 通过id获取商品
     *
     * @param id
     * @return
     */
    @Override
    public ItemModel getItemById(Integer id) {
        //校验入参
        ItemDO itemDO = itemDOMapper.selectByPrimaryKey(id);
        if (itemDO == null) {
            return null;
        }

        //查询库存信息
        ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());

        //将商品和库存转化成实体
        ItemModel itemModel = this.conventItemModelFromItemDO(itemDO, itemStockDO);

        //通过itemId，查看是否有活动
        PromoModel promoModel = promoService.getPromoByItemId(itemDO.getId());
        if (promoModel != null && promoModel.getStatus() != 3) {
            itemModel.setPromoModel(promoModel);
        }
        return itemModel;
    }

    /**
     * 创建商品
     *
     * @param itemModel
     * @return
     * @throws BusinessException
     */
    @Override
    @Transactional
    public ItemModel creatItem(ItemModel itemModel) throws BusinessException {
        //校验入参
        ValidationResult result = validator.validate(itemModel);
        if (result.isHasErrors()) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }
        //转化itemModel->dataobject
        ItemDO itemDO = this.coverItemDOlFormModel(itemModel);

        //写入数据库
        itemDOMapper.insertSelective(itemDO);
        itemModel.setId(itemDO.getId());

        //获取itemstockDO

        ItemStockDO itemStockDO = this.coverItemStockDOFromItemModel(itemModel);
        itemStockDOMapper.insertSelective(itemStockDO);


        return this.getItemById(itemModel.getId());
    }

    /**
     * 获取商品列表
     *
     * @return
     */
    @Override
    public List<ItemModel> listItem() {
        //先从redis缓存读取数据
        List<ItemDO> itemDOList = (List<ItemDO>) redisTemplate.opsForValue().get("allItem");
        if (itemDOList == null) {
            //双重验证
            synchronized (this) {
                itemDOList = (List<ItemDO>) redisTemplate.opsForValue().get("allItem");
                if (itemDOList == null) {
                    System.out.println("数据库读取6666");

                    //去数据库查询所以商品
                    itemDOList = itemDOMapper.listItem();

                    //将数据库查询的商品放入redis缓存
                    redisTemplate.opsForValue().set("allItem", itemDOList);
                }else{
                    System.out.println("缓存读取2222");
                }
            }

        }else{
            System.out.println("缓存读取1111");
        }

        List<ItemModel> itemModelList = itemDOList.stream().map(itemDO -> {
            ItemStockDO itemStockDO = itemStockDOMapper.selectByItemId(itemDO.getId());
            ItemModel itemModel = this.conventItemModelFromItemDO(itemDO, itemStockDO);
            return itemModel;
        }).collect(Collectors.toList());
        return itemModelList;
    }

    /**
     * 库存扣减
     *
     * @param itemId
     * @param amount
     * @return
     */
    @Override
    @Transactional
    public boolean decreaseStock(Integer itemId, Integer amount) {
        int affectedRow = itemStockDOMapper.decreaseStock(itemId, amount);
        if (affectedRow > 0) {
            //减库存成功
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public boolean increaseSales(Integer itemId, Integer amount) {
        int affectedRow = itemDOMapper.increaseSales(itemId, amount);
        if (affectedRow > 0) {
            return true;
        }
        return false;
    }

    /**
     * itemModel转化itemDo
     *
     * @param itemModel
     * @return
     * @throws BusinessException
     */
    public ItemDO coverItemDOlFormModel(ItemModel itemModel) throws BusinessException {
        if (itemModel == null) {
            return null;
        }
        ItemDO itemDO = new ItemDO();
        BeanUtils.copyProperties(itemModel, itemDO);
        itemDO.setPrice(itemModel.getPrice().doubleValue());
        return itemDO;
    }

    /**
     * itemmodel转itemstockDO
     *
     * @param itemModel
     * @return
     */
    public ItemStockDO coverItemStockDOFromItemModel(ItemModel itemModel) {
        if (itemModel == null) {
            return null;
        }
        ItemStockDO itemStockDO = new ItemStockDO();
        itemStockDO.setItemId(itemModel.getId());
        itemStockDO.setStock(itemModel.getStock());
        return itemStockDO;
    }

    public ItemModel conventItemModelFromItemDO(ItemDO itemDO, ItemStockDO itemStockDO) {
        if (itemDO == null || itemStockDO == null) {
            return null;
        }
        ItemModel itemModel = new ItemModel();

        BeanUtils.copyProperties(itemDO, itemModel);
        itemModel.setPrice(new BigDecimal(itemDO.getPrice()));
        itemModel.setStock(itemStockDO.getStock());
        return itemModel;

    }
}
