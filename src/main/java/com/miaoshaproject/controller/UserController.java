package com.miaoshaproject.controller;

import com.alibaba.druid.util.StringUtils;
import com.miaoshaproject.controller.viewModel.UserVO;
import com.miaoshaproject.error.BusinessException;
import com.miaoshaproject.error.EmBusinessError;
import com.miaoshaproject.redis.RedisUtil;
import com.miaoshaproject.response.CommonReturnType;
import com.miaoshaproject.service.UserService;
import com.miaoshaproject.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * 用户controller
 */
@RestController
@RequestMapping("/user")
@CrossOrigin(allowCredentials = "true", allowedHeaders = "*")
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    @Autowired
    private HttpServletRequest request;

    /**
     * 根据手机号获取验证码
     *
     * @param telphone
     * @return
     */
    @RequestMapping(value = "/getOtp", method = RequestMethod.POST, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType getOtp(@RequestParam(name = "telphone") String telphone) {
        //生成10000到99999的随机数
        Random random = new Random();
        int randomInt = random.nextInt(99999);
        randomInt += 10000;
        String otpCode = String.valueOf(randomInt);
        //将生成的otp与之对应的手机号存入session
        request.getSession().setAttribute(telphone, otpCode);

        // 向该手机号发送otp信息
        //SendMsgUtil.SendMsg(telphone,otpCode);
        System.out.printf("telPhone:" + telphone + "&otpCode:" + otpCode);
        return CommonReturnType.create(null);
    }

    /**
     * 根据UserID获取用户信息
     *
     * @param id
     * @return
     * @throws BusinessException
     */
    @RequestMapping("/get")
    public CommonReturnType getUser(@RequestParam Integer id) throws BusinessException {
        //通过userService获取userModel
        UserModel userModel = userService.getUserById(id);
        if (userModel == null) {
            throw new BusinessException(EmBusinessError.USER_NOT_EXIST);
        }
        UserVO userVO = coverFromObject(userModel);
        return CommonReturnType.create(userVO);
    }

    /**
     * 用户登录
     * @param telphone
     * @param password
     * @return
     * @throws BusinessException
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType login(@RequestParam String telphone, @RequestParam String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        //入参校验
        if (StringUtils.isEmpty(telphone) || StringUtils.isEmpty(password)) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }
        //验证登录信息
       UserModel userModel  = userService.validatLogin(telphone, this.encodeMd5(password));
        //将用户信息保存到session
        request.getSession().setAttribute("IS_LOGIN",true);
        request.getSession().setAttribute("LOGIN_USERINFO",userModel);

        return CommonReturnType.create(null);
    }

    /**
     * 用户注册
     *
     * @param otpCode
     * @param telPhone
     * @param age
     * @param name
     * @param gender
     * @return
     */
    @RequestMapping(value = "/register", method = RequestMethod.POST, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType register(@RequestParam("otpCode") String otpCode, @RequestParam("telPhone") String telPhone, @RequestParam("age") Integer age
            , @RequestParam("name") String name, @RequestParam("gender") Integer gender, @RequestParam("password") String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        //获取出session中的otpcode

        String otpcodeSession = (String) request.getSession().getAttribute(telPhone);
        if (otpcodeSession == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "该手机号错误，请重新发送验证码");
        }
        if (!StringUtils.equals(otpcodeSession, otpCode)) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "验证码不一致");
        }
        UserModel userModel = new UserModel();
        userModel.setName(name);
        userModel.setTelphone(telPhone);
        userModel.setAge(age);
        userModel.setGender(gender.byteValue());
        userModel.setEncrptPassword(this.encodeMd5(password));

        userService.register(userModel);

        return CommonReturnType.create(null);
    }

    /**
     * 获取64位MD5加密密码
     *
     * @param str
     * @return
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    public  String encodeMd5(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        BASE64Encoder bse64 = new BASE64Encoder();
        String newStr = bse64.encode(md5.digest(str.getBytes("utf-8")));
        return newStr;
    }

    /**
     * 生成对应用户展示层的模型
     *
     * @param userModel
     * @return
     */
    private UserVO coverFromObject(UserModel userModel) {
        if (userModel == null) {
            return null;
        }
        UserVO userVO = new UserVO();

        BeanUtils.copyProperties(userModel, userVO);
        return userVO;
    }

}
