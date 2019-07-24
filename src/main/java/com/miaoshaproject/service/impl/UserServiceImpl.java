package com.miaoshaproject.service.impl;


import com.miaoshaproject.dao.UserDOMapper;
import com.miaoshaproject.dao.UserPasswordDOMapper;
import com.miaoshaproject.dataobject.UserDO;
import com.miaoshaproject.dataobject.UserPasswordDO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.redis.RedisUtil;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.UserModel;
import com.miaoshaproject.validator.ValidationResult;
import com.miaoshaproject.validator.ValidatorImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDOMapper userDOMapper;

    @Autowired
    private UserPasswordDOMapper userPasswordDOMapper;

    @Autowired
    private ValidatorImpl validator;

    @Autowired
    private RedisUtil redisUtil;

    @Resource
    private RedisTemplate<Object, Object> redisTemplate;

    @Override
    public UserModel getUserById(Integer userId) {
        //默认从redis缓存取出数据
        //UserDO userDO = (UserDO) redisUtil.lGetIndex("userDO", 2);
        UserDO userDO = (UserDO) redisTemplate.opsForValue().get("userDO" + userId);

        if (userDO == null) {
            //调用UserDomapper通过Id获取userdateobject
            userDO = userDOMapper.selectByPrimaryKey(userId);
            if (userDO == null) {
                return null;
            }
            //redisUtil.lSet("userDO", userDO);
            redisTemplate.opsForValue().set("userDO" + userId, userDO);
        }

        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());
        return coverFromDataObject(userDO, userPasswordDO);
    }

    @Transactional
    @Override
    public void register(UserModel userModel) throws BusinessException {
        if (userModel == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }
        /*if (StringUtils.isEmpty(userModel.getName())
                || StringUtils.isEmpty(userModel.getTelphone())
                || userModel.getAge() == null
                || userModel.getGender() == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }*/

        ValidationResult result = validator.validate(userModel);
        if (result.isHasErrors()) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, result.getErrMag());
        }
        //创建一个userdo
        UserDO userDO = coverFromUserModel(userModel);

        //参数合法insert usermodel
        try {
            userDOMapper.insertSelective(userDO);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "该手机号用户已存在");
        }
        userModel.setId(userDO.getId());

        UserPasswordDO userPasswordDO = coverUserPasswordFromUserModel(userModel);
        userPasswordDOMapper.insertSelective(userPasswordDO);
    }

    @Override
    public UserModel validatLogin(String telPhone, String encPassword) throws BusinessException {
        //通过手机号查询相应的userDo
        UserDO userDO = userDOMapper.selectByTelPhone(telPhone);
        if (userDO == null) {
            throw new BusinessException(EmBusinessError.LOGIN_VALIDATION_ERROR);
        }
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());
        UserModel userModel = coverFromDataObject(userDO, userPasswordDO);


        //校验密码是否相同
        if (!StringUtils.equals(encPassword, userModel.getEncrptPassword())) {
            throw new BusinessException(EmBusinessError.LOGIN_VALIDATION_ERROR);
        }

        return userModel;
    }

    /**
     * 创建一个方法生成userPasswordDO
     *
     * @param userModel
     * @return
     */
    private UserPasswordDO coverUserPasswordFromUserModel(UserModel userModel) {
        if (userModel == null) {
            return null;
        }
        UserPasswordDO userPasswordDO = new UserPasswordDO();
        userPasswordDO.setEncrptPassword(userModel.getEncrptPassword());
        userPasswordDO.setUserId(userModel.getId());
        BeanUtils.copyProperties(userModel, userPasswordDO);
        return userPasswordDO;
    }

    //新建一个创建userDO的方法
    private UserDO coverFromUserModel(UserModel userModel) {
        if (userModel == null) {
            return null;
        }
        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(userModel, userDO);
        return userDO;
    }

    //新建一个userDo转userModel方法
    private UserModel coverFromDataObject(UserDO userDO, UserPasswordDO userPasswordDO) {
        if (userDO == null) {
            return null;
        }
        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userDO, userModel);
        if (userPasswordDO != null) {
            userModel.setEncrptPassword(userPasswordDO.getEncrptPassword());
        }
        return userModel;
    }

}
