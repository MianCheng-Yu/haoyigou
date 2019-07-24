package com.miaoshaproject.service;

import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.service.model.UserModel;

public interface UserService{
    UserModel getUserById(Integer userId);

    void register(UserModel userModel) throws BusinessException;

    UserModel validatLogin(String telPhone,String encPassword) throws BusinessException;
}
