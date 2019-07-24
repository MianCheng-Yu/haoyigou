package com.miaoshaproject.controller;

import com.miaoshaproject.controller.viewModel.ItemVO;
import com.miaoshaproject.dataobject.ItemDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.ItemService;
import com.miaoshaproject.service.model.ItemModel;
import com.miaoshaproject.service.model.PromoModel;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/item")
@CrossOrigin(allowCredentials = "true", allowedHeaders = "*")
public class ItemController extends BaseController {

    @Autowired
    private ItemService itemService;

    /**
     * 新增商品
     *
     * @param title
     * @param description
     * @param price
     * @param stock
     * @param imgUrl
     * @return
     * @throws BusinessException
     */
    @RequestMapping(value = "/creatItem", method = RequestMethod.POST, consumes = {CONTENT_TYPE_FORMED})
    public CommonReturnType creatItem(@RequestParam(name = "title") String title,
                                      @RequestParam(name = "description") String description,
                                      @RequestParam(name = "price") BigDecimal price,
                                      @RequestParam(name = "stock") Integer stock,
                                      @RequestParam(name = "imgUrl") String imgUrl) throws BusinessException {

        ItemModel itemModel = new ItemModel();
        itemModel.setTitle(title);
        itemModel.setDescription(description);
        itemModel.setPrice(price);
        itemModel.setImgUrl(imgUrl);
        itemModel.setStock(stock);

        ItemModel itemModelReturn = itemService.creatItem(itemModel);
        return CommonReturnType.create(itemModelReturn);
    }

    /**
     * 获取商品详情
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/getItem", method = RequestMethod.GET)
    public CommonReturnType getItemById(@RequestParam(name = "id") Integer id) throws BusinessException {
        if (id == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }
        ItemModel itemModel = itemService.getItemById(id);
        ItemVO itemVO = this.conventItemVOFromItemModel(itemModel);
        return CommonReturnType.create(itemVO);
    }

    /**
     * 获取商品列表
     *
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public CommonReturnType getItemList() {

        List<ItemModel> itemModels = itemService.listItem();
        //使用stream apij将itemModel转化成ItemVO
        List<ItemVO> itemVOList = itemModels.stream().map(itemModel -> {
            ItemVO itemVO = this.conventItemVOFromItemModel(itemModel);
            return itemVO;
        }).collect(Collectors.toList());

        return CommonReturnType.create(itemVOList);

    }

    /**
     * 获取商品列表
     *
     * @return
     */
    @RequestMapping(value = "/test_list", method = RequestMethod.GET)
    public List<ItemModel> testList() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                itemService.listItem();
            }
        };
        ExecutorService executorService = Executors.newFixedThreadPool(20);
        for (int i = 0; i <10000 ; i++) {
            executorService.submit(runnable);
        }

        return itemService.listItem();

    }



    public ItemVO conventItemVOFromItemModel(ItemModel itemModel) {
        if (itemModel == null) {
            return null;
        }
        ItemVO itemVO = new ItemVO();

        BeanUtils.copyProperties(itemModel, itemVO);
        PromoModel promoModel = itemModel.getPromoModel();
        if(promoModel!=null){
            itemVO.setPromoPrice(promoModel.getPromoPrice());
            itemVO.setPromoId(promoModel.getId());
            itemVO.setStartTime(promoModel.getStartDate().toString(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss ")));
            itemVO.setPromoStatus(promoModel.getStatus());
        }
        return itemVO;
    }
}
